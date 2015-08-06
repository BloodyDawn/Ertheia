package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.serverpackets.EtcStatusUpdate;

/**
 * @author nBd
 */

public class CharmOfCourage extends L2Effect
{
	public CharmOfCourage(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CHARMOFCOURAGE;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected() instanceof L2PcInstance)
		{
			getEffected().broadcastPacket(new EtcStatusUpdate((L2PcInstance) getEffected()));
			return true;
		}
		return false;
	}

	@Override
	public void onExit()
	{
		if(getEffected() instanceof L2PcInstance)
		{
			getEffected().broadcastPacket(new EtcStatusUpdate((L2PcInstance) getEffected()));
		}
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_CHARM_OF_COURAGE;
	}
}
