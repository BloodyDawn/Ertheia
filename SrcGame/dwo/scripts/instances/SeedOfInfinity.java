package dwo.scripts.instances;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.instancemanager.GraciaSeedsManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Util;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class SeedOfInfinity extends Quest
{
	private static final String qn = SeedOfInfinity.class.getSimpleName();

	private static final int EKIMUS = 29150;

	private static final int[][] MONSTERS = {
		{EKIMUS, -179527, 209660, -15515, 0}
	};
	private static final int START_NPC = 32537;
	private static final int TELEPORTER_NPC = 32540;
	private static final int[] SPAWN = {
		-179199, 210379, -15504
	};
	private static final int[] FARM_SPAWN = {
		-178237, 209699, -12467
	};
	private static final int[][] FARM_SPAWNS = {
		{22512, -176275, 209772, -11945, 0, 60, 0}, {18680, -175845, 209458, -11927, 0, 60, 0},
		{22509, -175845, 209458, -11927, 0, 60, 0}, {22512, -175993, 209617, -11935, 0, 60, 0},
		{22514, -176030, 209657, -11942, 0, 60, 0}, {22513, -175526, 209689, -11929, 0, 60, 0},
		{22513, -175821, 209841, -11934, 0, 60, 0}, {22509, -175962, 210060, -11935, 0, 60, 0},
		{22510, -175709, 210182, -11940, 0, 60, 0}, {22514, -176125, 210283, -11937, 0, 60, 0},
		{22515, -176096, 210630, -11929, 0, 60, 0}, {22512, -176233, 209950, -11954, 0, 60, 0},
		{22512, -176233, 209950, -11954, 0, 60, 0}, {22511, -176382, 210387, -11931, 0, 60, 0},
		{22515, -176275, 209772, -11945, 0, 60, 0}, {22511, -175887, 209388, -11924, 0, 60, 0},
		{22510, -178355, 211512, -12039, 0, 60, 0}, {22511, -178298, 211489, -12040, 0, 60, 0},
		{22512, -178251, 211443, -12040, 0, 60, 0}, {22513, -178192, 211410, -12038, 0, 60, 0},
		{22514, -178114, 211392, -12034, 0, 60, 0}, {22515, -178016, 211391, -12029, 0, 60, 0},
		{22512, -178233, 211415, -12041, 0, 60, 0}, {22512, -178121, 211942, -12038, 0, 60, 0},
		{22515, -178465, 211956, -12033, 0, 60, 0}, {22511, -178794, 211845, -12024, 0, 60, 0},
		{22513, -178721, 211117, -12033, 0, 60, 0}, {22510, -178394, 211192, -12064, 0, 60, 0},
		{22514, -178341, 211585, -12037, 0, 60, 0}, {22512, -178749, 211372, -12023, 0, 60, 0},
		{22515, -178749, 211372, -12023, 0, 60, 0}, {22515, -178749, 211372, -12023, 0, 60, 0},
		{22512, -178668, 211401, -12023, 0, 60, 0}, {22515, -178233, 211486, -12025, 0, 60, 0},
		{22511, -177800, 211693, -12022, 0, 60, 0}, {22513, -178241, 212331, -12039, 0, 60, 0},
		{22510, -178500, 212303, -12032, 0, 60, 0}, {22514, -178744, 212145, -12028, 0, 60, 0},
		{22512, -178775, 212082, -12028, 0, 60, 0}, {22515, -178888, 211794, -12017, 0, 60, 0},
		{22511, -178766, 211366, -12025, 0, 60, 0}, {22513, -178290, 211312, -12051, 0, 60, 0},
		{22510, -178137, 211425, -12036, 0, 60, 0}, {22514, -178021, 212036, -12042, 0, 60, 0},
		{22512, -178844, 211655, -12016, 0, 60, 0}, {22512, -178812, 211556, -12017, 0, 60, 0},
		{22512, -178812, 211556, -12017, 0, 60, 0}, {22512, -178879, 212140, -12026, 0, 60, 0},
		{22515, -178882, 211570, -12019, 0, 60, 0}, {22511, -178882, 211570, -12019, 0, 60, 0},
		{22515, -178913, 211755, -12016, 0, 60, 0}, {22511, -179034, 211253, -12017, 0, 60, 0},
		{22513, -178323, 211235, -12043, 0, 60, 0}, {22510, -178178, 211761, -12031, 0, 60, 0},
		{22514, -177870, 211725, -12024, 0, 60, 0}, {22515, -177824, 211364, -12022, 0, 60, 0},
		{22511, -178225, 211525, -12033, 0, 60, 0}, {22513, -178369, 212009, -12034, 0, 60, 0},
		{22510, -178611, 211572, -12031, 0, 60, 0}, {22514, -178425, 211450, -12038, 0, 60, 0},
		{22512, -176233, 209950, -11954, 0, 60, 0}, {22515, -175993, 209617, -11935, 0, 60, 0},
		{22511, -176276, 209463, -11930, 0, 60, 0}, {22513, -175526, 209689, -11929, 0, 60, 0},
		{22510, -175709, 210182, -11940, 0, 60, 0}, {22514, -176125, 210283, -11937, 0, 60, 0},
		{22515, -176069, 210630, -11929, 0, 60, 0}, {22511, -176382, 210387, -11931, 0, 60, 0},
		{22513, -175821, 209841, -11934, 0, 60, 0}, {22514, -176030, 209898, -11942, 0, 60, 0},
		{22512, -176275, 209772, -11945, 0, 60, 0}, {22515, -176275, 209772, -11945, 0, 60, 0},
		{22512, -176295, 207901, -11938, 0, 60, 0}, {22515, -176058, 208020, -11938, 0, 60, 0},
		{22511, -175992, 208382, -11931, 0, 60, 0}, {22513, -175852, 208587, -11927, 0, 60, 0},
		{22510, -175647, 208197, -11934, 0, 60, 0}, {22514, -175646, 207974, -11949, 0, 60, 0},
		{22512, -175938, 207707, -11936, 0, 60, 0}, {22515, -176191, 207949, -11938, 0, 60, 0},
		{22510, -175593, 208831, -11928, 0, 60, 0}, {22514, -175672, 208629, -11930, 0, 60, 0},
		{22512, -177844, 208700, -12420, 0, 60, 0}, {22512, -178461, 208111, -12413, 0, 60, 0},
		{22515, -178424, 206971, -12086, 0, 60, 0}, {22511, -178200, 206690, -12037, 0, 60, 0},
		{22513, -178250, 206202, -12041, 0, 60, 0}, {22510, -178623, 206043, -12032, 0, 60, 0},
		{22514, -178877, 206217, -12022, 0, 60, 0}, {22512, -178991, 206486, -12019, 0, 60, 0},
		{22515, -178405, 206744, -12042, 0, 60, 0}, {22511, -178519, 206555, -12037, 0, 60, 0},
		{22513, -178301, 205970, -12040, 0, 60, 0}, {22510, -177734, 206223, -12029, 0, 60, 0},
		{22514, -177928, 206168, -12040, 0, 60, 0}, {22515, -177881, 206559, -12026, 0, 60, 0},
		{22511, -178115, 206851, -12033, 0, 60, 0}, {22511, -178687, 206812, -12028, 0, 60, 0},
		{22513, -178924, 207125, -12018, 0, 60, 0}, {22510, -179158, 206808, -12017, 0, 60, 0},
		{22514, -179014, 206101, -12023, 0, 60, 0}, {22515, -178461, 208111, -12413, 0, 60, 0},
		{22512, -178461, 208111, -12413, 0, 60, 0}, {22512, -180883, 208155, -12427, 0, 60, 0},
		{22512, -180975, 207071, -12071, 0, 60, 0}, {22515, -181203, 206941, -12047, 0, 60, 0},
		{22511, -181679, 206644, -11976, 0, 60, 0}, {22513, -181326, 206219, -12035, 0, 60, 0},
		{22510, -180928, 206087, -12039, 0, 60, 0}, {22514, -180887, 206088, -12038, 0, 60, 0},
		{22515, -180582, 206252, -12029, 0, 60, 0}, {22511, -180267, 206399, -12020, 0, 60, 0},
		{22513, -180781, 206682, -12032, 0, 60, 0}, {22510, -181049, 206620, -12036, 0, 60, 0},
		{22514, -181390, 206524, -12025, 0, 60, 0}, {22511, -181279, 205892, -12042, 0, 60, 0},
		{22514, -180217, 206219, -12022, 0, 60, 0}, {22512, -180468, 206586, -12015, 0, 60, 0},
		{22515, -180628, 206481, -12023, 0, 60, 0}, {22510, -181075, 206411, -12031, 0, 60, 0},
		{22514, -181160, 206246, -12040, 0, 60, 0}, {22511, -180669, 206936, -12035, 0, 60, 0},
		{22513, -180499, 207087, -12021, 0, 60, 0}, {22512, -182906, 208305, -11984, 0, 60, 0},
		{22515, -183312, 208851, -11927, 0, 60, 0}, {22511, -183538, 208716, -11927, 0, 60, 0},
		{22513, -183727, 207940, -11948, 0, 60, 0}, {22510, -183480, 207843, -11944, 0, 60, 0},
		{22514, -183346, 207465, -11928, 0, 60, 0}, {22512, -183121, 207598, -11928, 0, 60, 0},
		{22515, -183082, 207799, -11931, 0, 60, 0}, {22511, -183069, 208150, -11942, 0, 60, 0},
		{22513, -183495, 208270, -11934, 0, 60, 0}, {22510, -183428, 208036, -11940, 0, 60, 0},
		{22512, -183279, 208088, -11941, 0, 60, 0}, {22510, -182922, 208102, -11962, 0, 60, 0},
		{22514, -182735, 207917, -11938, 0, 60, 0}, {22512, -183121, 207598, -11928, 0, 60, 0},
		{22512, -183121, 207598, -11928, 0, 60, 0}, {22511, -181467, 209502, -12422, 0, 60, 0},
		{22515, -182903, 209889, -11985, 0, 60, 0}, {22511, -182881, 210281, -11943, 0, 60, 0},
		{22512, -183050, 210470, -11931, 0, 60, 0}, {22513, -183640, 210396, -11955, 0, 60, 0},
		{22510, -183778, 210047, -11939, 0, 60, 0}, {22514, -183722, 209836, -11936, 0, 60, 0},
		{22512, -183505, 209601, -11926, 0, 60, 0}, {22515, -183350, 209389, -11927, 0, 60, 0},
		{22511, -183081, 209484, -11935, 0, 60, 0}, {22510, -182626, 210334, -11922, 0, 60, 0},
		{22514, -183124, 210666, -11927, 0, 60, 0}, {22513, -183494, 210533, -11941, 0, 60, 0},
		{22511, -183445, 210262, -11943, 0, 60, 0}, {22512, -183566, 210031, -11940, 0, 60, 0},
		{22514, -183315, 209813, -11935, 0, 60, 0}, {22512, -183092, 209697, -11938, 0, 60, 0},
		{22515, -183010, 210084, -11956, 0, 60, 0}, {22513, -182792, 210174, -11945, 0, 60, 0},
		{22510, -182903, 209889, -11985, 0, 60, 0}, {22510, -180915, 210107, -12414, 0, 60, 0},
		{22512, -180904, 211237, -12059, 0, 60, 0}, {22515, -181175, 211937, -12040, 0, 60, 0},
		{22511, -181175, 211937, -12040, 0, 60, 0}, {22512, -181083, 211692, -12033, 0, 60, 0},
		{22515, -181220, 212016, -12041, 0, 60, 0}, {22511, -180982, 212136, -12038, 0, 60, 0},
		{22513, -180735, 211701, -12030, 0, 60, 0}, {22510, -181161, 211379, -12035, 0, 60, 0},
		{22514, -181424, 211567, -12029, 0, 60, 0}, {22512, -181470, 212041, -12031, 0, 60, 0},
		{22515, -181119, 212334, -12037, 0, 60, 0}, {22511, -180781, 211969, -12032, 0, 60, 0},
		{22513, -180542, 211650, -12023, 0, 60, 0}, {22510, -180104, 211430, -12018, 0, 60, 0},
		{22512, -180433, 211413, -12017, 0, 60, 0}, {22515, -180908, 211412, -12042, 0, 60, 0},
		{22510, -181409, 211358, -12026, 0, 60, 0}, {22512, -181577, 211124, -12024, 0, 60, 0},
		{22513, -179671, 209677, -12435, 0, 60, 0}, {22512, -179659, 210949, -12758, 0, 60, 0},
		{22515, -179563, 211293, -12789, 0, 60, 0}, {22511, -179723, 211315, -12786, 0, 60, 0},
		{22511, -179783, 210772, -12794, 0, 60, 0}, {22510, -179783, 210772, -12794, 0, 60, 0},
		{22513, -179424, 210838, -12792, 0, 60, 0}, {22511, -179476, 211078, -12790, 0, 60, 0},
		{22513, -179621, 210793, -12792, 0, 60, 0}, {22510, -179833, 210862, -12795, 0, 60, 0},
		{22511, -178424, 210134, -12413, 0, 60, 0}, {22512, -178539, 212137, -12033, 0, 60, 0},
		{22511, -178398, 211992, -12037, 0, 60, 0}, {22510, -178803, 211840, -12024, 0, 60, 0},
		{22511, -175887, 209388, -11924, 0, 60, 0}, {22512, -179499, 208711, -12472, 0, 60, 0},
		{22515, -179423, 208644, -12470, 0, 60, 0}, {22511, -179317, 208593, -12470, 0, 60, 0},
		{22513, -179024, 208526, -12461, 0, 60, 0}, {22510, -178983, 208389, -12470, 0, 60, 0},
		{22514, -178821, 208411, -12468, 0, 60, 0}, {22509, -178396, 211542, -12038, 0, 60, 0},
		{22509, -178973, 211447, -12018, 0, 60, 0}, {22509, -177847, 212068, -12033, 0, 60, 0},
		{22509, -178496, 211223, -12047, 0, 60, 0}, {22509, -178882, 211570, -12019, 0, 60, 0},
		{22509, -178627, 211068, -12052, 0, 60, 0}, {22509, -175845, 209458, -11927, 0, 60, 0},
		{22509, -176275, 209772, -11945, 0, 60, 0}, {22509, -175962, 210060, -11935, 0, 60, 0},
		{22509, -176047, 208614, -11929, 0, 60, 0}, {22509, -176187, 208303, -11940, 0, 60, 0},
		{22509, -175668, 208360, -11928, 0, 60, 0}, {22509, -178090, 206361, -12033, 0, 60, 0},
		{22509, -178448, 206165, -12033, 0, 60, 0}, {22509, -180368, 206685, -12015, 0, 60, 0},
		{22509, -180917, 205819, -12034, 0, 60, 0}, {22509, -180909, 206418, -12028, 0, 60, 0},
		{22509, -181486, 208681, -12415, 0, 60, 0}, {22509, -183683, 208392, -11930, 0, 60, 0},
		{22509, -183092, 208597, -11927, 0, 60, 0}, {22509, -183145, 207906, -11936, 0, 60, 0},
		{22509, -183351, 210434, -11937, 0, 60, 0}, {22509, -182980, 209920, -11956, 0, 60, 0},
		{22509, -181175, 211937, -12040, 0, 60, 0}, {22509, -180660, 212008, -12029, 0, 60, 0},
		{22509, -181168, 211198, -12044, 0, 60, 0}, {22509, -179903, 211066, -12799, 0, 60, 0},
		{22509, -178362, 211631, -12028, 0, 60, 0}, {22509, -179130, 208586, -12462, 0, 60, 0},
		{22509, -180783, 206887, -12047, 0, 60, 0}, {22509, -180783, 206887, -12047, 0, 60, 0},
		{22509, -180873, 206906, -12052, 0, 60, 0}, {22510, -180783, 206887, -12047, 0, 60, 0},
		{22510, -180873, 206906, -12052, 0, 60, 0}, {22511, -180894, 206826, -12045, 0, 60, 0},
		{22511, -180873, 206906, -12052, 0, 60, 0}, {22512, -180633, 206802, -12015, 0, 60, 0},
		{22512, -180873, 206906, -12052, 0, 60, 0}, {22513, -180783, 206887, -12047, 0, 60, 0},
		{22513, -180873, 206906, -12052, 0, 60, 0}, {22514, -180783, 206887, -12047, 0, 60, 0},
		{22514, -180873, 206906, -12052, 0, 60, 0}, {22515, -180894, 206826, -12045, 0, 60, 0},
		{22515, -180873, 206906, -12052, 0, 60, 0}, {22513, -178323, 211235, -12043, 0, 60, 0},
		{22509, -178627, 210865, -12052, 0, 60, 0}, {22510, -178611, 211572, -12031, 0, 60, 0},
		{22514, -178425, 211250, -12038, 0, 60, 0}, {22511, -178225, 211525, -12043, 0, 60, 0},
		{22515, -177824, 211364, -12022, 0, 60, 0}, {22510, -178178, 211761, -12031, 0, 60, 0},
		{22514, -177870, 211725, -12024, 0, 60, 0}, {22511, -178398, 211992, -12037, 0, 60, 0},
		{22513, -178369, 212009, -12034, 0, 60, 0},
	};
	private static final int INACTIVITYTIME = 900000;
	protected static long _LastAction;
	private static List<L2Npc> _spawnedMobs = new FastList<L2Npc>().shared();
	private static boolean ALLOW_ENTRY;
	// Task
	protected ScheduledFuture<?> _activityCheckTask;

	public SeedOfInfinity()
	{

		addStartNpc(START_NPC, TELEPORTER_NPC);
		addTalkId(START_NPC, TELEPORTER_NPC);

		addAttackId(EKIMUS, EKIMUS + 1, EKIMUS + 2);
		addKillId(EKIMUS);

		if(GraciaSeedsManager.getInstance().getSoIState() == 1)
		{
			ALLOW_ENTRY = true;
		}
		else
		{
			ALLOW_ENTRY = false;
			spawnMobs();
			long time = GraciaSeedsManager.getInstance().getSoITimeForNextStateChange();
			ThreadPoolManager.getInstance().scheduleGeneral(() -> ALLOW_ENTRY = true, time);
		}
	}

	public static void main(String[] args)
	{
		new SeedOfInfinity();
	}

	private void spawnMobs()
	{
		for(int[] spawn : FARM_SPAWNS)
		{
			addSpawnSoI(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], spawn[5], spawn[6]);
		}
	}

	private boolean checkConditions(L2PcInstance player)
	{
		L2Party party = player.getParty();
		if(party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		if(!party.getLeader().equals(player))
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		if(player.getParty().getMembers().size() < 7 && !player.isGM())
		{
			player.sendMessage("In order to enter you need to be in party with 9 party members.");
			return false;
		}
		if(!ALLOW_ENTRY)
		{
			player.sendMessage("You cannot enter right now another adventures are engaged in battle with Ekimus");
			return false;
		}
		for(L2PcInstance partyMember : party.getMembers())
		{
			if(partyMember.getLevel() < 75)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			else if(!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		_LastAction = System.currentTimeMillis();
		return null;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if("spawn".equals(event))
		{
			for(int[] loc : MONSTERS)
			{
				L2Npc temp = addSpawn(loc[0], loc[1], loc[2], loc[3], loc[4], false, 0);
				_spawnedMobs.add(temp);
			}
		}
		else if("StopSoIAi".equalsIgnoreCase(event))
		{
			unSpawnMobs();
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(npc.getNpcId() == EKIMUS)
		{
			_activityCheckTask.cancel(false);
			GraciaSeedsManager.getInstance().increaseSoIEkimusKilled();
			// spawning gate keeper
			_spawnedMobs.stream().filter(mob -> mob != null).forEach(mob -> mob.getLocationController().delete());
			_spawnedMobs.clear();
			spawnMobs();
			addSpawn(TELEPORTER_NPC, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 5 * 60 * 1000);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc.getNpcId() == START_NPC)
		{
			if(GraciaSeedsManager.getInstance().getSoIState() == 1)
			{
				if(checkConditions(player))
				{
					ALLOW_ENTRY = false;
					_LastAction = System.currentTimeMillis();
					// Start repeating timer to check for inactivity
					_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
					for(L2PcInstance member : player.getParty().getMembers())
					{
						member.teleToLocation(SPAWN[0], SPAWN[1], SPAWN[2], true);
						if(!player.getPets().isEmpty())
						{
							for(L2Summon pet : player.getPets())
							{
								pet.teleToLocation(SPAWN[0], SPAWN[1], SPAWN[2], true);
							}
						}
					}

					startQuestTimer("spawn", 5 * 60 * 1000, npc, player);
				}
			}
			else if(GraciaSeedsManager.getInstance().getSoIState() == 2)
			{
				player.teleToLocation(FARM_SPAWN[0], FARM_SPAWN[1], FARM_SPAWN[2], true);
				if(!player.getPets().isEmpty())
				{
					for(L2Summon pet : player.getPets())
					{
						pet.teleToLocation(FARM_SPAWN[0], FARM_SPAWN[1], FARM_SPAWN[2], true);
					}
				}
			}
		}
		return null;
	}

	public L2Npc addSpawnSoI(int npcId, int x, int y, int z, int heading, int respawnTime, int instanceId)
	{
		L2NpcTemplate npc = NpcTable.getInstance().getTemplate(npcId);
		if(npc == null)
		{
			_log.log(Level.INFO, "GraciaSeedsManager[GraciaSeedsManager.addSpawnSoI()]: L2NpcTemplate is Null, probably invalid npc id in the config?");
			return null;
		}

		L2Spawn _npcSpawn;

		try
		{
			_npcSpawn = new L2Spawn(npc);
			_npcSpawn.setLocx(x);
			_npcSpawn.setLocy(y);
			_npcSpawn.setLocz(z);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(heading);
			_npcSpawn.setRespawnDelay(respawnTime);
			_npcSpawn.setInstanceId(instanceId);
			SpawnTable.getInstance().addNewSpawn(_npcSpawn);
			_npcSpawn.init();

			_spawnedMobs.add(_npcSpawn.getLastSpawn());

			return _npcSpawn.getLastSpawn();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, e.getMessage(), e);
		}
		return null;
	}

	public void unSpawnMobs()
	{
		if(!_spawnedMobs.isEmpty())
		{
			_spawnedMobs.stream().filter(mob -> mob != null).forEach(mob -> mob.getLocationController().delete());
		}
		_spawnedMobs.clear();
	}

	private class CheckActivity implements Runnable
	{
		@Override
		public void run()
		{
			Long temp = System.currentTimeMillis() - _LastAction;
			if(temp > INACTIVITYTIME)
			{
				_activityCheckTask.cancel(false);
				ALLOW_ENTRY = true;
				_spawnedMobs.stream().filter(mob -> mob != null).forEach(mob -> mob.getLocationController().delete());
				_spawnedMobs.clear();
			}
		}
	}
}
