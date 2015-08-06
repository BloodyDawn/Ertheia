package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.cache.CrestCache;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PledgeCrest extends L2GameServerPacket
{
	private final int _crestId;
	private final byte[] _data;

	public PledgeCrest(int crestId)
	{
		_crestId = crestId;
		_data = CrestCache.getInstance().getPledgeCrest(_crestId);
	}

	@Override
	protected void writeImpl()
	{
		writeD(200); // приаритет
		writeD(_crestId);
		if(_data != null)
		{
			writeD(_data.length);
			writeB(_data);
		}
		else
		{
			writeD(0);
		}
	}
}
