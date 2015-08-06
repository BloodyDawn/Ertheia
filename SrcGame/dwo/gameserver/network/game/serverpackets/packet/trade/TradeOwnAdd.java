package dwo.gameserver.network.game.serverpackets.packet.trade;

import dwo.gameserver.model.items.TradeItem;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class TradeOwnAdd extends L2GameServerPacket
{
	private TradeItem _item;

	public TradeOwnAdd(TradeItem item)
	{
		_item = item;
	}

	@Override
	protected void writeImpl()
	{
		writeH(0x01);
		writeTradeItem(_item);
	}
}
