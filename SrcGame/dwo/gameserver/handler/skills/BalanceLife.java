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
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;

public class BalanceLife implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.BALANCE_LIFE
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		// L2Character activeChar = activeChar;
		// check for other effects
		ISkillHandler handler = SkillHandler.getInstance().getHandler(L2SkillType.BUFF);

		if(handler != null)
		{
			handler.useSkill(activeChar, skill, targets);
		}

		L2PcInstance player = null;
		if(activeChar instanceof L2PcInstance)
		{
			player = (L2PcInstance) activeChar;
		}

		double fullHP = 0;
		double currentHPs = 0;

		for(L2Character target : (L2Character[]) targets)
		{
			// Не лечим, если персонаж мертв
			if(target == null || target.isDead())
			{
				continue;
			}

			// Персонаж, владеющий Проклятым Оружием не может быть вылечен
			if(!target.equals(activeChar))
			{
				if(target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
				{
					continue;
				}
				else if(player != null && player.isCursedWeaponEquipped())
				{
					continue;
				}
			}

			fullHP += target.getMaxHp();
			currentHPs += target.getCurrentHp();
		}

		double percentHP = currentHPs / fullHP;

		for(L2Character target : (L2Character[]) targets)
		{
			if(target == null || target.isDead())
			{
				continue;
			}

			// Персонаж, владеющий Проклятым Оружием не может быть вылечен
			if(!target.equals(activeChar))
			{
				if(target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
				{
					continue;
				}
				else if(player != null && player.isCursedWeaponEquipped())
				{
					continue;
				}
			}

			double newHP = target.getMaxHp() * percentHP;

			if(newHP > target.getCurrentHp()) // Цель должна получить лечение
			{
				// Если у персонажа Hp = MaxHP, то не добавляем ничего
				if(target.getCurrentHp() > target.getMaxRecoverableHp())
				{
					newHP = target.getCurrentHp();
				}
				// Или если восстановленное ХП превышает лимит по возможному для лечения ХП
				else if(newHP > target.getMaxRecoverableHp())
				{
					newHP = target.getMaxRecoverableHp();
				}
			}
			target.setCurrentHp(newHP);

			StatusUpdate su = new StatusUpdate(target);
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			target.sendPacket(su);
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
