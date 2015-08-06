package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * Change Hair Style effect.
 * @author Zoey76
 */
public class ChangeHairStyle extends L2Effect
{
	public ChangeHairStyle(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}

	@Override
	public boolean onStart()
	{
		if(getEffector() == null || getEffected() == null || !getEffector().isPlayer() || !getEffected().isPlayer() || getEffected().isAlikeDead())
		{
			return false;
		}

		L2PcInstance player = getEffector().getActingPlayer();
		player.getAppearance().setHairStyle(getSkill().getHairStyleId());
		player.broadcastUserInfo();
		return true;
	}
}