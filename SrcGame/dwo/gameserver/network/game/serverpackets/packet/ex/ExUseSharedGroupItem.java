package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExUseSharedGroupItem extends L2GameServerPacket
{
	private final int _itemId;
	private final int _grpId;
	private final int _remainingTime;
	private final int _totalTime;

	public ExUseSharedGroupItem(int itemId, int grpId, long remainingTime, int totalTime)
	{
		_itemId = itemId;
		_grpId = grpId;
		_remainingTime = (int) (remainingTime / 1000);
		_totalTime = totalTime / 1000;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_itemId);
		writeD(_grpId);
		writeD(_remainingTime);
		writeD(_totalTime);
	}
}