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
package dwo.gameserver.model.skills.base.l2skills;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.calculations.PhysicalDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.stats.StatsSet;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class L2SkillChargeDmg extends L2Skill
{
	private static final Logger _logDamage = LogManager.getLogger("Pdamage");

	public L2SkillChargeDmg(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if(activeChar.isAlikeDead())
		{
			return;
		}

		double modifier = 0;
		if(activeChar.isPlayer())
		{
			// thanks L0ngh0rn Vargas of L2Guru: 70*((0.8+0.201*No.Charges) * (PATK+POWER)) / PDEF
			// TODO: Нужно переделать метод повышения и понижения зарядок, сейчас он не юзабелен для скиллов не требующих полной зарядки. Наглядно смотрим : L2PcInstance строку 15903
			// Если у чара 10 зарядок и заюзал скил требующий 5 зарядок то вернет как полжено 5 зарядок, но если у чара будет 1 - 5 зарядок то вернет не 1 - 5 зарядок, а 0.
			// TODO нету разброса
			modifier = 1.19;

			if(getNumCharges() > 0)
			{
				// Для старых скилов
				modifier = 0.8 + 0.201 * getNumCharges();
			}

			if(activeChar.getActingPlayer().getCharges() > 0)
			{
				if(getMaxChargesConsumeCount() > 0)
				{
					int decreaseCharges = activeChar.getActingPlayer().getCharges() >= getMaxChargesConsumeCount() ? getMaxChargesConsumeCount() : activeChar.getActingPlayer().getCharges();

					// Верная формула
					modifier += 0.12 * decreaseCharges;

					 /* Для новых скиллов которые могут бить и без зарядки, но с зарядкой бьют сильнее, потребляют так же фиксированное кол-во зарядок. */
					((L2PcInstance) activeChar).decreaseCharges(decreaseCharges, true);
				}
			}
		}

		boolean soul = activeChar.isSoulshotCharged(this);
		for(L2Character target : (L2Character[]) targets)
		{
			if(target.isAlikeDead())
			{
				continue;
			}

			// TODO: should we use dual or not?
			// because if so, damage are lowered but we don't do anything special with dual then
			// like in doAttackHitByDual which in fact does the calcPhysDam call twice
			//boolean dual  = caster.isUsingDualWeapon();
			byte shld = Shield.calcShldUse(activeChar, target, this);

			double damage = isStaticDamage() ? getPower() : PhysicalDamage.calcPhysDam(activeChar, target, this, shld, false, false, soul);

			PhysicalDamage.physDamEffect(activeChar, target, this, shld, damage, modifier, _logDamage);
		}
		activeChar.ssUncharge(this);
	}
}
