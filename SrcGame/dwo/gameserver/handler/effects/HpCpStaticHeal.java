package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * L2GOD Team
 *
 * @author Bacek
 *         Date: 07.06.13
 *         Time: 22:48
 */
public class HpCpStaticHeal extends L2Effect
{
	public HpCpStaticHeal(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CPHEAL;
	}

	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		if(target == null || target.isDead() || target instanceof L2DoorInstance)
		{
			return false;
		}

		double amount = calc();
		double add = 0;

		if(amount > 0 && target.getCurrentHp() < target.getMaxRecoverableHp())
		{
			add = Math.min(amount, target.getMaxRecoverableHp() - target.getCurrentHp());
			target.setCurrentHp(add + target.getCurrentHp());
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
			sm.addNumber((int) add);
			target.sendPacket(sm);

			amount -= add;
		}

		if(amount > 0 && target.getCurrentCp() < target.getMaxRecoverableCp())
		{
			add = Math.min(amount, target.getMaxRecoverableCp() - target.getCurrentCp());
			target.setCurrentCp(add + target.getCurrentCp());
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
			sm.addNumber((int) add);
			target.sendPacket(sm);
		}

		return true;
	}

}
