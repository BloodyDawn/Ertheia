package dwo.gameserver.instancemanager;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.L2Mentee;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.arrays.L2TIntObjectHashMap;
import dwo.gameserver.util.database.DatabaseUtils;
import gnu.trove.iterator.TIntObjectIterator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.Collection;

public class MentorManager
{
	private static final Logger _log = LogManager.getLogger(MentorManager.class);
	private static final SkillHolder[][] _menteeEffects = {
		{
			// Бафы ученика
			new SkillHolder(9227, 1), // Mentor's Poem of Horn
			new SkillHolder(9228, 1), // Mentor's Poem of Drum
			new SkillHolder(9229, 1), // Mentor's Poem of Lute
			new SkillHolder(9230, 1), // Mentor's Poem of Organ
			new SkillHolder(9231, 1), // Mentor's Poem of Guitar
			new SkillHolder(9232, 1), // Mentor's Poem of Harp
			new SkillHolder(9233, 1), // Mentor's Guidance
			new SkillHolder(17082, 1), // Соната Битвы Наставника
			new SkillHolder(17083, 1), // Соната Движения Наставника
			new SkillHolder(17084, 1), // Соната Расслабления Наставника
		}, {
		// Бафы наставника
		new SkillHolder(9256, 1), // Mentee's Appreciation
	}
	};
	private static final SkillHolder[][] _menteeSkills = {
		{
			// Скилл ученика
			new SkillHolder(9379, 1),
		}, {
		// Скиллы наставника
		new SkillHolder(9376, 1), new SkillHolder(9377, 1), new SkillHolder(9378, 1),
	}
	};
	private static final int[][] _menteeCoins = {
		// уровень, количество монет
		{50, 68}, {51, 16}, {52, 7}, {53, 9}, {54, 11}, {55, 13}, {56, 16}, {57, 19}, {58, 23}, {59, 29}, {60, 37},
		{61, 51}, {62, 20}, {63, 24}, {64, 30}, {65, 36}, {66, 44}, {67, 55}, {68, 67}, {69, 84}, {70, 107}, {71, 120},
		{72, 92}, {73, 114}, {74, 139}, {75, 172}, {76, 213}, {77, 629}, {78, 322}, {79, 413},
	};
	private final L2TIntObjectHashMap<L2TIntObjectHashMap<L2Mentee>> _menteeData = new L2TIntObjectHashMap<>();
	private final L2TIntObjectHashMap<L2Mentee> _mentors = new L2TIntObjectHashMap<>();

	public static MentorManager getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * @param forMentor для наставника или ученика?
	 * @return скиллы наставничества
	 */
	public static SkillHolder[] getMenteeSkills(boolean forMentor)
	{
		return _menteeSkills[forMentor ? 1 : 0];
	}

	/**
	 * Удаляет все скиллы наставничества на игроке
	 * @param player ID ученика
	 */
	public static void deleteMenteeSkills(L2PcInstance player)
	{
		if(player == null)
		{
			return;
		}

		for(SkillHolder sk : _menteeSkills[0])
		{
			player.getAllSkills().stream().filter(skills -> skills.getId() == sk.getSkillId()).forEach(player::removeSkill);
		}
	}

	/**
	 * Удаляет с указанного игрока скиллы наставника
	 * @param player инстанс ученика
	 */
	public static void deleteMentorSkills(L2PcInstance player)
	{
		if(player == null)
		{
			return;
		}

		for(SkillHolder sk : _menteeSkills[1])
		{
			player.getAllSkills().stream().filter(skill -> skill.getId() == sk.getSkillId()).forEach(player::removeSkill);
		}
	}

	/**
	 * @param forMentor для наставника или ученика?
	 * @return эффекты наставничества
	 */
	public static SkillHolder[] getMenteeEffects(boolean forMentor)
	{
		return _menteeEffects[forMentor ? 1 : 0];
	}

	/**
	 * Отменяет все эффекты наставничества на игроке
	 * @param player ID ученика
	 */
	public static void cancelMenteeBuffs(L2PcInstance player)
	{
		if(player == null)
		{
			return;
		}

		for(SkillHolder sk : _menteeEffects[0])
		{
			for(L2Effect ef : player.getAllEffects())
			{
				if(ef != null && ef.getSkill().getId() == sk.getSkillId())
				{
					player.removeEffect(ef);
				}
			}
		}
	}

	/**
	 * Снимает с указанного игрока бафы наставника
	 * @param player инстанс ученика
	 */
	public static void cancelMentorBuffs(L2PcInstance player)
	{
		if(player == null)
		{
			return;
		}

		for(SkillHolder sk : _menteeEffects[1])
		{
			for(L2Effect ef : player.getAllEffects())
			{
				if(ef != null && ef.getSkill().getId() == sk.getSkillId())
				{
					player.removeEffect(ef);
				}
			}
		}
	}

	/**
	 * Устанавливает штраф для наставника
	 * TODO: Переделать в userVar
	 * @param mentorId ID наставника
	 * @param penalty время штрафа
	 */
	public static void setPenalty(int mentorId, long penalty)
	{
		GlobalVariablesManager.getInstance().storeVariable("Mentor-Penalty-" + mentorId, String.valueOf(System.currentTimeMillis() + penalty));
	}

	/**
	 * @param mentorId ID наставника
	 * @return время штрафа для указанного наставника
	 */
	public static long getMentorPenalty(int mentorId)
	{
		String var = GlobalVariablesManager.getInstance().getStoredVariable("Mentor-Penalty-" + mentorId);
		if(var == null || !Util.isDigit(var))
		{
			return 0L;
		}
		return Long.parseLong(var);
	}

	/**
	 * @param mentorId ID наставника
	 * @param menteeId ID ученика
	 * @return все-ли ученики наставника находяться оффлайн
	 */
	public static boolean isAllMenteesOffline(int mentorId, int menteeId)
	{
		boolean isAllMenteesOffline = true;
		for(L2Mentee men : getInstance().getMentees(mentorId))
		{
			if(men.isOnline() && men.getObjectId() != menteeId)
			{
				if(isAllMenteesOffline)
				{
					isAllMenteesOffline = false;
					break;
				}
			}
		}
		return isAllMenteesOffline;
	}

	/**
	 * Загружает всех учеников для указанного наставника
	 * @param mentorId ID наставника
	 */
	public void loadMentees(int mentorId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT charId FROM character_mentees WHERE mentorId = ?");
			statement.setInt(1, mentorId);
			rset = statement.executeQuery();
			while(rset.next())
			{
				addMentor(mentorId, rset.getInt("charId"));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Загружает наставника для указанного ID игрока
	 * @param menteeId ID ученика
	 */
	public void loadMentor(int menteeId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT mentorId FROM character_mentees WHERE charId = ?");
			statement.setInt(1, menteeId);
			rset = statement.executeQuery();
			if(rset.next())
			{
				addMentor(rset.getInt("mentorId"), menteeId);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Удаляет наставника у ученика
	 * @param mentorId ID наставника
	 * @param menteeId ID ученика
	 */
	public void deleteMentor(int mentorId, int menteeId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_mentees WHERE mentorId = ? AND charId = ?");
			statement.setInt(1, mentorId);
			statement.setInt(2, menteeId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			removeMentor(mentorId, menteeId);
		}
	}

	/**
	 * @param objectId ID персонажа
	 * @return является-ли персонаж уже чьим-то наставником
	 */
	public boolean isMentor(int objectId)
	{
		return _menteeData.containsKey(objectId);
	}

	/**
	 * @param objectId ID персонажа
	 * @return является-ли персонаж уже чьим-то учеником
	 */
	public boolean isMentee(int objectId)
	{
		for(L2TIntObjectHashMap<L2Mentee> data : _menteeData.valueCollection())
		{
			if(data.containsKey(objectId))
			{
				return true;
			}
		}
		return false;
	}

	public L2TIntObjectHashMap<L2TIntObjectHashMap<L2Mentee>> getMentorData()
	{
		return _menteeData;
	}

	/**
	 * Добавляет наставника ученику
	 * @param mentorId ID наставника
	 * @param menteeId ID ученика
	 */
	public void addMentor(int mentorId, int menteeId)
	{
		if(_menteeData.containsKey(mentorId))
		{
			if(_menteeData.get(mentorId).containsKey(menteeId))
			{
				_menteeData.get(mentorId).get(menteeId).load(); // Just reloading data if is already there
			}
			else
			{
				_menteeData.get(mentorId).put(menteeId, new L2Mentee(menteeId));
			}
		}
		else
		{
			L2TIntObjectHashMap<L2Mentee> data = new L2TIntObjectHashMap<>();
			data.put(menteeId, new L2Mentee(menteeId));
			_menteeData.put(mentorId, data);
		}
	}

	/**
	 * Удаляет наставника у ученика
	 * @param mentorId ID наставника
	 * @param menteeId ID ученика
	 */
	public void removeMentor(int mentorId, int menteeId)
	{
		if(_menteeData.containsKey(mentorId))
		{
			_menteeData.get(mentorId).remove(menteeId);
			if(_menteeData.get(mentorId).size() < 1)
			{
				_menteeData.remove(mentorId);
				_mentors.remove(mentorId);
			}
		}
	}

	/**
	 * @param menteeId ID ученика
	 * @return L2Mentee-инстанс наставника, принадлежащего указанному ученику
	 */
	public L2Mentee getMentor(int menteeId)
	{
		TIntObjectIterator<L2TIntObjectHashMap<L2Mentee>> iterator = _menteeData.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			if(iterator.value().containsKey(menteeId))
			{
				if(_mentors.containsKey(iterator.key()))
				{
					return _mentors.get(iterator.key());
				}
				else
				{
					L2Mentee mentor = new L2Mentee(iterator.key());
					_mentors.put(iterator.key(), mentor);
					return mentor;
				}
			}
		}
		return null;
	}

	public Collection<L2Mentee> getMentees(int mentorId)
	{
		if(_menteeData.containsKey(mentorId))
		{
			return _menteeData.get(mentorId).valueCollection();
		}
		return null;
	}

	/**
	 * @param mentorId ID наставника
	 * @param menteeId ID ученика
	 * @return L2Mentee-инстанс ученика
	 */
	public L2Mentee getMentee(int mentorId, int menteeId)
	{
		if(_menteeData.containsKey(mentorId))
		{
			return _menteeData.get(mentorId).get(menteeId);
		}
		return null;
	}

	private static class SingletonHolder
	{
		protected static final MentorManager _instance = new MentorManager();
	}
}