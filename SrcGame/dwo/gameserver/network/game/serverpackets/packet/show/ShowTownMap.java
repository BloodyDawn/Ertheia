package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ShowTownMap extends L2GameServerPacket
{
	private String _texture;
	private int _x;
	private int _y;

	public ShowTownMap(String texture, int x, int y)
	{
		_texture = texture;
		_x = x;
		_y = y;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_texture);
		writeD(_x);
		writeD(_y);
	}
}
