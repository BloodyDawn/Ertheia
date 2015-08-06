package dwo.scripts.ai.zone;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Util;
import javolution.util.FastList;

import java.util.List;

/**
 * User: Keiichi
 * Date: 13.07.12
 * Time: 1:34
 * L2GOD Team
 * Да, вот такой вот адовый пиздец, т.к. через волкеры сделать задуманное не получится,
 * т.к. еще куча примочек нужно делать в виде рандомного спавна Плетней в определенной зоне
 * и мобов которые ставят ловушки рандомно, спячих нпц ночью и т.д.
 */
public class GardenOfGenesis extends Quest
{
	private static final int mgg_gc_beholder = 22952;
	private static final int mgg_gc_lookout = 22964;
	private static final int mgg_gd_manager = 22954;
	// маршруты
	// 1я урна
	// 1й Смотритель Сада
	private static final Location mgg_gc_beholder1_1 = new Location(217711, 109466, -1300, 0);
	private static final Location mgg_gc_beholder1_2 = new Location(216755, 109470, -1300, 0);
	private static final Location mgg_gc_beholder1_3 = new Location(216089, 110142, -1300, 0);
	private static final Location mgg_gc_beholder1_4 = new Location(216088, 111086, -1300, 0);
	// 2й Смотритель Сада
	private static final Location mgg_gc_beholder2_1 = new Location(216091, 111235, -1300, 0);
	private static final Location mgg_gc_beholder2_2 = new Location(216098, 112183, -1300, 0);
	private static final Location mgg_gc_beholder2_3 = new Location(216780, 112868, -1300, 0);
	private static final Location mgg_gc_beholder2_4 = new Location(217719, 112876, -1300, 0);
	// 3й Смотритель Сада
	private static final Location mgg_gc_beholder3_1 = new Location(217860, 112872, -1300, 0);
	private static final Location mgg_gc_beholder3_2 = new Location(218789, 112862, -1300, 0);
	private static final Location mgg_gc_beholder3_3 = new Location(219484, 112174, -1300, 0);
	private static final Location mgg_gc_beholder3_4 = new Location(219491, 111232, -1300, 0);
	// 4й Смотритель Сада
	private static final Location mgg_gc_beholder4_1 = new Location(219482, 111102, -1300, 0);
	private static final Location mgg_gc_beholder4_2 = new Location(219479, 110114, -1300, 0);
	private static final Location mgg_gc_beholder4_3 = new Location(218835, 109471, -1300, 0);
	private static final Location mgg_gc_beholder4_4 = new Location(217854, 109462, -1300, 0);
	// 2я урна
	// 1й Смотритель Сада
	private static final Location mgg_gc_beholder5_1 = new Location(211294, 119296, -1300, 0);
	private static final Location mgg_gc_beholder5_2 = new Location(211297, 118315, -1300, 0);
	private static final Location mgg_gc_beholder5_3 = new Location(210619, 117662, -1300, 0);
	private static final Location mgg_gc_beholder5_4 = new Location(209663, 117653, -1300, 0);
	// 2й Смотритель Сада
	private static final Location mgg_gc_beholder6_1 = new Location(209540, 117657, -1300, 0);
	private static final Location mgg_gc_beholder6_2 = new Location(208570, 117655, -1300, 0);
	private static final Location mgg_gc_beholder6_3 = new Location(207897, 118329, -1300, 0);
	private static final Location mgg_gc_beholder6_4 = new Location(207901, 119269, -1300, 0);
	// 3й Смотритель Сада
	private static final Location mgg_gc_beholder7_1 = new Location(207900, 119423, -1300, 0);
	private static final Location mgg_gc_beholder7_2 = new Location(207900, 120364, -1300, 0);
	private static final Location mgg_gc_beholder7_3 = new Location(208585, 121051, -1300, 0);
	private static final Location mgg_gc_beholder7_4 = new Location(209524, 121063, -1300, 0);
	// 4й Смотритель Сада
	private static final Location mgg_gc_beholder8_1 = new Location(209666, 121058, -1300, 0);
	private static final Location mgg_gc_beholder8_2 = new Location(210614, 121052, -1300, 0);
	private static final Location mgg_gc_beholder8_3 = new Location(211297, 120378, -1300, 0);
	private static final Location mgg_gc_beholder8_4 = new Location(211304, 119421, -1300, 0);
	// 3я урна
	// 1й Смотритель Сада
	private static final Location mgg_gc_beholder9_1 = new Location(212861, 117267, -896, 0);
	private static final Location mgg_gc_beholder9_2 = new Location(214538, 117263, -896, 0);
	private static final Location mgg_gc_beholder9_3 = new Location(215695, 116089, -896, 0);
	// 2й Смотритель Сада
	private static final Location mgg_gc_beholder10_1 = new Location(215693, 116089, -896, 0);
	private static final Location mgg_gc_beholder10_2 = new Location(215693, 114415, -896, 0);
	private static final Location mgg_gc_beholder10_3 = new Location(214527, 113264, -896, 0);
	// 3й Смотритель Сада
	private static final Location mgg_gc_beholder11_1 = new Location(214527, 113264, -896, 0);
	private static final Location mgg_gc_beholder11_2 = new Location(212855, 113271, -896, 0);
	private static final Location mgg_gc_beholder11_3 = new Location(211697, 114428, -896, 0);
	// 4й Смотритель Сада
	private static final Location mgg_gc_beholder12_1 = new Location(211697, 114428, -896, 0);
	private static final Location mgg_gc_beholder12_2 = new Location(211693, 116096, -896, 0);
	private static final Location mgg_gc_beholder12_3 = new Location(212860, 117268, -896, 0);
	// Внутренняя дорожка
	// 1й Защитник Афроса
	private static final Location mgg_gc_lookout1_1 = new Location(216866, 119427, -1766, 0);
	private static final Location mgg_gc_lookout1_2 = new Location(216864, 119767, -1766, 0);
	private static final Location mgg_gc_lookout1_3 = new Location(217400, 120305, -1766, 0);
	private static final Location mgg_gc_lookout1_4 = new Location(217725, 120310, -1766, 0);
	// 2й Защитник Афроса
	private static final Location mgg_gc_lookout2_1 = new Location(217855, 120307, -1766, 0);
	private static final Location mgg_gc_lookout2_2 = new Location(218177, 120304, -1766, 0);
	private static final Location mgg_gc_lookout2_3 = new Location(218723, 119726, -1766, 0);
	private static final Location mgg_gc_lookout2_4 = new Location(218722, 119420, -1766, 0);
	// 3й Защитник Афроса
	private static final Location mgg_gc_lookout3_1 = new Location(218725, 119299, -1766, 0);
	private static final Location mgg_gc_lookout3_2 = new Location(218720, 118951, -1766, 0);
	private static final Location mgg_gc_lookout3_3 = new Location(218182, 118415, -1766, 0);
	private static final Location mgg_gc_lookout3_4 = new Location(217849, 118409, -1766, 0);
	// 4й Защитник Афроса
	private static final Location mgg_gc_lookout4_1 = new Location(217725, 118417, -1766, 0);
	private static final Location mgg_gc_lookout4_2 = new Location(217397, 118420, -1766, 0);
	private static final Location mgg_gc_lookout4_3 = new Location(216870, 118952, -1766, 0);
	private static final Location mgg_gc_lookout4_4 = new Location(216864, 119298, -1766, 0);
	// Внешняя дорожка
	// 1й Защитник Афроса
	private static final Location mgg_gc_lookout5_1 = new Location(216910, 118367, -1735, 0);
	private static final Location mgg_gc_lookout5_2 = new Location(217236, 118051, -1735, 0);
	private static final Location mgg_gc_lookout5_3 = new Location(218367, 118043, -1735, 0);
	private static final Location mgg_gc_lookout5_4 = new Location(218685, 118362, -1735, 0);
	// 2й Защитник Афроса
	private static final Location mgg_gc_lookout6_1 = new Location(218797, 118469, -1735, 0);
	private static final Location mgg_gc_lookout6_2 = new Location(219174, 118844, -1735, 0);
	private static final Location mgg_gc_lookout6_3 = new Location(219169, 119920, -1735, 0);
	private static final Location mgg_gc_lookout6_4 = new Location(218817, 120251, -1735, 0);
	// 3й Защитник Афроса
	private static final Location mgg_gc_lookout7_1 = new Location(218715, 120364, -1735, 0);
	private static final Location mgg_gc_lookout7_2 = new Location(218362, 120717, -1735, 0);
	private static final Location mgg_gc_lookout7_3 = new Location(217244, 120716, -1735, 0);
	private static final Location mgg_gc_lookout7_4 = new Location(216876, 120364, -1735, 0);
	// 4й Защитник Афроса
	private static final Location mgg_gc_lookout8_1 = new Location(216768, 120364, -1735, 0);
	private static final Location mgg_gc_lookout8_2 = new Location(216449, 119911, -1735, 0);
	private static final Location mgg_gc_lookout8_3 = new Location(216457, 118815, -1735, 0);
	private static final Location mgg_gc_lookout8_4 = new Location(216823, 118462, -1735, 0);
	private static int fountain_1_zone = 4600041;
	private static int fountain_2_zone = 4600042;
	private static int fountain_3_zone = 4600043;
	public List<L2PcInstance> PlayersInZone1 = new FastList<>();
	public List<L2PcInstance> PlayersInZone2 = new FastList<>();
	public List<L2PcInstance> PlayersInZone3 = new FastList<>();
	private L2Npc mgg_gc_beholder1;
	private L2Npc mgg_gc_beholder2;
	private L2Npc mgg_gc_beholder3;
	private L2Npc mgg_gc_beholder4;
	private L2Npc mgg_gc_beholder5;
	private L2Npc mgg_gc_beholder6;
	private L2Npc mgg_gc_beholder7;
	private L2Npc mgg_gc_beholder8;
	private L2Npc mgg_gc_beholder9;
	private L2Npc mgg_gc_beholder10;
	private L2Npc mgg_gc_beholder11;
	private L2Npc mgg_gc_beholder12;
	private L2Npc mgg_gc_lookout1;
	private L2Npc mgg_gc_lookout2;
	private L2Npc mgg_gc_lookout3;
	private L2Npc mgg_gc_lookout4;
	private L2Npc mgg_gc_lookout5;
	private L2Npc mgg_gc_lookout6;
	private L2Npc mgg_gc_lookout7;
	private L2Npc mgg_gc_lookout8;

	public GardenOfGenesis()
	{

		addKillId(mgg_gc_beholder);
		addKillId(mgg_gc_lookout);
		addAggroRangeEnterId(mgg_gc_beholder);
		addEnterZoneId(fountain_1_zone);
		addExitZoneId(fountain_1_zone);
		addEnterZoneId(fountain_2_zone);
		addExitZoneId(fountain_2_zone);
		addEnterZoneId(fountain_3_zone);
		addExitZoneId(fountain_3_zone);

		addSpawnId(mgg_gd_manager);

		startQuestTimer("mgg_gc_beholder_1_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_beholder_2_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_beholder_3_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_beholder_4_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_beholder_5_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_beholder_6_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_beholder_7_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_beholder_8_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_beholder_9_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_beholder_10_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_beholder_11_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_beholder_12_spawn", 6000, null, null);

		startQuestTimer("mgg_gc_lookout_1_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_lookout_2_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_lookout_3_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_lookout_4_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_lookout_5_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_lookout_6_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_lookout_7_spawn", 6000, null, null);
		startQuestTimer("mgg_gc_lookout_8_spawn", 6000, null, null);
	}

	public static void main(String[] args)
	{
		new GardenOfGenesis();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("Check400_1"))
		{
			PlayersInZone1.stream().filter(pc -> Util.checkIfInRange(400, npc, pc, true)).forEach(pc -> {
				if(!npc.isCastingNow() && !npc.isAttackingNow() && !npc.isInCombat() && !pc.isDead())
				{
					((L2Attackable) npc).attackCharacter(pc);
				}
			});
			startQuestTimer("Check400_1", 1500, npc, null);
		}
		else if(event.equalsIgnoreCase("Check400_2"))
		{
			PlayersInZone2.stream().filter(pc -> Util.checkIfInRange(400, npc, pc, true)).forEach(pc -> {
				if(!npc.isCastingNow() && !npc.isAttackingNow() && !npc.isInCombat() && !pc.isDead())
				{
					((L2Attackable) npc).attackCharacter(pc);
				}
			});
			startQuestTimer("Check400_2", 1500, npc, null);
		}
		else if(event.equalsIgnoreCase("Check400_3"))
		{
			PlayersInZone3.stream().filter(pc -> Util.checkIfInRange(400, npc, pc, true)).forEach(pc -> {
				if(!npc.isCastingNow() && !npc.isAttackingNow() && !npc.isInCombat() && !pc.isDead())
				{
					((L2Attackable) npc).attackCharacter(pc);
				}
			});
			startQuestTimer("Check400_3", 1500, npc, null);
		}
		/* 1я урна */
		// 1й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_beholder_1_spawn") && mgg_gc_beholder1 == null)
		{
			mgg_gc_beholder1 = addSpawn(mgg_gc_beholder, 217711, 109466, -1300, 0, false, 0);
			mgg_gc_beholder1.setIsNoRndWalk(true);
			mgg_gc_beholder1.setRunning();
			startQuestTimer("mgg_gc_beholder_1_move_1", 5000, mgg_gc_beholder1, null);
			startQuestTimer("Check400_1", 1500, mgg_gc_beholder1, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_1_move_1") && mgg_gc_beholder1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_1_move_1", 60000, mgg_gc_beholder1, null);
			}
			else
			{
				mgg_gc_beholder1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder1_2);
				mgg_gc_beholder1.setRunning();
				startQuestTimer("mgg_gc_beholder_1_move_2", 10000, mgg_gc_beholder1, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_1_move_2") && mgg_gc_beholder1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_1_move_2", 60000, mgg_gc_beholder1, null);
			}
			else
			{
				mgg_gc_beholder1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder1_3);
				mgg_gc_beholder1.setRunning();
				startQuestTimer("mgg_gc_beholder_1_move_3", 10000, mgg_gc_beholder1, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_1_move_3") && mgg_gc_beholder1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_1_move_3", 60000, mgg_gc_beholder1, null);
			}
			else
			{
				mgg_gc_beholder1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder1_4);
				mgg_gc_beholder1.setRunning();
				startQuestTimer("mgg_gc_beholder_1_move_4", 10000, mgg_gc_beholder1, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_1_move_4") && mgg_gc_beholder1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_1_move_4", 60000, mgg_gc_beholder1, null);
			}
			else
			{
				mgg_gc_beholder1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder1_3);
				mgg_gc_beholder1.setRunning();
				startQuestTimer("mgg_gc_beholder_1_move_5", 10000, mgg_gc_beholder1, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_1_move_5") && mgg_gc_beholder1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_1_move_5", 60000, mgg_gc_beholder1, null);
			}
			else
			{
				mgg_gc_beholder1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder1_2);
				mgg_gc_beholder1.setRunning();
				startQuestTimer("mgg_gc_beholder_1_move_6", 10000, mgg_gc_beholder1, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_1_move_6") && mgg_gc_beholder1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_1_move_6", 60000, mgg_gc_beholder1, null);
			}
			else
			{
				mgg_gc_beholder1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder1_1);
				mgg_gc_beholder1.setRunning();
				startQuestTimer("mgg_gc_beholder_1_move_1", 10000, mgg_gc_beholder1, null);
			}
		}
		// 2й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_beholder_2_spawn") && mgg_gc_beholder2 == null)
		{
			mgg_gc_beholder2 = addSpawn(mgg_gc_beholder, 216091, 111235, -1300, 0, false, 0);
			mgg_gc_beholder2.setIsNoRndWalk(true);
			mgg_gc_beholder2.setRunning();
			startQuestTimer("mgg_gc_beholder_2_move_1", 5000, mgg_gc_beholder2, null);
			startQuestTimer("Check400_1", 1500, mgg_gc_beholder2, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_2_move_1") && mgg_gc_beholder2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_2_move_1", 60000, mgg_gc_beholder2, null);
			}
			else
			{
				mgg_gc_beholder2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder2_2);
				mgg_gc_beholder2.setRunning();
				startQuestTimer("mgg_gc_beholder_2_move_2", 10000, mgg_gc_beholder2, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_2_move_2") && mgg_gc_beholder2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_2_move_2", 60000, mgg_gc_beholder2, null);
			}
			else
			{
				mgg_gc_beholder2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder2_3);
				mgg_gc_beholder2.setRunning();
				startQuestTimer("mgg_gc_beholder_2_move_3", 10000, mgg_gc_beholder2, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_2_move_3") && mgg_gc_beholder2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_2_move_3", 60000, mgg_gc_beholder2, null);
			}
			else
			{
				mgg_gc_beholder2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder2_4);
				mgg_gc_beholder2.setRunning();
				startQuestTimer("mgg_gc_beholder_2_move_4", 10000, mgg_gc_beholder2, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_2_move_4") && mgg_gc_beholder2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_2_move_4", 60000, mgg_gc_beholder2, null);
			}
			else
			{
				mgg_gc_beholder2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder2_3);
				mgg_gc_beholder2.setRunning();
				startQuestTimer("mgg_gc_beholder_2_move_5", 10000, mgg_gc_beholder2, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_2_move_5") && mgg_gc_beholder2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_2_move_5", 60000, mgg_gc_beholder2, null);
			}
			else
			{
				mgg_gc_beholder2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder2_2);
				mgg_gc_beholder2.setRunning();
				startQuestTimer("mgg_gc_beholder_2_move_6", 10000, mgg_gc_beholder2, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_2_move_6") && mgg_gc_beholder2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_2_move_6", 60000, mgg_gc_beholder2, null);
			}
			else
			{
				mgg_gc_beholder2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder2_1);
				mgg_gc_beholder2.setRunning();
				startQuestTimer("mgg_gc_beholder_2_move_1", 10000, mgg_gc_beholder2, null);
			}
		}
		// 3й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_beholder_3_spawn") && mgg_gc_beholder3 == null)
		{
			mgg_gc_beholder3 = addSpawn(mgg_gc_beholder, 217860, 112872, -1300, 0, false, 0);
			mgg_gc_beholder3.setIsNoRndWalk(true);
			mgg_gc_beholder3.setRunning();
			startQuestTimer("mgg_gc_beholder_3_move_1", 5000, mgg_gc_beholder3, null);
			startQuestTimer("Check400_1", 1500, mgg_gc_beholder3, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_3_move_1") && mgg_gc_beholder3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_3_move_1", 60000, mgg_gc_beholder3, null);
			}
			else
			{
				mgg_gc_beholder3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder3_2);
				mgg_gc_beholder3.setRunning();
				startQuestTimer("mgg_gc_beholder_3_move_2", 10000, mgg_gc_beholder3, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_3_move_2") && mgg_gc_beholder3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_3_move_2", 60000, mgg_gc_beholder3, null);
			}
			else
			{
				mgg_gc_beholder3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder3_3);
				mgg_gc_beholder3.setRunning();
				startQuestTimer("mgg_gc_beholder_3_move_3", 10000, mgg_gc_beholder3, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_3_move_3") && mgg_gc_beholder3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_3_move_3", 60000, mgg_gc_beholder3, null);
			}
			else
			{
				mgg_gc_beholder3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder3_4);
				mgg_gc_beholder3.setRunning();
				startQuestTimer("mgg_gc_beholder_3_move_4", 10000, mgg_gc_beholder3, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_3_move_4") && mgg_gc_beholder3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_3_move_4", 60000, mgg_gc_beholder3, null);
			}
			else
			{
				mgg_gc_beholder3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder3_3);
				mgg_gc_beholder3.setRunning();
				startQuestTimer("mgg_gc_beholder_3_move_5", 10000, mgg_gc_beholder3, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_3_move_5") && mgg_gc_beholder3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_3_move_5", 60000, mgg_gc_beholder3, null);
			}
			else
			{
				mgg_gc_beholder3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder3_2);
				mgg_gc_beholder3.setRunning();
				startQuestTimer("mgg_gc_beholder_3_move_6", 10000, mgg_gc_beholder3, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_3_move_6") && mgg_gc_beholder3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_3_move_6", 60000, mgg_gc_beholder3, null);
			}
			else
			{
				mgg_gc_beholder3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder3_1);
				mgg_gc_beholder3.setRunning();
				startQuestTimer("mgg_gc_beholder_3_move_1", 10000, mgg_gc_beholder3, null);
			}
		}
		// 4й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_beholder_4_spawn") && mgg_gc_beholder4 == null)
		{
			mgg_gc_beholder4 = addSpawn(mgg_gc_beholder, 219482, 111102, -1300, 0, false, 0);
			mgg_gc_beholder4.setIsNoRndWalk(true);
			mgg_gc_beholder4.setRunning();
			startQuestTimer("mgg_gc_beholder_4_move_1", 5000, mgg_gc_beholder4, null);
			startQuestTimer("Check400_1", 1500, mgg_gc_beholder4, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_4_move_1") && mgg_gc_beholder4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_4_move_1", 60000, mgg_gc_beholder4, null);
			}
			else
			{
				mgg_gc_beholder4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder4_2);
				mgg_gc_beholder4.setRunning();
				startQuestTimer("mgg_gc_beholder_4_move_2", 10000, mgg_gc_beholder4, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_4_move_2") && mgg_gc_beholder4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_4_move_2", 60000, mgg_gc_beholder4, null);
			}
			else
			{
				mgg_gc_beholder4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder4_3);
				mgg_gc_beholder4.setRunning();
				startQuestTimer("mgg_gc_beholder_4_move_3", 10000, mgg_gc_beholder4, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_4_move_3") && mgg_gc_beholder4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_4_move_3", 60000, mgg_gc_beholder4, null);
			}
			else
			{
				mgg_gc_beholder4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder4_4);
				mgg_gc_beholder4.setRunning();
				startQuestTimer("mgg_gc_beholder_4_move_4", 10000, mgg_gc_beholder4, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_4_move_4") && mgg_gc_beholder3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_4_move_4", 60000, mgg_gc_beholder4, null);
			}
			else
			{
				mgg_gc_beholder4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder4_3);
				mgg_gc_beholder4.setRunning();
				startQuestTimer("mgg_gc_beholder_4_move_5", 10000, mgg_gc_beholder4, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_4_move_5") && mgg_gc_beholder3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_4_move_5", 60000, mgg_gc_beholder4, null);
			}
			else
			{
				mgg_gc_beholder4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder4_2);
				mgg_gc_beholder4.setRunning();
				startQuestTimer("mgg_gc_beholder_4_move_6", 10000, mgg_gc_beholder4, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_4_move_6") && mgg_gc_beholder4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_4_move_6", 60000, mgg_gc_beholder4, null);
			}
			else
			{
				mgg_gc_beholder4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder4_1);
				mgg_gc_beholder4.setRunning();
				startQuestTimer("mgg_gc_beholder_4_move_1", 10000, mgg_gc_beholder4, null);
			}
		}
		/*******************************************/
		/* 2я урна */
		// 1й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_beholder_5_spawn") && mgg_gc_beholder5 == null)
		{
			mgg_gc_beholder5 = addSpawn(mgg_gc_beholder, 211294, 119296, -1300, 0, false, 0);
			mgg_gc_beholder5.setIsNoRndWalk(true);
			mgg_gc_beholder5.setRunning();
			startQuestTimer("mgg_gc_beholder_5_move_1", 5000, mgg_gc_beholder5, null);
			startQuestTimer("Check400_2", 1500, mgg_gc_beholder5, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_5_move_1") && mgg_gc_beholder5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_5_move_1", 60000, mgg_gc_beholder5, null);
			}
			else
			{
				mgg_gc_beholder5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder5_2);
				mgg_gc_beholder5.setRunning();
				startQuestTimer("mgg_gc_beholder_5_move_2", 10000, mgg_gc_beholder5, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_5_move_2") && mgg_gc_beholder5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_5_move_2", 60000, mgg_gc_beholder5, null);
			}
			else
			{
				mgg_gc_beholder5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder5_3);
				mgg_gc_beholder5.setRunning();
				startQuestTimer("mgg_gc_beholder_5_move_3", 10000, mgg_gc_beholder5, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_5_move_3") && mgg_gc_beholder5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_5_move_3", 60000, mgg_gc_beholder5, null);
			}
			else
			{
				mgg_gc_beholder5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder5_4);
				mgg_gc_beholder5.setRunning();
				startQuestTimer("mgg_gc_beholder_5_move_4", 10000, mgg_gc_beholder5, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_5_move_4") && mgg_gc_beholder5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_5_move_4", 60000, mgg_gc_beholder5, null);
			}
			else
			{
				mgg_gc_beholder5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder5_3);
				mgg_gc_beholder5.setRunning();
				startQuestTimer("mgg_gc_beholder_5_move_5", 10000, mgg_gc_beholder5, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_5_move_5") && mgg_gc_beholder5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_5_move_5", 60000, mgg_gc_beholder5, null);
			}
			else
			{
				mgg_gc_beholder5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder5_2);
				mgg_gc_beholder5.setRunning();
				startQuestTimer("mgg_gc_beholder_5_move_6", 10000, mgg_gc_beholder5, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_5_move_6") && mgg_gc_beholder5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_5_move_6", 60000, mgg_gc_beholder5, null);
			}
			else
			{
				mgg_gc_beholder5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder5_1);
				mgg_gc_beholder5.setRunning();
				startQuestTimer("mgg_gc_beholder_5_move_1", 10000, mgg_gc_beholder5, null);
			}
		}
		// 2й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_beholder_6_spawn") && mgg_gc_beholder6 == null)
		{
			mgg_gc_beholder6 = addSpawn(mgg_gc_beholder, 209540, 117657, -1300, 0, false, 0);
			mgg_gc_beholder6.setIsNoRndWalk(true);
			mgg_gc_beholder6.setRunning();
			startQuestTimer("mgg_gc_beholder_6_move_1", 5000, mgg_gc_beholder6, null);
			startQuestTimer("Check400_2", 1500, mgg_gc_beholder6, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_6_move_1") && mgg_gc_beholder6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_6_move_1", 60000, mgg_gc_beholder6, null);
			}
			else
			{
				mgg_gc_beholder6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder6_2);
				mgg_gc_beholder6.setRunning();
				startQuestTimer("mgg_gc_beholder_6_move_2", 10000, mgg_gc_beholder6, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_6_move_2") && mgg_gc_beholder6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_6_move_2", 60000, mgg_gc_beholder6, null);
			}
			else
			{
				mgg_gc_beholder6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder6_3);
				mgg_gc_beholder6.setRunning();
				startQuestTimer("mgg_gc_beholder_6_move_3", 10000, mgg_gc_beholder6, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_6_move_3") && mgg_gc_beholder6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_6_move_3", 60000, mgg_gc_beholder2, null);
			}
			else
			{
				mgg_gc_beholder6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder6_4);
				mgg_gc_beholder6.setRunning();
				startQuestTimer("mgg_gc_beholder_6_move_4", 10000, mgg_gc_beholder6, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_6_move_4") && mgg_gc_beholder6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_6_move_4", 60000, mgg_gc_beholder6, null);
			}
			else
			{
				mgg_gc_beholder6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder6_3);
				mgg_gc_beholder6.setRunning();
				startQuestTimer("mgg_gc_beholder_6_move_5", 10000, mgg_gc_beholder6, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_6_move_5") && mgg_gc_beholder6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_6_move_5", 60000, mgg_gc_beholder6, null);
			}
			else
			{
				mgg_gc_beholder6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder6_2);
				mgg_gc_beholder6.setRunning();
				startQuestTimer("mgg_gc_beholder_6_move_6", 10000, mgg_gc_beholder6, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_6_move_6") && mgg_gc_beholder6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_6_move_6", 60000, mgg_gc_beholder6, null);
			}
			else
			{
				mgg_gc_beholder6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder6_1);
				mgg_gc_beholder6.setRunning();
				startQuestTimer("mgg_gc_beholder_6_move_1", 10000, mgg_gc_beholder6, null);
			}
		}
		// 3й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_beholder_7_spawn") && mgg_gc_beholder7 == null)
		{
			mgg_gc_beholder7 = addSpawn(mgg_gc_beholder, 207900, 119423, -1300, 0, false, 0);
			mgg_gc_beholder7.setIsNoRndWalk(true);
			mgg_gc_beholder7.setRunning();
			startQuestTimer("mgg_gc_beholder_7_move_1", 5000, mgg_gc_beholder7, null);
			startQuestTimer("Check400_2", 1500, mgg_gc_beholder7, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_7_move_1") && mgg_gc_beholder7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_7_move_1", 60000, mgg_gc_beholder7, null);
			}
			else
			{
				mgg_gc_beholder7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder7_2);
				mgg_gc_beholder7.setRunning();
				startQuestTimer("mgg_gc_beholder_7_move_2", 10000, mgg_gc_beholder7, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_7_move_2") && mgg_gc_beholder7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_7_move_2", 60000, mgg_gc_beholder7, null);
			}
			else
			{
				mgg_gc_beholder7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder7_3);
				mgg_gc_beholder7.setRunning();
				startQuestTimer("mgg_gc_beholder_7_move_3", 10000, mgg_gc_beholder7, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_7_move_3") && mgg_gc_beholder7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_7_move_3", 60000, mgg_gc_beholder7, null);
			}
			else
			{
				mgg_gc_beholder7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder7_4);
				mgg_gc_beholder7.setRunning();
				startQuestTimer("mgg_gc_beholder_7_move_4", 10000, mgg_gc_beholder7, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_7_move_4") && mgg_gc_beholder7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_7_move_4", 60000, mgg_gc_beholder7, null);
			}
			else
			{
				mgg_gc_beholder7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder7_3);
				mgg_gc_beholder7.setRunning();
				startQuestTimer("mgg_gc_beholder_7_move_5", 10000, mgg_gc_beholder7, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_7_move_5") && mgg_gc_beholder7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_7_move_5", 60000, mgg_gc_beholder7, null);
			}
			else
			{
				mgg_gc_beholder7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder7_2);
				mgg_gc_beholder7.setRunning();
				startQuestTimer("mgg_gc_beholder_7_move_6", 10000, mgg_gc_beholder7, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_7_move_6") && mgg_gc_beholder7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_7_move_6", 60000, mgg_gc_beholder7, null);
			}
			else
			{
				mgg_gc_beholder7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder7_1);
				mgg_gc_beholder7.setRunning();
				startQuestTimer("mgg_gc_beholder_7_move_1", 10000, mgg_gc_beholder7, null);
			}
		}
		// 4й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_beholder_8_spawn") && mgg_gc_beholder8 == null)
		{
			mgg_gc_beholder8 = addSpawn(mgg_gc_beholder, 209666, 121058, -1300, 0, false, 0);
			mgg_gc_beholder8.setIsNoRndWalk(true);
			mgg_gc_beholder8.setRunning();
			startQuestTimer("mgg_gc_beholder_8_move_1", 5000, mgg_gc_beholder8, null);
			startQuestTimer("Check400_2", 1500, mgg_gc_beholder8, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_8_move_1") && mgg_gc_beholder8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_8_move_1", 60000, mgg_gc_beholder8, null);
			}
			else
			{
				mgg_gc_beholder8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder8_2);
				mgg_gc_beholder8.setRunning();
				startQuestTimer("mgg_gc_beholder_8_move_2", 10000, mgg_gc_beholder8, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_8_move_2") && mgg_gc_beholder8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_8_move_2", 60000, mgg_gc_beholder8, null);
			}
			else
			{
				mgg_gc_beholder8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder8_3);
				mgg_gc_beholder8.setRunning();
				startQuestTimer("mgg_gc_beholder_8_move_3", 10000, mgg_gc_beholder8, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_8_move_3") && mgg_gc_beholder8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_8_move_3", 60000, mgg_gc_beholder8, null);
			}
			else
			{
				mgg_gc_beholder8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder8_4);
				mgg_gc_beholder8.setRunning();
				startQuestTimer("mgg_gc_beholder_8_move_4", 10000, mgg_gc_beholder8, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_8_move_4") && mgg_gc_beholder8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_8_move_4", 60000, mgg_gc_beholder8, null);
			}
			else
			{
				mgg_gc_beholder8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder8_3);
				mgg_gc_beholder8.setRunning();
				startQuestTimer("mgg_gc_beholder_8_move_5", 10000, mgg_gc_beholder8, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_8_move_5") && mgg_gc_beholder8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_8_move_5", 60000, mgg_gc_beholder8, null);
			}
			else
			{
				mgg_gc_beholder8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder8_2);
				mgg_gc_beholder8.setRunning();
				startQuestTimer("mgg_gc_beholder_8_move_6", 10000, mgg_gc_beholder8, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_8_move_6") && mgg_gc_beholder8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_8_move_6", 60000, mgg_gc_beholder8, null);
			}
			else
			{
				mgg_gc_beholder8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder8_1);
				mgg_gc_beholder8.setRunning();
				startQuestTimer("mgg_gc_beholder_8_move_1", 10000, mgg_gc_beholder8, null);
			}
		}
		/*******************************************/
		/* 3я урна */
		// 1й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_beholder_9_spawn") && mgg_gc_beholder9 == null)
		{
			mgg_gc_beholder9 = addSpawn(mgg_gc_beholder, 212861, 117267, -896, 0, false, 0);
			mgg_gc_beholder9.setIsNoRndWalk(true);
			mgg_gc_beholder9.setRunning();
			startQuestTimer("mgg_gc_beholder_9_move_1", 5000, mgg_gc_beholder9, null);
			startQuestTimer("Check400_3", 1500, mgg_gc_beholder9, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_9_move_1") && mgg_gc_beholder9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_9_move_1", 60000, mgg_gc_beholder9, null);
			}
			else
			{
				mgg_gc_beholder9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder9_2);
				mgg_gc_beholder9.setRunning();
				startQuestTimer("mgg_gc_beholder_9_move_2", 10000, mgg_gc_beholder9, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_9_move_2") && mgg_gc_beholder9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_9_move_2", 60000, mgg_gc_beholder9, null);
			}
			else
			{
				mgg_gc_beholder9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder9_3);
				mgg_gc_beholder9.setRunning();
				startQuestTimer("mgg_gc_beholder_9_move_3", 10000, mgg_gc_beholder9, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_9_move_3") && mgg_gc_beholder9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_9_move_3", 60000, mgg_gc_beholder9, null);
			}
			else
			{
				mgg_gc_beholder9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder9_2);
				mgg_gc_beholder9.setRunning();
				startQuestTimer("mgg_gc_beholder_9_move_4", 10000, mgg_gc_beholder9, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_9_move_4") && mgg_gc_beholder9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_9_move_4", 60000, mgg_gc_beholder9, null);
			}
			else
			{
				mgg_gc_beholder9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder9_1);
				mgg_gc_beholder9.setRunning();
				startQuestTimer("mgg_gc_beholder_9_move_1", 10000, mgg_gc_beholder9, null);
			}
		}
		// 2й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_beholder_10_spawn") && mgg_gc_beholder10 == null)
		{
			mgg_gc_beholder10 = addSpawn(mgg_gc_beholder, 215693, 116089, -896, 0, false, 0);
			mgg_gc_beholder10.setIsNoRndWalk(true);
			mgg_gc_beholder10.setRunning();
			startQuestTimer("mgg_gc_beholder_10_move_1", 5000, mgg_gc_beholder10, null);
			startQuestTimer("Check400_3", 1500, mgg_gc_beholder10, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_10_move_1") && mgg_gc_beholder10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_10_move_1", 60000, mgg_gc_beholder10, null);
			}
			else
			{
				mgg_gc_beholder10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder10_2);
				mgg_gc_beholder10.setRunning();
				startQuestTimer("mgg_gc_beholder_10_move_2", 10000, mgg_gc_beholder10, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_10_move_2") && mgg_gc_beholder10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_10_move_2", 60000, mgg_gc_beholder10, null);
			}
			else
			{
				mgg_gc_beholder10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder10_3);
				mgg_gc_beholder10.setRunning();
				startQuestTimer("mgg_gc_beholder_10_move_3", 10000, mgg_gc_beholder10, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_10_move_3") && mgg_gc_beholder10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_10_move_3", 60000, mgg_gc_beholder10, null);
			}
			else
			{
				mgg_gc_beholder10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder10_2);
				mgg_gc_beholder10.setRunning();
				startQuestTimer("mgg_gc_beholder_10_move_4", 10000, mgg_gc_beholder10, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_10_move_4") && mgg_gc_beholder10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_10_move_4", 60000, mgg_gc_beholder10, null);
			}
			else
			{
				mgg_gc_beholder10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder10_1);
				mgg_gc_beholder10.setRunning();
				startQuestTimer("mgg_gc_beholder_10_move_1", 10000, mgg_gc_beholder10, null);
			}
		}
		// 3й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_beholder_11_spawn") && mgg_gc_beholder11 == null)
		{
			mgg_gc_beholder11 = addSpawn(mgg_gc_beholder, 214527, 113264, -896, 0, false, 0);
			mgg_gc_beholder11.setIsNoRndWalk(true);
			mgg_gc_beholder11.setRunning();
			startQuestTimer("mgg_gc_beholder_11_move_1", 5000, mgg_gc_beholder11, null);
			startQuestTimer("Check400_3", 1500, mgg_gc_beholder11, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_11_move_1") && mgg_gc_beholder7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_11_move_1", 60000, mgg_gc_beholder7, null);
			}
			else
			{
				if(mgg_gc_beholder11 != null)
				{
					mgg_gc_beholder11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder11_2);
					mgg_gc_beholder11.setRunning();
					startQuestTimer("mgg_gc_beholder_11_move_2", 10000, mgg_gc_beholder11, null);
				}
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_11_move_2") && mgg_gc_beholder11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_11_move_2", 60000, mgg_gc_beholder11, null);
			}
			else
			{
				mgg_gc_beholder11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder11_3);
				mgg_gc_beholder11.setRunning();
				startQuestTimer("mgg_gc_beholder_11_move_3", 10000, mgg_gc_beholder11, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_11_move_3") && mgg_gc_beholder11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_11_move_3", 60000, mgg_gc_beholder11, null);
			}
			else
			{
				mgg_gc_beholder11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder11_2);
				mgg_gc_beholder11.setRunning();
				startQuestTimer("mgg_gc_beholder_11_move_4", 10000, mgg_gc_beholder11, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_11_move_4") && mgg_gc_beholder11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_11_move_4", 60000, mgg_gc_beholder11, null);
			}
			else
			{
				mgg_gc_beholder11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder11_1);
				mgg_gc_beholder11.setRunning();
				startQuestTimer("mgg_gc_beholder_11_move_1", 10000, mgg_gc_beholder11, null);
			}
		}
		// 4й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_beholder_12_spawn") && mgg_gc_beholder12 == null)
		{
			mgg_gc_beholder12 = addSpawn(mgg_gc_beholder, 211697, 114428, -896, 0, false, 0);
			mgg_gc_beholder12.setIsNoRndWalk(true);
			mgg_gc_beholder12.setRunning();
			startQuestTimer("mgg_gc_beholder_12_move_1", 5000, mgg_gc_beholder12, null);
			startQuestTimer("Check400_3", 1500, mgg_gc_beholder12, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_12_move_1") && mgg_gc_beholder12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_12_move_1", 60000, mgg_gc_beholder12, null);
			}
			else
			{
				mgg_gc_beholder12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder12_2);
				mgg_gc_beholder12.setRunning();
				startQuestTimer("mgg_gc_beholder_12_move_2", 10000, mgg_gc_beholder12, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_12_move_2") && mgg_gc_beholder12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_12_move_2", 60000, mgg_gc_beholder12, null);
			}
			else
			{
				mgg_gc_beholder12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder12_3);
				mgg_gc_beholder12.setRunning();
				startQuestTimer("mgg_gc_beholder_12_move_3", 10000, mgg_gc_beholder12, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_12_move_3") && mgg_gc_beholder12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_12_move_3", 60000, mgg_gc_beholder12, null);
			}
			else
			{
				mgg_gc_beholder12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder12_2);
				mgg_gc_beholder12.setRunning();
				startQuestTimer("mgg_gc_beholder_12_move_4", 10000, mgg_gc_beholder12, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_beholder_12_move_4") && mgg_gc_beholder12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_beholder_12_move_4", 60000, mgg_gc_beholder12, null);
			}
			else
			{
				mgg_gc_beholder12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_beholder12_1);
				mgg_gc_beholder12.setRunning();
				startQuestTimer("mgg_gc_beholder_12_move_1", 10000, mgg_gc_beholder12, null);
			}
		}
		/*******************************************/
		// 1й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_lookout_1_spawn") && mgg_gc_lookout1 == null)
		{
			mgg_gc_lookout1 = addSpawn(mgg_gc_lookout, 217711, 109466, -1300, 0, false, 0);
			mgg_gc_lookout1.setIsNoRndWalk(true);
			mgg_gc_lookout1.setRunning();
			startQuestTimer("mgg_gc_lookout_1_move_1", 5000, mgg_gc_lookout1, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_1_move_1") && mgg_gc_lookout1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_1_move_1", 60000, mgg_gc_lookout1, null);
			}
			else
			{
				mgg_gc_lookout1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout1_2);
				mgg_gc_lookout1.setRunning();
				startQuestTimer("mgg_gc_lookout_1_move_2", 10000, mgg_gc_lookout1, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_1_move_2") && mgg_gc_lookout1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_1_move_2", 60000, mgg_gc_lookout1, null);
			}
			else
			{
				mgg_gc_lookout1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout1_3);
				mgg_gc_lookout1.setRunning();
				startQuestTimer("mgg_gc_lookout_1_move_3", 10000, mgg_gc_lookout1, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_1_move_3") && mgg_gc_lookout1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_1_move_3", 60000, mgg_gc_lookout1, null);
			}
			else
			{
				mgg_gc_lookout1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout1_4);
				mgg_gc_lookout1.setRunning();
				startQuestTimer("mgg_gc_lookout_1_move_4", 10000, mgg_gc_lookout1, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_1_move_4") && mgg_gc_lookout1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_1_move_4", 60000, mgg_gc_lookout1, null);
			}
			else
			{
				mgg_gc_lookout1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout1_3);
				mgg_gc_lookout1.setRunning();
				startQuestTimer("mgg_gc_lookout_1_move_5", 10000, mgg_gc_lookout1, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_1_move_5") && mgg_gc_lookout1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_1_move_5", 60000, mgg_gc_lookout1, null);
			}
			else
			{
				mgg_gc_lookout1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout1_2);
				mgg_gc_lookout1.setRunning();
				startQuestTimer("mgg_gc_lookout_1_move_6", 10000, mgg_gc_lookout1, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_1_move_6") && mgg_gc_lookout1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_1_move_6", 60000, mgg_gc_lookout1, null);
			}
			else
			{
				mgg_gc_lookout1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout1_1);
				mgg_gc_lookout1.setRunning();
				startQuestTimer("mgg_gc_lookout_1_move_1", 10000, mgg_gc_lookout1, null);
			}
		}
		// 2й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_lookout_2_spawn") && mgg_gc_lookout2 == null)
		{
			mgg_gc_lookout2 = addSpawn(mgg_gc_lookout, 217711, 109466, -1300, 0, false, 0);
			mgg_gc_lookout2.setIsNoRndWalk(true);
			mgg_gc_lookout2.setRunning();
			startQuestTimer("mgg_gc_lookout_2_move_1", 5000, mgg_gc_lookout2, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_2_move_1") && mgg_gc_lookout2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_2_move_1", 60000, mgg_gc_lookout2, null);
			}
			else
			{
				mgg_gc_lookout2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout2_2);
				mgg_gc_lookout2.setRunning();
				startQuestTimer("mgg_gc_lookout_2_move_2", 10000, mgg_gc_lookout2, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_2_move_2") && mgg_gc_lookout2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_2_move_2", 60000, mgg_gc_lookout2, null);
			}
			else
			{
				mgg_gc_lookout2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout2_3);
				mgg_gc_lookout2.setRunning();
				startQuestTimer("mgg_gc_lookout_2_move_3", 10000, mgg_gc_lookout2, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_2_move_3") && mgg_gc_lookout2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_2_move_3", 60000, mgg_gc_lookout2, null);
			}
			else
			{
				mgg_gc_lookout2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout2_4);
				mgg_gc_lookout2.setRunning();
				startQuestTimer("mgg_gc_lookout_2_move_4", 10000, mgg_gc_lookout2, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_2_move_4") && mgg_gc_lookout2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_2_move_4", 60000, mgg_gc_lookout2, null);
			}
			else
			{
				mgg_gc_lookout2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout2_3);
				mgg_gc_lookout2.setRunning();
				startQuestTimer("mgg_gc_lookout_2_move_5", 10000, mgg_gc_lookout2, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_2_move_5") && mgg_gc_lookout2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_2_move_5", 60000, mgg_gc_lookout2, null);
			}
			else
			{
				mgg_gc_lookout2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout2_2);
				mgg_gc_lookout2.setRunning();
				startQuestTimer("mgg_gc_lookout_2_move_6", 10000, mgg_gc_lookout2, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_2_move_6") && mgg_gc_lookout2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_2_move_6", 60000, mgg_gc_lookout2, null);
			}
			else
			{
				mgg_gc_lookout2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout2_1);
				mgg_gc_lookout2.setRunning();
				startQuestTimer("mgg_gc_lookout_2_move_1", 10000, mgg_gc_lookout2, null);
			}
		}
		// 3й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_lookout_3_spawn") && mgg_gc_lookout3 == null)
		{
			mgg_gc_lookout3 = addSpawn(mgg_gc_lookout, 217711, 109466, -1300, 0, false, 0);
			mgg_gc_lookout3.setIsNoRndWalk(true);
			mgg_gc_lookout3.setRunning();
			startQuestTimer("mgg_gc_lookout_3_move_1", 5000, mgg_gc_lookout3, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_3_move_1") && mgg_gc_lookout3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_3_move_1", 60000, mgg_gc_lookout3, null);
			}
			else
			{
				mgg_gc_lookout3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout3_2);
				mgg_gc_lookout3.setRunning();
				startQuestTimer("mgg_gc_lookout_3_move_2", 10000, mgg_gc_lookout3, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_3_move_2") && mgg_gc_lookout3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_3_move_2", 60000, mgg_gc_lookout3, null);
			}
			else
			{
				mgg_gc_lookout3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout3_3);
				mgg_gc_lookout3.setRunning();
				startQuestTimer("mgg_gc_lookout_3_move_3", 10000, mgg_gc_lookout3, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_3_move_3") && mgg_gc_lookout3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_3_move_3", 60000, mgg_gc_lookout3, null);
			}
			else
			{
				mgg_gc_lookout3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout3_4);
				mgg_gc_lookout3.setRunning();
				startQuestTimer("mgg_gc_lookout_3_move_4", 10000, mgg_gc_lookout3, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_3_move_4") && mgg_gc_lookout3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_3_move_4", 60000, mgg_gc_lookout3, null);
			}
			else
			{
				mgg_gc_lookout3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout3_3);
				mgg_gc_lookout3.setRunning();
				startQuestTimer("mgg_gc_lookout_3_move_5", 10000, mgg_gc_lookout3, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_3_move_5") && mgg_gc_lookout3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_3_move_5", 60000, mgg_gc_lookout3, null);
			}
			else
			{
				mgg_gc_lookout3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout3_2);
				mgg_gc_lookout3.setRunning();
				startQuestTimer("mgg_gc_lookout_3_move_6", 10000, mgg_gc_lookout3, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_3_move_6") && mgg_gc_lookout3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_3_move_6", 60000, mgg_gc_lookout3, null);
			}
			else
			{
				mgg_gc_lookout3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout3_1);
				mgg_gc_lookout3.setRunning();
				startQuestTimer("mgg_gc_lookout_3_move_1", 10000, mgg_gc_lookout3, null);
			}
		}
		// 4й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_lookout_4_spawn") && mgg_gc_lookout4 == null)
		{
			mgg_gc_lookout4 = addSpawn(mgg_gc_lookout, 217711, 109466, -1300, 0, false, 0);
			mgg_gc_lookout4.setIsNoRndWalk(true);
			mgg_gc_lookout4.setRunning();
			startQuestTimer("mgg_gc_lookout_4_move_1", 5000, mgg_gc_lookout4, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_4_move_1") && mgg_gc_lookout4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_4_move_1", 60000, mgg_gc_lookout4, null);
			}
			else
			{
				mgg_gc_lookout4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout4_2);
				mgg_gc_lookout4.setRunning();
				startQuestTimer("mgg_gc_lookout_4_move_2", 10000, mgg_gc_lookout4, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_4_move_2") && mgg_gc_lookout4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_4_move_2", 60000, mgg_gc_lookout4, null);
			}
			else
			{
				mgg_gc_lookout4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout4_3);
				mgg_gc_lookout4.setRunning();
				startQuestTimer("mgg_gc_lookout_4_move_3", 10000, mgg_gc_lookout4, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_4_move_3") && mgg_gc_lookout4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_4_move_3", 60000, mgg_gc_lookout4, null);
			}
			else
			{
				mgg_gc_lookout4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout4_4);
				mgg_gc_lookout4.setRunning();
				startQuestTimer("mgg_gc_lookout_4_move_4", 10000, mgg_gc_lookout4, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_4_move_4") && mgg_gc_lookout4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_4_move_4", 60000, mgg_gc_lookout4, null);
			}
			else
			{
				mgg_gc_lookout4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout4_3);
				mgg_gc_lookout4.setRunning();
				startQuestTimer("mgg_gc_lookout_4_move_5", 10000, mgg_gc_lookout4, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_4_move_5") && mgg_gc_lookout4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_4_move_5", 60000, mgg_gc_lookout4, null);
			}
			else
			{
				mgg_gc_lookout4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout4_2);
				mgg_gc_lookout4.setRunning();
				startQuestTimer("mgg_gc_lookout_4_move_6", 10000, mgg_gc_lookout4, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_4_move_6") && mgg_gc_lookout4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_4_move_6", 60000, mgg_gc_lookout4, null);
			}
			else
			{
				mgg_gc_lookout4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout4_1);
				mgg_gc_lookout4.setRunning();
				startQuestTimer("mgg_gc_lookout_4_move_1", 10000, mgg_gc_lookout4, null);
			}
		}
		/*******************************************/
		// Внешняя дорожка
		// 1й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_lookout_5_spawn") && mgg_gc_lookout5 == null)
		{
			mgg_gc_lookout5 = addSpawn(mgg_gc_lookout, 216910, 118367, -1735, 0, false, 0);
			mgg_gc_lookout5.setIsNoRndWalk(true);
			mgg_gc_lookout5.setRunning();
			startQuestTimer("mgg_gc_lookout_5_move_1", 5000, mgg_gc_lookout5, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_5_move_1") && mgg_gc_lookout5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_5_move_1", 60000, mgg_gc_lookout5, null);
			}
			else
			{
				mgg_gc_lookout5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout5_2);
				mgg_gc_lookout5.setRunning();
				startQuestTimer("mgg_gc_lookout_5_move_2", 10000, mgg_gc_lookout5, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_5_move_2") && mgg_gc_lookout5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_5_move_2", 60000, mgg_gc_lookout5, null);
			}
			else
			{
				mgg_gc_lookout5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout5_3);
				mgg_gc_lookout5.setRunning();
				startQuestTimer("mgg_gc_lookout_5_move_3", 10000, mgg_gc_lookout5, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_5_move_3") && mgg_gc_lookout5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_5_move_3", 60000, mgg_gc_lookout5, null);
			}
			else
			{
				mgg_gc_lookout5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout5_4);
				mgg_gc_lookout5.setRunning();
				startQuestTimer("mgg_gc_lookout_5_move_4", 10000, mgg_gc_lookout5, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_5_move_4") && mgg_gc_lookout5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_5_move_4", 60000, mgg_gc_lookout5, null);
			}
			else
			{
				mgg_gc_lookout5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout5_3);
				mgg_gc_lookout5.setRunning();
				startQuestTimer("mgg_gc_lookout_5_move_5", 10000, mgg_gc_lookout5, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_5_move_5") && mgg_gc_lookout5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_5_move_5", 60000, mgg_gc_lookout5, null);
			}
			else
			{
				mgg_gc_lookout5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout5_2);
				mgg_gc_lookout5.setRunning();
				startQuestTimer("mgg_gc_lookout_5_move_6", 10000, mgg_gc_lookout5, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_5_move_6") && mgg_gc_lookout5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_5_move_6", 60000, mgg_gc_lookout5, null);
			}
			else
			{
				mgg_gc_lookout5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout5_1);
				mgg_gc_lookout5.setRunning();
				startQuestTimer("mgg_gc_lookout_5_move_1", 10000, mgg_gc_lookout5, null);
			}
		}
		// 2й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_lookout_6_spawn") && mgg_gc_lookout6 == null)
		{
			mgg_gc_lookout2 = addSpawn(mgg_gc_lookout, 218797, 118469, -1735, 0, false, 0);
			mgg_gc_lookout2.setIsNoRndWalk(true);
			mgg_gc_lookout2.setRunning();
			startQuestTimer("mgg_gc_lookout_6_move_1", 5000, mgg_gc_lookout6, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_6_move_1") && mgg_gc_lookout6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_6_move_1", 60000, mgg_gc_lookout6, null);
			}
			else
			{
				mgg_gc_lookout6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout6_2);
				mgg_gc_lookout6.setRunning();
				startQuestTimer("mgg_gc_lookout_6_move_2", 10000, mgg_gc_lookout6, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_6_move_2") && mgg_gc_lookout6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_6_move_2", 60000, mgg_gc_lookout6, null);
			}
			else
			{
				mgg_gc_lookout6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout6_3);
				mgg_gc_lookout6.setRunning();
				startQuestTimer("mgg_gc_lookout_6_move_3", 10000, mgg_gc_lookout6, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_6_move_3") && mgg_gc_lookout6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_6_move_3", 60000, mgg_gc_lookout6, null);
			}
			else
			{
				mgg_gc_lookout6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout6_4);
				mgg_gc_lookout6.setRunning();
				startQuestTimer("mgg_gc_lookout_6_move_4", 10000, mgg_gc_lookout6, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_6_move_4") && mgg_gc_lookout6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_6_move_4", 60000, mgg_gc_lookout6, null);
			}
			else
			{
				mgg_gc_lookout6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout6_3);
				mgg_gc_lookout6.setRunning();
				startQuestTimer("mgg_gc_lookout_6_move_5", 10000, mgg_gc_lookout6, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_6_move_5") && mgg_gc_lookout6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_6_move_5", 60000, mgg_gc_lookout6, null);
			}
			else
			{
				mgg_gc_lookout6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout6_2);
				mgg_gc_lookout6.setRunning();
				startQuestTimer("mgg_gc_lookout_6_move_6", 10000, mgg_gc_lookout6, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_6_move_6") && mgg_gc_lookout6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_6_move_6", 60000, mgg_gc_lookout6, null);
			}
			else
			{
				mgg_gc_lookout6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout6_1);
				mgg_gc_lookout6.setRunning();
				startQuestTimer("mgg_gc_lookout_6_move_1", 10000, mgg_gc_lookout6, null);
			}
		}
		// 3й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_lookout_7_spawn") && mgg_gc_lookout7 == null)
		{
			mgg_gc_lookout7 = addSpawn(mgg_gc_lookout, 218715, 120364, -1735, 0, false, 0);
			mgg_gc_lookout7.setIsNoRndWalk(true);
			mgg_gc_lookout7.setRunning();
			startQuestTimer("mgg_gc_lookout_7_move_1", 5000, mgg_gc_lookout7, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_7_move_1") && mgg_gc_lookout7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_7_move_1", 60000, mgg_gc_lookout7, null);
			}
			else
			{
				mgg_gc_lookout7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout7_2);
				mgg_gc_lookout7.setRunning();
				startQuestTimer("mgg_gc_lookout_7_move_2", 10000, mgg_gc_lookout7, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_7_move_2") && mgg_gc_lookout7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_7_move_2", 60000, mgg_gc_lookout3, null);
			}
			else
			{
				mgg_gc_lookout7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout7_3);
				mgg_gc_lookout7.setRunning();
				startQuestTimer("mgg_gc_lookout_7_move_3", 10000, mgg_gc_lookout7, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_7_move_3") && mgg_gc_lookout7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_7_move_3", 60000, mgg_gc_lookout7, null);
			}
			else
			{
				mgg_gc_lookout7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout7_4);
				mgg_gc_lookout7.setRunning();
				startQuestTimer("mgg_gc_lookout_7_move_4", 10000, mgg_gc_lookout7, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_7_move_4") && mgg_gc_lookout7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_7_move_4", 60000, mgg_gc_lookout7, null);
			}
			else
			{
				mgg_gc_lookout7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout7_3);
				mgg_gc_lookout7.setRunning();
				startQuestTimer("mgg_gc_lookout_7_move_5", 10000, mgg_gc_lookout7, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_7_move_5") && mgg_gc_lookout7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_7_move_5", 60000, mgg_gc_lookout7, null);
			}
			else
			{
				mgg_gc_lookout7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout7_2);
				mgg_gc_lookout7.setRunning();
				startQuestTimer("mgg_gc_lookout_7_move_6", 10000, mgg_gc_lookout7, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_7_move_6") && mgg_gc_lookout7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_7_move_6", 60000, mgg_gc_lookout7, null);
			}
			else
			{
				mgg_gc_lookout7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout7_1);
				mgg_gc_lookout7.setRunning();
				startQuestTimer("mgg_gc_lookout_7_move_1", 10000, mgg_gc_lookout7, null);
			}
		}
		// 4й Смотритель Сада
		else if(event.equalsIgnoreCase("mgg_gc_lookout_8_spawn") && mgg_gc_lookout8 == null)
		{
			mgg_gc_lookout8 = addSpawn(mgg_gc_lookout, 216768, 120364, -1735, 0, false, 0);
			mgg_gc_lookout8.setIsNoRndWalk(true);
			mgg_gc_lookout8.setRunning();
			startQuestTimer("mgg_gc_lookout_8_move_1", 5000, mgg_gc_lookout8, null);
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_8_move_1") && mgg_gc_lookout8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_8_move_1", 60000, mgg_gc_lookout8, null);
			}
			else
			{
				mgg_gc_lookout8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout8_2);
				mgg_gc_lookout8.setRunning();
				startQuestTimer("mgg_gc_lookout_8_move_2", 10000, mgg_gc_lookout8, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_8_move_2") && mgg_gc_lookout8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_8_move_2", 60000, mgg_gc_lookout8, null);
			}
			else
			{
				mgg_gc_lookout8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout8_3);
				mgg_gc_lookout8.setRunning();
				startQuestTimer("mgg_gc_lookout_8_move_3", 10000, mgg_gc_lookout8, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_8_move_3") && mgg_gc_lookout8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_8_move_3", 60000, mgg_gc_lookout8, null);
			}
			else
			{
				mgg_gc_lookout8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout8_4);
				mgg_gc_lookout8.setRunning();
				startQuestTimer("mgg_gc_lookout_8_move_4", 10000, mgg_gc_lookout8, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_8_move_4") && mgg_gc_lookout8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_8_move_4", 60000, mgg_gc_lookout8, null);
			}
			else
			{
				mgg_gc_lookout8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout8_3);
				mgg_gc_lookout8.setRunning();
				startQuestTimer("mgg_gc_lookout_8_move_5", 10000, mgg_gc_lookout8, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_8_move_5") && mgg_gc_lookout8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_8_move_5", 60000, mgg_gc_lookout8, null);
			}
			else
			{
				mgg_gc_lookout8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout8_2);
				mgg_gc_lookout8.setRunning();
				startQuestTimer("mgg_gc_lookout_8_move_6", 10000, mgg_gc_lookout8, null);
			}
		}
		else if(event.equalsIgnoreCase("mgg_gc_lookout_8_move_6") && mgg_gc_lookout8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mgg_gc_lookout_8_move_6", 60000, mgg_gc_lookout8, null);
			}
			else
			{
				mgg_gc_lookout8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mgg_gc_lookout8_1);
				mgg_gc_lookout8.setRunning();
				startQuestTimer("mgg_gc_lookout_8_move_1", 10000, mgg_gc_lookout8, null);
			}
		}
		/*******************************************/
		/*
		else if (npc.getNpcId() == test) // (GameTimeController.getInstance().isNight())
		{
			npc.setIsInSocialAction(true);
		}
		*/

		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(npc == null)
		{
			return super.onKill(npc, killer, isPet);
		}

		if(npc.getNpcId() == mgg_gc_beholder)
		{
			if(npc.equals(mgg_gc_beholder1))
			{
				startQuestTimer("mgg_gc_beholder_1_spawn", 120000, null, null);
				mgg_gc_beholder1 = null;
			}
			else if(npc.equals(mgg_gc_beholder2))
			{
				startQuestTimer("mgg_gc_beholder_2_spawn", 120000, null, null);
				mgg_gc_beholder2 = null;
			}
			else if(npc.equals(mgg_gc_beholder3))
			{
				startQuestTimer("mgg_gc_beholder_3_spawn", 120000, null, null);
				mgg_gc_beholder3 = null;
			}
			else if(npc.equals(mgg_gc_beholder4))
			{
				startQuestTimer("mgg_gc_beholder_4_spawn", 120000, null, null);
				mgg_gc_beholder4 = null;
			}
			else if(npc.equals(mgg_gc_beholder5))
			{
				startQuestTimer("mgg_gc_beholder_5_spawn", 120000, null, null);
				mgg_gc_beholder5 = null;
			}
			else if(npc.equals(mgg_gc_beholder6))
			{
				startQuestTimer("mgg_gc_beholder_6_spawn", 120000, null, null);
				mgg_gc_beholder6 = null;
			}
			else if(npc.equals(mgg_gc_beholder7))
			{
				startQuestTimer("mgg_gc_beholder_7_spawn", 120000, null, null);
				mgg_gc_beholder7 = null;
			}
			else if(npc.equals(mgg_gc_beholder8))
			{
				startQuestTimer("mgg_gc_beholder_8_spawn", 120000, null, null);
				mgg_gc_beholder8 = null;
			}
			else if(npc.equals(mgg_gc_beholder9))
			{
				startQuestTimer("mgg_gc_beholder_9_spawn", 120000, null, null);
				mgg_gc_beholder9 = null;
			}
			else if(npc.equals(mgg_gc_beholder10))
			{
				startQuestTimer("mgg_gc_beholder_10_spawn", 120000, null, null);
				mgg_gc_beholder10 = null;
			}
			else if(npc.equals(mgg_gc_beholder11))
			{
				startQuestTimer("mgg_gc_beholder_11_spawn", 120000, null, null);
				mgg_gc_beholder11 = null;
			}
			else if(npc.equals(mgg_gc_beholder12))
			{
				startQuestTimer("mgg_gc_beholder_12_spawn", 120000, null, null);
				mgg_gc_beholder12 = null;
			}
		}
		else if(npc.getNpcId() == mgg_gc_lookout)
		{
			if(npc.equals(mgg_gc_lookout1))
			{
				startQuestTimer("mgg_gc_lookout_1_spawn", 120000, null, null);
				mgg_gc_lookout1 = null;
			}
			else if(npc.equals(mgg_gc_lookout2))
			{
				startQuestTimer("mgg_gc_lookout_2_spawn", 120000, null, null);
				mgg_gc_lookout2 = null;
			}
			else if(npc.equals(mgg_gc_lookout3))
			{
				startQuestTimer("mgg_gc_lookout_3_spawn", 120000, null, null);
				mgg_gc_lookout3 = null;
			}
			else if(npc.equals(mgg_gc_lookout4))
			{
				startQuestTimer("mgg_gc_lookout_4_spawn", 120000, null, null);
				mgg_gc_lookout4 = null;
			}
			else if(npc.equals(mgg_gc_lookout5))
			{
				startQuestTimer("mgg_gc_lookout_5_spawn", 120000, null, null);
				mgg_gc_lookout5 = null;
			}
			else if(npc.equals(mgg_gc_lookout6))
			{
				startQuestTimer("mgg_gc_lookout_6_spawn", 120000, null, null);
				mgg_gc_lookout6 = null;
			}
			else if(npc.equals(mgg_gc_lookout7))
			{
				startQuestTimer("mgg_gc_lookout_7_spawn", 120000, null, null);
				mgg_gc_lookout7 = null;
			}
			else if(npc.equals(mgg_gc_lookout8))
			{
				startQuestTimer("mgg_gc_lookout_8_spawn", 120000, null, null);
				mgg_gc_lookout8 = null;
			}
		}

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(!npc.isCastingNow() && !npc.isAttackingNow() && !npc.isInCombat() && !player.isDead())
		{
			((L2Attackable) npc).attackCharacter(player);
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance)
		{
			if(zone.getId() == fountain_1_zone)
			{
				PlayersInZone1.add((L2PcInstance) character);
			}
			else if(zone.getId() == fountain_2_zone)
			{
				PlayersInZone2.add((L2PcInstance) character);
			}
			else if(zone.getId() == fountain_3_zone)
			{
				PlayersInZone3.add((L2PcInstance) character);
			}
		}
		return super.onEnterZone(character, zone);
	}

	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance)
		{
			if(zone.getId() == fountain_1_zone)
			{
				PlayersInZone1.remove(character);
			}
			else if(zone.getId() == fountain_2_zone)
			{
				PlayersInZone2.remove(character);
			}
			else if(zone.getId() == fountain_3_zone)
			{
				PlayersInZone3.remove(character);
			}
		}
		return super.onExitZone(character, zone);
	}

	private static class Cast implements Runnable
	{
		L2Skill _skill;
		L2Character _npc;

		public Cast(L2Skill skill, L2Character npc)
		{
			_skill = skill;
			_npc = npc;
		}

		@Override
		public void run()
		{
			if(_npc != null && !_npc.isDead() && !_npc.isCastingNow())
			{

				_npc.setIsCastingNow(true);
				_npc.doCast(_skill);
			}
		}
	}
}
