package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;

public class MarkOfVoid extends L2Effect
{

	public MarkOfVoid(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MARK_OF_VOID;
	}

	@Override
	public boolean onStart()
	{
		return super.onStart();
	}

	@Override
	public void onExit()
	{
		super.onExit();
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
		getEffected().reduceCurrentHpByDOT(damage, getEffector(), getSkill());

		if(getEffector() != null && !getEffector().isDead())
		{
			double bonus = damage * (getEffected().isPlayer() ? 0.25 : 0.50);
			getEffector().setCurrentHp(getEffector().getCurrentHp() + bonus);
			getEffector().setCurrentMp(getEffector().getCurrentMp() + bonus);

			if(!getEffector().getPets().isEmpty() && bonus > 0.0)
			{
				getEffector().getPets().stream().filter(summon -> !summon.equals(getEffected()) && !summon.isDead() && summon.getCurrentHp() > 0).forEach(summon -> {
					summon.setCurrentHp(summon.getCurrentHp() + bonus);
					summon.setCurrentMp(summon.getCurrentMp() + bonus);
				});
			}
		}
		return getSkill().isToggle();
	}
}