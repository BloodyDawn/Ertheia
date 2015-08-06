package dwo.config.mods;

import dwo.config.Config;
import dwo.config.ConfigProperties;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 27.05.12
 * Time: 22:32
 */

public class ConfigPremium extends Config
{
	private static final String path = PREMIUM_CONFIGURATION_FILE;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			PREMIUM_ENABLED = getBoolean(properties, "PremiumEnable", false);
			PREMIUM_EXPSP_RATE = getDouble(properties, "PremiumExpSpRate", 2.0);
			PREMIUM_DROP_ITEM_RATE = getDouble(properties, "PremiumDropItemRate", 2.0);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}