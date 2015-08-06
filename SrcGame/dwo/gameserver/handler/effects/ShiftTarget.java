package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * User: Bacek
 * Date: 23.06.13
 * Time: 8:23
 */
public class ShiftTarget extends L2Effect
{
	public ShiftTarget(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SHIFT_TARGET;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected().isAlikeDead())
		{
			return false;
		}

		for(L2Character obj : getEffector().getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if(!(obj instanceof L2MonsterInstance) || obj.isDead() || obj.getTarget() == null || !obj.getTarget().equals(getEffector()))
			{
				continue;
			}

			L2Attackable hater = (L2Attackable) obj;
			hater.addDamageHate(getEffected(), 0, hater.getHating(getEffector()) + 1);
		}

		return true;
	}

	@Override
	public void onExit()
	{
	}
}