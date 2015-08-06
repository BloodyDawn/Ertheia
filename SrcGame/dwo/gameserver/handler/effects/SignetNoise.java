package dwo.gameserver.handler.effects;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2EffectPointInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * @author Forsaiken, Sami
 */

public class SignetNoise extends L2Effect
{
	private L2EffectPointInstance _actor;

	public SignetNoise(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_GROUND;
	}

	@Override
	public boolean onStart()
	{
		_actor = (L2EffectPointInstance) getEffected();
		return true;
	}

	@Override
	public void onExit()
	{
		if(_actor != null)
		{
			_actor.getLocationController().delete();
		}
	}

	@Override
	public boolean onActionTime()
	{
		if(getCount() == getEffectTemplate().getTotalTickCount())
		{
			return false; // do nothing first time
		}

		L2PcInstance caster = (L2PcInstance) getEffector();

		for(L2Character target : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if(target == null || target.equals(caster))
			{
				continue;
			}
			if(!GeoEngine.getInstance().canSeeTarget(_actor, target))
			{
				return false;
			}

			if(caster.canAttackCharacter(target))
			{
				for(L2Effect effect : target.getAllEffects())
				{
					if(effect != null && effect.getSkill().isDance())
					{
						effect.exit();
					}
				}
			}
		}
		return getSkill().isToggle();
	}
}
