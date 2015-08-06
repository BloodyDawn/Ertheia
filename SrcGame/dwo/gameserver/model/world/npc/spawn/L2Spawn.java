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
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.LocationsTable;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.holders.WorldStatisticStatueHolder;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * This class manages the spawn and respawn of a group of L2NpcInstance that are in the same are and have the same type.
 * <p/>
 * <B><U> Concept</U> :</B><BR><BR>
 * L2NpcInstance can be spawned either in a random position into a location area (if Lox=0 and Locy=0), either at an exact position.
 * The heading of the L2NpcInstance can be a random heading if not defined (value= -1) or an exact heading (ex : merchant...).<BR><BR>
 *
 * @author Nightmare
 * @version $Revision: 1.9.2.3.2.8 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2Spawn
{
	protected static final Logger _log = LogManager.getLogger(L2Spawn.class);
	private static List<SpawnListener> _spawnListeners = new FastList<>();

	// private String _location = DEFAULT_LOCATION;
	/**
	 * The current number of SpawnTask in progress or stand by of this L2Spawn
	 */
	protected int _scheduledCount;
	/**
	 * The link on the L2NpcTemplate object containing generic and static properties of this spawn (ex : RewardExp, RewardSP, AggroRange...)
	 */
	private L2NpcTemplate _template;
	/**
	 * The identifier of the location area where L2NpcInstance can be spawned
	 */
	private int _location;
	/**
	 * The maximum number of L2NpcInstance that can manage this L2Spawn
	 */
	private int _maximumCount;
	/**
	 * The current number of L2NpcInstance managed by this L2Spawn
	 */
	private int _currentCount;
	/**
	 * The X position of the spawn point
	 */
	private int _locX;
	/**
	 * The Y position of the spawn point
	 */
	private int _locY;
	/**
	 * The Z position of the spawn point
	 */
	private int _locZ;
	/**
	 * The heading of L2NpcInstance when they are spawned
	 */
	private int _heading;
	/**
	 * The delay between a L2NpcInstance remove and its re-spawn.
	 * By default, respawn delay is 60 seconds.
	 */
	private int _respawnDelay = 60 * 1000;
	/**
	 * Minimum delay RaidBoss
	 */
	private int _respawnMinDelay;
	/**
	 * Maximum delay RaidBoss
	 */
	private int _respawnMaxDelay;
	private int _instanceId;
	/**
	 * The generic constructor of L2NpcInstance managed by this L2Spawn
	 */
	private Constructor<?> _constructor;
	/**
	 * If True a L2NpcInstance is respawned each time that another is killed
	 */
	private boolean _doRespawn;
	/**
	 * If true then spawn is custom
	 */
	private boolean _customSpawn;
	private L2Npc _lastSpawn;
	// L2jS ADD Custom
	private int _onKillDelay;

	/**
	 * Constructor of L2Spawn.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Spawn owns generic and static properties (ex : RewardExp, RewardSP, AggroRange...).
	 * All of those properties are stored in a different L2NpcTemplate for each type of L2Spawn.
	 * Each template is loaded once in the server cache memory (reduce memory use).
	 * When a new instance of L2Spawn is created, server just create a link between the instance and the template.
	 * This link is stored in <B>_template</B><BR><BR>
	 * <p/>
	 * Each L2NpcInstance is linked to a L2Spawn that manages its spawn and respawn (delay, location...).
	 * This link is stored in <B>_spawn</B> of the L2NpcInstance<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the _template of the L2Spawn </li>
	 * <li>Calculate the implementationName used to generate the generic constructor of L2NpcInstance managed by this L2Spawn</li>
	 * <li>Create the generic constructor of L2NpcInstance managed by this L2Spawn</li><BR><BR>
	 *
	 * @param mobTemplate The L2NpcTemplate to link to this L2Spawn
	 */
	public L2Spawn(L2NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException, NoSuchMethodException
	{
		// Set the _template of the L2Spawn
		_template = mobTemplate;

		if(_template == null)
		{
			return;
		}

		// The Name of the L2NpcInstance type managed by this L2Spawn
		String implementationName = _template.getType(); // implementing class name

		if(mobTemplate instanceof WorldStatisticStatueHolder)
		{
			implementationName = "L2WorldStatue";
		}

		// Create the generic constructor of L2NpcInstance managed by this L2Spawn
		Class<?>[] parameters = {int.class, Class.forName("dwo.gameserver.model.actor.templates.L2NpcTemplate")};
		_constructor = Class.forName("dwo.gameserver.model.actor.instance." + implementationName + "Instance").getConstructor(parameters);
	}

	public static void addSpawnListener(SpawnListener listener)
	{
		synchronized(_spawnListeners)
		{
			_spawnListeners.add(listener);
		}
	}

	public static void removeSpawnListener(SpawnListener listener)
	{
		synchronized(_spawnListeners)
		{
			_spawnListeners.remove(listener);
		}
	}

	public static void notifyNpcSpawned(L2Npc npc)
	{
		synchronized(_spawnListeners)
		{
			for(SpawnListener listener : _spawnListeners)
			{
				listener.npcSpawned(npc);
			}
		}
	}

	/**
	 * @return maximum number of L2NpcInstance that this L2Spawn can manage
	 */
	public int getAmount()
	{
		return _maximumCount;
	}

	/**
	 * @param amount maximum number of L2NpcInstance that this L2Spawn can manage
	 */
	public void setAmount(int amount)
	{
		_maximumCount = amount;
	}

	/**
	 * @return Identifier of the location area where L2NpcInstance can be spawned
	 */
	public int getLocationId()
	{
		return _location;
	}

	/**
	 * @return X position of the spawn point
	 */
	public int getLocx()
	{
		return _locX;
	}

	/**
	 * @param locx X position of the spawn point
	 */
	public void setLocx(int locx)
	{
		_locX = locx;
	}

	/**
	 * @return Y position of the spawn point
	 */
	public int getLocy()
	{
		return _locY;
	}

	/**
	 * @param locy Y position of the spawn point
	 */
	public void setLocy(int locy)
	{
		_locY = locy;
	}

	/**
	 * @return Z position of the spawn point
	 */
	public int getLocz()
	{
		return _locZ;
	}

	/**
	 * @param locz Z position of the spawn point
	 */
	public void setLocz(int locz)
	{
		_locZ = locz;
	}

	public Location getLoc()
	{
		return new Location(_locX, _locY, _locZ, _heading);
	}

	/**
	 * @return Indentifier of the L2NpcInstance manage by this L2spawn contained in the L2NpcTemplate
	 */
	public int getNpcId()
	{
		return _template.getNpcId();
	}

	/**
	 * @return heading of L2NpcInstance when they are spawned
	 */
	public int getHeading()
	{
		return _heading;
	}

	/**
	 * @param heading heading of L2NpcInstance when they are spawned
	 */
	public void setHeading(int heading)
	{
		_heading = heading;
	}

	/**
	 * @return delay between a L2NpcInstance remove and its respawn
	 */
	public int getRespawnDelay()
	{
		return _respawnDelay;
	}

	/**
	 * @param i delay in seconds
	 */
	public void setRespawnDelay(int i)
	{
		if(i < 0)
		{
			_log.log(Level.WARN, "respawn delay is negative for spawn:" + this);
		}

		if(i < 10)
		{
			i = 10;
		}

		_respawnDelay = i * 1000;
	}

	/**
	 * @return Min RaidBoss Spawn delay
	 */
	public int getRespawnMinDelay()
	{
		return _respawnMinDelay;
	}

	/**
	 * @param date Minimum Respawn Delay
	 */
	public void setRespawnMinDelay(int date)
	{
		_respawnMinDelay = date;
	}

	/**
	 * @return Max RaidBoss Spawn delay
	 */
	public int getRespawnMaxDelay()
	{
		return _respawnMaxDelay;
	}

	/**
	 * @param date Maximum Respawn Delay
	 */
	public void setRespawnMaxDelay(int date)
	{
		_respawnMaxDelay = date;
	}

	/**
	 * @param location area where L2NpcInstance can be spawned
	 */
	public void setLocation(int location)
	{
		_location = location;
	}

	public void setLocation(Location loc)
	{
		_locX = loc.getX();
		_locY = loc.getY();
		_locZ = loc.getZ();
	}

	/**
	 * @return type of spawn
	 */
	public boolean isCustom()
	{
		return _customSpawn;
	}

	/**
	 * @param custom is the spawn as custom&
	 */
	public void setCustom(boolean custom)
	{
		_customSpawn = custom;
	}

	/**
	 * Decrease the current number of L2NpcInstance of this L2Spawn and if necessary create a SpawnTask to launch after the respawn Delay.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Decrease the current number of L2NpcInstance of this L2Spawn </li>
	 * <li>Check if respawn is possible to prevent multiple respawning caused by lag </li>
	 * <li>Update the current number of SpawnTask in progress or stand by of this L2Spawn </li>
	 * <li>Create a new SpawnTask to launch after the respawn Delay </li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A respawn is possible ONLY if _doRespawn=True and _scheduledCount + _currentCount < _maximumCount</B></FONT><BR><BR>
	 * @param oldNpc
	 */
	public void decreaseCount(L2Npc oldNpc)
	{
		// sanity check
		if(_currentCount <= 0)
		{
			return;
		}

		// Decrease the current number of L2NpcInstance of this L2Spawn
		_currentCount--;

		// Check if respawn is possible to prevent multiple respawning caused by lag
		if(_doRespawn && _scheduledCount + _currentCount < _maximumCount)
		{
			// Update the current number of SpawnTask in progress or stand by of this L2Spawn
			_scheduledCount++;

			// Create a new SpawnTask to launch after the respawn Delay
			//ClientScheduler.getInstance().scheduleLow(new SpawnTask(npcId), _respawnDelay);
			ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(oldNpc), _respawnDelay);
		}
	}

	/**
	 * Create the initial spawning and set _doRespawn to True.<BR><BR>
	 *
	 * @return The number of L2NpcInstance that were spawned
	 */
	public int init()
	{
		while(_currentCount < _maximumCount)
		{
			doSpawn();
		}
		_doRespawn = true;

		return _currentCount;
	}

	/**
	 * Create a L2NpcInstance in this L2Spawn.<BR><BR>
	 * @param isSummon is Summon Spawn
	 * @return doSpawn
	 */
	public L2Npc spawnOne(boolean isSummon)
	{
		return doSpawn(isSummon);
	}

	/**
	 * Return true if respawn enabled
	 */
	public boolean isRespawnEnabled()
	{
		return _doRespawn;
	}

	/**
	 * Set _doRespawn to False to stop respawn in thios L2Spawn.<BR><BR>
	 */
	public void stopRespawn()
	{
		_doRespawn = false;
	}

	/**
	 * Set _doRespawn to True to start or restart respawn in this L2Spawn.<BR><BR>
	 */
	public void startRespawn()
	{
		_doRespawn = true;
	}

	public L2Npc doSpawn()
	{
		return doSpawn(false);
	}

	/**
	 * Create the L2NpcInstance, add it to the world and lauch its OnSpawn action.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * L2NpcInstance can be spawned either in a random position into a location area (if Lox=0 and Locy=0), either at an exact position.
	 * The heading of the L2NpcInstance can be a random heading if not defined (value= -1) or an exact heading (ex : merchant...).<BR><BR>
	 * <p/>
	 * <B><U> Actions for an random spawn into location area</U> : <I>(if Locx=0 and Locy=0)</I></B><BR><BR>
	 * <li>Get L2NpcInstance Init parameters and its generate an Identifier </li>
	 * <li>Call the constructor of the L2NpcInstance </li>
	 * <li>Calculate the random position in the location area (if Locx=0 and Locy=0) or get its exact position from the L2Spawn </li>
	 * <li>Set the position of the L2NpcInstance </li>
	 * <li>Set the HP and MP of the L2NpcInstance to the max </li>
	 * <li>Set the heading of the L2NpcInstance (random heading if not defined : value=-1) </li>
	 * <li>Link the L2NpcInstance to this L2Spawn </li>
	 * <li>Init other values of the L2NpcInstance (ex : from its L2CharTemplate for INT, STR, DEX...) and add it in the world </li>
	 * <li>Lauch the action OnSpawn fo the L2NpcInstance </li><BR><BR>
	 * <li>Increase the current number of L2NpcInstance managed by this L2Spawn  </li><BR><BR>
	 * @param isSummonSpawn is Summon spawn?
	 * @return mob instance
	 */
	public L2Npc doSpawn(boolean isSummonSpawn)
	{
		L2Npc mob = null;
		try
		{
			// Check if the L2Spawn is not a L2Pet or L2Minion or L2Decoy spawn
			if(_template.isType("L2Pet") || _template.isType("L2Decoy") || _template.isType("L2Trap") || _template.isType("L2EffectPoint"))
			{
				_currentCount++;

				return mob;
			}

			// Get L2NpcInstance Init parameters and its generate an Identifier
			Object[] parameters = {IdFactory.getInstance().getNextId(), _template};

			// Call the constructor of the L2NpcInstance
			// (can be a L2ArtefactInstance, L2FriendlyMobInstance, L2GuardInstance, L2MonsterInstance, L2SiegeGuardInstance, L2BoxInstance,
			// L2FeedableBeastInstance, L2TamedBeastInstance, L2FolkInstance or L2TvTEventNpcInstance)
			Object tmp = _constructor.newInstance(parameters);
			((L2Object) tmp).getInstanceController().setInstanceId(_instanceId); // Must be done before object is spawned into visible world
			if(isSummonSpawn && tmp instanceof L2Character)
			{
				((L2Character) tmp).setShowSummonAnimation(isSummonSpawn);
			}
			// Check if the Instance is a L2NpcInstance
			if(!(tmp instanceof L2Npc))
			{
				return mob;
			}
			mob = (L2Npc) tmp;
			return initializeNpcInstance(mob);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "NPC " + _template.getNpcId() + " class " + _template.getType() + " not found", e);
		}
		return mob;
	}

	/**
	 * @param mob
	 * @return
	 */
	private L2Npc initializeNpcInstance(L2Npc mob)
	{
		int newlocx;
		int newlocy;
		int newlocz;

		// If Locx=0 and Locy=0, the L2NpcInstance must be spawned in an area defined by location
		if(_locX == 0 && _locY == 0)
		{
			if(_location == 0)
			{
				return mob;
			}

			// Calculate the random position in the location area
			int[] p = LocationsTable.getInstance().getRandomPoint(_location);

			// Set the calculated position of the L2NpcInstance
			newlocx = p[0];
			newlocy = p[1];
			newlocz = GeoEngine.getInstance().getSpawnHeight(newlocx, newlocy, p[2], p[3]) + 40;
		}
		else
		{
			// The L2NpcInstance is spawned at the exact position (Lox, Locy, Locz)
			newlocx = _locX;
			newlocy = _locY;
			newlocz = _locZ;
		}

		mob.stopAllEffects();

		mob.setIsDead(false);
		// Reset decay info
		mob.setDecayed(false);
		// Set the HP and MP of the L2NpcInstance to the max
		mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());

		// Set the heading of the L2NpcInstance (random heading if not defined)
		if(_heading == -1)
		{
			mob.setHeading(Rnd.get(61794));
		}
		else
		{
			mob.setHeading(_heading);
		}

		if(mob instanceof L2Attackable)
		{
			((L2Attackable) mob).setChampion(false);
		}

		if(Config.CHAMPION_ENABLE)
		{
			// Set champion on next spawn
			if(mob instanceof L2MonsterInstance && !_template.isQuestMonster() && !mob.isRaid() && !mob.isRaidMinion() && Config.CHAMPION_FREQUENCY > 0 && mob.getLevel() >= Config.CHAMP_MIN_LVL && mob.getLevel() <= Config.CHAMP_MAX_LVL && (Config.CHAMPION_ENABLE_IN_INSTANCES || _instanceId == 0))
			{
				if(Rnd.getChance(Config.CHAMPION_FREQUENCY))
				{
					((L2Attackable) mob).setChampion(true);
				}
			}
		}

		// Link the L2NpcInstance to this L2Spawn
		mob.setSpawn(this);

		// Init other values of the L2NpcInstance (ex : from its L2CharTemplate for INT, STR, DEX...) and add it in the world as a visible object
		mob.getLocationController().spawn(newlocx, newlocy, newlocz);

		notifyNpcSpawned(mob);

		_lastSpawn = mob;

		// Increase the current number of L2NpcInstance managed by this L2Spawn
		_currentCount++;
		return mob;
	}

	public L2Npc getLastSpawn()
	{
		return _lastSpawn;
	}

	/**
	 * @param oldNpc
	 */
	public void respawnNpc(L2Npc oldNpc)
	{
		if(_doRespawn)
		{
			// TODO: That's should be done within LocationController.
			WorldManager.getInstance().removeVisibleObject(oldNpc, oldNpc.getLocationController().getWorldRegion());
			WorldManager.getInstance().removeObject(oldNpc);

			oldNpc.refreshID();
			initializeNpcInstance(oldNpc);
		}
	}

	public L2NpcTemplate getTemplate()
	{
		return _template;
	}

	public int getInstanceId()
	{
		return _instanceId;
	}

	public void setInstanceId(int instanceId)
	{
		_instanceId = instanceId;
	}

	@Override
	public String toString()
	{
		return "L2Spawn [_template=" + getNpcId() + ", _locX=" + _locX + ", _locY=" + _locY + ", _locZ=" + _locZ + ", _heading=" + _heading + ']';
	}

	public int getOnKillDelay()
	{
		return _onKillDelay;
	}

	public void setOnKillDelay(int delay)
	{
		_onKillDelay = delay;
	}

	/**
	 * The task launching the function doSpawn()
	 */
	class SpawnTask implements Runnable
	{
		private L2Npc _oldNpc;

		public SpawnTask(L2Npc pOldNpc)
		{
			_oldNpc = pOldNpc;
		}

		@Override
		public void run()
		{
			try
			{
				respawnNpc(_oldNpc);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
			_scheduledCount--;
		}
	}
}
