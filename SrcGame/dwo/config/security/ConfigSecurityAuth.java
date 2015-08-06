package dwo.config.security;

import dwo.config.Config;
import dwo.config.ConfigProperties;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: Bacek
 * Date: xx.xx.xx
 * Time: xx:xx
 */

public class ConfigSecurityAuth extends Config
{
	private static final String path = SECURITY_CONFIG_FILE;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			SECOND_AUTH_ENABLED = getBoolean(properties, "SecondAuthEnabled", false);
			SECOND_AUTH_MAX_ATTEMPTS = getInt(properties, "SecondAuthMaxAttempts", 5);
			SECOND_AUTH_BAN_TIME = getInt(properties, "SecondAuthBanTime", 480);
			SECOND_AUTH_REC_LINK = getString(properties, "SecondAuthRecoveryLink", "5");
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
