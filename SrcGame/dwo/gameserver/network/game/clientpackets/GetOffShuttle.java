package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.vehicle.ShuttleManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2ShuttleInstance;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle.ExStopMoveInShuttle;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle.ExSuttleGetOff;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 05.02.12
 * Time: 20:26
 */

public class GetOffShuttle extends L2GameClientPacket
{
	private int _shuttleId;
	private int _x;
	private int _y;
	private int _z;

	@Override
	protected void readImpl()
	{
		_shuttleId = readD();
		_x = readD();
		_y = readD();
		_z = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(!activeChar.isInShuttle() || activeChar.getShuttle().getId() != _shuttleId || activeChar.getShuttle().isMoving() || !activeChar.isInsideRadius(_x, _y, _z, 1000, true, false))
		{
			activeChar.sendActionFailed();
			return;
		}

		L2ShuttleInstance shuttle = ShuttleManager.getInstance().getShuttle(_shuttleId);
		shuttle.removePassenger(activeChar);
		activeChar.broadcastPacket(new ExStopMoveInShuttle(activeChar, _shuttleId));
		activeChar.setVehicle(null);
		activeChar.setInVehiclePosition(null);
		activeChar.broadcastPacket(new ExSuttleGetOff(activeChar.getObjectId(), _shuttleId, _x, _y, _z));
		activeChar.setXYZ(_x, _y, _z + 50);
		activeChar.revalidateZone(true);
	}

	@Override
	public String getType()
	{
		return "[C] DO:80 RequestGetOffShuttle";
	}
}
