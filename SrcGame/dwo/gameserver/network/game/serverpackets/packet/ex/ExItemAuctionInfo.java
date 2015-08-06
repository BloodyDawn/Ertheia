package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.items.ItemInfo;
import dwo.gameserver.model.items.itemauction.ItemAuction;
import dwo.gameserver.model.items.itemauction.ItemAuctionBid;
import dwo.gameserver.model.items.itemauction.ItemAuctionState;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author Forsaiken
 * Format: (cdqd)(dddqhhhdhhdddhhhhhhhhhhh)(ddd)(dddqhhhdhhdddhhhhhhhhhhh)
 */

public class ExItemAuctionInfo extends L2GameServerPacket
{
	private final boolean _refresh;
	private final int _timeRemaining;
	private final ItemAuction _currentAuction;
	private final ItemAuction _nextAuction;

	public ExItemAuctionInfo(boolean refresh, ItemAuction currentAuction, ItemAuction nextAuction)
	{
		if(currentAuction == null)
		{
			throw new NullPointerException();
		}

		_timeRemaining = currentAuction.getAuctionState() != ItemAuctionState.STARTED ? 0 : (int) (currentAuction.getFinishingTimeRemaining() / 1000);

		_refresh = refresh;
		_currentAuction = currentAuction;
		_nextAuction = nextAuction;
	}

	@Override
	protected void writeImpl()
	{
		writeC(_refresh ? 0x00 : 0x01);
		writeD(_currentAuction.getInstanceId());

		ItemAuctionBid highestBid = _currentAuction.getHighestBid();
		writeQ(highestBid != null ? highestBid.getLastBid() : _currentAuction.getAuctionInitBid());

		writeD(_timeRemaining);
		writeItemInfo(_currentAuction.getItemInfo());

		if(_nextAuction != null)
		{
			writeQ(_nextAuction.getAuctionInitBid());
			writeD((int) (_nextAuction.getStartingTime() / 1000)); // unix time in seconds
			writeItemInfo(_nextAuction.getItemInfo());
		}
	}
}