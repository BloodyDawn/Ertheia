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
 *
 */
public class VehicleInfo extends L2GameServerPacket
{
	// Store some parameters here because they can be changed during broadcast
	private final int _objId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;

	public VehicleInfo(L2BoatInstance boat)
	{
		_objId = boat.getObjectId();
		_x = boat.getX();
		_y = boat.getY();
		_z = boat.getZ();
		_heading = boat.getHeading();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
	}
}
