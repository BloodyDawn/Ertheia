package dwo.scripts.dynamicspawn;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.DynamicSpawnData;
import dwo.gameserver.model.holders.SpawnsHolder;
import dwo.gameserver.model.world.quest.Quest;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 24.03.12
 * Time: 15:05
 */

public class TestCats extends Quest
{
	public TestCats()
	{
		SpawnsHolder holder = DynamicSpawnData.getInstance().getSpawnsHolder("TestServerCats");
		if(holder == null)
		{
			_log.log(Level.ERROR, "Spawn holder [TestServerCats] for class: " + getClass().getSimpleName() + " is null!");
			return;
		}
		holder.spawnAll();
	}

	public static void main(String[] args)
	{
		if(Config.ENABLE_TEST_CATS_SPAWN)
		{
			_log.log(Level.INFO, "[DYNAMIC SPAWNS] : Spawning test cats...");
			new TestCats();
		}
	}
}