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
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2SiegeFlagInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.commons.lang3.ArrayUtils;

public class HealPercent implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.HEAL_PERCENT, L2SkillType.MANAHEAL_PERCENT, L2SkillType.CPHEAL_PERCENT,
		L2SkillType.HPMPHEAL_PERCENT, L2SkillType.HPMPCPHEAL_PERCENT, L2SkillType.HPCPHEAL_PERCENT
	};

	private static final int[] ALLOWED_ON_CELESTIAL = {
		1505,    // Sublime Self-Sacrifice
		23172,    // Phoenix Agathion Special Skill - Nirvana Rebirth
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		//check for other effects
		ISkillHandler handler = SkillHandler.getInstance().getHandler(L2SkillType.BUFF);

		boolean cp = false;
		boolean hp = false;
		boolean mp = false;
		switch(skill.getSkillType())
		{
			case CPHEAL_PERCENT:
				cp = true;
				break;
			case HEAL_PERCENT:
				hp = true;
				break;
			case MANAHEAL_PERCENT:
				mp = true;
				break;
			case HPMPHEAL_PERCENT:
				mp = true;
				hp = true;
				break;
			case HPMPCPHEAL_PERCENT:
				cp = true;
				hp = true;
				mp = true;
				break;
			case HPCPHEAL_PERCENT:
				hp = true;
				cp = true;
				break;
		}

		StatusUpdate su = null;
		double amount = 0;
		boolean full = skill.getPower() == 100.0;
		for(L2Character target : (L2Character[]) targets)
		{
			if(target == null || target.isDead())
			{
				continue;
			}
			if(target.isInvul() && !ArrayUtils.contains(ALLOWED_ON_CELESTIAL, skill.getId()))
			{
				continue;
			}

			// Cursed weapon owner can't heal or be healed
			if(!target.equals(activeChar))
			{
				if(activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isCursedWeaponEquipped())
				{
					continue;
				}
				if(target.isPlayer() && ((L2PcInstance) target).isCursedWeaponEquipped())
				{
					continue;
				}
			}

			// Doors and flags can't be healed in any way
			if(hp && (target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance))
			{
				continue;
			}

			// Only players have CP
			if(cp && target.isPlayer())
			{
				amount = full ? target.getMaxCp() : target.getMaxCp() * skill.getPower() / 100.0;

				amount = Math.min(amount, target.getMaxRecoverableCp() - target.getCurrentCp());

				// Prevent negative amounts
				if(amount < 0)
				{
					amount = 0;
				}

				// To prevent -value heals, set the value only if current cp is less than max recoverable.
				if(target.getCurrentCp() < target.getMaxRecoverableCp())
				{
					target.setCurrentCp(amount + target.getCurrentCp());
				}
				target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED).addNumber((int) amount));
				su = new StatusUpdate(target);
				su.addAttribute(StatusUpdate.CUR_CP, (int) target.getCurrentCp());
			}

			if(hp)
			{
				if(full)
				{
					amount = target.getMaxHp();
				}
				else
				{
					amount = target.getMaxHp() * skill.getPower() / 100.0;
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

				if(target.isPlayer())
				{
					if(activeChar.equals(target))
					{
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED).addNumber((int) amount));
					}
					else
					{
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1).addCharName(activeChar).addNumber((int) amount));
					}
					su = new StatusUpdate(target);
					su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
				}
			}

			if(mp)
			{
				amount = full ? target.getMaxMp() : target.getMaxMp() * skill.getPower() / 100.0;

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

				if(target.isPlayer())
				{
					if(activeChar.equals(target))
					{
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED).addNumber((int) amount));
					}
					else
					{
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_C1).addCharName(activeChar).addNumber((int) amount));
					}
					su = new StatusUpdate(target);
					su.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
				}
			}

			if(target.isPlayer())
			{
				target.sendPacket(su);
			}

			if(handler != null)
			{
				handler.useSkill(activeChar, skill, targets);
			}
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}