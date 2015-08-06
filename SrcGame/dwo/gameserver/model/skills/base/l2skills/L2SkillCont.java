package dwo.gameserver.model.skills.base.l2skills;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;

public class L2SkillCont extends L2Skill
{
	private final int _castSkillId;
	private final int _castSkillLevel;

	public L2SkillCont(StatsSet set)
	{
		super(set);
		_castSkillId = set.getInteger("skillToCast", 0);
		_castSkillLevel = set.getInteger("skillToCastLevel", 0);
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{

	}

	/**
	 * @return skill ID that may casted on action time of current skill effect.
	 */
	public int getCastSkillId()
	{
		return _castSkillId;
	}

	/**
	 * @return skill level that may casted on action time of current skill effect.
	 */
	public int getCastSkillLevel()
	{
		return _castSkillLevel;
	}
}
