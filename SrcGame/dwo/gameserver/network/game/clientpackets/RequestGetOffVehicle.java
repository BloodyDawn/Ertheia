/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.GetOffVehicle;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.StopMoveInVehicle;

/**
 * @author Maktakien
 */
public class RequestGetOffVehicle extends L2GameClientPacket
{
	private int _boatId;
	private int _x;
	private int _y;
	private int _z;

	@Override
	protected void readImpl()
	{
		_boatId = readD();
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
		if(!activeChar.isInBoat() || activeChar.getBoat().getObjectId() != _boatId || activeChar.getBoat().isMoving() || !activeChar.isInsideRadius(_x, _y, _z, 1000, true, false))
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.broadcastPacket(new StopMoveInVehicle(activeChar, _boatId));
		activeChar.setVehicle(null);
		activeChar.setInVehiclePosition(null);
		activeChar.sendActionFailed();
		activeChar.broadcastPacket(new GetOffVehicle(activeChar.getObjectId(), _boatId, _x, _y, _z));
		activeChar.setXYZ(_x, _y, _z);
		activeChar.setInsideZone(L2Character.ZONE_PEACE, false);
		activeChar.revalidateZone(true);
	}

	@Override
	public String getType()
	{
		return "[S] 5d GetOffVehicle";
	}
}
