package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2NpcInstance;
import dwo.gameserver.model.actor.instance.L2SiegeSummonInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.serverpackets.FinishRotating;
import dwo.gameserver.network.game.serverpackets.StartRotating;

/**
 * @author decad
 *
 * Implementation of the Bluff Effect
 */

public class Bluff extends L2Effect
{
	public Bluff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BLUFF; // test for bluff effect
	}

	@Override
	public boolean onStart()
	{
		if(getEffected() instanceof L2NpcInstance)
		{
			return false;
		}

		if(getEffected().isNpc() && ((L2Npc) getEffected()).getNpcId() == 35062)
		{
			return false;
		}

		if(getEffected() instanceof L2SiegeSummonInstance)
		{
			return false;
		}

		getEffected().broadcastPacket(new StartRotating(getEffected().getObjectId(), getEffected().getHeading(), 1, 65535));
		getEffected().broadcastPacket(new FinishRotating(getEffected().getObjectId(), getEffector().getHeading(), 65535));
		getEffected().setHeading(getEffector().getHeading());
		return true;
	}

}
