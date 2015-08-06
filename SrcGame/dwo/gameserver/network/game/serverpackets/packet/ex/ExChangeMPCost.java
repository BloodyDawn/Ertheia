package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExChangeMPCost extends L2GameServerPacket
{

	@Override
	protected void writeImpl()
	{
		writeD(0); // ?
		writeF(0); // ?
	}
}
