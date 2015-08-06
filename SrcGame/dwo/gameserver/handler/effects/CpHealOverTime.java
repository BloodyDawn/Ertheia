package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;

public class CpHealOverTime extends L2Effect
{
	public CpHealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	// Special constructor to steal this effect
	public CpHealOverTime(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CPHEAL_OVER_TIME;
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
		{
			return false;
		}

		double cp = getEffected().getCurrentCp();
		double maxcp = getEffected().getMaxRecoverableCp();

		// Not needed to set the CP and send update packet if player is already at max CP
		if(cp >= maxcp)
		{
			return false;
		}

		cp += calc();
		if(cp > maxcp)
		{
			cp = maxcp;
		}

		getEffected().setCurrentCp(cp);
		StatusUpdate sump = new StatusUpdate(getEffected());
		sump.addAttribute(StatusUpdate.CUR_CP, (int) cp);
		getEffected().sendPacket(sump);
		return getSkill().isToggle();
	}

	@Override
	protected boolean effectCanBeStolen()
	{
		return true;
	}
}
