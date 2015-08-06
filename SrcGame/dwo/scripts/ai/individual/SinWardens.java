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

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * Manages Sin Wardens disappearing and chat
 * @author GKR
 */
public class SinWardens extends Quest
{
	private static final int[] SIN_WARDEN_MINIONS = {
		22424, 22425, 22426, 22427, 22428, 22429, 22430, 22432, 22433, 22434, 22435, 22436, 22437, 22438
	};

	private final TIntIntHashMap killedMinionsCount = new TIntIntHashMap();

	public SinWardens()
	{
		addKillId(SIN_WARDEN_MINIONS);
	}

	public static void main(String[] args)
	{
		new SinWardens();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(npc.isMinion())
		{
			L2MonsterInstance master = ((L2MonsterInstance) npc).getLeader();
			if(master != null && !master.isDead())
			{
				int killedCount = killedMinionsCount.containsKey(master.getObjectId()) ? killedMinionsCount.get(master.getObjectId()) : 0;
				killedCount++;

				if(killedCount == 5)
				{
					master.broadcastPacket(new NS(master.getObjectId(), ChatType.ALL, master.getNpcId(), NpcStringId.WE_MIGHT_NEED_NEW_SLAVES_ILL_BE_BACK_SOON_SO_WAIT));
					master.doDie(killer);
					killedMinionsCount.remove(master.getObjectId());
				}
				else
				{
					killedMinionsCount.put(master.getObjectId(), killedCount);
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}
}
