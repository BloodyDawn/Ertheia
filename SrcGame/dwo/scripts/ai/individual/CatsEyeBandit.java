package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NS;

public class CatsEyeBandit extends Quest
{
	private static final int Cats_Eye_Bandit = 27038;

	public CatsEyeBandit()
	{
		addAttackId(Cats_Eye_Bandit);
		addKillId(Cats_Eye_Bandit);
	}

	public static void main(String[] args)
	{
		new CatsEyeBandit();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.getNpcId() == Cats_Eye_Bandit)
		{
			if(npc.getAiVar("firstAttacked") != null)
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "You childish fool, do you think you can catch me?"));
			}
			else
			{
				npc.setAiVar("firstAttacked", 1);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "I must do something about this shameful incident..."));
		return super.onKill(npc, killer, isPet);
	}
}
