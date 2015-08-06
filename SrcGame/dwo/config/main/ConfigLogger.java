package dwo.config.main;

import dwo.config.Config;
import dwo.config.ConfigProperties;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.08.11
 * Time: 18:52
 */
public class ConfigLogger extends Config
{
	private static final String path = LOGGER_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			ALLOW_LOG_FILE = getBoolean(properties, "AllowLogToFiles", true);
			LOG_CHAT = getBoolean(properties, "LogChat", false);
			LOG_ITEMS = getBoolean(properties, "LogItems", false);
			LOG_ITEM_ENCHANTS = getBoolean(properties, "LogItemEnchants", false);
			LOG_SKILL_ENCHANTS = getBoolean(properties, "LogSkillEnchants", false);
			GMAUDIT = getBoolean(properties, "GMAudit", false);
			LOG_GAME_DAMAGE = getBoolean(properties, "LogGameDamage", false);
			LOG_GAME_DAMAGE_THRESHOLD = getInt(properties, "LogGameDamageThreshold", 5000);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}