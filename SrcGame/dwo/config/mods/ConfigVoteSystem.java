package dwo.config.mods;

import dwo.config.Config;
import dwo.config.ConfigProperties;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.07.11
 * Time: 12:13
 **/

public class ConfigVoteSystem extends Config
{
	private static final String path = VOTE_SYSTEM_CONFIG_FILE;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			MMO_TOP_MANAGER_ENABLED = getBoolean(properties, "MMOTopEnable", false);
			MMO_TOP_MANAGER_INTERVAL = getInt(properties, "MMOTopManagerInterval", 300000);
			MMO_TOP_WEB_ADDRESS = getString(properties, "MMOTopUrl", "");
			MMO_TOP_SAVE_DAYS = getInt(properties, "MMOTopSaveDays", 30);
			MMO_TOP_REWARD = getIntArray(properties, "MMOTopReward", new int[0], ",");
			MMO_TOP_REWARD_NO_CLAN = getIntArray(properties, "MMOTopRewardNoClan", new int[0], ",");

			L2_TOP_MANAGER_ENABLED = getBoolean(properties, "L2TopManagerEnabled", false);
			L2_TOP_MANAGER_INTERVAL = getInt(properties, "L2TopManagerInterval", 300000);
			L2_TOP_WEB_ADDRESS = getString(properties, "L2TopWebAddress", "");
			L2_TOP_SMS_ADDRESS = getString(properties, "L2TopSmsAddress", "");
			L2_TOP_PREFIX = getString(properties, "L2TopPrefix", "");
			L2_TOP_SAVE_DAYS = getInt(properties, "L2TopSaveDays", 30);
			L2_TOP_REWARD = getIntArray(properties, "L2TopReward", new int[0], ",");
			L2_TOP_REWARD_NO_CLAN = getIntArray(properties, "L2TopRewardNoClan", new int[0], ",");
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}