package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

public class DeluLizardmanSpecialAgent extends Quest
{
	private static final int Delu_Lizardman_Special_Agent = 21105;

	public DeluLizardmanSpecialAgent()
	{
		addAttackId(Delu_Lizardman_Special_Agent);
	}

	public static void main(String[] args)
	{
		new DeluLizardmanSpecialAgent();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if(npc.getNpcId() == Delu_Lizardman_Special_Agent)
		{
			if(npc.getAiVar("firstAttacked") != null)
			{
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "Hey! Were having a duel here!"));
				}
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), player.getName() + "How dare you interrupt our fight! Hey guys, help!"));
				}
			}
			npc.setAiVar("firstAttacked", 1);
		}
		return super.onAttack(npc, player, damage, isPet);
	}
}
