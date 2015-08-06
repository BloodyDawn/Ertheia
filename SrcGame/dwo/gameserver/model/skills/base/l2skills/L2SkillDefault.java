package dwo.gameserver.model.skills.base.l2skills;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;

public class L2SkillDefault extends L2Skill
{
	public L2SkillDefault(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		caster.sendActionFailed();
		caster.sendMessage("Skill not implemented. Skill ID: " + getId() + ' ' + getSkillType());
	}
}