package dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle;

import dwo.gameserver.model.actor.instance.L2ShuttleInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: ANZO,Bacek
 * Date: 16.09.11
 * Time: 22:05
 */

public class ExSuttleMove extends L2GameServerPacket
{
	private int _shuttleType;
	private int _x;
	private int _y;
	private int _z;
	private int _speed;

	public ExSuttleMove(L2ShuttleInstance shuttle)
	{
		_shuttleType = shuttle.getId();
		_x = shuttle.getXdestination();
		_y = shuttle.getYdestination();
		_z = shuttle.getZdestination();
		_speed = (int) shuttle.getStat().getMoveSpeed();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_shuttleType);
		writeD(_speed);
		writeD(0x00);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
