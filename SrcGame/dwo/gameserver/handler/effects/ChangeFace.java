package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * Change Face effect.
 * @author Zoey76
 */
public class ChangeFace extends L2Effect
{
	public ChangeFace(Env env, EffectTemplate template)
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
		player.getAppearance().setFace(getSkill().getFaceId());
		player.broadcastUserInfo();
		return true;
	}
}
