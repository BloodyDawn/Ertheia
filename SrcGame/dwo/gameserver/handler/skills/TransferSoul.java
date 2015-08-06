/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;

public class TransferSoul implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {L2SkillType.TRANSFER_SOUL};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(activeChar == null || activeChar.isAlikeDead())
		{
			return;
		}

		for(L2Object element : targets)
		{
			L2PcInstance target = (L2PcInstance) element;
			L2PcInstance caster = (L2PcInstance) activeChar;

			int casterSoul = 0;
			casterSoul = caster.getSouls();

			L2Skill soulmastery = SkillTable.getInstance().getInfo(L2Skill.SKILL_SOUL_MASTERY, caster.getSkillLevel(L2Skill.SKILL_SOUL_MASTERY));
			if(soulmastery != null)
			{
				if(casterSoul > 0)
				{
					L2Skill soulmasteryTarget = SkillTable.getInstance().getInfo(L2Skill.SKILL_SOUL_MASTERY, target.getSkillLevel(L2Skill.SKILL_SOUL_MASTERY));
					if(soulmasteryTarget != null)
					{
						target.increaseSouls(1);
						caster.decreaseSouls(1, skill);
					}
				}
			}
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
