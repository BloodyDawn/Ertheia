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
package dwo.gameserver.instancemanager;

import dwo.gameserver.GameTimeController;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2RaidBossInstance;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author godson
 */

public class DayNightSpawnManager
{

	private static Logger _log = LogManager.getLogger(DayNightSpawnManager.class);

	private List<L2Spawn> _dayCreatures;
	private List<L2Spawn> _nightCreatures;
	private Map<L2Spawn, L2RaidBossInstance> _bosses;

	//private static int _currentState;  // 0 = Day, 1 = Night

	private DayNightSpawnManager()
	{
		_log.log(Level.INFO, "DayNightSpawnManager: Initializing...");
		_dayCreatures = new ArrayList<>();
		_nightCreatures = new ArrayList<>();
		_bosses = new FastMap<>();
	}

	public static DayNightSpawnManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void addDayCreature(L2Spawn spawnDat)
	{
		_dayCreatures.add(spawnDat);
	}

	public void addNightCreature(L2Spawn spawnDat)
	{
		_nightCreatures.add(spawnDat);
	}

	/**
	 * Spawn Day Creatures, and Unspawn Night Creatures
	 */
	public void spawnDayCreatures()
	{
		spawnCreatures(_nightCreatures, _dayCreatures, "night", "day");
	}

	/**
	 * Spawn Night Creatures, and Unspawn Day Creatures
	 */
	public void spawnNightCreatures()
	{
		spawnCreatures(_dayCreatures, _nightCreatures, "day", "night");
	}

	/**
	 * Manage Spawn/Respawn
	 * Arg 1 : List with spawns must be unspawned
	 * Arg 2 : List with spawns must be spawned
	 * Arg 3 : String for log info for unspawned L2NpcInstance
	 * Arg 4 : String for log info for spawned L2NpcInstance
	 */
	private void spawnCreatures(List<L2Spawn> unSpawnCreatures, List<L2Spawn> spawnCreatures, String UnspawnLogInfo, String SpawnLogInfo)
	{
		try
		{
			if(!unSpawnCreatures.isEmpty())
			{
				int i = 0;
				for(L2Spawn spawn : unSpawnCreatures)
				{
					if(spawn == null)
					{
						continue;
					}

					spawn.stopRespawn();
					L2Npc last = spawn.getLastSpawn();
					if(last != null)
					{
						last.getLocationController().delete();
						i++;
					}
				}
				_log.log(Level.INFO, "DayNightSpawnManager: Removed " + i + ' ' + UnspawnLogInfo + " creatures");
			}

			int i = 0;
			for(L2Spawn spawnDat : spawnCreatures)
			{
				if(spawnDat == null)
				{
					continue;
				}
				spawnDat.startRespawn();
				spawnDat.doSpawn();
				i++;
			}

			_log.log(Level.INFO, "DayNightSpawnManager: Spawned " + i + ' ' + SpawnLogInfo + " creatures");
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while spawning creatures: " + e.getMessage(), e);
		}
	}

	private void changeMode(int mode)
	{
		if(_nightCreatures.isEmpty() && _dayCreatures.isEmpty())
		{
			return;
		}

		switch(mode)
		{
			case 0:
				spawnDayCreatures();
				specialNightBoss(0);
				break;
			case 1:
				spawnNightCreatures();
				specialNightBoss(1);
				break;
			default:
				_log.log(Level.WARN, "DayNightSpawnManager: Wrong mode sent");
				break;
		}
	}

	public DayNightSpawnManager trim()
	{
		((ArrayList<?>) _nightCreatures).trimToSize();
		((ArrayList<?>) _dayCreatures).trimToSize();
		return this;
	}

	public void notifyChangeMode()
	{
		try
		{
			if(GameTimeController.getInstance().isNight())
			{
				changeMode(1);
			}
			else
			{
				changeMode(0);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "DayNightSpawnManager: Error while notifyChangeMode(): " + e.getMessage(), e);
		}
	}

	public void cleanUp()
	{
		_nightCreatures.clear();
		_dayCreatures.clear();
		_bosses.clear();
	}

	private void specialNightBoss(int mode)
	{
		try
		{
			L2RaidBossInstance boss;
			for(Map.Entry<L2Spawn, L2RaidBossInstance> l2SpawnL2RaidBossInstanceEntry : _bosses.entrySet())
			{
				boss = l2SpawnL2RaidBossInstanceEntry.getValue();

				if(boss == null && mode == 1)
				{
					boss = (L2RaidBossInstance) l2SpawnL2RaidBossInstanceEntry.getKey().doSpawn();
					RaidBossSpawnManager.getInstance().notifySpawnNightBoss(boss);
					_bosses.remove(l2SpawnL2RaidBossInstanceEntry.getKey());
					_bosses.put(l2SpawnL2RaidBossInstanceEntry.getKey(), boss);
					continue;
				}

				if(boss == null && mode == 0)
				{
					continue;
				}

				if(boss != null && boss.getNpcId() == 25328 && boss.getRaidStatus() == RaidBossSpawnManager.StatusEnum.ALIVE)
				{
					handleHellmans(boss, mode);
				}
				return;
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while specialNoghtBoss(): " + e.getMessage(), e);
		}
	}

	private void handleHellmans(L2RaidBossInstance boss, int mode)
	{
		switch(mode)
		{
			case 0:
				boss.getLocationController().delete();
				_log.log(Level.INFO, "DayNightSpawnManager: Deleting Hellman raidboss");
				break;
			case 1:
				if(!boss.isVisible())
				{
					boss.getLocationController().spawn();
				}
				_log.log(Level.INFO, "DayNightSpawnManager: Spawning Hellman raidboss");
				break;
		}
	}

	public L2RaidBossInstance handleBoss(L2Spawn spawnDat)
	{
		if(_bosses.containsKey(spawnDat))
		{
			return _bosses.get(spawnDat);
		}

		if(GameTimeController.getInstance().isNight())
		{
			L2RaidBossInstance raidboss = (L2RaidBossInstance) spawnDat.doSpawn();
			_bosses.put(spawnDat, raidboss);
			return raidboss;
		}
		_bosses.put(spawnDat, null);

		return null;
	}

	private static class SingletonHolder
	{
		protected static final DayNightSpawnManager _instance = new DayNightSpawnManager();
	}
}
