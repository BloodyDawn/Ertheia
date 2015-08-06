package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

public class NecromancerOfTheValley extends Quest
{
	public NecromancerOfTheValley()
	{
		addKillId(22858);
	}

	public static void main(String[] args)
	{
		new NecromancerOfTheValley();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(Rnd.getChance(30))
		{
			L2Character attacker = isPet ? killer.getPets().getFirst() : killer;
			// Exploding Orc Ghost
			L2Attackable explodingOrc = (L2Attackable) addSpawn(22818, npc.getX(), npc.getY(), npc.getZ() + 10, npc.getHeading(), false, 0, true);
			explodingOrc.setRunning();
			explodingOrc.addDamageHate(attacker, 0, 500);
			explodingOrc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);

			// Wrathful Orc Ghost
			L2Attackable wrathfulOrc = (L2Attackable) addSpawn(22819, npc.getX(), npc.getY(), npc.getZ() + 10, npc.getHeading(), false, 0, false);
			wrathfulOrc.setRunning();
			wrathfulOrc.addDamageHate(attacker, 0, 500);
			wrathfulOrc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);

		}
		return super.onKill(npc, killer, isPet);
	}
}