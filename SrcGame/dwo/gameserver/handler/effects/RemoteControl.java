package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 11.09.12
 * Time: 20:01
 */
public class RemoteControl extends L2Effect
{
	private static final int TIME_BOMB = 10786;

	public RemoteControl(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NEGATE_MARK;
	}

	@Override
	public boolean onStart()
	{
		L2Character effected = getEffected();
		L2Character effector = getEffector();

		L2Effect effect = effected.getFirstEffect(TIME_BOMB);
		if(effect != null && effect.getEffector().equals(effector))
		{
			effect.exit();
		}

		return true;
	}

}
