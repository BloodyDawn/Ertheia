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
package dwo.gameserver.instancemanager.vehicle;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.instance.L2AirShipInstance;
import dwo.gameserver.model.actor.instance.L2ControllableAirShipInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2CharTemplate;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.zone.VehiclePathPoint;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.airship.ExAirShipTeleportList;
import dwo.gameserver.util.database.DatabaseUtils;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AirShipManager
{
	private static final Logger _log = LogManager.getLogger(AirShipManager.class);

	private static final String LOAD_DB = "SELECT * FROM airships";
	private static final String ADD_DB = "INSERT INTO airships (owner_id,fuel) VALUES (?,?)";
	private static final String UPDATE_DB = "UPDATE airships SET fuel=? WHERE owner_id=?";

	private L2CharTemplate _airShipTemplate;
	private TIntObjectHashMap<StatsSet> _airShipsInfo = new TIntObjectHashMap<>();
	private TIntObjectHashMap<L2AirShipInstance> _airShips = new TIntObjectHashMap<>();
	private TIntObjectHashMap<AirShipTeleportList> _teleports = new TIntObjectHashMap<>();

	private AirShipManager()
	{
		StatsSet npcDat = new StatsSet();
		npcDat.set("npcId", 9);
		npcDat.set("level", 0);
		npcDat.set("jClass", "boat");

		npcDat.set("str", 0);
		npcDat.set("con", 0);
		npcDat.set("dex", 0);
		npcDat.set("int", 0);
		npcDat.set("wit", 0);
		npcDat.set("men", 0);

		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseAccCombat", 38);
		npcDat.set("baseEvasRate", 38);
		npcDat.set("base_critical", 38);

		npcDat.set("collision_radius", 0);
		npcDat.set("collision_height", 0);
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("base_attack_range", 0);
		npcDat.set("org_mp", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("exp", 0);
		npcDat.set("sp", 0);
		npcDat.set("base_physical_attack", 0);
		npcDat.set("base_magic_attack", 0);
		npcDat.set("base_attack_speed", 0);
		npcDat.set("agro_range", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("slot_rhand", 0);
		npcDat.set("slot_lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("ground_high", 0);
		npcDat.set("ground_low", 0);
		npcDat.set("name", "AirShip");
		npcDat.set("org_hp", 50000);
		npcDat.set("org_hp_regen", 3.0e-3f);
		npcDat.set("org_mp_regen", 3.0e-3f);
		npcDat.set("base_defend", 100);
		npcDat.set("base_magic_defend", 100);
		_airShipTemplate = new L2CharTemplate(npcDat);

		load();
	}

	public static AirShipManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public L2AirShipInstance getNewAirShip(int x, int y, int z, int heading)
	{
		L2AirShipInstance airShip = new L2AirShipInstance(IdFactory.getInstance().getNextId(), _airShipTemplate);

		airShip.setHeading(heading);
		airShip.setXYZ(x, y, z, false);
		airShip.getLocationController().spawn();
		airShip.getStat().setMoveSpeed(280);
		airShip.getStat().setRotationSpeed(2000);
		return airShip;
	}

	public L2AirShipInstance getNewAirShip(int x, int y, int z, int heading, int ownerId)
	{
		StatsSet info = _airShipsInfo.get(ownerId);
		if(info == null)
		{
			return null;
		}

		L2AirShipInstance airShip;
		if(_airShips.containsKey(ownerId))
		{
			airShip = _airShips.get(ownerId);
			airShip.refreshID();
		}
		else
		{
			airShip = new L2ControllableAirShipInstance(IdFactory.getInstance().getNextId(), _airShipTemplate, ownerId);
			_airShips.put(ownerId, airShip);

			airShip.setMaxFuel(600);
			airShip.setFuel(info.getInteger("fuel"));
			airShip.getStat().setMoveSpeed(280);
			airShip.getStat().setRotationSpeed(2000);
		}

		airShip.setHeading(heading);
		airShip.setXYZ(x, y, z, false);
		airShip.getLocationController().spawn();
		return airShip;
	}

	public void removeAirShip(L2AirShipInstance ship)
	{
		if(ship.getOwnerId() != 0)
		{
			storeInDb(ship.getOwnerId());
			StatsSet info = _airShipsInfo.get(ship.getOwnerId());
			if(info != null)
			{
				info.set("fuel", ship.getFuel());
			}
		}
	}

	public boolean hasAirShipLicense(int ownerId)
	{
		return _airShipsInfo.contains(ownerId);
	}

	public void registerLicense(int ownerId)
	{
		if(!_airShipsInfo.contains(ownerId))
		{
			StatsSet info = new StatsSet();
			info.set("fuel", 600);

			_airShipsInfo.put(ownerId, info);

			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(ADD_DB);
				statement.setInt(1, ownerId);
				statement.setInt(2, info.getInteger("fuel"));
				statement.executeUpdate();
			}
			catch(SQLException e)
			{
				_log.log(Level.ERROR, "AirShipManager: Could not add new airship license: " + e.getMessage(), e);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "AirShipManager: Error while initializing: " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
	}

	public boolean hasAirShip(int ownerId)
	{
		L2AirShipInstance ship = _airShips.get(ownerId);
		return !(ship == null || !(ship.isVisible() || ship.isTeleporting()));

	}

	public void registerAirShipTeleportList(int dockId, int locationId, VehiclePathPoint[][] tp, int[] fuelConsumption)
	{
		if(tp.length != fuelConsumption.length)
		{
			return;
		}

		_teleports.put(dockId, new AirShipTeleportList(locationId, fuelConsumption, tp));
	}

	public void sendAirShipTeleportList(L2PcInstance player)
	{
		if(player == null || !player.isInAirShip())
		{
			return;
		}

		L2AirShipInstance ship = player.getAirShip();
		if(!ship.isCaptain(player) || !ship.isInDock() || ship.isMoving())
		{
			return;
		}

		int dockId = ship.getDockId();
		if(!_teleports.contains(dockId))
		{
			return;
		}

		AirShipTeleportList all = _teleports.get(dockId);
		player.sendPacket(new ExAirShipTeleportList(all.location, all.routes, all.fuel));
	}

	public VehiclePathPoint[] getTeleportDestination(int dockId, int index)
	{
		AirShipTeleportList all = _teleports.get(dockId);
		if(all == null)
		{
			return null;
		}

		if(index < -1 || index >= all.routes.length)
		{
			return null;
		}

		return all.routes[index + 1];
	}

	public int getFuelConsumption(int dockId, int index)
	{
		AirShipTeleportList all = _teleports.get(dockId);
		if(all == null)
		{
			return 0;
		}

		if(index < -1 || index >= all.fuel.length)
		{
			return 0;
		}

		return all.fuel[index + 1];
	}

	private void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(LOAD_DB);
			rset = statement.executeQuery();

			while(rset.next())
			{
				StatsSet info = new StatsSet();
				info.set("fuel", rset.getInt("fuel"));

				_airShipsInfo.put(rset.getInt("owner_id"), info);
				info = null;
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "AirShipManager: Could not load airships table: " + e.getMessage(), e);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "AirShipManager: Error while initializing: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		_log.log(Level.INFO, "AirShipManager: Loaded " + _airShipsInfo.size() + " private airships");
	}

	private void storeInDb(int ownerId)
	{
		StatsSet info = _airShipsInfo.get(ownerId);
		if(info == null)
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(UPDATE_DB);
			statement.setInt(1, info.getInteger("fuel"));
			statement.setInt(2, ownerId);
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "AirShipManager: Could not update airships table: " + e.getMessage(), e);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "AirShipManager: Error while save: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private static class AirShipTeleportList
	{
		public int location;
		public int[] fuel;
		public VehiclePathPoint[][] routes;

		public AirShipTeleportList(int loc, int[] f, VehiclePathPoint[][] r)
		{
			location = loc;
			fuel = f;
			routes = r;
		}
	}

	private static class SingletonHolder
	{
		protected static final AirShipManager _instance = new AirShipManager();
	}
}