package dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle;

import dwo.gameserver.model.actor.instance.L2ShuttleInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: ANZO,Bacek
 * Date: 16.09.11
 * Time: 22:04
 */

public class ExShuttuleInfo extends L2GameServerPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _ElevatorId;
	private int[] _type = new int[5];

	public ExShuttuleInfo(L2ShuttleInstance shuttle)
	{
		_x = shuttle.getX();
		_y = shuttle.getY();
		_z = shuttle.getZ();
		_ElevatorId = shuttle.getId();
		_type = shuttle.getType();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_ElevatorId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(0x00);
		writeD(_ElevatorId);
		writeD(0x02);

		if(_ElevatorId == 1 || _ElevatorId == 2)
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(-50);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(200);
			writeD(45);
			writeD(_type[0]);
			writeD(_type[1]);
			writeD(_type[2]);
			writeD(0x00);
			writeD(0x00);
			writeD(-50);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(-200);
			writeD(45);
			writeD(_type[3]);
			writeD(_type[4]);
		}

		if(_ElevatorId == 3)
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(-115);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(_type[0]);
			writeD(_type[1]);
			writeD(_type[2]);
			writeD(0x00);
			writeD(0x00);
			writeD(-115);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(_type[3]);
			writeD(_type[4]);
		}
	}
}