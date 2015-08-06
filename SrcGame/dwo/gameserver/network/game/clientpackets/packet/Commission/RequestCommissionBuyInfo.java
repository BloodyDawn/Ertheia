package dwo.gameserver.network.game.clientpackets.packet.Commission;

import dwo.gameserver.instancemanager.CommissionManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.CommissionItemHolder;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.commission.ExCloseCommission;
import dwo.gameserver.network.game.serverpackets.packet.commission.ExResponseCommissionBuyInfo;

/**
 * L2GOD Team
 * User: Keiichi, Bacek
 * Date: 24.07.2011
 * Time: 9:09:42
 */

public class RequestCommissionBuyInfo extends L2GameClientPacket
{
	private long _CommissionDBId;
	private int _ItemType;

	@Override
	protected void readImpl()
	{
		_CommissionDBId = readQ();    //номер лота
		_ItemType = readD();    //номер кнопки (строки в 1 вкладке)
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

		CommissionItemHolder holder = CommissionManager.getInstance().getCommissionLot(_CommissionDBId);
		if(holder != null)
		{
			cha.sendPacket(new ExResponseCommissionBuyInfo(holder));
		}
	}

	@Override
	public String getType()
	{
		return "[C] DO:A1 RequestCommissionBuyInfo";
	}
}
