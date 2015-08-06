package dwo.scripts.instances;

import dwo.config.scripts.ConfigChaosFestival;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.sql.ChaosFestivalTable;
import dwo.gameserver.engine.hookengine.AbstractHookImpl;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.engine.hookengine.IHook;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.controller.player.ObserverController;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ChaosFestivalEntry;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.curioushouse.ExCuriousHouseEnter;
import dwo.gameserver.network.game.serverpackets.packet.curioushouse.ExCuriousHouseLeave;
import dwo.gameserver.network.game.serverpackets.packet.curioushouse.ExCuriousHouseMemberList;
import dwo.gameserver.network.game.serverpackets.packet.curioushouse.ExCuriousHouseMemberUpdate;
import dwo.gameserver.network.game.serverpackets.packet.curioushouse.ExCuriousHouseRemainTime;
import dwo.gameserver.network.game.serverpackets.packet.curioushouse.ExCuriousHouseResult;
import dwo.gameserver.network.game.serverpackets.packet.curioushouse.ExCuriousHouseState;
import dwo.gameserver.util.Rnd;
import dwo.scripts.ai.group_template.ChaosFestivalMobs;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Chaos Festival instance.
 *
 * @author Yorie
 */
public class ChaosFestival extends Quest
{
	private static final String qn = "ChaosFestival";
	private static final int GRANKAIN_LUMIERE = 33685;
	private static final int[] ARENAS = {
		InstanceZoneId.ARENA_1.getId(), InstanceZoneId.ARENA_2.getId(), InstanceZoneId.ARENA_3.getId(),
		InstanceZoneId.ARENA_4.getId(),
	};
	private static final Location[][] LOCS = {
		{
			new Location(-82630, -259280, -3328), new Location(-82179, -259732, -3328),
			new Location(-81736, -259292, -3328), new Location(-82174, -258850, -3328)
		}, {
		new Location(-81259, -245270, -3324), new Location(-82585, -245264, -3324),
		new Location(-82577, -246079, -3326), new Location(-81275, -246075, -3324)
	}, {
		new Location(-9593, -220697, -7664), new Location(-8562, -220710, -7670), new Location(-8577, -219705, -7670),
		new Location(-9540, -219710, -7672)
	}, {
		new Location(-212014, 244496, 2117), new Location(-212019, 245300, 2117), new Location(-212907, 244896, 2038),
		new Location(-214072, 244892, 2032), new Location(-214909, 245300, 2112), new Location(-214908, 244485, 2117)
	}
	};
	private static final int[][][][] SPAWN_LOCS = {
		{
			{{-81687, -246264, -3327}, {-81144, -245085, -3327}}, {{-82706, -246265, -3327}, {-82165, -245084, -3327}},
		}, {
		{{-82007, -259115, -3327}, {-81208, -258481, -3327}}, {{-83155, -260093, -3327}, {-81994, -259461, -3327}},
		{{-83256, -260095, -3327}, {-82358, -259465, -3327}}, {{-83158, -259112, -3327}, {-82355, -258481, -3327}},
	}, {
		{{-9816, -220960, -7671}, {-8308, -220589, -7671}}, {{-8694, -220549, -7671}, {-8303, -219836, -7671}},
		{{-9821, -219835, -7671}, {-8367, -219449, -7671}}, {{-9815, -220575, -7671}, {-9415, -219835, -7671}},
	}, {
		{{-215197, 244686, 2119}, {-211850, 244327, 2119}}, {{-215081, 245458, 2119}, {-211982, 245081, 2119}}
	}
	};
	private static final List<L2PcInstance> _invitedPlayers = new FastList<>();
	private static final Map<Integer, Boolean> _invitedPlayersMap = new FastMap<>();
	private static final Map<Integer, Long> _inviteAcceptTime = new FastMap<>();
	private static final List<L2PcInstance> _fightingNow = new FastList<>();
	private static ChaosFestival _chaosFestivalInstance;
	private static List<ChaosFestivalWorld> battles = new FastList<>();
	private static long _initialTime;
	private static long _invitationEndTime;
	private static long _teleportTime;
	private static ChaosFestivalStatus _festivalStatus;
	private static IHook _playerFestivalHook = new PlayerFestivalHook();
	private static Future<?> _inviteTask;
	private static Future<?> _teleportTask;
	private static Future<?> _endTask;

	public ChaosFestival()
	{
		schedule();

		if(ConfigChaosFestival.CHAOS_FESTIVAL_MONSTER_ID > 0)
		{
			addKillId(ConfigChaosFestival.CHAOS_FESTIVAL_MONSTER_ID);
		}
	}

	public static void main(String[] args)
	{
		_chaosFestivalInstance = new ChaosFestival();
	}

	public static ChaosFestival getInstance()
	{
		return _chaosFestivalInstance;
	}

	/**
	 * Добавляет очко бана игроку. При наличии этих очков игрок не может участвовать в фестивале.
	 * @param player Игрок.
	 */
	public void banPlayer(L2PcInstance player)
	{
		banPlayer(player.getObjectId());
	}

	/**
	 * Добавляет очко бана игроку. При наличии этих очков игрок не может участвовать в фестивале.
	 * @param playerId ID игрока.
	 */
	private void banPlayer(int playerId)
	{
		ChaosFestivalEntry entry = ChaosFestivalTable.getInstance().getEntry(playerId);
		entry.setSkipRounds(entry.getSkipRounds() + 1);
		entry.setTotalBans(entry.getTotalBans() + 1);
	}

	@Nullable
	private int[] randomSpawnLoc(ChaosFestivalWorld world)
	{
		try
		{
			int[][][] locs = SPAWN_LOCS[world.mapIndex];
			int[][] square = locs[Rnd.get(locs.length)];

			int minX;
			int maxX;
			int minY;
			int maxY;
			int minZ;
			int maxZ;
			if(square[0][0] > square[1][0])
			{
				minX = square[1][0];
				maxX = square[0][0];
			}
			else
			{
				minX = square[0][0];
				maxX = square[1][0];
			}

			if(square[0][1] > square[1][1])
			{
				minY = square[1][1];
				maxY = square[0][1];
			}
			else
			{
				minY = square[0][1];
				maxY = square[1][1];
			}

			if(square[0][2] > square[1][2])
			{
				minZ = square[1][2];
				maxZ = square[0][2];
			}
			else
			{
				minZ = square[0][2];
				maxZ = square[1][2];
			}

			return new int[]{Rnd.get(minX, maxX), Rnd.get(minY, maxY), (minZ + maxZ) / 2};
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Chaos Festival has bad spawn coords for loc id " + world.mapIndex, e);
		}
		return null;
	}

	/**
	 * Создает инстанс фестиваля для группы игроков.
	 * @param players Группа игроков.
	 */
	public void enterInstance(List<L2PcInstance> players)
	{
		int selection = Rnd.get(ARENAS.length);
		int arenaId = ARENAS[selection];

		int instanceId = InstanceManager.getInstance().createDynamicInstance("ChaosFestival.xml");
		ChaosFestivalWorld world = new ChaosFestivalWorld();
		InstanceManager.getInstance().addWorld(world);
		world.startTime = System.currentTimeMillis();
		world.instanceId = instanceId;
		world.templateId = arenaId;
		world.status = 0;
		world.mapIndex = selection;
		InstanceManager.getInstance().addWorld(world);

		int[] coords = randomSpawnLoc(world);
		if(coords == null)
		{
			_log.error("Chaos Festival: Bad observer teleport coords!");
		}
		else
		{
			world.observerTeleportLoc = new Location(coords[0], coords[1], coords[2]);
		}

		battles.add(world);

		startChallenge(players, world);
	}

	/**
	 * Начать соревнование.
	 *
	 * @param players Группа игроков.
	 * @param world Игровой мир.
	 */
	public void startChallenge(final List<L2PcInstance> players, final ChaosFestivalWorld world)
	{
		world.status = 1;
		// Предварительная настройка игроков
		for(L2PcInstance player : players)
		{
			// Правила фестиваля
			player.sendPacket(new NpcHtmlMessage(5, HtmCache.getInstance().getHtm(player.getLang(), "default/grankain_lumiere011.htm")));
			player.sendPacket(new ExCuriousHouseEnter());
			if(players.size() > 1)
			{
				player.sendPacket(new ExCuriousHouseMemberList(players));
			}

			if(player.isFlying() && player.getTransformationId() > 0)
			{
				player.untransform(true);
			}

			// Не может двигаться и невидим
			player.setIsCanMove(false);
			player.setInsideZone(L2Character.ZONE_PVP, true);
			player.setInsideZone(L2Character.ZONE_NOBOOKMARK, true);
			player.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, true);
			player.setInsideZone(L2Character.ZONE_NOITEMDROP, true);
			player.setInsideZone(L2Character.ZONE_NORESTART, true);
			player.setInsideZone(L2Character.ZONE_NOSTORE, true);
			player.getAppearance().setInvisible();

			for(L2Summon summon : player.getPets())
			{
				summon.getLocationController().setVisible(false);
			}

			// Инициализация мира
			world.allowed.add(player.getObjectId());
			world.members.add(player);
			_fightingNow.add(player);

			world.playerStartLocations.put(player.getObjectId(), player.getLoc());
			player.setTarget(null);
			Location loc = LOCS[world.mapIndex][Rnd.get(LOCS[world.mapIndex].length)];
			player.teleToInstance(loc, world.instanceId);

			player.sendPacket(new ExCuriousHouseEnter());

			// Отключаем клан-скиллы
			player.disableSkillsOfTargetType(L2TargetType.TARGET_ALLY);
			player.disableSkillsOfTargetType(L2TargetType.TARGET_CLAN);
			player.disableSkillsOfTargetType(L2TargetType.TARGET_CLAN_MEMBER);

			for(L2Summon summon : player.getPets())
			{
				summon.disableSkillsOfTargetType(L2TargetType.TARGET_ALLY);
				summon.disableSkillsOfTargetType(L2TargetType.TARGET_CLAN);
				summon.disableSkillsOfTargetType(L2TargetType.TARGET_CLAN_MEMBER);
			}

			// Предметы Фестиваль Хаоса (Защита, Атака, Магия)
			player.getInventory().addItem(ProcessType.QUEST, 35991, 1, player, null, true);
			player.getInventory().addItem(ProcessType.QUEST, 35992, 1, player, null, true);
			player.getInventory().addItem(ProcessType.QUEST, 35993, 1, player, null, true);

			// Хуки
			player.getHookContainer().addHook(HookType.ON_SUMMON_SPAWN, _playerFestivalHook);
			player.getHookContainer().addHook(HookType.ON_DIE, _playerFestivalHook);
			player.getHookContainer().addHook(HookType.ON_ATTACK, _playerFestivalHook);
			player.getHookContainer().addHook(HookType.ON_ENTER_INSTANCE, _playerFestivalHook);
		}

		// Подготовка игроков к началу матча (длится 60 секунд)
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if(world.status > 1)
				{
					return;
				}

				// Спауним мобов, если нужно
				if(ConfigChaosFestival.CHAOS_FESTIVAL_MONSTER_ID > 0 && Rnd.get() <= ConfigChaosFestival.CHAOS_FESTIVAL_MONSTER_APPEAR_CHANCE / 100.0)
				{
					int count = Rnd.get(ConfigChaosFestival.CHAOS_FESTIVAL_MONSTER_COUNT[0], ConfigChaosFestival.CHAOS_FESTIVAL_MONSTER_COUNT[1]);
					for(int i = 0; i < count; ++i)
					{
						int[] loc = randomSpawnLoc(world);

						if(loc != null)
						{
							world.npcs.add(addSpawn(ConfigChaosFestival.CHAOS_FESTIVAL_MONSTER_ID, loc[0], loc[1], loc[2], 0, false, 0, false, world.instanceId));
							++world.monsterCount;
						}
					}
				}

				int index = 1;
				for(L2PcInstance player : players)
				{
					player.setIsCanMove(true);

					// Скроем имена игроков
					if(ConfigChaosFestival.CHAOS_FESTIVAL_HIDE_PLAYER_NAMES)
					{
						player.getAppearance().setVisibleName("Player" + index);
						player.getAppearance().ownerSeesRealName();
						++index;
					}

					// Скроем все знаки отличия
					if(ConfigChaosFestival.CHAOS_FESTIVAL_HIDE_DISTINCTIONS)
					{
						player.setHideInfo(true);
						player.getAppearance().setVisibleTitle("");
						player.getAppearance().disableNameColor();
						player.getAppearance().disableTitleColor();
					}

					player.getLocationController().setVisible(true);
					player.getAppearance().setVisible();

					for(L2Summon summon : player.getPets())
					{
						summon.getLocationController().setVisible(true);
					}

					// Бафф Энергия Хаоса
					SkillTable.getInstance().getInfo(7115, 1).getEffects(player, player);

					// Соревнования начались
					SystemMessage msg = SystemMessage.getSystemMessage(1496);
					player.sendPacket(msg);

					// Таймер матча
					world.timerTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
					{
						private int _time = 360;

						@Override
						public void run()
						{
							if(world.status > 1)
							{
								if(world.timerTask != null)
								{
									world.timerTask.cancel(false);
									world.timerTask = null;
								}
								return;
							}

							if(_time > 0)
							{
								for(L2PcInstance player : world.members)
								{
									player.sendPacket(new ExCuriousHouseRemainTime(_time));

									player.broadcastPacket(new ExCuriousHouseMemberUpdate(player));
								}
								--_time;
							}
							else if(world.timerTask != null)
							{
								world.timerTask.cancel(false);
								world.timerTask = null;
							}
						}
					}, 0, 1000);
				}

				// Если количество игроков - 1, то он становится проигравшим, завершаем матч
				if(players.size() <= 1 && world.monsterCount <= 0)
				{
					endChallenge(world);
				}

				// Спаун коробок со вкусностями во время матча
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					private int _iteration;

					@Override
					public void run()
					{
						if(world.status > 1)
						{
							return;
						}

						int[] loc = randomSpawnLoc(world);

						if(loc != null)
						{
							switch(_iteration)
							{
								case 0:
									for(byte i = 0; i < ConfigChaosFestival.CHAOS_FESTIVAL_FIRST_HERB_BOX_COUNT; ++i)
									{
										addSpawn(ChaosFestivalMobs.HERB_BOX, loc[0], loc[1], loc[2], 0, false, 1000 * (5 * 60 + 40), false, world.instanceId);
									}

									ThreadPoolManager.getInstance().scheduleGeneral(this, 100000);
									break;
								case 1:
									for(byte i = 0; i < ConfigChaosFestival.CHAOS_FESTIVAL_SECOND_HERB_BOX_COUNT; ++i)
									{
										addSpawn(ChaosFestivalMobs.HERB_BOX, loc[0], loc[1], loc[2], 0, false, 1000 * 4 * 60, false, world.instanceId);
									}

									ThreadPoolManager.getInstance().scheduleGeneral(this, 120000);
									break;
								case 2:
									for(byte i = 0; i < ConfigChaosFestival.CHAOS_FESTIVAL_THIRD_HERB_BOX_COUNT; ++i)
									{
										addSpawn(ChaosFestivalMobs.HERB_BOX, loc[0], loc[1], loc[2], 0, false, 1000 * 4 * 60, false, world.instanceId);
									}

									ThreadPoolManager.getInstance().scheduleGeneral(this, 120000);
									break;
							}
							++_iteration;
						}
						else
						{
							_log.error("Chaos Festival: Bad spawn coords for Herb Boxes.");
						}
					}
				}, 20000);

				// Через 6 минут заканчивается матч
				ThreadPoolManager.getInstance().scheduleGeneral(() -> endChallenge(world), 360000);
			}
		}, ConfigChaosFestival.CHAOS_FESTIVAL_PREPARATION_TIME * 1000);

		// Сообщения таймера подготовки перед стартом матча
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			private int _counter;
			private int _secs = 60;

			private boolean isFightingNow(L2PcInstance player)
			{
				return _fightingNow.contains(player);
			}

			@Override
			public void run()
			{
				if(world.status > 1)
				{
					return;
				}

				if(_secs > 0)
				{
					SystemMessage msg = SystemMessage.getSystemMessage(1495);
					msg.addNumber(_secs);

					players.stream().filter(this::isFightingNow).forEach(player -> player.sendPacket(msg));

					if(_counter == 0)
					{
						ThreadPoolManager.getInstance().scheduleGeneral(this, 30000);
						_secs -= 30;
					}
					else if(_counter < 3)
					{
						ThreadPoolManager.getInstance().scheduleGeneral(this, 10000);
						_secs -= 10;
					}
					else if(_counter == 3)
					{
						ThreadPoolManager.getInstance().scheduleGeneral(this, 5000);
						_secs -= 5;
					}
					else
					{
						ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
						_secs -= 1;
					}
				}

				++_counter;
			}

		}, 0);

		// Сообщения зоны
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			private final int[] _messages = {3744, 3745, 3746};
			private int _currentMessageIndex;

			private boolean isFightingNow(L2PcInstance player)
			{
				return _fightingNow.contains(player);
			}

			@Override
			public void run()
			{
				if(world.status > 1)
				{
					return;
				}

				SystemMessage msg = SystemMessage.getSystemMessage(_messages[_currentMessageIndex]);
				players.stream().filter(this::isFightingNow).forEach(player -> player.sendPacket(msg));
				++_currentMessageIndex;

				if(_currentMessageIndex < _messages.length)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(this, 4000);
				}
			}

		}, 5000);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		ChaosFestivalWorld world = InstanceManager.getInstance().getInstanceWorld(npc, ChaosFestivalWorld.class);

		if(world != null)
		{
			int kills = world.monsterKills.containsKey(killer.getObjectId()) ? world.monsterKills.get(killer.getObjectId()) : 0;
			world.monsterKills.put(killer.getObjectId(), ++kills);
			--world.monsterCount;

			if(world.monsterCount <= 0)
			{
				endChallenge(world);
			}
		}

		return null;
	}

	/**
	 * Завершить соревнование группы игроков.
	 *
	 * @param world Игрокой мир.
	 */
	public void endChallenge(ChaosFestivalWorld world)
	{
		if(world.status != 1)
		{
			return;
		}

		world.status = 2;

		world.npcs.stream().filter(npc -> npc != null).forEach(npc -> npc.getLocationController().delete());

		// Подсчет победителей, проигравших, лучшего убийцы и последнего выжившего
		List<L2PcInstance> winners = new FastList<>();
		L2PcInstance lastSurvivor = null;
		List<L2PcInstance> losers = new FastList<>();
		L2PcInstance bestKiller = null;
		int maxKills = -1;

		try
		{
			if(world.members.size() > 1)
			{
				boolean isWinner = false;
				for(L2PcInstance player : world.members)
				{
					if(player == null)
					{
						continue;
					}

					if(maxKills < 0 || world.kills.containsKey(player.getObjectId()) && world.kills.get(player.getObjectId()) > maxKills)
					{
						bestKiller = player;
						maxKills = world.kills.containsKey(player.getObjectId()) ? world.kills.get(player.getObjectId()) : 0;
					}

					if(!world.died.contains(player.getObjectId()) && !player.isDead())
					{
						world.survivorTimes.put(player.getObjectId(), (int) ((System.currentTimeMillis() - world.startTime) / 1000));
						isWinner = true;
						winners.add(player);
					}
					else
					{
						lastSurvivor = player;
						losers.add(player);
					}

					HookManager.getInstance().notifyEvent(HookType.ON_CHAOS_BATTLE_END, player.getHookContainer(), player, isWinner);
				}
			}
			else if(world.members.size() == 1)
			{
				L2PcInstance player = world.members.get(0);
				boolean isWinner = false;
				if(world.allowed.size() == 1 || world.monsterKills.get(player.getObjectId()) != null && world.monsterKills.get(player.getObjectId()) >= 5)
				{
					winners.add(player);
					isWinner = true;
				}

				HookManager.getInstance().notifyEvent(HookType.ON_CHAOS_BATTLE_END, player.getHookContainer(), player, isWinner);
			}

			for(L2PcInstance player : world.members)
			{
				if(player.isDead())
				{
					player.doRevive();
				}

				if(player != null)
				{
					SystemMessage msg = SystemMessage.getSystemMessage(1499);
					msg.addNumber(30);
					player.sendPacket(msg);

					ExCuriousHouseResult.PlayerState state = winners.contains(player) ? ExCuriousHouseResult.PlayerState.WIN : losers.contains(player) ? ExCuriousHouseResult.PlayerState.LOSE : ExCuriousHouseResult.PlayerState.TIE;
					player.sendPacket(new ExCuriousHouseResult(player, world.members, state, world.kills, world.survivorTimes));
				}
			}

			if(world.timerTask != null)
			{
				world.timerTask.cancel(true);
				world.timerTask = null;
			}

			if(lastSurvivor != null)
			{
				losers.remove(lastSurvivor);
			}

			// Ничья
			if(winners.size() == 1)
			{
				SystemMessage msg = SystemMessage.getSystemMessage(1497);
				msg.addPcName(winners.get(0));
				world.members.stream().filter(player -> player != null).forEach(player -> player.sendPacket(msg));

				for(int i = ConfigChaosFestival.CHAOS_FESTIVAL_MYST_BOX_MIN_COUNT; i <= ConfigChaosFestival.CHAOS_FESTIVAL_MYST_BOX_MAX_COUNT; ++i)
				{
					int[] loc = randomSpawnLoc(world);
					addSpawn(ChaosFestivalMobs.MYST_BOX, loc[0], loc[1], loc[2], 0, false, 0, false, world.instanceId);
				}
			}
			else
			{
				SystemMessage msg = SystemMessage.getSystemMessage(1498);
				for(L2PcInstance player : world.members)
				{
					player.sendPacket(msg);
				}
			}
		}
		catch(Exception e)
		{
			_log.error("Error during ending Chaos Festival battle.", e);
		}

		L2PcInstance fBestKiller = bestKiller;
		L2PcInstance fLastSurvivor = lastSurvivor;

		// Раздача наград и выход из инстанса
		ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			if(fBestKiller != null)
			{
				for(int id : ConfigChaosFestival.CHAOS_FESTIVAL_BEST_KILLER_REWARD_ITEMS)
				{
					fBestKiller.addItem(ProcessType.QUEST, id, 1, null, true);
				}
			}

			if(fLastSurvivor != null)
			{
				for(int id : ConfigChaosFestival.CHAOS_FESTIVAL_LAST_SURVIVOR_REWARD_BUFFS)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(id, 1);
					skill.getEffects(fLastSurvivor, fLastSurvivor);
				}
			}

			for(L2PcInstance winner : winners)
			{
				for(int id : ConfigChaosFestival.CHAOS_FESTIVAL_WINNER_REWARD_BUFFS)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(id, 1);
					skill.getEffects(winner, winner);
				}
			}

			for(L2PcInstance loser : losers)
			{
				for(int id : ConfigChaosFestival.CHAOS_FESTIVAL_LOSER_REWARD_BUFFS)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(id, 1);
					skill.getEffects(loser, loser);
				}
			}

			world.members.forEach(this::exitChallenge);

			InstanceManager.getInstance().destroyInstance(world.instanceId);
		}, 30000);
	}

	/**
	 * Выйти из соревнования. Если игрок вышел менее, чем через 7 минут после регистрации, то на него накладывается бан.
	 *
	 * @param player Игрок.
	 */
	public void exitChallenge(L2PcInstance player)
	{
		ChaosFestivalWorld world = InstanceManager.getInstance().getInstanceWorld(player, ChaosFestivalWorld.class);

		if(world != null)
		{
			if(player.isDead())
			{
				player.doRevive();
			}

			if(player.getObserverController().isObserving())
			{
				player.getObserverController().leave();
			}

			player.getHookContainer().removeHook(HookType.ON_DISCONNECT, _playerFestivalHook);
			player.getHookContainer().removeHook(HookType.ON_DELETEME, _playerFestivalHook);
			player.getHookContainer().removeHook(HookType.ON_SUMMON_SPAWN, _playerFestivalHook);
			player.getHookContainer().removeHook(HookType.ON_DIE, _playerFestivalHook);
			player.getHookContainer().removeHook(HookType.ON_HP_CHANGED, _playerFestivalHook);
			player.getHookContainer().removeHook(HookType.ON_ENTER_INSTANCE, _playerFestivalHook);

			player.getAppearance().setVisible();
			player.setInsideZone(L2Character.ZONE_PVP, false);
			player.setInsideZone(L2Character.ZONE_NOBOOKMARK, false);
			player.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false);
			player.setInsideZone(L2Character.ZONE_NOITEMDROP, false);
			player.setInsideZone(L2Character.ZONE_NORESTART, false);
			player.setInsideZone(L2Character.ZONE_NOSTORE, false);

			if(player.isDead())
			{
				player.doRevive(100.0);
			}

			player.setIsCanMove(true);
			if(ConfigChaosFestival.CHAOS_FESTIVAL_HIDE_PLAYER_NAMES)
			{
				player.getAppearance().setVisibleName(null);
				player.getAppearance().ownerSeesDisplayName();
			}

			if(ConfigChaosFestival.CHAOS_FESTIVAL_HIDE_DISTINCTIONS)
			{
				player.setHideInfo(false);
				player.getAppearance().setVisibleTitle(null);
				player.getAppearance().enableNameColor();
				player.getAppearance().enableTitleColor();
			}

			for(L2Summon summon : player.getPets())
			{
				summon.getLocationController().setVisible(true);
				summon.setCurrentHp(summon.getMaxHp());
				summon.setCurrentMp(summon.getMaxMp());
			}

			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());

			// Если игрок отказался от участия через 7 минут после принятия соглашения на участие - баним его на один раунд фестиваля
			if(!world.isDone && _inviteAcceptTime.containsKey(player.getObjectId()) && System.currentTimeMillis() - _inviteAcceptTime.get(player.getObjectId()) > 7 * 60 * 1000)
			{
				banPlayer(player);
			}

			player.enableSkillsOfTargetType(L2TargetType.TARGET_ALLY);
			player.enableSkillsOfTargetType(L2TargetType.TARGET_CLAN);
			player.enableSkillsOfTargetType(L2TargetType.TARGET_CLAN_MEMBER);

			for(L2Summon summon : player.getPets())
			{
				summon.getLocationController().setVisible(true);
				summon.disableSkillsOfTargetType(L2TargetType.TARGET_ALLY);
				summon.disableSkillsOfTargetType(L2TargetType.TARGET_CLAN);
				summon.disableSkillsOfTargetType(L2TargetType.TARGET_CLAN_MEMBER);
			}

			Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
			if(instance != null)
			{
				instance.removePlayer(player.getObjectId());
			}

			player.getInstanceController().setInstanceId(0);
			if(world.playerStartLocations.containsKey(player.getObjectId()))
			{
				player.teleToLocation(world.playerStartLocations.get(player.getObjectId()));
			}

			if(world.members.contains(player))
			{
				world.members.remove(player);
			}

			if(world.members.size() <= 1)
			{
				_chaosFestivalInstance.endChallenge(world);
			}

			if(world.kills.containsKey(player.getObjectId()))
			{
				world.kills.remove(player.getObjectId());
			}

			if(world.died.contains(player.getObjectId()))
			{
				world.died.remove(player.getObjectId());
			}

			if(world.survivorTimes.containsKey(player.getObjectId()))
			{
				world.survivorTimes.remove(player.getObjectId());
			}

			if(_fightingNow.contains(player))
			{
				_fightingNow.remove(player);
			}

			if(_invitedPlayers.contains(player))
			{
				_invitedPlayers.remove(player);
			}

			if(_invitedPlayersMap.containsKey(player.getObjectId()))
			{
				_invitedPlayersMap.remove(player.getObjectId());
			}

			player.sendPacket(new ExCuriousHouseState(ExCuriousHouseState.ChaosFestivalInviteState.IDLE));
		}

		player.sendPacket(new ExCuriousHouseLeave());

		if(_invitedPlayersMap.containsKey(player.getObjectId()))
		{
			_invitedPlayers.remove(player);
			_invitedPlayersMap.remove(player.getObjectId());
		}
	}

	public void cleanUp()
	{
		if(_inviteTask != null)
		{
			_inviteTask.cancel(true);
			_inviteTask = null;
		}

		if(_teleportTask != null)
		{
			_teleportTask.cancel(true);
			_teleportTask = null;
		}

		if(_endTask != null)
		{
			_endTask.cancel(true);
			_endTask = null;
		}

		_invitedPlayers.clear();
		_invitedPlayersMap.clear();
		_initialTime = 0;
		_inviteAcceptTime.clear();
		_festivalStatus = ChaosFestivalStatus.SCHEDULED;
		_fightingNow.clear();
		battles.clear();
	}

	/***
	 * @param player проверяемый игрок
	 * @return {@code true} если игрок в данный момент участвует в боях Фестиваля
	 */
	public boolean isFightingNow(L2PcInstance player)
	{
		return _fightingNow.contains(player);
	}

	/**
	 * Может ли игрок участвовать в Фестивале Хаоса?
	 *
	 * @param player Игрок.
	 * @return True, если может.
	 */
	public boolean canParticipate(L2PcInstance player)
	{
		if(player.isGM())
		{
			return true;
		}

		if(ChaosFestivalTable.getInstance().getEntry(player).getSkipRounds() > 0 || ChaosFestivalTable.getInstance().getEntry(player).getTotalBans() >= ConfigChaosFestival.CHAOS_FESTIVAL_TOTAL_BANS_LIMIT)
		{
			return false;
		}

		if(player.getClan() == null || player.getClan().getLevel() < 3)
		{
			return false;
		}

		if(player.getLevel() < 85 || player.getClassId().level() < 4)
		{
			SystemMessage msg = SystemMessage.getSystemMessage(3733);
			msg.addNumber(85);
			player.sendPacket(msg);
			return false;
		}

		return true;
	}

	/**
	 * Добавить игрока на фестиваль.
	 *
	 * @param player Игрок.
	 */
	public void addMember(L2PcInstance player)
	{
		player.getHookContainer().addHook(HookType.ON_DISCONNECT, _playerFestivalHook);
		player.getHookContainer().addHook(HookType.ON_DELETEME, _playerFestivalHook);
		_invitedPlayers.add(player);
		_invitedPlayersMap.put(player.getObjectId(), true);
		player.sendPacket(SystemMessage.getSystemMessage(3732));
		player.sendPacket(SystemMessageId.getSystemMessageId(3890));
		player.sendPacket(new ExCuriousHouseState(ExCuriousHouseState.ChaosFestivalInviteState.PREPARE));
	}

	/***
	 * @param player проверяемый игрок
	 * @return {@code true} если указанный игрок зарегистрирован в листе ожидания
	 */
	public boolean isRegistered(L2PcInstance player)
	{
		return _invitedPlayersMap.containsKey(player.getObjectId());
	}

	/**
	 * Для возможности запуска фестиваля вручную.
	 */
	public void testStartFestival()
	{
		cleanUp();

		_initialTime = System.currentTimeMillis();
		_invitationEndTime = _initialTime + 30 * 1000;
		_teleportTime = _invitationEndTime + 5 * 1000;

		_inviteTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PlayerInviteTask(), 0, 30000);
		_teleportTask = ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(), 35000);
	}

	/**
	 * Возможность вручную сохранить данные фестиваля.
	 */
	public void testSaveData()
	{
		ChaosFestivalTable.getInstance().cleanUp();
	}

	private void schedule()
	{
		if(!ConfigChaosFestival.CHAOS_FESTIVAL_ENABLED)
		{
			return;
		}

		Calendar now = Calendar.getInstance();
		int hour = now.get(Calendar.HOUR);

		Calendar schedule;

		// Если сервер ребутнулся и уже находимся в промежутках, когда начинаются матчи фестиваля
		if(hour >= ConfigChaosFestival.CHAOS_FESTIVAL_START_HOUR && hour < ConfigChaosFestival.CHAOS_FESTIVAL_END_HOUR)
		{
			int minute = now.get(Calendar.MINUTE);

			// Находимся в промежутке приглашения игроков
			if(minute >= 0 && minute < 5 || minute >= 20 && minute < 25 || minute >= 40 && minute < 45)
			{
				int teleportMinute = (minute + 5) / 5 * 5 + 5;
				Calendar teleportTime = Calendar.getInstance();
				teleportTime.set(Calendar.MINUTE, teleportMinute);
				teleportTime.set(Calendar.SECOND, 0);
				teleportTime.set(Calendar.MILLISECOND, 0);

				_inviteTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PlayerInviteTask(), 0, 30000);
				_teleportTask = ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(), teleportTime.getTimeInMillis() - System.currentTimeMillis());
				_endTask = ThreadPoolManager.getInstance().scheduleGeneral(new EndTask(), teleportTime.getTimeInMillis() - System.currentTimeMillis() + 6 * 60 * 1000);

				logScheduleInfo(System.currentTimeMillis() + 0, teleportTime.getTimeInMillis(), teleportTime.getTimeInMillis() + 6 * 60 * 1000);

				schedule = now;
				schedule.add(Calendar.MILLISECOND, 30000);
				_initialTime = teleportTime.getTimeInMillis() - 10 * 60 * 1000; // Начальное время - время ТП минус 10 минут
			}
			// Если нет, то надо зашедулиться на следующий цикл
			else
			{
				int inviteMinute = (minute + 20) / 20 * 20 % 60;
				Calendar inviteTime = Calendar.getInstance();
				inviteTime.set(Calendar.MINUTE, inviteMinute);
				inviteTime.set(Calendar.SECOND, 0);
				inviteTime.set(Calendar.MILLISECOND, 0);

				_inviteTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PlayerInviteTask(), inviteTime.getTimeInMillis() - System.currentTimeMillis(), 30000);
				_teleportTask = ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(), inviteTime.getTimeInMillis() - System.currentTimeMillis() + 10 * 60 * 1000);
				_endTask = ThreadPoolManager.getInstance().scheduleGeneral(new EndTask(), inviteTime.getTimeInMillis() - System.currentTimeMillis() + 10 * 60 * 1000 + 6 * 60 * 1000);

				logScheduleInfo(inviteTime.getTimeInMillis(), inviteTime.getTimeInMillis() + 10 * 60 * 1000, inviteTime.getTimeInMillis() + 10 * 60 * 1000 + 6 * 60 * 1000);

				schedule = inviteTime;
				_initialTime = schedule.getTimeInMillis();
			}
		}
		// Время еще не подошло, зашедулим :)
		else
		{
			Calendar nextSchedule = Calendar.getInstance();
			nextSchedule.set(Calendar.HOUR, ConfigChaosFestival.CHAOS_FESTIVAL_START_HOUR);
			nextSchedule.set(Calendar.MINUTE, 0);
			nextSchedule.set(Calendar.SECOND, 0);
			nextSchedule.set(Calendar.MILLISECOND, 0);

			_inviteTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PlayerInviteTask(), nextSchedule.getTimeInMillis() - System.currentTimeMillis(), 30000);
			_teleportTask = ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(), nextSchedule.getTimeInMillis() - System.currentTimeMillis() + 10 * 60 * 1000);
			_endTask = ThreadPoolManager.getInstance().scheduleGeneral(new EndTask(), nextSchedule.getTimeInMillis() - System.currentTimeMillis() + 10 * 60 * 1000 + 6 * 60 * 1000);

			logScheduleInfo(nextSchedule.getTimeInMillis(), nextSchedule.getTimeInMillis() + 10 * 60 * 1000, nextSchedule.getTimeInMillis() + 10 * 60 * 1000 + 6 * 60 * 1000);

			schedule = nextSchedule;
			_initialTime = schedule.getTimeInMillis();
		}

		_festivalStatus = ChaosFestivalStatus.SCHEDULED;
		_invitationEndTime = _initialTime + 5 * 60 * 1000;
		_teleportTime = _invitationEndTime + 5 * 60 * 1000;
		_playerFestivalHook = new PlayerFestivalHook();

		// Logging
		String hours = schedule.get(Calendar.HOUR) > 10 ? String.valueOf(schedule.get(Calendar.HOUR)) : '0' + String.valueOf(schedule.get(Calendar.HOUR));
		String minutes = schedule.get(Calendar.MINUTE) > 10 ? String.valueOf(schedule.get(Calendar.MINUTE)) : '0' + String.valueOf(schedule.get(Calendar.MINUTE));
		_log.log(Level.INFO, "Chaos festival scheduled for running at " + hours + ':' + minutes);
	}

	private void logScheduleInfo(long inviteTaskTime, long teleportTaskTime, long endTaskTime)
	{
		StringBuilder info = new StringBuilder();
		Calendar calendar = Calendar.getInstance();

		calendar.setTimeInMillis(inviteTaskTime);
		info.append(calendar.getTime());

		calendar.setTimeInMillis(teleportTaskTime);
		info.append(calendar.getTime());

		calendar.setTimeInMillis(endTaskTime);
		info.append(calendar.getTime());
	}

	/**
	 * Calculates list of currently active CF battles and returns their world objects.
	 * @return CF worlds.
	 */
	public List<ChaosFestivalWorld> getActiveBattleWorlds()
	{
		return battles;
	}

	public ChaosFestivalStatus getStatus()
	{
		return _festivalStatus;
	}

	/**
	 * @author Yorie
	 */
	public static enum ChaosFestivalStatus
	{
		/**
		 * The CF match set was scheduled for run.
		 */
		SCHEDULED,
		/**
		 * At this period players are invited for participating in CF.
		 */
		INVITING,
		/**
		 * Battle preparation period start after players was invited and teleported to arenas.
		 */
		PREPARING,
		/**
		 * Battle began, players fighting.
		 */
		RUNNING
	}

	public static class ChaosFestivalWorld extends InstanceWorld
	{
		public Location observerTeleportLoc;
		/**
		 * Battle timer task for current match. Usually 6 minutes given.
		 */
		public Future<?> timerTask;
		/**
		 * CF arena can be filled with monsters.
		 * This value is count of monsters that was spawned in current match.
		 */
		public int monsterCount;
		/**
		 * List of monster kills.
		 * Each index is player object ID.
		 * Each value is count of monsters killed by specified player.
		 */
		public Map<Integer, Integer> monsterKills = new FastMap<>();
		/**
		 * List of any NPCs in the match. Usually monsters only.
		 */
		public List<L2Npc> npcs = new FastList<>();
		/**
		 * True if battle ended.
		 */
		boolean isDone;
		/**
		 * Start time of CF in milliseconds.
		 */
		long startTime;
		/**
		 * CF can be started at 4 different arenas. This ID is index of arena.
		 */
		int mapIndex = -1;
		/**
		 * Stores player locations where they was before teleporting to CF arena.
		 */
		Map<Integer, Location> playerStartLocations = new FastMap<>();
		/**
		 * List of current match members.
		 */
		List<L2PcInstance> members = new FastList<>();
		/**
		 * List of kill counters for each player.
		 * Each index of the map is player object ID.
		 * Each value is count of frags.
		 */
		Map<Integer, Integer> kills = new FastMap<>();
		/**
		 * TODO: Remove?
		 */
		List<Integer> died = new FastList<>();
		/**
		 * List of survival timers for each CF member.
		 * Each index is player object ID.
		 * Each value is time in milliseconds meaning how long player was alive on arena.
		 */
		Map<Integer, Integer> survivorTimes = new FastMap<>();
	}

	public static class PlayerComparator implements Comparator<L2PcInstance>
	{
		@Override
		public int compare(L2PcInstance player1, L2PcInstance player2)
		{
			return player1.getLevel() < player2.getLevel() ? -1 : player1.getLevel() == player2.getLevel() ? 0 : 1;
		}
	}

	public static class PlayerFestivalHook extends AbstractHookImpl
	{
		@Override
		public void onDie(L2PcInstance player, L2Character killer)
		{
			ChaosFestivalWorld world = InstanceManager.getInstance().getInstanceWorld(killer, ChaosFestivalWorld.class);

			if(world != null)
			{
				world.survivorTimes.put(player.getObjectId(), (int) ((System.currentTimeMillis() - world.startTime) / 1000));

				if(killer instanceof L2PcInstance && !player.equals(killer))
				{
					if(world.kills.containsKey(killer.getObjectId()))
					{
						world.kills.put(killer.getObjectId(), world.kills.get(killer.getObjectId()) + 1);
					}
					else
					{
						world.kills.put(killer.getObjectId(), 1);
					}
				}

				Location dieLoc = player.getLocationController().getLoc();
				int instanceId = world.instanceId;
				getInstance().exitChallenge(player);

				int aliveCount = 0;
				for(L2PcInstance member : world.members)
				{
					if(member != null && !member.isDead())
					{
						++aliveCount;
					}
				}

				if(aliveCount <= 1)
				{
					getInstance().endChallenge(world);
				}

				player.getObserverController().enter(dieLoc, ObserverController.ObserveType.CHAOS_FESTIVAL, instanceId);
			}
			else
			{
				player.getHookContainer().removeHook(HookType.ON_DIE, this);
			}
		}

		@Override
		public void onDisconnect(L2PcInstance player)
		{
			synchronized(this)
			{
				if(player == null)
				{
					return;
				}

				ChaosFestivalWorld world = InstanceManager.getInstance().getInstanceWorld(player, ChaosFestivalWorld.class);

				if(world != null && world.members.contains(player))
				{
					if(player.isDead())
					{
						player.doRevive(100.0);
					}

					world.members.remove(player);
					// Если остался только один игрок - завершаем состязание
					if(world.members.size() <= 1)
					{
						getInstance().endChallenge(world);
					}

					for(L2PcInstance member : world.members)
					{
						member.sendPacket(new ExCuriousHouseMemberList(world.members));
					}
				}

				if(_inviteAcceptTime.containsKey(player.getObjectId()) && System.currentTimeMillis() - _inviteAcceptTime.get(player.getObjectId()) > 7 * 60 * 1000)
				{
					getInstance().banPlayer(player);
				}

				player.sendPacket(new ExCuriousHouseLeave());
				if(world != null && world.playerStartLocations.containsKey(player.getObjectId()))
				{
					player.teleToLocation(world.playerStartLocations.get(player.getObjectId()));
				}
			}
		}

		@Override
		public void onAttack(L2PcInstance player, L2Character attacker, boolean summonAttacked)
		{
			ChaosFestivalWorld world = InstanceManager.getInstance().getInstanceWorld(player, ChaosFestivalWorld.class);

			if(world != null)
			{
				player.broadcastPacket(new ExCuriousHouseMemberUpdate(player));
			}
			else
			{
				player.getHookContainer().removeHook(HookType.ON_ATTACK, this);
			}
		}

		@Override
		public void onDeleteMe(L2PcInstance player)
		{
			onDisconnect(player);
		}

		@Override
		public void onSummonSpawn(L2Summon summon)
		{
			ChaosFestivalWorld world = InstanceManager.getInstance().getInstanceWorld(summon.getOwner(), ChaosFestivalWorld.class);

			if(world != null)
			{
				if(_festivalStatus == ChaosFestivalStatus.PREPARING)
				{
					summon.getLocationController().setVisible(false);
				}

				summon.disableSkillsOfTargetType(L2TargetType.TARGET_ALLY);
				summon.disableSkillsOfTargetType(L2TargetType.TARGET_CLAN);
				summon.disableSkillsOfTargetType(L2TargetType.TARGET_CLAN_MEMBER);
			}
			else
			{
				summon.getOwner().getHookContainer().removeHook(HookType.ON_SUMMON_SPAWN, this);
			}
		}

		@Override
		public void onEnterInstance(L2PcInstance player, Instance instance)
		{
			// Returning to world?
			if(instance.getId() <= 0)
			{
				player.getHookContainer().removeHook(HookType.ON_ENTER_INSTANCE, this);
				getInstance().exitChallenge(player);
			}
		}
	}

	public static class PlayerInviteTask implements Runnable
	{
		private int _counter;

		@Override
		public void run()
		{
			// При первом запуске шедулим отправление сообщений о начале
			if(_counter == 0)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					private int _counter = 5;

					@Override
					public void run()
					{
						for(L2PcInstance player : _invitedPlayers)
						{
							SystemMessage msg = SystemMessage.getSystemMessage(3737);
							msg.addNumber(_counter);
							player.sendPacket(msg);
						}

						--_counter;

						if(_counter > 0)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
						}
					}
				}, System.currentTimeMillis() - _teleportTime - 5000);

				++_counter;
			}

			// Заканчиваем приглашение через 5 минут
			if(System.currentTimeMillis() >= _invitationEndTime)
			{
				// Отменяем приглашения у несогласившихся на участие игроков
				for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
				{
					if(!_invitedPlayersMap.containsKey(player.getObjectId()))
					{
						player.sendPacket(new ExCuriousHouseState(ExCuriousHouseState.ChaosFestivalInviteState.IDLE));
						player.sendPacket(new ExCuriousHouseLeave());
					}
				}

				_festivalStatus = ChaosFestivalStatus.PREPARING;

				if(_inviteTask != null)
				{
					_inviteTask.cancel(true);
					_inviteTask = null;
				}

				SystemMessage msg = SystemMessage.getSystemMessage(3782);
				msg.addNumber(3782);
				for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
				{
					if(getInstance().canParticipate(player) && !_invitedPlayersMap.containsKey(player.getObjectId()) && !_fightingNow.contains(player))
					{
						player.sendPacket(msg);
					}
				}

				return;
			}

			_festivalStatus = ChaosFestivalStatus.INVITING;

			for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
			{
				if(getInstance().canParticipate(player) && !_invitedPlayersMap.containsKey(player.getObjectId()) && !_fightingNow.contains(player))
				{
					player.sendPacket(new ExCuriousHouseState(ExCuriousHouseState.ChaosFestivalInviteState.INVITE));
				}
			}
		}
	}

	public static class TeleportTask implements Runnable
	{
		/**
		 * Уменьшает бан на участие у каждого игрока, имеющего запись об участии в фестивале.
		 */
		public void reducePenalties()
		{
			ChaosFestivalTable.getInstance().getFestivalEntries().values().stream().filter(entry -> entry.getSkipRounds() > 0).forEach(entry -> entry.setSkipRounds(entry.getSkipRounds() - 1));
		}

		/**
		 * Проверка возможности принятия участия.
		 */
		private void checkPlayers()
		{
			Collections.shuffle(_invitedPlayers);

			// Let's sort players by levels
			if(ConfigChaosFestival.CHAOS_FESTIVAL_BALANCE)
			{
				Collections.sort(_invitedPlayers, new PlayerComparator());
			}

			List<L2PcInstance> members = null;
			for(int i = 0, j = _invitedPlayers.size(); i < j; ++i)
			{
				if(i % ConfigChaosFestival.CHAOS_FESTIVAL_MAX_PLAYERS_PER_MATCH == 0)
				{
					if(members != null && !members.isEmpty())
					{
						teleportPlayers(members);
					}

					members = new FastList<>();
				}

				try
				{
					if(_invitedPlayers.get(i) != null)
					{
                        if (members != null) {
                            members.add(_invitedPlayers.get(i));
                        }
                    }
				}
				catch(NullPointerException ignored)
				{
				}
			}

			if(members != null && !members.isEmpty())
			{
				teleportPlayers(members);
			}
		}		
        
        @Override
		public void run()
		{
			if(_inviteTask != null)
			{
				_inviteTask.cancel(true);
				_inviteTask = null;
			}

			_festivalStatus = ChaosFestivalStatus.RUNNING;

			checkPlayers();
			reducePenalties();
		}

		private void teleportPlayers(List<L2PcInstance> players)
		{
			for(L2PcInstance player : players)
			{
				/*
				 * - Не находится в другом инстансе;
				 * - Инвентарь занят менее, чем на 80%;
				 * - Состоит в клане;
				 * - Клан 3+ уровня;
				 * - 76 уровень с третьей профессией;
				 * - Не ПК.
				 */
				if(!player.isGM())
				{
					if(player.getInstanceId() > 0 ||
						!player.isInventoryUnder(0.8, true) ||
						player.getClan() == null ||
						player.getLevel() < 85 || player.getClassId().level() < 4 ||
						player.hasBadReputation())
					{
						getInstance().banPlayer(player);
						_invitedPlayers.remove(player);
						_invitedPlayersMap.remove(player.getObjectId());
						players.remove(player);
						continue;
					}
				}

				if(player.isMounted())
				{
					player.dismount();
				}

				// Убираем петов
				for(L2Summon summon : player.getPets())
				{
					if(summon.isPet())
					{
						summon.getLocationController().decay();
						continue;
					}
					summon.setCurrentHp(summon.getMaxHp());
					summon.setCurrentMp(summon.getMaxMp());
				}

				// Убираем из группы
				if(player.getParty() != null)
				{
					player.leaveParty();
				}

				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp());
				player.setCurrentMp(player.getMaxMp());

				// Активируем доступные умения игрока
				for(L2Skill skill : player.getSkills().values())
				{
					long reuse = player.getSkillRemainingReuseTime(skill.getReuseHashCode());
					if(reuse > 0 && reuse - System.currentTimeMillis() < 15000)
					{
						player.enableSkill(skill);
					}
				}

				// Отменяем полетные трансформации
				if(player.isFlying() && player.getTransformationId() > 0)
				{
					player.untransform(true);
				}

				if(player.isInDuel())
				{
					player.setIsInDuel(0);
				}
			}

			getInstance().enterInstance(players);
		}



	}

	public static class EndTask implements Runnable
	{
		@Override
		public void run()
		{
			getInstance().cleanUp();
			getInstance().schedule();
		}
	}
}
