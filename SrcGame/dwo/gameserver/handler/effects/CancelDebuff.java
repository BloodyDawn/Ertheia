package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.util.Rnd;

public class CancelDebuff extends L2Effect
{
	public CancelDebuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	private static boolean cancel(L2Character caster, L2Character target, L2Skill skill, double baseRate)
	{
		if(target.isDead())
		{
			return false;
		}

		int cancelLvl = skill.getMagicLevel();
		int count = skill.getMaxNegatedEffects();

		L2Effect effect;
		int lastCanceledSkillId = 0;
		L2Effect[] effects = target.getAllEffects();
		for(int i = effects.length; --i >= 0; )
		{
			effect = effects[i];
			if(effect == null)
			{
				continue;
			}

			if(!effect.getSkill().isDebuff() || !effect.getSkill().canBeDispeled())
			{
				effects[i] = null;
				continue;
			}

			if(effect.getSkill().getId() == lastCanceledSkillId)
			{
				effect.cancel(); // this skill already canceled
				continue;
			}

			if(!calcCancelSuccess(effect, cancelLvl, (int) baseRate))
			{
				continue;
			}

			lastCanceledSkillId = effect.getSkill().getId();
			effect.cancel();
			count--;

			if(count == 0)
			{
				break;
			}
		}
		return true;
	}

	private static boolean calcCancelSuccess(L2Effect effect, int cancelLvl, int baseRate)
	{
		int rate = 2 * (cancelLvl - effect.getSkill().getMagicLevel());
		rate += (effect.getAbnormalTime() - effect.getTime()) / 1200;
		rate += baseRate;

		if(rate < effect.getSkill().getMinChance())
		{
			rate = effect.getSkill().getMinChance();
		}
		else if(rate > effect.getSkill().getMaxChance())
		{
			rate = effect.getSkill().getMaxChance();
		}

		return Rnd.getChance(rate);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CANCEL_DEBUFF;
	}

	@Override
	public boolean onStart()
	{
		return cancel(getEffector(), getEffected(), getSkill(), getEffectPower());
	}
}