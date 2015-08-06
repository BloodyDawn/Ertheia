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
public class ConfigEventCTF extends PropertyListenerImpl
{
	public static boolean CTF_EVENT_ENABLED;
	public static boolean CTF_EVENT_IN_INSTANCE;
	public static String CTF_EVENT_INSTANCE_FILE = "Event_TvT.xml";
	@CfgSplit(splitter = ",")
	public static String[] CTF_EVENT_INTERVAL = {"20:00"};
	public static int CTF_EVENT_PARTICIPATION_TIME = 3600;
	public static int CTF_EVENT_RUNNING_TIME = 1800;
	public static int CTF_EVENT_PARTICIPATION_NPC_ID;
	@CfgSplit(splitter = ",")
	public static int[] CTF_EVENT_PARTICIPATION_NPC_COORDINATES = new int[3];
	@CfgSplit(splitter = ",")
	public static int[] CTF_EVENT_PARTICIPATION_FEE = new int[2];
	public static int CTF_EVENT_MIN_PLAYERS_IN_TEAMS = 1;
	public static int CTF_EVENT_MAX_PLAYERS_IN_TEAMS = 20;
	public static int CTF_EVENT_RESPAWN_DELAY = 20;
	public static int CTF_EVENT_START_LEAVE_TELEPORT_DELAY = 20;
	public static String CTF_EVENT_TEAM_1_NAME = "Team1";
	@CfgSplit(splitter = ",")
	public static int[] CTF_EVENT_TEAM_1_COORDINATES = new int[3];
	@CfgSplit(splitter = ",")
	public static int[] CTF_EVENT_TEAM_1_FLAG_COORDINATES = new int[3];
	public static int CTF_EVENT_TEAM_1_FLAG_NPC_ID;
	public static String CTF_EVENT_TEAM_2_NAME = "Team2";
	@CfgSplit(splitter = ",")
	public static int[] CTF_EVENT_TEAM_2_COORDINATES = new int[3];
	@CfgSplit(splitter = ",")
	public static int[] CTF_EVENT_TEAM_2_FLAG_COORDINATES = new int[3];
	public static int CTF_EVENT_TEAM_2_FLAG_NPC_ID;
	@CfgIgnore
	public static List<int[]> CTF_EVENT_REWARDS = new FastList<>();
	public static boolean CTF_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = true;
	public static boolean CTF_EVENT_SCROLL_ALLOWED;
	public static boolean CTF_EVENT_POTIONS_ALLOWED;
	public static boolean CTF_EVENT_SUMMON_BY_ITEM_ALLOWED;
	public static boolean CTF_EVENT_DESPAWN_SUMMON_ON_TELEPORT = true;
	public static boolean CTF_REWARD_TEAM_TIE;
	public static byte CTF_EVENT_MIN_LVL = 1;
	public static byte CTF_EVENT_MAX_LVL = 80;
	public static int CTF_EVENT_EFFECTS_REMOVAL;
	@CfgIgnore
	public static TIntIntHashMap CTF_EVENT_FIGHTER_BUFFS;
	@CfgIgnore
	public static TIntIntHashMap CTF_EVENT_MAGE_BUFFS;
	public static CTFEventRespawnType CTF_EVENT_RESPAWN_TYPE = CTFEventRespawnType.MASS;
	public static int CTF_EVENT_FLAG_ITEM_ID;
	@CfgIgnore
	public static int CTF_EVENT_FLAG_EXPIRE_TIME;
	public static int CTF_EVENT_IDLE_TIME_CHECKER;
	public static int CTF_EVENT_MAX_DUALBOXES;

	private ConfigEventCTF()
	{
		try
		{
			ConfigParser.parse(this, ConfigEvents.PATH, true);
			if(CTF_EVENT_ENABLED)
			{
				boolean enabled = false;
				if(CTF_EVENT_PARTICIPATION_NPC_ID == 0)
				{
					_log.log(Level.WARN, "CTFEventEngine[Config.load()]: invalid config property -> CTFEventParticipationNpcId");
				}
				else if(CTF_EVENT_TEAM_1_FLAG_NPC_ID == 0 || CTF_EVENT_TEAM_2_FLAG_NPC_ID == 0)
				{
					_log.log(Level.WARN, "CTFEventEngine[Config.load()]: invalid config property -> CTFEventTeamFlagId, flag id should not be 0");
				}
				else if(CTF_EVENT_FLAG_ITEM_ID == 0)
				{
					_log.log(Level.WARN, "CTFEventEngine[Config.load()]: invalid config property -> CTFEventFlagItemId, flag id should not be 0");
				}
				else if(CTF_EVENT_PARTICIPATION_NPC_COORDINATES.length < 3)
				{
					_log.log(Level.WARN, "CTFEventEngine[Config.load()]: invalid config property -> CTFEventParticipationNpcCoordinates");
				}
				else if(CTF_EVENT_TEAM_1_COORDINATES.length < 3 || CTF_EVENT_TEAM_1_FLAG_COORDINATES.length < 3)
				{
					_log.log(Level.WARN, "CTFEventEngine[Config.load()]: invalid config property -> CTFEventTeam1Coordinates");
				}
				else if(CTF_EVENT_TEAM_2_COORDINATES.length < 3 || CTF_EVENT_TEAM_2_FLAG_COORDINATES.length < 3)
				{
					_log.log(Level.WARN, "CTFEventEngine[Config.load()]: invalid config property -> CTFEventTeam2Coordinates");
				}
				else
				{
					enabled = true;
				}

				CTF_EVENT_ENABLED = enabled;
			}
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + ConfigEvents.PATH + " File.", e);
		}
	}

	@Cfg("CTF_EVENT_MAGE_BUFFS")
	private static void ctfEventMageBuffer(String value)
	{
		String[] buffs = value.split(";");
		if(!buffs[0].isEmpty())
		{
			CTF_EVENT_MAGE_BUFFS = new TIntIntHashMap(buffs.length);
			for(String skill : buffs)
			{
				String[] skillSplit = skill.split(",");
				if(skillSplit.length == 2)
				{
					try
					{
						CTF_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
					}
					catch(NumberFormatException nfe)
					{
						if(!skill.isEmpty())
						{
							_log.log(Level.WARN, StringUtil.concat("CTFEventEngine[Config.load()]: invalid config property -> CTFEventMageBuffs \"", skill, "\""));
						}
					}
				}
				else
				{
					_log.log(Level.WARN, StringUtil.concat("CTFEventEngine[Config.load()]: invalid config property -> CTFEventMageBuffs \"", skill, "\""));
				}
			}
		}
	}

	@Cfg("CTF_EVENT_FIGHTER_BUFFS")
	private static void ctfEventFighterBuffs(String value)
	{
		String[] buffs = value.split(";");
		if(!buffs[0].isEmpty())
		{
			CTF_EVENT_FIGHTER_BUFFS = new TIntIntHashMap(buffs.length);
			for(String skill : buffs)
			{
				String[] skillSplit = skill.split(",");
				if(skillSplit.length == 2)
				{
					try
					{
						CTF_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
					}
					catch(NumberFormatException nfe)
					{
						if(!skill.isEmpty())
						{
							_log.log(Level.WARN, StringUtil.concat("CTFEventEngine[Config.load()]: invalid config property -> CTFEventFighterBuffs \"", skill, "\""));
						}
					}
				}
				else
				{
					_log.log(Level.WARN, StringUtil.concat("CTFEventEngine[Config.load()]: invalid config property -> CTFEventFighterBuffs \"", skill, "\""));
				}
			}
		}
	}

	@Cfg("CTF_EVENT_REWARDS")
	private static void ctfEventReward(String value)
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
					CTF_EVENT_REWARDS.add(new int[]{
						Integer.parseInt(rewardSplit[0]), Integer.parseInt(rewardSplit[1])
					});
				}
				catch(NumberFormatException nfe)
				{
					if(!reward.isEmpty())
					{
						_log.log(Level.WARN, StringUtil.concat("CTFEventEngine[Config.load()]: invalid config property -> CTFEventReward \"", reward, "\""));
					}
				}
			}
			else
			{
				_log.log(Level.WARN, StringUtil.concat("CTFEventEngine[Config.load()]: invalid config property -> CTFEventReward \"", reward, "\""));
			}
		}
	}

	@Cfg("CTF_EVENT_FLAG_EXPIRE_TIME")
	private static void ctfEventFlagExpireTime(int time)
	{
		if(time > 0)
		{
			CTF_EVENT_FLAG_EXPIRE_TIME = time * 60 * 1000;
		}
	}

	public static void loadConfig()
	{
		new ConfigEventCTF();
	}

	public static enum CTFEventRespawnType
	{
		MASS,
		CLASSIC,
		MANUAL
	}
}
