package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 03.04.12
 * Time: 23:29
 */
public class CpHpHeal extends L2Effect
{
	public CpHpHeal(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CPHPHEAL;
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

		double powerCP = calc();
		double powerHP = calc();

		powerCP = Math.min(powerCP, target.getMaxRecoverableCp() - target.getCurrentCp());
		powerHP = Math.min(powerHP, target.getMaxRecoverableHp() - target.getCurrentHp());

		// Prevent negative amounts
		if(powerCP < 0)
		{
			powerCP = 0;
		}
		if(powerHP < 0)
		{
			powerHP = 0;
		}

		// To prevent -value heals, set the value only if current Cp is less than max recoverable.
		if(target.getCurrentCp() < target.getMaxRecoverableCp())
		{
			target.setCurrentCp(powerCP + target.getCurrentCp());
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
			target.sendPacket(sm);
			su.addAttribute(StatusUpdate.CUR_CP, (int) target.getCurrentCp());
			target.sendPacket(su);
		}
		else
		{
			target.setCurrentHp(powerHP + target.getCurrentHp());
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
			target.sendPacket(sm);
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			target.sendPacket(su);
		}

		return true;
	}

}
