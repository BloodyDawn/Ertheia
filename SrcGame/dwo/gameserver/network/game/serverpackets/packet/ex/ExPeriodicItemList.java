package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExPeriodicItemList extends L2GameServerPacket
{
	private int _result;
	private int _objectID;
	private int _period;

	public ExPeriodicItemList(int result, int objectID, int period)
	{
		_result = result;
		_objectID = objectID;
		_period = period;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_result);
		writeD(_objectID);
		writeD(_period);
	}
}