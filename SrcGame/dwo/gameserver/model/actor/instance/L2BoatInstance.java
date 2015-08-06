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
package dwo.gameserver.model.actor.instance;

import dwo.gameserver.model.actor.L2Vehicle;
import dwo.gameserver.model.actor.ai.L2BoatAI;
import dwo.gameserver.model.actor.templates.L2CharTemplate;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.VehicleDeparture;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.VehicleInfo;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.VehicleStart;

/**
 * @author Maktakien, reworked by DS
 */
public class L2BoatInstance extends L2Vehicle
{
	public L2BoatInstance(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		setAI(new L2BoatAI(new AIAccessor()));
	}

	@Override
	public boolean isBoat()
	{
		return true;
	}

	@Override
	public void oustPlayer(L2PcInstance player)
	{
		super.oustPlayer(player);

		Location loc = getOustLoc();
		if(player.isOnline())
		{
			player.teleToLocation(loc);
		}
		else
		{
			player.setXYZ(loc, false); // disconnects handling
		}
	}

	@Override
	public void stopMove(Location pos, boolean updateKnownObjects)
	{
		super.stopMove(pos, updateKnownObjects);

		broadcastPacket(new VehicleStart(this, 0));
		broadcastPacket(new VehicleInfo(this));
	}

	@Override
	public boolean moveToNextRoutePoint()
	{
		boolean result = super.moveToNextRoutePoint();
		if(result)
		{
			broadcastPacket(new VehicleDeparture(this));
		}

		return result;
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new VehicleInfo(this));
	}
}
