package dwo.gameserver.instancemanager.events.KOTH;

import dwo.config.events.ConfigEventKOTH;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.events.EventState;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.olympiad.OlympiadManager;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.Say2;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoInvenWeight;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.StringUtil;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Nik
 * Some of the code is taken and modified from FBIagent's TvT Event,
 * to make this event more comfortable for the users when they set
 * the configuration for the event, and for the players when they
 * participate in it.
 */

public class KOTHEvent
{
	protected static final Logger _log = LogManager.getLogger(KOTHEvent.class);
	private static final String htmlPath = "mods/KOTHEvent/";
	protected static KOTHEventTeam[] _team = new KOTHEventTeam[2];
	protected static byte HillOwner = -1;
	private static EventState _state = EventState.INACTIVE;
	private static L2Spawn _npcSpawn;
	private static L2Npc _lastNpcSpawn;
	private static int _KOTHEventInstance;
	private static ScheduledFuture<?> RespawnTask;
	private static ScheduledFuture<?> HillPointsTask;
	private static ScheduledFuture<?> StatusScreenMsgTask;

	public static void init()
	{
		_team[0] = new KOTHEventTeam(ConfigEventKOTH.KOTH_EVENT_TEAM_1_NAME, ConfigEventKOTH.KOTH_EVENT_TEAM_1_COORDINATES);
		_team[1] = new KOTHEventTeam(ConfigEventKOTH.KOTH_EVENT_TEAM_2_NAME, ConfigEventKOTH.KOTH_EVENT_TEAM_2_COORDINATES);
	}

	public static boolean startParticipation()
	{
		L2NpcTemplate npc = NpcTable.getInstance().getTemplate(ConfigEventKOTH.KOTH_EVENT_PARTICIPATION_NPC_ID);
		if(npc == null)
		{
			_log.log(Level.WARN, "KOTHEventEngine[KOTHEvent.startParticipation()]: L2NpcTemplate is Null, probably invalid npc id in the config?");
			return false;
		}

		try
		{
			_npcSpawn = new L2Spawn(npc);
			_npcSpawn.setLocx(ConfigEventKOTH.KOTH_EVENT_PARTICIPATION_NPC_COORDINATES[0]);
			_npcSpawn.setLocy(ConfigEventKOTH.KOTH_EVENT_PARTICIPATION_NPC_COORDINATES[1]);
			_npcSpawn.setLocz(ConfigEventKOTH.KOTH_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(0);
			_npcSpawn.setRespawnDelay(1);
			SpawnTable.getInstance().addNewSpawn(_npcSpawn);
			_npcSpawn.init();
			_lastNpcSpawn = _npcSpawn.getLastSpawn();
			_lastNpcSpawn.setCurrentHp(_lastNpcSpawn.getMaxHp());
			_lastNpcSpawn.setTitle("Царь горы");
			_lastNpcSpawn.isAggressive();
			_lastNpcSpawn.getLocationController().decay();
			_lastNpcSpawn.getLocationController().spawn(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			_lastNpcSpawn.broadcastPacket(new MagicSkillUse(_lastNpcSpawn, _lastNpcSpawn, 2025, 1, 1, 1));
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "KOTHEventEngine[KOTHEvent.startParticipation()]: exception: " + e);
			return false;
		}

		setState(EventState.PARTICIPATING);
		return true;
	}

	private static int highestLevelPlayerId(Map<Integer, L2PcInstance> players)
	{
		int maxLevel = Integer.MIN_VALUE;
		int maxLevelId = -1;
		for(L2PcInstance player : players.values())
		{
			if(player.getLevel() >= maxLevel)
			{
				maxLevel = player.getLevel();
				maxLevelId = player.getObjectId();
			}
		}
		return maxLevelId;
	}

	public static boolean startFight()
	{
		setState(EventState.STARTING);

		Map<Integer, L2PcInstance> allRegistered = new FastMap<>();
		allRegistered.putAll(_team[0].getRegisteredPlayers());
		allRegistered.putAll(_team[1].getRegisteredPlayers());
		_team[0].clean();
		_team[1].clean();
		int[] balance = {0, 0};
		int priority = 0;
		int highestLevelPlayerId;
		L2PcInstance highestLevelPlayer;
		while(!allRegistered.isEmpty())
		{
			highestLevelPlayerId = highestLevelPlayerId(allRegistered);
			highestLevelPlayer = allRegistered.get(highestLevelPlayerId);
			allRegistered.remove(highestLevelPlayerId);
			_team[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			if(allRegistered.isEmpty())
			{
				break;
			}
			priority = 1 - priority;
			highestLevelPlayerId = highestLevelPlayerId(allRegistered);
			highestLevelPlayer = allRegistered.get(highestLevelPlayerId);
			allRegistered.remove(highestLevelPlayerId);
			_team[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			priority = balance[0] > balance[1] ? 1 : 0;
		}

		if(_team[0].getRegisteredPlayersCount() < ConfigEventKOTH.KOTH_EVENT_MIN_PLAYERS_IN_TEAMS || _team[1].getRegisteredPlayersCount() < ConfigEventKOTH.KOTH_EVENT_MIN_PLAYERS_IN_TEAMS)
		{
			setState(EventState.INACTIVE);
			_team[0].clean();
			_team[1].clean();
			unSpawnNpc();
			return false;
		}

		if(ConfigEventKOTH.KOTH_EVENT_IN_INSTANCE)
		{
			try
			{
				_KOTHEventInstance = InstanceManager.getInstance().createDynamicInstance(ConfigEventKOTH.KOTH_EVENT_INSTANCE_FILE);
				InstanceManager.getInstance().getInstance(_KOTHEventInstance).setAllowSummon(false);
				InstanceManager.getInstance().getInstance(_KOTHEventInstance).setPvPInstance(true);
				InstanceManager.getInstance().getInstance(_KOTHEventInstance).setEmptyDestroyTime(ConfigEventKOTH.KOTH_EVENT_START_LEAVE_TELEPORT_DELAY * 1000 + 60000L);
			}
			catch(Exception e)
			{
				_KOTHEventInstance = 0;
				_log.log(Level.ERROR, "KOTHEventEngine[KOTHEvent.createDynamicInstance]: exception: " + e);
			}
		}

		setState(EventState.STARTED);
		startHillPointsTask();
		startStatusScreenMsgTask();
		if(ConfigEventKOTH.KOTH_EVENT_RESPAWN_TYPE == ConfigEventKOTH.KOTHEventRespawnType.MASS)
		{
			startRespawnLoop();
		}

		for(KOTHEventTeam team : _team)
		{
			team.getRegisteredPlayers().values().stream().filter(player -> player != null).forEach(player -> new KOTHEventTeleporter(player, team.getCoordinates(), false, false));
		}

		return true;
	}

	public static void startHillPointsTask()
	{
		synchronized(KOTHEvent.class)
		{
			HillPointsTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new HillPointsTask(), ConfigEventKOTH.KOTH_EVENT_POINTS[1], ConfigEventKOTH.KOTH_EVENT_POINTS[1]);
		}
	}

	public static void stopHillPointsTask()
	{
		synchronized(KOTHEvent.class)
		{
			if(HillPointsTask != null)
			{
				HillPointsTask.cancel(false);
				HillPointsTask = null;
			}
		}
	}

	public static void startStatusScreenMsgTask()
	{
		synchronized(KOTHEvent.class)
		{
			if(ConfigEventKOTH.KOTH_EVENT_STATUS_DELAY >= 10)
			{
				StatusScreenMsgTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> screenMsgToAllParticipants(_team[0].getName() + ':' + _team[0].getPoints() + " - " + _team[1].getName() + ':' + _team[1].getPoints(), 1000), ConfigEventKOTH.KOTH_EVENT_STATUS_DELAY, ConfigEventKOTH.KOTH_EVENT_STATUS_DELAY);
			}
		}
	}

	public static void stopStatusScreenMsgTask()
	{
		synchronized(KOTHEvent.class)
		{
			if(StatusScreenMsgTask != null)
			{
				StatusScreenMsgTask.cancel(false);
				StatusScreenMsgTask = null;
			}
		}
	}

	public static String calculateWinner()
	{
		if(_team[0].getPoints() == _team[1].getPoints())
		{
			if(_team[0].getRegisteredPlayersCount() == 0 || _team[1].getRegisteredPlayersCount() == 0)
			{
				setState(EventState.REWARDING);
				return "Царь горы: Ивент закончен. Никто не победил!";
			}
			sysMsgToAllParticipants("Царь горы: Ивент закончился вничью.");
			if(ConfigEventKOTH.KOTH_REWARD_TEAM_TIE)
			{
				rewardTeam(0);
				rewardTeam(1);
				return "Царь горы: Ивент закончился вничью.";
			}
			else
			{
				return "Царь горы: Ивент закончился вничью.";
			}
		}

		setState(EventState.REWARDING);
		stopHillPointsTask();
		stopRespawnLoop();

		KOTHEventTeam winner = _team[_team[0].getPoints() > _team[1].getPoints() ? 0 : 1];
		KOTHEventTeam loser = _team[_team[1].getPoints() > _team[0].getPoints() ? 0 : 1];
		if(winner.equals(_team[0]))
		{
			rewardTeam(0);
		}
		else
		{
			rewardTeam(1);
		}

		return "Царь горы: Ивент окончен. Команда " + winner.getName() + " победила с " + winner.getPoints() + " очками. " + "Проигравшие заработали только " + loser.getPoints() + " очка(ов).";
	}

	private static void rewardTeam(int teamId)
	{
		KOTHEventTeam team = _team[teamId];
		for(L2PcInstance player : team.getRegisteredPlayers().values())
		{
			if(player == null)
			{
				continue;
			}
			SystemMessage sm = null;

			for(int[] reward : ConfigEventKOTH.KOTH_EVENT_REWARDS)
			{
				PcInventory inv = player.getInventory();
				// Check for stackable item, non stackabe items need to be added one by one
				if(ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
				{
					inv.addItem(ProcessType.EVENT, reward[0], reward[1], player, player);

					if(reward[1] > 1)
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(reward[0]);
						sm.addItemNumber(reward[1]);
					}
					else
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
						sm.addItemName(reward[0]);
					}

					player.sendPacket(sm);
				}
				else
				{
					for(int i = 0; i < reward[1]; ++i)
					{
						inv.addItem(ProcessType.EVENT, reward[0], 1, player, player);
						sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
						sm.addItemName(reward[0]);
						player.sendPacket(sm);
					}
				}
			}

			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Reward.htm"));
			player.sendPacket(new ExUserInfoInvenWeight(player));
			player.sendPacket(npcHtmlMessage);
		}
	}

	public static void stopFight()
	{
		setState(EventState.INACTIVATING);
		unSpawnNpc();

		for(KOTHEventTeam team : _team)
		{
			team.getRegisteredPlayers().values().stream().filter(player -> player != null).forEach(player -> new KOTHEventTeleporter(player, ConfigEventKOTH.KOTH_EVENT_PARTICIPATION_NPC_COORDINATES, false, false));
		}
		_team[0].clean();
		_team[1].clean();
		setState(EventState.INACTIVE);
	}

	private static boolean checkDualbox(L2PcInstance player)
	{
		String hwid = player.getClient().getHWID();
		for(L2PcInstance plr : _team[0].getRegisteredPlayers().values())
		{
			if(plr.getClient().getHWID() != null && plr.getClient().getHWID().equals(hwid))
			{
				return true;
			}
		}
		for(L2PcInstance plr : _team[1].getRegisteredPlayers().values())
		{
			if(plr.getClient().getHWID() != null && plr.getClient().getHWID().equals(hwid))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean addParticipant(L2PcInstance player)
	{
		synchronized(KOTHEvent.class)
		{
			if(player == null)
			{
				return false;
			}
			if(checkDualbox(player))
			{
				player.sendMessage("Извините, но в ивенте не разрешено участие нескольких персонажей с одного компьютера.");
				return false;
			}
			byte teamId = 0;

			teamId = _team[0].getRegisteredPlayersCount() == _team[1].getRegisteredPlayersCount() ? (byte) Rnd.get(2) : (byte) (_team[0].getRegisteredPlayersCount() > _team[1].getRegisteredPlayersCount() ? 1 : 0);

			return _team[teamId].addPlayer(player);
		}
	}

	public static int getMemberCount()
	{
		int count = 0;
		for(KOTHEventTeam team : _team)
		{
			if(team != null)
			{
				count += team.getRegisteredPlayersCount();
			}
		}
		return count;
	}

	public static boolean addPlayerOnTheHill(L2PcInstance player)
	{
		synchronized(KOTHEvent.class)
		{
			if(player == null)
			{
				return false;
			}
			byte teamId = getParticipantTeamId(player.getObjectId());
			if(teamId == -1)
			{
				return false;
			}
			_team[teamId].addPlayerOnTheHill(player);
			return true;
		}
	}

	public static boolean removeParticipant(int playerObjectId)
	{
		byte teamId = getParticipantTeamId(playerObjectId);

		if(teamId != -1)
		{
			_team[teamId].removePlayer(playerObjectId);
			_team[teamId].removePlayerOnTheHill(playerObjectId);
			return true;
		}

		return false;
	}

	public static boolean removePlayerOnTheHill(int playerObjectId)
	{
		synchronized(KOTHEvent.class)
		{
			byte teamId = getParticipantTeamId(playerObjectId);

			if(teamId == -1)
			{
				return false;
			}
			_team[teamId].removePlayerOnTheHill(playerObjectId);
			return true;
		}
	}

	public static boolean payParticipationFee(L2PcInstance player)
	{
		int itemId = ConfigEventKOTH.KOTH_EVENT_PARTICIPATION_FEE[0];
		int itemNum = ConfigEventKOTH.KOTH_EVENT_PARTICIPATION_FEE[1];
		if(itemId == 0 || itemNum == 0)
		{
			return true;
		}

		if(player.getInventory().getInventoryItemCount(itemId, -1) < itemNum)
		{
			return false;
		}

		return player.destroyItemByItemId(ProcessType.EVENT, itemId, itemNum, _lastNpcSpawn, true);
	}

	public static String getParticipationFee()
	{
		int itemId = ConfigEventKOTH.KOTH_EVENT_PARTICIPATION_FEE[0];
		int itemNum = ConfigEventKOTH.KOTH_EVENT_PARTICIPATION_FEE[1];

		if(itemId == 0 || itemNum == 0)
		{
			return "-";
		}

		return StringUtil.concat(String.valueOf(itemNum), " ", ItemTable.getInstance().getTemplate(itemId).getName());
	}

	public static void sysMsgToAllParticipants(String message)
	{
		Say2 cs = null;
		for(L2PcInstance player : _team[0].getRegisteredPlayers().values())
		{
			if(player != null)
			{
				cs = new Say2(0, ChatType.BATTLEFIELD, "Царь горы", message);
				player.sendPacket(cs);
			}
		}

		for(L2PcInstance player : _team[1].getRegisteredPlayers().values())
		{
			if(player != null)
			{
				cs = new Say2(0, ChatType.BATTLEFIELD, "Царь горы", message);
				player.sendPacket(cs);
			}
		}
	}

	public static void screenMsgToAllParticipants(String message, int delay)
	{
		_team[0].getRegisteredPlayers().values().stream().filter(player -> player != null).forEach(player -> player.sendPacket(new ExShowScreenMessage(message, delay)));

		_team[1].getRegisteredPlayers().values().stream().filter(player -> player != null).forEach(player -> player.sendPacket(new ExShowScreenMessage(message, delay)));
	}

	public static void startRespawnLoop()
	{
		synchronized(KOTHEvent.class)
		{
			RespawnTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RespawnTask(), ConfigEventKOTH.KOTH_EVENT_RESPAWN_DELAY * 1000, ConfigEventKOTH.KOTH_EVENT_RESPAWN_DELAY * 1000);
		}
	}

	public static void stopRespawnLoop()
	{
		synchronized(KOTHEvent.class)
		{
			if(RespawnTask != null)
			{
				RespawnTask.cancel(false);
				RespawnTask = null;
			}
		}
	}

	private static void unSpawnNpc()
	{
		_lastNpcSpawn.getLocationController().delete();
		_npcSpawn.stopRespawn();
		_npcSpawn = null;
		_lastNpcSpawn = null;
	}

	public static void onToVillage(L2PcInstance player)
	{
		if(!isStarted() || player == null)
		{
			return;
		}
		byte teamId = getParticipantTeamId(player.getObjectId());
		if(teamId == -1)
		{
			return;
		}

		// MASS
		if(ConfigEventKOTH.KOTH_EVENT_RESPAWN_TYPE == ConfigEventKOTH.KOTHEventRespawnType.MASS)
		{
			_team[teamId].addDeadPlayer(player);
			player.sendMessage("Вы воскресните через " + RespawnTask.getDelay(TimeUnit.SECONDS) + " секунд(ы).");
		}
		// MANUAL
		else if(ConfigEventKOTH.KOTH_EVENT_RESPAWN_TYPE == ConfigEventKOTH.KOTHEventRespawnType.MANUAL)
		{
			new KOTHEventTeleporter(player, _team[teamId].getCoordinates(), false, false);
		}
	}

	public static void onLogin(L2PcInstance player)
	{
		if(!isStarting() && !isStarted() || player == null)
		{
			return;
		}

		byte teamId = getParticipantTeamId(player.getObjectId());

		if(teamId == -1)
		{
			return;
		}

		_team[teamId].addPlayer(player);
		new KOTHEventTeleporter(player, _team[teamId].getCoordinates(), true, false);
	}

	public static void onLogout(L2PcInstance player)
	{
		if((isStarting() || isStarted() || isParticipating()) && player != null)
		{
			if(removeParticipant(player.getObjectId()))
			{
				player.setXYZ(ConfigEventKOTH.KOTH_EVENT_PARTICIPATION_NPC_COORDINATES[0] + Rnd.get(101) - 50, ConfigEventKOTH.KOTH_EVENT_PARTICIPATION_NPC_COORDINATES[1] + Rnd.get(101) - 50, ConfigEventKOTH.KOTH_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
			}
		}
	}

	public static void onBypass(String command, L2PcInstance player)
	{
		synchronized(KOTHEvent.class)
		{
			if(player == null || !isParticipating())
			{
				return;
			}
			String htmContent;

			if(command.equals("KOTH_event_participation"))
			{
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
				int playerLevel = player.getLevel();

				if(player.isCursedWeaponEquipped())
				{
					htmContent = HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "CursedWeaponEquipped.htm");
					if(htmContent != null)
					{
						npcHtmlMessage.setHtml(htmContent);
					}
				}
				else if(OlympiadManager.getInstance().isRegistered(player))
				{
					htmContent = HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Olympiad.htm");
					if(htmContent != null)
					{
						npcHtmlMessage.setHtml(htmContent);
					}
				}
				else if(player.hasBadReputation())
				{
					htmContent = HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Karma.htm");
					if(htmContent != null)
					{
						npcHtmlMessage.setHtml(htmContent);
					}
				}
				else if(playerLevel < ConfigEventKOTH.KOTH_EVENT_MIN_LVL || playerLevel > ConfigEventKOTH.KOTH_EVENT_MAX_LVL)
				{
					htmContent = HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Level.htm");
					if(htmContent != null)
					{
						npcHtmlMessage.setHtml(htmContent);
						npcHtmlMessage.replace("%min%", String.valueOf(ConfigEventKOTH.KOTH_EVENT_MIN_LVL));
						npcHtmlMessage.replace("%max%", String.valueOf(ConfigEventKOTH.KOTH_EVENT_MAX_LVL));
					}
				}
				else if(_team[0].getRegisteredPlayersCount() == ConfigEventKOTH.KOTH_EVENT_MAX_PLAYERS_IN_TEAMS && _team[1].getRegisteredPlayersCount() == ConfigEventKOTH.KOTH_EVENT_MAX_PLAYERS_IN_TEAMS)
				{
					htmContent = HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "TeamsFull.htm");
					if(htmContent != null)
					{
						npcHtmlMessage.setHtml(htmContent);
						npcHtmlMessage.replace("%max%", String.valueOf(ConfigEventKOTH.KOTH_EVENT_MAX_PLAYERS_IN_TEAMS));
					}
				}
				else if(!payParticipationFee(player))
				{
					htmContent = HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "ParticipationFee.htm");
					if(htmContent != null)
					{
						npcHtmlMessage.setHtml(htmContent);
						npcHtmlMessage.replace("%fee%", getParticipationFee());
					}
				}
				else if(addParticipant(player))
				{
					npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Registered.htm"));
				}
				else
				{
					return;
				}

				player.sendPacket(npcHtmlMessage);
			}
			else if(command.equals("KOTH_event_remove_participation"))
			{
				removeParticipant(player.getObjectId());

				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

				npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Unregistered.htm"));
				player.sendPacket(npcHtmlMessage);
			}
		}
	}

	public static boolean isAutoAttackable(L2PcInstance player, L2Character attacker)
	{
		if(player == null || attacker == null || !(attacker instanceof L2PcInstance))
		{
			return true;
		}

		byte playerTeamId = getParticipantTeamId(player.getObjectId());
		byte attackerTeamId = getParticipantTeamId(attacker.getObjectId());

		if(playerTeamId != -1 && attackerTeamId == -1 || playerTeamId == -1 && attackerTeamId != -1)
		{
			return false;
		}

		return !(playerTeamId != -1 && attackerTeamId != -1 && playerTeamId == attackerTeamId && player.getObjectId() != attacker.getObjectId());

	}

	public static boolean onAction(L2PcInstance player, int targetedPlayerObjectId)
	{
		if(!isStarted() || player == null)
		{
			return true;
		}

		if(player.isGM())
		{
			return true;
		}

		byte playerTeamId = getParticipantTeamId(player.getObjectId());
		byte targetedPlayerTeamId = getParticipantTeamId(targetedPlayerObjectId);

		if(playerTeamId != -1 && targetedPlayerTeamId == -1 || playerTeamId == -1 && targetedPlayerTeamId != -1)
		{
			return false;
		}

		return !(playerTeamId != -1 && targetedPlayerTeamId != -1 && playerTeamId == targetedPlayerTeamId && player.getObjectId() != targetedPlayerObjectId && !ConfigEventKOTH.KOTH_EVENT_TARGET_TEAM_MEMBERS_ALLOWED);

	}

	public static boolean onScrollUse(int playerObjectId)
	{
		if(!isStarted())
		{
			return true;
		}

		return !(isPlayerParticipant(playerObjectId) && !ConfigEventKOTH.KOTH_EVENT_SCROLL_ALLOWED);

	}

	public static boolean onPotionUse(int playerObjectId)
	{
		if(!isStarted())
		{
			return true;
		}

		return !(isPlayerParticipant(playerObjectId) && !ConfigEventKOTH.KOTH_EVENT_POTIONS_ALLOWED);

	}

	public static boolean onEscapeUse(int playerObjectId)
	{
		if(!isStarted())
		{
			return true;
		}

		return !isPlayerParticipant(playerObjectId);

	}

	public static boolean onItemSummon(int playerObjectId)
	{
		if(!isStarted())
		{
			return true;
		}

		return !(isPlayerParticipant(playerObjectId) && !ConfigEventKOTH.KOTH_EVENT_SUMMON_BY_ITEM_ALLOWED);

	}

	public static void onKill(L2Character killerCharacter, L2PcInstance killedPlayer)
	{
		if(!isStarted() || killedPlayer == null)
		{
			return;
		}

		byte killedTeamId = getParticipantTeamId(killedPlayer.getObjectId());
		if(killedTeamId == -1)
		{
			return;
		}

		if(ConfigEventKOTH.KOTH_EVENT_RESPAWN_TYPE == ConfigEventKOTH.KOTHEventRespawnType.CLASSIC)
		{
			new KOTHEventTeleporter(killedPlayer, _team[killedTeamId].getCoordinates(), false, false);
		}
	}

	public static void onTeleported(L2PcInstance player)
	{
		if(!isStarted() || player == null || !isPlayerParticipant(player.getObjectId()))
		{
			return;
		}

		if(player.isMageClass())
		{
			if(ConfigEventKOTH.KOTH_EVENT_MAGE_BUFFS != null && !ConfigEventKOTH.KOTH_EVENT_MAGE_BUFFS.isEmpty())
			{
				for(int i : ConfigEventKOTH.KOTH_EVENT_MAGE_BUFFS.keys())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, ConfigEventKOTH.KOTH_EVENT_MAGE_BUFFS.get(i));
					if(skill != null)
					{
						skill.getEffects(player, player);
					}
				}
			}
		}
		else
		{
			if(ConfigEventKOTH.KOTH_EVENT_FIGHTER_BUFFS != null && !ConfigEventKOTH.KOTH_EVENT_FIGHTER_BUFFS.isEmpty())
			{
				for(int i : ConfigEventKOTH.KOTH_EVENT_FIGHTER_BUFFS.keys())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, ConfigEventKOTH.KOTH_EVENT_FIGHTER_BUFFS.get(i));
					if(skill != null)
					{
						skill.getEffects(player, player);
					}
				}
			}
		}
	}

	public static boolean checkForSkill(L2PcInstance source, L2PcInstance target, L2Skill skill)
	{
		if(!isStarted())
		{
			return true;
		}

		int sourcePlayerId = source.getObjectId();
		int targetPlayerId = target.getObjectId();
		boolean isSourceParticipant = isPlayerParticipant(sourcePlayerId);
		boolean isTargetParticipant = isPlayerParticipant(targetPlayerId);

		if(!isSourceParticipant && !isTargetParticipant)
		{
			return true;
		}
		if(!(isSourceParticipant && isTargetParticipant))
		{
			return false;
		}
		if(getParticipantTeamId(sourcePlayerId) != getParticipantTeamId(targetPlayerId))
		{
			if(!skill.isOffensive())
			{
				return false;
			}
		}
		return true;
	}

	private static void setState(EventState state)
	{
		synchronized(_state)
		{
			_state = state;
		}
	}

	public static boolean isInactive()
	{
		boolean isInactive;

		synchronized(_state)
		{
			isInactive = _state == EventState.INACTIVE;
		}

		return isInactive;
	}

	public static boolean isInactivating()
	{
		boolean isInactivating;

		synchronized(_state)
		{
			isInactivating = _state == EventState.INACTIVATING;
		}

		return isInactivating;
	}

	public static boolean isParticipating()
	{
		boolean isParticipating;

		synchronized(_state)
		{
			isParticipating = _state == EventState.PARTICIPATING;
		}

		return isParticipating;
	}

	public static boolean isStarting()
	{
		boolean isStarting;

		synchronized(_state)
		{
			isStarting = _state == EventState.STARTING;
		}

		return isStarting;
	}

	public static boolean isStarted()
	{
		boolean isStarted;

		synchronized(_state)
		{
			isStarted = _state == EventState.STARTED;
		}

		return isStarted;
	}

	public static boolean isRewarding()
	{
		boolean isRewarding;

		synchronized(_state)
		{
			isRewarding = _state == EventState.REWARDING;
		}

		return isRewarding;
	}

	public static byte getParticipantTeamId(int playerObjectId)
	{
		return (byte) (_team[0].containsRegisteredPlayer(playerObjectId) ? 0 : _team[1].containsRegisteredPlayer(playerObjectId) ? 1 : -1);
	}

	public static int[] getParticipantTeamCoordinates(int playerObjectId)
	{
		return _team[0].containsRegisteredPlayer(playerObjectId) ? _team[0].getCoordinates() : _team[1].containsRegisteredPlayer(playerObjectId) ? _team[1].getCoordinates() : null;
	}

	public static boolean isPlayerParticipant(int playerObjectId)
	{
		if(!isParticipating() && !isStarting() && !isStarted())
		{
			return false;
		}

		return _team[0].containsRegisteredPlayer(playerObjectId) || _team[1].containsRegisteredPlayer(playerObjectId);
	}

	public static int getParticipatedPlayersCount()
	{
		if(!isParticipating() && !isStarting() && !isStarted())
		{
			return 0;
		}

		return _team[0].getRegisteredPlayersCount() + _team[1].getRegisteredPlayersCount();
	}

	public static KOTHEventTeam getParticipantEnemyTeam(int playerObjectId)
	{
		return _team[0].containsRegisteredPlayer(playerObjectId) ? _team[1] : _team[1].containsRegisteredPlayer(playerObjectId) ? _team[0] : null;
	}

	public static int[] getTeamsPlayerCounts()
	{
		return new int[]{
			_team[0].getRegisteredPlayersCount(), _team[1].getRegisteredPlayersCount()
		};
	}

	public static long[] getTeamsPoints()
	{
		return new long[]{
			_team[0].getPoints(), _team[1].getPoints()
		};
	}

	public static int getKOTHEventInstance()
	{
		return _KOTHEventInstance;
	}

	protected static class HillPointsTask implements Runnable
	{
		//KOTH_EVENT_POINTS:
		// 0 = points per executed task
		// 1 = execute task rate(in miliseconds)
		// 2 = additional points
		long points = ConfigEventKOTH.KOTH_EVENT_POINTS[0];
		int diff;

		@Override
		public void run()
		{
			if(_team[0].getPlayersOnTheHillCount() > _team[1].getPlayersOnTheHillCount())
			{
				diff = _team[0].getPlayersOnTheHillCount() - _team[1].getPlayersOnTheHillCount();
				points += diff * ConfigEventKOTH.KOTH_EVENT_POINTS[2];
				_team[0].increasePoints(points);
				if(HillOwner != 0)
				{
					screenMsgToAllParticipants(_team[0].getName() + " захватил гору!", 3000);
				}
				HillOwner = 0;
			}
			else if(_team[1].getPlayersOnTheHillCount() > _team[0].getPlayersOnTheHillCount())
			{
				diff = _team[1].getPlayersOnTheHillCount() - _team[0].getPlayersOnTheHillCount();
				points += diff * ConfigEventKOTH.KOTH_EVENT_POINTS[2];
				_team[1].increasePoints(points);
				if(HillOwner != 1)
				{
					screenMsgToAllParticipants(_team[1].getName() + " захватил гору!", 3000);
				}
				HillOwner = 1;
			}
			else
			{
				if(HillOwner != -1)
				{
					screenMsgToAllParticipants("Никто не владеет горой!", 3000);
				}
				HillOwner = -1;
			}
		}
	}

	protected static class RespawnTask implements Runnable
	{
		@Override
		public void run()
		{
			for(L2PcInstance dead : _team[0]._deadPlayers.values())
			{
				if(dead == null)
				{
					return;
				}
				new KOTHEventTeleporter(dead, _team[0].getCoordinates(), true, false);
				_team[0].removeDeadPlayer(dead.getObjectId());
			}
			for(L2PcInstance dead : _team[1]._deadPlayers.values())
			{
				if(dead == null)
				{
					return;
				}
				new KOTHEventTeleporter(dead, _team[1].getCoordinates(), true, false);
				_team[1].removeDeadPlayer(dead.getObjectId());
			}
		}
	}
}