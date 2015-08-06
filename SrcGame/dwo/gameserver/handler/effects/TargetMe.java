package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2SiegeSummonInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;

/**
 * @author -Nemesiss-
 */

public class TargetMe extends L2Effect
{
	public TargetMe(Env env, EffectTemplate template)
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

			if(getEffected().getTarget() != getEffector())
			{
				L2PcInstance effector = getEffector().getActingPlayer();
				// If effector is null, then its not a player, but NPC. If its not null, then it should check if the skill is pvp skill.
				if(effector == null || effector.checkPvpSkill(getEffected(), getSkill()))
				{
					// Target is different
					getEffected().setTarget(getEffector());
					if(getEffected().isPlayer())
					{
						getEffected().sendPacket(new MyTargetSelected(getEffector().getObjectId(), 0));
					}
				}

				if(getEffected() instanceof L2PcInstance)
				{
					getEffected().sendPacket(new MyTargetSelected(getEffector().getObjectId(), 0));
				}
				// Target is different
				getEffected().setTarget(getEffector());
			}
			// ((L2Playable)getEffected()).setLockedTarget(getEffector()); // Закомментировал, игроки говорят что агр не лочит таргеты. Есть видео подтверждающие это.
			// getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getEffector());
			return true;
		}
		return getEffected() instanceof L2Attackable && !getEffected().isRaid();

	}

	@Override
	public void onExit()
	{
		/*
		if (getEffected() instanceof L2Playable)
		{
			((L2Playable)getEffected()).setLockedTarget(null);
		}
		*/
	}

}