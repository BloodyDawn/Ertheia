package dwo.config.events;

import dwo.config.PropertyListenerImpl;
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
public class ConfigEventKOTH extends PropertyListenerImpl
{
	public static boolean KOTH_EVENT_ENABLED;
	public static boolean KOTH_EVENT_IN_INSTANCE;
	public static String KOTH_EVENT_INSTANCE_FILE = "Event_TvT.xml";
	@CfgSplit(splitter = ",")
	public static String[] KOTH_EVENT_INTERVAL = {"20:00"};
	public static int KOTH_EVENT_PARTICIPATION_TIME = 3600;
	public static int KOTH_EVENT_RUNNING_TIME = 1800;
	public static int KOTH_EVENT_PARTICIPATION_NPC_ID;
	@CfgSplit(splitter = ",")
	public static int[] KOTH_EVENT_PARTICIPATION_NPC_COORDINATES = new int[3];
	@CfgSplit(splitter = ",")
	public static int[] KOTH_EVENT_PARTICIPATION_FEE = new int[2];
	public static int KOTH_EVENT_MIN_PLAYERS_IN_TEAMS = 1;
	public static int KOTH_EVENT_MAX_PLAYERS_IN_TEAMS = 20;
	public static int KOTH_EVENT_RESPAWN_DELAY = 20;
	public static int KOTH_EVENT_START_LEAVE_TELEPORT_DELAY = 20;
	public static int KOTH_EVENT_STATUS_DELAY = 60;
	public static String KOTH_EVENT_TEAM_1_NAME = "Team1";
	@CfgSplit(splitter = ",")
	public static int[] KOTH_EVENT_TEAM_1_COORDINATES = new int[3];
	public static String KOTH_EVENT_TEAM_2_NAME = "Team2";
	@CfgSplit(splitter = ",")
	public static int[] KOTH_EVENT_TEAM_2_COORDINATES = new int[3];
	@CfgIgnore
	public static List<int[]> KOTH_EVENT_REWARDS = new FastList<>();
	public static boolean KOTH_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = true;
	public static boolean KOTH_EVENT_SCROLL_ALLOWED;
	public static boolean KOTH_EVENT_POTIONS_ALLOWED;
	public static boolean KOTH_EVENT_SUMMON_BY_ITEM_ALLOWED;
	public static boolean KOTH_EVENT_DESPAWN_SUMMON_ON_TELEPORT = true;
	public static boolean KOTH_REWARD_TEAM_TIE;
	public static byte KOTH_EVENT_MIN_LVL = 1;
	public static byte KOTH_EVENT_MAX_LVL = 80;
	public static int KOTH_EVENT_EFFECTS_REMOVAL;
	@CfgIgnore
	public static TIntIntHashMap KOTH_EVENT_FIGHTER_BUFFS;
	@CfgIgnore
	public static TIntIntHashMap KOTH_EVENT_MAGE_BUFFS;
	@CfgSplit(splitter = ",")
	public static int[] KOTH_EVENT_POINTS = {100, 1000, 0};
	public static KOTHEventRespawnType KOTH_EVENT_RESPAWN_TYPE = KOTHEventRespawnType.MASS;

	public ConfigEventKOTH()
	{
		try
		{
			ConfigParser.parse(this, ConfigEvents.PATH, true);

			if(KOTH_EVENT_ENABLED)
			{
				boolean enabled = false;
				if(KOTH_EVENT_PARTICIPATION_NPC_ID == 0)
				{
					_log.log(Level.WARN, "KOTHEventEngine[Config.load()]: invalid config property -> KOTHEventParticipationNpcId");
				}
				else if(KOTH_EVENT_PARTICIPATION_NPC_COORDINATES.length < 3)
				{
					_log.log(Level.WARN, "KOTHEventEngine[Config.load()]: invalid config property -> KOTHEventParticipationNpcCoordinates");
				}
				else if(KOTH_EVENT_TEAM_1_COORDINATES.length < 3)
				{
					_log.log(Level.WARN, "KOTHEventEngine[Config.load()]: invalid config property -> KOTHEventTeam1Coordinates");
				}
				else if(KOTH_EVENT_TEAM_2_COORDINATES.length < 3)
				{
					_log.log(Level.WARN, "KOTHEventEngine[Config.load()]: invalid config property -> KOTHEventTeam2Coordinates");
				}
				else if(KOTH_EVENT_POINTS[1] < 250 || KOTH_EVENT_POINTS[1] > 60000)
				{
					_log.log(Level.WARN, "KOTHEventEngine[Config.load()]: invalid config property -> KOTHEventPoints");
				}
				else
				{
					enabled = true;
				}
				KOTH_EVENT_ENABLED = enabled;
			}
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + ConfigEvents.PATH + " File.", e);
		}
	}

	@Cfg("KOTH_EVENT_MAGE_BUFFS")
	private static void kothEventMageBuffs(String value)
	{
		String[] buffs = value.split(";");
		if(!buffs[0].isEmpty())
		{
			KOTH_EVENT_MAGE_BUFFS = new TIntIntHashMap(buffs.length);
			for(String skill : buffs)
			{
				String[] skillSplit = skill.split(",");
				if(skillSplit.length == 2)
				{
					try
					{
						KOTH_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
					}
					catch(NumberFormatException nfe)
					{
						if(!skill.isEmpty())
						{
							_log.log(Level.WARN, StringUtil.concat("KOTHEventEngine[Config.load()]: invalid config property -> KOTHEventMageBuffs \"", skill, "\""));
						}
					}
				}
				else
				{
					_log.log(Level.WARN, StringUtil.concat("KOTHEventEngine[Config.load()]: invalid config property -> KOTHEventMageBuffs \"", skill, "\""));
				}
			}
		}
	}

	@Cfg("KOTH_EVENT_FIGHTER_BUFFS")
	private static void kothEventFighterBuffs(String value)
	{
		String[] buffs = value.split(";");
		if(!buffs[0].isEmpty())
		{
			KOTH_EVENT_FIGHTER_BUFFS = new TIntIntHashMap(buffs.length);
			for(String skill : buffs)
			{
				String[] skillSplit = skill.split(",");
				if(skillSplit.length == 2)
				{
					try
					{
						KOTH_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
					}
					catch(NumberFormatException nfe)
					{
						if(!skill.isEmpty())
						{
							_log.log(Level.WARN, StringUtil.concat("KOTHEventEngine[Config.load()]: invalid config property -> KOTHEventFighterBuffs \"", skill, "\""));
						}
					}
				}
				else
				{
					_log.log(Level.WARN, StringUtil.concat("KOTHEventEngine[Config.load()]: invalid config property -> KOTHEventFighterBuffs \"", skill, "\""));
				}
			}
		}
	}

	@Cfg("KOTH_EVENT_REWARDS")
	private static void kothEventReward(String value)
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
					KOTH_EVENT_REWARDS.add(new int[]{
						Integer.parseInt(rewardSplit[0]), Integer.parseInt(rewardSplit[1])
					});
				}
				catch(NumberFormatException nfe)
				{
					if(!reward.isEmpty())
					{
						_log.log(Level.WARN, StringUtil.concat("KOTHEventEngine[Config.load()]: invalid config property -> KOTHEventReward \"", reward, "\""));
					}
				}
			}
			else
			{
				_log.log(Level.WARN, StringUtil.concat("KOTHEventEngine[Config.load()]: invalid config property -> KOTHEventReward \"", reward, "\""));
			}
		}
	}

	public static void loadConfig()
	{
		new ConfigEventKOTH();
	}

	public static enum KOTHEventRespawnType
	{
		MASS,
		CLASSIC,
		MANUAL
	}
}
