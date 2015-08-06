package dwo.gameserver.network.game.clientpackets.packet.Commission;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.commission.ExCloseCommission;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 18.07.2011
 * Time: 0:00:48
 */

public class RequestCommissionCancel extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// ?
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance cha = getClient().getActiveChar();

		if(cha == null)
		{
			return;
		}

		L2Npc manager = cha.getLastFolkNPC();
		if(manager == null || !manager.canInteract(cha))
		{
			cha.sendPacket(new ExCloseCommission());
		}
	}

	@Override
	public String getType()
	{
		return "[C] DO:9E RequestCommissionCancel";
	}
}
