package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 09.03.12
 * Time: 15:48
 */

public class TalismanPower extends L2Effect
{
	public TalismanPower(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.TALISMAN_POWER;
	}

	@Override
	public boolean onStart()
	{
		L2Skill skill = SkillTable.getInstance().getInfo(getSkill().getTriggeredId(), getSkill().getTriggeredLevel());
		if(skill != null)
		{
			skill.getEffects(getEffected(), getEffected());
		}
		return super.onStart();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().stopSkillEffects(getSkill().getTriggeredId());
	}
}