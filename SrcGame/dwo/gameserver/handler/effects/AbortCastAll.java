package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 09.09.12
 * Time: 4:14
 * L2GOD Team
 * info: эффект прерывания всех типов скиллов физ/маг.
 */
public class AbortCastAll extends L2Effect
{
	public AbortCastAll(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.ABORT_CAST;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected() == null || getEffected().equals(getEffector()))
		{
			return false;
		}

		if(getEffected().isRaid())
		{
			return false;
		}

		getEffected().breakCastAll();
		return true;
	}

}
