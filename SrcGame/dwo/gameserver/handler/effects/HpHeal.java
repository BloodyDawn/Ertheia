package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.base.formulas.calculations.MagicalDamage;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class HpHeal extends L2Effect
{
	public HpHeal(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HEAL;
	}

	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		L2Character activeChar = getEffector();
		if(target == null || target.isDead() || target instanceof L2DoorInstance)
		{
			return false;
		}

		double amount = calc();
		double staticShotBonus = 0;
		int mAtkMul = 1;

		boolean sps = activeChar.isSpiritshotCharged(getSkill());
		boolean bss = activeChar.isBlessedSpiritshotCharged(getSkill());

		if((sps || bss) && activeChar.isPlayer() && activeChar.getActingPlayer().isMageClass() || activeChar.isSummon())
		{
			staticShotBonus = getSkill().getMpConsume(); // static bonus for spiritshots

			if(bss)
			{
				mAtkMul = 4;
				staticShotBonus *= 2.4; // static bonus for blessed spiritshots
			}
			else
			{
				mAtkMul = 2;
			}
		}
		else if((sps || bss) && activeChar.isNpc())
		{
			staticShotBonus = 2.4 * getSkill().getMpConsume(); // always blessed spiritshots
			mAtkMul = 4;
		}
		else
		{
			// no static bonus
			// grade dynamic bonus
			L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
			if(weaponInst != null)
			{
				// no static bonus
				// grade dynamic bonus
				switch(weaponInst.getItem().getItemGrade())
				{
					case S84:
						mAtkMul = 4;
						break;
					case S80:
						mAtkMul = 2;
						break;
					// TODO: R - Grade
				}
			}
		}

		// shot dynamic bonus
		if(bss)
		{
			mAtkMul <<= 2; // 16x/8x/4x s84/s80/other
		}
		else
		{
			mAtkMul += 1; // 5x/3x/1x s84/s80/other
		}

		if(!getSkill().isStaticHeal())
		{
			amount += staticShotBonus + Math.sqrt(mAtkMul * activeChar.getMAtk(activeChar, null));
			amount *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;
			// Healer proficiency (since CT1)
			amount *= activeChar.calcStat(Stats.HEAL_PROFICIENCY, 100, null, null) / 100;
			// Extra bonus (since CT1.5)
			if(!getSkill().isStatic())
			{
				amount += target.calcStat(Stats.HEAL_STATIC_BONUS, 0, null, null);
			}

			// Heal critic, since CT2.3 Gracia Final
			if(!getSkill().isStatic() && MagicalDamage.calcMCrit(activeChar.getMCriticalHit(target, getSkill())))
			{
				amount *= 3;
			}
		}

		amount = Math.min(amount, target.getMaxRecoverableHp() - target.getCurrentHp());

		// Prevent negative amounts
		if(amount < 0)
		{
			amount = 0;
		}

		target.setCurrentHp(amount + target.getCurrentHp());
		StatusUpdate su = new StatusUpdate(target);
		su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
		target.sendPacket(su);

		if(target instanceof L2PcInstance)
		{
			if(getSkill().getId() == 4051)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REJUVENATING_HP);
				target.sendPacket(sm);
			}
			else
			{
				if(activeChar instanceof L2PcInstance && !activeChar.equals(target))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
					sm.addString(activeChar.getName());
					sm.addNumber((int) amount);
					target.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
					sm.addNumber((int) amount);
					target.sendPacket(sm);
				}
			}
		}
		return true;
	}

}