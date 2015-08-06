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
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 * @author nBd
 */

public class Soul implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.CHARGESOUL
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!activeChar.isPlayer() || activeChar.isAlikeDead())
		{
			return;
		}

		L2PcInstance player = activeChar.getActingPlayer();

		int soulMastery = (int) player.getStat().calcStat(Stats.SOUL_MASTERY, 0, null, null);
		if(soulMastery > 0)
		{
			if(player.getSouls() < soulMastery)
			{
				int count;

				count = player.getSouls() + skill.getNumSouls() <= soulMastery ? skill.getNumSouls() : soulMastery - player.getSouls();

				player.increaseSouls(count);
			}
			else
			{
				player.sendPacket(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
			}
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}