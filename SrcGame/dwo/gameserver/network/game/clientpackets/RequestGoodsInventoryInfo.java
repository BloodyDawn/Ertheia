package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExGoodsInventoryResult;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.10.11
 * Time: 8:27
 */

public class RequestGoodsInventoryInfo extends L2GameClientPacket
{
	private int unk1;

	@Override
	protected void readImpl()
	{
		unk1 = readC();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		/*
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("RequestGoodsInventoryInfo"))
		{
			return;
		}

		if (activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(new ExGoodsInventoryResult(ExGoodsInventoryResult.SYS_NOT_TRADE));
			return;
		}

		if (activeChar.getPremiumItemList().isEmpty())
		{
			activeChar.sendPacket(new ExGoodsInventoryResult(ExGoodsInventoryResult.SYS_NO_ITEMS));
			return;
		}
		activeChar.sendPacket(new ExGoodsInventoryInfo(activeChar.getPremiumItemList()));
		*/
		activeChar.sendPacket(new ExGoodsInventoryResult(ExGoodsInventoryResult.SYS_NO_ITEMS));
	}

	@Override
	public String getType()
	{
		return "[C] D0:B1 RequestGoodsInventoryInfo";
	}
}
