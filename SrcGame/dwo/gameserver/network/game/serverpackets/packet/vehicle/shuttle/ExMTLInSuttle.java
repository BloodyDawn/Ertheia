package dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle;

import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.util.geometry.Point3D;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.07.11
 * Time: 15:27
 */

public class ExMTLInSuttle extends L2GameServerPacket
{
	private int _charObjId;
	private int _shuttleId;
	private Point3D _destination;
	private Point3D _origin;

	public ExMTLInSuttle(L2PcInstance player, Point3D destination, Point3D origin)
	{
		_charObjId = player.getObjectId();
		_shuttleId = player.getShuttle().getId();
		_destination = destination;
		_origin = origin;
	}

	public ExMTLInSuttle(L2PcInstance owner, L2Summon summon, Point3D destination, Point3D origin)
	{
		_charObjId = summon.getObjectId();
		_shuttleId = owner.getShuttle().getId();
		_destination = destination;
		_origin = origin;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		writeD(_shuttleId);
		writeD(_destination.getX());
		writeD(_destination.getY());
		writeD(_destination.getZ());
		writeD(_origin.getX());
		writeD(_origin.getY());
		writeD(_origin.getZ());
	}
}
