package dwo.config.events;

import dwo.config.PropertyListenerImpl;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.util.StringUtil;
import gnu.trove.map.hash.TIntIntHashMap;
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
public class ConfigEventTvT extends PropertyListenerImpl
{
	public static boolean TVT_EVENT_ENABLED;

	public static boolean TVT_EVENT_IN_INSTANCE;

	public static String TVT_EVENT_INSTANCE_FILE = "Event_TVT.xml";

	@CfgSplit(splitter = ",")
	public static String[] TVT_EVENT_INTERVAL = {"20:00"};

	public static int TVT_EVENT_PARTICIPATION_TIME = 3600;

	public static int TVT_EVENT_RUNNING_TIME = 1800;

	public static int TVT_EVENT_PARTICIPATION_NPC_ID;

	@CfgSplit(splitter = ",")
	public static int[] TVT_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];

	@CfgSplit(splitter = ",")
	public static int[] TVT_EVENT_PARTICIPATION_FEE = new int[2];

	public static int TVT_EVENT_MIN_PLAYERS_IN_TEAMS = 1;

	public static int TVT_EVENT_MAX_PLAYERS_IN_TEAMS = 20;

	public static int TVT_EVENT_RESPAWN_TELEPORT_DELAY = 20;

	public static int TVT_EVENT_START_LEAVE_TELEPORT_DELAY = 20;

	public static String TVT_EVENT_TEAM_1_NAME = "Team1";

	@CfgSplit(splitter = ",")
	public static int[] TVT_EVENT_TEAM_1_COORDINATES = new int[3];

	public static String TVT_EVENT_TEAM_2_NAME = "Team2";

	@CfgSplit(splitter = ",")
	public static int[] TVT_EVENT_TEAM_2_COORDINATES = new int[3];

	@CfgIgnore
	public static List<int[]> TVT_EVENT_REWARDS = new FastList<>();

	public static boolean TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = true;

	@CfgSplit
	public static List<Integer> TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED_EXCEPTIONS = new FastList<>();

	public static boolean TVT_EVENT_SCROLL_ALLOWED;

	public static boolean TVT_EVENT_POTIONS_ALLOWED;

	public static boolean TVT_EVENT_SUMMON_BY_ITEM_ALLOWED;

	@CfgSplit
	public static List<Integer> TVT_DOORS_IDS_TO_OPEN = new FastList<>();

	@CfgSplit
	public static List<Integer> TVT_DOORS_IDS_TO_CLOSE = new FastList<>();

	public static boolean TVT_REWARD_TEAM_TIE;

	public static byte TVT_EVENT_MIN_LVL = 1;

	public static byte TVT_EVENT_MAX_LVL = 80;

	public static int TVT_EVENT_EFFECTS_REMOVAL;

	@CfgIgnore
	public static TIntIntHashMap TVT_EVENT_FIGHTER_BUFFS;

	@CfgIgnore
	public static TIntIntHashMap TVT_EVENT_MAGE_BUFFS;

	public static boolean TVT_ALLOW_VOICED_COMMAND;

	public static boolean TVT_FIRST_BLOOD_MODE;

	public static int TVT_FIRST_BLOOD_REWARD_ID = PcInventory.ADENA_ID;

	public static int TVT_FIRST_BLOOD_REWARD_COUNT = 1000;

	public static int TVT_KILLING_SPREE_REWARD_ID = PcInventory.ADENA_ID;

	public static int TVT_KILLING_SPREE_REWARD_COUNT = 10000;

	public static boolean TVT_KILLING_SPREE_MODE;

	public static boolean TVT_UNSTOPPABLE_MODE;

	public static int TVT_UNSTOPPABLE_REWARD_ID = PcInventory.ADENA_ID;

	public static int TVT_UNSTOPPABLE_REWARD_COUNT = 100000;

	public static boolean TVT_GOD_LIKE_MODE;

	public static int TVT_GOD_LIKE_REWARD_ID = PcInventory.ADENA_ID;

	public static int TVT_GOD_LIKE_REWARD_COUNT = 1000000;

	public static boolean TVT_ENABLE_MIN_KILLS_TO_REWARD = true;

	@Cfg
	public static int TVT_MIN_KILLS_TO_REWARD = 5;

	public static int TVT_EVENT_MAX_PARTICIPANTS_PER_IP;

	public ConfigEventTvT()
	{
		try
		{
			ConfigParser.parse(this, ConfigEvents.PATH, true);

			if(TVT_EVENT_ENABLED)
			{
				boolean enabled = false;
				if(TVT_EVENT_PARTICIPATION_NPC_ID == 0)
				{
					_log.log(Level.WARN, "TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationNpcId");
				}
				else if(TVT_EVENT_PARTICIPATION_NPC_COORDINATES.length < 3)
				{
					_log.log(Level.WARN, "TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationNpcCoordinates");
				}
				else if(TVT_EVENT_TEAM_1_COORDINATES.length < 3)
				{
					_log.log(Level.WARN, "TvTEventEngine[Config.load()]: invalid config property -> TvTEventTeam1Coordinates");
				}
				else if(TVT_EVENT_TEAM_2_COORDINATES.length < 3)
				{
					_log.log(Level.WARN, "TvTEventEngine[Config.load()]: invalid config property -> TvTEventTeam2Coordinates");
				}
				else
				{
					enabled = true;
				}

				TVT_EVENT_ENABLED = enabled;
			}
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + ConfigEvents.PATH + " File.", e);
		}
	}

	@Cfg("TVT_EVENT_FIGHTER_BUFFS")
	private static void tvtEventFighterBuffs(String value)
	{
		String[] buffs = value.split(";");
		if(!buffs[0].isEmpty())
		{
			TVT_EVENT_FIGHTER_BUFFS = new TIntIntHashMap(buffs.length);
			for(String skill : buffs)
			{
				String[] skillSplit = skill.split(",");
				if(skillSplit.length == 2)
				{
					try
					{
						TVT_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
					}
					catch(NumberFormatException nfe)
					{
						if(!skill.isEmpty())
						{
							_log.log(Level.WARN, StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventFighterBuffs \"", skill, "\""));
						}
					}
				}
				else
				{
					_log.log(Level.WARN, StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventFighterBuffs \"", skill, "\""));
				}
			}
		}
	}

	@Cfg("TVT_EVENT_MAGE_BUFFS")
	private static void tvtEventMageBuffer(String value)
	{
		String[] buffs = value.split(";");
		if(!buffs[0].isEmpty())
		{
			TVT_EVENT_MAGE_BUFFS = new TIntIntHashMap(buffs.length);
			for(String skill : buffs)
			{
				String[] skillSplit = skill.split(",");
				if(skillSplit.length == 2)
				{
					try
					{
						TVT_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
					}
					catch(NumberFormatException nfe)
					{
						if(!skill.isEmpty())
						{
							_log.log(Level.WARN, StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventMageBuffs \"", skill, "\""));
						}
					}
				}
				else
				{
					_log.log(Level.WARN, StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventMageBuffs \"", skill, "\""));
				}
			}
		}
	}

	@Cfg("TVT_EVENT_REWARDS")
	private static void tvtEventReward(String value)
	{
		if(value.isEmpty())
		{
			value = "57,100000";
		}

		String[] rewards = value.split(";");
		for(String reward : rewards)
		{
			String[] rewardSplit = reward.split(",");
			if(rewardSplit.length == 2)
			{
				try
				{
					TVT_EVENT_REWARDS.add(new int[]{
						Integer.parseInt(rewardSplit[0]), Integer.parseInt(rewardSplit[1])
					});
				}
				catch(NumberFormatException nfe)
				{
					if(!reward.isEmpty())
					{
						_log.log(Level.WARN, StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventReward \"", reward, "\""));
					}
				}
			}
			else
			{
				_log.log(Level.WARN, StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventReward \"", reward, "\""));
			}
		}
	}

	public static void loadConfig()
	{
		new ConfigEventTvT();
	}

	@Override
	public void onInvalidPropertyCast(String name, String value)
	{
		// Doors IDs can't be empty
		if(!name.equals("TVT_DOORS_IDS_TO_OPEN") && !name.equals("TVT_DOORS_IDS_TO_CLOSE"))
		{
			super.onInvalidPropertyCast(name, value);
		}
	}
}
