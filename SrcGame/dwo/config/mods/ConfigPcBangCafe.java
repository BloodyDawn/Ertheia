package dwo.config.mods;

import dwo.config.Config;
import dwo.config.ConfigProperties;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 05.08.11
 * Time: 19:12
 */
public class ConfigPcBangCafe extends Config
{
	private static final String path = PCCAFE_CONFIGURATION_FILE;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			PCBANG_ACQUISITIONPOINTSRANDOM = getBoolean(properties, "AcquisitionPointsRandom", false);
			PCBANG_ENABLE_DOUBLE_ACQUISITION_POINTS = getBoolean(properties, "DoublingAcquisitionPoints", false);
			PCBANG_DOUBLE_ACQUISITION_CHANCE = getInt(properties, "DoublingAcquisitionPointsChance", 1);
			PCBANG_DOUBLE_ACQUISITION_RATE = getDouble(properties, "AcquisitionPointsRate", 1.0);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
