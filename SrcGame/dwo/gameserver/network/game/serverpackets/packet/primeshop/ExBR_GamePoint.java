package dwo.gameserver.network.game.serverpackets.packet.primeshop;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExBR_GamePoint extends L2GameServerPacket
{
	private int _charId;
	private int _charPoints;

	public ExBR_GamePoint(L2PcInstance player)
	{
		_charId = player.getObjectId();
		_charPoints = player.getGamePoints();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charId);
		writeQ(_charPoints);
		writeD(0x00);
	}
}
