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

import dwo.gameserver.instancemanager.vehicle.AirShipManager;
import dwo.gameserver.model.actor.L2Vehicle;
import dwo.gameserver.model.actor.ai.L2AirShipAI;
import dwo.gameserver.model.actor.templates.L2CharTemplate;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.airship.ExAirShipInfo;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.airship.ExGetOffAirShip;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.airship.ExGetOnAirShip;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.airship.ExMoveToLocationAirShip;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.airship.ExStopMoveAirShip;
import dwo.gameserver.util.geometry.Point3D;

/**
 * Flying airships. Very similar to Maktakien boats (see L2BoatInstance) but these do fly :P
 *
 * @author DrHouse, reworked by DS
 */
public class L2AirShipInstance extends L2Vehicle
{
	public L2AirShipInstance(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		setAI(new L2AirShipAI(new AIAccessor()));
	}

	@Override
	public boolean isAirShip()
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
			player.broadcastPacket(new ExGetOffAirShip(player, this, loc.getX(), loc.getY(), loc.getZ()));
			player.getKnownList().removeAllKnownObjects();
			player.setXYZ(loc.getX(), loc.getY(), loc.getZ());
			player.revalidateZone(true);
		}
		else
		{
			player.setXYZ(loc.getX(), loc.getY(), loc.getZ(), false);
		}
	}

	@Override
	public boolean addPassenger(L2PcInstance player)
	{
		if(!super.addPassenger(player))
		{
			return false;
		}

		player.setVehicle(this);
		player.setInVehiclePosition(new Point3D(0, 0, 0));
		player.broadcastPacket(new ExGetOnAirShip(player, this));
		player.getKnownList().removeAllKnownObjects();
		player.setXYZ(getX(), getY(), getZ());
		player.revalidateZone(true);
		return true;
	}

	@Override
	public void updateAbnormalEffect()
	{
		broadcastPacket(new ExAirShipInfo(this));
	}

	@Override
	public void stopMove(Location pos, boolean updateKnownObjects)
	{
		super.stopMove(pos, updateKnownObjects);

		broadcastPacket(new ExStopMoveAirShip(this));
	}

	@Override
	public boolean moveToNextRoutePoint()
	{
		boolean result = super.moveToNextRoutePoint();
		if(result)
		{
			broadcastPacket(new ExMoveToLocationAirShip(this));
		}

		return result;
	}

	@Override
	public boolean onDelete()
	{
		super.onDelete();
		AirShipManager.getInstance().removeAirShip(this);
		return true;
	}

	public boolean isOwner(L2PcInstance player)
	{
		return false;
	}

	public int getOwnerId()
	{
		return 0;
	}

	public boolean isCaptain(L2PcInstance player)
	{
		return false;
	}

	public int getCaptainId()
	{
		return 0;
	}

	public int getHelmObjectId()
	{
		return 0;
	}

	public int getHelmItemId()
	{
		return 0;
	}

	public boolean setCaptain(L2PcInstance player)
	{
		return false;
	}

	public int getFuel()
	{
		return 0;
	}

	public void setFuel(int f)
	{

	}

	public int getMaxFuel()
	{
		return 0;
	}

	public void setMaxFuel(int mf)
	{

	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new ExAirShipInfo(this));
	}
}