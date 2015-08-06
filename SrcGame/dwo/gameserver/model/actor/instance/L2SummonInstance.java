/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.actor.instance;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.CharSummonTable;
import dwo.gameserver.datatables.xml.SummonPointsTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSummon;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.world.npc.SummonLifetime;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

public class L2SummonInstance extends L2Summon
{
	public static final String RESTORE_SKILL_SAVE = "SELECT summon_object_id, skill_id, skill_level, effect_count, effect_cur_time, buff_index FROM character_summon_skills_save WHERE owner_id=? AND owner_class_index=? AND summon_object_id=? ORDER BY buff_index ASC";
	public static final String DELETE_SKILL_SAVE = "DELETE FROM character_summon_skills_save WHERE owner_id=? AND owner_class_index=? AND summon_object_id=?";
	protected static final Logger log = LogManager.getLogger(L2SummonInstance.class);
	private static final String ADD_SKILL_SAVE = "INSERT INTO character_summon_skills_save (owner_id, owner_class_index, summon_object_id, skill_id, skill_level, effect_count, effect_cur_time, buff_index) VALUES (?,?,?,?,?,?,?,?)";
	private final int _totalLifeTime;
	private final int _timeLostIdle;
	private final int _timeLostActive;
	public int lastShowntimeRemaining; // Following FbiAgent's example to avoid sending useless packets
	private float _expPenalty; // exp decrease multiplier (i.e. 0.3 (= 30%) for shadow)
	private int _timeRemaining;
	private boolean _isOnline;
	private String _summonGroupReplace;
	private Future<?> _summonLifeTask;
	private int _referenceSkill;
	private boolean _shareElementals;
	private double _sharedElementalsPercent = 1;
	private ScheduledFuture<?> _skillCastingTask;
	private L2Skill _skill;
	private boolean _isMovementDisable;

	public L2SummonInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill, int restoreObjectId)
	{
		super(objectId, template, owner);
		setRestoredObjectId(restoreObjectId);
		setShowSummonAnimation(true);

		if(skill != null)
		{
			L2SkillSummon summonSkill = (L2SkillSummon) skill;
			_totalLifeTime = summonSkill.getTotalLifeTime();
			_timeLostIdle = summonSkill.getTimeLostIdle();
			_timeLostActive = summonSkill.getTimeLostActive();
			_referenceSkill = summonSkill.getId();
			_isMovementDisable = summonSkill.isMovementDisable();
		}
		else
		{
			// defaults
			_totalLifeTime = 1200000; // 20 minutes
			_timeLostIdle = 1000;
			_timeLostActive = 1000;
		}
		_timeRemaining = _totalLifeTime;
		lastShowntimeRemaining = _totalLifeTime;

		// When no item consume is defined task only need to check when summon life time has ended.
		// Otherwise have to destroy items from owner's inventory in order to let summon live.
		int delay = 1000;

		_summonLifeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SummonLifetime(getOwner(), this), delay, delay);

		if(!getOwner().getOlympiadController().isParticipating())
		{
			restoreEffects();
		}

		_isOnline = true;
	}

	public L2SummonInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
	{
		this(objectId, template, owner, skill, -1);
	}

	@Override
	public int getSummonType()
	{
		return 1;
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		synchronized(this)
		{
			if(isDead())
			{
				return false;
			}

			if(isNoblesseBlessed())
			{
				stopNoblesseBlessing(null);
				storeEffect(false);
			}
			else
			{
				storeEffect(false);
			}

			if(!super.doDie(killer))
			{
				return false;
			}
		}

		if(_skillCastingTask != null)
		{
			_skillCastingTask.cancel(false);
			_skillCastingTask = null;
		}

		if(_summonLifeTask != null)
		{
			_summonLifeTask.cancel(false);
			_summonLifeTask = null;
		}

		CharSummonTable.getInstance().removeResummon(getOwner());
		return true;
	}

	@Override
	protected boolean unSummon(L2PcInstance owner, boolean ignoreDeathAndVis)
	{
		if(_skillCastingTask != null)
		{
			_skillCastingTask.cancel(false);
			_skillCastingTask = null;
		}

		if(_summonLifeTask != null)
		{
			_summonLifeTask.cancel(false);
			_summonLifeTask = null;
		}

		super.unSummon(owner, ignoreDeathAndVis);

		if(!_restoreSummon)
		{
			CharSummonTable.getInstance().removeResummon(owner);
		}

		return true;
	}

	/**
	 * Останавливает таск после удаления объекта
	 */
	@Override
	public boolean onDecay()
	{
		if(_skillCastingTask != null)
		{
			_skillCastingTask.cancel(false);
			_skillCastingTask = null;
		}
		return super.onDecay();
	}

	@Override
	public void setRestoreSummon(boolean val)
	{
		_restoreSummon = val;
	}

	@Override
	public int getPointsToSummon()
	{
		return SummonPointsTable.getInstance().getPointsForSummonId(getNpcId());
	}

	public float getExpPenalty()
	{
		return _expPenalty;
	}

	public void setExpPenalty(float expPenalty)
	{
		_expPenalty = expPenalty;
	}

	public void setSharedElementals(boolean val)
	{
		_shareElementals = val;
	}

	public boolean isSharingElementals()
	{
		return _shareElementals;
	}

	public void setSharedElementalsValue(double val)
	{
		_sharedElementalsPercent = val;
	}

	public double sharedElementalsPercent()
	{
		return _sharedElementalsPercent;
	}

	public int getTotalLifeTime()
	{
		return _totalLifeTime;
	}

	public int getTimeLostIdle()
	{
		return _timeLostIdle;
	}

	public int getTimeLostActive()
	{
		return _timeLostActive;
	}

	public int getTimeRemaining()
	{
		return _timeRemaining;
	}

	public void setTimeRemaining(int time)
	{
		_timeRemaining = time;
	}

	public void decTimeRemaining(int value)
	{
		_timeRemaining -= value;
	}

	public void addExpAndSp(int addToExp, int addToSp)
	{
		getOwner().addExpAndSp(addToExp, addToSp);
	}

	@Override
	public boolean destroyItemByItemId(ProcessType process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		return getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
	}

	@Override
	public boolean destroyItem(ProcessType process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		return getOwner().destroyItem(process, objectId, count, reference, sendMessage);
	}

	@Override
	public boolean isMovementDisabled()
	{
		return _isMovementDisable;
	}

	@Override
	public int getLevel()
	{
		return getTemplate() != null ? getTemplate().getLevel() : 0;
	}

	@Override
	public byte getAttackElement()
	{
		if(_shareElementals && getOwner() != null)
		{
			return getOwner().getAttackElement();
		}
		return super.getAttackElement();
	}

	@Override
	public int getAttackElementValue(byte attackAttribute)
	{
		if(_shareElementals && getOwner() != null)
		{
			return (int) (getOwner().getAttackElementValue(attackAttribute) * sharedElementalsPercent());
		}
		// 100% of master
		return getOwner().getAttackElementValue(attackAttribute);
	}

	@Override
	public int getDefenseElementValue(byte defenseAttribute)
	{
		if(_shareElementals && getOwner() != null)
		{
			return (int) (getOwner().getDefenseElementValue(defenseAttribute) * sharedElementalsPercent());
		}
		return super.getDefenseElementValue(defenseAttribute);
	}

	public int getReferenceSkill()
	{
		return _referenceSkill;
	}

	/**
	 * Стартует таск на каст заданного скилла
	 *
	 * @param skillId ID умения
	 * @param skilllLevel уровень умения
	 */
	public void startSkillCastingTask(int skillId, int skilllLevel)
	{
		_skill = SkillTable.getInstance().getInfo(skillId, skilllLevel);
		if(_skillCastingTask == null)
		{
			_skillCastingTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SummonSkillCastingTask(this), 100, _skill.getReuseDelay());
		}
	}

	@Override
	public boolean isOnline()
	{
		return _isOnline;
	}

	@Override
	public void store()
	{
		if(_referenceSkill == 0 || isDead())
		{
		}
	}

	@Override
	public void storeEffect(boolean storeEffects)
	{
		if(!Config.SUMMON_STORE_SKILL_COOLTIME)
		{
			return;
		}

		if(getOwner().getOlympiadController().isParticipating())
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			// Delete all current stored effects for summon to avoid dupe
			statement = con.prepareStatement(DELETE_SKILL_SAVE);

			statement.setInt(1, getOwner().getObjectId());
			statement.setInt(2, getOwner().getClassIndex());
			statement.setInt(3, getObjectId());

			statement.execute();
			DatabaseUtils.closeStatement(statement);

			int buff_index = 0;

			List<Integer> storedSkills = new FastList<>();

			//Store all effect data along with calculated remaining
			statement = con.prepareStatement(ADD_SKILL_SAVE);

			if(storeEffects)
			{
				for(L2Effect effect : getAllEffects())
				{
					switch(effect.getEffectType())
					{
						case HEAL_OVER_TIME:
						case CPHEAL_OVER_TIME:
							// TODO: Fix me.
						case HIDE:
							continue;
					}

					L2Skill skill = effect.getSkill();
					if(storedSkills.contains(skill.getReuseHashCode()))
					{
						continue;
					}

					storedSkills.add(skill.getReuseHashCode());

					if(!effect.isHerbEffect() && effect.isInUse() && !skill.isToggle())
					{
						statement.setInt(1, getOwner().getObjectId());
						statement.setInt(2, getOwner().getClassIndex());
						statement.setInt(3, getObjectId());
						statement.setInt(4, skill.getId());
						statement.setInt(5, skill.getLevel());
						statement.setInt(6, effect.getCount());
						statement.setInt(7, effect.getTime());
						statement.setInt(8, ++buff_index);
						statement.execute();
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not store summon effect data: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public String getSummonGroupReplace()
	{
		return _summonGroupReplace;
	}

	public void setSummonGroupReplace(String name)
	{
		_summonGroupReplace = name;
	}

	/**
	 * Используется новым саммоном Древо Жизни, которые ничего не делают, кроме как кастуют заданный скилл.
	 */
	private class SummonSkillCastingTask implements Runnable
	{
		private final L2SummonInstance _caster;

		protected SummonSkillCastingTask(L2SummonInstance caster)
		{
			_caster = caster;
		}

		@Override
		public void run()
		{
			if(_skill == null || _caster == null)
			{
				if(_caster._skillCastingTask != null)
				{
					_caster._skillCastingTask.cancel(false);
					_caster._skillCastingTask = null;
				}
				return;
			}

			_caster.doSimultaneousCast(_skill);
		}
	}
}