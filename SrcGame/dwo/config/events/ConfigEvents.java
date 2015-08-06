package dwo.config.events;

import dwo.config.PropertyListenerImpl;
import jfork.nproperty.Cfg;
import jfork.nproperty.ConfigParser;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.xx
 * Time: xx:xx
 */

@Cfg
public class ConfigEvents extends PropertyListenerImpl
{
	public static final String PATH = "./config/events/Events.ini";

	public static boolean ENABLE_ITEM_ON_GAME_TIME_EVENT;

	public static int ITEM_ON_GAME_TIME_GIVE_TIME = 1;

	public static int ITEM_ON_GAME_TIME_GIVE_ITEM_ID = 4037;

	public static int ITEM_ON_GAME_TIME_GIVE_COUNT = 1;

	public static boolean ENABLE_BLOCK_CHECKER_EVENT = true;

	public static int MIN_BLOCK_CHECKER_TEAM_MEMBERS = 2;

	public static boolean HBCE_FAIR_PLAY = true;

	public static boolean EVENT_FREYA_CELEBRATION_ENABLE;

	public static boolean EVENT_GIFT_OF_VITALITY_ENABLE;

	public static boolean EVENT_HEAVY_MEDAL_ENABLE;

	public static boolean EVENT_MASTER_OF_ENCHANTING_ENABLE;

	public static boolean EVENT_VALENTINE_ENABLE;

	public static boolean EVENT_ALFABET_ENABLE;

	public static int ALFABET_MOB_MIN_LEVEL = 1;

	public static int ALFABET_MOB_MAX_LEVEL = 1;

	public static boolean ITEMS_ON_LEVEL_ENABLE;

	private ConfigEvents()
	{
		try
		{
			ConfigParser.parse(this, PATH, true);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + PATH + " config file.", e);
		}

		ConfigEventKOTH.loadConfig();
		ConfigEventCTF.loadConfig();
		ConfigEventTvT.loadConfig();

		ConfigParser.cleanCache(PATH);
	}

	@Cfg("MIN_BLOCK_CHECKER_TEAM_MEMBERS")
	private static void blockCheckerMinTeamMembers(int minTeamMembers)
	{
		MIN_BLOCK_CHECKER_TEAM_MEMBERS = minTeamMembers;
		if(MIN_BLOCK_CHECKER_TEAM_MEMBERS < 1)
		{
			MIN_BLOCK_CHECKER_TEAM_MEMBERS = 1;
		}
		else if(MIN_BLOCK_CHECKER_TEAM_MEMBERS > 6)
		{
			MIN_BLOCK_CHECKER_TEAM_MEMBERS = 6;
		}
	}

	public static void loadConfig()
	{
		new ConfigEvents();
	}
}