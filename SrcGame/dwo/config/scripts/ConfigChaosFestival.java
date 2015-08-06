package dwo.config.scripts;

import dwo.config.PropertyListenerImpl;
import javolution.util.FastList;
import jfork.nproperty.Cfg;
import jfork.nproperty.CfgIgnore;
import jfork.nproperty.CfgSplit;
import jfork.nproperty.ConfigParser;
import org.apache.log4j.Level;

import java.util.List;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.xx
 * Time: xx:xx
 */

@Cfg
public class ConfigChaosFestival extends PropertyListenerImpl
{
	public static boolean CHAOS_FESTIVAL_ENABLED;
	public static int CHAOS_FESTIVAL_START_HOUR;
	public static int CHAOS_FESTIVAL_END_HOUR;
	public static boolean CHAOS_FESTIVAL_BALANCE;
	public static int CHAOS_FESTIVAL_TOTAL_BANS_LIMIT;
	public static int CHAOS_FESTIVAL_MAX_PLAYERS_PER_MATCH;
	public static int CHAOS_FESTIVAL_PREPARATION_TIME;
	public static int CHAOS_FESTIVAL_FIRST_HERB_BOX_COUNT;
	public static int CHAOS_FESTIVAL_SECOND_HERB_BOX_COUNT;
	public static int CHAOS_FESTIVAL_THIRD_HERB_BOX_COUNT;
	public static boolean CHAOS_FESTIVAL_HIDE_PLAYER_NAMES;
	public static boolean CHAOS_FESTIVAL_HIDE_DISTINCTIONS;
	@CfgSplit(splitter = ",")
	public static List<Integer> CHAOS_FESTIVAL_WINNER_REWARD_BUFFS = new FastList<>();
	@CfgSplit(splitter = ",")
	public static List<Integer> CHAOS_FESTIVAL_LOSER_REWARD_BUFFS = new FastList<>();
	@CfgSplit(splitter = ",")
	public static List<Integer> CHAOS_FESTIVAL_LAST_SURVIVOR_REWARD_BUFFS = new FastList<>();
	@CfgSplit(splitter = ",")
	public static List<Integer> CHAOS_FESTIVAL_BEST_KILLER_REWARD_ITEMS = new FastList<>();
	public static int CHAOS_FESTIVAL_MYST_BOX_MIN_COUNT;
	public static int CHAOS_FESTIVAL_MYST_BOX_MAX_COUNT;
	public static int CHAOS_FESTIVAL_MYST_BOX_SIGNS_MIN_COUNT;
	public static int CHAOS_FESTIVAL_MYST_BOX_SIGNS_MAX_COUNT;
	@CfgIgnore
	public static List<int[]> CHAOS_FESTIVAL_MYST_BOX_RANDOM_REWARDS = new FastList<>();
	public static int CHAOS_FESTIVAL_MONSTER_ID;
	@CfgSplit(splitter = "-")
	public static int[] CHAOS_FESTIVAL_MONSTER_COUNT = {5, 8};
	public static int CHAOS_FESTIVAL_MONSTER_APPEAR_CHANCE = 30;

	private ConfigChaosFestival()
	{
		String path = "./config/scripts/ChaosFestival.ini";
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigParser.parse(this, path);

			if(CHAOS_FESTIVAL_WINNER_REWARD_BUFFS.isEmpty())
			{
				CHAOS_FESTIVAL_WINNER_REWARD_BUFFS.add(9540);
				CHAOS_FESTIVAL_WINNER_REWARD_BUFFS.add(9541);
				CHAOS_FESTIVAL_WINNER_REWARD_BUFFS.add(9542);
			}

			if(CHAOS_FESTIVAL_LOSER_REWARD_BUFFS.isEmpty())
			{
				CHAOS_FESTIVAL_LOSER_REWARD_BUFFS.add(9540);
				CHAOS_FESTIVAL_LOSER_REWARD_BUFFS.add(9541);
			}

			if(CHAOS_FESTIVAL_LAST_SURVIVOR_REWARD_BUFFS.isEmpty())
			{
				CHAOS_FESTIVAL_LAST_SURVIVOR_REWARD_BUFFS.add(9540);
				CHAOS_FESTIVAL_LAST_SURVIVOR_REWARD_BUFFS.add(9541);
				CHAOS_FESTIVAL_LAST_SURVIVOR_REWARD_BUFFS.add(9542);
			}

			if(CHAOS_FESTIVAL_LAST_SURVIVOR_REWARD_BUFFS.isEmpty())
			{
				CHAOS_FESTIVAL_BEST_KILLER_REWARD_ITEMS.add(35982);
			}
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}

	public static void loadConfig()
	{
		new ConfigChaosFestival();
	}

	@Cfg("CHAOS_FESTIVAL_MYST_BOX_RANDOM_REWARDS")
	private void mystBoxRandomRewards(String value)
	{
		if(value == null)
		{
			_log.warn("Chaos Festival myst box random rewards configuration is invalid.");
			return;
		}

		String[] rewards = value.split(";");
		for(String rewardInfo : rewards)
		{
			String[] info = rewardInfo.split(",");
			if(info.length < 2)
			{
				_log.warn("Chaos Festival myst box random rewards configuration is invalid.");
				break;
			}

			int itemId;
			int chance;
			try
			{
				itemId = Integer.parseInt(info[0]);
				chance = Integer.parseInt(info[1]);
			}
			catch(NumberFormatException e)
			{
				_log.warn("Chaos Festival myst box random rewards configuration is invalid.");
				break;
			}
			CHAOS_FESTIVAL_MYST_BOX_RANDOM_REWARDS.add(new int[]{itemId, chance});
		}
	}
}
