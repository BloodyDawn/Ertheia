package dwo.gameserver.network.game.serverpackets.packet.commission;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExShowCommission extends L2GameServerPacket
{

	@Override
	protected void writeImpl()
	{
		writeD(0x01); // 1 - open commision shop
	}
}
