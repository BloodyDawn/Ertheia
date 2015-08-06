package dwo.gameserver.network.game.clientpackets.packet.Commission;

import dwo.gameserver.instancemanager.CommissionManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.commission.ExCloseCommission;

import static dwo.gameserver.instancemanager.CommissionManager.Window.Sell;

/**
 * L2GOD Team
 * User: Keiichi,Bacek
 * Date: 22.07.2011
 * Time: 23:58:25
 */

public class RequestCommissionRegisteredItem extends L2GameClientPacket
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

		CommissionManager.getInstance().showPlayerLots(cha, Sell, 0, -1, -1, "");
	}

	@Override
	public String getType()
	{
		return "[C] DO:A3 RequestCommissionRegisteredItem";
	}
}
