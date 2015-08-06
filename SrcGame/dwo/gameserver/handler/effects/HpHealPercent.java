package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * @author UnAfraid
 */

public class HpHealPercent extends L2Effect
{
	public HpHealPercent(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HEAL_PERCENT;
	}

	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		if(target == null || target.isDead() || target instanceof L2DoorInstance)
		{
			return false;
		}

		StatusUpdate su = new StatusUpdate(target);
		double amount = 0;
		double power = calc();
		boolean full = power == 100.0;

		if(full)
		{
			amount = target.getMaxHp();
		}
		else
		{
			amount = target.getMaxHp() * power / 100.0;
			amount *= target.calcStat(Stats.HEAL_PERCENT_BONUS, 1, null, null);
		}

		amount = Math.min(amount, target.getMaxRecoverableHp() - target.getCurrentHp());

		// Prevent negative amounts
		if(amount < 0)
		{
			amount = 0;
		}

		// To prevent -value heals, set the value only if current hp is less than max recoverable.
		if(target.getCurrentHp() < target.getMaxRecoverableHp())
		{
			target.setCurrentHp(amount + target.getCurrentHp());
		}

		SystemMessage sm;
		if(getEffector().getObjectId() == target.getObjectId())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
			sm.addCharName(getEffector());
		}

		sm.addNumber((int) amount);
		target.sendPacket(sm);
		su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
		target.sendPacket(su);

		return true;
	}

}
