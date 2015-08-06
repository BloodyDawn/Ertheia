package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

public class TurekOrcFootman extends Quest
{
	private static final int TUREKF = 20499;

	public TurekOrcFootman()
	{
		addAttackId(TUREKF);
	}

	public static void main(String[] args)
	{
		new TurekOrcFootman();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.getNpcId() == TUREKF)
		{
			if(npc.getAiVar("firstAttacked") != null)
			{
				if(Rnd.getChance(20))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "There is no reason for you to kill me! I have nothing you need!"));
				}
			}
			else
			{
				npc.setAiVar("firstAttacked", 1);
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "We shall see about that!"));
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
}