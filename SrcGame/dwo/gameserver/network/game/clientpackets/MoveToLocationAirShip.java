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

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.vehicle.AirShipManager;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2AirShipInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.VehiclePathPoint;
import dwo.gameserver.network.game.components.SystemMessageId;

public class MoveToLocationAirShip extends L2GameClientPacket
{
	public static final int STEP = 300;

	private int _command;
	private int _param1;
	private int _param2;

	@Override
	protected void readImpl()
	{
		_command = readD();
		_param1 = readD();
		if(_buf.remaining() > 0)
		{
			_param2 = readD();
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(!activeChar.isInAirShip())
		{
			return;
		}

		L2AirShipInstance ship = activeChar.getAirShip();
		if(!ship.isCaptain(activeChar))
		{
			return;
		}

		int z = ship.getZ();

		switch(_command)
		{
			case 0:
				if(!ship.canBeControlled())
				{
					return;
				}
				if(_param1 < WorldManager.GRACIA_MAX_X)
				{
					ship.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(_param1, _param2, z, 0));
				}
				break;
			case 1:
				if(!ship.canBeControlled())
				{
					return;
				}
				ship.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				break;
			case 2:
				if(!ship.canBeControlled())
				{
					return;
				}
				if(z < WorldManager.GRACIA_MAX_Z)
				{
					z = Math.min(z + STEP, WorldManager.GRACIA_MAX_Z);
					ship.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(ship.getX(), ship.getY(), z, 0));
				}
				break;
			case 3:
				if(!ship.canBeControlled())
				{
					return;
				}
				if(z > WorldManager.GRACIA_MIN_Z)
				{
					z = Math.max(z - STEP, WorldManager.GRACIA_MIN_Z);
					ship.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(ship.getX(), ship.getY(), z, 0));
				}
				break;
			case 4:
				if(!ship.isInDock() || ship.isMoving())
				{
					return;
				}

				VehiclePathPoint[] dst = AirShipManager.getInstance().getTeleportDestination(ship.getDockId(), _param1);
				if(dst == null)
				{
					return;
				}

				// Consume fuel, if needed
				int fuelConsumption = AirShipManager.getInstance().getFuelConsumption(ship.getDockId(), _param1);
				if(fuelConsumption > 0)
				{
					if(fuelConsumption > ship.getFuel())
					{
						activeChar.sendPacket(SystemMessageId.THE_AIRSHIP_CANNOT_TELEPORT);
						return;
					}
					ship.setFuel(ship.getFuel() - fuelConsumption);
				}

				ship.executePath(dst);
				break;
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:38 MoveToLocationAirShip";
	}
}