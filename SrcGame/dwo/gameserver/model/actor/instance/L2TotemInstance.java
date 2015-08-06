package dwo.gameserver.model.actor.instance;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import javolution.util.FastList;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * @author UnAfraid
 */

public class L2TotemInstance extends L2Npc
{
	protected List<L2Skill> _skills = new FastList<>();
	private List<ScheduledFuture<?>> _aiTasks = new FastList<>();

	public L2TotemInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	private static boolean checkPlayerCondition(L2TotemInstance caster, L2PcInstance target, L2Skill skill)
	{
		if(!(caster.getOwner() instanceof L2PcInstance))
		{
			return false;
		}

		if(skill.isClanSkill())
		{
			if(target.getClan() != null && skill.getSkillType() == L2SkillType.BUFF && ((L2PcInstance) caster.getOwner()).getClan().equals(target.getClan()))
			{
				return true;
			}

			if(skill.getSkillType() == L2SkillType.DEBUFF && !((L2PcInstance) caster.getOwner()).getClan().equals(target.getClan()))
			{
				return true;
			}
		}
		else
		{
			return true;
		}

		return false;
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		// TODO: Все же наверно они убиваемы !!!
		player.sendActionFailed();
	}

	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		return 900;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	public boolean onDelete()
	{
		if(!_aiTasks.isEmpty())
		{
			for(ScheduledFuture<?> task : _aiTasks)
			{
				task.cancel(false);
				_aiTasks.remove(task);
			}
			_skills.clear();
		}
		return super.onDelete();
	}

	/**
	 * Стартует таск на каст заданного скилла
	 *
	 * @param skillId ID умения
	 * @param skilllLevel уровень умения
	 */
	public void startSkillCastingTask(int skillId, int skilllLevel)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(skillId, skilllLevel);
		_skills.add(skill);
		_aiTasks.add(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new TotemAI(this, skill), 3000, skill.getReuseDelay() > 3000 ? skill.getReuseDelay() : 3000));
	}

	private class TotemAI implements Runnable
	{
		private final L2TotemInstance _caster;
		private final L2Skill _skill;

		protected TotemAI(L2TotemInstance caster, L2Skill skill)
		{
			_caster = caster;
			_skill = skill;
		}

		@Override
		public void run()
		{
			if(_skills == null)
			{
				if(!_caster._aiTasks.isEmpty())
				{
					for(ScheduledFuture<?> task : _caster._aiTasks)
					{
						task.cancel(false);
						_caster._aiTasks.remove(task);
					}
				}
				return;
			}

			getKnownList().getKnownPlayersInRadius(_skill.getSkillRadius()).stream().filter(player -> checkPlayerCondition(_caster, player, _skill)).forEach(player -> _skill.getEffects(player, player));
		}
	}
}
