package dwo.gameserver.network.game.clientpackets.packet.Commission;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.commission.ExCloseCommission;
import dwo.gameserver.network.game.serverpackets.packet.commission.ExResponseCommissionItemList;

/**
 * L2GOD Team
 * User: Keiichi, Bacek
 * Date: 17.07.2011
 * Time: 23:51:26
 */

public class RequestCommissionRegistrableItemList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
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
			return;
		}

		cha.sendPacket(new ExResponseCommissionItemList(cha));
	}

	@Override
	public String getType()
	{
		return "[C] DO:9B RequestCommissionRegistrableItemList";
	}
}
