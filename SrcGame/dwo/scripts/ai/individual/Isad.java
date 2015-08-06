package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.08.12
 * Time: 17:43
 * АИ лучников-охранников в Крепости Гильотины
 */

public class Isad extends Quest
{
	private static final int Isad = 25539;
	private static final int[] npcStrings = {1801739, 1801740};

	public Isad()
	{
		addAggroRangeEnterId(Isad);
	}

	public static void main(String[] args)
	{
		new Isad();
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), npcStrings[Rnd.get(npcStrings.length)]));
		return super.onAggroRangeEnter(npc, player, isPet);
	}
}