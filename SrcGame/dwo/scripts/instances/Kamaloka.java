package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class Kamaloka extends Quest
{
	/*
	 * Reset time for all kamaloka
	 * Default: 6:30AM on server time
	 */
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;

	/*
	 * Time after which instance without players will be destroyed
	 * Default: 5 minutes
	 */
	private static final int EMPTY_DESTROY_TIME = 5;

	/*
	 * Time to destroy instance (and eject players away) after boss defeat
	 * Default: 5 minutes
	 */
	private static final int EXIT_TIME = 5;

	/*
	 * Maximum level difference between players level and kamaloka level
	 * Default: 5
	 */
	private static final int MAX_LEVEL_DIFFERENCE = 5;

	/*
	 * If true shaman in the first room will have same npcId as other mobs, making radar useless
	 * Default: true (but not retail like)
	 */
	private static final boolean STEALTH_SHAMAN = true;

	/*
	 * Hardcoded instance ids for kamaloka
	 */
	private static final int[] INSTANCE_IDS = {
		InstanceZoneId.KAMALOKA_HALL_OF_THE_ABYSS_23.getId(), InstanceZoneId.KAMALOKA_HALL_OF_THE_ABYSS_26.getId(),
		InstanceZoneId.KAMALOKA_LABYRINTH_OF_THE_ABYSS_29.getId(),

		InstanceZoneId.KAMALOKA_HALL_OF_THE_ABYSS_33.getId(), InstanceZoneId.KAMALOKA_HALL_OF_THE_ABYSS_36.getId(),
		InstanceZoneId.KAMALOKA_LABYRINTH_OF_THE_ABYSS_39.getId(),

		InstanceZoneId.KAMALOKA_HALL_OF_THE_ABYSS_43.getId(), InstanceZoneId.KAMALOKA_HALL_OF_THE_ABYSS_46.getId(),
		InstanceZoneId.KAMALOKA_LABYRINTH_OF_THE_ABYSS_49.getId(),

		InstanceZoneId.KAMALOKA_HALL_OF_THE_ABYSS_53.getId(), InstanceZoneId.KAMALOKA_HALL_OF_THE_ABYSS_56.getId(),
		InstanceZoneId.KAMALOKA_LABYRINTH_OF_THE_ABYSS_59.getId(),

		InstanceZoneId.KAMALOKA_HALL_OF_THE_ABYSS_63.getId(), InstanceZoneId.KAMALOKA_HALL_OF_THE_ABYSS_66.getId(),
		InstanceZoneId.KAMALOKA_LABYRINTH_OF_THE_ABYSS_69.getId(),

		InstanceZoneId.KAMALOKA_HALL_OF_THE_ABYSS_73.getId(), InstanceZoneId.KAMALOKA_LABYRINTH_OF_THE_ABYSS_78.getId(),
		InstanceZoneId.KAMALOKA_LABYRINTH_OF_THE_ABYSS_81.getId(),
		InstanceZoneId.KAMALOKA_LABYRINTH_OF_THE_ABYSS_83.getId()
	};

	/*
	 * Level of the kamaloka
	 */
	private static final int[] LEVEL = {23, 26, 29, 33, 36, 39, 43, 46, 49, 53, 56, 59, 63, 66, 69, 73, 78, 81, 83};

	/*
	 * Duration of the instance, minutes
	 */
	private static final int[] DURATION = {30, 30, 45, 30, 30, 45, 30, 30, 45, 30, 30, 45, 30, 30, 45, 30, 45, 45, 45};

	/*
	 * Maximum party size for the instance
	 */
	private static final int[] MAX_PARTY_SIZE = {6, 6, 7, 6, 6, 7, 6, 6, 7, 6, 6, 7, 6, 6, 7, 6, 7, 7, 7};

	/*
	 * Teleport points into instances
	 * 
	 * x, y, z
	 */
	private static final int[][] TELEPORTS = {
		{-88429, -220629, -7903}, {-82464, -219532, -7899}, {-10700, -174882, -10936}, // -76280, -185540, -10936
		{-89683, -213573, -8106}, {-81413, -213568, -8104}, {-10700, -174882, -10936}, // -76280, -174905, -10936
		{-89759, -206143, -8120}, {-81415, -206078, -8107}, {-10700, -174882, -10936}, {-56999, -219856, -8117},
		{-48794, -220261, -8075}, {-10700, -174882, -10936}, {-56940, -212939, -8072}, {-55566, -206139, -8120},
		{-10700, -174882, -10936}, {-49805, -206139, -8117}, {-10700, -174882, -10936}, {-10700, -174882, -10936},
		{22010, -174867, -10904}
	};

	private static final int[] CrumaTeleportIds = {63, 64, 66};
	private static final int[] InsolenceTeleportIds = {67, 69, 70, 72};

	/*
	 * Respawn delay for the mobs in the first room, seconds
	 * Default: 25
	 */
	private static final int FIRST_ROOM_RESPAWN_DELAY = 25;

	/*
	 * First room information, null if room not spawned
	 * Skill is casted on the boss when shaman is defeated and mobs respawn stopped
	 * Default: 5699 (decrease pdef)
	 * 
	 * shaman npcId, minions npcId, skillId, skillLvl
	 */
	private static final int[][] FIRST_ROOM = {
		null, null, {22485, 22486, 5699, 1}, null, null, {22488, 22489, 5699, 2}, null, null, {22491, 22492, 5699, 3},
		null, null, {22494, 22495, 5699, 4}, null, null, {22497, 22498, 5699, 5}, null, {22500, 22501, 5699, 6},
		{22503, 22504, 5699, 7}, {25706, 25707, 5699, 7}
	};

	/*
	 * First room spawns, null if room not spawned
	 * 
	 * x, y, z
	 */
	private static final int[][][] FIRST_ROOM_SPAWNS = {
		null, null, {
		{-12381, -174973, -10955}, {-12413, -174905, -10955}, {-12377, -174838, -10953}, {-12316, -174903, -10953},
		{-12326, -174786, -10953}, {-12330, -175024, -10953}, {-12211, -174900, -10955}, {-12238, -174849, -10953},
		{-12233, -174954, -10953}
	}, null, null, {
		{-12381, -174973, -10955}, {-12413, -174905, -10955}, {-12377, -174838, -10953}, {-12316, -174903, -10953},
		{-12326, -174786, -10953}, {-12330, -175024, -10953}, {-12211, -174900, -10955}, {-12238, -174849, -10953},
		{-12233, -174954, -10953}
	}, null, null, {
		{-12381, -174973, -10955}, {-12413, -174905, -10955}, {-12377, -174838, -10953}, {-12316, -174903, -10953},
		{-12326, -174786, -10953}, {-12330, -175024, -10953}, {-12211, -174900, -10955}, {-12238, -174849, -10953},
		{-12233, -174954, -10953}
	}, null, null, {
		{-12381, -174973, -10955}, {-12413, -174905, -10955}, {-12377, -174838, -10953}, {-12316, -174903, -10953},
		{-12326, -174786, -10953}, {-12330, -175024, -10953}, {-12211, -174900, -10955}, {-12238, -174849, -10953},
		{-12233, -174954, -10953}
	}, null, null, {
		{-12381, -174973, -10955}, {-12413, -174905, -10955}, {-12377, -174838, -10953}, {-12316, -174903, -10953},
		{-12326, -174786, -10953}, {-12330, -175024, -10953}, {-12211, -174900, -10955}, {-12238, -174849, -10953},
		{-12233, -174954, -10953}
	}, null, {
		{-12381, -174973, -10955}, {-12413, -174905, -10955}, {-12377, -174838, -10953}, {-12316, -174903, -10953},
		{-12326, -174786, -10953}, {-12330, -175024, -10953}, {-12211, -174900, -10955}, {-12238, -174849, -10953},
		{-12233, -174954, -10953}
	}, {
		{-12381, -174973, -10955}, {-12413, -174905, -10955}, {-12377, -174838, -10953}, {-12316, -174903, -10953},
		{-12326, -174786, -10953}, {-12330, -175024, -10953}, {-12211, -174900, -10955}, {-12238, -174849, -10953},
		{-12233, -174954, -10953}
	}, {
		{20409, -174827, -10912}, {20409, -174947, -10912}, {20494, -174887, -10912}, {20494, -174767, -10912},
		{20614, -174887, -10912}, {20579, -174827, -10912}, {20579, -174947, -10912}, {20494, -175007, -10912},
		{20374, -174887, -10912}
	}
	};

	/*
	 * Second room information, null if room not spawned
	 * Skill is casted on the boss when all mobs are defeated
	 * Default: 5700 (decrease mdef)
	 * 
	 * npcId, skillId, skillLvl
	 */
	private static final int[][] SECOND_ROOM = {
		null, null, {22487, 5700, 1}, null, null, {22490, 5700, 2}, null, null, {22493, 5700, 3}, null, null,
		{22496, 5700, 4}, null, null, {22499, 5700, 5}, null, {22502, 5700, 6}, {22505, 5700, 7}, {25708, 5700, 7}

	};

	/*
	 * Spawns for second room, null if room not spawned
	 * 
	 * x, y, z
	 */
	private static final int[][][] SECOND_ROOM_SPAWNS = {
		null, null, {
		{-14547, -174901, -10690}, {-14543, -175030, -10690}, {-14668, -174900, -10690}, {-14538, -174774, -10690},
		{-14410, -174904, -10690}
	}, null, null, {
		{-14547, -174901, -10690}, {-14543, -175030, -10690}, {-14668, -174900, -10690}, {-14538, -174774, -10690},
		{-14410, -174904, -10690}
	}, null, null, {
		{-14547, -174901, -10690}, {-14543, -175030, -10690}, {-14668, -174900, -10690}, {-14538, -174774, -10690},
		{-14410, -174904, -10690}
	}, null, null, {
		{-14547, -174901, -10690}, {-14543, -175030, -10690}, {-14668, -174900, -10690}, {-14538, -174774, -10690},
		{-14410, -174904, -10690}
	}, null, null, {
		{-14547, -174901, -10690}, {-14543, -175030, -10690}, {-14668, -174900, -10690}, {-14538, -174774, -10690},
		{-14410, -174904, -10690}
	}, null, {
		{-14547, -174901, -10690}, {-14543, -175030, -10690}, {-14668, -174900, -10690}, {-14538, -174774, -10690},
		{-14410, -174904, -10690}
	}, {
		{-14547, -174901, -10690}, {-14543, -175030, -10690}, {-14668, -174900, -10690}, {-14538, -174774, -10690},
		{-14410, -174904, -10690}
	}, {
		{18175, -174991, -10653}, {18070, -174890, -10655}, {18157, -174886, -10655}, {18249, -174885, -10653},
		{18144, -174821, -10648}
	}
	};

	// miniboss info
	// skill is casted on the boss when miniboss is defeated
	// npcId, x, y, z, skill id, skill level
	/*
	 * Miniboss information, null if miniboss not spawned
	 * Skill is casted on the boss when miniboss is defeated
	 * Default: 5701 (decrease patk)
	 * 
	 * npcId, x, y, z, skillId, skillLvl
	 */
	private static final int[][] MINIBOSS = {
		null, null, {25616, -16874, -174900, -10427, 5701, 1}, null, null, {25617, -16874, -174900, -10427, 5701, 2},
		null, null, {25618, -16874, -174900, -10427, 5701, 3}, null, null, {25619, -16874, -174900, -10427, 5701, 4},
		null, null, {25620, -16874, -174900, -10427, 5701, 5}, null, {25621, -16874, -174900, -10427, 5701, 6},
		{25622, -16874, -174900, -10427, 5701, 7}, {25709, 15828, -174885, -10384, 5701, 7}
	};

	/*
	 * Bosses of the kamaloka
	 * Instance ends when boss is defeated
	 * 
	 * npcId, x, y, z
	 */
	private static final int[][] BOSS = {
		{18554, -88998, -220077, -7892}, {18555, -81891, -220078, -7893}, {29129, -20659, -174903, -9983},
		{18558, -89183, -213564, -8100}, {18559, -81937, -213566, -8100}, {29132, -20659, -174903, -9983},
		{18562, -89054, -206144, -8115}, {18564, -81937, -206077, -8100}, {29135, -20659, -174903, -9983},
		{18566, -56281, -219859, -8115}, {18568, -49336, -220260, -8068}, {29138, -20659, -174903, -9983},
		{18571, -56415, -212939, -8068}, {18573, -56281, -206140, -8115}, {29141, -20659, -174903, -9983},
		{18577, -49084, -206140, -8115}, {29144, -20659, -174903, -9983}, {29147, -20659, -174903, -9983},
		{25710, 12047, -174887, -9944}
	};

	/*
	 * Escape telepoters spawns, null if not spawned
	 * 
	 * x, y, z
	 */
	private static final int[][] TELEPORTERS = {
		null, null, {-10865, -174905, -10944}, null, null, {-10865, -174905, -10944}, null, null,
		{-10865, -174905, -10944}, null, null, {-10865, -174905, -10944}, null, null, {-10865, -174905, -10944}, null,
		{-10865, -174905, -10944}, {-10865, -174905, -10944}, {21837, -174885, -10904}
	};

	/*
	 * Escape teleporter npcId
	 */
	private static final int TELEPORTER = 32496;

	/*
	 * Kamaloka captains (start npc's) npcIds.
	 */
	private static final int[] CAPTAINS = {30332, 30071, 30916, 30196, 31981, 31340};

	public Kamaloka()
	{
		addFirstTalkId(TELEPORTER);
		addAskId(CAPTAINS, -588);
		addAskId(TELEPORTER, -588);

		for(int[] mob : FIRST_ROOM) //shaman
		{
			if(mob != null)
			{
				if(STEALTH_SHAMAN)
				{
					addKillId(mob[1]);
				}
				else
				{
					addKillId(mob[0]);
				}
			}
		}
		for(int[] mob : SECOND_ROOM)
		{
			if(mob != null)
			{
				addKillId(mob[0]);
			}
		}
		for(int[] mob : MINIBOSS)
		{
			if(mob != null)
			{
				addKillId(mob[0]);
			}
		}
		for(int[] mob : BOSS)
		{
			addKillId(mob[0]);
		}
	}

	/**
	 * Check if party with player as leader allowed to enter
	 *
	 * @param player party leader
	 * @param index (0-18) index of the kamaloka in arrays
	 *
	 * @return true if party allowed to enter
	 */
	private static boolean checkConditions(L2PcInstance player, int index)
	{
		L2Party party = player.getParty();
		// player must be in party
		if(party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		// ...and be party leader
		if(!party.getLeader().equals(player))
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		// party must not exceed max size for selected instance
		if(party.getMemberCount() > MAX_PARTY_SIZE[index])
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}

		// get level of the instance
		int level = LEVEL[index];
		// and client name
		String instanceName = InstanceManager.getInstance().getInstanceIdName(INSTANCE_IDS[index]);

		Map<Integer, Long> instanceTimes;
		// for each party member
		for(L2PcInstance partyMember : party.getMembers())
		{
			// player level must be in range
			if(Math.abs(partyMember.getLevel() - level) > MAX_LEVEL_DIFFERENCE)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(partyMember));
				return false;
			}
			// player must be near party leader
			if(!partyMember.isInsideRadius(player, 1000, true, true))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED).addPcName(partyMember));
				return false;
			}
			// get instances reenter times for player
			instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(partyMember.getObjectId());
			if(instanceTimes != null)
			{
				for(Map.Entry<Integer, Long> integerLongEntry : instanceTimes.entrySet())
				{
					// find instance with same name (kamaloka or labyrinth)
					if(!instanceName.equals(InstanceManager.getInstance().getInstanceIdName(integerLongEntry.getKey())))
					{
						continue;
					}
					// if found instance still can't be reentered - exit
					if(System.currentTimeMillis() < integerLongEntry.getValue())
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(partyMember));
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Called on instance finish and handles reenter time for instance
	 * @param world instanceWorld
	 */
	private static void finishInstance(InstanceWorld world)
	{
		if(world instanceof KamaWorld)
		{
			Calendar reenter = Calendar.getInstance();
			reenter.set(Calendar.MINUTE, RESET_MIN);
			// if time is >= RESET_HOUR - roll to the next day
			if(reenter.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
			{
				reenter.add(Calendar.DATE, 1);
			}
			reenter.set(Calendar.HOUR_OF_DAY, RESET_HOUR);

			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
			sm.addString(InstanceManager.getInstance().getInstanceIdName(world.templateId));

			// set instance reenter time for all allowed players
			for(int objectId : world.allowed)
			{
				L2PcInstance obj = WorldManager.getInstance().getPlayer(objectId);
				if(obj != null && obj.isOnline())
				{
					InstanceManager.getInstance().setInstanceTime(objectId, world.templateId, reenter.getTimeInMillis());
					obj.sendPacket(sm);
				}
			}

			// destroy instance after EXIT_TIME
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			inst.setDuration(EXIT_TIME * 60000);
			inst.setEmptyDestroyTime(0);
		}
	}

	public static void main(String[] args)
	{
		new Kamaloka();
	}

	/**
	 * Handling enter of the players into kamaloka
	 *
	 * @param player party leader
	 * @param index (0-18) kamaloka index in arrays
	 */
	private void enterInstance(L2PcInstance player, int index)
	{
		synchronized(this)
		{
			int templateId;
			try
			{
				templateId = INSTANCE_IDS[index];
			}
			catch(ArrayIndexOutOfBoundsException e)
			{
				_log.log(Level.ERROR, getClass().getSimpleName() + ": ArrayIndexOutOfBoundsException while enterInstance");
				return;
			}

			// check for existing instances for this player
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			// player already in the instance
			if(world != null)
			{
				// but not in kamaloka
				if(!(world instanceof KamaWorld) || world.templateId != templateId)
				{
					player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
					return;
				}
				// check for level difference again on reenter
				if(Math.abs(player.getLevel() - LEVEL[((KamaWorld) world).index]) > MAX_LEVEL_DIFFERENCE)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(player));
					return;
				}
				// check what instance still exist
				Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
				if(inst != null)
				{
					player.teleToInstance(new Location(TELEPORTS[index]), world.instanceId);
				}
			}
			// Creating new kamaloka instance
			else
			{
				if(!checkConditions(player, index))
				{
					return;
				}

				// Creating dynamic instance without template
				int instanceId = InstanceManager.getInstance().createDynamicInstance(null);
				Instance inst = InstanceManager.getInstance().getInstance(instanceId);
				// set name for the kamaloka
				inst.setName(InstanceManager.getInstance().getInstanceIdName(templateId));
				// set return location
				inst.setSpawnLoc(player.getLoc());
				// disable summon friend into instance
				inst.setAllowSummon(false);
				// set duration and empty destroy time
				inst.setDuration(DURATION[index] * 60000);
				inst.setEmptyDestroyTime(EMPTY_DESTROY_TIME * 60000);

				// Creating new instanceWorld, using our instanceId and templateId
				world = new KamaWorld();
				world.instanceId = instanceId;
				world.templateId = templateId;
				// set index for easy access to the arrays
				((KamaWorld) world).index = index;
				InstanceManager.getInstance().addWorld(world);
				world.status = 0;
				// spawn npcs
				spawnKama((KamaWorld) world);

				// and finally teleport party into instance
				L2Party party = player.getParty();
				for(L2PcInstance partyMember : party.getMembers())
				{
					world.allowed.add(partyMember.getObjectId());
					player.teleToInstance(new Location(TELEPORTS[index]), instanceId);
				}
			}
		}
	}

	/**
	 * Spawn all NPCs in kamaloka
	 * @param world instanceWorld
	 */
	private void spawnKama(KamaWorld world)
	{
		int[] npcs;
		int[][] spawns;
		L2Npc npc;
		int index = world.index;

		// first room
		npcs = FIRST_ROOM[index];
		spawns = FIRST_ROOM_SPAWNS[index];
		if(npcs != null)
		{
			world.firstRoom = new ArrayList<>(spawns.length - 1);
			int shaman = Rnd.get(spawns.length); // random position for shaman

			for(int i = 0; i < spawns.length; i++)
			{
				if(i == shaman)
				{
					// stealth shaman use same npcId as other mobs
					npc = addSpawn(STEALTH_SHAMAN ? npcs[1] : npcs[0], spawns[i][0], spawns[i][1], spawns[i][2], 0, false, 0, false, world.instanceId);
					world.shaman = npc.getObjectId();
				}
				else
				{
					npc = addSpawn(npcs[1], spawns[i][0], spawns[i][1], spawns[i][2], 0, false, 0, false, world.instanceId);
					L2Spawn spawn = npc.getSpawn();
					spawn.setRespawnDelay(FIRST_ROOM_RESPAWN_DELAY);
					spawn.setAmount(1);
					spawn.startRespawn();
					world.firstRoom.add(spawn); //store mobs spawns
				}
				npc.setIsNoRndWalk(true);
			}
		}

		// second room
		npcs = SECOND_ROOM[index];
		spawns = SECOND_ROOM_SPAWNS[index];
		if(npcs != null)
		{
			world.secondRoom = new ArrayList<>(spawns.length);

			for(int[] spawn : spawns)
			{
				npc = addSpawn(npcs[0], spawn[0], spawn[1], spawn[2], 0, false, 0, false, world.instanceId);
				npc.setIsNoRndWalk(true);
				world.secondRoom.add(npc.getObjectId());
			}
		}

		// miniboss
		if(MINIBOSS[index] != null)
		{
			npc = addSpawn(MINIBOSS[index][0], MINIBOSS[index][1], MINIBOSS[index][2], MINIBOSS[index][3], 0, false, 0, false, world.instanceId);
			npc.setIsNoRndWalk(true);
			world.miniBoss = npc.getObjectId();
		}

		// escape teleporter
		if(TELEPORTERS[index] != null)
		{
			addSpawn(TELEPORTER, TELEPORTERS[index][0], TELEPORTERS[index][1], TELEPORTERS[index][2], 0, false, 0, false, world.instanceId);
			// Спауним Лией
			if(ArrayUtils.contains(CrumaTeleportIds, world.templateId))
			{
				addSpawn(33155, TELEPORTERS[index][0] + 100, TELEPORTERS[index][1], TELEPORTERS[index][2], 0, false, 0, false, world.instanceId);
			}
			else if(ArrayUtils.contains(InsolenceTeleportIds, world.templateId))
			{
				addSpawn(33406, TELEPORTERS[index][0] + 100, TELEPORTERS[index][1], TELEPORTERS[index][2], 0, false, 0, false, world.instanceId);
			}
		}
		// boss
		npc = addSpawn(BOSS[index][0], BOSS[index][1], BOSS[index][2], BOSS[index][3], 0, false, 0, false, world.instanceId);
		((L2MonsterInstance) npc).setOnKillDelay(100);
		world.boss = npc;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(npc.getNpcId())
		{
			case TELEPORTER:
				if(reply == 9981)
				{
					L2Party party = player.getParty();
					// only party leader can talk with escape teleporter
					if(party != null && party.isLeader(player))
					{
						InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
						if(world instanceof KamaWorld)
						{
							// party members must be in the instance
							if(world.allowed.contains(player.getObjectId()))
							{
								Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);

								// teleports entire party away
								party.getMembers().stream().filter(partyMember -> partyMember != null && partyMember.getInstanceId() == world.instanceId).forEach(partyMember -> partyMember.teleToInstance(inst.getReturnLoc(), 0));
							}
						}
					}
				}
				break;
			case 30332:
				switch(reply)
				{
					// Войти в Камалоку (Зал Бездны). (Уровень: 23)
					case 9981:
						enterInstance(player, 0);
						break;
					// Войти в Камалоку (Зал Бездны). (Уровень: 26)
					case 9982:
						enterInstance(player, 1);
						break;
					// Войти в Окрестности Камалоки (Зал Бездны). (Уровень: 26, очки ПА: 2500)
					case 9983:
						enterInstance(player, 1);  // TODO
						break;
					// Войти в Камалоку (Лабиринт Бездны). (Уровень: 29)
					case 9984:
						enterInstance(player, 2);
						break;
				}
			case 30071:
				switch(reply)
				{
					// Войти в Камалоку, Зал Бездны. (Уровень 33)
					case 9981:
						enterInstance(player, 3);
						break;
					// Войти в Камалоку, Зал Бездны. (Уровень 36)
					case 9982:
						enterInstance(player, 4);
						break;
					// Войти в Земли Камалоки, Зал Бездны. (Уровень 36, необходимо 2500 очков ПА)
					case 9983:
						enterInstance(player, 4);  // TODO
						break;
					// Войти в Земли Камалоки, Лабиринт Бездны (Уровень 39)
					case 9984:
						enterInstance(player, 5);
						break;
				}
			case 30916:
				switch(reply)
				{
					// Войти в Камалоку, Зал Бездны (Уровень: 43)
					case 9981:
						enterInstance(player, 6);
						break;
					// Войти в Камалоку, Зал Бездны (Уровень: 46)
					case 9982:
						enterInstance(player, 7);
						break;
					// Войти в Окрестности Камалоки, Зал Бездны (Уровень: 46, очки ПА: 2500)
					case 9983:
						enterInstance(player, 7);  // TODO
						break;
					// Войти в Камалоку, Лабиринт Бездны (Уровень: 49)
					case 9984:
						enterInstance(player, 8);
						break;
				}
			case 30196:
				switch(reply)
				{
					// Войти в Камалоку (Зал Бездны) - 53-й уровень
					case 9981:
						enterInstance(player, 9);
						break;
					// Войти в Камалоку (Зал Бездны) - 56-й уровень
					case 9982:
						enterInstance(player, 10);
						break;
					// Войти в Камалоку (Зал Бездны) - 56-й уровень, необходимо 2500 очков ПА
					case 9983:
						enterInstance(player, 10);  // TODO
						break;
					// Войти в Камалоку (Лабиринт Бездны) - 59-й уровень
					case 9984:
						enterInstance(player, 11);
						break;
				}
			case 31981:
				switch(reply)
				{
					// Войти в Камалоку, Зал Бездны (Уровень: 63)
					case 9981:
						enterInstance(player, 12);
						break;
					// Войти в Камалоку, Зал Бездны (Уровень: 66)
					case 9982:
						enterInstance(player, 13);
						break;
					// Войти в Окрестности Камалоки, Зал Бездны (Уровень: 66, очки ПА: 2500)
					case 9983:
						enterInstance(player, 13);  // TODO
						break;
					// Войти в Камалоку, Лабиринт Бездны (Уровень: 69)
					case 9984:
						enterInstance(player, 14);
						break;
				}
			case 31340:
				switch(reply)
				{
					// Войти в Камалоку, Зал Бездны (Уровень 73)
					case 9981:
						enterInstance(player, 15);
						break;
					// Войти в Камалоку, Лабиринт Бездны (Уровень 78)
					case 9982:
						enterInstance(player, 16);
						break;
					// Войти в Камалоку, Лабиринт Бездны (Уровень 81)
					case 9983:
						enterInstance(player, 17);
						break;
					// Войти в Камалоку, Лабиринт Бездны (Уровень 83)
					case 9984:
						enterInstance(player, 18);
						break;
				}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpWorld instanceof KamaWorld)
		{
			KamaWorld world = (KamaWorld) tmpWorld;
			int objectId = npc.getObjectId();

			// first room was spawned ?
			if(world.firstRoom != null)
			{
				// is shaman killed ?
				if(world.shaman != 0 && world.shaman == objectId)
				{
					world.shaman = 0;
					// stop respawn of the minions
					world.firstRoom.stream().filter(spawn -> spawn != null).forEach(L2Spawn::stopRespawn);
					world.firstRoom.clear();
					world.firstRoom = null;

					if(world.boss != null)
					{
						int skillId = FIRST_ROOM[world.index][2];
						int skillLvl = FIRST_ROOM[world.index][3];
						if(skillId != 0 && skillLvl != 0)
						{
							L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
							if(skill != null)
							{
								skill.getEffects(world.boss, world.boss);
							}
						}
					}

					return super.onKill(npc, player, isPet);
				}
			}

			// second room was spawned ?
			if(world.secondRoom != null)
			{
				boolean all = true;
				// check for all mobs in the second room
				for(int i = 0; i < world.secondRoom.size(); i++)
				{
					// found killed now mob
					if(world.secondRoom.get(i) == objectId)
					{
						world.secondRoom.set(i, 0);
					}
					// found alive mob
					else if(world.secondRoom.get(i) != 0)
					{
						all = false;
					}
				}
				// all mobs killed ?
				if(all)
				{
					world.secondRoom.clear();
					world.secondRoom = null;

					if(world.boss != null)
					{
						int skillId = SECOND_ROOM[world.index][1];
						int skillLvl = SECOND_ROOM[world.index][2];
						if(skillId != 0 && skillLvl != 0)
						{
							L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
							if(skill != null)
							{
								skill.getEffects(world.boss, world.boss);
							}
						}
					}

					return super.onKill(npc, player, isPet);
				}
			}

			// miniboss spawned ?
			if(world.miniBoss != 0 && world.miniBoss == objectId)
			{
				world.miniBoss = 0;

				if(world.boss != null)
				{
					int skillId = MINIBOSS[world.index][4];
					int skillLvl = MINIBOSS[world.index][5];
					if(skillId != 0 && skillLvl != 0)
					{
						L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
						if(skill != null)
						{
							skill.getEffects(world.boss, world.boss);
						}
					}
				}

				return super.onKill(npc, player, isPet);
			}

			// boss was killed, finish instance
			if(world.boss != null && world.boss.equals(npc))
			{
				world.boss = null;
				finishInstance(world);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc.getNpcId() == TELEPORTER)
		{
			return player.isInParty() && player.getParty().isLeader(player) ? "labyrinth_gatekeeper001.htm" : "labyrinth_gatekeeper001a.htm";
		}
		return null;
	}

	private class KamaWorld extends InstanceWorld
	{
		public int index;                // 0-18 index of the kama type in arrays
		public int shaman;            // objectId of the shaman
		public List<L2Spawn> firstRoom;    // list of the spawns in the first room (excluding shaman)
		public ArrayList<Integer> secondRoom;// list of objectIds mobs in the second room
		public int miniBoss;        // objectId of the miniboss
		public L2Npc boss;        // boss
	}
}