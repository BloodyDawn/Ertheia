package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2SiegeSummonInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.02.12
 * Time: 16:54
 */

public class TargetLock extends L2Effect
{
	public TargetLock(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.TARGET_ME;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected() instanceof L2Playable)
		{
			if(getEffected() instanceof L2SiegeSummonInstance)
			{
				return false;
			}

			getEffected().setTarget(getEffector());
			((L2Playable) getEffected()).setLockedTarget(getEffector());
			getEffected().sendPacket(new MyTargetSelected(getEffector().getObjectId(), 0));
			// getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getEffector());
			return true;
		}
		return getEffected() instanceof L2Attackable && !getEffected().isRaid();

	}

	@Override
	public void onExit()
	{
		if(getEffected() instanceof L2Playable)
		{
			((L2Playable) getEffected()).setLockedTarget(null);
		}
	}

}
