package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

public class TimakOrcOverlord extends Quest
{
	private static final int Timak_Orc_Overlord = 20588;

	public TimakOrcOverlord()
	{
		addAttackId(Timak_Orc_Overlord);
	}

	public static void main(String[] args)
	{
		new TimakOrcOverlord();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.getNpcId() == Timak_Orc_Overlord)
		{
			if(npc.getAiVar("firstAttacked") != null)
			{
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "Dear ultimate power!!!"));
				}
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "Now the real fight!"));
				}
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "Dear ultimate power!!!"));
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
