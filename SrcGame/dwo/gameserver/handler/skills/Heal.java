/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.handler.SkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2SiegeFlagInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.calculations.MagicalDamage;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class Heal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.HEAL, L2SkillType.HEAL_STATIC, L2SkillType.HEAL_COHERENTLY
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		//check for other effects
		ISkillHandler handler = SkillHandler.getInstance().getHandler(L2SkillType.BUFF);

		double skillPower = skill.getPower();
		boolean sps = activeChar.isSpiritshotCharged(skill);
		boolean bss = activeChar.isBlessedSpiritshotCharged(skill);

		switch(skill.getSkillType())
		{
			case HEAL_STATIC:
				break;
			default:
				double staticShotBonus = 0;
				int mAtkMul = 1; // mAtk multiplier
				if((sps || bss) && activeChar.isPlayer() && activeChar.getActingPlayer().isMageClass() || activeChar.isSummon())
				{
					staticShotBonus = skill.getMpConsume(); // static bonus for spiritshots

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
					staticShotBonus = 2.4 * skill.getMpConsume(); // always blessed spiritshots
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
							case S:
								mAtkMul = 2;
								break;
							case S80:
								mAtkMul = 2;
								break;
							case S84:
								mAtkMul = 4;
								break;
							case R:
								mAtkMul = 4; // Уточнить бонус!
								break;
							case R95:
								mAtkMul = 4; // Уточнить бонус!
								break;
							case R99:
								mAtkMul = 4; // Уточнить бонус!
								break;
							default:
								mAtkMul = 1; // other grade
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
				}

				// (корень(мАтак) + повер ) * 2
				skillPower += staticShotBonus + Math.sqrt(mAtkMul * activeChar.getMAtk(activeChar, null));
				activeChar.spsUncharge(skill);
		}

		double power;
		double powerHp = 0.0D;
		double powerCp = 0.0D;

		for(L2Character target : (L2Character[]) targets)
		{
			// We should not heal if char is dead/invul
			if(target == null || target.isDead() || target.isInvul())
			{
				continue;
			}

			if(target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance)
			{
				continue;
			}

			// Player holding a cursed weapon can't be healed and can't heal
			if(!target.equals(activeChar))
			{
				if(target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
				{
					continue;
				}
				else if(activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isCursedWeaponEquipped())
				{
					continue;
				}
			}

			switch(skill.getSkillType())
			{
				case HEAL_PERCENT:
					power = target.getMaxHp() * skillPower / 100.0 * target.calcStat(Stats.HEAL_PERCENT_BONUS, 1, null, null);
					break;
				//case HEAL_COHERENTLY:
				//	power = skillPower / 2;
				//	break;
				default:
					power = skillPower;
					power *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;
			}

			// Healer proficiency (since CT1)
			power *= activeChar.calcStat(Stats.HEAL_PROFICIENCY, 100, null, null) / 100;
			// Extra bonus (since CT1.5)
			if(!skill.isStatic())
			{
				power += target.calcStat(Stats.HEAL_STATIC_BONUS, 0, null, null);
			}

			// Heal critic, since CT2.3 Gracia Final
			if(!skill.isStatic() && MagicalDamage.calcMCrit(activeChar.getMCriticalHit(target, skill)))
			{
				switch(skill.getSkillType())
				{
					case HEAL:
						power *= 2;
						break;
					case HEAL_COHERENTLY:
						power *= 3;    // Уточнить бонус!
						break;
					default:
						power *= 1; // Уточнить бонус!
						break;
				}
			}

			if(power < 0)
			{
				power = 0;
			}

			if(skill.getSkillType() == L2SkillType.HEAL_COHERENTLY && target instanceof L2Playable)
			{
				// Первым проверяем HP, если power + текущее HP > максимального ХП, то остаток вливаем в CP
				if(target.getCurrentHp() < target.getMaxRecoverableHp())
				{
					if(target.getCurrentHp() + power > target.getMaxRecoverableHp())
					{
						powerHp = target.getMaxRecoverableHp() - target.getCurrentHp();
						powerCp = power - powerHp;
					}
					else
					{
						powerHp = power;
					}
				}
				// Вторым проверяем CP, если power + текущее CP > максимального CP то режем повер на остаток
				// Обработки ХП нет, т.к. она в первом условии до этого.
				else if(target.getCurrentCp() < target.getMaxRecoverableCp())
				{
					powerCp = target.getCurrentCp() + power > target.getMaxRecoverableCp() ? target.getMaxRecoverableCp() - target.getCurrentCp() : power;
				}
			}
			else if(target.getCurrentHp() < target.getMaxRecoverableHp())
			{
				powerHp = Math.min(power, target.getMaxHp() - target.getCurrentHp());
			}

			target.setCurrentHp(target.getCurrentHp() + powerHp);

			if(target instanceof L2PcInstance)
			{
				target.setCurrentCp(target.getCurrentCp() + powerCp);
			}

			StatusUpdate su = new StatusUpdate(target);
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			if(target instanceof L2PcInstance)
			{
				su.addAttribute(StatusUpdate.CUR_CP, (int) target.getCurrentCp());
			}
			target.sendPacket(su);

			if(target instanceof L2PcInstance)
			{
				if(skill.getId() == 4051)
				{
					target.sendPacket(SystemMessageId.REJUVENATING_HP);
				}
				else
				{
					if(activeChar instanceof L2PcInstance && !activeChar.equals(target))
					{
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1).addString(activeChar.getName()).addNumber((int) powerHp));
						if(powerCp > 0)
						{
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_CP_WILL_BE_RESTORED_BY_C1).addString(activeChar.getName()).addNumber((int) powerCp));
						}
					}
					else
					{
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED).addNumber((int) powerHp));
						if(powerCp > 0)
						{
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED).addNumber((int) powerCp));
						}
					}
				}
			}
		}

		if(skill.isSuicideAttack())
		{
			activeChar.doDie(activeChar);
		}

		if(handler != null)
		{
			handler.useSkill(activeChar, skill, targets);
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}