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
package dwo.scripts.ai.individual;

import dwo.gameserver.instancemanager.HellboundManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.world.quest.Quest;

/**
 * Manages Naia's cast on the Hellbound Core
 * @author GKR
 */
public class HellboundCore extends Quest
{
	private static final int NAIA = 18484;
	private static final int HELLBOUND_CORE = 32331;

	private static SkillHolder BEAM = new SkillHolder(5493, 1);

	public HellboundCore(int id, String name, String descr)
	{
		super(id, name, descr);

		addSpawnId(HELLBOUND_CORE);
	}

	public static void main(String[] args)
	{
		new HellboundCore(-1, "HellboundCore", "ai");
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("cast") && HellboundManager.getInstance().getLevel() <= 6)
		{
			npc.getKnownList().getKnownCharactersInRadius(900).stream().filter(naia -> naia != null && naia instanceof L2MonsterInstance && ((L2MonsterInstance) naia).getNpcId() == NAIA && !naia.isDead()).forEach(naia -> {
				naia.setTarget(npc);
				naia.doSimultaneousCast(BEAM.getSkill());
			});
			startQuestTimer("cast", 10000, npc, null);
		}
		return null;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		startQuestTimer("cast", 10000, npc, null);
		return super.onSpawn(npc);
	}
}
