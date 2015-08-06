package dwo.gameserver.model.holders;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;

public class SpawnHolder
{
	private final StatsSet _set;
	private final Location _loc;

	public SpawnHolder(StatsSet set)
	{
		_set = set;
		_loc = new Location(set);
	}

	public StatsSet getStatsSet()
	{
		return _set;
	}

	public Location getLocation()
	{
		return _loc;
	}

	public L2Npc doSpawn()
	{
		return doSpawn(true, 0);
	}

	public L2Npc doSpawn(int instanceId)
	{
		return doSpawn(true, instanceId);
	}

	public L2Npc doSpawn(boolean register, int instanceId)
	{
		int npcId = _set.getInteger("npcid");
		L2Npc result = null;
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if(template != null)
			{
				int x = _set.getInteger("x");
				int y = _set.getInteger("y");
				int z = _set.getInteger("z");
				boolean randomOffset = _set.getBool("randomoffset", false);
				int heading = _set.getInteger("heading", 0);
				boolean isSummonSpawn = _set.getBool("issummonspawn", false);
				int despawnDelay = _set.getInteger("despawndelay", 0);
				int respawnDelay = _set.getInteger("delay", 0);
				if(x == 0 && y == 0)
				{
					return null;
				}
				if(randomOffset)
				{
					int offset;

					offset = Rnd.get(2); // Get the direction of the offset
					if(offset == 0)
					{
						offset = -1;
					} // make offset negative
					offset *= Rnd.get(50, 100);
					x += offset;

					offset = Rnd.get(2); // Get the direction of the offset
					if(offset == 0)
					{
						offset = -1;
					} // make offset negative
					offset *= Rnd.get(50, 100);
					y += offset;
				}
				L2Spawn spawn = new L2Spawn(template);
				spawn.setInstanceId(instanceId);
				spawn.setHeading(heading);
				spawn.setLocx(x);
				spawn.setLocy(y);
				spawn.setLocz(z + 20);

				if(respawnDelay > 0)
				{
					spawn.setRespawnDelay(respawnDelay);
					spawn.startRespawn();
					spawn.setAmount(1);
				}
				else
				{
					spawn.stopRespawn();
				}

				result = spawn.spawnOne(isSummonSpawn);

				if(despawnDelay > 0)
				{
					result.scheduleDespawn(despawnDelay);
				}

				if(register)
				{
					SpawnTable.getInstance().addNewSpawn(spawn);
				}
			}
		}
		catch(Exception ignored)
		{
		}

		return result;
	}

	public void unSpawn()
	{
		unSpawn(0);
	}

	public void unSpawn(int instanceId)
	{
		SpawnTable.getInstance().getSpawns(_set.getInteger("npcid")).stream().filter(spawn -> spawn.getLocx() == _set.getInteger("x") && spawn.getLocy() == _set.getInteger("y") && spawn.getInstanceId() == instanceId).forEach(spawn -> {
			spawn.getLastSpawn().getLocationController().delete();
			spawn.stopRespawn();
		});
	}
}
