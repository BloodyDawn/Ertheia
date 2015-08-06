package dwo.gameserver.network.game.serverpackets.packet.lobby;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class CharacterCreateSuccess extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeD(0x01);
	}
}
