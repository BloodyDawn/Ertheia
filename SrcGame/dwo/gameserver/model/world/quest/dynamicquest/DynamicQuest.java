package dwo.gameserver.model.world.quest.dynamicquest;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.DynamicQuestManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExDynamicQuest;
import gnu.trove.map.hash.TIntIntHashMap;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * L2GOD Team
 * User: Yorie, ANZO
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class DynamicQuest extends Quest
{
	private DynamicQuestTemplate _template;
	private Map<Integer, L2PcInstance> _participiants = new FastMap<Integer, L2PcInstance>().shared();
	private boolean _questStarted;
	private Future<?> _inviteTask;
	private Future<?> _endTask;
	private int _currentPoints;
	private TIntIntHashMap _pointsByMembers = new TIntIntHashMap();
	private List<String> _winners = new FastList();
	private List<String> _eliteWinners = new FastList();
	private List<DynamicQuestResult> _results = new FastList();
	private long _lastResultUpdate;
	/**
	 * Здесь фиксируем различное время начала (старт квеста, таск на старт через 3 минуты и прочее)
	 */
	private long _initialTime;

	public DynamicQuest(DynamicQuestTemplate quest)
	{
		super(quest.getTaskId(), quest.getQuestName(), "");
		_template = quest;

		if(_template == null)
		{
			_log.log(Level.WARN, "Trying to start dynamic quest (campain or zone quest) with empty template!");
			return;
		}

		if(Config.DYNAMIC_QUEST_SYSTEM)
		{
			DynamicQuestManager.getInstance().addQuest(this);

			init();
		}
	}

	private void init()
	{
		if(_template.isCampain())
		{
			for(DynamicQuestTemplate.DynamicQuestDate startDate : _template.getStartDates())
			{
				long scheduleTime = startDate.getNextScheduleTime() - System.currentTimeMillis();

				if(scheduleTime <= 0)
				{
					startQuest();
				}
				else
				{
					_initialTime = System.currentTimeMillis() + scheduleTime;
					ThreadPoolManager.getInstance().scheduleGeneral(this::startQuest, scheduleTime);

					// Шедулим квест на следующую неделю
					ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this::startQuest, scheduleTime, 7 * 24 * 60 * 60 * 1000);
				}
			}
		}
		else if(_template.isZoneQuest())
		{
			if(_template.isAutostart())
			{
				startQuest();
			}
		}
	}

	/**
	 * Стартуем квест. Аттачим зоны и NPC, запускаем задание на завершение квеста.
	 */
	public void startQuest()
	{
		if(_questStarted)
		{
			return;
		}

		onCampainStart();

		if(_template.getSpawnHolder() != null)
		{
			_template.getSpawnHolder().spawnAll();
		}

		if(_template.getAllZones() != null)
		{
			for(L2ZoneType zone : _template.getAllZones())
			{
				addEnterZoneId(zone.getId());
				addExitZoneId(zone.getId());
			}
		}

		_lastResultUpdate = 0;
		_results.clear();
		_participiants.clear();
		_winners.clear();
		_eliteWinners.clear();
		_currentPoints = 0;
		_pointsByMembers.clear();

		_template.getAllPoints().keySet().forEach(this::addKillId);

		_questStarted = true;

		if(_template.isCampain())
		{
			if(_inviteTask == null)
			{
				_inviteTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this::invitePlayers, 0, 5 * 60 * 1000);
			}
		}
		else
		{
			invitePlayers();
		}

		_initialTime = System.currentTimeMillis() + _template.getDuration() * 60 * 1000;
		if(_endTask == null)
		{
			_endTask = ThreadPoolManager.getInstance().scheduleGeneral(() -> endQuest(true), _template.getDuration() * 60 * 1000);
		}

		// TODO
		//_log.log(Level.INFO, getClass().getSimpleName() + ": Dynamic quest " + _template.getQuestName() + " started!");
	}

	/**
	 * Приглашение игроков со всего мира на кампанию
	 */
	public void invitePlayers()
	{
		if(!_questStarted)
		{
			return;
		}

		if(_template.isCampain())
		{
			for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
			{
				if(player.getLevel() >= _template.getMinLevel() && !_participiants.containsKey(player.getObjectId()))
				{
					player.sendPacket(new ExDynamicQuest(player, _template.isCampain(), _template.getQuestId(), DynamicQuestManager.getStepId(_template.getTaskId())).startCampain());
				}
			}
		}
		else
		{
			for(L2ZoneType zone : _template.getAllZones())
			{
				zone.getPlayersInside().stream().filter(player -> player.getParty() != null || player.isGM()).forEach(player -> {
					player.sendPacket(new ExDynamicQuest(player, _template.isCampain(), _template.getQuestId(), DynamicQuestManager.getStepId(_template.getTaskId())).startCampain());
					if(DynamicQuestManager.getStepId(_template.getTaskId()) != 1)
					{
						sendProgress(player, ExDynamicQuest.UpdateAction.ACTION_PROGRESS);
					}

					addParticipiant(player);
				});
			}
		}
	}

	/**
	 * Завершаем квест, чистимся ;)
	 */
	public void endQuest(boolean startNextTask)
	{
		if(!_questStarted)
		{
			return;
		}

		_questStarted = false;

		boolean isFailed = false;

		if(_currentPoints < _template.getPoints())
		{
			isFailed = true;
		}

		if(_template.getSpawnHolder() != null)
		{
			_template.getSpawnHolder().unSpawnAll();
		}

		// Вычислим минимальное количество очков для того, чтобы получить элитную награду
		int minElitePoints = 0;

		List<Integer> points = new ArrayList<>(_pointsByMembers.size());

		for(Integer point : _pointsByMembers.values())
		{
			if(point != null)
			{
				points.add(point);
			}
		}

		Collections.sort(points, Collections.reverseOrder());

		if(!points.isEmpty())    // Т.к получается  points.get(-1) и выходит нпе
		{
			minElitePoints = points.size() >= 7 ? points.get(6) : points.get(points.size() - 1);
		}

		// Отображение диалогов и выдача наград
		for(L2PcInstance player : _participiants.values())
		{
			if(isFailed)
			{
				onQuestFailed(player, player.getQuestState(_template.getQuestName()));
				showDialog(player, "failed");
			}
			else
			{
				int participiantPoints = _pointsByMembers.containsKey(player.getObjectId()) ? _pointsByMembers.get(player.getObjectId()) : 0;
				onQuestEnd(player, player.getQuestState(_template.getQuestName()));
				if(participiantPoints >= minElitePoints && minElitePoints > 0)
				{
					if(!showDialog(player, "elite_reward"))
					{
						giveReward(player);
					}

					_eliteWinners.add(player.getName());
				}
				else
				{
					if(!showDialog(player, "reward"))
					{
						giveReward(player);
					}

					_winners.add(player.getName());
				}
			}

			if(isFailed)
			{
				if(!_template.isCampain())
				{
					sendProgress(player, ExDynamicQuest.UpdateAction.ACTION_FAIL);
				}
			}
			else
			{
				sendProgress(player, ExDynamicQuest.UpdateAction.ACTION_GET_REWARD);

				if(_template.isCampain() || _template.isZoneQuest())
				{
					// Даем игроку три минуты на то, чтобы забрать награду
					ThreadPoolManager.getInstance().scheduleGeneral(() -> {
						_currentPoints = 0;
						sendProgress(player, ExDynamicQuest.UpdateAction.ACTION_VIEW_RESULT);
						player.sendPacket(new ExDynamicQuest(player, _template.isCampain(), _template.getQuestId(), DynamicQuestManager.getStepId(_template.getTaskId())).endCampain());
					}, 180000);
				}
			}
		}

		if(isFailed && _template.isCampain())
		{
			for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
			{
				if(player.getLevel() < _template.getMinLevel())
				{
					continue;
				}

				sendProgress(player, ExDynamicQuest.UpdateAction.ACTION_FAIL);
				ThreadPoolManager.getInstance().scheduleGeneral(() -> player.sendPacket(new ExDynamicQuest(player, _template.isCampain(), _template.getQuestId(), DynamicQuestManager.getStepId(_template.getTaskId())).endCampain()), 300000);
			}
		}

		/* TODO Дебаг ( сделать вывод в дебаг фаил )
		if (isFailed)
		{
			_log.log(Level.INFO, getClass().getSimpleName() + ": Dynamic quest \"" + _template.getQuestName() + "\" failed! Quest done.");
		}
		else
		{
			_log.log(Level.INFO, getClass().getSimpleName() + ": Dynamic quest \"" + _template.getQuestName() + "\" completed! Players rewarded!");
		}
         */
		// Не осталось новых этапов кампании, выходим
		if(_inviteTask != null)
		{
			_inviteTask.cancel(true);
			_inviteTask = null;
		}
		if(_endTask != null)
		{
			_endTask.cancel(true);
			_endTask = null;
		}

		_allEventTimers.clear();
		_questInvolvedNpcs.clear();
		_questInvolvedAskIds.clear();

		// Стартуем следующий этап, если есть
		if(!_template.isCampain())
		{
			if(isFailed)
			{
				int restartTime = 180000;
				_initialTime = System.currentTimeMillis() + restartTime;
				ThreadPoolManager.getInstance().scheduleGeneral(this::startQuest, restartTime);

				//TODO Дебаг ( сделать вывод в дебаг фаил )
				//_log.log(Level.INFO, getClass().getSimpleName() + ": Dynamic quest " + _template.getQuestName() + " failed! Restarting in " + (restartTime / (1000 * 60)) + " min.");
			}
			else
			{
				int nextTaskId = _template.getNextTaskId();

				if(nextTaskId > 0)
				{
					DynamicQuest quest = DynamicQuestManager.getInstance().getQuestByTaskId(nextTaskId);
					if(quest != null)
					{
						_initialTime = System.currentTimeMillis() + 180000;
						ThreadPoolManager.getInstance().scheduleGeneral(() -> {
							_participiants.values().stream().filter(player -> player != null).forEach(player -> player.sendPacket(new ExDynamicQuest(player, _template.isCampain(), _template.getQuestId(), DynamicQuestManager.getStepId(_template.getTaskId())).endCampain()));

							if(startNextTask)
							{
								ThreadPoolManager.getInstance().scheduleGeneral(quest::startQuest, 5500);
							}
						}, 180000);
					}

					// TODO Дебаг ( сделать вывод в дебаг фаил )
					// _log.log(Level.INFO, getClass().getSimpleName() + ": Dynamic quest " + _template.getQuestName() + " continue to next stage! (" + nextTaskId + ")");
				}
				else  /* Есть локальные квесты у которых нету больше этапов, поэтому сразу по завершению квеста делаем рестарт */
				{
					int restartTime = 180000;
					_initialTime = System.currentTimeMillis() + restartTime;
					ThreadPoolManager.getInstance().scheduleGeneral(this::startQuest, restartTime);
				}
			}
		}

		onCampainDone(!isFailed);
	}

	public void onCampainStart()
	{

	}

	/**
	 * Вызывается при завершении кампании.
	 *
	 * @param succeed Если кампания завершена успешно, то true.
	 */
	public void onCampainDone(boolean succeed)
	{

	}

	public void onQuestEnd(L2PcInstance player, QuestState qs)
	{

	}

	public void onQuestFailed(L2PcInstance player, QuestState qs)
	{

	}

	public void onRewardReceived(L2PcInstance player, QuestState qs)
	{

	}

	public List<ItemHolder> getRewards()
	{
		return _template.getAllRewards();
	}

	/**
	 * Добавить участника кампании.
	 * @param player Участник.
	 */
	public void addParticipiant(L2PcInstance player)
	{
		if(!_questStarted)
		{
			return;
		}

		if(!_participiants.containsKey(player.getObjectId()))
		{
			addEventId(HookType.ON_ENTER_WORLD);
		}

		DynamicQuestState st = newQuestState(player);
		player.setQuestState(st);
		st.startQuest();
		_participiants.put(player.getObjectId(), player);
		sendProgress(player, ExDynamicQuest.UpdateAction.ACTION_PROGRESS);

		showDialog(player, "accept");
	}

	/**
	 *
	 * @param player Игрок.
	 * @return True, если игрок является участником кампании.
	 */
	public boolean isParticipiant(L2PcInstance player)
	{
		return _participiants.containsKey(player.getObjectId()) || _winners.contains(player.getName()) || _eliteWinners.contains(player.getName());
	}

	public Map<Integer, L2PcInstance> getAllParticipiants()
	{
		return _participiants;
	}

	public boolean showDialog(L2PcInstance player, String type)
	{
		String html = HtmCache.getInstance().getHtm(player.getLang(), "default/" + _template.getDialog(type) + ".htm");
		if(html == null)
		{
			return false;
		}

		player.sendPacket(new NpcHtmlMessage(5, html));
		return true;
	}

	public void sendProgress(L2PcInstance player, ExDynamicQuest.UpdateAction updateAction)
	{
		player.sendPacket(new ExDynamicQuest(player, _template.isCampain(), getQuestId(), DynamicQuestManager.getStepId(_template.getTaskId())).setProgressUpdate(updateAction, getRemainingTime(), _template.getTaskId(), _currentPoints, _template.getPoints()));
	}

	public void sendResults(L2PcInstance player)
	{
		List<String> memberList = new FastList(_winners);
		memberList.addAll(_eliteWinners);

		long currentTime = System.currentTimeMillis();
		// Кэшируем
		if(_lastResultUpdate == 0 || _lastResultUpdate + 5000 <= currentTime)
		{
			_lastResultUpdate = currentTime;
			_results = new FastList();
			// Формируем список групп для зоновых квестов
			if(_template.isCampain())
			{
				int currentContributed = _pointsByMembers.containsKey(player.getObjectId()) ? _pointsByMembers.get(player.getObjectId()) : 0;
				int additionalContributed = 0;
				int totalContributed = currentContributed;
				_results.add(new DynamicQuestResult(player.getName(), currentContributed, additionalContributed, totalContributed));
			}
			else
			{
				Map<String, DynamicQuestResult> unsorted = new FastMap();
				// Те, кто уже был добавлен в список результатов
				List<Integer> passedParticipiants = new FastList();

				for(Map.Entry<Integer, L2PcInstance> integerL2PcInstanceEntry : _participiants.entrySet())
				{
					if(passedParticipiants.contains(integerL2PcInstanceEntry.getKey()))
					{
						continue;
					}

					passedParticipiants.add(integerL2PcInstanceEntry.getKey());
					L2PcInstance participiant = integerL2PcInstanceEntry.getValue();
					// Игрок куда-то пропал? Ну и ладно ^_^
					if(participiant == null)
					{
						continue;
					}

					L2Party party = participiant.getParty();
					// Нет пати?
					if(party == null && !participiant.isGM())
					{
						continue;
					}

					String leader;
					leader = party != null ? party.getLeader().getName() : participiant.getName();

					DynamicQuestResult currentResult;
					int currentContributed = _pointsByMembers.containsKey(integerL2PcInstanceEntry.getKey()) ? _pointsByMembers.get(integerL2PcInstanceEntry.getKey()) : 0;
					int additionalContributed = 0;
					int totalContributed = currentContributed;
					if(unsorted.containsKey(leader))
					{
						currentResult = unsorted.get(leader);
						currentResult.setContributed(ContributionType.CURRENT, currentContributed + currentResult.getContributed(ContributionType.CURRENT));
						currentResult.setContributed(ContributionType.ADDITIONAL, additionalContributed + currentResult.getContributed(ContributionType.ADDITIONAL));
						currentResult.setContributed(ContributionType.TOTAL, totalContributed + currentResult.getContributed(ContributionType.TOTAL));
					}
					else
					{
						currentResult = new DynamicQuestResult(leader, currentContributed, additionalContributed, totalContributed);
						unsorted.put(leader, currentResult);
						_results.add(currentResult);
					}
				}
			}

			Collections.sort(_results);
		}

		player.sendPacket(new ExDynamicQuest(player, _template.isCampain(), _template.getQuestId(), DynamicQuestManager.getStepId(_template.getTaskId())).setResultsUpdate(_results, getRemainingTime()));
	}

	public void sendRewardInfo(L2PcInstance player)
	{
		if(_eliteWinners.contains(player.getName()))
		{
			showDialog(player, "elite_reward");
		}
		else
		{
			showDialog(player, "reward");
		}
	}

	public void giveReward(L2PcInstance player)
	{
		if(!_winners.contains(player.getName()) && !_eliteWinners.contains(player.getName()))
		{
			return;
		}

		List<ItemHolder> rewards = _eliteWinners.contains(player.getName()) ? _template.getAllEliteRewards() : _template.getAllRewards();

		for(ItemHolder item : rewards)
		{
			player.addItem(ProcessType.QUEST, item.getId(), item.getCount(), null, true);
		}

		showDialog(player, "finish");

		_winners.remove(player.getName());
		_eliteWinners.remove(player.getName());
	}

	/**
	 * @return Оставшееся время в секундах
	 */
	public int getRemainingTime()
	{
		return Math.max(0, (int) ((_initialTime - System.currentTimeMillis()) / 1000));
	}

	public DynamicQuestTemplate getTemplate()
	{
		return _template;
	}

	public List<String> getEliteWinners()
	{
		return _eliteWinners;
	}

	public List<String> getWinners()
	{
		return _winners;
	}

	public int getCollectedPoints()
	{
		return _currentPoints;
	}

	public boolean isStarted()
	{
		return _questStarted;
	}

	/**
	 * @return ID квеста кампании.
	 */
	@Override
	public int getQuestId()
	{
		return _template.getQuestId();
	}

	@Override
	public DynamicQuestState newQuestState(L2PcInstance player)
	{
		return new DynamicQuestState(this, player, getInitialState());
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		if(!_questStarted)
		{
			return null;
		}

		if(!_participiants.containsKey(st.getPlayer().getObjectId()))
		{
			return "";
		}

		try
		{
			int points = (int) (_template.getKillPoint(npc.getNpcId()) * Config.RATE_CAMPAINS);

			if(DynamicQuestManager.hasGmBonus() && st.getPlayer().isGM())
			{
				points *= _template.getPoints() / 10;
			}

			// Пересчитаем очки для всей группы, если она есть
			if(!_template.isCampain())
			{
				L2Party party = st.getPlayer().getParty();
				if(party != null)
				{
					for(L2PcInstance partyMember : party.getMembers())
					{
						if(partyMember.getObjectId() == st.getPlayer().getObjectId())
						{
							continue;
						}

						int memberPoints = points;
						if(_pointsByMembers.containsKey(partyMember.getObjectId()))
						{
							memberPoints += _pointsByMembers.get(partyMember.getObjectId());
						}
						_pointsByMembers.put(partyMember.getObjectId(), memberPoints);
					}
					points *= party.getMemberCount();
				}
			}

			// Глобальный счет и счет игрока
			if(points > 0)
			{
				_currentPoints += points;
				int objectId = st.getPlayer().getObjectId();

				if(_pointsByMembers.containsKey(objectId))
				{
					points += _pointsByMembers.get(objectId);
				}

				_currentPoints = Math.min(_currentPoints, _template.getPoints());

				_pointsByMembers.put(st.getPlayer().getObjectId(), points);
			}

			// Достижение нужного счета
			if(_currentPoints >= _template.getPoints())
			{
				for(L2PcInstance player : _participiants.values())
				{
					sendProgress(player, ExDynamicQuest.UpdateAction.ACTION_PROGRESS);
				}

				endQuest(true);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Dynamic Quest onKill event failed: Quest=" + _template.getQuestName(), e);
		}

		return null;
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(!_questStarted)
		{
			return null;
		}

		if(character.isPlayer())
		{
			character.sendPacket(new ExDynamicQuest((L2PcInstance) character, _template.isCampain(), _template.getQuestId(), DynamicQuestManager.getStepId(_template.getTaskId())).startCampain());

			if(!_template.isCampain())
			{
				addParticipiant((L2PcInstance) character);
			}
		}
		return null;
	}

	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if(!_questStarted)
		{
			return null;
		}

		if(character.isPlayer())
		{
			if(!_template.isCampain())
			{
				sendProgress((L2PcInstance) character, ExDynamicQuest.UpdateAction.ACTION_FAIL);
				character.sendPacket(new ExDynamicQuest((L2PcInstance) character, _template.isCampain(), _template.getQuestId(), DynamicQuestManager.getStepId(_template.getTaskId())).endCampain());

				if(_participiants.containsKey(character.getObjectId()))
				{
					_participiants.remove(character.getObjectId());
				}
			}
		}
		return null;
	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{
		if(!_questStarted)
		{
			return;
		}

		if(_participiants.containsKey(player.getObjectId()))
		{
			sendProgress(player, ExDynamicQuest.UpdateAction.ACTION_PROGRESS);
		}
	}

	public enum ContributionType
	{
		CURRENT,
		ADDITIONAL,
		TOTAL,
	}

	public class DynamicQuestResult implements Comparable
	{
		private final String _participiant;
		private int _currentContributed;
		private int _additionalContributed;
		private int _totalContributed;

		public DynamicQuestResult(String participiant, int currentContributed, int additionalContributed, int totalContributed)
		{
			_participiant = participiant;
			_currentContributed = currentContributed;
			_additionalContributed = additionalContributed;
			_totalContributed = totalContributed;
		}

		public String getParticipiant()
		{
			return _participiant;
		}

		public int getContributed(ContributionType type)
		{
			switch(type)
			{
				case CURRENT:
					return _currentContributed;
				case ADDITIONAL:
					return _additionalContributed;
				case TOTAL:
					return _totalContributed;
			}
			return 0;
		}

		public void setContributed(ContributionType type, int value)
		{
			switch(type)
			{
				case CURRENT:
					_currentContributed = value;
					break;
				case ADDITIONAL:
					_additionalContributed = value;
					break;
				case TOTAL:
					_totalContributed = value;
					break;
			}
		}

		@Override
		public int compareTo(Object o)
		{
			if(o instanceof DynamicQuestResult)
			{
				int oValue = ((DynamicQuestResult) o).getContributed(ContributionType.TOTAL);
				return oValue > _totalContributed ? 1 : oValue == _totalContributed ? 0 : -1;
			}
			return 0;
		}
	}
}