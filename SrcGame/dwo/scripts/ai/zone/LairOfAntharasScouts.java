package dwo.scripts.ai.zone;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Util;
import javolution.util.FastList;

import java.util.List;

public class LairOfAntharasScouts extends Quest
{
	private static final int KNORIKS = 22857;
	private static final int LEADER = 22848;
	//positions
	private static final Location KNORIKS1_1 = new Location(141066, 109833, -3948, 0);
	private static final Location KNORIKS1_2 = new Location(143074, 108515, -3948, 0);
	private static final Location KNORIKS1_3 = new Location(142526, 107273, -3948, 0);
	private static final Location KNORIKS1_4 = new Location(140838, 107214, -3951, 0);
	private static final Location KNORIKS1_5 = new Location(140496, 108408, -3948, 0);
	private static final Location KNORIKS2_1 = new Location(143597, 110262, -3948, 0);
	private static final Location KNORIKS2_2 = new Location(142395, 110049, -3948, 0);
	private static final Location KNORIKS2_3 = new Location(142716, 111710, -3951, 0);
	private static final Location KNORIKS2_4 = new Location(141859, 112189, -3724, 0);
	private static final Location KNORIKS2_5 = new Location(140908, 113241, -3724, 0);
	private static final Location KNORIKS3_1 = new Location(140345, 117830, -3916, 0);
	private static final Location KNORIKS3_2 = new Location(140956, 119039, -3916, 0);
	private static final Location KNORIKS3_3 = new Location(140252, 119843, -3916, 0);
	private static final Location KNORIKS3_4 = new Location(141523, 121498, -3916, 0);
	private static final Location KNORIKS3_5 = new Location(143559, 120685, -3916, 0);
	private static final Location KNORIKS4_1 = new Location(140879, 119310, -3916, 0);
	private static final Location KNORIKS4_2 = new Location(142782, 118546, -3916, 0);
	private static final Location KNORIKS4_3 = new Location(142573, 117016, -3916, 0);
	private static final Location KNORIKS4_4 = new Location(140606, 117867, -3916, 0);
	private static final Location KNORIKS5_1 = new Location(140107, 119921, -3916, 0);
	private static final Location KNORIKS5_2 = new Location(141008, 118970, -3919, 0);
	private static final Location KNORIKS5_3 = new Location(143323, 120148, -3916, 0);
	private static final Location KNORIKS5_4 = new Location(142034, 121549, -3916, 0);
	private static final Location KNORIKS6_1 = new Location(149338, 110834, -5468, 0);
	private static final Location KNORIKS6_2 = new Location(150230, 109590, -5142, 0);
	private static final Location KNORIKS6_3 = new Location(153260, 109255, -5138, 0);
	private static final Location KNORIKS6_4 = new Location(153269, 107558, -5142, 0);
	private static final Location KNORIKS6_5 = new Location(150486, 107402, -4731, 0);
	private static final Location KNORIKS7_1 = new Location(149471, 111131, -5493, 0);
	private static final Location KNORIKS7_2 = new Location(153232, 111487, -5524, 0);
	private static final Location KNORIKS7_3 = new Location(153153, 112879, -5501, 0);
	private static final Location KNORIKS7_4 = new Location(151180, 114916, -5476, 0);
	private static final Location KNORIKS7_5 = new Location(148845, 114941, -5476, 0);
	private static final Location KNORIKS8_1 = new Location(146531, 108943, -3512, 0);
	private static final Location KNORIKS8_2 = new Location(147663, 107221, -4046, 0);
	private static final Location KNORIKS8_3 = new Location(149443, 108481, -4457, 0);
	private static final Location KNORIKS8_4 = new Location(150826, 107373, -4798, 0);
	private static final Location KNORIKS9_1 = new Location(147500, 109784, -3948, 0);
	private static final Location KNORIKS9_2 = new Location(145690, 109183, -3948, 0);
	private static final Location KNORIKS9_3 = new Location(144245, 107470, -3948, 0);
	private static final Location KNORIKS9_4 = new Location(142627, 107044, -3948, 0);
	private static final Location CIRCLE_1 = new Location(146563, 116289, -3724, 0);
	private static final Location CIRCLE_2 = new Location(145226, 115393, -3724, 0);
	private static final Location CIRCLE_3 = new Location(145118, 113569, -3724, 0);
	private static final Location CIRCLE_4 = new Location(145919, 112722, -3724, 0);
	private static final Location CIRCLE_5 = new Location(147854, 112029, -3724, 0);
	private static final Location CIRCLE_6 = new Location(148945, 112795, -3724, 0);
	private static final Location CIRCLE_7 = new Location(149210, 115054, -3727, 0);
	private static final Location CIRCLE_8 = new Location(148101, 116015, -3724, 0);
	private static int LoAzone = 46001;
	// Zone stuff
	public List<L2PcInstance> PlayersInZone = new FastList<>();
	// Npcs
	private L2Npc knoriks1;
	private L2Npc knoriks2;
	private L2Npc knoriks3;
	private L2Npc knoriks4;
	private L2Npc knoriks5;
	private L2Npc knoriks6;
	private L2Npc knoriks7;
	private L2Npc knoriks8;
	private L2Npc knoriks9;
	private L2Npc knoriks10;
	private L2Npc knoriks11;
	private L2Npc knoriks12;
	private L2Npc leader1;
	private L2Npc leader2;
	private L2Npc leader3;
	private L2Npc leader4;
	private L2Npc leader5;
	private L2Npc leader6;
	private L2Npc leader7;
	private L2Npc leader8;
	private L2Npc leader9;
	private L2Npc leader10;
	private L2Npc leader11;
	private L2Npc leader12;
	private L2Npc leader13;
	private L2Npc leader14;
	private L2Npc leader15;
	private L2Npc leader16;
	private L2Npc leader17;
	private L2Npc leader18;

	public LairOfAntharasScouts()
	{
		addKillId(KNORIKS, LEADER);
		addAggroRangeEnterId(KNORIKS, LEADER);
		addEnterZoneId(LoAzone);
		addExitZoneId(LoAzone);

		startQuestTimer("knoriks1spawn", 6000, null, null);
		startQuestTimer("knoriks2spawn", 6000, null, null);
		startQuestTimer("knoriks3spawn", 6000, null, null);
		startQuestTimer("knoriks4spawn", 6000, null, null);
		startQuestTimer("knoriks5spawn", 6000, null, null);
		startQuestTimer("knoriks6spawn", 6000, null, null);
		startQuestTimer("knoriks7spawn", 6000, null, null);
		startQuestTimer("knoriks8spawn", 6000, null, null);
		startQuestTimer("knoriks9spawn", 6000, null, null);
		startQuestTimer("knoriks10spawn", 6000, null, null);
		startQuestTimer("knoriks11spawn", 26000, null, null);
		startQuestTimer("knoriks12spawn", 46000, null, null);
		startQuestTimer("leader1spawn", 4000, null, null);
		startQuestTimer("leader2spawn", 8000, null, null);
		startQuestTimer("leader3spawn", 12000, null, null);
		startQuestTimer("leader4spawn", 16000, null, null);
		startQuestTimer("leader5spawn", 20000, null, null);
		startQuestTimer("leader6spawn", 24000, null, null);
		startQuestTimer("leader7spawn", 28000, null, null);
		startQuestTimer("leader8spawn", 32000, null, null);
		startQuestTimer("leader9spawn", 36000, null, null);
		startQuestTimer("leader10spawn", 40000, null, null);
		startQuestTimer("leader11spawn", 44000, null, null);
		startQuestTimer("leader12spawn", 48000, null, null);
		startQuestTimer("leader13spawn", 52000, null, null);
		startQuestTimer("leader14spawn", 56000, null, null);
		startQuestTimer("leader15spawn", 60000, null, null);
		startQuestTimer("leader16spawn", 64000, null, null);
		startQuestTimer("leader17spawn", 68000, null, null);
		startQuestTimer("leader18spawn", 72000, null, null);
	}

	public static void main(String[] args)
	{
		new LairOfAntharasScouts();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("Check400"))
		{
			PlayersInZone.stream().filter(pc -> Util.checkIfInRange(400, npc, pc, true)).forEach(pc -> {
				if(!npc.isCastingNow() && !npc.isAttackingNow() && !npc.isInCombat() && !pc.isDead() && !(npc.getNpcId() == LEADER && pc.isSilentMoving()))
				{
					((L2Attackable) npc).attackCharacter(pc);
				}
			});
			startQuestTimer("Check400", 1500, npc, null);
		}
		else if(event.equalsIgnoreCase("knoriks1spawn") && knoriks1 == null)
		{
			knoriks1 = addSpawn(KNORIKS, 141066, 109833, -3948, 0, false, 0);
			knoriks1.setIsNoRndWalk(true);
			knoriks1.setRunning();
			startQuestTimer("knoriks1move1", 5000, knoriks1, null);
			startQuestTimer("Check400", 1500, knoriks1, null);
		}
		else if(event.equalsIgnoreCase("knoriks1move1") && knoriks1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks1move1", 60000, knoriks1, null);
			}
			else
			{
				knoriks1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS1_2);
				knoriks1.setRunning();
				startQuestTimer("knoriks1move2", 12000, knoriks1, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks1move2") && knoriks1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks1move2", 60000, knoriks1, null);
			}
			else
			{
				knoriks1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS1_3);
				knoriks1.setRunning();
				startQuestTimer("knoriks1move3", 7000, knoriks1, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks1move3") && knoriks1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks1move3", 60000, knoriks1, null);
			}
			else
			{
				knoriks1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS1_4);
				knoriks1.setRunning();
				startQuestTimer("knoriks1move4", 8000, knoriks1, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks1move4") && knoriks1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks1move4", 60000, knoriks1, null);
			}
			else
			{
				knoriks1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS1_5);
				knoriks1.setRunning();
				startQuestTimer("knoriks1move5", 6000, knoriks1, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks1move5") && knoriks1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks1move5", 60000, knoriks1, null);
			}
			else
			{
				knoriks1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS1_1);
				knoriks1.setRunning();
				startQuestTimer("knoriks1move1", 7000, knoriks1, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks2spawn") && knoriks2 == null)
		{
			knoriks2 = addSpawn(KNORIKS, 143597, 110262, -3948, 0, false, 0);
			knoriks2.setIsNoRndWalk(true);
			knoriks2.setRunning();
			startQuestTimer("knoriks2move1", 5000, knoriks2, null);
			startQuestTimer("Check400", 1500, knoriks2, null);
		}
		else if(event.equalsIgnoreCase("knoriks2move1") && knoriks2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks2move1", 60000, knoriks2, null);
			}
			else
			{
				knoriks2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS2_2);
				knoriks2.setRunning();
				startQuestTimer("knoriks2move2", 6000, knoriks2, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks2move2") && knoriks2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks2move2", 60000, knoriks2, null);
			}
			else
			{
				knoriks2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS2_3);
				knoriks2.setRunning();
				startQuestTimer("knoriks2move3", 9000, knoriks2, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks2move3") && knoriks2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks2move3", 60000, knoriks2, null);
			}
			else
			{
				knoriks2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS2_4);
				knoriks2.setRunning();
				startQuestTimer("knoriks2move4", 5000, knoriks2, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks2move4") && knoriks2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks2move4", 60000, knoriks2, null);
			}
			else
			{
				knoriks2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS2_5);
				knoriks2.setRunning();
				startQuestTimer("knoriks2move5", 7000, knoriks2, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks2move5") && knoriks2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks2move5", 60000, knoriks2, null);
			}
			else
			{
				knoriks2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS2_4);
				knoriks2.setRunning();
				startQuestTimer("knoriks2move6", 7000, knoriks2, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks2move6") && knoriks2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks2move6", 60000, knoriks2, null);
			}
			else
			{
				knoriks2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS2_3);
				knoriks2.setRunning();
				startQuestTimer("knoriks2move7", 5000, knoriks2, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks2move7") && knoriks2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks2move7", 60000, knoriks2, null);
			}
			else
			{
				knoriks2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS2_2);
				knoriks2.setRunning();
				startQuestTimer("knoriks2move8", 9000, knoriks2, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks2move8") && knoriks2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks2move8", 60000, knoriks2, null);
			}
			else
			{
				knoriks2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS2_1);
				knoriks2.setRunning();
				startQuestTimer("knoriks2move1", 6000, knoriks2, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks3spawn") && knoriks3 == null)
		{
			knoriks3 = addSpawn(KNORIKS, 140345, 117830, -3916, 0, false, 0);
			knoriks3.setIsNoRndWalk(true);
			knoriks3.setRunning();
			startQuestTimer("knoriks3move1", 5000, knoriks3, null);
			startQuestTimer("Check400", 1500, knoriks3, null);
		}
		else if(event.equalsIgnoreCase("knoriks3move1") && knoriks3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks3move1", 60000, knoriks3, null);
			}
			else
			{
				knoriks3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS3_2);
				knoriks3.setRunning();
				startQuestTimer("knoriks3move2", 7000, knoriks3, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks3move2") && knoriks3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks3move2", 60000, knoriks3, null);
			}
			else
			{
				knoriks3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS3_3);
				knoriks3.setRunning();
				startQuestTimer("knoriks3move3", 5000, knoriks3, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks3move3") && knoriks3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks3move3", 60000, knoriks3, null);
			}
			else
			{
				knoriks3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS3_4);
				knoriks3.setRunning();
				startQuestTimer("knoriks3move4", 10000, knoriks3, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks3move4") && knoriks3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks3move4", 60000, knoriks3, null);
			}
			else
			{
				knoriks3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS3_5);
				knoriks3.setRunning();
				startQuestTimer("knoriks3move5", 11000, knoriks3, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks3move5") && knoriks3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks3move5", 60000, knoriks3, null);
			}
			else
			{
				knoriks3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS3_4);
				knoriks3.setRunning();
				startQuestTimer("knoriks3move2", 11000, knoriks3, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks3move6") && knoriks3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks3move6", 60000, knoriks3, null);
			}
			else
			{
				knoriks3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS3_3);
				knoriks3.setRunning();
				startQuestTimer("knoriks3move7", 10000, knoriks3, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks3move7") && knoriks3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks3move7", 60000, knoriks3, null);
			}
			else
			{
				knoriks3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS3_2);
				knoriks3.setRunning();
				startQuestTimer("knoriks3move8", 5000, knoriks3, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks3move8") && knoriks3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks3move8", 60000, knoriks3, null);
			}
			else
			{
				knoriks3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS3_1);
				knoriks3.setRunning();
				startQuestTimer("knoriks3move1", 7000, knoriks3, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks4spawn") && knoriks4 == null)
		{
			knoriks4 = addSpawn(KNORIKS, 140879, 119310, -3916, 0, false, 0);
			knoriks4.setIsNoRndWalk(true);
			knoriks4.setRunning();
			startQuestTimer("knoriks4move1", 5000, knoriks4, null);
			startQuestTimer("Check400", 1500, knoriks4, null);
		}
		else if(event.equalsIgnoreCase("knoriks4move1") && knoriks4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks4move1", 60000, knoriks4, null);
			}
			else
			{
				knoriks4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS4_2);
				knoriks4.setRunning();
				startQuestTimer("knoriks4move2", 10000, knoriks4, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks4move2") && knoriks4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks4move2", 60000, knoriks4, null);
			}
			else
			{
				knoriks4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS4_3);
				knoriks4.setRunning();
				startQuestTimer("knoriks4move3", 8000, knoriks4, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks4move3") && knoriks4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks4move3", 60000, knoriks4, null);
			}
			else
			{
				knoriks4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS4_4);
				knoriks4.setRunning();
				startQuestTimer("knoriks4move4", 10000, knoriks4, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks4move4") && knoriks4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks4move4", 60000, knoriks4, null);
			}
			else
			{
				knoriks4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS4_1);
				knoriks4.setRunning();
				startQuestTimer("knoriks4move1", 7000, knoriks4, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks5spawn") && knoriks5 == null)
		{
			knoriks5 = addSpawn(KNORIKS, 140107, 119921, -3916, 0, false, 0);
			knoriks5.setIsNoRndWalk(true);
			knoriks5.setRunning();
			startQuestTimer("knoriks5move1", 5000, knoriks5, null);
			startQuestTimer("Check400", 1500, knoriks5, null);
		}
		else if(event.equalsIgnoreCase("knoriks5move1") && knoriks5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks5move1", 60000, knoriks5, null);
			}
			else
			{
				knoriks5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS5_2);
				knoriks5.setRunning();
				startQuestTimer("knoriks5move2", 7000, knoriks5, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks5move2") && knoriks5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks5move2", 60000, knoriks5, null);
			}
			else
			{
				knoriks5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS5_3);
				knoriks5.setRunning();
				startQuestTimer("knoriks5move3", 13000, knoriks5, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks5move3") && knoriks5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks5move3", 60000, knoriks5, null);
			}
			else
			{
				knoriks5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS5_4);
				knoriks5.setRunning();
				startQuestTimer("knoriks5move4", 10000, knoriks5, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks5move4") && knoriks5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks5move4", 60000, knoriks5, null);
			}
			else
			{
				knoriks5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS5_1);
				knoriks5.setRunning();
				startQuestTimer("knoriks5move1", 13000, knoriks5, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks6spawn") && knoriks6 == null)
		{
			knoriks6 = addSpawn(KNORIKS, 149338, 110834, -5468, 0, false, 0);
			knoriks6.setIsNoRndWalk(true);
			knoriks6.setRunning();
			startQuestTimer("knoriks6move1", 5000, knoriks6, null);
			startQuestTimer("Check400", 1500, knoriks6, null);
		}
		else if(event.equalsIgnoreCase("knoriks6move1") && knoriks6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks6move1", 60000, knoriks6, null);
			}
			else
			{
				knoriks6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS6_2);
				knoriks6.setRunning();
				startQuestTimer("knoriks6move2", 8000, knoriks6, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks6move2") && knoriks6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks6move2", 60000, knoriks6, null);
			}
			else
			{
				knoriks6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS6_3);
				knoriks6.setRunning();
				startQuestTimer("knoriks6move3", 16000, knoriks6, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks6move3") && knoriks6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks6move3", 60000, knoriks6, null);
			}
			else
			{
				knoriks6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS6_4);
				knoriks6.setRunning();
				startQuestTimer("knoriks6move4", 8000, knoriks6, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks6move4") && knoriks6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks6move4", 60000, knoriks6, null);
			}
			else
			{
				knoriks6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS6_5);
				knoriks6.setRunning();
				startQuestTimer("knoriks6move5", 14000, knoriks6, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks6move5") && knoriks6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks6move5", 60000, knoriks6, null);
			}
			else
			{
				knoriks6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS6_4);
				knoriks6.setRunning();
				startQuestTimer("knoriks6move6", 14000, knoriks6, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks6move6") && knoriks6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks6move6", 60000, knoriks6, null);
			}
			else
			{
				knoriks6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS6_3);
				knoriks6.setRunning();
				startQuestTimer("knoriks6move7", 8000, knoriks6, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks6move7") && knoriks6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks6move7", 60000, knoriks6, null);
			}
			else
			{
				knoriks6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS6_2);
				knoriks6.setRunning();
				startQuestTimer("knoriks6move8", 16000, knoriks6, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks6move8") && knoriks6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks6move8", 60000, knoriks6, null);
			}
			else
			{
				knoriks6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS6_1);
				knoriks6.setRunning();
				startQuestTimer("knoriks6move1", 8000, knoriks6, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks7spawn") && knoriks7 == null)
		{
			knoriks7 = addSpawn(KNORIKS, 149471, 111131, -5493, 0, false, 0);
			knoriks7.setIsNoRndWalk(true);
			knoriks7.setRunning();
			startQuestTimer("knoriks7move1", 5000, knoriks7, null);
			startQuestTimer("Check400", 1500, knoriks7, null);
		}
		else if(event.equalsIgnoreCase("knoriks7move1") && knoriks7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks7move1", 60000, knoriks7, null);
			}
			else
			{
				knoriks7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS7_2);
				knoriks7.setRunning();
				startQuestTimer("knoriks7move2", 22000, knoriks7, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks7move2") && knoriks7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks7move2", 60000, knoriks7, null);
			}
			else
			{
				knoriks7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS7_3);
				knoriks7.setRunning();
				startQuestTimer("knoriks7move3", 7000, knoriks7, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks7move3") && knoriks7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks7move3", 60000, knoriks7, null);
			}
			else
			{
				knoriks7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS7_4);
				knoriks7.setRunning();
				startQuestTimer("knoriks7move4", 13000, knoriks7, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks7move4") && knoriks7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks7move4", 60000, knoriks7, null);
			}
			else
			{
				knoriks7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS7_5);
				knoriks7.setRunning();
				startQuestTimer("knoriks7move5", 12000, knoriks7, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks7move5") && knoriks7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks7move5", 60000, knoriks7, null);
			}
			else
			{
				knoriks7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS7_4);
				knoriks7.setRunning();
				startQuestTimer("knoriks7move6", 12000, knoriks7, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks7move6") && knoriks7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks7move6", 60000, knoriks7, null);
			}
			else
			{
				knoriks7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS7_3);
				knoriks7.setRunning();
				startQuestTimer("knoriks7move7", 13000, knoriks7, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks7move7") && knoriks7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks7move7", 60000, knoriks7, null);
			}
			else
			{
				knoriks7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS7_2);
				knoriks7.setRunning();
				startQuestTimer("knoriks7move8", 7000, knoriks7, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks7move8") && knoriks7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks7move8", 60000, knoriks7, null);
			}
			else
			{
				knoriks7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS7_1);
				knoriks7.setRunning();
				startQuestTimer("knoriks7move1", 22000, knoriks7, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks8spawn") && knoriks8 == null)
		{
			knoriks8 = addSpawn(KNORIKS, 146531, 108943, -3512, 0, false, 0);
			knoriks8.setIsNoRndWalk(true);
			knoriks8.setRunning();
			startQuestTimer("knoriks8move1", 5000, knoriks8, null);
			startQuestTimer("Check400", 1500, knoriks8, null);
		}
		else if(event.equalsIgnoreCase("knoriks8move1") && knoriks8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks8move1", 60000, knoriks8, null);
			}
			else
			{
				knoriks8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS8_2);
				knoriks8.setRunning();
				startQuestTimer("knoriks8move2", 12000, knoriks8, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks8move2") && knoriks8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks8move2", 60000, knoriks8, null);
			}
			else
			{
				knoriks8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS8_3);
				knoriks8.setRunning();
				startQuestTimer("knoriks8move3", 11000, knoriks8, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks8move3") && knoriks8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks8move3", 60000, knoriks8, null);
			}
			else
			{
				knoriks8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS8_4);
				knoriks8.setRunning();
				startQuestTimer("knoriks8move4", 9000, knoriks8, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks8move4") && knoriks8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks8move4", 60000, knoriks8, null);
			}
			else
			{
				knoriks8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS8_3);
				knoriks8.setRunning();
				startQuestTimer("knoriks8move5", 9000, knoriks8, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks8move5") && knoriks8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks8move5", 60000, knoriks8, null);
			}
			else
			{
				knoriks8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS8_2);
				knoriks8.setRunning();
				startQuestTimer("knoriks8move6", 11000, knoriks8, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks8move6") && knoriks8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks8move6", 60000, knoriks8, null);
			}
			else
			{
				knoriks8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS8_1);
				knoriks8.setRunning();
				startQuestTimer("knoriks8move1", 12000, knoriks8, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks9spawn") && knoriks9 == null)
		{
			knoriks9 = addSpawn(KNORIKS, 147500, 109784, -3948, 0, false, 0);
			knoriks9.setIsNoRndWalk(true);
			knoriks9.setRunning();
			startQuestTimer("knoriks9move1", 5000, knoriks9, null);
			startQuestTimer("Check400", 1500, knoriks9, null);
		}
		else if(event.equalsIgnoreCase("knoriks9move1") && knoriks9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks9move1", 60000, knoriks9, null);
			}
			else
			{
				knoriks9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS9_2);
				knoriks9.setRunning();
				startQuestTimer("knoriks9move2", 10000, knoriks9, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks9move2") && knoriks9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks9move2", 60000, knoriks9, null);
			}
			else
			{
				knoriks9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS9_3);
				knoriks9.setRunning();
				startQuestTimer("knoriks9move3", 11000, knoriks9, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks9move3") && knoriks9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks9move3", 60000, knoriks9, null);
			}
			else
			{
				knoriks9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS9_4);
				knoriks9.setRunning();
				startQuestTimer("knoriks9move4", 8000, knoriks9, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks9move4") && knoriks9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks9move4", 60000, knoriks9, null);
			}
			else
			{
				knoriks9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS9_3);
				knoriks9.setRunning();
				startQuestTimer("knoriks9move5", 8000, knoriks9, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks9move5") && knoriks9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks9move5", 60000, knoriks9, null);
			}
			else
			{
				knoriks9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS9_2);
				knoriks9.setRunning();
				startQuestTimer("knoriks9move6", 11000, knoriks9, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks9move6") && knoriks9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks9move6", 60000, knoriks9, null);
			}
			else
			{
				knoriks9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, KNORIKS9_1);
				knoriks9.setRunning();
				startQuestTimer("knoriks9move1", 10000, knoriks9, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks10spawn") && knoriks10 == null)
		{
			knoriks10 = addSpawn(KNORIKS, 146563, 116289, -3724, 0, false, 0);
			knoriks10.setIsNoRndWalk(true);
			knoriks10.setRunning();
			startQuestTimer("knoriks10circle1", 5000, knoriks10, null);
			startQuestTimer("Check400", 1500, knoriks10, null);
		}
		else if(event.equalsIgnoreCase("knoriks10circle1") && knoriks10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks10circle1", 60000, knoriks10, null);
			}
			else
			{
				knoriks10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				knoriks10.setRunning();
				startQuestTimer("knoriks10circle2", 9000, knoriks10, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks10circle2") && knoriks10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks10circle2", 60000, knoriks10, null);
			}
			else
			{
				knoriks10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				knoriks10.setRunning();
				startQuestTimer("knoriks10circle3", 10000, knoriks10, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks10circle3") && knoriks10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks10circle3", 60000, knoriks10, null);
			}
			else
			{
				knoriks10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				knoriks10.setRunning();
				startQuestTimer("knoriks10circle4", 6000, knoriks10, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks10circle4") && knoriks10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks10circle4", 60000, knoriks10, null);
			}
			else
			{
				knoriks10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				knoriks10.setRunning();
				startQuestTimer("knoriks10circle5", 11000, knoriks10, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks10circle5") && knoriks10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks10circle5", 60000, knoriks10, null);
			}
			else
			{
				knoriks10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				knoriks10.setRunning();
				startQuestTimer("knoriks10circle6", 7000, knoriks10, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks10circle6") && knoriks10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks10circle6", 60000, knoriks10, null);
			}
			else
			{
				knoriks10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				knoriks10.setRunning();
				startQuestTimer("knoriks10circle7", 12000, knoriks10, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks10circle7") && knoriks10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks10circle7", 60000, knoriks10, null);
			}
			else
			{
				knoriks10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				knoriks10.setRunning();
				startQuestTimer("knoriks10circle8", 8000, knoriks10, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks10circle8") && knoriks10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks10circle8", 60000, knoriks10, null);
			}
			else
			{
				knoriks10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				knoriks10.setRunning();
				startQuestTimer("knoriks10circle1", 8000, knoriks10, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks11spawn") && knoriks11 == null)
		{
			knoriks11 = addSpawn(KNORIKS, 146563, 116289, -3724, 0, false, 0);
			knoriks11.setIsNoRndWalk(true);
			knoriks11.setRunning();
			startQuestTimer("knoriks11circle1", 5000, knoriks11, null);
			startQuestTimer("Check400", 1500, knoriks11, null);
		}
		else if(event.equalsIgnoreCase("knoriks11circle1") && knoriks11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks11circle1", 60000, knoriks11, null);
			}
			else
			{
				knoriks11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				knoriks11.setRunning();
				startQuestTimer("knoriks11circle2", 9000, knoriks11, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks11circle2") && knoriks11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks11circle2", 60000, knoriks11, null);
			}
			else
			{
				knoriks11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				knoriks11.setRunning();
				startQuestTimer("knoriks11circle3", 10000, knoriks11, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks11circle3") && knoriks11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks11circle3", 60000, knoriks11, null);
			}
			else
			{
				knoriks11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				knoriks11.setRunning();
				startQuestTimer("knoriks11circle4", 6000, knoriks11, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks11circle4") && knoriks11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks11circle4", 60000, knoriks11, null);
			}
			else
			{
				knoriks11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				knoriks11.setRunning();
				startQuestTimer("knoriks11circle5", 11000, knoriks11, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks11circle5") && knoriks11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks11circle5", 60000, knoriks11, null);
			}
			else
			{
				knoriks11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				knoriks11.setRunning();
				startQuestTimer("knoriks11circle6", 7000, knoriks11, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks11circle6") && knoriks11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks11circle6", 60000, knoriks11, null);
			}
			else
			{
				knoriks11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				knoriks11.setRunning();
				startQuestTimer("knoriks11circle7", 12000, knoriks11, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks11circle7") && knoriks11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks11circle7", 60000, knoriks11, null);
			}
			else
			{
				knoriks11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				knoriks11.setRunning();
				startQuestTimer("knoriks11circle8", 8000, knoriks11, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks11circle8") && knoriks11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks11circle8", 60000, knoriks11, null);
			}
			else
			{
				knoriks11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				knoriks11.setRunning();
				startQuestTimer("knoriks11circle1", 8000, knoriks11, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks12spawn") && knoriks12 == null)
		{
			knoriks12 = addSpawn(KNORIKS, 146563, 116289, -3724, 0, false, 0);
			knoriks12.setIsNoRndWalk(true);
			knoriks12.setRunning();
			startQuestTimer("knoriks12circle1", 5000, knoriks12, null);
			startQuestTimer("Check400", 1500, knoriks12, null);
		}
		else if(event.equalsIgnoreCase("knoriks12circle1") && knoriks12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks12circle1", 60000, knoriks12, null);
			}
			else
			{
				knoriks12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				knoriks12.setRunning();
				startQuestTimer("knoriks12circle2", 9000, knoriks12, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks12circle2") && knoriks12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks12circle2", 60000, knoriks12, null);
			}
			else
			{
				knoriks12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				knoriks12.setRunning();
				startQuestTimer("knoriks12circle3", 10000, knoriks12, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks12circle3") && knoriks12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks12circle3", 60000, knoriks12, null);
			}
			else
			{
				knoriks12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				knoriks12.setRunning();
				startQuestTimer("knoriks12circle4", 6000, knoriks12, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks12circle4") && knoriks12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks12circle4", 60000, knoriks12, null);
			}
			else
			{
				knoriks12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				knoriks12.setRunning();
				startQuestTimer("knoriks12circle5", 11000, knoriks12, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks12circle5") && knoriks12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks12circle5", 60000, knoriks12, null);
			}
			else
			{
				knoriks12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				knoriks12.setRunning();
				startQuestTimer("knoriks12circle6", 7000, knoriks12, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks12circle6") && knoriks12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks12circle6", 60000, knoriks12, null);
			}
			else
			{
				knoriks12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				knoriks12.setRunning();
				startQuestTimer("knoriks12circle7", 12000, knoriks12, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks12circle7") && knoriks12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks12circle7", 60000, knoriks12, null);
			}
			else
			{
				knoriks12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				knoriks12.setRunning();
				startQuestTimer("knoriks12circle8", 8000, knoriks12, null);
			}
		}
		else if(event.equalsIgnoreCase("knoriks12circle8") && knoriks12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("knoriks12circle8", 60000, knoriks12, null);
			}
			else
			{
				knoriks12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				knoriks12.setRunning();
				startQuestTimer("knoriks12circle1", 8000, knoriks12, null);
			}
		}
		else if(event.equalsIgnoreCase("leader1spawn") && leader1 == null)
		{
			leader1 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader1.setIsNoRndWalk(true);
			leader1.setRunning();
			startQuestTimer("leader1circle1", 5000, leader1, null);
			startQuestTimer("Check400", 1500, leader1, null);
			startQuestTimer("control_minions", 5000, leader1, null);
		}
		else if(event.equalsIgnoreCase("leader1circle1") && leader1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader1circle1", 60000, leader1, null);
			}
			else
			{
				leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader1.setRunning();
				startQuestTimer("leader1circle2", 9000, leader1, null);
			}
		}
		else if(event.equalsIgnoreCase("leader1circle2") && leader1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader1circle2", 60000, leader1, null);
			}
			else
			{
				leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader1.setRunning();
				startQuestTimer("leader1circle3", 10000, leader1, null);
			}
		}
		else if(event.equalsIgnoreCase("leader1circle3") && leader1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader1circle3", 60000, leader1, null);
			}
			else
			{
				leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader1.setRunning();
				startQuestTimer("leader1circle4", 6000, leader1, null);
			}
		}
		else if(event.equalsIgnoreCase("leader1circle4") && leader1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader1circle4", 60000, leader1, null);
			}
			else
			{
				leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader1.setRunning();
				startQuestTimer("leader1circle5", 11000, leader1, null);
			}
		}
		else if(event.equalsIgnoreCase("leader1circle5") && leader1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader1circle5", 60000, leader1, null);
			}
			else
			{
				leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader1.setRunning();
				startQuestTimer("leader1circle6", 7000, leader1, null);
			}
		}
		else if(event.equalsIgnoreCase("leader1circle6") && leader1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader1circle6", 60000, leader1, null);
			}
			else
			{
				leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader1.setRunning();
				startQuestTimer("leader1circle7", 12000, leader1, null);
			}
		}
		else if(event.equalsIgnoreCase("leader1circle7") && leader1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader1circle7", 60000, leader1, null);
			}
			else
			{
				leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader1.setRunning();
				startQuestTimer("leader1circle8", 8000, leader1, null);
			}
		}
		else if(event.equalsIgnoreCase("leader1circle8") && leader1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader1circle8", 60000, leader1, null);
			}
			else
			{
				leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader1.setRunning();
				startQuestTimer("leader1circle1", 8000, leader1, null);
			}
		}
		else if(event.equalsIgnoreCase("leader2spawn") && leader2 == null)
		{
			leader2 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader2.setIsNoRndWalk(true);
			leader2.setRunning();
			startQuestTimer("leader2circle1", 5000, leader2, null);
			startQuestTimer("Check400", 1500, leader2, null);
			startQuestTimer("control_minions", 5000, leader2, null);
		}
		else if(event.equalsIgnoreCase("leader2circle1") && leader2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader2circle1", 60000, leader2, null);
			}
			else
			{
				leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader2.setRunning();
				startQuestTimer("leader2circle2", 9000, leader2, null);
			}
		}
		else if(event.equalsIgnoreCase("leader2circle2") && leader2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader2circle2", 60000, leader2, null);
			}
			else
			{
				leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader2.setRunning();
				startQuestTimer("leader2circle3", 10000, leader2, null);
			}
		}
		else if(event.equalsIgnoreCase("leader2circle3") && leader2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader2circle3", 60000, leader2, null);
			}
			else
			{
				leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader2.setRunning();
				startQuestTimer("leader2circle4", 6000, leader2, null);
			}
		}
		else if(event.equalsIgnoreCase("leader2circle4") && leader2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader2circle4", 60000, leader2, null);
			}
			else
			{
				leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader2.setRunning();
				startQuestTimer("leader2circle5", 11000, leader2, null);
			}
		}
		else if(event.equalsIgnoreCase("leader2circle5") && leader2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader2circle5", 60000, leader2, null);
			}
			else
			{
				leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader2.setRunning();
				startQuestTimer("leader2circle6", 7000, leader2, null);
			}
		}
		else if(event.equalsIgnoreCase("leader2circle6") && leader2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader2circle6", 60000, leader2, null);
			}
			else
			{
				leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader2.setRunning();
				startQuestTimer("leader2circle7", 12000, leader2, null);
			}
		}
		else if(event.equalsIgnoreCase("leader2circle7") && leader2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader2circle7", 60000, leader2, null);
			}
			else
			{
				leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader2.setRunning();
				startQuestTimer("leader2circle8", 8000, leader2, null);
			}
		}
		else if(event.equalsIgnoreCase("leader2circle8") && leader2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader2circle8", 60000, leader2, null);
			}
			else
			{
				leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader2.setRunning();
				startQuestTimer("leader2circle1", 8000, leader2, null);
			}
		}
		else if(event.equalsIgnoreCase("leader3spawn") && leader3 == null)
		{
			leader3 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader3.setIsNoRndWalk(true);
			leader3.setRunning();
			startQuestTimer("leader3circle1", 5000, leader3, null);
			startQuestTimer("Check400", 1500, leader3, null);
			startQuestTimer("control_minions", 5000, leader3, null);
		}
		else if(event.equalsIgnoreCase("leader3circle1") && leader3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader3circle1", 60000, leader3, null);
			}
			else
			{
				leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader3.setRunning();
				startQuestTimer("leader3circle2", 9000, leader3, null);
			}
		}
		else if(event.equalsIgnoreCase("leader3circle2") && leader3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader3circle2", 60000, leader3, null);
			}
			else
			{
				leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader3.setRunning();
				startQuestTimer("leader3circle3", 10000, leader3, null);
			}
		}
		else if(event.equalsIgnoreCase("leader3circle3") && leader3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader3circle3", 60000, leader3, null);
			}
			else
			{
				leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader3.setRunning();
				startQuestTimer("leader3circle4", 6000, leader3, null);
			}
		}
		else if(event.equalsIgnoreCase("leader3circle4") && leader3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader3circle4", 60000, leader3, null);
			}
			else
			{
				leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader3.setRunning();
				startQuestTimer("leader3circle5", 11000, leader3, null);
			}
		}
		else if(event.equalsIgnoreCase("leader3circle5") && leader3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader3circle5", 60000, leader3, null);
			}
			else
			{
				leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader3.setRunning();
				startQuestTimer("leader3circle6", 7000, leader3, null);
			}
		}
		else if(event.equalsIgnoreCase("leader3circle6") && leader3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader3circle6", 60000, leader3, null);
			}
			else
			{
				leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader3.setRunning();
				startQuestTimer("leader3circle7", 12000, leader3, null);
			}
		}
		else if(event.equalsIgnoreCase("leader3circle7") && leader3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader3circle7", 60000, leader3, null);
			}
			else
			{
				leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader3.setRunning();
				startQuestTimer("leader3circle8", 8000, leader3, null);
			}
		}
		else if(event.equalsIgnoreCase("leader3circle8") && leader3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader3circle8", 60000, leader3, null);
			}
			else
			{
				leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader3.setRunning();
				startQuestTimer("leader3circle1", 8000, leader3, null);
			}
		}
		else if(event.equalsIgnoreCase("leader4spawn") && leader4 == null)
		{
			leader4 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader4.setIsNoRndWalk(true);
			leader4.setRunning();
			startQuestTimer("leader4circle1", 5000, leader4, null);
			startQuestTimer("Check400", 1500, leader4, null);
			startQuestTimer("control_minions", 5000, leader4, null);
		}
		else if(event.equalsIgnoreCase("leader4circle1") && leader4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader4circle1", 60000, leader4, null);
			}
			else
			{
				leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader4.setRunning();
				startQuestTimer("leader4circle2", 9000, leader4, null);
			}
		}
		else if(event.equalsIgnoreCase("leader4circle2") && leader4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader4circle2", 60000, leader4, null);
			}
			else
			{
				leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader4.setRunning();
				startQuestTimer("leader4circle3", 10000, leader4, null);
			}
		}
		else if(event.equalsIgnoreCase("leader4circle3") && leader4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader4circle3", 60000, leader4, null);
			}
			else
			{
				leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader4.setRunning();
				startQuestTimer("leader4circle4", 6000, leader4, null);
			}
		}
		else if(event.equalsIgnoreCase("leader4circle4") && leader4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader4circle4", 60000, leader4, null);
			}
			else
			{
				leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader4.setRunning();
				startQuestTimer("leader4circle5", 11000, leader4, null);
			}
		}
		else if(event.equalsIgnoreCase("leader4circle5") && leader4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader4circle5", 60000, leader4, null);
			}
			else
			{
				leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader4.setRunning();
				startQuestTimer("leader4circle6", 7000, leader4, null);
			}
		}
		else if(event.equalsIgnoreCase("leader4circle6") && leader4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader4circle6", 60000, leader4, null);
			}
			else
			{
				leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader4.setRunning();
				startQuestTimer("leader4circle7", 12000, leader4, null);
			}
		}
		else if(event.equalsIgnoreCase("leader4circle7") && leader4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader4circle7", 60000, leader4, null);
			}
			else
			{
				leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader4.setRunning();
				startQuestTimer("leader4circle8", 8000, leader4, null);
			}
		}
		else if(event.equalsIgnoreCase("leader4circle8") && leader4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader4circle8", 60000, leader4, null);
			}
			else
			{
				leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader4.setRunning();
				startQuestTimer("leader4circle1", 8000, leader4, null);
			}
		}
		else if(event.equalsIgnoreCase("leader5spawn") && leader5 == null)
		{
			leader5 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader5.setIsNoRndWalk(true);
			leader5.setRunning();
			startQuestTimer("leader5circle1", 5000, leader5, null);
			startQuestTimer("Check400", 1500, leader5, null);
			startQuestTimer("control_minions", 5000, leader5, null);
		}
		else if(event.equalsIgnoreCase("leader5circle1") && leader5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader5circle1", 60000, leader5, null);
			}
			else
			{
				leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader5.setRunning();
				startQuestTimer("leader5circle2", 9000, leader5, null);
			}
		}
		else if(event.equalsIgnoreCase("leader5circle2") && leader5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader5circle2", 60000, leader5, null);
			}
			else
			{
				leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader5.setRunning();
				startQuestTimer("leader5circle3", 10000, leader5, null);
			}
		}
		else if(event.equalsIgnoreCase("leader5circle3") && leader5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader5circle3", 60000, leader5, null);
			}
			else
			{
				leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader5.setRunning();
				startQuestTimer("leader5circle4", 6000, leader5, null);
			}
		}
		else if(event.equalsIgnoreCase("leader5circle4") && leader5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader5circle4", 60000, leader5, null);
			}
			else
			{
				leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader5.setRunning();
				startQuestTimer("leader5circle5", 11000, leader5, null);
			}
		}
		else if(event.equalsIgnoreCase("leader5circle5") && leader5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader5circle5", 60000, leader5, null);
			}
			else
			{
				leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader5.setRunning();
				startQuestTimer("leader5circle6", 7000, leader5, null);
			}
		}
		else if(event.equalsIgnoreCase("leader5circle6") && leader5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader5circle6", 60000, leader5, null);
			}
			else
			{
				leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader5.setRunning();
				startQuestTimer("leader5circle7", 12000, leader5, null);
			}
		}
		else if(event.equalsIgnoreCase("leader5circle7") && leader5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader5circle7", 60000, leader5, null);
			}
			else
			{
				leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader5.setRunning();
				startQuestTimer("leader5circle8", 8000, leader5, null);
			}
		}
		else if(event.equalsIgnoreCase("leader5circle8") && leader5 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader5circle8", 60000, leader5, null);
			}
			else
			{
				leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader5.setRunning();
				startQuestTimer("leader5circle1", 8000, leader5, null);
			}
		}
		else if(event.equalsIgnoreCase("leader6spawn") && leader6 == null)
		{
			leader6 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader6.setIsNoRndWalk(true);
			leader6.setRunning();
			startQuestTimer("leader6circle1", 5000, leader6, null);
			startQuestTimer("Check400", 1500, leader6, null);
			startQuestTimer("control_minions", 5000, leader6, null);
		}
		else if(event.equalsIgnoreCase("leader6circle1") && leader6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader6circle1", 60000, leader6, null);
			}
			else
			{
				leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader6.setRunning();
				startQuestTimer("leader6circle2", 9000, leader6, null);
			}
		}
		else if(event.equalsIgnoreCase("leader6circle2") && leader6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader6circle2", 60000, leader6, null);
			}
			else
			{
				leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader6.setRunning();
				startQuestTimer("leader6circle3", 10000, leader6, null);
			}
		}
		else if(event.equalsIgnoreCase("leader6circle3") && leader6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader6circle3", 60000, leader6, null);
			}
			else
			{
				leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader6.setRunning();
				startQuestTimer("leader6circle4", 6000, leader6, null);
			}
		}
		else if(event.equalsIgnoreCase("leader6circle4") && leader6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader6circle4", 60000, leader6, null);
			}
			else
			{
				leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader6.setRunning();
				startQuestTimer("leader6circle5", 11000, leader6, null);
			}
		}
		else if(event.equalsIgnoreCase("leader6circle5") && leader6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader6circle5", 60000, leader6, null);
			}
			else
			{
				leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader6.setRunning();
				startQuestTimer("leader6circle6", 7000, leader6, null);
			}
		}
		else if(event.equalsIgnoreCase("leader6circle6") && leader6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader6circle6", 60000, leader6, null);
			}
			else
			{
				leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader6.setRunning();
				startQuestTimer("leader6circle7", 12000, leader6, null);
			}
		}
		else if(event.equalsIgnoreCase("leader6circle7") && leader6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader6circle7", 60000, leader6, null);
			}
			else
			{
				leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader6.setRunning();
				startQuestTimer("leader6circle8", 8000, leader6, null);
			}
		}
		else if(event.equalsIgnoreCase("leader6circle8") && leader6 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader6circle8", 60000, leader6, null);
			}
			else
			{
				leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader6.setRunning();
				startQuestTimer("leader6circle1", 8000, leader6, null);
			}
		}
		else if(event.equalsIgnoreCase("leader7spawn") && leader7 == null)
		{
			leader7 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader7.setIsNoRndWalk(true);
			leader7.setRunning();
			startQuestTimer("leader7circle1", 5000, leader7, null);
			startQuestTimer("Check400", 1500, leader7, null);
			startQuestTimer("control_minions", 5000, leader7, null);
		}
		else if(event.equalsIgnoreCase("leader7circle1") && leader7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader7circle1", 60000, leader7, null);
			}
			else
			{
				leader7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader7.setRunning();
				startQuestTimer("leader7circle2", 9000, leader7, null);
			}
		}
		else if(event.equalsIgnoreCase("leader7circle2") && leader7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader7circle2", 60000, leader7, null);
			}
			else
			{
				leader7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader7.setRunning();
				startQuestTimer("leader7circle3", 10000, leader7, null);
			}
		}
		else if(event.equalsIgnoreCase("leader7circle3") && leader7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader7circle3", 60000, leader7, null);
			}
			else
			{
				leader7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader7.setRunning();
				startQuestTimer("leader7circle4", 6000, leader7, null);
			}
		}
		else if(event.equalsIgnoreCase("leader7circle4") && leader7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader7circle4", 60000, leader7, null);
			}
			else
			{
				leader7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader7.setRunning();
				startQuestTimer("leader7circle5", 11000, leader7, null);
			}
		}
		else if(event.equalsIgnoreCase("leader7circle5") && leader7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader7circle5", 60000, leader7, null);
			}
			else
			{
				leader7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader7.setRunning();
				startQuestTimer("leader7circle6", 7000, leader7, null);
			}
		}
		else if(event.equalsIgnoreCase("leader7circle6") && leader7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader7circle6", 60000, leader7, null);
			}
			else
			{
				leader7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader7.setRunning();
				startQuestTimer("leader7circle7", 12000, leader7, null);
			}
		}
		else if(event.equalsIgnoreCase("leader7circle7") && leader7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader7circle7", 60000, leader7, null);
			}
			else
			{
				leader7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader7.setRunning();
				startQuestTimer("leader7circle8", 8000, leader7, null);
			}
		}
		else if(event.equalsIgnoreCase("leader7circle8") && leader7 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader7circle8", 60000, leader7, null);
			}
			else
			{
				leader7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader7.setRunning();
				startQuestTimer("leader7circle1", 8000, leader7, null);
			}
		}
		else if(event.equalsIgnoreCase("leader8spawn") && leader8 == null)
		{
			leader8 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader8.setIsNoRndWalk(true);
			leader8.setRunning();
			startQuestTimer("leader8circle1", 5000, leader8, null);
			startQuestTimer("Check400", 1500, leader8, null);
			startQuestTimer("control_minions", 5000, leader8, null);
		}
		else if(event.equalsIgnoreCase("leader8circle1") && leader8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader8circle1", 60000, leader8, null);
			}
			else
			{
				leader8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader8.setRunning();
				startQuestTimer("leader8circle2", 9000, leader8, null);
			}
		}
		else if(event.equalsIgnoreCase("leader8circle2") && leader8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader8circle2", 60000, leader8, null);
			}
			else
			{
				leader8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader8.setRunning();
				startQuestTimer("leader8circle3", 10000, leader8, null);
			}
		}
		else if(event.equalsIgnoreCase("leader8circle3") && leader8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader8circle3", 60000, leader8, null);
			}
			else
			{
				leader8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader8.setRunning();
				startQuestTimer("leader8circle4", 6000, leader8, null);
			}
		}
		else if(event.equalsIgnoreCase("leader8circle4") && leader8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader8circle4", 60000, leader8, null);
			}
			else
			{
				leader8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader8.setRunning();
				startQuestTimer("leader8circle5", 11000, leader8, null);
			}
		}
		else if(event.equalsIgnoreCase("leader8circle5") && leader8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader8circle5", 60000, leader8, null);
			}
			else
			{
				leader8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader8.setRunning();
				startQuestTimer("leader8circle6", 7000, leader8, null);
			}
		}
		else if(event.equalsIgnoreCase("leader8circle6") && leader8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader8circle6", 60000, leader8, null);
			}
			else
			{
				leader8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader8.setRunning();
				startQuestTimer("leader8circle7", 12000, leader8, null);
			}
		}
		else if(event.equalsIgnoreCase("leader8circle7") && leader8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader8circle7", 60000, leader8, null);
			}
			else
			{
				leader8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader8.setRunning();
				startQuestTimer("leader8circle8", 8000, leader8, null);
			}
		}
		else if(event.equalsIgnoreCase("leader8circle8") && leader8 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader8circle8", 60000, leader8, null);
			}
			else
			{
				leader8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader8.setRunning();
				startQuestTimer("leader8circle1", 8000, leader8, null);
			}
		}
		else if(event.equalsIgnoreCase("leader9spawn") && leader9 == null)
		{
			leader9 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader9.setIsNoRndWalk(true);
			leader9.setRunning();
			startQuestTimer("leader9circle1", 5000, leader9, null);
			startQuestTimer("Check400", 1500, leader9, null);
			startQuestTimer("control_minions", 5000, leader9, null);
		}
		else if(event.equalsIgnoreCase("leader9circle1") && leader9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader9circle1", 60000, leader9, null);
			}
			else
			{
				leader9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader9.setRunning();
				startQuestTimer("leader9circle2", 9000, leader9, null);
			}
		}
		else if(event.equalsIgnoreCase("leader9circle2") && leader9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader9circle2", 60000, leader9, null);
			}
			else
			{
				leader9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader9.setRunning();
				startQuestTimer("leader9circle3", 10000, leader9, null);
			}
		}
		else if(event.equalsIgnoreCase("leader9circle3") && leader9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader9circle3", 60000, leader9, null);
			}
			else
			{
				leader9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader9.setRunning();
				startQuestTimer("leader9circle4", 6000, leader9, null);
			}
		}
		else if(event.equalsIgnoreCase("leader9circle4") && leader9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader9circle4", 60000, leader9, null);
			}
			else
			{
				leader9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader9.setRunning();
				startQuestTimer("leader9circle5", 11000, leader9, null);
			}
		}
		else if(event.equalsIgnoreCase("leader9circle5") && leader9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader9circle5", 60000, leader9, null);
			}
			else
			{
				leader9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader9.setRunning();
				startQuestTimer("leader9circle6", 7000, leader9, null);
			}
		}
		else if(event.equalsIgnoreCase("leader9circle6") && leader9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader9circle6", 60000, leader9, null);
			}
			else
			{
				leader9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader9.setRunning();
				startQuestTimer("leader9circle7", 12000, leader9, null);
			}
		}
		else if(event.equalsIgnoreCase("leader9circle7") && leader9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader9circle7", 60000, leader9, null);
			}
			else
			{
				leader9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader9.setRunning();
				startQuestTimer("leader9circle8", 8000, leader9, null);
			}
		}
		else if(event.equalsIgnoreCase("leader9circle8") && leader9 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader9circle8", 60000, leader9, null);
			}
			else
			{
				leader9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader9.setRunning();
				startQuestTimer("leader9circle1", 8000, leader9, null);
			}
		}
		else if(event.equalsIgnoreCase("leader10spawn") && leader10 == null)
		{
			leader10 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader10.setIsNoRndWalk(true);
			leader10.setRunning();
			startQuestTimer("leader10circle1", 5000, leader10, null);
			startQuestTimer("Check400", 1500, leader10, null);
			startQuestTimer("control_minions", 5000, leader10, null);
		}
		else if(event.equalsIgnoreCase("leader10circle1") && leader10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader10circle1", 60000, leader10, null);
			}
			else
			{
				leader10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader10.setRunning();
				startQuestTimer("leader10circle2", 9000, leader10, null);
			}
		}
		else if(event.equalsIgnoreCase("leader10circle2") && leader10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader10circle2", 60000, leader10, null);
			}
			else
			{
				leader10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader10.setRunning();
				startQuestTimer("leader10circle3", 10000, leader10, null);
			}
		}
		else if(event.equalsIgnoreCase("leader10circle3") && leader10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader10circle3", 60000, leader10, null);
			}
			else
			{
				leader10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader10.setRunning();
				startQuestTimer("leader10circle4", 6000, leader10, null);
			}
		}
		else if(event.equalsIgnoreCase("leader10circle4") && leader10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader10circle4", 60000, leader10, null);
			}
			else
			{
				leader10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader10.setRunning();
				startQuestTimer("leader10circle5", 11000, leader10, null);
			}
		}
		else if(event.equalsIgnoreCase("leader10circle5") && leader10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader10circle5", 60000, leader10, null);
			}
			else
			{
				leader10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader10.setRunning();
				startQuestTimer("leader10circle6", 7000, leader10, null);
			}
		}
		else if(event.equalsIgnoreCase("leader10circle6") && leader10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader10circle6", 60000, leader10, null);
			}
			else
			{
				leader10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader10.setRunning();
				startQuestTimer("leader10circle7", 12000, leader10, null);
			}
		}
		else if(event.equalsIgnoreCase("leader10circle7") && leader10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader10circle7", 60000, leader10, null);
			}
			else
			{
				leader10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader10.setRunning();
				startQuestTimer("leader10circle8", 8000, leader10, null);
			}
		}
		else if(event.equalsIgnoreCase("leader10circle8") && leader10 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader10circle8", 60000, leader10, null);
			}
			else
			{
				leader10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader10.setRunning();
				startQuestTimer("leader10circle1", 8000, leader10, null);
			}
		}
		else if(event.equalsIgnoreCase("leader11spawn") && leader11 == null)
		{
			leader11 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader11.setIsNoRndWalk(true);
			leader11.setRunning();
			startQuestTimer("leader11circle1", 5000, leader11, null);
			startQuestTimer("Check400", 1500, leader11, null);
			startQuestTimer("control_minions", 5000, leader11, null);
		}
		else if(event.equalsIgnoreCase("leader11circle1") && leader11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader11circle1", 60000, leader11, null);
			}
			else
			{
				leader11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader11.setRunning();
				startQuestTimer("leader11circle2", 9000, leader11, null);
			}
		}
		else if(event.equalsIgnoreCase("leader11circle2") && leader11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader11circle2", 60000, leader11, null);
			}
			else
			{
				leader11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader11.setRunning();
				startQuestTimer("leader11circle3", 10000, leader11, null);
			}
		}
		else if(event.equalsIgnoreCase("leader11circle3") && leader11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader11circle3", 60000, leader11, null);
			}
			else
			{
				leader11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader11.setRunning();
				startQuestTimer("leader11circle4", 6000, leader11, null);
			}
		}
		else if(event.equalsIgnoreCase("leader11circle4") && leader11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader11circle4", 60000, leader11, null);
			}
			else
			{
				leader11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader11.setRunning();
				startQuestTimer("leader11circle5", 11000, leader11, null);
			}
		}
		else if(event.equalsIgnoreCase("leader11circle5") && leader11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader11circle5", 60000, leader11, null);
			}
			else
			{
				leader11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader11.setRunning();
				startQuestTimer("leader11circle6", 7000, leader11, null);
			}
		}
		else if(event.equalsIgnoreCase("leader11circle6") && leader11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader11circle6", 60000, leader11, null);
			}
			else
			{
				leader11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader11.setRunning();
				startQuestTimer("leader11circle7", 12000, leader11, null);
			}
		}
		else if(event.equalsIgnoreCase("leader11circle7") && leader11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader11circle7", 60000, leader11, null);
			}
			else
			{
				leader11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader11.setRunning();
				startQuestTimer("leader11circle8", 8000, leader11, null);
			}
		}
		else if(event.equalsIgnoreCase("leader11circle8") && leader11 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader11circle8", 60000, leader11, null);
			}
			else
			{
				leader11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader11.setRunning();
				startQuestTimer("leader11circle1", 8000, leader11, null);
			}
		}
		else if(event.equalsIgnoreCase("leader12spawn") && leader12 == null)
		{
			leader12 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader12.setIsNoRndWalk(true);
			leader12.setRunning();
			startQuestTimer("leader12circle1", 5000, leader12, null);
			startQuestTimer("Check400", 1500, leader12, null);
			startQuestTimer("control_minions", 5000, leader12, null);
		}
		else if(event.equalsIgnoreCase("leader12circle1") && leader12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader12circle1", 60000, leader12, null);
			}
			else
			{
				leader12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader12.setRunning();
				startQuestTimer("leader12circle2", 9000, leader12, null);
			}
		}
		else if(event.equalsIgnoreCase("leader12circle2") && leader12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader12circle2", 60000, leader12, null);
			}
			else
			{
				leader12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader12.setRunning();
				startQuestTimer("leader12circle3", 10000, leader12, null);
			}
		}
		else if(event.equalsIgnoreCase("leader12circle3") && leader12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader12circle3", 60000, leader12, null);
			}
			else
			{
				leader12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader12.setRunning();
				startQuestTimer("leader12circle4", 6000, leader12, null);
			}
		}
		else if(event.equalsIgnoreCase("leader12circle4") && leader12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader12circle4", 60000, leader12, null);
			}
			else
			{
				leader12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader12.setRunning();
				startQuestTimer("leader12circle5", 11000, leader12, null);
			}
		}
		else if(event.equalsIgnoreCase("leader12circle5") && leader12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader12circle5", 60000, leader12, null);
			}
			else
			{
				leader12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader12.setRunning();
				startQuestTimer("leader12circle6", 7000, leader12, null);
			}
		}
		else if(event.equalsIgnoreCase("leader12circle6") && leader12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader12circle6", 60000, leader12, null);
			}
			else
			{
				leader12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader12.setRunning();
				startQuestTimer("leader12circle7", 12000, leader12, null);
			}
		}
		else if(event.equalsIgnoreCase("leader12circle7") && leader12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader12circle7", 60000, leader12, null);
			}
			else
			{
				leader12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader12.setRunning();
				startQuestTimer("leader12circle8", 8000, leader12, null);
			}
		}
		else if(event.equalsIgnoreCase("leader12circle8") && leader12 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader12circle8", 60000, leader12, null);
			}
			else
			{
				leader12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader12.setRunning();
				startQuestTimer("leader12circle1", 8000, leader12, null);
			}
		}
		else if(event.equalsIgnoreCase("leader13spawn") && leader13 == null)
		{
			leader13 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader13.setIsNoRndWalk(true);
			leader13.setRunning();
			startQuestTimer("leader13circle1", 5000, leader13, null);
			startQuestTimer("Check400", 1500, leader13, null);
			startQuestTimer("control_minions", 5000, leader13, null);
		}
		else if(event.equalsIgnoreCase("leader13circle1") && leader13 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader13circle1", 60000, leader13, null);
			}
			else
			{
				leader13.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader13.setRunning();
				startQuestTimer("leader13circle2", 9000, leader13, null);
			}
		}
		else if(event.equalsIgnoreCase("leader13circle2") && leader13 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader13circle2", 60000, leader13, null);
			}
			else
			{
				leader13.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader13.setRunning();
				startQuestTimer("leader13circle3", 10000, leader13, null);
			}
		}
		else if(event.equalsIgnoreCase("leader13circle3") && leader13 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader13circle3", 60000, leader13, null);
			}
			else
			{
				leader13.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader13.setRunning();
				startQuestTimer("leader13circle4", 6000, leader13, null);
			}
		}
		else if(event.equalsIgnoreCase("leader13circle4") && leader13 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader13circle4", 60000, leader13, null);
			}
			else
			{
				leader13.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader13.setRunning();
				startQuestTimer("leader13circle5", 11000, leader13, null);
			}
		}
		else if(event.equalsIgnoreCase("leader13circle5") && leader13 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader13circle5", 60000, leader13, null);
			}
			else
			{
				leader13.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader13.setRunning();
				startQuestTimer("leader13circle6", 7000, leader13, null);
			}
		}
		else if(event.equalsIgnoreCase("leader13circle6") && leader13 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader13circle6", 60000, leader13, null);
			}
			else
			{
				leader13.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader13.setRunning();
				startQuestTimer("leader13circle7", 12000, leader13, null);
			}
		}
		else if(event.equalsIgnoreCase("leader13circle7") && leader13 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader13circle7", 60000, leader13, null);
			}
			else
			{
				leader13.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader13.setRunning();
				startQuestTimer("leader13circle8", 8000, leader13, null);
			}
		}
		else if(event.equalsIgnoreCase("leader13circle8") && leader13 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader13circle8", 60000, leader13, null);
			}
			else
			{
				leader13.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader13.setRunning();
				startQuestTimer("leader13circle1", 8000, leader13, null);
			}
		}
		else if(event.equalsIgnoreCase("leader14spawn") && leader14 == null)
		{
			leader14 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader14.setIsNoRndWalk(true);
			leader14.setRunning();
			startQuestTimer("leader14circle1", 5000, leader14, null);
			startQuestTimer("Check400", 1500, leader14, null);
			startQuestTimer("control_minions", 5000, leader14, null);
		}
		else if(event.equalsIgnoreCase("leader14circle1") && leader14 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader14circle1", 60000, leader14, null);
			}
			else
			{
				leader14.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader14.setRunning();
				startQuestTimer("leader14circle2", 9000, leader14, null);
			}
		}
		else if(event.equalsIgnoreCase("leader14circle2") && leader14 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader14circle2", 60000, leader14, null);
			}
			else
			{
				leader14.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader14.setRunning();
				startQuestTimer("leader14circle3", 10000, leader14, null);
			}
		}
		else if(event.equalsIgnoreCase("leader14circle3") && leader14 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader14circle3", 60000, leader14, null);
			}
			else
			{
				leader14.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader14.setRunning();
				startQuestTimer("leader14circle4", 6000, leader14, null);
			}
		}
		else if(event.equalsIgnoreCase("leader14circle4") && leader14 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader14circle4", 60000, leader14, null);
			}
			else
			{
				leader14.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader14.setRunning();
				startQuestTimer("leader14circle5", 11000, leader14, null);
			}
		}
		else if(event.equalsIgnoreCase("leader14circle5") && leader14 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader14circle5", 60000, leader14, null);
			}
			else
			{
				leader14.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader14.setRunning();
				startQuestTimer("leader14circle6", 7000, leader14, null);
			}
		}
		else if(event.equalsIgnoreCase("leader14circle6") && leader14 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader14circle6", 60000, leader14, null);
			}
			else
			{
				leader14.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader14.setRunning();
				startQuestTimer("leader14circle7", 12000, leader14, null);
			}
		}
		else if(event.equalsIgnoreCase("leader14circle7") && leader14 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader14circle7", 60000, leader14, null);
			}
			else
			{
				leader14.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader14.setRunning();
				startQuestTimer("leader14circle8", 8000, leader14, null);
			}
		}
		else if(event.equalsIgnoreCase("leader14circle8") && leader14 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader14circle8", 60000, leader14, null);
			}
			else
			{
				leader14.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader14.setRunning();
				startQuestTimer("leader14circle1", 8000, leader14, null);
			}
		}
		else if(event.equalsIgnoreCase("leader15spawn") && leader15 == null)
		{
			leader15 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader15.setIsNoRndWalk(true);
			leader15.setRunning();
			startQuestTimer("leader15circle1", 5000, leader15, null);
			startQuestTimer("Check400", 1500, leader15, null);
			startQuestTimer("control_minions", 5000, leader15, null);
		}
		else if(event.equalsIgnoreCase("leader15circle1") && leader15 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader15circle1", 60000, leader15, null);
			}
			else
			{
				leader15.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader15.setRunning();
				startQuestTimer("leader15circle2", 9000, leader15, null);
			}
		}
		else if(event.equalsIgnoreCase("leader15circle2") && leader15 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader15circle2", 60000, leader15, null);
			}
			else
			{
				leader15.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader15.setRunning();
				startQuestTimer("leader15circle3", 10000, leader15, null);
			}
		}
		else if(event.equalsIgnoreCase("leader15circle3") && leader15 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader15circle3", 60000, leader15, null);
			}
			else
			{
				leader15.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader15.setRunning();
				startQuestTimer("leader15circle4", 6000, leader15, null);
			}
		}
		else if(event.equalsIgnoreCase("leader15circle4") && leader15 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader15circle4", 60000, leader15, null);
			}
			else
			{
				leader15.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader15.setRunning();
				startQuestTimer("leader15circle5", 11000, leader15, null);
			}
		}
		else if(event.equalsIgnoreCase("leader15circle5") && leader15 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader15circle5", 60000, leader15, null);
			}
			else
			{
				leader15.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader15.setRunning();
				startQuestTimer("leader15circle6", 7000, leader15, null);
			}
		}
		else if(event.equalsIgnoreCase("leader15circle6") && leader15 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader15circle6", 60000, leader15, null);
			}
			else
			{
				leader15.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader15.setRunning();
				startQuestTimer("leader15circle7", 12000, leader15, null);
			}
		}
		else if(event.equalsIgnoreCase("leader15circle7") && leader15 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader15circle7", 60000, leader15, null);
			}
			else
			{
				leader15.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader15.setRunning();
				startQuestTimer("leader15circle8", 8000, leader15, null);
			}
		}
		else if(event.equalsIgnoreCase("leader15circle8") && leader15 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader15circle8", 60000, leader15, null);
			}
			else
			{
				leader15.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader15.setRunning();
				startQuestTimer("leader15circle1", 8000, leader15, null);
			}
		}
		else if(event.equalsIgnoreCase("leader16spawn") && leader16 == null)
		{
			leader16 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader16.setIsNoRndWalk(true);
			leader16.setRunning();
			startQuestTimer("leader16circle1", 5000, leader16, null);
			startQuestTimer("Check400", 1500, leader16, null);
			startQuestTimer("control_minions", 5000, leader16, null);
		}
		else if(event.equalsIgnoreCase("leader16circle1") && leader16 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader16circle1", 60000, leader16, null);
			}
			else
			{
				leader16.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader16.setRunning();
				startQuestTimer("leader16circle2", 9000, leader16, null);
			}
		}
		else if(event.equalsIgnoreCase("leader16circle2") && leader16 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader16circle2", 60000, leader16, null);
			}
			else
			{
				leader16.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader16.setRunning();
				startQuestTimer("leader16circle3", 10000, leader16, null);
			}
		}
		else if(event.equalsIgnoreCase("leader16circle3") && leader16 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader16circle3", 60000, leader16, null);
			}
			else
			{
				leader16.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader16.setRunning();
				startQuestTimer("leader16circle4", 6000, leader16, null);
			}
		}
		else if(event.equalsIgnoreCase("leader16circle4") && leader16 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader16circle4", 60000, leader16, null);
			}
			else
			{
				leader16.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader16.setRunning();
				startQuestTimer("leader16circle5", 11000, leader16, null);
			}
		}
		else if(event.equalsIgnoreCase("leader16circle5") && leader16 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader16circle5", 60000, leader16, null);
			}
			else
			{
				leader16.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader16.setRunning();
				startQuestTimer("leader16circle6", 7000, leader16, null);
			}
		}
		else if(event.equalsIgnoreCase("leader16circle6") && leader16 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader16circle6", 60000, leader16, null);
			}
			else
			{
				leader16.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader16.setRunning();
				startQuestTimer("leader16circle7", 12000, leader16, null);
			}
		}
		else if(event.equalsIgnoreCase("leader16circle7") && leader16 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader16circle7", 60000, leader16, null);
			}
			else
			{
				leader16.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader16.setRunning();
				startQuestTimer("leader16circle8", 8000, leader16, null);
			}
		}
		else if(event.equalsIgnoreCase("leader16circle8") && leader16 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader16circle8", 60000, leader16, null);
			}
			else
			{
				leader16.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader16.setRunning();
				startQuestTimer("leader16circle1", 8000, leader16, null);
			}
		}
		else if(event.equalsIgnoreCase("leader17spawn") && leader17 == null)
		{
			leader17 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader17.setIsNoRndWalk(true);
			leader17.setRunning();
			startQuestTimer("leader17circle1", 5000, leader17, null);
			startQuestTimer("Check400", 1500, leader17, null);
			startQuestTimer("control_minions", 5000, leader17, null);
		}
		else if(event.equalsIgnoreCase("leader17circle1") && leader17 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader17circle1", 60000, leader17, null);
			}
			else
			{
				leader17.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader17.setRunning();
				startQuestTimer("leader17circle2", 9000, leader17, null);
			}
		}
		else if(event.equalsIgnoreCase("leader17circle2") && leader17 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader17circle2", 60000, leader17, null);
			}
			else
			{
				leader17.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader17.setRunning();
				startQuestTimer("leader17circle3", 10000, leader17, null);
			}
		}
		else if(event.equalsIgnoreCase("leader17circle3") && leader17 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader17circle3", 60000, leader17, null);
			}
			else
			{
				leader17.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader17.setRunning();
				startQuestTimer("leader17circle4", 6000, leader17, null);
			}
		}
		else if(event.equalsIgnoreCase("leader17circle4") && leader17 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader17circle4", 60000, leader17, null);
			}
			else
			{
				leader17.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader17.setRunning();
				startQuestTimer("leader17circle5", 11000, leader17, null);
			}
		}
		else if(event.equalsIgnoreCase("leader17circle5") && leader17 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader17circle5", 60000, leader17, null);
			}
			else
			{
				leader17.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader17.setRunning();
				startQuestTimer("leader17circle6", 7000, leader17, null);
			}
		}
		else if(event.equalsIgnoreCase("leader17circle6") && leader17 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader17circle6", 60000, leader17, null);
			}
			else
			{
				leader17.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader17.setRunning();
				startQuestTimer("leader17circle7", 12000, leader17, null);
			}
		}
		else if(event.equalsIgnoreCase("leader17circle7") && leader17 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader17circle7", 60000, leader17, null);
			}
			else
			{
				leader17.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader17.setRunning();
				startQuestTimer("leader17circle8", 8000, leader17, null);
			}
		}
		else if(event.equalsIgnoreCase("leader17circle8") && leader17 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader17circle8", 60000, leader17, null);
			}
			else
			{
				leader17.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader17.setRunning();
				startQuestTimer("leader17circle1", 8000, leader17, null);
			}
		}
		else if(event.equalsIgnoreCase("leader18spawn") && leader18 == null)
		{
			leader18 = addSpawn(LEADER, 146563, 116289, -3724, 0, false, 0);
			leader18.setIsNoRndWalk(true);
			leader18.setRunning();
			startQuestTimer("leader18circle1", 5000, leader18, null);
			startQuestTimer("Check400", 1500, leader18, null);
			startQuestTimer("control_minions", 5000, leader18, null);
		}
		else if(event.equalsIgnoreCase("leader18circle1") && leader18 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader18circle1", 60000, leader18, null);
			}
			else
			{
				leader18.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_2);
				leader18.setRunning();
				startQuestTimer("leader18circle2", 9000, leader18, null);
			}
		}
		else if(event.equalsIgnoreCase("leader18circle2") && leader18 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader18circle2", 60000, leader18, null);
			}
			else
			{
				leader18.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_3);
				leader18.setRunning();
				startQuestTimer("leader18circle3", 10000, leader18, null);
			}
		}
		else if(event.equalsIgnoreCase("leader18circle3") && leader18 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader18circle3", 60000, leader18, null);
			}
			else
			{
				leader18.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_4);
				leader18.setRunning();
				startQuestTimer("leader18circle4", 6000, leader18, null);
			}
		}
		else if(event.equalsIgnoreCase("leader18circle4") && leader18 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader18circle4", 60000, leader18, null);
			}
			else
			{
				leader18.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_5);
				leader18.setRunning();
				startQuestTimer("leader18circle5", 11000, leader18, null);
			}
		}
		else if(event.equalsIgnoreCase("leader18circle5") && leader18 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader18circle5", 60000, leader18, null);
			}
			else
			{
				leader18.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_6);
				leader18.setRunning();
				startQuestTimer("leader18circle6", 7000, leader18, null);
			}
		}
		else if(event.equalsIgnoreCase("leader18circle6") && leader18 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader18circle6", 60000, leader18, null);
			}
			else
			{
				leader18.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_7);
				leader18.setRunning();
				startQuestTimer("leader18circle7", 12000, leader18, null);
			}
		}
		else if(event.equalsIgnoreCase("leader18circle7") && leader18 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader18circle7", 60000, leader18, null);
			}
			else
			{
				leader18.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_8);
				leader18.setRunning();
				startQuestTimer("leader18circle8", 8000, leader18, null);
			}
		}
		else if(event.equalsIgnoreCase("leader18circle8") && leader18 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("leader18circle8", 60000, leader18, null);
			}
			else
			{
				leader18.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CIRCLE_1);
				leader18.setRunning();
				startQuestTimer("leader18circle1", 8000, leader18, null);
			}
		}
		else if(event.equalsIgnoreCase("control_minions"))
		{
			if(((L2MonsterInstance) npc).hasMinions())
			{
				List<L2MonsterInstance> minions = ((L2MonsterInstance) npc).getMinionList().getSpawnedMinions();
				minions.stream().filter(minion -> !minion.isCastingNow() && !minion.isAttackingNow() && !minion.isInCombat()).forEach(minion -> {
					minion.setRunning();
					minion.setisReturningToSpawnPoint(false);
					minion.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, npc);
				});
			}
			startQuestTimer("control_minions", 10000, npc, null);
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(npc == null)
		{
			return super.onKill(npc, killer, isPet);
		}
		if(npc.getNpcId() == KNORIKS)
		{
			if(npc.equals(knoriks1))
			{
				startQuestTimer("knoriks1spawn", 300000, null, null);
				knoriks1 = null;
			}
			else if(npc.equals(knoriks2))
			{
				startQuestTimer("knoriks2spawn", 300000, null, null);
				knoriks2 = null;
			}
			else if(npc.equals(knoriks3))
			{
				startQuestTimer("knoriks3spawn", 300000, null, null);
				knoriks3 = null;
			}
			else if(npc.equals(knoriks4))
			{
				startQuestTimer("knoriks4spawn", 300000, null, null);
				knoriks4 = null;
			}
			else if(npc.equals(knoriks5))
			{
				startQuestTimer("knoriks5spawn", 300000, null, null);
				knoriks5 = null;
			}
			else if(npc.equals(knoriks6))
			{
				startQuestTimer("knoriks6spawn", 300000, null, null);
				knoriks6 = null;
			}
			else if(npc.equals(knoriks7))
			{
				startQuestTimer("knoriks7spawn", 300000, null, null);
				knoriks7 = null;
			}
			else if(npc.equals(knoriks8))
			{
				startQuestTimer("knoriks8spawn", 300000, null, null);
				knoriks8 = null;
			}
			else if(npc.equals(knoriks9))
			{
				startQuestTimer("knoriks9spawn", 300000, null, null);
				knoriks9 = null;
			}
			else if(npc.equals(knoriks10))
			{
				startQuestTimer("knoriks10spawn", 300000, null, null);
				knoriks10 = null;
			}
			else if(npc.equals(knoriks11))
			{
				startQuestTimer("knoriks11spawn", 300000, null, null);
				knoriks11 = null;
			}
			else if(npc.equals(knoriks12))
			{
				startQuestTimer("knoriks12spawn", 300000, null, null);
				knoriks12 = null;
			}
		}
		else if(npc.getNpcId() == LEADER)
		{
			if(npc.equals(leader1))
			{
				startQuestTimer("leader1spawn", 60000, null, null);
				leader1 = null;
			}
			else if(npc.equals(leader2))
			{
				startQuestTimer("leader2spawn", 60000, null, null);
				leader2 = null;
			}
			else if(npc.equals(leader3))
			{
				startQuestTimer("leader3spawn", 60000, null, null);
				leader3 = null;
			}
			else if(npc.equals(leader4))
			{
				startQuestTimer("leader4spawn", 60000, null, null);
				leader4 = null;
			}
			else if(npc.equals(leader5))
			{
				startQuestTimer("leader5spawn", 60000, null, null);
				leader5 = null;
			}
			else if(npc.equals(leader6))
			{
				startQuestTimer("leader6spawn", 60000, null, null);
				leader6 = null;
			}
			else if(npc.equals(leader7))
			{
				startQuestTimer("leader7spawn", 60000, null, null);
				leader7 = null;
			}
			else if(npc.equals(leader8))
			{
				startQuestTimer("leader8spawn", 60000, null, null);
				leader8 = null;
			}
			else if(npc.equals(leader9))
			{
				startQuestTimer("leader9spawn", 60000, null, null);
				leader9 = null;
			}
			else if(npc.equals(leader10))
			{
				startQuestTimer("leader10spawn", 60000, null, null);
				leader10 = null;
			}
			else if(npc.equals(leader11))
			{
				startQuestTimer("leader11spawn", 60000, null, null);
				leader11 = null;
			}
			else if(npc.equals(leader12))
			{
				startQuestTimer("leader12spawn", 60000, null, null);
				leader12 = null;
			}
			else if(npc.equals(leader13))
			{
				startQuestTimer("leader13spawn", 60000, null, null);
				leader13 = null;
			}
			else if(npc.equals(leader14))
			{
				startQuestTimer("leader14spawn", 60000, null, null);
				leader14 = null;
			}
			else if(npc.equals(leader15))
			{
				startQuestTimer("leader15spawn", 60000, null, null);
				leader15 = null;
			}
			else if(npc.equals(leader16))
			{
				startQuestTimer("leader16spawn", 60000, null, null);
				leader16 = null;
			}
			else if(npc.equals(leader17))
			{
				startQuestTimer("leader17spawn", 60000, null, null);
				leader17 = null;
			}
			else if(npc.equals(leader18))
			{
				startQuestTimer("leader18spawn", 60000, null, null);
				leader18 = null;
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(!npc.isCastingNow() && !npc.isAttackingNow() && !npc.isInCombat() && !player.isDead())
		{
			((L2Attackable) npc).addDamageHate(player, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			npc.enableAllSkills();
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance)
		{
			if(zone.getId() == LoAzone)
			{
				PlayersInZone.add((L2PcInstance) character);
			}
		}
		return super.onEnterZone(character, zone);
	}

	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance)
		{
			if(zone.getId() == LoAzone)
			{
				PlayersInZone.remove(character);
			}
		}
		return super.onExitZone(character, zone);
	}
}