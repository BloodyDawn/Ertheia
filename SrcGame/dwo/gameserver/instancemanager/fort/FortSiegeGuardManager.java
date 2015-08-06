package dwo.gameserver.instancemanager.fort;

import dwo.gameserver.datatables.xml.FortSpawnList;
import dwo.gameserver.model.holders.FortFacilitySpawnHolder;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.residence.fort.FortSpawnType;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

public class FortSiegeGuardManager
{
	private static final Logger _log = LogManager.getLogger(FortSiegeGuardManager.class);

	private Fort _fort;

	public FortSiegeGuardManager(Fort fort)
	{
		_fort = fort;
	}

	/***
	 * Спауним гвардов форта в зависимости от уровня Facility текущего форта
	 */
	public void spawnOnSiegeGuard()
	{
		try
		{
			List<FortFacilitySpawnHolder> guardList = FortSpawnList.getInstance().getSpawnForFort(_fort, FortSpawnType.SPAWN_ON_SIEGE);
			if(guardList != null)
			{
				for(FortFacilitySpawnHolder spawnDat : guardList)
				{
					// Если список не требует вообще апгрейда форта или если форт сейчас у NPC
					if(spawnDat.getType() == null || _fort.getOwnerClan() == null)
					{
						for(L2Spawn spawn : spawnDat.getSpawnList())
						{
							spawn.doSpawn();
							spawn.startRespawn();
						}
					}
					// Если список спауна требует определенный уровень апгрейда и у форта этот апгрейд есть
					else if(_fort.getFacilityLevel(spawnDat.getType()) != 0)
					{
						if(_fort.getFacilityLevel(spawnDat.getType()) >= spawnDat.getLevel())
						{
							for(L2Spawn spawn : spawnDat.getSpawnList())
							{
								spawn.doSpawn();
								spawn.startRespawn();
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, getClass().getSimpleName() + ": Error spawning siege guards for fort " + _fort.getName() + ':' + e.getMessage(), e);
		}
	}

	/***
	 * Убираем весь спаун гвардов из форта
	 */
	public void unspawnOnSiegeGuard()
	{
		try
		{
			List<FortFacilitySpawnHolder> guardList = FortSpawnList.getInstance().getSpawnForFort(_fort, FortSpawnType.SPAWN_ON_SIEGE);

			if(guardList != null)
			{
				for(FortFacilitySpawnHolder holder : guardList)
				{
					for(L2Spawn spawn : holder.getSpawnList())
					{
						spawn.stopRespawn();
						if(spawn.getLastSpawn() != null)
						{
							spawn.getLastSpawn().doDie(spawn.getLastSpawn());
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "FortSiegeGuardManager: Error unspawning siege guards for fort " + _fort.getName() + ':' + e.getMessage(), e);
		}
	}

	public Fort getFort()
	{
		return _fort;
	}
}
