package dwo.config.mods;

import dwo.config.Config;
import dwo.config.ConfigProperties;
import org.apache.log4j.Level;

public class ConfigChars extends Config
{
	private static final String path = L2JS_CHARS_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			ALLOW_NEW_CHARACTER_TITLE = getBoolean(properties, "AllowNewCharacterTitle", false);
			NEW_CHARACTER_TITLE = getString(properties, "NewCharacterTitle", "GodWorld");

            TITLE_PVP_MODE = getBoolean(properties, "TitlePVPMode", false);
            TITLE_PVP_MODE_FOR_SELF = getBoolean(properties, "TitlePVPModeSelf", false);
            TITLE_PVP_MODE_RATE = getFloat(properties, "TitlePVPModeRate", 1.5f);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}