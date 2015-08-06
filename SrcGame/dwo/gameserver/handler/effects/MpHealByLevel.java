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
 * L2GOD Team
 * User: Keiichi
 * Date: 20.11.2011
 * Time: 2:21:10
 */

public class MpHealByLevel extends L2Effect
{
	public MpHealByLevel(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MANAHEAL_BY_LEVEL;
	}

	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		if(target == null || target.isDead() || target instanceof L2DoorInstance)
		{
			return false;
		}

		restoreMp(target);

		return true;
	}

	/**
	 * Restores character MP.
	 *
	 * @param target Target character.
	 * @return Rest of restoration (i.e. AMOUNT - REAL_RESTORE_AMOUNT. This value can be added to HP/CP or etc.
	 */
	public double restoreMp(L2Character target)
	{
		StatusUpdate su = new StatusUpdate(target);

		double amount = calc();

		//recharged mp influenced by difference between target level and skill level
		//if target is within 5 levels or lower then skill level there's no penalty.
		amount = target.calcStat(Stats.RECHARGE_MP_RATE, amount, null, null);
		if(target.getLevel() > getSkill().getMagicLevel())
		{
			int lvlDiff = target.getLevel() - getSkill().getMagicLevel();
			//if target is too high compared to skill level, the amount of recharged mp gradually decreases.
			if(lvlDiff == 6)        //6 levels difference:
			{
				amount *= 0.9;            //only 90% effective
			}
			else if(lvlDiff == 7)
			{
				amount *= 0.8;            //80%
			}
			else if(lvlDiff == 8)
			{
				amount *= 0.7;            //70%
			}
			else if(lvlDiff == 9)
			{
				amount *= 0.6;            //60%
			}
			else if(lvlDiff == 10)
			{
				amount *= 0.5;            //50%
			}
			else if(lvlDiff == 11)
			{
				amount *= 0.4;            //40%
			}
			else if(lvlDiff == 12)
			{
				amount *= 0.3;            //30%
			}
			else if(lvlDiff == 13)
			{
				amount *= 0.2;            //20%
			}
			else if(lvlDiff == 14)
			{
				amount *= 0.1;            //10%
			}
			else if(lvlDiff >= 15)    //15 levels or more:
			{
				amount = 0;                //0mp recharged
			}
		}

		double rest = Math.max(0, amount - (target.getMaxRecoverableMp() - target.getCurrentMp()));
		amount = Math.min(amount, target.getMaxRecoverableMp() - target.getCurrentMp());

		// Prevent negative amounts
		if(amount < 0)
		{
			amount = 0;
		}

		// To prevent -value heals, set the value only if current mp is less than max recoverable.
		if(target.getCurrentMp() < target.getMaxRecoverableMp())
		{
			target.setCurrentMp(amount + target.getCurrentMp());
		}

		SystemMessage sm;
		if(getEffector().getObjectId() == target.getObjectId())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_C1);
			sm.addCharName(getEffector());
		}
		sm.addNumber((int) amount);
		target.sendPacket(sm);
		su.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
		target.sendPacket(su);
		return rest;
	}

}
