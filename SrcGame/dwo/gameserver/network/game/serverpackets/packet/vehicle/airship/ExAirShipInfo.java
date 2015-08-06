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
package dwo.gameserver.network.game.serverpackets.packet.vehicle.airship;

import dwo.gameserver.model.actor.instance.L2AirShipInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExAirShipInfo extends L2GameServerPacket
{
	// store some parameters, because they can be changed during broadcast
	private final L2AirShipInstance _ship;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	private final int _moveSpeed;
	private final int _rotationSpeed;
	private final int _captain;
	private final int _helm;

	public ExAirShipInfo(L2AirShipInstance ship)
	{
		_ship = ship;
		_x = ship.getX();
		_y = ship.getY();
		_z = ship.getZ();
		_heading = ship.getHeading();
		_moveSpeed = (int) ship.getStat().getMoveSpeed();
		_rotationSpeed = ship.getStat().getRotationSpeed();
		_captain = ship.getCaptainId();
		_helm = ship.getHelmObjectId();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_ship.getObjectId());
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);

		writeD(_captain);
		writeD(_moveSpeed);
		writeD(_rotationSpeed);
		writeD(_helm);
		if(_helm == 0)
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
		else
		{
			writeD(0x16e); // Controller X
			writeD(0x00); // Controller Y
			writeD(0x6b); // Controller Z
			writeD(0x15c); // Captain X
			writeD(0x00); // Captain Y
			writeD(0x69); // Captain Z
		}

		writeD(_ship.getFuel());
		writeD(_ship.getMaxFuel());
	}
}