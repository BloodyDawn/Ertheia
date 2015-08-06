package dwo.gameserver.model.world.quest.dynamicquest;

import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.holders.SpawnsHolder;
import dwo.gameserver.model.world.zone.L2ZoneType;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 23.05.12
 * Time: 8:49
 */

public class DynamicQuestTemplate
{
	private final int _questId;
	private final int _taskId;
	private final String _questName;
	private final String _questTitle;
	private final int _duration;
	private final int _points;
	private final int _nextTaskId;
	private final boolean _autostart;
	private final SpawnsHolder _spawns;
	private final List<L2ZoneType> _zones;
	private final Map<String, String> _dialogs;
	private final Map<Integer, Integer> _killPoints;
	private final List<ItemHolder> _rewards;
	private final List<ItemHolder> _eliteRewards;
	private final int _minLevel;
	private final List<DynamicQuestDate> _startDates;

	/**
	 * Для зоновых квестов.
	 *
	 * @param questId ID квеста.
	 * @param questName Название квеста.
	 * @param duration Продолжительность квеста в минутах.
	 * @param minLevel Минимальный уровень участника.
	 * @param spawns Список спаунов для квеста.
	 */
	public DynamicQuestTemplate(int questId, int taskId, String questName, String questTitle, int duration, int minLevel, int points, int nextTaskId, boolean autostart, SpawnsHolder spawns, List<L2ZoneType> zones, List<DynamicQuestDate> dates, Map<Integer, Integer> killPoints, List<ItemHolder> rewads, List<ItemHolder> eliteRewads, Map<String, String> dialogs)
	{
		_questId = questId;
		_taskId = taskId;
		_questName = questName;
		_questTitle = questTitle;
		_duration = duration;
		_points = points;
		_minLevel = minLevel;
		_nextTaskId = nextTaskId;
		_autostart = autostart;
		_spawns = spawns;
		_zones = zones;
		_killPoints = killPoints;
		_rewards = rewads;
		_eliteRewards = eliteRewads;
		_dialogs = dialogs;
		_startDates = dates;
	}

	/**
	 * @return id квеста компании
	 */
	public int getQuestId()
	{
		return _questId;
	}

	public int getTaskId()
	{
		return _taskId;
	}

	/**
	 * @return имя квеста компании
	 */
	public String getQuestName()
	{
		return _questName;
	}

	public String getQuestTitle()
	{
		return _questTitle;
	}

	public int getDuration()
	{
		return _duration;
	}

	public int getMinLevel()
	{
		return _minLevel;
	}

	public int getPoints()
	{
		return _points;
	}

	public int getNextTaskId()
	{
		return _nextTaskId;
	}

	public boolean isAutostart()
	{
		return _autostart;
	}

	/**
	 * @return Список зон для зоновых квестов.
	 */
	public List<L2ZoneType> getAllZones()
	{
		return _zones;
	}

	public List<ItemHolder> getAllRewards()
	{
		return _rewards;
	}

	public List<ItemHolder> getAllEliteRewards()
	{
		return _eliteRewards;
	}

	public Map<Integer, Integer> getAllPoints()
	{
		return _killPoints;
	}

	public String getDialog(String type)
	{
		return _dialogs.get(type);
	}

	public int getKillPoint(int npcId)
	{
		return _killPoints.containsKey(npcId) ? _killPoints.get(npcId) : 0;
	}

	/**
	 * @return Список дат начала кампаний.
	 */
	public List<DynamicQuestDate> getStartDates()
	{
		return _startDates;
	}

	/**
	 * @return SpawnsHolder для текущего квеста компании
	 */
	public SpawnsHolder getSpawnHolder()
	{
		return _spawns;
	}

	/**
	 *
	 * @return True, если данный квест является зоновым квестом.
	 */
	public boolean isZoneQuest()
	{
		return _zones != null && _startDates == null;
	}

	/**
	 *
	 * @return True, если данный квест является кампанией.
	 */
	public boolean isCampain()
	{
		return _zones == null && _startDates != null;
	}

	public static class DynamicQuestDate
	{
		private final String _dayOfWeek;
		private final int _hour;
		private final int _minute;

		public DynamicQuestDate(String dayOfWeek, int hours, int minutes)
		{
			_dayOfWeek = dayOfWeek.toLowerCase();
			_hour = hours;
			_minute = minutes;
		}

		public String getDayOfWeek()
		{
			return _dayOfWeek;
		}

		public int getHour()
		{
			return _hour;
		}

		public int getMinutes()
		{
			return _minute;
		}

		public long getNextScheduleTime()
		{
			int dayOfWeekNumber;
			switch(_dayOfWeek)
			{
				case "monday":
				case "mon":
					dayOfWeekNumber = Calendar.MONDAY;
					break;
				case "tuesday":
				case "tue":
					dayOfWeekNumber = Calendar.TUESDAY;
					break;
				case "wednesday":
				case "wed":
					dayOfWeekNumber = Calendar.WEDNESDAY;
					break;
				case "thursday":
				case "thu":
					dayOfWeekNumber = Calendar.THURSDAY;
					break;
				case "friday":
				case "fri":
					dayOfWeekNumber = Calendar.FRIDAY;
					break;
				case "saturday":
				case "sat":
					dayOfWeekNumber = Calendar.SATURDAY;
					break;
				case "sunday":
				case "sun":
					dayOfWeekNumber = Calendar.SUNDAY;
					break;
				default:
					return 0;
			}

			Calendar current = Calendar.getInstance();
			current.set(Calendar.HOUR, _hour);
			current.set(Calendar.MINUTE, _minute);

			int dayDiff = dayOfWeekNumber - current.get(Calendar.DAY_OF_WEEK);

			if(dayDiff > 0)
			{
				current.add(Calendar.DAY_OF_MONTH, dayDiff);
			}
			else if(dayDiff < 0)
			{
				current.add(Calendar.DAY_OF_MONTH, 7 - Math.abs(dayDiff));
			}

			return current.getTimeInMillis();
		}
	}
}