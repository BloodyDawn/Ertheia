package dwo.config.main;

import dwo.config.PropertyListenerImpl;
import jfork.nproperty.Cfg;
import jfork.nproperty.CfgSplit;
import jfork.nproperty.ConfigParser;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 17.10.12
 * Time: 5:21
 */

@Cfg
public class ConfigLocalization extends PropertyListenerImpl
{
	public static final String PATH = "./config/main/Localization.ini";

	public static boolean MULTILANG_ENABLE = true;

	@CfgSplit(splitter = ";")
	public static String[] MULTILANG_ALLOWED = {"ru", "en"};

	public static String MULTILANG_DEFAULT = "ru";

	public ConfigLocalization()
	{
		try
		{
			ConfigParser.parse(this, PATH, true);
		}
		catch(Exception e)
		{
			throw new Error("Failed to load " + PATH + " config file.", e);
		}
		finally
		{
			ConfigParser.cleanCache(PATH);
		}
	}

	public static void loadConfig()
	{
		new ConfigLocalization();
	}
}