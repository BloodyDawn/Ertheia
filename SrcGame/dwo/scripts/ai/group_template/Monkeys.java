package dwo.scripts.ai.group_template;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.instancemanager.WalkingManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 01.12.11
 * Time: 6:29
 */

public class Monkeys extends Quest
{
	private static final int _monkeyIsolated = 33203;
	private static final int _monkeyIdGroup = 33198;

	private static final List<L2Npc> _monkeyGroup1 = new ArrayList<>(5);
	private static final List<L2Npc> _monkeyGroup2 = new ArrayList<>(5);
	private static final List<L2Npc> _monkeyGroup3 = new ArrayList<>(6);

	public Monkeys()
	{
		// Первая группа бибизянок
		_monkeyGroup1.add(spawnMonkey(_monkeyIdGroup, new Location(-109539, 246350, -3000)));
		_monkeyGroup1.add(spawnMonkey(_monkeyIdGroup, new Location(-109539, 246250, -2992)));
		_monkeyGroup1.add(spawnMonkey(_monkeyIdGroup, new Location(-109539, 246150, -2984)));
		_monkeyGroup1.add(spawnMonkey(_monkeyIdGroup, new Location(-109539, 246050, -2976)));
		_monkeyGroup1.add(spawnMonkey(_monkeyIdGroup, new Location(-109539, 246000, -2976)));
		// Вторая группа бибизянок
		_monkeyGroup2.add(spawnMonkey(_monkeyIdGroup, new Location(-109638, 246350, -2992)));
		_monkeyGroup2.add(spawnMonkey(_monkeyIdGroup, new Location(-109638, 246250, -2984)));
		_monkeyGroup2.add(spawnMonkey(_monkeyIdGroup, new Location(-109638, 246150, -2984)));
		_monkeyGroup2.add(spawnMonkey(_monkeyIdGroup, new Location(-109638, 246050, -2976)));
		_monkeyGroup2.add(spawnMonkey(_monkeyIdGroup, new Location(-109638, 246000, -2968)));
		// Третья группа бибизянок
		_monkeyGroup3.add(spawnMonkey(_monkeyIdGroup, new Location(-109150, 237450, -2928)));
		_monkeyGroup3.add(spawnMonkey(_monkeyIdGroup, new Location(-109050, 237450, -2928)));
		_monkeyGroup3.add(spawnMonkey(_monkeyIdGroup, new Location(-109950, 237450, -2928)));
		_monkeyGroup3.add(spawnMonkey(_monkeyIdGroup, new Location(-109850, 237450, -2928)));
		_monkeyGroup3.add(spawnMonkey(_monkeyIdGroup, new Location(-109750, 237450, -2928)));
		_monkeyGroup3.add(spawnMonkey(_monkeyIdGroup, new Location(-109650, 237450, -2928)));
		ThreadPoolManager.getInstance().scheduleGeneral(new StartMonkeys(), 45000);
	}

	public static void main(String[] args)
	{
		new Monkeys();
	}

	private L2Npc spawnMonkey(int npcId, Location loc)
	{
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		try
		{
			L2Spawn npcSpawn = new L2Spawn(npcTemplate);
			npcSpawn.setLocation(loc);
			npcSpawn.setHeading(0);
			npcSpawn.setAmount(1);
			SpawnTable.getInstance().addNewSpawn(npcSpawn);
			return npcSpawn.spawnOne(false);
		}
		catch(Exception ignored)
		{
		}
		return null;
	}

	private class StartMonkeys implements Runnable
	{
		public StartMonkeys()
		{
		}

		@Override
		public void run()
		{
			for(L2Npc m1 : _monkeyGroup1)
			{
				WalkingManager.getInstance().startMoving(m1, 13);
			}
			for(L2Npc m2 : _monkeyGroup2)
			{
				WalkingManager.getInstance().startMoving(m2, 14);
			}
			for(L2Npc m3 : _monkeyGroup3)
			{
				WalkingManager.getInstance().startMoving(m3, 15);
			}
		}
	}
}
