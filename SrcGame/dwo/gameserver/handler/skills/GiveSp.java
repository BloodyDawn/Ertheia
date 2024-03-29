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
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;

/**
 * @author Forsaiken
 */

public class GiveSp implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.GIVE_SP
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		for(L2Object obj : targets)
		{
			L2Character target = (L2Character) obj;
			if(target != null)
			{
				int spToAdd = (int) skill.getPower();
				target.addExpAndSp(0, spToAdd);
			}
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}