package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

/**
 * Ol Mahum General AI
 */
public class OlMahumGeneral extends Quest
{
	private static final int Ol_Mahum_General = 20438;

	public OlMahumGeneral()
	{
		addAttackId(Ol_Mahum_General);
	}

	public static void main(String[] args)
	{
		new OlMahumGeneral();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.getNpcId() == Ol_Mahum_General)
		{
			if(npc.getAiVar("firstAttacked") != null)
			{
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "We shall see about that!"));
				}
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "I will definitely repay this humiliation!"));
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
