package dwo.scripts.ai.zone;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Util;
import javolution.util.FastList;

import java.util.List;

public class DragonValleyScouts extends Quest
{
	private static final int MESMER = 22820;
	private static final int DRAGON = 22832;
	private static final int SAND = 22833;
	private static final int DUST = 22834;
	private static final int HOWL = 22859;
	//positions
	private static final Location MESMER1_1 = new Location(85217, 112145, -3062, 0);
	private static final Location MESMER1_2 = new Location(83813, 112763, -3054, 0);
	private static final Location MESMER1_3 = new Location(82557, 113417, -3070, 0);
	private static final Location MESMER1_4 = new Location(81877, 114310, -3241, 0);
	private static final Location MESMER1_5 = new Location(79994, 114552, -3726, 0);
	private static final Location MESMER2_1 = new Location(86712, 107490, -3157, 0);
	private static final Location MESMER2_2 = new Location(87901, 106346, -3186, 0);
	private static final Location MESMER2_3 = new Location(89855, 106920, -3187, 0);
	private static final Location MESMER2_4 = new Location(88495, 107883, -3061, 0);
	private static final Location MESMER3_1 = new Location(83251, 118020, -3005, 0);
	private static final Location MESMER3_2 = new Location(83026, 119405, -3036, 0);
	private static final Location MESMER3_3 = new Location(84656, 120628, -2995, 0);
	private static final Location MESMER3_4 = new Location(86269, 120257, -3016, 0);
	private static final Location MESMER3_5 = new Location(85353, 118701, -3046, 0);
	private static final Location HOWL1_1 = new Location(85471, 107857, -3266, 0);
	private static final Location HOWL1_2 = new Location(83971, 108829, -3114, 0);
	private static final Location HOWL1_3 = new Location(81532, 109327, -3079, 0);
	private static final Location HOWL1_4 = new Location(79913, 110522, -3003, 0);
	private static final Location HOWL2_1 = new Location(86470, 111975, -3229, 0);
	private static final Location HOWL2_2 = new Location(88216, 112408, -3295, 0);
	private static final Location HOWL2_3 = new Location(90150, 112893, -3011, 0);
	private static final Location HOWL2_4 = new Location(91815, 111887, -2989, 0);
	private static final Location HOWL2_5 = new Location(93661, 112881, -3062, 0);
	private static final Location HOWL3_1 = new Location(88634, 120573, -3059, 0);
	private static final Location HOWL3_2 = new Location(90387, 120144, -3048, 0);
	private static final Location HOWL3_3 = new Location(91849, 120036, -2934, 0);
	private static final Location HOWL3_4 = new Location(93150, 118864, -2984, 0);
	private static final Location HOWL3_5 = new Location(91350, 118136, -3061, 0);
	private static final Location HOWL3_6 = new Location(89661, 119026, -3061, 0);
	private static final Location HOWL3_7 = new Location(87791, 119537, -3061, 0);
	private static final Location DRAGON1_1 = new Location(116435, 114779, -3074, 0);
	private static final Location DRAGON1_2 = new Location(115549, 114332, -3092, 0);
	private static final Location DRAGON1_3 = new Location(113395, 115131, -3212, 0);
	private static final Location DRAGON1_4 = new Location(111958, 116609, -3033, 0);
	private static final Location DRAGON1_5 = new Location(113427, 117342, -3155, 0);
	private static final Location DRAGON1_6 = new Location(113427, 116226, -3217, 0);
	private static final Location DRAGON2_1 = new Location(116570, 109961, -3039, 0);
	private static final Location DRAGON2_2 = new Location(113960, 110239, -3042, 0);
	private static final Location DRAGON2_3 = new Location(113635, 113101, -3025, 0);
	private static final Location DRAGON2_4 = new Location(112686, 113013, -2780, 0);
	private static final Location DRAGON2_5 = new Location(110731, 112921, -2802, 0);
	private static final Location DRAGON3_1 = new Location(126397, 112708, -3460, 0);
	private static final Location DRAGON3_2 = new Location(124880, 111644, -3140, 0);
	private static final Location DRAGON3_3 = new Location(124530, 109107, -3073, 0);
	private static final Location DRAGON3_4 = new Location(122161, 108210, -2980, 0);
	private static final Location SAND1_1 = new Location(121919, 109431, -2892, 0);
	private static final Location SAND1_2 = new Location(120312, 108365, -2963, 0);
	private static final Location SAND1_3 = new Location(118446, 109742, -2960, 0);
	private static final Location SAND1_4 = new Location(119085, 110740, -3053, 0);
	private static final Location SAND1_5 = new Location(120708, 109664, -3018, 0);
	private static final Location SAND2_1 = new Location(112031, 118624, -3097, 0);
	private static final Location SAND2_2 = new Location(113701, 117022, -3141, 0);
	private static final Location SAND2_3 = new Location(111589, 116815, -3025, 0);
	private static final Location DUST1_1 = new Location(115832, 109832, -3041, 0);
	private static final Location DUST1_2 = new Location(117081, 110901, -3009, 0);
	private static final Location DUST1_3 = new Location(118087, 110446, -2975, 0);
	private static final Location DUST1_4 = new Location(117358, 109806, -2948, 0);
	private static final Location DUST2_1 = new Location(88605, 108604, -3031, 0);
	private static final Location DUST2_2 = new Location(90169, 108614, -3042, 0);
	private static final Location DUST2_3 = new Location(91637, 107756, -3061, 0);
	private static final Location DUST2_4 = new Location(89901, 107344, -3121, 0);
	private static final Location DUST2_5 = new Location(88074, 107683, -3074, 0);
	private static final Location DUST3_1 = new Location(94410, 107622, -3035, 0);
	private static final Location DUST3_2 = new Location(95986, 107871, -3149, 0);
	private static final Location DUST3_3 = new Location(97177, 109531, -3683, 0);
	private static final Location DUST3_4 = new Location(96450, 110359, -3726, 0);
	private static final Location DUST3_5 = new Location(94764, 109893, -3714, 0);
	private static final Location DUST4_1 = new Location(109438, 114358, -3078, 0);
	private static final Location DUST4_2 = new Location(109139, 116196, -3098, 0);
	private static final Location DUST4_3 = new Location(108362, 117856, -3060, 0);
	private static final Location DUST4_4 = new Location(106247, 118078, -3049, 0);
	private static final Location DUST4_5 = new Location(104919, 117450, -3061, 0);
	private static int DVzone = 46002;
	// Zone stuff
	public List<L2PcInstance> PlayersInZone = new FastList<>();
	// Npcs
	private L2Npc mesmer1;
	private L2Npc mesmer2;
	private L2Npc mesmer3;
	private L2Npc howl1;
	private L2Npc howl2;
	private L2Npc howl3;
	private L2Npc dragon1;
	private L2Npc dragon2;
	private L2Npc dragon3;
	private L2Npc sand1;
	private L2Npc sand2;
	private L2Npc dust1;
	private L2Npc dust2;
	private L2Npc dust3;
	private L2Npc dust4;

	public DragonValleyScouts()
	{
		addKillId(MESMER);
		addKillId(HOWL);
		addKillId(DRAGON);
		addKillId(SAND);
		addKillId(DUST);
		addAggroRangeEnterId(MESMER);
		addAggroRangeEnterId(HOWL);
		addAggroRangeEnterId(DRAGON);
		addAggroRangeEnterId(SAND);
		addAggroRangeEnterId(DUST);
		addEnterZoneId(DVzone);
		addExitZoneId(DVzone);

		startQuestTimer("mesmer1spawn", 6000, null, null);
		startQuestTimer("mesmer2spawn", 6000, null, null);
		startQuestTimer("mesmer3spawn", 6000, null, null);
		startQuestTimer("howl1spawn", 6000, null, null);
		startQuestTimer("howl2spawn", 6000, null, null);
		startQuestTimer("howl3spawn", 6000, null, null);
		startQuestTimer("dragon1spawn", 6000, null, null);
		startQuestTimer("dragon2spawn", 6000, null, null);
		startQuestTimer("dragon3spawn", 6000, null, null);
		startQuestTimer("sand1spawn", 6000, null, null);
		startQuestTimer("sand2spawn", 6000, null, null);
		startQuestTimer("dust1spawn", 6000, null, null);
		startQuestTimer("dust2spawn", 6000, null, null);
		startQuestTimer("dust3spawn", 6000, null, null);
		startQuestTimer("dust4spawn", 6000, null, null);
	}

	public static void main(String[] args)
	{
		new DragonValleyScouts();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("Check400"))
		{
			PlayersInZone.stream().filter(pc -> Util.checkIfInRange(400, npc, pc, true)).forEach(pc -> {
				if(!npc.isCastingNow() && !npc.isAttackingNow() && !npc.isInCombat() && !pc.isDead())
				{
					((L2Attackable) npc).attackCharacter(pc);
				}
			});
			startQuestTimer("Check400", 1500, npc, null);
		}
		else if(event.equalsIgnoreCase("mesmer1spawn") && mesmer1 == null)
		{
			mesmer1 = addSpawn(MESMER, 85137, 111995, -3083, 0, false, 0);
			mesmer1.setIsNoRndWalk(true);
			mesmer1.setRunning();
			startQuestTimer("mesmer1move1", 5000, mesmer1, null);
			startQuestTimer("Check400", 1500, mesmer1, null);
		}
		else if(event.equalsIgnoreCase("mesmer1move1") && mesmer1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer1move1", 60000, mesmer1, null);
			}
			else
			{
				mesmer1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER1_2);
				mesmer1.setRunning();
				startQuestTimer("mesmer1move15", 10000, mesmer1, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer1move15") && mesmer1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer1move15", 60000, mesmer1, null);
			}
			else
			{
				mesmer1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER1_3);
				mesmer1.setRunning();
				startQuestTimer("mesmer1move2", 10000, mesmer1, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer1move2") && mesmer1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer1move2", 60000, mesmer1, null);
			}
			else
			{
				mesmer1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER1_4);
				mesmer1.setRunning();
				startQuestTimer("mesmer1move3", 8000, mesmer1, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer1move3") && mesmer1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer1move3", 60000, mesmer1, null);
			}
			else
			{
				mesmer1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER1_5);
				mesmer1.setRunning();
				startQuestTimer("mesmer1move4", 13000, mesmer1, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer1move4") && mesmer1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer1move4", 60000, mesmer1, null);
			}
			else
			{
				mesmer1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER1_4);
				mesmer1.setRunning();
				startQuestTimer("mesmer1move5", 13000, mesmer1, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer1move5") && mesmer1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer1move5", 60000, mesmer1, null);
			}
			else
			{
				mesmer1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER1_3);
				mesmer1.setRunning();
				startQuestTimer("mesmer1move6", 8000, mesmer1, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer1move6") && mesmer1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer1move6", 60000, mesmer1, null);
			}
			else
			{
				mesmer1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER1_2);
				mesmer1.setRunning();
				startQuestTimer("mesmer1move7", 10000, mesmer1, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer1move7") && mesmer1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer1move7", 60000, mesmer1, null);
			}
			else
			{
				mesmer1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER1_1);
				mesmer1.setRunning();
				startQuestTimer("mesmer1move1", 10000, mesmer1, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer2spawn") && mesmer2 == null)
		{
			mesmer2 = addSpawn(MESMER, 86712, 107490, -3157, 0, false, 0);
			mesmer2.setIsNoRndWalk(true);
			mesmer2.setRunning();
			startQuestTimer("mesmer2move1", 5000, mesmer2, null);
			startQuestTimer("Check400", 1500, mesmer2, null);
		}
		else if(event.equalsIgnoreCase("mesmer2move1") && mesmer2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer2move1", 60000, mesmer2, null);
			}
			else
			{
				mesmer2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER2_2);
				mesmer2.setRunning();
				startQuestTimer("mesmer2move2", 11000, mesmer2, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer2move2") && mesmer2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer2move2", 60000, mesmer2, null);
			}
			else
			{
				mesmer2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER2_3);
				mesmer2.setRunning();
				startQuestTimer("mesmer2move3", 12000, mesmer2, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer2move3") && mesmer2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer2move3", 60000, mesmer2, null);
			}
			else
			{
				mesmer2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER2_4);
				mesmer2.setRunning();
				startQuestTimer("mesmer2move4", 11000, mesmer2, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer2move4") && mesmer2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer2move4", 60000, mesmer2, null);
			}
			else
			{
				mesmer2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER2_1);
				mesmer2.setRunning();
				startQuestTimer("mesmer2move1", 11000, mesmer2, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer3spawn") && mesmer3 == null)
		{
			mesmer3 = addSpawn(MESMER, 83251, 118020, -3005, 0, false, 0);
			mesmer3.setIsNoRndWalk(true);
			mesmer3.setRunning();
			startQuestTimer("mesmer3move1", 5000, mesmer3, null);
			startQuestTimer("Check400", 1500, mesmer3, null);
		}
		else if(event.equalsIgnoreCase("mesmer3move1") && mesmer3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer3move1", 60000, mesmer3, null);
			}
			else
			{
				mesmer3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER3_2);
				mesmer3.setRunning();
				startQuestTimer("mesmer3move2", 9000, mesmer3, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer3move2") && mesmer3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer3move2", 60000, mesmer3, null);
			}
			else
			{
				mesmer3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER3_3);
				mesmer3.setRunning();
				startQuestTimer("mesmer3move3", 13000, mesmer3, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer3move3") && mesmer3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer3move3", 60000, mesmer3, null);
			}
			else
			{
				mesmer3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER3_4);
				mesmer3.setRunning();
				startQuestTimer("mesmer3move4", 10000, mesmer3, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer3move4") && mesmer3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer3move4", 60000, mesmer3, null);
			}
			else
			{
				mesmer3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER3_5);
				mesmer3.setRunning();
				startQuestTimer("mesmer3move5", 12000, mesmer3, null);
			}
		}
		else if(event.equalsIgnoreCase("mesmer3move5") && mesmer3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("mesmer3move5", 60000, mesmer3, null);
			}
			else
			{
				mesmer3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MESMER3_1);
				mesmer3.setRunning();
				startQuestTimer("mesmer3move1", 14000, mesmer3, null);
			}
		}
		else if(event.equalsIgnoreCase("howl1spawn") && howl1 == null)
		{
			howl1 = addSpawn(HOWL, 85471, 107857, -3266, 0, false, 0);
			howl1.setIsNoRndWalk(true);
			howl1.setRunning();
			startQuestTimer("howl1move1", 5000, howl1, null);
			startQuestTimer("Check400", 1500, howl1, null);
		}
		else if(event.equalsIgnoreCase("howl1move1") && howl1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl1move1", 60000, howl1, null);
			}
			else
			{
				howl1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL1_2);
				howl1.setRunning();
				startQuestTimer("howl1move2", 10000, howl1, null);
			}
		}
		else if(event.equalsIgnoreCase("howl1move2") && howl1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl1move2", 60000, howl1, null);
			}
			else
			{
				howl1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL1_3);
				howl1.setRunning();
				startQuestTimer("howl1move3", 13000, howl1, null);
			}
		}
		else if(event.equalsIgnoreCase("howl1move3") && howl1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl1move3", 60000, howl1, null);
			}
			else
			{
				howl1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL1_4);
				howl1.setRunning();
				startQuestTimer("howl1move4", 10000, howl1, null);
			}
		}
		else if(event.equalsIgnoreCase("howl1move4") && howl1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl1move4", 60000, howl1, null);
			}
			else
			{
				howl1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL1_3);
				howl1.setRunning();
				startQuestTimer("howl1move5", 10000, howl1, null);
			}
		}
		else if(event.equalsIgnoreCase("howl1move5") && howl1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl1move5", 60000, howl1, null);
			}
			else
			{
				howl1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL1_2);
				howl1.setRunning();
				startQuestTimer("howl1move6", 13000, howl1, null);
			}
		}
		else if(event.equalsIgnoreCase("howl1move6") && howl1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl1move6", 60000, howl1, null);
			}
			else
			{
				howl1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL1_1);
				howl1.setRunning();
				startQuestTimer("howl1move1", 10000, howl1, null);
			}
		}
		else if(event.equalsIgnoreCase("howl2spawn") && howl2 == null)
		{
			howl2 = addSpawn(HOWL, 86470, 111975, -3229, 0, false, 0);
			howl2.setIsNoRndWalk(true);
			howl2.setRunning();
			startQuestTimer("howl2move1", 5000, howl2, null);
			startQuestTimer("Check400", 1500, howl2, null);
		}
		else if(event.equalsIgnoreCase("howl2move1") && howl2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl2move1", 60000, howl2, null);
			}
			else
			{
				howl2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL2_2);
				howl2.setRunning();
				startQuestTimer("howl2move2", 10000, howl2, null);
			}
		}
		else if(event.equalsIgnoreCase("howl2move2") && howl2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl2move2", 60000, howl2, null);
			}
			else
			{
				howl2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL2_3);
				howl2.setRunning();
				startQuestTimer("howl2move3", 11000, howl2, null);
			}
		}
		else if(event.equalsIgnoreCase("howl2move3") && howl2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl2move3", 60000, howl2, null);
			}
			else
			{
				howl2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL2_4);
				howl2.setRunning();
				startQuestTimer("howl2move4", 10000, howl2, null);
			}
		}
		else if(event.equalsIgnoreCase("howl2move4") && howl2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl2move4", 60000, howl2, null);
			}
			else
			{
				howl2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL2_5);
				howl2.setRunning();
				startQuestTimer("howl2move5", 11000, howl2, null);
			}
		}
		else if(event.equalsIgnoreCase("howl2move5") && howl2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl2move5", 60000, howl2, null);
			}
			else
			{
				howl2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL2_4);
				howl2.setRunning();
				startQuestTimer("howl2move6", 11000, howl2, null);
			}
		}
		else if(event.equalsIgnoreCase("howl2move6") && howl2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl2move6", 60000, howl2, null);
			}
			else
			{
				howl2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL2_3);
				howl2.setRunning();
				startQuestTimer("howl2move7", 10000, howl2, null);
			}
		}
		else if(event.equalsIgnoreCase("howl2move7") && howl2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl2move7", 60000, howl2, null);
			}
			else
			{
				howl2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL2_2);
				howl2.setRunning();
				startQuestTimer("howl2move8", 11000, howl2, null);
			}
		}
		else if(event.equalsIgnoreCase("howl2move8") && howl2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl2move8", 60000, howl2, null);
			}
			else
			{
				howl2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL2_1);
				howl2.setRunning();
				startQuestTimer("howl2move1", 10000, howl2, null);
			}
		}
		else if(event.equalsIgnoreCase("howl3spawn") && howl3 == null)
		{
			howl3 = addSpawn(HOWL, 88634, 120573, -3059, 0, false, 0);
			howl3.setIsNoRndWalk(true);
			howl3.setRunning();
			startQuestTimer("howl3move1", 5000, howl3, null);
			startQuestTimer("Check400", 1500, howl3, null);
		}
		else if(event.equalsIgnoreCase("howl3move1") && howl3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl3move1", 60000, howl3, null);
			}
			else
			{
				howl3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL3_2);
				howl3.setRunning();
				startQuestTimer("howl3move2", 10000, howl3, null);
			}
		}
		else if(event.equalsIgnoreCase("howl3move2") && howl3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl3move2", 60000, howl3, null);
			}
			else
			{
				howl3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL3_3);
				howl3.setRunning();
				startQuestTimer("howl3move3", 8000, howl3, null);
			}
		}
		else if(event.equalsIgnoreCase("howl3move3") && howl3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl3move3", 60000, howl3, null);
			}
			else
			{
				howl3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL3_4);
				howl3.setRunning();
				startQuestTimer("howl3move4", 10000, howl3, null);
			}
		}
		else if(event.equalsIgnoreCase("howl3move4") && howl3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl3move4", 60000, howl3, null);
			}
			else
			{
				howl3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL3_5);
				howl3.setRunning();
				startQuestTimer("howl3move5", 10000, howl3, null);
			}
		}
		else if(event.equalsIgnoreCase("howl3move5") && howl3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl3move5", 60000, howl3, null);
			}
			else
			{
				howl3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL3_6);
				howl3.setRunning();
				startQuestTimer("howl3move6", 11000, howl3, null);
			}
		}
		else if(event.equalsIgnoreCase("howl3move6") && howl3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl3move6", 60000, howl3, null);
			}
			else
			{
				howl3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL3_7);
				howl3.setRunning();
				startQuestTimer("howl3move7", 10000, howl3, null);
			}
		}
		else if(event.equalsIgnoreCase("howl3move7") && howl3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("howl3move7", 60000, howl3, null);
			}
			else
			{
				howl3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, HOWL3_1);
				howl3.setRunning();
				startQuestTimer("howl3move1", 7000, howl3, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon1spawn") && dragon1 == null)
		{
			dragon1 = addSpawn(DRAGON, 88634, 120573, -3059, 0, false, 0);
			dragon1.setIsNoRndWalk(true);
			dragon1.setRunning();
			startQuestTimer("dragon1move1", 5000, dragon1, null);
			startQuestTimer("Check400", 1500, dragon1, null);
		}
		else if(event.equalsIgnoreCase("dragon1move1") && dragon1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon1move1", 60000, dragon1, null);
			}
			else
			{
				dragon1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON1_2);
				dragon1.setRunning();
				startQuestTimer("dragon1move2", 5000, dragon1, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon1move2") && dragon1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon1move2", 60000, dragon1, null);
			}
			else
			{
				dragon1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON1_3);
				dragon1.setRunning();
				startQuestTimer("dragon1move3", 10000, dragon1, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon1move3") && dragon1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon1move3", 60000, dragon1, null);
			}
			else
			{
				dragon1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON1_4);
				dragon1.setRunning();
				startQuestTimer("dragon1move4", 10000, dragon1, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon1move4") && dragon1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon1move4", 60000, dragon1, null);
			}
			else
			{
				dragon1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON1_5);
				dragon1.setRunning();
				startQuestTimer("dragon1move5", 7000, dragon1, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon1move5") && dragon1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon1move5", 60000, dragon1, null);
			}
			else
			{
				dragon1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON1_6);
				dragon1.setRunning();
				startQuestTimer("dragon1move6", 5000, dragon1, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon1move6") && dragon1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon1move6", 60000, dragon1, null);
			}
			else
			{
				dragon1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON1_1);
				dragon1.setRunning();
				startQuestTimer("dragon1move1", 15000, dragon1, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon2spawn") && dragon2 == null)
		{
			dragon2 = addSpawn(DRAGON, 116570, 109961, -3039, 0, false, 0);
			dragon2.setIsNoRndWalk(true);
			dragon2.setRunning();
			startQuestTimer("dragon2move1", 5000, dragon2, null);
			startQuestTimer("Check400", 1500, dragon2, null);
		}
		else if(event.equalsIgnoreCase("dragon2move1") && dragon2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon2move1", 60000, dragon2, null);
			}
			else
			{
				dragon2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON2_2);
				dragon2.setRunning();
				startQuestTimer("dragon2move2", 12000, dragon2, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon2move2") && dragon2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon2move2", 60000, dragon2, null);
			}
			else
			{
				dragon2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON2_3);
				dragon2.setRunning();
				startQuestTimer("dragon2move3", 13000, dragon2, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon2move3") && dragon2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon2move3", 60000, dragon2, null);
			}
			else
			{
				dragon2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON2_4);
				dragon2.setRunning();
				startQuestTimer("dragon2move4", 5000, dragon2, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon2move4") && dragon2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon2move4", 60000, dragon2, null);
			}
			else
			{
				dragon2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON2_5);
				dragon2.setRunning();
				startQuestTimer("dragon2move5", 10000, dragon2, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon2move5") && dragon2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon2move5", 60000, dragon2, null);
			}
			else
			{
				dragon2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON2_4);
				dragon2.setRunning();
				startQuestTimer("dragon2move6", 10000, dragon2, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon2move6") && dragon2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon2move6", 60000, dragon2, null);
			}
			else
			{
				dragon2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON2_3);
				dragon2.setRunning();
				startQuestTimer("dragon2move7", 5000, dragon2, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon2move7") && dragon2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon2move7", 60000, dragon2, null);
			}
			else
			{
				dragon2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON2_2);
				dragon2.setRunning();
				startQuestTimer("dragon2move8", 13000, dragon2, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon2move8") && dragon2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon2move8", 60000, dragon2, null);
			}
			else
			{
				dragon2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON2_1);
				dragon2.setRunning();
				startQuestTimer("dragon2move1", 12000, dragon2, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon3spawn") && dragon3 == null)
		{
			dragon3 = addSpawn(DRAGON, 126397, 112708, -3460, 0, false, 0);
			dragon3.setIsNoRndWalk(true);
			dragon3.setRunning();
			startQuestTimer("dragon3move1", 5000, dragon3, null);
			startQuestTimer("Check400", 1500, dragon3, null);
		}
		else if(event.equalsIgnoreCase("dragon3move1") && dragon3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon3move1", 60000, dragon3, null);
			}
			else
			{
				dragon3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON3_2);
				dragon3.setRunning();
				startQuestTimer("dragon3move2", 9000, dragon3, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon3move2") && dragon3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon3move2", 60000, dragon3, null);
			}
			else
			{
				dragon3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON3_3);
				dragon3.setRunning();
				startQuestTimer("dragon3move3", 12000, dragon3, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon3move3") && dragon3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon3move3", 60000, dragon3, null);
			}
			else
			{
				dragon3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON3_4);
				dragon3.setRunning();
				startQuestTimer("dragon3move4", 12000, dragon3, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon3move4") && dragon3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon3move4", 60000, dragon3, null);
			}
			else
			{
				dragon3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON3_3);
				dragon3.setRunning();
				startQuestTimer("dragon3move5", 12000, dragon3, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon3move5") && dragon3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon3move5", 60000, dragon3, null);
			}
			else
			{
				dragon3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON3_2);
				dragon3.setRunning();
				startQuestTimer("dragon3move6", 12000, dragon3, null);
			}
		}
		else if(event.equalsIgnoreCase("dragon3move6") && dragon3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dragon3move6", 60000, dragon3, null);
			}
			else
			{
				dragon3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DRAGON3_1);
				dragon3.setRunning();
				startQuestTimer("dragon3move1", 9000, dragon3, null);
			}
		}
		else if(event.equalsIgnoreCase("sand1spawn") && sand1 == null)
		{
			sand1 = addSpawn(SAND, 121919, 109431, -2892, 0, false, 0);
			sand1.setIsNoRndWalk(true);
			sand1.setRunning();
			startQuestTimer("sand1move1", 5000, sand1, null);
			startQuestTimer("Check400", 1500, sand1, null);
		}
		else if(event.equalsIgnoreCase("sand1move1") && sand1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("sand1move1", 60000, sand1, null);
			}
			else
			{
				sand1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, SAND1_2);
				sand1.setRunning();
				startQuestTimer("sand1move2", 7000, sand1, null);
			}
		}
		else if(event.equalsIgnoreCase("sand1move2") && sand1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("sand1move2", 60000, sand1, null);
			}
			else
			{
				sand1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, SAND1_3);
				sand1.setRunning();
				startQuestTimer("sand1move3", 10000, sand1, null);
			}
		}
		else if(event.equalsIgnoreCase("sand1move3") && sand1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("sand1move3", 60000, sand1, null);
			}
			else
			{
				sand1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, SAND1_4);
				sand1.setRunning();
				startQuestTimer("sand1move4", 5000, sand1, null);
			}
		}
		else if(event.equalsIgnoreCase("sand1move4") && sand1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("sand1move4", 60000, sand1, null);
			}
			else
			{
				sand1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, SAND1_5);
				sand1.setRunning();
				startQuestTimer("sand1move5", 7000, sand1, null);
			}
		}
		else if(event.equalsIgnoreCase("sand1move5") && sand1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("sand1move5", 60000, sand1, null);
			}
			else
			{
				sand1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, SAND1_1);
				sand1.setRunning();
				startQuestTimer("sand1move1", 5000, sand1, null);
			}
		}
		else if(event.equalsIgnoreCase("sand2spawn") && sand2 == null)
		{
			sand2 = addSpawn(SAND, 112031, 118624, -3097, 0, false, 0);
			sand2.setIsNoRndWalk(true);
			sand2.setRunning();
			startQuestTimer("sand2move1", 5000, sand2, null);
			startQuestTimer("Check400", 1500, sand2, null);
		}
		else if(event.equalsIgnoreCase("sand2move1") && sand2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("sand2move1", 60000, sand2, null);
			}
			else
			{
				sand2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, SAND2_2);
				sand2.setRunning();
				startQuestTimer("sand2move2", 9000, sand2, null);
			}
		}
		else if(event.equalsIgnoreCase("sand2move2") && sand2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("sand2move2", 60000, sand2, null);
			}
			else
			{
				sand2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, SAND2_3);
				sand2.setRunning();
				startQuestTimer("sand2move3", 10000, sand2, null);
			}
		}
		else if(event.equalsIgnoreCase("sand2move3") && sand2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("sand2move3", 60000, sand2, null);
			}
			else
			{
				sand2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, SAND2_1);
				sand2.setRunning();
				startQuestTimer("sand2move1", 9000, sand2, null);
			}
		}
		else if(event.equalsIgnoreCase("dust1spawn") && dust1 == null)
		{
			dust1 = addSpawn(DUST, 115832, 109832, -3041, 0, false, 0);
			dust1.setIsNoRndWalk(true);
			dust1.setRunning();
			startQuestTimer("dust1move1", 5000, dust1, null);
			startQuestTimer("Check400", 1500, dust1, null);
		}
		else if(event.equalsIgnoreCase("dust1move1") && dust1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust1move1", 60000, dust1, null);
			}
			else
			{
				dust1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST1_2);
				dust1.setRunning();
				startQuestTimer("dust1move2", 10000, dust1, null);
			}
		}
		else if(event.equalsIgnoreCase("dust1move2") && dust1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust1move2", 60000, dust1, null);
			}
			else
			{
				dust1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST1_3);
				dust1.setRunning();
				startQuestTimer("dust1move3", 5000, dust1, null);
			}
		}
		else if(event.equalsIgnoreCase("dust1move3") && dust1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust1move3", 60000, dust1, null);
			}
			else
			{
				dust1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST1_4);
				dust1.setRunning();
				startQuestTimer("dust1move4", 5000, dust1, null);
			}
		}
		else if(event.equalsIgnoreCase("dust1move4") && dust1 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust1move4", 60000, dust1, null);
			}
			else
			{
				dust1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST1_1);
				dust1.setRunning();
				startQuestTimer("dust1move1", 10000, dust1, null);
			}
		}
		else if(event.equalsIgnoreCase("dust2spawn") && dust2 == null)
		{
			dust2 = addSpawn(DUST, 88605, 108604, -3031, 0, false, 0);
			dust2.setIsNoRndWalk(true);
			dust2.setRunning();
			startQuestTimer("dust2move1", 5000, dust2, null);
			startQuestTimer("Check400", 1500, dust2, null);
		}
		else if(event.equalsIgnoreCase("dust2move1") && dust2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust2move1", 60000, dust2, null);
			}
			else
			{
				dust2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST2_2);
				dust2.setRunning();
				startQuestTimer("dust2move2", 10000, dust2, null);
			}
		}
		else if(event.equalsIgnoreCase("dust2move2") && dust2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust2move2", 60000, dust2, null);
			}
			else
			{
				dust2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST2_3);
				dust2.setRunning();
				startQuestTimer("dust2move3", 10000, dust2, null);
			}
		}
		else if(event.equalsIgnoreCase("dust2move3") && dust2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust2move3", 60000, dust2, null);
			}
			else
			{
				dust2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST2_4);
				dust2.setRunning();
				startQuestTimer("dust2move4", 10000, dust2, null);
			}
		}
		else if(event.equalsIgnoreCase("dust2move4") && dust2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust2move4", 60000, dust2, null);
			}
			else
			{
				dust2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST2_5);
				dust2.setRunning();
				startQuestTimer("dust2move5", 11000, dust2, null);
			}
		}
		else if(event.equalsIgnoreCase("dust2move5") && dust2 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust2move5", 60000, dust2, null);
			}
			else
			{
				dust2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST2_1);
				dust2.setRunning();
				startQuestTimer("dust2move1", 7000, dust2, null);
			}
		}
		else if(event.equalsIgnoreCase("dust3spawn") && dust3 == null)
		{
			dust3 = addSpawn(DUST, 94410, 107622, -3035, 0, false, 0);
			dust3.setIsNoRndWalk(true);
			dust3.setRunning();
			startQuestTimer("dust3move1", 5000, dust3, null);
			startQuestTimer("Check400", 1500, dust3, null);
		}
		else if(event.equalsIgnoreCase("dust3move1") && dust3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust3move1", 60000, dust3, null);
			}
			else
			{
				dust3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST3_2);
				dust3.setRunning();
				startQuestTimer("dust3move2", 10000, dust3, null);
			}
		}
		else if(event.equalsIgnoreCase("dust3move2") && dust3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust3move2", 60000, dust3, null);
			}
			else
			{
				dust3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST3_3);
				dust3.setRunning();
				startQuestTimer("dust3move3", 12000, dust3, null);
			}
		}
		else if(event.equalsIgnoreCase("dust3move3") && dust3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust3move3", 60000, dust3, null);
			}
			else
			{
				dust3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST3_4);
				dust3.setRunning();
				startQuestTimer("dust3move4", 5000, dust3, null);
			}
		}
		else if(event.equalsIgnoreCase("dust3move4") && dust3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust3move4", 60000, dust3, null);
			}
			else
			{
				dust3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST3_5);
				dust3.setRunning();
				startQuestTimer("dust3move5", 10000, dust3, null);
			}
		}
		else if(event.equalsIgnoreCase("dust3move5") && dust3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust3move5", 60000, dust3, null);
			}
			else
			{
				dust3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST3_4);
				dust3.setRunning();
				startQuestTimer("dust3move6", 10000, dust3, null);
			}
		}
		else if(event.equalsIgnoreCase("dust3move6") && dust3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust3move6", 60000, dust3, null);
			}
			else
			{
				dust3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST3_3);
				dust3.setRunning();
				startQuestTimer("dust3move7", 5000, dust3, null);
			}
		}
		else if(event.equalsIgnoreCase("dust3move7") && dust3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust3move7", 60000, dust3, null);
			}
			else
			{
				dust3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST3_2);
				dust3.setRunning();
				startQuestTimer("dust3move8", 12000, dust3, null);
			}
		}
		else if(event.equalsIgnoreCase("dust3move8") && dust3 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust3move8", 60000, dust3, null);
			}
			else
			{
				dust3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST3_1);
				dust3.setRunning();
				startQuestTimer("dust3move1", 10000, dust3, null);
			}
		}
		else if(event.equalsIgnoreCase("dust4spawn") && dust4 == null)
		{
			dust4 = addSpawn(DUST, 109438, 114358, -3078, 0, false, 0);
			dust4.setIsNoRndWalk(true);
			dust4.setRunning();
			startQuestTimer("dust4move1", 5000, dust4, null);
			startQuestTimer("Check400", 1500, dust4, null);
		}
		else if(event.equalsIgnoreCase("dust4move1") && dust4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust4move1", 60000, dust4, null);
			}
			else
			{
				dust4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST4_2);
				dust4.setRunning();
				startQuestTimer("dust4move2", 11000, dust4, null);
			}
		}
		else if(event.equalsIgnoreCase("dust4move2") && dust4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust4move2", 60000, dust4, null);
			}
			else
			{
				dust4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST4_3);
				dust4.setRunning();
				startQuestTimer("dust4move3", 11000, dust4, null);
			}
		}
		else if(event.equalsIgnoreCase("dust4move3") && dust4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust4move3", 60000, dust4, null);
			}
			else
			{
				dust4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST4_4);
				dust4.setRunning();
				startQuestTimer("dust4move4", 13000, dust4, null);
			}
		}
		else if(event.equalsIgnoreCase("dust4move4") && dust4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust4move4", 60000, dust4, null);
			}
			else
			{
				dust4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST4_5);
				dust4.setRunning();
				startQuestTimer("dust4move5", 9000, dust4, null);
			}
		}
		else if(event.equalsIgnoreCase("dust4move5") && dust4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust4move5", 60000, dust4, null);
			}
			else
			{
				dust4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST4_4);
				dust4.setRunning();
				startQuestTimer("dust4move6", 9000, dust4, null);
			}
		}
		else if(event.equalsIgnoreCase("dust4move6") && dust4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust4move6", 60000, dust4, null);
			}
			else
			{
				dust4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST4_3);
				dust4.setRunning();
				startQuestTimer("dust4move7", 13000, dust4, null);
			}
		}
		else if(event.equalsIgnoreCase("dust4move7") && dust4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust4move7", 60000, dust4, null);
			}
			else
			{
				dust4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST4_2);
				dust4.setRunning();
				startQuestTimer("dust4move8", 11000, dust4, null);
			}
		}
		else if(event.equalsIgnoreCase("dust4move8") && dust4 != null)
		{
			if(npc.isCastingNow() || npc.isAttackingNow() || npc.isInCombat())
			{
				startQuestTimer("dust4move8", 60000, dust4, null);
			}
			else
			{
				dust4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, DUST4_1);
				dust4.setRunning();
				startQuestTimer("dust4move1", 11000, dust4, null);
			}
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
		if(npc.getNpcId() == MESMER)
		{
			if(npc.equals(mesmer1))
			{
				startQuestTimer("mesmer1spawn", 300000, null, null);
				mesmer1 = null;
			}
			else if(npc.equals(mesmer2))
			{
				startQuestTimer("mesmer2spawn", 300000, null, null);
				mesmer2 = null;
			}
			else if(npc.equals(mesmer3))
			{
				startQuestTimer("mesmer3spawn", 300000, null, null);
				mesmer3 = null;
			}
		}
		else if(npc.getNpcId() == HOWL)
		{
			if(npc.equals(howl1))
			{
				startQuestTimer("howl1spawn", 300000, null, null);
				howl1 = null;
			}
			else if(npc.equals(howl2))
			{
				startQuestTimer("howl2spawn", 300000, null, null);
				howl2 = null;
			}
			else if(npc.equals(howl3))
			{
				startQuestTimer("howl3spawn", 300000, null, null);
				howl3 = null;
			}
		}
		else if(npc.getNpcId() == DRAGON)
		{
			if(npc.equals(dragon1))
			{
				startQuestTimer("dragon1spawn", 300000, null, null);
				dragon1 = null;
			}
			else if(npc.equals(dragon2))
			{
				startQuestTimer("dragon2spawn", 300000, null, null);
				dragon2 = null;
			}
			else if(npc.equals(dragon3))
			{
				startQuestTimer("dragon3spawn", 300000, null, null);
				dragon3 = null;
			}
		}
		else if(npc.getNpcId() == SAND)
		{
			if(npc.equals(sand1))
			{
				startQuestTimer("sand1spawn", 300000, null, null);
				sand1 = null;
			}
			else if(npc.equals(sand2))
			{
				startQuestTimer("sand2spawn", 300000, null, null);
				sand2 = null;
			}
		}
		else if(npc.getNpcId() == DUST)
		{
			if(npc.equals(dust1))
			{
				startQuestTimer("dust1spawn", 300000, null, null);
				dust1 = null;
			}
			else if(npc.equals(dust2))
			{
				startQuestTimer("dust2spawn", 300000, null, null);
				dust2 = null;
			}
			else if(npc.equals(dust3))
			{
				startQuestTimer("dust3spawn", 300000, null, null);
				dust3 = null;
			}
			else if(npc.equals(dust4))
			{
				startQuestTimer("dust4spawn", 300000, null, null);
				dust4 = null;
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
			if(zone.getId() == DVzone)
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
			if(zone.getId() == DVzone)
			{
				PlayersInZone.remove(character);
			}
		}
		return super.onExitZone(character, zone);
	}
}