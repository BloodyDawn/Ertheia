package dwo.gameserver.model.skills.base.l2skills;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;

public class L2SkillSignetCasttime extends L2Skill
{
	public int _effectNpcId;
	public int _effectId;
	public int _effectLevel;

	public L2SkillSignetCasttime(StatsSet set)
	{
		super(set);
		_effectNpcId = set.getInteger("effectNpcId", -1);
		_effectId = set.getInteger("effectId", -1);
		_effectLevel = set.getInteger("effectLevel", getLevel());
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if(caster.isAlikeDead())
		{
			return;
		}

		getEffectsSelf(caster);
	}
}
