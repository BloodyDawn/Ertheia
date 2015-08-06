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
package dwo.gameserver.instancemanager.castle;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.List;

public class CastleSiegeGuardManager
{
	private static Logger _log = LogManager.getLogger(CastleSiegeGuardManager.class);

	// =========================================================
	// Data Field
	private Castle _castle;
	private List<L2Spawn> _siegeGuardSpawn = new FastList<>();

	// =========================================================
	// Constructor

	public CastleSiegeGuardManager(Castle castle)
	{
		_castle = castle;
	}

	// =========================================================
	// Method - Public

	/**
	 * Add guard.<BR><BR>
	 */
	public void addSiegeGuard(L2PcInstance activeChar, int npcId)
	{
		if(activeChar == null)
		{
			return;
		}
		addSiegeGuard(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
	}

	/**
	 * Add guard.<BR><BR>
	 */
	public void addSiegeGuard(int x, int y, int z, int heading, int npcId)
	{
		saveSiegeGuard(x, y, z, heading, npcId, 0);
	}

	/**
	 * Hire merc.<BR><BR>
	 */
	public void hireMerc(L2PcInstance activeChar, int npcId)
	{
		if(activeChar == null)
		{
			return;
		}
		hireMerc(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
	}

	/**
	 * Hire merc.<BR><BR>
	 */
	public void hireMerc(int x, int y, int z, int heading, int npcId)
	{
		saveSiegeGuard(x, y, z, heading, npcId, 1);
	}

	/**
	 * Remove a single mercenary, identified by the npcId and location.
	 * Presumably, this is used when a castle lord picks up a previously dropped ticket
	 */
	public void removeMerc(int npcId, int x, int y, int z)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("Delete From castle_siege_guards Where npcId = ? And x = ? AND y = ? AND z = ? AND isHired = 1");
			statement.setInt(1, npcId);
			statement.setInt(2, x);
			statement.setInt(3, y);
			statement.setInt(4, z);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "CastleSiegeGuardManager: Error deleting hired siege guard at " + x + ',' + y + ',' + z + ": " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Remove mercs.<BR><BR>
	 */
	public void removeMercs()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("Delete From castle_siege_guards Where castleId = ? And isHired = 1");
			statement.setInt(1, _castle.getCastleId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "CastleSiegeGuardManager: Error deleting hired siege guard for castle " + _castle.getName() + ": " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Spawn guards.<BR><BR>
	 */
	public void spawnSiegeGuard()
	{
		try
		{
			int hiredCount = 0;
			int hiredMax = CastleMercTicketManager.getInstance().getMaxAllowedMerc(_castle.getCastleId());
			boolean isHired = _castle.getOwnerId() > 0;
			loadSiegeGuard();
			for(L2Spawn spawn : _siegeGuardSpawn)
			{
				if(spawn != null)
				{
					spawn.init();
					if(isHired)
					{
						spawn.stopRespawn();
						if(++hiredCount > hiredMax)
						{
							return;
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "CastleSiegeGuardManager: Error spawning siege guards for castle " + _castle.getName(), e);
		}
	}

	/**
	 * Unspawn guards.<BR><BR>
	 */
	public void unspawnSiegeGuard()
	{
		for(L2Spawn spawn : _siegeGuardSpawn)
		{
			if(spawn == null)
			{
				continue;
			}

			spawn.stopRespawn();
			spawn.getLastSpawn().doDie(spawn.getLastSpawn());
		}

		_siegeGuardSpawn.clear();
	}

	// =========================================================
	// Method - Private

	/**
	 * Load guards.<BR><BR>
	 */
	private void loadSiegeGuard()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM castle_siege_guards Where castleId = ? And isHired = ?");
			statement.setInt(1, _castle.getCastleId());
			if(_castle.getOwnerId() > 0) // If castle is owned by a clan, then don't spawn default guards
			{
				statement.setInt(2, 1);
			}
			else
			{
				statement.setInt(2, 0);
			}
			rs = statement.executeQuery();

			L2Spawn spawn1;
			L2NpcTemplate template1;

			while(rs.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
				if(template1 != null)
				{
					spawn1 = new L2Spawn(template1);
					spawn1.setAmount(1);
					spawn1.setLocx(rs.getInt("x"));
					spawn1.setLocy(rs.getInt("y"));
					spawn1.setLocz(rs.getInt("z"));
					spawn1.setHeading(rs.getInt("heading"));
					spawn1.setRespawnDelay(rs.getInt("respawnDelay"));
					spawn1.setLocation(0);

					_siegeGuardSpawn.add(spawn1);
				}
				else
				{
					_log.log(Level.WARN, "CastleSiegeGuardManager: Missing npc data in npc table for id: " + rs.getInt("npcId"));
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "CastleSiegeGuardManager: Error loading siege guard for castle " + _castle.getName() + ": " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	/**
	 * Save guards.<BR><BR>
	 */
	private void saveSiegeGuard(int x, int y, int z, int heading, int npcId, int isHire)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("Insert Into castle_siege_guards (castleId, npcId, x, y, z, heading, respawnDelay, isHired) Values (?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setInt(1, _castle.getCastleId());
			statement.setInt(2, npcId);
			statement.setInt(3, x);
			statement.setInt(4, y);
			statement.setInt(5, z);
			statement.setInt(6, heading);
			statement.setInt(7, isHire == 1 ? 0 : 600);
			statement.setInt(8, isHire);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "CastleSiegeGuardManager: Error adding siege guard for castle " + _castle.getName() + ": " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	// =========================================================
	// Proeprty

	public Castle getCastle()
	{
		return _castle;
	}

	public List<L2Spawn> getSiegeGuardSpawn()
	{
		return _siegeGuardSpawn;
	}
}
