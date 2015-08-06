package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 05.10.12
 * Time: 15:33
 */
public class ExShowLines extends L2GameServerPacket
{
	protected int _x;
	protected int _y;
	protected int _z;

	public ExShowLines(L2PcInstance cha)
	{
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
	}

	@Override
	protected void writeImpl()
	{
		// hdccc
		/* Кол-во линий */
		writeH(0);
		/* Толщина линии */
		writeD(2);
		/* Управление цветом */ // 200 200 256 - желтый
		writeC(200);
		writeC(200);
		writeC(256);
		/* Координаты */
		writeD(_x);
		writeD(_y);
		writeD(_z);

		writeD(0x00);  //без понятия что енто такое
	}
}
