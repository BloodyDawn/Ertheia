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
package dwo.gameserver.util;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.npc.L2MinionData;
import javolution.util.FastList;
import javolution.util.FastSet;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author luisantonioa, DS
 */
public class MinionList
{
	private static Logger _log = LogManager.getLogger(MinionList.class);

	private final L2MonsterInstance _master;
	/** List containing the current spawned minions */
	private final List<L2MonsterInstance> _minionReferences;
	/** List containing the cached deleted minions for reuse */
	private List<L2MonsterInstance> _reusedMinionReferences;

	public MinionList(L2MonsterInstance pMaster)
	{
		if(pMaster == null)
		{
			throw new NullPointerException("MinionList: master is null");
		}

		_master = pMaster;
		_minionReferences = new FastList<L2MonsterInstance>().shared();
	}

	/**
	 * Init a Minion and add it in the world as a visible object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the template of the Minion to spawn</li> <li>Create and Init the
	 * Minion and generate its Identifier</li> <li>Set the Minion HP, MP and
	 * Heading</li> <li>Set the Minion leader to this RaidBoss</li> <li>Init the
	 * position of the Minion and add it in the world as a visible object</li><BR>
	 * <BR>
	 *
	 * @param master
	 *            L2MonsterInstance used as master for this minion
	 * @param minionId
	 *            The L2NpcTemplate Identifier of the Minion to spawn
	 */
	public static L2MonsterInstance spawnMinion(L2MonsterInstance master, int minionId)
	{
		// Get the template of the Minion to spawn
		L2NpcTemplate minionTemplate = NpcTable.getInstance().getTemplate(minionId);
		if(minionTemplate == null)
		{
			return null;
		}

		// Create and Init the Minion and generate its Identifier
		L2MonsterInstance minion = new L2MonsterInstance(IdFactory.getInstance().getNextId(), minionTemplate);
		return initializeNpcInstance(master, minion);
	}

	private static L2MonsterInstance initializeNpcInstance(L2MonsterInstance master, L2MonsterInstance minion)
	{
		minion.stopAllEffects();
		minion.setIsDead(false);
		minion.setDecayed(false);

		// Set the Minion HP, MP and Heading
		minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp());
		minion.setHeading(master.getHeading());

		// Set the Minion leader to this RaidBoss
		minion.setLeader(master);

		// move monster to masters instance
		minion.getInstanceController().setInstanceId(master.getInstanceId());

		// Init the position of the Minion and add it in the world as a visible
		// object
		int offset = 200;
		int minRadius = (int) master.getCollisionRadius() + 30;

		int newX = Rnd.get(minRadius << 1, offset << 1); // x
		int newY = Rnd.get(newX, offset << 1); // distance
		newY = (int) Math.sqrt(newY * newY - newX * newX); // y
		newX = newX > offset + minRadius ? master.getX() + newX - offset : master.getX() - newX + minRadius;
		newY = newY > offset + minRadius ? master.getY() + newY - offset : master.getY() - newY + minRadius;

		minion.getLocationController().spawn(newX, newY, master.getZ());

		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "Spawned minion template " + minion.getNpcId() + " with objid: " + minion.getObjectId() + " to boss " + master.getObjectId() + " ,at: " + minion.getX() + " x, " + minion.getY() + " y, " + minion.getZ() + " z");
		}

		return minion;
	}

	/**
	 * Returns list of the spawned (alive) minions.
	 */
	public List<L2MonsterInstance> getSpawnedMinions()
	{
		return _minionReferences;
	}

	/**
	 * Manage the spawn of Minions.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the Minion data of all Minions that must be spawn</li> <li>For
	 * each Minion type, spawn the amount of Minion needed</li><BR>
	 * <BR>
	 */
	public void spawnMinions()
	{
		if(_master.isAlikeDead())
		{
			return;
		}
		List<L2MinionData> minions = _master.getTemplate().getMinionData();
		if(minions == null)
		{
			return;
		}

		int minionCount;
		int minionId;
		int minionsToSpawn;
		for(L2MinionData minion : minions)
		{
			minionCount = minion.getAmount();
			minionId = minion.getMinionId();

			minionsToSpawn = minionCount - countSpawnedMinionsById(minionId);
			if(minionsToSpawn > 0)
			{
				for(int i = 0; i < minionsToSpawn; i++)
				{
					spawnMinion(minionId);
				}
			}
		}
		// remove non-needed minions
		deleteReusedMinions();
	}

	// hooks

	/**
	 * Delete all spawned minions and try to reuse them.
	 */
	public void deleteSpawnedMinions()
	{
		if(!_minionReferences.isEmpty())
		{
			_minionReferences.stream().filter(minion -> minion != null).forEach(minion -> {
				minion.setLeader(null);
				minion.getLocationController().delete();
				if(_reusedMinionReferences != null)
				{
					_reusedMinionReferences.add(minion);
				}
			});
			_minionReferences.clear();
		}
	}

	/**
	 * Delete all reused minions to prevent memory leaks.
	 */
	public void deleteReusedMinions()
	{
		if(_reusedMinionReferences != null)
		{
			_reusedMinionReferences.clear();
		}
	}

	/**
	 * Called on the master spawn
	 * Old minions (from previous spawn) are deleted.
	 * If master can respawn - enabled reuse of the killed minions.
	 */
	public void onMasterSpawn()
	{
		deleteSpawnedMinions();

		// if master has spawn and can respawn - try to reuse minions
		if(_reusedMinionReferences == null && _master.getTemplate().getMinionData() != null && _master.getSpawn() != null && _master.getSpawn().isRespawnEnabled())
		{
			_reusedMinionReferences = new FastList<L2MonsterInstance>().shared();
		}
	}

	/**
	 * Called on the minion spawn
	 * and added them in the list of the spawned minions.
	 */
	public void onMinionSpawn(L2MonsterInstance minion)
	{
		_minionReferences.add(minion);
	}

	/**
	 * Called on the master death/delete.
	 *
	 * @param force
	 *            if true - force delete of the spawned minions
	 *            By default minions deleted only for raidbosses
	 */
	public void onMasterDie(boolean force)
	{
		if(_master.isRaid() || force)
		{
			deleteSpawnedMinions();
		}
	}

	/**
	 * Called on the minion death/delete.
	 * Removed minion from the list of the spawned minions and reuse if
	 * possible.
	 *
	 * @param respawnTime
	 *            (ms) enable respawning of this minion while master is alive.
	 *            -1 - use default value: 0 (disable) for mobs and config value
	 *            for raids.
	 */
	public void onMinionDie(L2MonsterInstance minion, int respawnTime)
	{
		if(minion == null)
		{
			return;
		}

		minion.setLeader(null); // prevent memory leaks
		_minionReferences.remove(minion);
		if(_reusedMinionReferences != null)
		{
			_reusedMinionReferences.add(minion);
		}

		int time = respawnTime < 0 ? _master.isRaid() ? (int) Config.RAID_MINION_RESPAWN_TIMER : 0 : respawnTime;
		if(time > 0 && !_master.isAlikeDead())
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new MinionRespawnTask(minion), time);
		}
	}

	/**
	 * Called if master/minion was attacked.
	 * Master and all free minions receive aggro against attacker.
	 */
	public void onAssist(L2Character caller, L2Character attacker)
	{
		if(attacker == null)
		{
			return;
		}

		if(!_master.isAlikeDead() && !_master.isInCombat())
		{
			_master.addDamageHate(attacker, 0, 1);
		}

		boolean callerIsMaster = caller.equals(_master);
		int aggro = callerIsMaster ? 10 : 1;
		if(_master.isRaid())
		{
			aggro *= 10;
		}

		for(L2MonsterInstance minion : _minionReferences)
		{
			if(minion != null && !minion.isDead() && (callerIsMaster || !minion.isInCombat()))
			{
				minion.addDamageHate(attacker, 0, aggro);
			}
		}
	}

	/**
	 * Called from onTeleported() of the master
	 * Alive and able to move minions teleported to master.
	 */
	public void onMasterTeleported()
	{
		int offset = 200;
		int minRadius = (int) _master.getCollisionRadius() + 30;

		for(L2MonsterInstance minion : _minionReferences)
		{
			if(minion != null && !minion.isDead() && !minion.isMovementDisabled())
			{
				int newX = Rnd.get(minRadius << 1, offset << 1); // x
				int newY = Rnd.get(newX, offset << 1); // distance
				newY = (int) Math.sqrt(newY * newY - newX * newX); // y
				newX = newX > offset + minRadius ? _master.getX() + newX - offset : _master.getX() - newX + minRadius;
				newY = newY > offset + minRadius ? _master.getY() + newY - offset : _master.getY() - newY + minRadius;

				minion.teleToLocation(newX, newY, _master.getZ());
			}
		}
	}

	private void spawnMinion(int minionId)
	{
		if(minionId == 0)
		{
			return;
		}

		// searching in reused minions
		if(_reusedMinionReferences != null && !_reusedMinionReferences.isEmpty())
		{
			L2MonsterInstance minion;
			Iterator<L2MonsterInstance> iter = _reusedMinionReferences.iterator();
			while(iter.hasNext())
			{
				minion = iter.next();
				if(minion != null && minion.getNpcId() == minionId)
				{
					iter.remove();
					minion.refreshID();
					initializeNpcInstance(_master, minion);
					return;
				}
			}
		}
		// not found in cache
		spawnMinion(_master, minionId);
	}

	private int countSpawnedMinionsById(int minionId)
	{
		int count = 0;
		for(L2MonsterInstance minion : _minionReferences)
		{
			if(minion != null && minion.getNpcId() == minionId)
			{
				count++;
			}
		}
		return count;
	}

	// Statistics part

	public int countSpawnedMinions()
	{
		return _minionReferences.size();
	}

	public int lazyCountSpawnedMinionsGroups()
	{
		Set<Integer> seenGroups = new FastSet<>();
		for(L2MonsterInstance minion : _minionReferences)
		{
			if(minion == null)
			{
				continue;
			}

			seenGroups.add(minion.getNpcId());
		}
		return seenGroups.size();
	}

	private class MinionRespawnTask implements Runnable
	{
		private final L2MonsterInstance _minion;

		public MinionRespawnTask(L2MonsterInstance minion)
		{
			_minion = minion;
		}

		@Override
		public void run()
		{
			if(!_master.isAlikeDead() && _master.isVisible())
			{
				// minion can be already spawned or deleted
				if(!_minion.isVisible())
				{
					if(_reusedMinionReferences != null)
					{
						_reusedMinionReferences.remove(_minion);
					}

					_minion.refreshID();
					initializeNpcInstance(_master, _minion);
				}
			}
		}
	}
}
