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
package dwo.gameserver.network.game.serverpackets.packet.vehicle.boat;

import dwo.gameserver.model.actor.instance.L2BoatInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author Maktakien
 */

public class VehicleDeparture extends L2GameServerPacket
{
	// Store parameters because they can be changed during broadcast
	private final int _objId;
	private final int _moveSpeed;
	private final int _rotationSpeed;
	private final int _dx;
	private final int _dy;
	private final int _dz;

	public VehicleDeparture(L2BoatInstance boat)
	{
		_objId = boat.getObjectId();
		_dx = boat.getXdestination();
		_dy = boat.getYdestination();
		_dz = boat.getZdestination();
		_moveSpeed = (int) boat.getStat().getMoveSpeed();
		_rotationSpeed = boat.getStat().getRotationSpeed();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objId);
		writeD(_moveSpeed);
		writeD(_rotationSpeed);
		writeD(_dx);
		writeD(_dy);
		writeD(_dz);
	}
}
