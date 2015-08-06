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
package dwo.gameserver.model.world.npc.spawn;

import dwo.config.Config;
import dwo.gameserver.Announcements;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.database.DatabaseUtils;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Auto Spawn Handler
 * <p/>
 * Allows spawning of a NPC object based on a timer. (From the official idea
 * used for the Merchant and Blacksmith of Mammon)
 * <p/>
 * General Usage: - Call registerSpawn() with the parameters listed below. int
 * npcId int[][] spawnPoints or specify NULL to add points later. int
 * initialDelay (If < 0 = default value) int respawnDelay (If < 0 = default
 * value) int despawnDelay (If < 0 = default value or if = 0, function disabled)
 * <p/>
 * spawnPoints is a standard two-dimensional int array containing X,Y and Z
 * coordinates. The default respawn/despawn delays are currently every hour (as
 * for Mammon on official servers).
 * - The resulting AutoSpawnInstance object represents the newly added spawn
 * index. - The interal methods of this object can be used to adjust random
 * spawning, for instance a call to setRandomSpawn(1, true); would set the spawn
 * at index 1 to be randomly rather than sequentially-based. - Also they can be
 * used to specify the number of NPC instances to spawn using setSpawnCount(),
 * and broadcast a message to all users using setBroadcast().
 * <p/>
 * Random Spawning = OFF by default Broadcasting = OFF by default
 *
 * @author Tempy
 */
public class AutoSpawnHandler
{
	protected static final Logger _log = LogManager.getLogger(AutoSpawnHandler.class);

	private static final int DEFAULT_INITIAL_SPAWN = 30000; // 30 seconds after registration
	private static final int DEFAULT_RESPAWN = 3600000; // 1 hour in millisecs
	private static final int DEFAULT_DESPAWN = 3600000; // 1 hour in millisecs

	protected TIntObjectHashMap<AutoSpawnInstance> _registeredSpawns;
	protected TIntObjectHashMap<ScheduledFuture<?>> _runningSpawns;

	protected boolean _activeState = true;

	private AutoSpawnHandler()
	{
		_registeredSpawns = new TIntObjectHashMap<>();
		_runningSpawns = new TIntObjectHashMap<>();

		restoreSpawnData();
	}

	public static AutoSpawnHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	public int size()
	{
		return _registeredSpawns.size();
	}

	public void reload()
	{
		// stop all timers
		for(ScheduledFuture<?> sf : _runningSpawns.values(new ScheduledFuture<?>[0]))
		{
			if(sf != null)
			{
				sf.cancel(true);
			}
		}
		// unregister all registered spawns
		for(AutoSpawnInstance asi : _registeredSpawns.values(new AutoSpawnInstance[0]))
		{
			if(asi != null)
			{
				removeSpawn(asi);
			}
		}

		// create clean list
		_registeredSpawns = new TIntObjectHashMap<>();
		_runningSpawns = new TIntObjectHashMap<>();

		// load
		restoreSpawnData();
	}

	private void restoreSpawnData()
	{
		int numLoaded = 0;
		ThreadConnection con = null;

		try
		{
			FiltredPreparedStatement statement;
			FiltredPreparedStatement statement2;
			ResultSet rs;
			ResultSet rs2;

			con = L2DatabaseFactory.getInstance().getConnection();

			// Restore spawn group data, then the location data.
			statement = con.prepareStatement("SELECT * FROM random_spawn ORDER BY groupId ASC");
			rs = statement.executeQuery();

			statement2 = con.prepareStatement("SELECT * FROM random_spawn_loc WHERE groupId=?");
			while(rs.next())
			{
				// Register random spawn group, set various options on the
				// created spawn instance.
				AutoSpawnInstance spawnInst = registerSpawn(rs.getInt("npcId"), rs.getInt("initialDelay"), rs.getInt("respawnDelay"), rs.getInt("despawnDelay"));

				spawnInst.setSpawnCount(rs.getInt("count"));
				spawnInst.setBroadcast(rs.getBoolean("broadcastSpawn"));
				spawnInst.setRandomSpawn(rs.getBoolean("randomSpawn"));
				numLoaded++;

				// Restore the spawn locations for this spawn group/instance.
				statement2.setInt(1, rs.getInt("groupId"));
				rs2 = statement2.executeQuery();
				statement2.clearParameters();

				while(rs2.next())
				{
					// Add each location to the spawn group/instance.
					spawnInst.addSpawnLocation(rs2.getInt("x"), rs2.getInt("y"), rs2.getInt("z"), rs2.getInt("heading"));
				}
				rs2.close();
			}
			statement2.close();
			rs.close();
			DatabaseUtils.closeStatement(statement);

			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "AutoSpawnHandler: Loaded " + numLoaded + " spawn group(s) from the database.");
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "AutoSpawnHandler: Could not restore spawn data: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeConnection(con);
		}
	}

	/**
	 * Registers a spawn with the given parameters with the spawner, and marks
	 * it as active. Returns a AutoSpawnInstance containing info about the
	 * spawn.
	 *
	 * @param npcId     npcId
	 * @param spawnPoints spawnPoints
	 * @param initialDelay     initialDelay (If < 0 = default value)
	 * @param respawnDelay     respawnDelay (If < 0 = default value)
	 * @param despawnDelay     despawnDelay (If < 0 = default value or if = 0, function disabled)
	 * @return AutoSpawnInstance spawnInst
	 */
	public AutoSpawnInstance registerSpawn(int npcId, int[][] spawnPoints, int initialDelay, int respawnDelay, int despawnDelay)
	{
		if(initialDelay < 0)
		{
			initialDelay = DEFAULT_INITIAL_SPAWN;
		}

		if(respawnDelay < 0)
		{
			respawnDelay = DEFAULT_RESPAWN;
		}

		if(despawnDelay < 0)
		{
			despawnDelay = DEFAULT_DESPAWN;
		}

		AutoSpawnInstance newSpawn = new AutoSpawnInstance(npcId, initialDelay, respawnDelay, despawnDelay);

		if(spawnPoints != null)
		{
			for(int[] spawnPoint : spawnPoints)
			{
				newSpawn.addSpawnLocation(spawnPoint);
			}
		}

		int newId = IdFactory.getInstance().getNextId();
		newSpawn._objectId = newId;
		_registeredSpawns.put(newId, newSpawn);

		setSpawnActive(newSpawn, true);

		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "AutoSpawnHandler: Registered auto spawn for NPC ID " + npcId + " (Object ID = " + newId + ").");
		}

		return newSpawn;
	}

	/**
	 * Registers a spawn with the given parameters with the spawner, and marks
	 * it as active. Returns a AutoSpawnInstance containing info about the
	 * spawn. <BR>
	 * <B>Warning:</B> Spawn locations must be specified separately using
	 * addSpawnLocation().
	 *
	 * @param npcId npcId
	 * @param initialDelay initialDelay (If < 0 = default value)
	 * @param respawnDelay respawnDelay (If < 0 = default value)
	 * @param despawnDelay despawnDelay (If < 0 = default value or if = 0, function disabled)
	 * @return AutoSpawnInstance spawnInst
	 */
	public AutoSpawnInstance registerSpawn(int npcId, int initialDelay, int respawnDelay, int despawnDelay)
	{
		return registerSpawn(npcId, null, initialDelay, respawnDelay, despawnDelay);
	}

	/**
	 * Remove a registered spawn from the list, specified by the given spawn
	 * instance.
	 *
	 * @param spawnInst spawnInst
	 * @return boolean removedSuccessfully
	 */
	public boolean removeSpawn(AutoSpawnInstance spawnInst)
	{
		if(!isSpawnRegistered(spawnInst))
		{
			return false;
		}

		try
		{
			// Try to remove from the list of registered spawns if it exists.
			_registeredSpawns.remove(spawnInst.getNpcId());

			// Cancel the currently associated running scheduled task.
			ScheduledFuture<?> respawnTask = _runningSpawns.remove(spawnInst._objectId);
			respawnTask.cancel(false);

			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "AutoSpawnHandler: Removed auto spawn for NPC ID " + spawnInst._npcId + " (Object ID = " + spawnInst._objectId + ").");
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "AutoSpawnHandler: Could not auto spawn for NPC ID " + spawnInst._npcId + " (Object ID = " + spawnInst._objectId + "): " + e.getMessage(), e);
			return false;
		}

		return true;
	}

	/**
	 * Remove a registered spawn from the list, specified by the given spawn
	 * object ID.
	 *
	 * @param objectId objectId
	 * @return boolean removedSuccessfully
	 */
	public void removeSpawn(int objectId)
	{
		removeSpawn(_registeredSpawns.get(objectId));
	}

	/**
	 * Sets the active state of the specified spawn.
	 *
	 * @param spawnInst spawnInst
	 * @param isActive           isActive
	 */
	public void setSpawnActive(AutoSpawnInstance spawnInst, boolean isActive)
	{
		if(spawnInst == null)
		{
			return;
		}

		int objectId = spawnInst._objectId;

		if(isSpawnRegistered(objectId))
		{
			ScheduledFuture<?> spawnTask;

			if(isActive)
			{
				AutoSpawner rs = new AutoSpawner(objectId);

				spawnTask = spawnInst._desDelay > 0 ? ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(rs, spawnInst._initDelay, spawnInst._resDelay) : ThreadPoolManager.getInstance().scheduleEffect(rs, spawnInst._initDelay);

				_runningSpawns.put(objectId, spawnTask);
			}
			else
			{
				AutoDespawner rd = new AutoDespawner(objectId);
				spawnTask = _runningSpawns.remove(objectId);

				if(spawnTask != null)
				{
					spawnTask.cancel(false);
				}

				ThreadPoolManager.getInstance().scheduleEffect(rd, 0);
			}

			spawnInst.setSpawnActive(isActive);
		}
	}

	/**
	 * Sets the active state of all auto spawn instances to that specified, and
	 * cancels the scheduled spawn task if necessary.
	 *
	 * @param isActive
	 */
	public void setAllActive(boolean isActive)
	{
		if(_activeState == isActive)
		{
			return;
		}

		for(AutoSpawnInstance spawnInst : _registeredSpawns.values(new AutoSpawnInstance[0]))
		{
			setSpawnActive(spawnInst, isActive);
		}

		_activeState = isActive;
	}

	/**
	 * Returns the number of milliseconds until the next occurrance of the given
	 * spawn.
	 *
	 * @param spawnInst
	 */
	public long getTimeToNextSpawn(AutoSpawnInstance spawnInst)
	{
		int objectId = spawnInst.getObjectId();

		if(!isSpawnRegistered(objectId))
		{
			return -1;
		}

		return _runningSpawns.get(objectId).getDelay(TimeUnit.MILLISECONDS);
	}

	/**
	 * Attempts to return the AutoSpawnInstance associated with the given NPC or
	 * Object ID type. <BR>
	 * Note: If isObjectId == false, returns first instance for the specified
	 * NPC ID.
	 *
	 * @param id
	 * @param isObjectId
	 * @return AutoSpawnInstance spawnInst
	 */
	public AutoSpawnInstance getAutoSpawnInstance(int id, boolean isObjectId)
	{
		if(isObjectId)
		{
			if(isSpawnRegistered(id))
			{
				return _registeredSpawns.get(id);
			}
		}
		else
		{
			for(AutoSpawnInstance spawnInst : _registeredSpawns.values(new AutoSpawnInstance[0]))
			{
				if(spawnInst.getNpcId() == id)
				{
					return spawnInst;
				}
			}
		}
		return null;
	}

	public TIntObjectHashMap<AutoSpawnInstance> getAutoSpawnInstances(int npcId)
	{
		TIntObjectHashMap<AutoSpawnInstance> spawnInstList = new TIntObjectHashMap<>();

		for(AutoSpawnInstance spawnInst : _registeredSpawns.values(new AutoSpawnInstance[0]))
		{
			if(spawnInst.getNpcId() == npcId)
			{
				spawnInstList.put(spawnInst.getObjectId(), spawnInst);
			}
		}

		return spawnInstList;
	}

	/**
	 * Tests if the specified object ID is assigned to an auto spawn.
	 *
	 * @param objectId
	 * @return boolean isAssigned
	 */
	public boolean isSpawnRegistered(int objectId)
	{
		return _registeredSpawns.containsKey(objectId);
	}

	/**
	 * Tests if the specified spawn instance is assigned to an auto spawn.
	 *
	 * @param spawnInst
	 * @return boolean isAssigned
	 */
	public boolean isSpawnRegistered(AutoSpawnInstance spawnInst)
	{
		return _registeredSpawns.containsValue(spawnInst);
	}

	/**
	 * AutoSpawnInstance Class <BR>
	 * <BR>
	 * Stores information about a registered auto spawn.
	 *
	 * @author Tempy
	 */
	public static class AutoSpawnInstance
	{
		protected int _objectId;

		protected int _spawnIndex;

		protected int _npcId;

		protected int _initDelay;

		protected int _resDelay;

		protected int _desDelay;

		protected int _spawnCount = 1;

		protected int _lastLocIndex = -1;

		private final List<L2Npc> _npcList = new FastList<>();

		private List<Location> _locList = new FastList<>();

		private boolean _spawnActive;

		private boolean _randomSpawn;

		private boolean _broadcastAnnouncement;

		protected AutoSpawnInstance(int npcId, int initDelay, int respawnDelay, int despawnDelay)
		{
			_npcId = npcId;
			_initDelay = initDelay;
			_resDelay = respawnDelay;
			_desDelay = despawnDelay;
		}

		protected boolean addNpcInstance(L2Npc npcInst)
		{
			return _npcList.add(npcInst);
		}

		protected boolean removeNpcInstance(L2Npc npcInst)
		{
			return _npcList.remove(npcInst);
		}

		public int getObjectId()
		{
			return _objectId;
		}

		public int getInitialDelay()
		{
			return _initDelay;
		}

		public int getRespawnDelay()
		{
			return _resDelay;
		}

		public int getDespawnDelay()
		{
			return _desDelay;
		}

		public int getNpcId()
		{
			return _npcId;
		}

		public int getSpawnCount()
		{
			return _spawnCount;
		}

		public void setSpawnCount(int spawnCount)
		{
			_spawnCount = spawnCount;
		}

		public Location[] getLocationList()
		{
			return _locList.toArray(new Location[_locList.size()]);
		}

		public L2Npc[] getNPCInstanceList()
		{
			L2Npc[] ret;
			synchronized(_npcList)
			{
				ret = new L2Npc[_npcList.size()];
				_npcList.toArray(ret);
			}

			return ret;
		}

		public L2Spawn[] getSpawns()
		{
			List<L2Spawn> npcSpawns = _npcList.stream().map(L2Npc::getSpawn).collect(Collectors.toCollection(FastList::new));

			return npcSpawns.toArray(new L2Spawn[npcSpawns.size()]);
		}

		public void setBroadcast(boolean broadcastValue)
		{
			_broadcastAnnouncement = broadcastValue;
		}

		public boolean isSpawnActive()
		{
			return _spawnActive;
		}

		protected void setSpawnActive(boolean activeValue)
		{
			_spawnActive = activeValue;
		}

		public boolean isRandomSpawn()
		{
			return _randomSpawn;
		}

		public void setRandomSpawn(boolean randValue)
		{
			_randomSpawn = randValue;
		}

		public boolean isBroadcasting()
		{
			return _broadcastAnnouncement;
		}

		public boolean addSpawnLocation(int x, int y, int z, int heading)
		{
			return _locList.add(new Location(x, y, z, heading));
		}

		public boolean addSpawnLocation(int[] spawnLoc)
		{
			if(spawnLoc.length != 3)
			{
				return false;
			}

			return addSpawnLocation(spawnLoc[0], spawnLoc[1], spawnLoc[2], -1);
		}

		public Location removeSpawnLocation(int locIndex)
		{
			try
			{
				return _locList.remove(locIndex);
			}
			catch(IndexOutOfBoundsException e)
			{
				return null;
			}
		}
	}

	private static class SingletonHolder
	{
		protected static final AutoSpawnHandler _instance = new AutoSpawnHandler();
	}

	/**
	 * AutoSpawner Class <BR>
	 * <BR>
	 * This handles the main spawn task for an auto spawn instance, and
	 * initializes a despawner if required.
	 *
	 * @author Tempy
	 */
	private class AutoSpawner implements Runnable
	{
		private int _objectId;

		protected AutoSpawner(int objectId)
		{
			_objectId = objectId;
		}

		@Override
		public void run()
		{
			try
			{
				// Retrieve the required spawn instance for this spawn task.
				AutoSpawnInstance spawnInst = _registeredSpawns.get(_objectId);

				// If the spawn is not scheduled to be active, cancel the spawn
				// task.
				if(!spawnInst.isSpawnActive())
				{
					return;
				}

				Location[] locationList = spawnInst.getLocationList();

				// If there are no set co-ordinates, cancel the spawn task.
				if(locationList.length == 0)
				{
					_log.log(Level.INFO, "AutoSpawnHandler: No location co-ords specified for spawn instance (Npc ID = " + spawnInst.getNpcId() + ").");
					return;
				}

				int locationCount = locationList.length;
				int locationIndex = Rnd.get(locationCount);

                /*
                     * If random spawning is disabled, the spawn at the next set of
                     * co-ordinates after the last. If the index is greater than the
                     * number of possible spawns, reset the counter to zero.
                     */
				if(!spawnInst.isRandomSpawn())
				{
					locationIndex = spawnInst._lastLocIndex + 1;

					if(locationIndex == locationCount)
					{
						locationIndex = 0;
					}

					spawnInst._lastLocIndex = locationIndex;
				}

				// Set the X, Y and Z co-ordinates, where this spawn will take
				// place.
				int x = locationList[locationIndex].getX();
				int y = locationList[locationIndex].getY();
				int z = locationList[locationIndex].getZ();
				int heading = locationList[locationIndex].getHeading();

				// Fetch the template for this NPC ID and create a new spawn.
				L2NpcTemplate npcTemp = NpcTable.getInstance().getTemplate(spawnInst.getNpcId());
				if(npcTemp == null)
				{
					_log.log(Level.WARN, "Couldnt find NPC id" + spawnInst.getNpcId() + " Try to update your DP");
					return;
				}
				L2Spawn newSpawn = new L2Spawn(npcTemp);

				newSpawn.setLocx(x);
				newSpawn.setLocy(y);
				newSpawn.setLocz(z);
				if(heading != -1)
				{
					newSpawn.setHeading(heading);
				}
				newSpawn.setAmount(spawnInst.getSpawnCount());
				if(spawnInst._desDelay == 0)
				{
					newSpawn.setRespawnDelay(spawnInst._resDelay);
				}

				// Add the new spawn information to the spawn table, but do not
				// store it.
				SpawnTable.getInstance().addNewSpawn(newSpawn);
				L2Npc npcInst = null;

				if(spawnInst._spawnCount == 1)
				{
					npcInst = newSpawn.doSpawn();
					npcInst.setXYZ(npcInst.getX(), npcInst.getY(), npcInst.getZ());
					spawnInst.addNpcInstance(npcInst);
				}
				else
				{
					for(int i = 0; i < spawnInst._spawnCount; i++)
					{
						npcInst = newSpawn.doSpawn();

						// To prevent spawning of more than one NPC in the exact
						// same spot,
						// move it slightly by a small random offset.
						npcInst.setXYZ(npcInst.getX() + Rnd.get(50), npcInst.getY() + Rnd.get(50), npcInst.getZ());

						// Add the NPC instance to the list of managed
						// instances.
						spawnInst.addNpcInstance(npcInst);
					}
				}

				String nearestTown = MapRegionManager.getInstance().getClosestTownName(npcInst.getLoc());

				// Announce to all players that the spawn has taken place, with
				// the nearest town location.
				if(spawnInst.isBroadcasting() && npcInst != null)
				{
					Announcements.getInstance().announceToAll("The " + npcInst.getName() + " has spawned near " + nearestTown + '!');
				}

				// If there is no despawn time, do not create a despawn task.
				if(spawnInst.getDespawnDelay() > 0)
				{
					AutoDespawner rd = new AutoDespawner(_objectId);
					ThreadPoolManager.getInstance().scheduleAi(rd, spawnInst.getDespawnDelay() - 1000);
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "AutoSpawnHandler: An error occurred while initializing spawn instance (Object ID = " + _objectId + "): " + e.getMessage(), e);
			}
		}
	}

	/**
	 * AutoDespawner Class <BR>
	 * <BR>
	 * Simply used as a secondary class for despawning an auto spawn instance.
	 *
	 * @author Tempy
	 */
	private class AutoDespawner implements Runnable
	{
		private int _objectId;

		protected AutoDespawner(int objectId)
		{
			_objectId = objectId;
		}

		@Override
		public void run()
		{
			try
			{
				AutoSpawnInstance spawnInst = _registeredSpawns.get(_objectId);

				if(spawnInst == null)
				{
					_log.log(Level.INFO, "AutoSpawnHandler: No spawn registered for object ID = " + _objectId + '.');
					return;
				}

				for(L2Npc npcInst : spawnInst.getNPCInstanceList())
				{
                    if (npcInst != null) 
                    {
                        npcInst.getLocationController().delete();
                        SpawnTable.getInstance().deleteSpawn(npcInst.getSpawn());
                        spawnInst.removeNpcInstance(npcInst);
                    }
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "AutoSpawnHandler: An error occurred while despawning spawn (Object ID = " + _objectId + "): " + e.getMessage(), e);
			}
		}
	}
}
