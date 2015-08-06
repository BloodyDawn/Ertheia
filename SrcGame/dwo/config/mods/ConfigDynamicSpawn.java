package dwo.config.mods;

import dwo.config.Config;
import dwo.config.ConfigProperties;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 24.03.12
 * Time: 15:17
 */

public class ConfigDynamicSpawn extends Config
{
	private static final String path = DYNAMIC_SPAWN_CONFIGURATION_FILE;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			ENABLE_TEST_CATS_SPAWN = getBoolean(properties, "EnableTestCatsSpawn", false);
			ENABLE_NEWS_NPC_SPAWN = getBoolean(properties, "EnableNpcNewsSpawn", false);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
