package dwo.scripts.services;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.packet.variation.ExShowVariationCancelWindow;
import dwo.gameserver.network.game.serverpackets.packet.variation.ExShowVariationMakeWindow;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 04.10.12
 * Time: 22:54
 */

public class Augment extends Quest
{
	private static final int[] NPCs = {
		31960, 30688, 31583, 30678, 31271, 30317, 30898, 30298, 30458, 30300, 30471, 31990, 31316, 30846
	};

	public Augment()
	{
		addAskId(NPCs, -503);
	}

	public static void main(String[] args)
	{
		new Augment();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 100) // Вставка
		{
			player.sendPacket(new ExShowVariationMakeWindow());
		}
		else if(reply == 200) // Удаление
		{
			player.sendPacket(new ExShowVariationCancelWindow());
		}
		return null;
	}
}