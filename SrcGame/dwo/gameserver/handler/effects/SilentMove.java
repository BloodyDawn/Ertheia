package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;

public class SilentMove extends L2Effect
{
	public SilentMove(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	// Special constructor to steal this effect
	public SilentMove(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SILENT_MOVE;
	}

	@Override
	public boolean onStart()
	{
		super.onStart();
		return true;
	}

	@Override
	public void onExit()
	{
		super.onExit();
	}

	@Override
	public boolean onActionTime()
	{
		// Only cont skills shouldn't end
		if(getSkill().getSkillType() != L2SkillType.CONT)
		{
			return false;
		}

		if(getEffected().isDead())
		{
			return false;
		}

		double manaDam = calc();

		if(manaDam > getEffected().getCurrentMp())
		{
			getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
			return false;
		}

		getEffected().reduceCurrentMp(manaDam);
		return getSkill().isToggle();
	}

	@Override
	protected boolean effectCanBeStolen()
	{
		return true;
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_SILENT_MOVE;
	}
}
