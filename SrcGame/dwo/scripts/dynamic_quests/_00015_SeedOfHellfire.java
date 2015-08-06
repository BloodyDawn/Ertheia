package dwo.scripts.dynamic_quests;

import dwo.gameserver.datatables.xml.DynamicQuestsData;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.instancemanager.WalkingManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest;
import dwo.gameserver.model.world.zone.Location;

import java.util.Set;

/**
 * User: Keiichi
 * Date: 03.11.12
 * Time: 21:15
 * L2GOD Team/
 */
public class _00015_SeedOfHellfire extends DynamicQuest
{
	//int [] _npc = {23233, 23220};

	private static final int СмотрительКунда = 23220;
	private static final int ИнженерСофа = 23233;

	private _00015_SeedOfHellfire(int id)
	{
		super(DynamicQuestsData.getInstance().getQuest(id));
	}

	public static void main(String[] args)
	{
		new _00015_SeedOfHellfire(1501);
	}

	/*
	 * При старте компании когда спавнятся нпц, спавним нпц которые должны бегать от точки к точке, механизм решил использовать как и в WalkingNpcs спавним нпц и запускаем бегать его по routeId.
	 * WalkingManager.getInstance().startMoving(getNpcSpawnInstance(npcId, new Location(x,x,z), respawnDelay), routeId);
	 */
	@Override
	public void onCampainStart()
	{
		// Добавить остальных смотрителей.
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(23220, new Location(-145489, 149324, -11981), 30), 39);
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(23220, new Location(-145489, 145154, -11981), 30), 40);
	}

	@Override
	public void onCampainDone(boolean succeed)
	{
		Set<L2Spawn> spawns = SpawnTable.getInstance().getSpawns(СмотрительКунда);
		for(L2Spawn spawn : spawns)
		{
			WalkingManager.getInstance().cancelMoving(spawn.getLastSpawn(), true);
			spawn.stopRespawn();
		}

		spawns = SpawnTable.getInstance().getSpawns(ИнженерСофа);
		for(L2Spawn spawn : spawns)
		{
			WalkingManager.getInstance().cancelMoving(spawn.getLastSpawn(), true);
			spawn.stopRespawn();
		}
	}

	private L2Npc getNpcSpawnInstance(int npcId, Location loc, int respawnDelay)
	{
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);

		try
		{
			L2Spawn npcSpawn = new L2Spawn(npcTemplate);

			npcSpawn.setLocation(loc);
			npcSpawn.setHeading(0);
			npcSpawn.setAmount(1);
			if(respawnDelay == 0)
			{
				npcSpawn.stopRespawn();
			}
			else
			{
				npcSpawn.setRespawnDelay(respawnDelay);
				npcSpawn.startRespawn();
			}
			SpawnTable.getInstance().addNewSpawn(npcSpawn);

			return npcSpawn.spawnOne(false);
		}
		catch(Exception ignored)
		{
		}
		return null;
	}
}