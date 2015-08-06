package dwo.config.network;

import dwo.config.PropertyListenerImpl;
import jfork.nproperty.Cfg;
import jfork.nproperty.ConfigParser;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 01.07.13
 * Time: 16:53
 */

@Cfg
public class ConfigGuardEngine extends PropertyListenerImpl
{
	public static boolean GUARD_ENGINE_ENABLE;
	public static String GUARD_ENGINE_STATIC_KEY;

	public static boolean GUARD_ENGINE_MAX_WINDOWS_RESTRICT_ENABLE;
	public static int GUARD_ENGINE_MAX_WINDOWS_COUNT;

	public static int GUARDENGINE_BANTIME_PACKETHACK;
	public static int GUARDENGINE_BANTIME_CLIENT_HACK;
	public static int GUARDENGINE_BANTIME_INGAME_BOT;
	public static int GUARDENGINE_BANTIME_BAD_APPLICATION;

	private ConfigGuardEngine()
	{
		String path = "./config/network/GuardEngine.ini";
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
		new ConfigGuardEngine();
	}
}