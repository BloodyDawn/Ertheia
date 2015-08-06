package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExPledgeEmblem extends L2GameServerPacket
{
	private int _crestId;
	private int _pledgeId;
	private int _order;
	private byte[] _data;

	public ExPledgeEmblem(int pledgeId, int crestId, int order, byte[] data)
	{
		_pledgeId = pledgeId;
		_crestId = crestId;
		_order = order;
		_data = data;
	}

	@Override
	protected void writeImpl()
	{
		// ddd ddd
		writeD(200);            // ServerIdx
		writeD(_pledgeId);    // PledgeId
		writeD(_crestId);        // CrestId
		writeD(_order);        // Emblem_order
		writeD(65664);            // TotalBmpSize
		writeD(_data.length);    // Size
		writeB(_data);
	}
}