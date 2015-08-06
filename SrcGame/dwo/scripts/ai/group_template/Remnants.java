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
package dwo.scripts.ai.group_template;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;

/**
 * @author DS
 */
public class Remnants extends Quest
{
	private static final int[] NPCS = {
		18463, 18464, 18465
	};

	private static final int HOLY_WATER = 2358;

	// TODO: Find retail strings.
	// private static final String MSG = "The holy water affects Remnants Ghost. You have freed his soul.";
	// private static final String MSG_DEREK = "The holy water affects Derek. You have freed his soul.";

	public Remnants()
	{
		addSpawnId(NPCS);
		addSkillSeeId(NPCS);
	}

	public static void main(String[] args)
	{
		new Remnants();
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(skill.getId() == HOLY_WATER)
		{
			if(!npc.isDead())
			{
				if(targets.length > 0 && targets[0].equals(npc))
				{
					if(npc.getCurrentHp() < npc.getMaxHp() * 0.02) // Lower, than 2%
					{
						npc.doDie(caster);
					}
				}
			}
		}

		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsMortal(false);
		return super.onSpawn(npc);
	}
}
