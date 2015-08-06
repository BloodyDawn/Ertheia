package dwo.config.scripts;

import dwo.config.PropertyListenerImpl;
import jfork.nproperty.Cfg;
import jfork.nproperty.ConfigParser;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 10.03.13
 * Time: 21:17
 */

@Cfg
public class ConfigWorldStatistic extends PropertyListenerImpl
{
	public static boolean WORLD_STATISTIC_ENABLED;

	private ConfigWorldStatistic()
	{
		String path = "./config/scripts/WorldStatistic.ini";
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigParser.parse(this, path);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}

	public static void loadConfig()
	{
		new ConfigWorldStatistic();
	}
}