package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.ItemAuctionManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemauction.ItemAuction;
import dwo.gameserver.model.items.itemauction.ItemAuctionInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.FloodAction;

public class RequestBidItemAuction extends L2GameClientPacket
{
	private int _instanceId;
	private long _bid;

	@Override
	protected void readImpl()
	{
		_instanceId = readD();
		_bid = readQ();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		// can't use auction fp here
		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.ITEM_AUCTION_BID))
		{
			activeChar.sendMessage("Вы ставите слишком часто.");
			return;
		}

		if(_bid < 0 || _bid > PcInventory.MAX_ADENA)
		{
			return;
		}

		ItemAuctionInstance instance = ItemAuctionManager.getInstance().getManagerInstance(_instanceId);
		if(instance != null)
		{
			ItemAuction auction = instance.getCurrentAuction();
			if(auction != null)
			{
				auction.registerBid(activeChar, _bid);
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:39 RequestBidItemAuction";
	}
}