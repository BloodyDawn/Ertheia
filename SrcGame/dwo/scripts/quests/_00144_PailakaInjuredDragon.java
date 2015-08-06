package dwo.scripts.quests;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.SpecialCamera;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public class _00144_PailakaInjuredDragon extends Quest
{
	private static final String qn = "144_PailakaInjuredDragon";
	private static final int MIN_LEVEL = 73;
	private static final int MAX_LEVEL = 77;
	private static final int EXIT_TIME = 5;
	// Телепорты
	private static final Location ENTRY_POINT = new Location(125757, -40928, -3736);
	private static final Location EXIT_POINT = new Location(143323, -58992, -3464);
	private static final TIntObjectHashMap<int[]> NOEXIT_ZONES = new TIntObjectHashMap<>();
	private static final TIntObjectHashMap<L2Skill> Buffs = new TIntObjectHashMap<>();
	private static final TIntIntHashMap behindWall = new TIntIntHashMap();
	// NPCS
	private static final int KETRA_ORC_SHAMAN = 32499;
	private static final int KETRA_ORC_SUPPORTER = 32502;
	private static final int KETRA_ORC_SUPPORTER2 = 32512;
	private static final int KETRA_ORC_INTELIGENCE_OFFICER = 32509;
	// Arrays
	private static final int[] NPCS = {
		KETRA_ORC_SHAMAN, KETRA_ORC_SUPPORTER, KETRA_ORC_INTELIGENCE_OFFICER, KETRA_ORC_SUPPORTER2
	};
	// WALL MOBS
	private static final int VARKA_SILENOS_RECRUIT = 18635;
	private static final int VARKA_SILENOS_FOOTMAN = 18636;
	private static final int VARKA_SILENOS_WARRIOR = 18642;
	private static final int VARKA_SILENOS_OFFICER = 18646;
	private static final int VARKAS_COMMANDER = 18654;
	private static final int VARKA_ELITE_GUARD = 18653;
	private static final int VARKA_SILENOS_GREAT_MAGUS = 18649;
	private static final int VARKA_SILENOS_GENERAL = 18650;
	private static final int VARKA_SILENOS_HEAD_GUARD = 18655;
	private static final int PROPHET_GUARD = 18657;
	private static final int VARKAS_PROPHET = 18659;
	// EXTRA WALL SILENOS
	private static final int VARKA_SILENOS_MEDIUM = 18644;
	private static final int VARKA_SILENOS_PRIEST = 18641;
	private static final int VARKA_SILENOS_SHAMAN = 18640;
	private static final int VARKA_SILENOS_SEER = 18648;
	private static final int VARKA_SILENOS_MAGNUS = 18645;
	private static final int DISCIPLE_OF_PROPHET = 18658;
	private static final int VARKA_HEAD_MAGUS = 18656;
	private static final int VARKA_SILENOS_GREAT_SEER = 18652;
	private static final int[] WALL_MONSTERS = {
		// 1st Row Mobs
		VARKA_SILENOS_FOOTMAN, VARKA_SILENOS_WARRIOR, VARKA_SILENOS_OFFICER, VARKAS_COMMANDER, VARKA_SILENOS_RECRUIT,
		PROPHET_GUARD, VARKA_ELITE_GUARD, VARKA_SILENOS_GREAT_MAGUS, VARKA_SILENOS_GENERAL, VARKA_SILENOS_HEAD_GUARD,
		PROPHET_GUARD, VARKAS_PROPHET,

		// 2nd Row Mobs
		DISCIPLE_OF_PROPHET, VARKA_HEAD_MAGUS, VARKA_SILENOS_GREAT_SEER, VARKA_SILENOS_SHAMAN, VARKA_SILENOS_MAGNUS,
		VARKA_SILENOS_SEER, VARKA_SILENOS_MEDIUM, VARKA_SILENOS_PRIEST
	};
	// NORMAL MOBS
	private static final int ANTYLOPE_1 = 18637;
	private static final int ANTYLOPE_2 = 18643;
	private static final int ANTYLOPE_3 = 18651;
	private static final int FLAVA = 18647;
	private static final int[] OTHER_MONSTERS = {
		ANTYLOPE_1, ANTYLOPE_2, ANTYLOPE_3, FLAVA
	};
	// BOSS
	private static final int LATANA = 18660;
	// ITEMS
	private static final int SPEAR = 13052;
	private static final int ENCHSPEAR = 13053;
	private static final int LASTSPEAR = 13054;
	private static final int STAGE1 = 13056;
	private static final int STAGE2 = 13057;
	private static final int SHIELD_POTION = 13032;
	private static final int HEAL_POTION = 13033;
	private static final int[] ITEMS = {
		SPEAR, ENCHSPEAR, LASTSPEAR, STAGE1, STAGE2, SHIELD_POTION, HEAL_POTION
	};
	// Rewards
	private static final int PSHIRT = 13296;
	private static final int SCROLL_OF_ESCAPE = 736;
	private static final int[][] spawns1 = {
		// 200001 zone
		{18635, 122077, -46098, -2914, 5028}, {18635, 122019, -45634, -2908, 64841},
		{18636, 122023, -45769, -2910, 63593}, {18636, 122050, -45999, -2910, 1701}, {18642, 122038, -45904, -2914, 771}
	};
	private static final int[][] spawns2 = {
		// 200002 zone
		{18653, 115997, -46271, -2637, 63732}, {18654, 116056, -46341, -2630, 63791},
		{18646, 116054, -46439, -2637, 63477}, {18646, 116057, -46521, -2644, 65025},
		{18654, 116084, -46587, -2651, 1141}, {18653, 116063, -46680, -2658, 4323}
	};
	private static final int[][] spawns3 = {
		// 200003 zone
		{18635, 117184, -51722, -2575, 55468}, {18636, 117097, -51774, -2577, 51453},
		{18642, 117013, -51840, -2578, 55135}, {18636, 116926, -51876, -2580, 56322},
		{18635, 116876, -51937, -2572, 59301}
	};
	private static final int[][] spawns4 = {
		// 200004 zone
		{18653, 116213, -50301, -2622, 50611}, {18654, 116155, -50420, -2635, 53262},
		{18646, 116075, -50463, -2642, 53954}, {18646, 116020, -50497, -2643, 55751},
		{18654, 115952, -50528, -2646, 52507}, {18653, 115780, -50465, -2620, 52135}
	};
	private static final int[][] spawns5 = {
		// 200005 zone
		{18649, 111606, -43746, -2633, 62267}, {18650, 111602, -43846, -2638, 63427},
		{18649, 111616, -43922, -2635, 60699}, {18650, 111618, -44003, -2637, 59709},
		{18649, 111614, -44071, -2642, 63794}, {18650, 111619, -44129, -2650, 2365}
	};
	private static final int[][] spawns6 = {
		// 200006 zone
		{18657, 109272, -45665, -2210, 4731}, {18655, 109326, -45781, -2235, 5945},
		{18657, 109393, -45893, -2253, 5549}, {18655, 109458, -45980, -2263, 6915},
		{18657, 109516, -46032, -2268, 4362}, {18655, 109584, -46100, -2263, 9356}
	};
	private static final int[][] spawns7 = {
		// 200007 zone
		{18653, 116432, -55729, -2441, 65250}, {18654, 116499, -55822, -2449, 63477},
		{18646, 116503, -55918, -2449, 589}, {18646, 116490, -56022, -2445, 61772},
		{18654, 116481, -56108, -2437, 62531}, {18653, 116433, -56194, -2432, 927}
	};
	private static final int[][] spawns8 = {
		// 200008 zone
		{18655, 108892, -41031, -2204, 64381}, {18657, 108888, -41114, -2195, 64256},
		{18655, 108886, -41185, -2196, 64884}, {18657, 108878, -41260, -2188, 64876},
		{18655, 108862, -41323, -2174, 62422}, {18657, 108869, -41376, -2163, 2280},
		{18655, 108877, -41436, -2144, 64321}
	};
	private static final int[][] spawns9 = {
		// 200009 zone
		{18650, 110205, -39800, -1840, 46882}, {18649, 110110, -39799, -1850, 46038},
		{18659, 110023, -39802, -1852, 48655}, {18649, 109955, -39811, -1855, 48647},
		{18650, 109874, -39833, -1837, 48408}
	};
	private static final int[][] otherspawns = {
		// others
		{18637, 123164, -52003, -2461, 13414}, {18637, 123726, -51665, -2467, 6603},
		{18637, 111641, -56027, -2783, 43022}, {18637, 123965, -51293, -2471, 5746},
		{18637, 125010, -50813, -2460, 1407}, {18637, 122474, -42594, -3240, 49260},
		{18637, 121814, -41622, -3181, 5457}, {18637, 121424, -41452, -3163, 41426},
		{18637, 121264, -41559, -3164, 47583}, {18637, 121516, -43081, -3216, 61535},
		{18647, 108759, -53478, -2606, 25521}, {18647, 108415, -53924, -2564, 18727},
		{18647, 107850, -52954, -2417, 21437}, {18651, 111214, -55683, -2798, 27410},
		{18651, 111515, -55735, -2811, 24213}, {18651, 110462, -54609, -2882, 24283},
		{18651, 111556, -54895, -2861, 20821},
		// npcs
		{32502, 125524, -40946, -3718, 8279}, {32509, 110074, -41324, -2285, 33580},
		{32509, 113745, -47565, -2593, 55710}
	};
	private static final List<PailakaDrop> DROPLIST = new ArrayList<>();
	private static final int[][] HP_HERBS_DROPLIST = {
		// itemId, count, chance
		{8601, 1, 40}, {8600, 1, 70}
	};
	private static final int[][] MP_HERBS_DROPLIST = {
		// itemId, count, chance
		{8604, 1, 40}, {8603, 1, 70}
	};

	public _00144_PailakaInjuredDragon()
	{
		NOEXIT_ZONES.put(200001, new int[]{123167, -45743, -3023});
		NOEXIT_ZONES.put(200002, new int[]{117783, -46398, -2560});
		NOEXIT_ZONES.put(200003, new int[]{116791, -51556, -2584});
		NOEXIT_ZONES.put(200004, new int[]{117993, -52505, -2480});
		NOEXIT_ZONES.put(200005, new int[]{113226, -44080, -2776});
		NOEXIT_ZONES.put(200006, new int[]{110326, -45016, -2444});
		NOEXIT_ZONES.put(200007, new int[]{118341, -55951, -2280});
		NOEXIT_ZONES.put(200008, new int[]{110127, -41562, -2332});
		NOEXIT_ZONES.put(200009, new int[]{110127, -41562, -2332});
		DROPLIST.add(new PailakaDrop(HEAL_POTION, 60));
		DROPLIST.add(new PailakaDrop(SHIELD_POTION, 50));
		Buffs.put(4357, SkillTable.getInstance().getInfo(4357, 2)); // Haste Lv2
		Buffs.put(4342, SkillTable.getInstance().getInfo(4342, 2)); // Wind Walk Lv2
		Buffs.put(4356, SkillTable.getInstance().getInfo(4356, 3)); // Empower Lv3
		Buffs.put(4355, SkillTable.getInstance().getInfo(4355, 3)); // Acumen Lv3
		Buffs.put(4351, SkillTable.getInstance().getInfo(4351, 6)); // Concentration Lv6
		Buffs.put(4345, SkillTable.getInstance().getInfo(4345, 3)); // Might Lv3
		Buffs.put(4358, SkillTable.getInstance().getInfo(4358, 3)); // Guidance Lv3
		Buffs.put(4359, SkillTable.getInstance().getInfo(4359, 3)); // Focus Lv3
		Buffs.put(4360, SkillTable.getInstance().getInfo(4360, 3)); // Death Wisper Lv3
		Buffs.put(4352, SkillTable.getInstance().getInfo(4352, 2)); // Berserker Spirit Lv2
		Buffs.put(4354, SkillTable.getInstance().getInfo(4354, 4)); // Vampiric Rage Lv4
		Buffs.put(4347, SkillTable.getInstance().getInfo(4347, 6)); // Blessed Body Lv6
		behindWall.put(VARKA_SILENOS_FOOTMAN, VARKA_SILENOS_MEDIUM);
		behindWall.put(VARKA_SILENOS_RECRUIT, VARKA_SILENOS_MEDIUM);
		behindWall.put(VARKA_SILENOS_WARRIOR, VARKA_SILENOS_PRIEST);
		behindWall.put(VARKA_ELITE_GUARD, VARKA_SILENOS_SHAMAN);
		behindWall.put(VARKA_SILENOS_OFFICER, VARKA_SILENOS_SEER);
		behindWall.put(VARKAS_COMMANDER, VARKA_SILENOS_SEER);
		behindWall.put(VARKA_SILENOS_GREAT_MAGUS, VARKA_SILENOS_MAGNUS);
		behindWall.put(VARKA_SILENOS_GENERAL, VARKA_SILENOS_MAGNUS);
		behindWall.put(VARKAS_PROPHET, DISCIPLE_OF_PROPHET);
		behindWall.put(VARKA_SILENOS_HEAD_GUARD, VARKA_HEAD_MAGUS);
		behindWall.put(PROPHET_GUARD, VARKA_SILENOS_GREAT_SEER);
		addStartNpc(KETRA_ORC_SHAMAN);
		addFirstTalkId(KETRA_ORC_INTELIGENCE_OFFICER);
		addTalkId(NPCS);

		addKillId(LATANA);
		addKillId(OTHER_MONSTERS);

		// Add aggro acting on main mobs
		addSpawnId(WALL_MONSTERS);
		addKillId(WALL_MONSTERS);
		addAttackId(WALL_MONSTERS);

		addAttackId(LATANA);

		// Add all no exit zones for mob walls
		addEnterZoneId(NOEXIT_ZONES.keys());
		addEnterZoneId(200010);

		questItemIds = ITEMS;
	}

	private static void dropHerb(L2Npc mob, L2PcInstance player, int[][] drop)
	{
		for(int[] aDrop : drop)
		{
			if(Rnd.getChance(aDrop[2]))
			{
				((L2MonsterInstance) mob).dropItem(player, aDrop[0], aDrop[1]);
				return;
			}
		}
	}

	private static void dropItem(L2Npc mob, L2PcInstance player)
	{
		for(PailakaDrop pd : DROPLIST)
		{
			if(Rnd.getChance(pd.getChance()))
			{
				((L2MonsterInstance) mob).dropItem(player, pd.getItemID(), Rnd.get(1, 4));
				return;
			}
		}
	}

	private static void giveBuff(L2Npc npc, L2PcInstance player, int skillId, Pailaka73World world)
	{
		npc.setTarget(player);
		npc.doCast(Buffs.get(skillId));
		world.buff_counter--;
	}

	private static void teleportPlayer(L2Playable player, int[] coords, int instanceId)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.getInstanceController().setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], true);
	}

	public static void main(String[] args)
	{
		new _00144_PailakaInjuredDragon();
	}

	private void enterInstance(L2PcInstance player)
	{
		synchronized(this)
		{
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			if(world != null)
			{
				if(player.isInParty())
				{
					player.leaveParty();
				}

				if(world.templateId != InstanceZoneId.PAILAKA_VARKA_SILENOS_BARRACKS.getId())
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
					return;
				}

				Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
				if(inst != null)
				{
					player.teleToInstance(ENTRY_POINT, world.instanceId);
				}
			}
			else
			{
				if(player.isInParty())
				{
					player.leaveParty();
				}
				world = new Pailaka73World();
				world.instanceId = InstanceManager.getInstance().createDynamicInstance(null);
				world.templateId = InstanceZoneId.PAILAKA_VARKA_SILENOS_BARRACKS.getId();
				Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
				InstanceManager.getInstance().addWorld(world);
				int time = 3600000; // 60 minutes
				instance.setDuration(time);
				instance.setEmptyDestroyTime(time);
				instance.setSpawnLoc(EXIT_POINT);
				instance.setName("Pailaka - Injured Dragon");
				instance.setAllowSummon(false);
				startInstance((Pailaka73World) world);
				world.allowed.add(player.getObjectId());
				player.teleToInstance(ENTRY_POINT, world.instanceId);
			}
		}
	}

	private void startInstance(Pailaka73World world)
	{
		L2Npc npc;
		for(int[] i : spawns1)
		{
			npc = addSpawn(i[0], i[1], i[2], i[3], i[4], false, 0, false, world.instanceId);
			world.zone1.add(npc);
			npc = spawnMageBehind(npc, behindWall.get(npc.getNpcId()));
			npc.getLocationController().decay();
			world.zone12.add(npc);
		}
		for(int[] i : spawns2)
		{
			npc = addSpawn(i[0], i[1], i[2], i[3], i[4], false, 0, false, world.instanceId);
			world.zone2.add(npc);
			npc = spawnMageBehind(npc, behindWall.get(npc.getNpcId()));
			npc.getLocationController().decay();
			world.zone22.add(npc);
		}
		for(int[] i : spawns3)
		{
			npc = addSpawn(i[0], i[1], i[2], i[3], i[4], false, 0, false, world.instanceId);
			world.zone3.add(npc);
			npc = spawnMageBehind(npc, behindWall.get(npc.getNpcId()));
			npc.getLocationController().decay();
			world.zone32.add(npc);
		}
		for(int[] i : spawns4)
		{
			npc = addSpawn(i[0], i[1], i[2], i[3], i[4], false, 0, false, world.instanceId);
			world.zone4.add(npc);
			npc = spawnMageBehind(npc, behindWall.get(npc.getNpcId()));
			npc.getLocationController().decay();
			world.zone42.add(npc);
		}
		for(int[] i : spawns5)
		{
			npc = addSpawn(i[0], i[1], i[2], i[3], i[4], false, 0, false, world.instanceId);
			world.zone5.add(npc);
			npc = spawnMageBehind(npc, behindWall.get(npc.getNpcId()));
			npc.getLocationController().decay();
			world.zone52.add(npc);
		}
		for(int[] i : spawns6)
		{
			npc = addSpawn(i[0], i[1], i[2], i[3], i[4], false, 0, false, world.instanceId);
			world.zone6.add(npc);
			npc = spawnMageBehind(npc, behindWall.get(npc.getNpcId()));
			npc.getLocationController().decay();
			world.zone62.add(npc);
		}
		for(int[] i : spawns7)
		{
			npc = addSpawn(i[0], i[1], i[2], i[3], i[4], false, 0, false, world.instanceId);
			world.zone7.add(npc);
			npc = spawnMageBehind(npc, behindWall.get(npc.getNpcId()));
			npc.getLocationController().decay();
			world.zone72.add(npc);
		}
		for(int[] i : spawns8)
		{
			npc = addSpawn(i[0], i[1], i[2], i[3], i[4], false, 0, false, world.instanceId);
			world.zone8.add(npc);
			npc = spawnMageBehind(npc, behindWall.get(npc.getNpcId()));
			npc.getLocationController().decay();
			world.zone82.add(npc);
		}
		for(int[] i : spawns9)
		{
			npc = addSpawn(i[0], i[1], i[2], i[3], i[4], false, 0, false, world.instanceId);
			world.zone9.add(npc);
			npc = spawnMageBehind(npc, behindWall.get(npc.getNpcId()));
			npc.getLocationController().decay();
			world.zone92.add(npc);
		}
		for(int[] i : otherspawns)
		{
			addSpawn(i[0], i[1], i[2], i[3], i[4], false, 0, false, world.instanceId);
		}
		world.Latana = addSpawn(LATANA, 105691, -41817, -1783, 36394, false, 0, false, world.instanceId);
		world.Latana.getSpawn().setOnKillDelay(0);
		world.Latana.disableCoreAI(true);
		world.Latana.setIsInvul(true);
	}

	private void checkOnKill(L2Npc npc, Pailaka73World world)
	{
		if(world.zone1 != null && world.zone1.contains(npc))
		{
			world.zone1.remove(npc);
			if(world.zone1.isEmpty())
			{
				world.zone[0] = true;
				world.zone1 = null;
				for(L2Npc npc2 : world.zone12)
				{
					npc2.getLocationController().delete();
				}
				world.zone12 = null;
			}
		}
		else if(world.zone2 != null && world.zone2.contains(npc))
		{
			world.zone2.remove(npc);
			if(world.zone2.isEmpty())
			{
				world.zone[1] = true;
				world.zone2 = null;
				for(L2Npc npc2 : world.zone22)
				{
					npc2.getLocationController().delete();
				}
				world.zone22 = null;
			}
		}
		else if(world.zone3 != null && world.zone3.contains(npc))
		{
			world.zone3.remove(npc);
			if(world.zone3.isEmpty())
			{
				world.zone[2] = true;
				world.zone3 = null;
				for(L2Npc npc2 : world.zone32)
				{
					npc2.getLocationController().delete();
				}
				world.zone32 = null;
			}
		}
		else if(world.zone4 != null && world.zone4.contains(npc))
		{
			world.zone4.remove(npc);
			if(world.zone4.isEmpty())
			{
				world.zone[3] = true;
				world.zone4 = null;
				for(L2Npc npc2 : world.zone42)
				{
					npc2.getLocationController().delete();
				}
				world.zone42 = null;
			}
		}
		else if(world.zone5 != null && world.zone5.contains(npc))
		{
			world.zone5.remove(npc);
			if(world.zone5.isEmpty())
			{
				world.zone[4] = true;
				world.zone5 = null;
				for(L2Npc npc2 : world.zone52)
				{
					npc2.getLocationController().delete();
				}
				world.zone52 = null;
			}
		}
		else if(world.zone6 != null && world.zone6.contains(npc))
		{
			world.zone6.remove(npc);
			if(world.zone6.isEmpty())
			{
				world.zone[5] = true;
				world.zone6 = null;
				for(L2Npc npc2 : world.zone62)
				{
					npc2.getLocationController().delete();
				}
				world.zone62 = null;
			}
		}
		else if(world.zone7 != null && world.zone7.contains(npc))
		{
			world.zone7.remove(npc);
			if(world.zone7.isEmpty())
			{
				world.zone[6] = true;
				world.zone7 = null;
				for(L2Npc npc2 : world.zone72)
				{
					npc2.getLocationController().delete();
				}
				world.zone72 = null;
			}
		}
		else if(world.zone8 != null && world.zone8.contains(npc))
		{
			world.zone8.remove(npc);
			if(world.zone8.isEmpty())
			{
				world.zone[7] = true;
				world.zone8 = null;
				for(L2Npc npc2 : world.zone82)
				{
					npc2.getLocationController().delete();
				}
				world.zone82 = null;
			}
		}
		else if(world.zone9 != null && world.zone9.contains(npc))
		{
			world.zone9.remove(npc);
			if(world.zone9.isEmpty())
			{
				world.zone[8] = true;
				world.zone9 = null;
				for(L2Npc npc2 : world.zone92)
				{
					npc2.getLocationController().delete();
				}
				world.zone92 = null;
			}
		}
	}

	private void checkOnAttack(L2Npc npc, Pailaka73World world)
	{
		if(world.zone1 != null && !world.spawned[0] && world.zone1.contains(npc))
		{
			for(L2Npc npc1 : world.zone12)
			{
				npc1.getLocationController().spawn();
			}
			world.spawned[0] = true;
		}
		else if(world.zone2 != null && !world.spawned[1] && world.zone2.contains(npc))
		{
			for(L2Npc npc1 : world.zone22)
			{
				npc1.getLocationController().spawn();
			}
			world.spawned[1] = true;
		}
		else if(world.zone3 != null && !world.spawned[2] && world.zone3.contains(npc))
		{
			for(L2Npc npc1 : world.zone32)
			{
				npc1.getLocationController().spawn();
			}
			world.spawned[2] = true;
		}
		else if(world.zone4 != null && !world.spawned[3] && world.zone4.contains(npc))
		{
			for(L2Npc npc1 : world.zone42)
			{
				npc1.getLocationController().spawn();
			}
			world.spawned[3] = true;
		}
		else if(world.zone5 != null && !world.spawned[4] && world.zone5.contains(npc))
		{
			for(L2Npc npc1 : world.zone52)
			{
				npc1.getLocationController().spawn();
			}
			world.spawned[4] = true;
		}
		else if(world.zone6 != null && !world.spawned[5] && world.zone6.contains(npc))
		{
			for(L2Npc npc1 : world.zone62)
			{
				npc1.getLocationController().spawn();
			}
			world.spawned[5] = true;
		}
		else if(world.zone7 != null && !world.spawned[6] && world.zone7.contains(npc))
		{
			for(L2Npc npc1 : world.zone72)
			{
				npc1.getLocationController().spawn();
			}
			world.spawned[6] = true;
		}
		else if(world.zone8 != null && !world.spawned[7] && world.zone8.contains(npc))
		{
			for(L2Npc npc1 : world.zone82)
			{
				npc1.getLocationController().spawn();
			}
			world.spawned[7] = true;
		}
		else if(world.zone9 != null && !world.spawned[8] && world.zone9.contains(npc))
		{
			for(L2Npc npc1 : world.zone92)
			{
				npc1.getLocationController().spawn();
			}
			world.spawned[8] = true;
		}
	}

	private L2Npc spawnMageBehind(L2Npc npc, int mageId)
	{
		double rads = Math.toRadians(Util.convertHeadingToDegree(npc.getSpawn().getHeading()) + 180);
		int mageX = (int) (npc.getX() + 150 * Math.cos(rads));
		int mageY = (int) (npc.getY() + 150 * Math.sin(rads));
		return addSpawn(mageId, mageX, mageY, npc.getZ(), npc.getSpawn().getHeading(), false, 0, false, npc.getInstanceId());
	}

	@Override
	public int getQuestId()
	{
		return 144;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(attacker.getInstanceId() > 0)
		{
			QuestState st = attacker.getQuestState(getClass());

			if(st != null && npc.getNpcId() == LATANA)
			{
				InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				if(tmpworld instanceof Pailaka73World)
				{
					if(npc.getNpcId() == LATANA)
					{
						if(Rnd.getChance(10) && npc.getCurrentHp() < npc.getMaxHp() * 0.3)
						{
							npc.setTarget(npc);
							npc.doCast(SkillTable.getInstance().getInfo(5718, 1));
						}
					}
					else
					{
						if(Util.checkIfInRange(60, npc, attacker, false) && Rnd.getChance(15))
						{
							checkOnAttack(npc, (Pailaka73World) tmpworld);
						}
					}
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}

		if(event.equals("Enter"))
		{
			if(player.getLevel() < MIN_LEVEL)
			{
				return null;
			}
			if(player.getLevel() > MAX_LEVEL)
			{
				return null;
			}
			enterInstance(player);
			return null;
		}
		if(event.equals("32499-02.htm")) // Shouldn't be 32499-04.htm ???
		{
			st.startQuest();
		}
		else if(event.equals("32499-05.htm"))
		{
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if(event.equals("32502-05.htm"))
		{
			if(!st.hasQuestItems(SPEAR))
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				st.giveItems(SPEAR, 1);
			}
		}
		else if(event.equals("32509-02.htm"))
		{
			if(st.hasQuestItems(SPEAR))
			{
				st.takeItems(SPEAR, 1);
				st.giveItems(ENCHSPEAR, 1);
				return "32509-02.htm";
			}
			else if(st.hasQuestItems(ENCHSPEAR))
			{
				st.takeItems(ENCHSPEAR, 1);
				st.giveItems(LASTSPEAR, 1);
				return "32509-03.htm";
			}
			else
			{
				return st.hasQuestItems(LASTSPEAR) ? "32509-03.htm" : "32509-07.htm";
			}
		}
		else if(event.equals("32512-02.htm"))
		{

		}
		else if(player.getInstanceId() > 0)
		{
			Pailaka73World world = (Pailaka73World) InstanceManager.getInstance().getWorld(player.getInstanceId());
			if(NumberUtils.isDigits(event))
			{
				if(world.buff_counter > 0)
				{
					int nr = Integer.parseInt(event);
					giveBuff(npc, player, nr, world);
					return "32509-06.htm";
				}
				else
				{
					return "32509-05.htm";
				}
			}
			else if(event.equals("32509-06.htm"))
			{
				if(world.buff_counter < 1)
				{
					return "32509-05.htm";
				}
			}
			else if(event.equals("camera1"))
			{
				world._hasDoneAnimation = true;
				player.abortAttack();
				player.abortCast();
				player.stopMove(null);
				if(!player.getPets().isEmpty())
				{
					for(L2Summon pet : player.getPets())
					{
						pet.abortAttack();
						pet.abortCast();
						pet.stopMove(null);
					}
				}
				player.sendPacket(new SpecialCamera(world.camera1.getObjectId(), 600, 200, 5, 0, 10000, -10, 8, 1, 1));
				startQuestTimer("camera2", 2000, npc, player);
				return null;
			}
			else if(event.equals("camera2"))
			{
				npc.getAttackable().attackCharacter(world.target1);
				player.sendPacket(new SpecialCamera(world.camera1.getObjectId(), 400, 200, 5, 4000, 10000, -10, 8, 1, 1));
				startQuestTimer("camera3", 4000, npc, player);
				return null;
			}
			else if(event.equals("camera3"))
			{
				npc.broadcastPacket(new MagicSkillUse(npc, world.target1, 5759, 1, 9700, 0));
				player.sendPacket(new SpecialCamera(world.camera1.getObjectId(), 300, 195, 4, 1500, 10000, -5, 10, 1, 1));
				startQuestTimer("camera4", 1700, npc, player);
				return null;
			}
			else if(event.equals("camera4"))
			{
				player.sendPacket(new SpecialCamera(npc.getObjectId(), 130, 2, 5, 0, 10000, 0, 0, 1, 0));
				startQuestTimer("camera5", 2000, npc, player);
				return null;
			}
			else if(event.equals("camera5"))
			{
				player.sendPacket(new SpecialCamera(npc.getObjectId(), 220, 0, 4, 800, 10000, 5, 10, 1, 0));
				startQuestTimer("camera6", 2000, npc, player);
				return null;
			}
			else if(event.equals("camera6"))
			{
				player.sendPacket(new SpecialCamera(npc.getObjectId(), 250, 185, 5, 4000, 10000, -5, 10, 1, 1));
				startQuestTimer("camera7", 4000, npc, player);
				return null;
			}
			else if(event.equals("camera7"))
			{
				npc.broadcastPacket(new MagicSkillUse(npc, world.target1, 5759, 1, 9700, 0));
				player.sendPacket(new SpecialCamera(npc.getObjectId(), 200, 0, 5, 2000, 10000, 0, 25, 1, 0));
				startQuestTimer("camera8", 4530, npc, player);
				return null;
			}
			else if(event.equals("camera8"))
			{
				player.sendPacket(new SpecialCamera(npc.getObjectId(), 300, -3, 5, 3500, 10000, 0, 6, 1, 0));
				startQuestTimer("camera9", 10000, npc, player);
				return null;
			}
			else if(event.equals("camera9"))
			{
				world.camera1.getLocationController().delete();
				world.target1.getLocationController().delete();
				world.Latana.disableCoreAI(false);
				world.Latana.setIsInvul(false);
				world.Latana.getAttackable().clearAggroList();
				world.Latana.getAttackable().attackCharacter(player);
				return null;
			}
			else if(event.equals("camera10"))
			{
				player.abortAttack();
				player.abortCast();
				player.stopMove(null);
				if(!player.getPets().isEmpty())
				{
					for(L2Summon pet : player.getPets())
					{
						pet.abortAttack();
						pet.abortCast();
						pet.stopMove(null);
					}
				}
				player.sendPacket(new SpecialCamera(npc.getObjectId(), 450, 200, 3, 0, 10000, -15, 20, 1, 1));
				startQuestTimer("camera11", 100, npc, player);
				return null;
			}
			else if(event.equals("camera11"))
			{
				player.sendPacket(new SpecialCamera(npc.getObjectId(), 350, 200, 5, 5600, 10000, -15, 10, 1, 1));
				startQuestTimer("camera12", 5600, npc, player);
				return null;
			}
			else if(event.equals("camera12"))
			{
				player.sendPacket(new SpecialCamera(npc.getObjectId(), 360, 200, 5, 1000, 2000, -15, 10, 1, 1));
				startQuestTimer("camera13", 10000, npc, player);
				return null;
			}
			else if(event.equals("camera13"))
			{
				addSpawn(KETRA_ORC_SUPPORTER2, 105764, -41657, -1784, 0, false, 0, false, player.getInstanceId());
				npc.getLocationController().delete();
				return null;
			}
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		Pailaka73World world = (Pailaka73World) InstanceManager.getInstance().getWorld(player.getInstanceId());
		if(world == null)
		{
			return super.onKill(npc, player, isPet);
		}
		checkOnKill(npc, world);
		switch(npc.getNpcId())
		{
			case LATANA:
				world.camera2 = addSpawn(18604, 105974, -41794, -1784, 32768, false, 0, false, world.instanceId);
				startQuestTimer("camera10", 100, world.camera2, player);
				QuestState st = player.getQuestState(getClass());
				if(st == null)
				{
					break;
				}
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			default:
				dropItem(npc, player);
				dropHerb(npc, player, HP_HERBS_DROPLIST);
				dropHerb(npc, player, MP_HERBS_DROPLIST);
				break;
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		switch(npc.getNpcId())
		{
			case KETRA_ORC_SHAMAN:
				switch(st.getState())
				{
					case CREATED:
						if(player.getLevel() < MIN_LEVEL)
						{
							return "32499-no.htm";
						}
						if(player.getLevel() > MAX_LEVEL)
						{
							return "32499-no.htm";
						}
						return "32499-01.htm";
					case STARTED:
						if(player.getLevel() < MIN_LEVEL)
						{
							return "32499-no.htm";
						}
						if(player.getLevel() > MAX_LEVEL)
						{
							return "32499-no.htm";
						}
						if(st.getCond() > 1)
						{
							return "32499-06.htm";
						}
					case COMPLETED:
						return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
					default:
						return getNoQuestMsg(player);
				}
			case KETRA_ORC_SUPPORTER:
				return st.getCond() > 2 ? "32502-05.htm" : "32502-01.htm";
			case KETRA_ORC_SUPPORTER2:
				if(st.isCompleted())
				{
					return "32512-02.htm";
				}
				if(st.getCond() == 4)
				{
					npc.setTarget(player);
					st.addExpAndSp(28000000, 2850000);
					st.giveItems(SCROLL_OF_ESCAPE, 1);
					st.giveItems(PSHIRT, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
					inst.setDuration(EXIT_TIME * 60000);
					inst.setEmptyDestroyTime(0);
					return "32512-01.htm";
				}
				break;
		}
		return getNoQuestMsg(player);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			newQuestState(player);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return "32509-00.htm";
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsOverloaded(true);
		return super.onSpawn(npc);
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2Playable && !character.isDead() && !character.isTeleporting())
		{
			Pailaka73World world = (Pailaka73World) InstanceManager.getInstance().getWorld(character.getInstanceId());
			if(zone.getId() == 200010)
			{
				if(world != null && !world._hasDoneAnimation && character instanceof L2PcInstance)
				{
					QuestState st = ((L2PcInstance) character).getQuestState(getClass());
					startQuestTimer("camera1", 100, world.Latana, character.getActingPlayer());
					world.target1 = addSpawn(18605, 105465, -41817, -1768, 0, false, 0, false, world.instanceId);
					world.camera1 = addSpawn(18603, 105974, -41794, -1784, 32768, false, 0, false, world.instanceId);
					st.playSound(QuestSound.OTHER_PAILAKA_BS);
				}
			}
			else if(world != null && world.templateId == InstanceZoneId.PAILAKA_VARKA_SILENOS_BARRACKS.getId() && !world.zone[zone.getId() - 200001])
			{
				teleportPlayer(character.getActingPlayer(), NOEXIT_ZONES.get(zone.getId()), world.instanceId);
			}
		}
		return super.onEnterZone(character, zone);
	}

	private static class PailakaDrop
	{
		private final int _itemId;
		private final int _chance;

		public PailakaDrop(int itemId, int chance)
		{
			_itemId = itemId;
			_chance = chance;
		}

		public int getItemID()
		{
			return _itemId;
		}

		public int getChance()
		{
			return _chance;
		}
	}

	public class Pailaka73World extends InstanceWorld
	{
		L2Npc target1;
		L2Npc camera1;
		L2Npc camera2;
		private int buff_counter = 5;
		private boolean _hasDoneAnimation;
		private boolean[] zone = {
			false, false, false, false, false, false, false, false, false
		};
		private boolean[] spawned = {
			false, false, false, false, false, false, false, false, false
		};
		private List<L2Npc> zone1 = new ArrayList<>();
		private List<L2Npc> zone12 = new ArrayList<>();
		private List<L2Npc> zone2 = new ArrayList<>();
		private List<L2Npc> zone22 = new ArrayList<>();
		private List<L2Npc> zone3 = new ArrayList<>();
		private List<L2Npc> zone32 = new ArrayList<>();
		private List<L2Npc> zone4 = new ArrayList<>();
		private List<L2Npc> zone42 = new ArrayList<>();
		private List<L2Npc> zone5 = new ArrayList<>();
		private List<L2Npc> zone52 = new ArrayList<>();
		private List<L2Npc> zone6 = new ArrayList<>();
		private List<L2Npc> zone62 = new ArrayList<>();
		private List<L2Npc> zone7 = new ArrayList<>();
		private List<L2Npc> zone72 = new ArrayList<>();
		private List<L2Npc> zone8 = new ArrayList<>();
		private List<L2Npc> zone82 = new ArrayList<>();
		private List<L2Npc> zone9 = new ArrayList<>();
		private List<L2Npc> zone92 = new ArrayList<>();
		private L2Npc Latana;
	}
}