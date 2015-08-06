package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.restriction.RestrictionChain;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;

public class SummonAgathion extends L2Effect
{
	public SummonAgathion(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	/**
	 * Set the player's agathion Id.
	 * @param player the player to set the agathion Id.
	 */
	protected void setAgathionId(L2PcInstance player)
	{
		player.setAgathionId(getSkill() == null ? 0 : getSkill().getNpcId());
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SUMMON_AGATHION;
	}

	@Override
	public boolean onStart()
	{
		if(getEffector() == null || getEffected() == null || !getEffector().isPlayer() || !getEffected().isPlayer() || getEffected().isAlikeDead())
		{
			return false;
		}

		L2PcInstance player = getEffector().getActingPlayer();
		if(!player.getRestrictionController().check(RestrictionChain.SUMMON_AGATHION).passed())
		{
			player.sendPacket(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}

		setAgathionId(player);
		player.broadcastUserInfo();
		return true;
	}

	@Override
	public void onExit()
	{
		super.onExit();

		L2PcInstance player = getEffector().getActingPlayer();
		if(player != null)
		{
			player.setAgathionId(0);
			player.broadcastUserInfo();
		}
	}
}
