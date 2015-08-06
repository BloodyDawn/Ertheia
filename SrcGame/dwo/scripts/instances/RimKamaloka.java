package dwo.scripts.instances;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.database.DatabaseUtils;
import gnu.trove.map.hash.TIntLongHashMap;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class RimKamaloka extends Quest
{
	/*
	 * Reset time for all kamaloka
	 * Default: 6:30AM on server time
	 */
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	private static final int LOCK_TIME = 10;
	/*
	 * Duration of the instance, minutes
	 */
	private static final int DURATION = 20;
	/*
	 * Time after which instance without players will be destroyed
	 * Default: 5 minutes
	 */
	private static final int EMPTY_DESTROY_TIME = 5;
	/*
	 * Time to destroy instance (and eject player away)
	 * Default: 10 minutes
	 */
	private static final int EXIT_TIME = 10;
	/*
	 * Maximum level difference between players level and kamaloka level
	 * Default: 5
	 */
	private static final int MAX_LEVEL_DIFFERENCE = 5;
	/*
	 * Respawn delay for the first generation of mobs
	 * Default: 30 seconds
	 */
	private static final int RESPAWN_DELAY = 30;
	/*
	 * Inactivity despawn delay for the new generations
	 * Default: 10 seconds
	 */
	private static final int DESPAWN_DELAY = 10000;
	/*
	 * Hardcoded instance ids for kamaloka
	 */
	private static final int[] INSTANCE_IDS = {
		InstanceZoneId.RIM_KAMALOKA_25.getId(), InstanceZoneId.RIM_KAMALOKA_30.getId(),
		InstanceZoneId.RIM_KAMALOKA_35.getId(), InstanceZoneId.RIM_KAMALOKA_40.getId(),
		InstanceZoneId.RIM_KAMALOKA_45.getId(), InstanceZoneId.RIM_KAMALOKA_50.getId(),
		InstanceZoneId.RIM_KAMALOKA_55.getId(), InstanceZoneId.RIM_KAMALOKA_60.getId(),
		InstanceZoneId.RIM_KAMALOKA_65.getId(), InstanceZoneId.RIM_KAMALOKA_70.getId(),
		InstanceZoneId.RIM_KAMALOKA_75.getId(), InstanceZoneId.RIM_KAMALOKA_80.getId(),
	};
	/*
	 * Level of the kamaloka
	 */
	private static final int[] LEVEL = {25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80};
	private static final int[][] TELEPORTS = {
		{10025, -219868, -8021}, {15617, -219883, -8021}, {22742, -220079, -7802}, {8559, -212987, -7802},
		{15867, -212994, -7802}, {23038, -213052, -8007}, {9139, -205132, -8007}, {15943, -205740, -8008},
		{22343, -206237, -7991}, {41496, -219694, -8759}, {48137, -219716, -8759}, {48137, -219716, -8759}
	};
	/*
	 * Npc IDs: 1st, 2nd, 3rd generations
	 */
	private static final int[][] KANABIONS = {
		{22452, 22453, 22454}, {22455, 22456, 22457}, {22458, 22459, 22460}, {22461, 22462, 22463},
		{22464, 22465, 22466}, {22467, 22468, 22469}, {22470, 22471, 22472}, {22473, 22474, 22475},
		{22476, 22477, 22478}, {22479, 22480, 22481}, {22482, 22483, 22484}, {22482, 22483, 22484}
	};

	/*
	 * Teleport points into instances
	 * 
	 * x, y, z
	 */
	private static final int[][][] SPAWNLIST = {
		{
			{8971, -219546, -8021}, {9318, -219644, -8021}, {9266, -220208, -8021}, {9497, -220054, -8024}
		}, {
		{16107, -219574, -8021}, {16769, -219885, -8021}, {16363, -220219, -8021}, {16610, -219523, -8021}
	}, {
		{23019, -219730, -7803}, {23351, -220455, -7803}, {23900, -219864, -7803}, {23851, -220294, -7803}
	}, {
		{9514, -212478, -7803}, {9236, -213348, -7803}, {8868, -212683, -7803}, {9719, -213042, -7803}
	}, {
		{16925, -212811, -7803}, {16885, -213199, -7802}, {16487, -213339, -7803}, {16337, -212529, -7803}
	}, {
		{23958, -213282, -8009}, {23292, -212782, -8012}, {23844, -212781, -8009}, {23533, -213301, -8009}
	}, {
		{8828, -205518, -8009}, {8895, -205989, -8009}, {9398, -205967, -8009}, {9393, -205409, -8009}
	}, {
		{16185, -205472, -8009}, {16808, -205929, -8009}, {16324, -206042, -8009}, {16782, -205454, -8009}
	}, {
		{23476, -206310, -7991}, {23230, -205861, -7991}, {22644, -205888, -7994}, {23078, -206714, -7991}
	}, {
		{42981, -219308, -8759}, {42320, -220160, -8759}, {42434, -219181, -8759}, {42101, -219550, -8759},
		{41859, -220236, -8759}, {42881, -219942, -8759}
	}, {
		{48770, -219304, -8759}, {49036, -220190, -8759}, {49363, -219814, -8759}, {49393, -219102, -8759},
		{49618, -220490, -8759}, {48526, -220493, -8759}
	}, {
		{48770, -219304, -8759}, {49036, -220190, -8759}, {49363, -219814, -8759}, {49393, -219102, -8759},
		{49618, -220490, -8759}, {48526, -220493, -8759}
	}
	};
	private static final int[][] REWARDERS = {
		{9261, -219862, -8021}, {16301, -219806, -8021}, {23478, -220079, -7799}, {9290, -212993, -7799},
		{16598, -212997, -7802}, {23650, -213051, -8007}, {9136, -205733, -8007}, {16508, -205737, -8007},
		{23229, -206316, -7991}, {42638, -219781, -8759}, {49014, -219737, -8759}, {49014, -219737, -8759}
	};
	private static final int[][][] REWARDS = {
		{ //20-30
			null,            // Grade F
			{13002, 4, 12824, 1},    // Grade D
			{13002, 4, 10836, 1},    // Grade C
			{13002, 4, 10837, 1},    // Grade B
			{13002, 4, 10838, 1},    // Grade A
			{13002, 4, 10844, 1}    // Grade S
		}, { // 25-35
		null, {13002, 4, 12828, 1}, {13002, 4, 10837, 1}, {13002, 4, 10838, 1}, {13002, 4, 10841, 1},
		{13002, 4, 12827, 1}
	}, { // 30-40
		null, {13002, 4, 10840, 1}, {13002, 4, 10841, 1}, {13002, 4, 10842, 1}, {13002, 4, 10843, 1},
		{13002, 4, 10844, 1}
	}, { // 35-45
		null, {13002, 5, 12826, 1}, {13002, 5, 10842, 1}, {13002, 5, 10843, 1}, {13002, 5, 10846, 1},
		{13002, 5, 12829, 1}
	}, { // 40-50
		null, {13002, 5, 10845, 1}, {13002, 5, 10846, 1}, {13002, 5, 10847, 1}, {13002, 5, 10848, 1},
		{13002, 5, 10849, 1}
	}, { // 45-55
		null, {13002, 5, 12828, 1}, {13002, 5, 10847, 1}, {13002, 5, 10848, 1}, {13002, 5, 10851, 1},
		{13002, 5, 12831, 1}
	}, { // 50-60
		null, {13002, 6, 10850, 1}, {13002, 6, 10851, 1}, {13002, 6, 10852, 1}, {13002, 6, 10853, 1},
		{13002, 6, 10854, 1}
	}, { //55-65 7
		null, {13002, 6, 12830, 1}, {13002, 6, 10852, 1}, {13002, 6, 10853, 1}, {13002, 6, 10856, 1},
		{13002, 6, 12833, 1}
	}, { // 60-70
		null, {13002, 7, 10855, 1}, {13002, 7, 10856, 1}, {13002, 7, 10857, 1}, {13002, 7, 10858, 1},
		{13002, 7, 10859, 1}
	}, { // 65-75
		null, {13002, 8, 12832, 1}, {13002, 8, 10857, 1}, {13002, 8, 10858, 1}, {13002, 8, 10861, 1},
		{13002, 8, 12834, 1}
	}, { // 70-80
		null, {13002, 8, 10860, 1}, {13002, 8, 10861, 1}, {13002, 8, 10862, 1}, {13002, 8, 10863, 1},
		{13002, 8, 10864, 1}
	}, { // 80+
		null, {13002, 8, 10860, 1}, {13002, 8, 10861, 1}, {13002, 8, 10862, 1}, {13002, 8, 10863, 1},
		{13002, 8, 10864, 1}
	}
	};
	private static final int START_NPC = 32484;
	private static final int REWARDER = 32485;
	private static String qn = "RimKamaloka";
	private NpcHtmlMessage LEADERBOARDS;

	public RimKamaloka()
	{

		addStartNpc(START_NPC);
		addFirstTalkId(START_NPC);
		addFirstTalkId(REWARDER);
		addTalkId(START_NPC);
		addTalkId(REWARDER);
		for(int[] list : KANABIONS)
		{
			addFactionCallId(list[0]);
			addAttackId(list);
			addKillId(list);
		}
		if(LEADERBOARDS == null)
		{
			loadRankAndUpdate();
		}
	}

	/**
	 * Check if party with player as leader allowed to enter
	 *
	 * @param player party leader
	 * @param index (0-17) index of the kamaloka in arrays
	 *
	 * @return true if party allowed to enter
	 */
	private static boolean checkConditions(L2PcInstance player, int index)
	{
		L2Party party = player.getParty();
		// player must not be in party
		if(party != null)
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}

		// get level of the instance
		int level = LEVEL[index];
		// and client name
		String instanceName = InstanceManager.getInstance().getInstanceIdName(INSTANCE_IDS[index]);

		// player level must be in range
		if(Math.abs(player.getLevel() - level) > MAX_LEVEL_DIFFERENCE)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}
		// get instances reenter times for player
		Map<Integer, Long> instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(player.getObjectId());
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
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
					sm.addPcName(player);
					player.sendPacket(sm);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Spawn all NPCs in kamaloka
	 * @param world instanceWorld
	 */
	private static void spawnKama(RimKamaWorld world)
	{
		int[][] spawnlist;
		int index = world.index;
		world.KANABION = KANABIONS[index][0];
		world.DOPPLER = KANABIONS[index][1];
		world.VOIDER = KANABIONS[index][2];

		try
		{
			L2NpcTemplate mob1 = NpcTable.getInstance().getTemplate(world.KANABION);

			spawnlist = SPAWNLIST[index];
			int length = spawnlist.length;

			L2Spawn spawn;
			for(int[] loc : spawnlist)
			{
				spawn = new L2Spawn(mob1);
				spawn.setInstanceId(world.instanceId);
				spawn.setLocx(loc[0]);
				spawn.setLocy(loc[1]);
				spawn.setLocz(loc[2]);
				spawn.setHeading(-1);
				spawn.setRespawnDelay(RESPAWN_DELAY);
				spawn.setAmount(1);
				spawn.startRespawn();
				spawn.doSpawn();
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "RimKamaloka: error during spawn: " + e);
		}
	}

	private static void spawnNextMob(RimKamaWorld world, L2Npc oldNpc, int npcId, L2PcInstance player)
	{
		if(world.isFinished)
		{
			return;
		}

		L2MonsterInstance monster = null;
		if(world.spawnedMobs != null && !world.spawnedMobs.isEmpty())
		{
			for(L2MonsterInstance mob : world.spawnedMobs)
			{
				if(mob == null || !mob.isDecayed() || mob.getNpcId() != npcId)
				{
					continue;
				}
				mob.setDecayed(false);
				mob.setIsDead(false);
				mob.overhitEnabled(false);
				mob.refreshID();
				monster = mob;
				break;
			}
		}

		boolean needSpawn = false;
		if(monster == null)
		{
			needSpawn = true;
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			monster = new L2MonsterInstance(IdFactory.getInstance().getNextId(), template);
			world.spawnedMobs.add(monster);
		}

		synchronized(world.lastAttack)
		{
			world.lastAttack.put(monster.getObjectId(), System.currentTimeMillis());
		}

		monster.setCurrentHpMp(monster.getMaxHp(), monster.getMaxMp());
		monster.setHeading(oldNpc.getHeading());
		monster.getInstanceController().setInstanceId(oldNpc.getInstanceId());

		if(needSpawn)
		{
			monster.getLocationController().spawn(oldNpc.getX(), oldNpc.getY(), oldNpc.getZ() + 20);
		}

		monster.setRunning();
		monster.addDamageHate(player, 0, 9999);
		monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
	}

	public static void main(String[] args)
	{
		new RimKamaloka();
	}

	/**
	 * Handling enter of the players into kamaloka
	 *
	 * @param player party leader
	 * @param index (0-17) kamaloka index in arrays
	 */
	protected void enterInstance(L2PcInstance player, int index)
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
				return;
			}

			// check for existing instances for this player
			InstanceWorld tmpWorld = InstanceManager.getInstance().getPlayerWorld(player);
			// player already in the instance
			if(tmpWorld != null)
			{
				// but not in kamaloka
				if(!(tmpWorld instanceof RimKamaWorld) || tmpWorld.templateId != templateId)
				{
					player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
					return;
				}
				RimKamaWorld world = (RimKamaWorld) tmpWorld;
				// check for level difference again on reenter
				if(Math.abs(player.getLevel() - LEVEL[world.index]) > MAX_LEVEL_DIFFERENCE)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(player);
					player.sendPacket(sm);
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

				// Creating new instanceWorld, using our instanceId and templateId
				RimKamaWorld world = new RimKamaWorld();
				world.instanceId = instanceId;
				world.templateId = templateId;
				// set index for easy access to the arrays
				world.index = index;
				InstanceManager.getInstance().addWorld(world);

				// spawn npcs
				spawnKama(world);
				world.finishTask = ThreadPoolManager.getInstance().scheduleGeneral(new FinishTask(world), DURATION * 60000);
				world.lockTask = ThreadPoolManager.getInstance().scheduleGeneral(new LockTask(world), LOCK_TIME * 60000);
				world.despawnTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new DespawnTask(world), 1000, 1000);

				world.allowed.add(player.getObjectId());

				player.teleToInstance(new Location(TELEPORTS[index]), instanceId);
			}
		}
	}

	private void rewardPlayer(RimKamaWorld world, L2Npc npc)
	{
		synchronized(this)
		{
			if(!world.isFinished || world.isRewarded)
			{
				return;
			}
			world.isRewarded = true;

			int[][] allRewards = REWARDS[world.index];
			int[] reward = allRewards[Math.min(world.grade, allRewards.length)];
			if(reward == null)
			{
				return;
			}
			for(int objectId : world.allowed)
			{
				L2PcInstance player = WorldManager.getInstance().getPlayer(objectId);
				if(player != null && player.isOnline())
				{
					player.sendMessage("Grade:" + world.grade);
					for(int i = 0; i < reward.length; i += 2)
					{
						player.addItem(ProcessType.QUEST, reward[i], reward[i + 1], npc, true);
					}
					ThreadConnection con = null;
					FiltredPreparedStatement insertion = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection();
						insertion = con.prepareStatement("INSERT INTO kamaloka_results (char_name,Level,Grade,Count) VALUES (?,?,?,?)");
						insertion.setString(1, player.getName());
						insertion.setInt(2, world.index);
						insertion.setInt(3, world.grade);
						insertion.setInt(4, Math.min(world.grade, allRewards.length));
						insertion.executeUpdate();
					}
					catch(Exception e)
					{
						_log.log(Level.ERROR, "Error while store RimKamaloka Rank", e);
					}
					finally
					{
						DatabaseUtils.closeDatabaseCS(con, insertion);
					}
					updateCharacterRank(player, world);
					loadRankAndUpdate();
				}
			}
		}
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc == null || attacker == null)
		{
			return null;
		}

		InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpWorld instanceof RimKamaWorld)
		{
			RimKamaWorld world = (RimKamaWorld) tmpWorld;
			synchronized(world.lastAttack)
			{
				world.lastAttack.put(npc.getObjectId(), System.currentTimeMillis());
			}

			int maxHp = npc.getMaxHp();
			if(npc.getCurrentHp() == maxHp)
			{
				if(damage * 100 / maxHp > 40)
				{
					int npcId = npc.getNpcId();
					int chance = Rnd.get(100);
					int nextId = 0;

					if(npcId == world.KANABION)
					{
						if(chance < 5)
						{
							nextId = world.DOPPLER;
						}
					}
					else if(npcId == world.DOPPLER)
					{
						if(chance < 5)
						{
							nextId = world.DOPPLER;
						}
						else if(chance < 10)
						{
							nextId = world.VOIDER;
						}
					}
					else if(npcId == world.VOIDER)
					{
						if(chance < 5)
						{
							nextId = world.VOIDER;
						}
					}

					if(nextId > 0)
					{
						spawnNextMob(world, npc, nextId, attacker);
					}
				}
			}
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(npc == null || player == null)
		{
			return null;
		}
		if(event.endsWith(".htm"))
		{
			return event;
		}
		if(event.equals("leaderboards"))
		{
			player.sendPacket(LEADERBOARDS);
		}
		else if(event.equalsIgnoreCase("Exit"))
		{
			try
			{
				InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				if(world instanceof RimKamaWorld && world.allowed.contains(player.getObjectId()))
				{
					Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
					player.teleToInstance(inst.getReturnLoc(), 0);
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "RimKamaloka: problem with exit: " + e);
			}
			return null;
		}
		else if(event.equalsIgnoreCase("Reward"))
		{
			try
			{
				InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				if(world instanceof RimKamaWorld && world.allowed.contains(player.getObjectId()))
				{
					rewardPlayer((RimKamaWorld) world, npc);
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "RimKamaloka: problem with reward: " + e);
			}
			return "Rewarded.htm";
		}

		try
		{
			enterInstance(player, Integer.parseInt(event));
		}
		catch(Exception e)
		{
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(npc == null || player == null)
		{
			return null;
		}

		InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpWorld instanceof RimKamaWorld)
		{
			RimKamaWorld world = (RimKamaWorld) tmpWorld;
			synchronized(world.lastAttack)
			{
				world.lastAttack.remove(npc.getObjectId());
			}

			int npcId = npc.getNpcId();
			int chance = Rnd.get(100);
			int nextId = 0;

			if(npcId == world.KANABION)
			{
				world.kanabionsCount++;
				if(((L2Attackable) npc).isOverhit())
				{
					if(chance < 35)
					{
						nextId = world.DOPPLER;
					}
					else if(chance < 50)
					{
						nextId = world.VOIDER;
					}
				}
				else if(chance < 20)
				{
					nextId = world.DOPPLER;
				}
			}
			else if(npcId == world.DOPPLER)
			{
				world.dopplersCount++;
				if(((L2Attackable) npc).isOverhit())
				{
					if(chance < 35)
					{
						nextId = world.DOPPLER;
					}
					else if(chance < 65)
					{
						nextId = world.VOIDER;
					}
				}
				else
				{
					if(chance < 15)
					{
						nextId = world.DOPPLER;
					}
					else if(chance < 30)
					{
						nextId = world.VOIDER;
					}
				}
			}
			else if(npcId == world.VOIDER)
			{
				world.voidersCount++;
				if(((L2Attackable) npc).isOverhit())
				{
					if(chance < 60)
					{
						nextId = world.VOIDER;
					}
				}
				else if(chance < 25)
				{
					nextId = world.VOIDER;
				}
			}

			if(nextId > 0)
			{
				spawnNextMob(world, npc, nextId, player);
			}
		}

		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc == null)
		{
			return null;
		}

		int npcId = npc.getNpcId();
		if(npcId == START_NPC)
		{
			return npc.getCastle().getName() + ".htm";
		}
		if(npcId == REWARDER)
		{
			InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if(tmpWorld instanceof RimKamaWorld)
			{
				RimKamaWorld world = (RimKamaWorld) tmpWorld;
				if(!world.isFinished)
				{
					return "";
				}

				switch(world.grade)
				{
					case 0:
						return "GradeF.htm";
					case 1:
						return "GradeD.htm";
					case 2:
						return "GradeC.htm";
					case 3:
						return "GradeB.htm";
					case 4:
						return "GradeA.htm";
					default:
						return "GradeS.htm";
				}
			}
		}

		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc == null)
		{
			return null;
		}

		int npcId = npc.getNpcId();
		if(npcId == REWARDER)
		{
			InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if(tmpWorld instanceof RimKamaWorld)
			{
				if(((RimKamaWorld) tmpWorld).isRewarded)
				{
					return "Rewarded.htm";
				}
			}
		}
		return npcId + ".htm";
	}

	@Override
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		if(npc == null || caller == null)
		{
			return null;
		}

		if(npc.getNpcId() == caller.getNpcId())
		{
			return null;
		}

		return super.onFactionCall(npc, caller, attacker, isPet);
	}

	public void updateCharacterRank(L2PcInstance player, RimKamaWorld world)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO kamaloka_ranks (charId, kanabionsCount, dopplersCount, voidersCount, total) VALUES (?, ?, ?, ?, ?)");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, world.kanabionsCount);
			statement.setInt(3, world.dopplersCount);
			statement.setInt(4, world.voidersCount);
			statement.setInt(5, world.kanabionsCount + world.dopplersCount + world.voidersCount);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void loadRankAndUpdate()
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(HtmCache.getInstance().getHtm("en", "scripts/instances/RimKamaloka/32484-leaderboards.htm"));

		StringBuilder sb = new StringBuilder();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM kamaloka_ranks GROUP BY charId ORDER BY total DESC");
			rset = statement.executeQuery();
			boolean first = false;
			while(rset.next())
			{
				if(first)
				{
					sb.append("<table width=270 border=0>" + "<tr><td fixwidth=170 align=center>").append(CharNameTable.getInstance().getNameById(rset.getInt("charId"))).append("</td><td fixwidth=100 align=center>S +++++ (").append(rset.getInt("total")).append(")</td></tr>").append("</table><br>");
				}
				else
				{
					msg.replace("%top1%", "<table width=270 border=0>" +
						"<tr><td fixwidth=170 align=center>" + CharNameTable.getInstance().getNameById(rset.getInt("charId")) + "</td><td fixwidth=100 align=center>S +++++ (" + rset.getInt("total") + ")</td></tr>" +
						"</table><br>");
					first = true;
				}
			}
			msg.replace("%top9%", sb.toString());
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		LEADERBOARDS = msg;
	}

	private class RimKamaWorld extends InstanceWorld
	{
		public int index;
		public int KANABION;
		public int DOPPLER;
		public int VOIDER;

		public int kanabionsCount;
		public int dopplersCount;
		public int voidersCount;
		public int grade;
		public boolean isFinished;
		public boolean isRewarded;

		public ScheduledFuture<?> lockTask;
		public ScheduledFuture<?> finishTask;

		public List<L2MonsterInstance> spawnedMobs = new FastList<>();
		public TIntLongHashMap lastAttack = new TIntLongHashMap();
		public ScheduledFuture<?> despawnTask;

		public RimKamaWorld()
		{
			//InstanceManager.getInstance().super();
		}
	}

	class LockTask implements Runnable
	{
		private RimKamaWorld _world;

		LockTask(RimKamaWorld world)
		{
			_world = world;
		}

		@Override
		public void run()
		{
			if(_world != null)
			{
				Calendar reenter = Calendar.getInstance();
				reenter.set(Calendar.MINUTE, RESET_MIN);
				// if time is >= RESET_HOUR - roll to the next day
				if(reenter.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
				{
					reenter.roll(Calendar.DATE, true);
				}
				reenter.set(Calendar.HOUR_OF_DAY, RESET_HOUR);

				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
				sm.addString(InstanceManager.getInstance().getInstanceIdName(_world.templateId));

				// set instance reenter time for all allowed players
				boolean found = false;
				for(int objectId : _world.allowed)
				{
					L2PcInstance player = WorldManager.getInstance().getPlayer(objectId);
					if(player != null && player.isOnline())
					{
						found = true;
						InstanceManager.getInstance().setInstanceTime(objectId, _world.templateId, reenter.getTimeInMillis());
						player.sendPacket(sm);
					}
				}
				if(!found)
				{
					_world.isFinished = true;
					_world.spawnedMobs.clear();
					_world.lastAttack.clear();
					if(_world.finishTask != null)
					{
						_world.finishTask.cancel(false);
						_world.finishTask = null;
					}
					if(_world.despawnTask != null)
					{
						_world.despawnTask.cancel(false);
						_world.despawnTask = null;
					}

					InstanceManager.getInstance().destroyInstance(_world.instanceId);
				}
			}
		}
	}

	class FinishTask implements Runnable
	{
		private RimKamaWorld _world;

		FinishTask(RimKamaWorld world)
		{
			_world = world;
		}

		@Override
		public void run()
		{
			if(_world != null)
			{
				_world.isFinished = true;
				if(_world.despawnTask != null)
				{
					_world.despawnTask.cancel(false);
					_world.despawnTask = null;
				}
				_world.spawnedMobs.clear();
				_world.lastAttack.clear();
				// destroy instance after EXIT_TIME
				Instance inst = InstanceManager.getInstance().getInstance(_world.instanceId);
				if(inst != null)
				{
					inst.removeNpcs();
					inst.setDuration(EXIT_TIME * 60000);
					if(inst.getPlayers().isEmpty())
					{
						inst.setDuration(EMPTY_DESTROY_TIME * 60000);
					}
					else
					{
						inst.setDuration(EXIT_TIME * 60000);
						inst.setEmptyDestroyTime(EMPTY_DESTROY_TIME * 60000);
					}
				}

				// calculate reward
				_world.grade = _world.kanabionsCount < 10 ? 0 : Math.min((_world.dopplersCount + 2 * _world.voidersCount) / _world.kanabionsCount + 1, 5);

				int index = _world.index;
				// spawn rewarder npc
				addSpawn(REWARDER, REWARDERS[index][0], REWARDERS[index][1], REWARDERS[index][2], 0, false, 0, false, _world.instanceId);
			}
		}
	}

	class DespawnTask implements Runnable
	{
		private RimKamaWorld _world;

		DespawnTask(RimKamaWorld world)
		{
			_world = world;
		}

		@Override
		public void run()
		{
			if(_world != null && !_world.isFinished && !_world.lastAttack.isEmpty() && !_world.spawnedMobs.isEmpty())
			{
				long time = System.currentTimeMillis();
				for(L2MonsterInstance mob : _world.spawnedMobs)
				{
					if(mob == null || mob.isDead() || !mob.isVisible())
					{
						continue;
					}
					if(_world.lastAttack.containsKey(mob.getObjectId()) && time - _world.lastAttack.get(mob.getObjectId()) > DESPAWN_DELAY)
					{
						mob.getLocationController().delete();
						synchronized(_world.lastAttack)
						{
							_world.lastAttack.remove(mob.getObjectId());
						}
					}
				}
			}
		}
	}
}