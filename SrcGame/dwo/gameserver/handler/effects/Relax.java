package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;

public class Relax extends L2Effect
{
	public Relax(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.RELAXING;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected() instanceof L2PcInstance)
		{
			((L2PcInstance) getEffected()).sitDown(false);
		}
		else
		{
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		}
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

		if(getEffected() instanceof L2PcInstance)
		{
			if(!((L2PcInstance) getEffected()).isSitting())
			{
				return false;
			}
		}

		if(getEffected().getCurrentHp() + 1 > getEffected().getMaxHp())
		{
			if(getSkill().isToggle())
			{
				getEffected().sendPacket(SystemMessageId.SKILL_DEACTIVATED_HP_FULL);
				return false;
			}
		}

		double manaDam = calc();

		if(manaDam > getEffected().getCurrentMp())
		{
			if(getSkill().isToggle())
			{
				getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
				return false;
			}
		}

		getEffected().reduceCurrentMp(manaDam);
		return getSkill().isToggle();
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_RELAXING;
	}
}
