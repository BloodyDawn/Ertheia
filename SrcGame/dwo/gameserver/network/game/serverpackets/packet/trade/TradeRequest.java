package dwo.gameserver.network.game.serverpackets.packet.trade;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class TradeRequest extends L2GameServerPacket
{
	private int _senderID;

	public TradeRequest(int senderID)
	{
		_senderID = senderID;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_senderID);
	}
}
