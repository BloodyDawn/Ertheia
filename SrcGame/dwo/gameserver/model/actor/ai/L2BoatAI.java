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
package dwo.gameserver.model.actor.ai;

import dwo.gameserver.model.actor.L2Vehicle;
import dwo.gameserver.model.actor.instance.L2BoatInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.VehicleDeparture;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.VehicleInfo;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.VehicleStart;

/**
 * @author DS
 */
public class L2BoatAI extends L2VehicleAI
{
	public L2BoatAI(L2Vehicle.AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	public L2BoatInstance getActor()
	{
		return (L2BoatInstance) _actor;
	}

	@Override
	protected void moveTo(Location loc)
	{
		if(!_actor.isMovementDisabled())
		{
			if(!_clientMoving)
			{
				_actor.broadcastPacket(new VehicleStart(getActor(), 1));
			}

			_clientMoving = true;
			_accessor.moveTo(loc.getX(), loc.getY(), loc.getZ());
			_actor.broadcastPacket(new VehicleDeparture(getActor()));
		}
	}

	@Override
	protected void clientStopMoving(Location pos)
	{
		if(_actor.isMoving())
		{
			_accessor.stopMove(pos);
		}

		if(_clientMoving || pos != null)
		{
			_clientMoving = false;
			_actor.broadcastPacket(new VehicleStart(getActor(), 0));
			_actor.broadcastPacket(new VehicleInfo(getActor()));
		}
	}

	@Override
	public void describeStateToPlayer(L2PcInstance player)
	{
		if(_clientMoving)
		{
			player.sendPacket(new VehicleDeparture(getActor()));
		}
	}
}