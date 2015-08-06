package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.packet.commission.ExShowCommission;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.09.12
 * Time: 15:00
 */

public class ComissionBroker extends Quest
{
	private static final int[] ComissionBrokers = {
		33417, 33418, 33447, 33528, 33529, 33530, 33531, 33532, 33533, 33534, 33551, 33552
	};

	public ComissionBroker()
	{
		addAskId(ComissionBrokers, -10303);
	}

	public static void main(String[] args)
	{
		new ComissionBroker();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 1)
		{
			player.tempInventoryDisable();
			player.sendPacket(new ExShowCommission());
			player.sendActionFailed();
		}
		return null;
	}
}