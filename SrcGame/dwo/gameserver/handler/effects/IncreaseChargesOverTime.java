package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 * L2GOD Team
 * @author Keiichi
 * Date: 25.03.12
 * Time: 3:10
 */

public class IncreaseChargesOverTime extends L2Effect
{
	public IncreaseChargesOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CHARGE_OVER_TIME;
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
		{
			return false;
		}

		double damage = calc();
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
		int increaseCount = 1; //Вынести хардкод кол-во зарядок за тик.
		int chargeMaxCount = (int) getEffected().calcStat(Stats.ENERGY_MASTERY, 0, null, null); //Максимальное кол-во зарядок берем из пассивки.

		if(((L2PcInstance) getEffected()).getCharges() < chargeMaxCount)
		{
			getEffected().reduceCurrentHpByDOT(damage, getEffector(), getSkill());
			((L2PcInstance) getEffected()).increaseCharges(increaseCount, chargeMaxCount);
		}

		return getSkill().isToggle();
	}
}
