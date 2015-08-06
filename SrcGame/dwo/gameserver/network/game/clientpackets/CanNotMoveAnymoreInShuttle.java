package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle.ExStopMoveInShuttle;
import dwo.gameserver.util.geometry.Point3D;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 17.10.11
 * Time: 18:03
 */

public class CanNotMoveAnymoreInShuttle extends L2GameClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private int _shuttleId;

	@Override
	protected void readImpl()
	{
		_shuttleId = readD();
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		if(player.isInShuttle())
		{
			if(player.getShuttle().getId() == _shuttleId)
			{
				player.setInVehiclePosition(new Point3D(_x, _y, _z));
				player.getLocationController().setHeading(_heading);
				player.broadcastPacket(new ExStopMoveInShuttle(player, _shuttleId));
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] 82 CannotMoveAnymoreInShuttle";
	}
}
