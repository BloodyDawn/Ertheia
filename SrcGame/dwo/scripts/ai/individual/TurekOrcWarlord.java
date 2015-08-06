package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

public class TurekOrcWarlord extends Quest
{
	private static final int Turek_Orc_Warlord = 20495;

	public TurekOrcWarlord()
	{
		addAttackId(Turek_Orc_Warlord);
	}

	public static void main(String[] args)
	{
		new TurekOrcWarlord();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.getNpcId() == Turek_Orc_Warlord)
		{
			if(npc.getAiVar("firstAttacked") != null)
			{
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "You wont take me down easily."));
				}
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "The battle has just begun!"));
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