package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

public class DeluLizardmanSpecialCommander extends Quest
{
	private static final int Delu_Lizardman_Special_Commander = 21107;

	private static String[] text = {
		"Come on, Ill take you on!", "Is this the death of him!", "Despicable person! Go to hell!",
		"Foul! We mean by fast for me executed!", "Contest is over! We attack!"
	};

	private static String[] text1 = {
		"Come on, Ill take you on!", "How dare you interrupt a sacred duel! You must be taught a lesson!"
	};

	public DeluLizardmanSpecialCommander()
	{
		addAttackId(Delu_Lizardman_Special_Commander);
	}

	public static void main(String[] args)
	{
		new DeluLizardmanSpecialCommander();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if(npc.getNpcId() == Delu_Lizardman_Special_Commander)
		{
			if(npc.getAiVar("firstAttacked") != null)
			{
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), text[Rnd.get(4)]));
				}
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), player.getName() + text1[Rnd.get(1)]));
				}
			}
			npc.setAiVar("firstAttacked", 1);
		}
		return super.onAttack(npc, player, damage, isPet);
	}
}