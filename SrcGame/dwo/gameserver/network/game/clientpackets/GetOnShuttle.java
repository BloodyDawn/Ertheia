package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.vehicle.ShuttleManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2ShuttleInstance;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle.ExSuttleGetOn;
import dwo.gameserver.util.geometry.Point3D;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 05.02.12
 * Time: 20:19
 */

public class GetOnShuttle extends L2GameClientPacket
{
	private int _shuttleId;
	private Point3D _pos;

	@Override
	protected void readImpl()
	{
		int x;
		int y;
		int z;
		_shuttleId = readD();
		x = readD();
		y = readD();
		z = readD();
		_pos = new Point3D(x, y, z);
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2ShuttleInstance shuttle;
		if(activeChar.isInShuttle())
		{
			shuttle = activeChar.getShuttle();
			if(shuttle.getId() != _shuttleId)
			{
				activeChar.sendActionFailed();
				return;
			}
		}
		else
		{
			shuttle = ShuttleManager.getInstance().getShuttle(_shuttleId);
			if(shuttle == null || shuttle.isMoving() || !activeChar.isInsideRadius(shuttle, 1000, true, false))
			{
				activeChar.sendActionFailed();
				return;
			}
		}

		activeChar.setInVehiclePosition(_pos);
		shuttle.addPassenger(activeChar);
		activeChar.setVehicle(shuttle);
		activeChar.broadcastPacket(new ExSuttleGetOn(activeChar.getObjectId(), shuttle.getId(), _pos));
		activeChar.revalidateZone(true);
	}

	@Override
	public String getType()
	{
		return "[C] DO:7F RequestGetOnShuttle";
	}
}
