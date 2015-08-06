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
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.calculations.PhysicalDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.fort.Fort;

/**
 * @author _tomciaaa_
 */
public class StrSiegeAssault implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.STRSIEGEASSAULT
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{

		if(!(activeChar instanceof L2PcInstance))
		{
			return;
		}

		L2PcInstance player = (L2PcInstance) activeChar;

		if(!player.isRidingStrider())
		{
			return;
		}
		if(!(player.getTarget() instanceof L2DoorInstance))
		{
			return;
		}

		Castle castle = CastleManager.getInstance().getCastle(player);
		Fort fort = FortManager.getInstance().getFort(player);

		if(castle == null && fort == null)
		{
			return;
		}

		if(castle != null)
		{
			if(!player.checkIfOkToUseStriderSiegeAssault(castle))
			{
				return;
			}
		}
		else
		{
			if(!player.checkIfOkToUseStriderSiegeAssault(fort))
			{
				return;
			}
		}

		try
		{
			// damage calculation
			int damage = 0;
			boolean soul = activeChar.isSoulshotCharged(skill);

			for(L2Character target : (L2Character[]) targets)
			{
				if(activeChar instanceof L2PcInstance && target instanceof L2PcInstance && ((L2PcInstance) target).isFakeDeath())
				{
					target.stopFakeDeath(true);
				}
				else if(target.isDead())
				{
					continue;
				}

				boolean dual = activeChar.isUsingDualWeapon();
				byte shld = Shield.calcShldUse(activeChar, target, skill);
				boolean crit = PhysicalDamage.calcCrit(activeChar.getCriticalHit(target, skill), true, target);

				if(!crit && (skill.getCondition() & L2Skill.COND_CRIT) != 0)
				{
					damage = 0;
				}
				else
				{
					damage = skill.isStaticDamage() ? (int) skill.getPower() : (int) PhysicalDamage.calcPhysDam(activeChar, target, skill, shld, crit, dual, soul);
				}

				if(damage > 0)
				{
					target.reduceCurrentHp(damage, activeChar, skill);
					activeChar.sendDamageMessage(target, damage, false, false, false);

				}
				else
				{
					activeChar.sendMessage(skill.getName() + " failed.");
				}
			}
			activeChar.ssUncharge(skill);
		}
		catch(Exception e)
		{
			player.sendMessage("Error using siege assault:" + e);
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
