package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 07.12.12
 * Time: 20:49
 */

public class HpDamOverTimePercent extends L2Effect
{
	public HpDamOverTimePercent(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DMG_OVER_TIME_PERCENT;
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
		{
			return false;
		}

		double damage = getEffected().getCurrentHp() * calc();

		if(damage >= getEffected().getCurrentHp() - 1)
		{
			if(getSkill().isToggle())
			{
				getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_HP);
				return false;
			}

			// For DOT skills that will not kill effected player.
			if(!getSkill().killByDOT())
			{
				// Fix for players dying by DOTs if HP < 1 since reduceCurrentHP method will kill them
				if(getEffected().getCurrentHp() <= 1)
				{
					return false;
				}

				damage = getEffected().getCurrentHp() - 1;
			}
		}
		getEffected().reduceCurrentHpByDOT(damage, getEffector(), getSkill());
		return getSkill().isToggle();
	}
}