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

import dwo.config.Config;
import dwo.gameserver.instancemanager.vehicle.BoatManager;
import dwo.gameserver.model.actor.instance.L2BoatInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.MoveToLocationInVehicle;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.StopMoveInVehicle;
import dwo.gameserver.util.geometry.Point3D;

public class RequestMoveToLocationInVehicle extends L2GameClientPacket
{
	private int _boatId;
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;

	@Override
	protected void readImpl()
	{
		_boatId = readD();   //objectId of boat
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

		if(Config.PLAYER_MOVEMENT_BLOCK_TIME > 0 && !activeChar.isGM() && activeChar.getNotMoveUntil() > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC);
			activeChar.sendActionFailed();
			return;
		}

		if(_targetX == _originX && _targetY == _originY && _targetZ == _originZ)
		{
			activeChar.sendPacket(new StopMoveInVehicle(activeChar, _boatId));
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

		if(!activeChar.getPets().isEmpty())
		{
			activeChar.sendPacket(SystemMessageId.RELEASE_PET_ON_BOAT);
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isTransformed())
		{
			activeChar.sendPacket(SystemMessageId.CANT_POLYMORPH_ON_BOAT);
			activeChar.sendActionFailed();
			return;
		}

		L2BoatInstance boat;
		if(activeChar.isInBoat())
		{
			boat = activeChar.getBoat();
			if(boat.getObjectId() != _boatId)
			{
				activeChar.sendActionFailed();
				return;
			}
		}
		else
		{
			boat = BoatManager.getInstance().getBoat(_boatId);
			if(boat == null || !boat.isInsideRadius(activeChar, 300, true, false))
			{
				activeChar.sendActionFailed();
				return;
			}
			activeChar.setVehicle(boat);
		}

		Point3D pos = new Point3D(_targetX, _targetY, _targetZ);
		Point3D originPos = new Point3D(_originX, _originY, _originZ);
		activeChar.setInVehiclePosition(pos);
		activeChar.broadcastPacket(new MoveToLocationInVehicle(activeChar, pos, originPos));
	}

	@Override
	public String getType()
	{
		return "[C] 75 RequestMoveToLocationInVehicle";
	}
}