package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

public class BrekaOrcOverlord extends Quest
{
	private static final int BREKA = 20270;

	public BrekaOrcOverlord()
	{
		addAttackId(BREKA);
	}

	public static void main(String[] args)
	{
		new BrekaOrcOverlord();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.getNpcId() == BREKA)
		{
			if(npc.getAiVar("firstAttacked") != null)
			{
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "Extreme strength! ! ! !"));
				}
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "Humph, wanted to win me to be also in tender!"));
				}
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "Haven't thought to use this unique skill for this small thing!"));
				}
			}
			else
			{
				npc.setAiVar("firstAttacked", 1);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
}
