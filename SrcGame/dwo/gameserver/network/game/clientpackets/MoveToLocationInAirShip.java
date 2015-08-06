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

import dwo.gameserver.model.actor.instance.L2AirShipInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.airship.ExMoveToLocationInAirShip;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.StopMoveInVehicle;
import dwo.gameserver.util.geometry.Point3D;

/**
 * format: ddddddd
 * X:%d Y:%d Z:%d OriginX:%d OriginY:%d OriginZ:%d
 *
 * @author GodKratos
 */

public class MoveToLocationInAirShip extends L2GameClientPacket
{
	private int _shipId;
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;

	@Override
	protected void readImpl()
	{
		_shipId = readD();
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(_targetX == _originX && _targetY == _originY && _targetZ == _originZ)
		{
			activeChar.sendPacket(new StopMoveInVehicle(activeChar, _shipId));
			return;
		}

		if(activeChar.isAttackingNow() && activeChar.getActiveWeaponItem() != null && activeChar.getActiveWeaponItem().getItemType() == L2WeaponType.BOW)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isSitting() || activeChar.isMovementDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!activeChar.isInAirShip())
		{
			activeChar.sendActionFailed();
			return;
		}

		L2AirShipInstance airShip = activeChar.getAirShip();
		if(airShip.getObjectId() != _shipId)
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.setInVehiclePosition(new Point3D(_targetX, _targetY, _targetZ));
		activeChar.broadcastPacket(new ExMoveToLocationInAirShip(activeChar));
	}

	@Override
	public String getType()
	{
		return "[C] D0:20 MoveToLocationInAirShip";
	}
}