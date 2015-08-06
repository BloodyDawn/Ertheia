package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.instance.L2NpcInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

/**
 * @author Maxi
 */
public class KarulBugbear extends Quest
{
	private static final int KARUL = 20600;

	public KarulBugbear()
	{
		addAttackId(KARUL);
	}

	public static void main(String[] args)
	{
		new KarulBugbear();
	}

	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.getNpcId() == KARUL)
		{
			if(npc.getAiVar("firstAttacked") != null)
			{
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "Your rear is practically unguarded!"));
				}
			}
			else
			{
				npc.setAiVar("firstAttacked", 1);
				if(Rnd.getChance(2))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "Watch your back!"));
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
}
