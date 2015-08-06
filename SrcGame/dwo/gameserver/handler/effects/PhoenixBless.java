package dwo.gameserver.handler.effects;

import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

public class PhoenixBless extends L2Effect
{
	public PhoenixBless(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.PHOENIX_BLESSING;
	}

	@Override
	public boolean onStart()
	{
		if(getEffector() instanceof L2PcInstance)
		{
			L2PcInstance player = getEffector().getActingPlayer();
			if(EventManager.isStarted() && EventManager.isPlayerParticipant(player))
			{
				player.sendMessage("You cannot use that skill in TvT Event");
				return false;
			}
		}
		return true;
	}

	@Override
	public void onExit()
	{
		if(getEffected() instanceof L2Playable)
		{
			((L2Playable) getEffected()).stopPhoenixBlessing(this);
		}
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_PHOENIX_BLESSING;
	}
}