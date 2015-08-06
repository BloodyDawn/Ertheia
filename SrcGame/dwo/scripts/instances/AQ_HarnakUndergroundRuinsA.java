package dwo.scripts.instances;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExSendUIEvent;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Util;
import dwo.scripts.quests._10338_OvercomeTheRock;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jetbrains.annotations.Nullable;

/**
 * Quest for 4th profession class change.
 * @author Yorie
 */
public class AQ_HarnakUndergroundRuinsA extends Quest
{
	/**
	 * Tele to/out instance point.
	 */
	private static final int[] ENTRY_POINT = {-107918, 205824, -10872};
	private static final int[] EXIT_POINT = {-116120, 236477, -3088};
	// Classes
	private static final int YUL_ARCHER = 142;
	private static final int OTHELL_ROGUE = 141;
	private static final int TYRR_WARRIOR = 140;
	private static final int SIGEL_KNIGHT = 139;
	private static final int FEOH_WIZARD = 143;
	private static final int ISS_CHANTER = 144;
	private static final int WYNN_SUMMONER = 145;
	private static final int AEORE_HEALER = 146;
	private static final int BATHUS = 27463;
	private static final int LOTUS = 27468;
	private static final int BAMONTI = 27464;
	private static final int CARCASS = 27465;
	private static final int WEISS_ELE = 27469;
	private static final int WEISS_KHAN = 27466;
	private static final int SEKNUS = 27467;
	private static final int RAKZAN = 27462;
	/**
	 * List of spawns for the first room of instance.
	 */
	private static final int[][] FIRST_ROOM_SPAWNS = {
		{BATHUS, -107648, 206592, -10872, 49536}, {LOTUS, -108320, 206480, -10872, 49536},
		{BAMONTI, -107536, 206480, -10872, 49536}, {CARCASS, -107536, 206320, -10872, 49536},
		{WEISS_ELE, -108320, 206320, -10872, 49536}, {WEISS_KHAN, -108096, 206704, -10872, 49536},
		{SEKNUS, -108208, 206592, -10872, 49536}, {RAKZAN, -107776, 206704, -10872, 49536},
	};
	// Mini raids
	private static final int BATHUS_RAID = 27437;
	private static final int LOTUS_RAID = 27439;
	private static final int BAMONTI_RAID = 27442;
	private static final int CARCASS_RAID = 27438;
	private static final int WEISS_ELE_RAID = 27454;
	private static final int WEISS_KHAN_RAID = 27441;
	private static final int SEKNUS_RAID = 27443;
	private static final int RAKZAN_RAID = 27440;

	private static final int LIGHT_SOURCE_OF_LIGHT = 33501;
	private static final int FIRE_SOURCE_OF_LIGHT = 33557;
	private static final int DARK_SOURCE_OF_LIGHT = 33556;

	private static final int HARNAK_SPIRIT = 27445;

	private static final int SEAL_CONTROL_DEVICE = 33548;

	private static final int HERMUNCUS = 33340;

	private static final int FIRST_DOOR = 16240100;
	private static final int SECOND_DOOR = 16240102;

	// Npc ID, X, Y, Z, H
	/**
	 * Center of first room.
	 * There is where all monsters from first room are going to.
	 */
	private static final int[] FIRST_ROOM_CENTER = {-107926, 206320, -10872, 49536};

	// X, Y, Z, [H]
	/**
	 * Second room center.
	 * There is where mini raid will going to.
	 */
	private static final int[] SECOND_ROOM_CENTER = {-107929, 208863, -10876, 49536};
	private static final int[] THIRD_ROOM_CENTER = {-107926, 211407, -10872, 11005};
	/**
	 * Where's mini raid from second room is spawned.
	 */
	private static final int[] MINI_RAID_SPAWN = {-107926, 209248, -10872, 49536};
	/**
	 * These three spawns used after defeating mini raid.
	 */
	private static final int[][] TRIO_SPAWN = {
		{-107926, 209248, -10872, 49536}, {-107776, 209248, -10872, 49536}, {-108096, 209248, -10872, 49536},
	};
	/**
	 * Spawns for second room, 11 monster around player.
	 */
	private static final int[][] ROUND_SPAWNS = {
		{-107650, 209142, -10872, 49536}, {-108206, 209134, -10872, 49536}, {-108314, 208699, -10872, 49536},
		{-108314, 209022, -10872, 49536}, {-108314, 208855, -10872, 49536}, {-107542, 209024, -10872, 49536},
		{-107926, 209248, -10872, 49536}, {-108096, 209248, -10872, 49536}, {-107541, 208857, -10872, 49536},
		{-107541, 208697, -10872, 49536}, {-107776, 209248, -10872, 49536},
	};
	// Last monster in second room
	private static final int[] LAST_SECOND_ROOM_SPAWN = {-107926, 209248, -10872, 49536};
	/*
	 * Seal control devices for ending of quest and defeating Harnak Spirit.
	 */
	private static final int[][] SEAL_CONTROL_DEVICE_SPAWNS = {
		{-107790, 211409, -10872, 32768}, {-108046, 211409, -10872, 32768}
	};
	/**
	 * This 4 monster are spawned when HP of Harnak Spirit is under 50%.
	 */
	private static final int[][] ENDING_GUARD_SPAWNS = {
		{-108500, 211232, -10872, 49536}, {-108500, 211596, -10872, 49536}, {-107349, 211596, -10872, 49536},
		{-107349, 211232, -10872, 49536},
	};
	private static final int[] LAST_SOURCE_OF_LIGHT_SPAWN = {-107926, 210899, -10872, 49536};
	private static final int[] HERMUNKUS_SPAWN = {-107926, 212489, -10824, 49536};
	// Locations
	private static final int FIRST_ROOM_HALL = 1002051;
	private static final int SECOND_ROOM_ENTRANCE = 1002052;
	private static final int THIRD_ROOM_ENTRANCE = 1002053;
	private static AQ_HarnakUndergroundRuinsA _instance;

	public AQ_HarnakUndergroundRuinsA()
	{
		addSpawnId(BATHUS, LOTUS, BAMONTI, CARCASS, WEISS_ELE, WEISS_KHAN, SEKNUS, RAKZAN);
		addAttackId(BATHUS, LOTUS, BAMONTI, CARCASS, WEISS_ELE, WEISS_KHAN, SEKNUS, RAKZAN);
		addKillId(BATHUS, LOTUS, BAMONTI, CARCASS, WEISS_ELE, WEISS_KHAN, SEKNUS, RAKZAN);
		addKillId(CARCASS_RAID, BAMONTI_RAID, BATHUS_RAID, RAKZAN_RAID, WEISS_KHAN_RAID, SEKNUS_RAID, LOTUS_RAID, WEISS_ELE_RAID);
		addKillId(HARNAK_SPIRIT);
		addAttackId(HARNAK_SPIRIT);
		addEnterZoneId(FIRST_ROOM_HALL, SECOND_ROOM_ENTRANCE, THIRD_ROOM_ENTRANCE);
		addAskId(SEAL_CONTROL_DEVICE, 10338);
	}

	public static void main(String[] args)
	{
		_instance = new AQ_HarnakUndergroundRuinsA();
	}

	public static AQ_HarnakUndergroundRuinsA getInstance()
	{
		return _instance;
	}

	/***
	 * Вход в инстанс
	 * @param player игрок
	 */
	public void enterInstance(L2PcInstance player)
	{
		QuestState qs = player.getQuestState(_10338_OvercomeTheRock.class);

		if(qs == null || qs.getCond() != 2 && qs.getCond() != 3)
		{
			return;
		}

		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(world != null)
		{
			if(!(world instanceof HarnakWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if(inst != null)
			{
				teleportPlayer(player, ENTRY_POINT, world.instanceId);
			}
		}
		else
		{
			int awakenClassId = Util.getGeneralIdForAwaken(Util.getAwakenRelativeClass(player.getClassId().getId()));

			if(awakenClassId < 0)
			{
				return;
			}

			int instanceId = InstanceManager.getInstance().createDynamicInstance("AQ_HarnakUndergroundRuinsA.xml");
			world = new HarnakWorld(player);
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.HARNAK_UNDERGROUND_RUINS_2.getId();
			InstanceManager.getInstance().addWorld(world);
			world.allowed.add(player.getObjectId());
			((HarnakWorld) world).awakenClass = awakenClassId;
			teleportPlayer(player, ENTRY_POINT, instanceId);

			if(qs.getCond() == 2)
			{
				// Show start messages to player
				screenMessage(player, NpcStringId.WHO_ARE_YOU_INVADER, 5000, 2000);
				screenMessage(player, NpcStringId.TESTIFY_MY_PRESENSE, 3000, 7000);
				screenMessage(player, NpcStringId.OR_I_CANT_SEND, 5000, 10000);

				InstanceWorld finalizedWorld = world;
				// Loading instance after 10 seconds, off-like
				ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					finalizedWorld.status = 1;
					spawnNpc(player);
				}, 10000);
			}
			else if(qs.getCond() == 3)
			{
				openDoor((HarnakWorld) world, FIRST_DOOR, 0);
				openDoor((HarnakWorld) world, SECOND_DOOR, 0);
				addSpawn(HERMUNCUS, HERMUNKUS_SPAWN[0], HERMUNKUS_SPAWN[1], HERMUNKUS_SPAWN[2], HERMUNKUS_SPAWN[3], false, 0, false, player.getInstanceId());
				((HarnakWorld) world).disableZones();
			}
		}
	}

	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.getInstanceController().setInstanceId(instanceId);
		player.teleToInstance(new Location(coords[0], coords[1], coords[2]), instanceId);
	}

	private void screenMessage(L2PcInstance player, NpcStringId message, int time)
	{
		player.sendPacket(new ExShowScreenMessage(message, ExShowScreenMessage.TOP_CENTER, time));
	}

	private void screenMessage(L2PcInstance player, NpcStringId message, int time, long delay)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(() -> player.sendPacket(new ExShowScreenMessage(message, ExShowScreenMessage.TOP_CENTER, time)), delay);
	}

	private void npcSay(L2PcInstance player, L2Npc npc, int message, ChatType chatType)
	{
		if(player != null && npc != null)
		{
			player.sendPacket(new NS(npc.getObjectId(), chatType, npc.getNpcId(), message));
		}
	}

	private void npcSay(L2PcInstance player, L2Npc npc, int message)
	{
		npcSay(player, npc, message, ChatType.NPC_ALL);
	}

	private void npcSay(L2PcInstance player, L2Npc npc, int message, long delay)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(() -> npcSay(player, npc, message, ChatType.NPC_ALL), delay);
	}

	private void openDoor(HarnakWorld world, int doorId, long delay)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			Instance instanceWorld = InstanceManager.getInstance().getInstance(world.instanceId);
			if(instanceWorld != null)
			{
				for(L2DoorInstance door : instanceWorld.getDoors())
				{
					if(door != null && door.getDoorId() == doorId)
					{
						door.openMe();
						break;
					}
				}
			}
		}, delay);
	}

	private void spawnNpc(L2PcInstance player)
	{
		spawnNpc(player, 0);
	}

	/**
	 * Spawns NPCs in instance according to its state.
	 */
	private void spawnNpc(final L2PcInstance player, int npcId)
	{
		final HarnakWorld world;
		if(InstanceManager.getInstance().getPlayerWorld(player) instanceof HarnakWorld)
		{
			world = (HarnakWorld) InstanceManager.getInstance().getPlayerWorld(player);
		}
		else
		{
			return;
		}

		switch(world.status)
		{
			// First 8 monsters
			case 1:
				for(int[] spawn : FIRST_ROOM_SPAWNS)
				{
					if(spawn.length < 5)
					{
						continue;
					}

					L2Npc npc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, player.getInstanceId());
					npc.setIsNoRndWalk(true);
					world.addSpawn(1, npc);
				}
				break;
			// 3 lights of power after defeating 8 monsters
			case 2:
				world.status = 3;

				world.clearSpawns(1);

				// Spawning Sources of Power
				world.addSpawn(1, addSpawn(LIGHT_SOURCE_OF_LIGHT, -107827, 206882, -10872, 49536, false, 0, false, player.getInstanceId()));
				world.addSpawn(1, addSpawn(FIRE_SOURCE_OF_LIGHT, -108030, 206882, -10872, 49536, false, 0, false, player.getInstanceId()));
				world.addSpawn(1, addSpawn(DARK_SOURCE_OF_LIGHT, -107937, 206882, -10872, 49536, false, 0, false, player.getInstanceId()));

				break;
			case 3:
				world.status = 4;
				L2Npc raid = addSpawn(npcId, MINI_RAID_SPAWN[0], MINI_RAID_SPAWN[1], MINI_RAID_SPAWN[2], MINI_RAID_SPAWN[3], false, 0, false, player.getInstanceId());
				// Moving raid to the center of room
				ThreadPoolManager.getInstance().scheduleGeneral(() -> raid.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(SECOND_ROOM_CENTER[0], SECOND_ROOM_CENTER[1], SECOND_ROOM_CENTER[2])), 2000);
				npcSay(player, raid, 1811181, 3000L);
				break;
			// Three light monsters after slaying mini raid
			case 4:
			{
				world.status = 5;
				FastList<L2Npc> monsters = new FastList<>(3);
				L2Npc spawn = addSpawn(npcId, TRIO_SPAWN[0][0], TRIO_SPAWN[0][1], TRIO_SPAWN[0][2], TRIO_SPAWN[0][3], false, 0, false, player.getInstanceId());
				spawn.setIsNoRndWalk(true);
				monsters.add(spawn);
				spawn = addSpawn(npcId, TRIO_SPAWN[1][0], TRIO_SPAWN[1][1], TRIO_SPAWN[1][2], TRIO_SPAWN[1][3], false, 0, false, player.getInstanceId());
				spawn.setIsNoRndWalk(true);
				monsters.add(spawn);
				spawn = addSpawn(npcId, TRIO_SPAWN[2][0], TRIO_SPAWN[2][1], TRIO_SPAWN[2][2], TRIO_SPAWN[2][3], false, 0, false, player.getInstanceId());
				spawn.setIsNoRndWalk(true);
				monsters.add(spawn);
				// Aggroing to player after 5 seconds of respawn
				ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					for(L2Npc mob : monsters)
					{
						L2Attackable attackable = mob.getAttackable();
						if(attackable != null)
						{
							attackable.attackCharacter(player);
						}
					}
				}, 5000);
				break;
			}
			case 5:
				world.status = 6;
				// Spawn round of monsters after 2 seconds
				ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					FastList<L2Npc> spawns = new FastList<>(ROUND_SPAWNS.length);
					L2Npc spawn;
					for(int[] spawnInfo : ROUND_SPAWNS)
					{
						spawn = addSpawn(npcId, spawnInfo[0], spawnInfo[1], spawnInfo[2], spawnInfo[3], false, 0, false, player.getInstanceId());
						spawn.setIsNoRndWalk(true);
						spawns.add(spawn);
					}

					ThreadPoolManager.getInstance().scheduleGeneral(() -> {
						for(L2Npc npc : spawns)
						{
							L2Attackable attackable = npc.getAttackable();
							if(attackable != null)
							{
								attackable.attackCharacter(player);
							}
						}
					}, 2000);

					// Spawn power of light
					L2Npc defenderLight = addSpawn(LIGHT_SOURCE_OF_LIGHT, SECOND_ROOM_CENTER[0], SECOND_ROOM_CENTER[1], SECOND_ROOM_CENTER[2], 0, false, 0, false, player.getInstanceId());
					screenMessage(player, NpcStringId.I_WILL_HELP_YOU_AGAIN, 5000, 2000);
					defenderLight.setTarget(player);
					// Cast UD
					defenderLight.doCast(SkillTable.getInstance().getInfo(14700, 1));
					ThreadPoolManager.getInstance().scheduleGeneral(() -> defenderLight.getLocationController().delete(), 5000);
				}, 3000);
				break;
			case 6:
				world.status = 7;
				ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					L2Npc npc = addSpawn(npcId, LAST_SECOND_ROOM_SPAWN[0], LAST_SECOND_ROOM_SPAWN[1], LAST_SECOND_ROOM_SPAWN[2], LAST_SECOND_ROOM_SPAWN[3], false, 0, false, player.getInstanceId());
					npc.setIsNoRndWalk(true);
					// Aggroing monster after 2 seconds of respawning
					ThreadPoolManager.getInstance().scheduleGeneral(() -> {
						L2Attackable attackable = npc.getAttackable();

						if(attackable != null)
						{
							attackable.attackCharacter(player);
						}
					}, 2000);
				}, 2000);
				break;
			case 8:
			{
				world.status = 9;
				L2Npc npc;
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						L2Npc npc = addSpawn(HARNAK_SPIRIT, THIRD_ROOM_CENTER[0], THIRD_ROOM_CENTER[1], THIRD_ROOM_CENTER[2], THIRD_ROOM_CENTER[3], false, 0, false, player.getInstanceId());
						npc.setIsNoRndWalk(true);
						world.addSpawn(3, npc);

						// Spawn healer light of power
						final L2Npc healer = addSpawn(LIGHT_SOURCE_OF_LIGHT, LAST_SOURCE_OF_LIGHT_SPAWN[0], LAST_SOURCE_OF_LIGHT_SPAWN[1], LAST_SOURCE_OF_LIGHT_SPAWN[2], LAST_SOURCE_OF_LIGHT_SPAWN[3], false, 0, false, player.getInstanceId());

						// Running healing task...
						Runnable healTask = new Runnable()
						{
							@Override
							public void run()
							{
								if(healer == null || world.status >= 15)
								{
									return;
								}

								healer.setTarget(player);

								if(healer.getTarget() == null)
								{
									return;
								}

								healer.doCast(SkillTable.getInstance().getInfo(14736, 1));

								ThreadPoolManager.getInstance().scheduleGeneral(this, 4000);
							}
						};
						healTask.run();
						world.addSpawn(3, healer);
					}
				}, 20000);

				npc = addSpawn(HERMUNCUS, HERMUNKUS_SPAWN[0], HERMUNKUS_SPAWN[1], HERMUNKUS_SPAWN[2], HERMUNKUS_SPAWN[3], false, 0, false, player.getInstanceId());
				npc.setDisplayEffect(0x01);
				world.addSpawn(3, npc);

				break;
			}
			case 13:
				world.status = 14;
				// Spawn seal control devices
				L2Npc lastSpawn = null;
				for(int[] spawnInfo : SEAL_CONTROL_DEVICE_SPAWNS)
				{
					lastSpawn = addSpawn(SEAL_CONTROL_DEVICE, spawnInfo[0], spawnInfo[1], spawnInfo[2], spawnInfo[3], false, 0, false, player.getInstanceId());
					world.addSpawn(3, lastSpawn);
				}
				if(lastSpawn != null)
				{
					final L2Npc finalizedSpawn = lastSpawn;
					npcSay(player, lastSpawn, 1811226);

					// Schedule timer broadcasting
					ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
					{
						private int _seconds;

						@Override
						public void run()
						{
							if(world.sealDeviceTalkings >= 2 || world.status > 14)
							{
								return;
							}

							boolean runNextTime = true;
							++_seconds;
							if(_seconds == 10)
							{
								npcSay(player, finalizedSpawn, 1811227);
							}
							else if(_seconds == 20)
							{
								npcSay(player, finalizedSpawn, 1811228);
							}
							else if(_seconds == 30)
							{
								npcSay(player, finalizedSpawn, 1811229);
							}
							else if(_seconds == 40)
							{
								npcSay(player, finalizedSpawn, 1811230);
							}
							else if(_seconds == 50)
							{
								npcSay(player, finalizedSpawn, 1811231);
							}
							else if(_seconds == 55)
							{
								npcSay(player, finalizedSpawn, 1811232);
							}
							else if(_seconds == 56)
							{
								npcSay(player, finalizedSpawn, 1811233);
							}
							else if(_seconds == 57)
							{
								npcSay(player, finalizedSpawn, 1811234);
							}
							else if(_seconds == 58)
							{
								npcSay(player, finalizedSpawn, 1811235);
							}
							else if(_seconds == 59)
							{
								npcSay(player, finalizedSpawn, 1811236);
							}// Player failed to defeat Harnak Spirit!
							else if(_seconds >= 60)
							{
								failed(player);
								runNextTime = false;
							}

							if(runNextTime)
							{
								ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
							}
						}
					}, 1000);
				}

				// Spawn ending guards
				for(int[] spawnInfo : ENDING_GUARD_SPAWNS)
				{
					L2Npc npc = addSpawn(npcId, spawnInfo[0], spawnInfo[1], spawnInfo[2], spawnInfo[3], false, 0, false, player.getInstanceId());
					npc.setRunning();
					L2Attackable attackable = npc.getAttackable();
					if(attackable != null)
					{
						attackable.attackCharacter(player);
					}
					world.addSpawn(3, npc);
				}

				if(player != null)
				{
					L2Npc harnak = world.getSpawn(3, HARNAK_SPIRIT);
					if(harnak != null)
					{
						harnak.setTarget(player);
						harnak.doCast(SkillTable.getInstance().getInfo(14697, 1));
					}
				}
				break;
		}
	}

	/**
	 * Called when player successfully done quest.
	 */
	private void done(L2PcInstance player)
	{
		HarnakWorld world = getWorld(player);

		if(world != null)
		{
			for(FastList<L2Npc> spawn : world.getRoomSpawn(3).values())
			{
				if(spawn == null)
				{
					continue;
				}

				for(L2Npc npc : spawn)
				{
					npc.getLocationController().delete();
				}
			}
		}

		player.showQuestMovie(ExStartScenePlayer.SCENE_AWAKENING_BOSS_ENDING_A);

		ThreadPoolManager.getInstance().scheduleGeneral(() -> addSpawn(HERMUNCUS, HERMUNKUS_SPAWN[0], HERMUNKUS_SPAWN[1], HERMUNKUS_SPAWN[2], HERMUNKUS_SPAWN[3], false, 0, false, player.getInstanceId()), 15000);

		QuestState qs = player.getQuestState(_10338_OvercomeTheRock.class);

		if(qs != null)
		{
			qs.setCond(3);
			qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
	}

	/**
	 * This method called when 60 sec. timer is gone and player didn't talk to seal controllers.
	 * @param player Current player.
	 */
	private void failed(L2PcInstance player)
	{
		HarnakWorld world = getWorld(player);
		if(world == null)
		{
			return;
		}

		if(world.getRoomSpawn(3) != null)
		{
			for(FastList<L2Npc> spawn : world.getRoomSpawn(3).values())
			{
				if(spawn == null)
				{
					continue;
				}

				for(L2Npc npc : spawn)
				{
					if(npc.getNpcId() == HERMUNCUS)
					{
						npc.setDisplayEffect(0x00);
					}
					else
					{
						npc.getLocationController().delete();
					}
				}
			}
		}

		world.status = 15;

		player.showQuestMovie(ExStartScenePlayer.SCENE_AWAKENING_BOSS_ENDING_B);
		ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			InstanceManager.getInstance().destroyInstance(player.getInstanceId());
			teleportPlayer(player, EXIT_POINT, 0);
		}, 40000);
	}

	/**
	 * Moves an NPC to the center of first room and sets ready to die flag.
	 * @param npc NPC who will be moved.
	 */
	private void moveFirstRoomCenter(HarnakWorld world, L2Npc npc)
	{
		if(npc == null)
		{
			return;
		}

		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(FIRST_ROOM_CENTER[0], FIRST_ROOM_CENTER[1], FIRST_ROOM_CENTER[2]));
		world.setReadyToDie(npc.getNpcId());
	}

	/**
	 * In first room player can safely hit only monster that is at center of room.
	 * If player hit another monster, all monsters in the room aggroing to player.
	 * Check it.
	 * @param npc NPC that is attacked.
	 * @param attacker Current character.
	 */
	private void checkFirstRoomAggro(L2Npc npc, L2PcInstance attacker)
	{
		HarnakWorld world = getWorld(attacker);
		int npcId = npc.getNpcId();
		switch(npcId)
		{
			case RAKZAN:
			case BATHUS:
			case BAMONTI:
			case CARCASS:
			case WEISS_KHAN:
			case SEKNUS:
			case LOTUS:
			case WEISS_ELE:
				if(!world.getReadyToDie(npc.getNpcId()) && world.getRoomSpawn(1) != null)
				{
					for(FastList<L2Npc> spawns : world.getRoomSpawn(1).values())
					{
						for(L2Npc spawn : spawns)
						{
							L2Attackable attackable = spawn.getAttackable();
							if(attackable != null)
							{
								attackable.attackCharacter(attacker);
							}
						}
					}
				}
				break;
		}
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		HarnakWorld world = getWorld(attacker);
		if(world == null)
		{
			return null;
		}

		switch(world.status)
		{
			case 1:
				checkFirstRoomAggro(npc, attacker);
				break;
			case 9:
			case 10:
			case 11:
			case 12:
				switch(world.status)
				{
					case 9:
						world.status = 10;
						screenMessage(attacker, NpcStringId.WANT_RELEASE_SHACKLES_OF_LIGHT, 10000);
						break;
					case 10:
						if(npc.getCurrentHp() / npc.getMaxHp() <= 0.75)
						{
							world.status = 11;
							screenMessage(attacker, NpcStringId.SLAY_IMPOERATOR_THAT_IS_SERVANT_OF_LIGHT, 5000);
						}
						break;
					case 11:
						if(npc.getCurrentHp() / npc.getMaxHp() <= 0.6)
						{
							world.status = 12;
							screenMessage(attacker, NpcStringId.RELEASE_MY_SHACKLES_AND_I_GIVE_YOU_NEW_POWER, 5000);
						}
						break;
					case 12:
						if(npc.getCurrentHp() / npc.getMaxHp() <= 0.5)
						{
							world.status = 13;
							screenMessage(attacker, NpcStringId.I_HAVE_NO_POWER_TO_DEAL_WITH_DEVICE_OF_SIGNS_HELP_ME_DEFENT_IT, 5000);
							attacker.sendPacket(new ExSendUIEvent(attacker, 0, 0, 60, 0, "Оставшееся время"));

							int npcId = 0;
							switch(ClassId.getClassId(world.awakenClass).getId())
							{
								case YUL_ARCHER:
									npcId = CARCASS;
									break;
								case OTHELL_ROGUE:
									npcId = BAMONTI;
									break;
								case TYRR_WARRIOR:
									npcId = BATHUS;
									break;
								case SIGEL_KNIGHT:
									npcId = RAKZAN;
									break;
								case FEOH_WIZARD:
									npcId = WEISS_KHAN;
									break;
								case ISS_CHANTER:
									npcId = SEKNUS;
									break;
								case WYNN_SUMMONER:
									npcId = LOTUS;
									break;
								case AEORE_HEALER:
									npcId = WEISS_ELE;
									break;
							}

							spawnNpc(attacker, npcId);
						}
						break;
				}
				break;
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == 10338)
		{
			if(reply == 1)
			{
				HarnakWorld world = getWorld(player);
				if(world.status == 14)
				{
					++world.sealDeviceTalkings;
					if(world.sealDeviceTalkings >= 2)
					{
						world.status = 15;
						done(player);
					}
					npc.setDisplayEffect(0x02);
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		synchronized(this)
		{
			HarnakWorld world = getWorld(killer);
			if(world == null)
			{
				return null;
			}

			int spawnNpcId = -1;
			switch(npc.getNpcId())
			{
				case RAKZAN:
					switch(world.status)
					{
						case 1:
							moveFirstRoomCenter(world, world.getSpawn(1, BATHUS));
							npcSay(world.getPlayer(), world.getSpawn(1, BATHUS), 10338016);
							npcSay(world.getPlayer(), world.getSpawn(1, BATHUS), 10338017, 5000L);
							++world.firstRoomKills;
							break;
						case 5:
							++world.secondRoomTrioKills;
							break;
						case 6:
							++world.secondRoomRoundKills;
							break;
						case 7:
							world.secondRoomLastKill = true;
							break;
					}
					break;
				case BATHUS:
					switch(world.status)
					{
						case 1:
							moveFirstRoomCenter(world, world.getSpawn(1, BAMONTI));
							npcSay(world.getPlayer(), world.getSpawn(1, BAMONTI), 10338018);
							npcSay(world.getPlayer(), world.getSpawn(1, BAMONTI), 10338019, 5000L);
							++world.firstRoomKills;
							break;
						case 5:
							++world.secondRoomTrioKills;
							break;
						case 6:
							++world.secondRoomRoundKills;
							break;
						case 7:
							world.secondRoomLastKill = true;
							break;
					}
					break;
				case BAMONTI:
					switch(world.status)
					{
						case 1:
							moveFirstRoomCenter(world, world.getSpawn(1, CARCASS));
							npcSay(world.getPlayer(), world.getSpawn(1, CARCASS), 10338028);
							npcSay(world.getPlayer(), world.getSpawn(1, CARCASS), 10338029, 5000L);
							++world.firstRoomKills;
							break;
						case 5:
							++world.secondRoomTrioKills;
							break;
						case 6:
							++world.secondRoomRoundKills;
							break;
						case 7:
							world.secondRoomLastKill = true;
							break;
					}
					break;
				case CARCASS:
					switch(world.status)
					{
						case 1:
							moveFirstRoomCenter(world, world.getSpawn(1, WEISS_KHAN));
							npcSay(world.getPlayer(), world.getSpawn(1, WEISS_KHAN), 10338022);
							npcSay(world.getPlayer(), world.getSpawn(1, WEISS_KHAN), 10338023, 5000L);
							++world.firstRoomKills;
							break;
						case 5:
							++world.secondRoomTrioKills;
							break;
						case 6:
							++world.secondRoomRoundKills;
							break;
						case 7:
							world.secondRoomLastKill = true;
							break;
					}
					break;
				case WEISS_KHAN:
					switch(world.status)
					{
						case 1:
							moveFirstRoomCenter(world, world.getSpawn(1, SEKNUS));
							npcSay(world.getPlayer(), world.getSpawn(1, SEKNUS), 10338020);
							npcSay(world.getPlayer(), world.getSpawn(1, SEKNUS), 10338021, 5000L);
							++world.firstRoomKills;
							break;
						case 5:
							++world.secondRoomTrioKills;
							break;
						case 6:
							++world.secondRoomRoundKills;
							break;
						case 7:
							world.secondRoomLastKill = true;
							break;
					}
					break;
				case SEKNUS:
					switch(world.status)
					{
						case 1:
							moveFirstRoomCenter(world, world.getSpawn(1, LOTUS));
							npcSay(world.getPlayer(), world.getSpawn(1, LOTUS), 10338024);
							npcSay(world.getPlayer(), world.getSpawn(1, LOTUS), 10338025, 5000L);
							++world.firstRoomKills;
							break;
						case 5:
							++world.secondRoomTrioKills;
							spawnNpc(killer, SEKNUS);
							break;
						case 6:
							++world.secondRoomRoundKills;
							break;
						case 7:
							world.secondRoomLastKill = true;
							break;
					}
					break;
				case LOTUS:
					switch(world.status)
					{
						case 1:
							moveFirstRoomCenter(world, world.getSpawn(1, WEISS_ELE));
							npcSay(world.getPlayer(), world.getSpawn(1, WEISS_ELE), 10338026);
							npcSay(world.getPlayer(), world.getSpawn(1, WEISS_ELE), 10338027, 5000L);
							++world.firstRoomKills;
							break;
						case 5:
							++world.secondRoomTrioKills;
							break;
						case 6:
							++world.secondRoomRoundKills;
							break;
						case 7:
							world.secondRoomLastKill = true;
							break;
					}
					break;
				// Spawn trio after mini raid defeating
				case WEISS_ELE:
					switch(world.status)
					{
						case 1:
							++world.firstRoomKills;
							break;
						case 5:
							++world.secondRoomTrioKills;
							break;
						case 6:
							++world.secondRoomRoundKills;
							break;
						case 7:
							world.secondRoomLastKill = true;
							break;
					}
					break;
				case RAKZAN_RAID:
					spawnNpcId = RAKZAN;
				case BATHUS_RAID:
					if(spawnNpcId < 0)
					{
						spawnNpcId = BATHUS;
					}
				case BAMONTI_RAID:
					if(spawnNpcId < 0)
					{
						spawnNpcId = BAMONTI;
					}
				case CARCASS_RAID:
					if(spawnNpcId < 0)
					{
						spawnNpcId = CARCASS;
					}
				case WEISS_KHAN_RAID:
					if(spawnNpcId < 0)
					{
						spawnNpcId = WEISS_KHAN;
					}
				case SEKNUS_RAID:
					if(spawnNpcId < 0)
					{
						spawnNpcId = SEKNUS;
					}
				case LOTUS_RAID:
					if(spawnNpcId < 0)
					{
						spawnNpcId = LOTUS;
					}
				case WEISS_ELE_RAID:
					if(spawnNpcId < 0)
					{
						spawnNpcId = WEISS_ELE;
					}

					spawnNpc(killer, spawnNpcId);
					break;
			}

			if(world.status == 1 && world.firstRoomKills >= 8)
			{
				openDoor(world, FIRST_DOOR, 2000);

				// Pass into 2nd room
				world.status = 2;
				spawnNpc(killer);
			}
			else if(world.status == 5 && world.secondRoomTrioKills >= 3 || world.status == 6 && world.secondRoomRoundKills >= ROUND_SPAWNS.length)
			{
				spawnNpc(killer, npc.getNpcId());
			}
			else if(world.status == 7 && world.secondRoomLastKill)
			{
				openDoor(world, SECOND_DOOR, 2000);
				world.status = 8;
			}

			return super.onKill(npc, killer, isPet);
		}
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		HarnakWorld world = getWorld(npc);

		switch(npc.getNpcId())
		{
			// Move first monster (Rakzan) to the center of room
			case RAKZAN:
				moveFirstRoomCenter(world, npc);
				npcSay(world.getPlayer(), npc, 10338014);
				npcSay(world.getPlayer(), npc, 10338015, 5000L);
				break;
		}
		return super.onSpawn(npc);
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance)
		{
			InstanceWorld world = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if(world instanceof HarnakWorld)
			{
				if(((HarnakWorld) world).isZonesDisabled())
				{
					return null;
				}

				switch(zone.getId())
				{
					case FIRST_ROOM_HALL:
						if(world.status != 3)
						{
							break;
						}

						L2Npc lightSourceOfLight = ((HarnakWorld) world).getSpawn(1, LIGHT_SOURCE_OF_LIGHT);
						L2Npc fireSourceOfLight = ((HarnakWorld) world).getSpawn(1, FIRE_SOURCE_OF_LIGHT);
						L2Npc darkSourceOfLight = ((HarnakWorld) world).getSpawn(1, DARK_SOURCE_OF_LIGHT);
						npcSay((L2PcInstance) character, lightSourceOfLight, 10338036, ChatType.TELL);
						npcSay((L2PcInstance) character, lightSourceOfLight, 10338037, ChatType.TELL);
						screenMessage((L2PcInstance) character, NpcStringId.ADVENTURER_HELP_ME_TAKE_POWER_OF_GIANTS_AND_ATTACK, 5000);
						screenMessage((L2PcInstance) character, NpcStringId.I_NOT_POWERFUL_BUT_TRYING_HELP_YOU, 5000, 5000);

						if(character.isVisible())
						{
							if(lightSourceOfLight != null)
							{
								lightSourceOfLight.setTarget(character);
								lightSourceOfLight.doCast(SkillTable.getInstance().getInfo(14625, 1));
							}

							if(fireSourceOfLight != null)
							{
								fireSourceOfLight.setTarget(character);
								fireSourceOfLight.doCast(SkillTable.getInstance().getInfo(14625, 1));
							}

							if(darkSourceOfLight != null)
							{
								darkSourceOfLight.setTarget(character);
								darkSourceOfLight.doCast(SkillTable.getInstance().getInfo(14625, 1));
							}
						}

						// Despawn fountains after casting release of power ;)
						ThreadPoolManager.getInstance().scheduleGeneral(() -> {
							if(lightSourceOfLight != null)
							{
								lightSourceOfLight.getLocationController().delete();
							}

							if(fireSourceOfLight != null)
							{
								fireSourceOfLight.getLocationController().delete();
							}

							if(darkSourceOfLight != null)
							{
								darkSourceOfLight.getLocationController().delete();
							}
						}, 10000);
						((HarnakWorld) world).clearSpawns(1);
						break;
					case SECOND_ROOM_ENTRANCE:
						if(world.status != 3)
						{
							break;
						}

						screenMessage((L2PcInstance) character, NpcStringId.CONFIRM_MY_PRESENSE, 3000, 1000);
						screenMessage((L2PcInstance) character, NpcStringId.STRONG_WEAK_LIGHT_FOLLOWER_DARK_FOLLOWER, 3000, 4000);
						screenMessage((L2PcInstance) character, NpcStringId.THIS_IS_NOT_LIGHT_YOU_NEED_COFIRM_MY_PRESENSE_THEN_I_HANDLE_MYSELF, 10000, 7000);

						// Spawn mini raid
						int raidId = 0;
						switch(ClassId.getClassId(((HarnakWorld) world).awakenClass).getId())
						{
							case YUL_ARCHER:
								raidId = CARCASS_RAID;
								break;
							case OTHELL_ROGUE:
								raidId = BAMONTI_RAID;
								break;
							case TYRR_WARRIOR:
								raidId = BATHUS_RAID;
								break;
							case SIGEL_KNIGHT:
								raidId = RAKZAN_RAID;
								break;
							case FEOH_WIZARD:
								raidId = WEISS_KHAN_RAID;
								break;
							case ISS_CHANTER:
								raidId = SEKNUS_RAID;
								break;
							case WYNN_SUMMONER:
								raidId = LOTUS_RAID;
								break;
							case AEORE_HEALER:
								raidId = WEISS_ELE_RAID;
								break;
						}

						if(raidId > 0)
						{
							int finalizedRaidId = raidId;
							ThreadPoolManager.getInstance().scheduleGeneral(() -> spawnNpc((L2PcInstance) character, finalizedRaidId), 8000);
						}
						else
						{
							_log.error("Cannot spawn mini rb for Harnak Underground Ruins by awakening quest. Raid spawn data not found.");
						}

						break;
					case THIRD_ROOM_ENTRANCE:
						if(world.status != 8)
						{
							break;
						}

						((L2PcInstance) character).showQuestMovie(ExStartScenePlayer.SCENE_AWAKENING_BOSS_OPENING);
						// Spawn Harnak after 30 seconds
						ThreadPoolManager.getInstance().scheduleGeneral(() -> spawnNpc((L2PcInstance) character), 10000);
						break;
				}
			}
		}
		return super.onEnterZone(character, zone);
	}

	public HarnakWorld getWorld(L2Character character)
	{
		InstanceWorld instanceWorld = null;
		if(character.getInstanceId() > 0)
		{
			instanceWorld = InstanceManager.getInstance().getWorld(character.getInstanceId());
		}

		return instanceWorld instanceof HarnakWorld ? (HarnakWorld) instanceWorld : null;
	}

	private class HarnakWorld extends InstanceWorld
	{
		/**
		 * Awakening class for player
		 */
		public int awakenClass = -1;
		/**
		 * Kill counters.
		 */
		public int firstRoomKills;
		public int secondRoomRoundKills;
		public int secondRoomTrioKills;
		public boolean secondRoomLastKill;
		public int sealDeviceTalkings;
		/**
		 * Single spawned NPCs.
		 * Access: _spawns.get(room).get(npcId),
		 * where room is room number of current instance.
		 */
		FastMap<Integer, FastMap<Integer, FastList<L2Npc>>> _spawns = new FastMap<>();
		/**
		 * The monster that can be killed by player, when monster is ready to die, another monsters do not aggro on attack.
		 * Used for first room.
		 */
		FastMap<Integer, Boolean> _readyToDie = new FastMap<>();
		/**
		 * Active player.
		 */
		L2PcInstance _player;
		private boolean _zonesDisabled;

		public HarnakWorld(L2PcInstance player)
		{
			_player = player;
		}

		public L2PcInstance getPlayer()
		{
			return _player;
		}

		public void addSpawn(int room, L2Npc npc)
		{
			FastMap<Integer, FastList<L2Npc>> spawn = _spawns.get(room);
			if(spawn == null)
			{
				spawn = new FastMap<>(1);
			}
			if(!spawn.containsKey(npc.getNpcId()))
			{
				spawn.put(npc.getNpcId(), new FastList<>(1));
			}

			spawn.get(npc.getNpcId()).add(npc);
			_spawns.put(room, spawn);
		}

		@Nullable
		public L2Npc getSpawn(int room, int npcId)
		{
			return _spawns.get(room) != null && _spawns.get(room).get(npcId) != null && !_spawns.get(room).get(npcId).isEmpty() ? _spawns.get(room).get(npcId).get(0) : null;
		}

		public void setReadyToDie(int npcId)
		{
			_readyToDie.put(npcId, true);
		}

		public boolean getReadyToDie(int npcId)
		{
			return _readyToDie.containsKey(npcId) ? _readyToDie.get(npcId) : false;
		}

		public FastMap<Integer, FastList<L2Npc>> getRoomSpawn(int room)
		{
			return _spawns.get(room);
		}

		public void clearSpawns(int room)
		{
			if(_spawns.containsKey(room) && _spawns.get(room) != null)
			{
				_spawns.get(room).clear();
			}
		}

		public void disableZones()
		{
			_zonesDisabled = true;
		}

		public boolean isZonesDisabled()
		{
			return _zonesDisabled;
		}
	}
}