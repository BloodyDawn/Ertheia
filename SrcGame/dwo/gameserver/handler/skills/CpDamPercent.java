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
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.calculations.CancelAttack;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

public class CpDamPercent implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.CPDAMPERCENT
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(activeChar.isAlikeDead())
		{
			return;
		}

		boolean ss = activeChar.isSoulshotCharged(skill);
		boolean sps = activeChar.isSpiritshotCharged(skill);
		boolean bss = activeChar.isBlessedSpiritshotCharged(skill);

		for(L2Character target : (L2Character[]) targets)
		{
			if(activeChar instanceof L2PcInstance && target instanceof L2PcInstance && ((L2PcInstance) target).isFakeDeath())
			{
				target.stopFakeDeath(true);
			}
			else if(target.isDead() || target.isInvul())
			{
				continue;
			}

			byte shld = Shield.calcShldUse(activeChar, target, skill);

			int damage = (int) (target.getCurrentCp() * (skill.getPower() / 100));

			// Если на цели висит баф, ограничивающий урон от умений - режем этот урон
			int targetMaxSkillDamage = (int) target.getStat().calcStat(Stats.MAX_SKILL_DAMAGE, 0, null, null);
			if(targetMaxSkillDamage > 0 && damage > targetMaxSkillDamage)
			{
				damage = targetMaxSkillDamage;
			}

			// Manage attack or cast break of the target (calculating rate, sending message...)
			CancelAttack.calcAtkBreak(target, damage);

			skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
			activeChar.sendDamageMessage(target, damage, false, false, false);
			target.setCurrentCp(target.getCurrentCp() - damage);
		}
		activeChar.spsUncharge(skill);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}