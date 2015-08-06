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
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.calculations.BlowDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Effects;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.L2Effect;

import java.util.List;

/**
 * @author DS
 */
public class Cancel implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.CANCEL,
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(activeChar.isNpc())
		{
			((L2Npc) activeChar)._soulshotcharged = false;
			((L2Npc) activeChar)._spiritshotcharged = false;
		}

		L2Character target;
		L2Effect effect;
		for(L2Object obj : targets)
		{
			if(!(obj instanceof L2Character))
			{
				continue;
			}
			target = (L2Character) obj;

			if(target.isDead())
			{
				continue;
			}

			List<L2Effect> canceled = Effects.calcCancel(activeChar, target, skill, skill.getPower());
			for(L2Effect eff : canceled)
			{
				eff.exit();
			}
			// Possibility of a lethal strike
			BlowDamage.calcLethalHit(activeChar, target, skill);
		}

		// Applying self-effects
		if(skill.hasSelfEffects())
		{
			effect = activeChar.getFirstEffect(skill.getId());
			if(effect != null && effect.isSelfEffect())
			{
				// Replace old effect with new one.
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
		}

		activeChar.spsUncharge(skill);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}