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
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.world.quest.Quest;

/**
 * @author Ectis
 */
public class Sandstorms extends Quest
{
	private static final int SANDSTORM = 32350;

	public Sandstorms()
	{
		addAttackId(SANDSTORM);
	}

	public static void main(String[] args)
	{
		new Sandstorms();
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(npc.getNpcId() == SANDSTORM)
		{
			npc.setTarget(player);
			npc.doCast(new SkillHolder(5435, 1).getSkill());
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
}
