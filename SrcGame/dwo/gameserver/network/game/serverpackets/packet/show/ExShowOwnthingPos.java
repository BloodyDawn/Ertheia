package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

@Deprecated
public class ExShowOwnthingPos extends L2GameServerPacket
{

	@Override
	protected void writeImpl()
	{
		writeD(0);
	}
}
