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
package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.MinionList;
import dwo.gameserver.util.Rnd;
import gnu.trove.set.hash.TIntHashSet;

/**
 * @author GKR
 */
public class Ranku extends Quest
{
	private static final int RANKU = 25542;
	private static final int MINION = 32305;
	private static final int MINION_2 = 25543;

	private static TIntHashSet myTrackingSet = new TIntHashSet();

	public Ranku()
	{

		addAttackId(RANKU);
		addKillId(RANKU);
		addKillId(MINION);
	}

	public static void main(String[] args)
	{
		new Ranku();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		if(npc.getNpcId() == RANKU)
		{
			((L2MonsterInstance) npc).getMinionList().getSpawnedMinions().stream().filter(minion -> minion != null && !minion.isDead() && !myTrackingSet.contains(minion.getObjectId())).forEach(minion -> {
				minion.broadcastPacket(new NS(minion.getObjectId(), ChatType.ALL, minion.getNpcId(), NpcStringId.DONT_KILL_ME_PLEASE_SOMETHINGS_STRANGLING_ME));
				startQuestTimer("checkup", 1000, npc, null);
				synchronized(myTrackingSet)
				{
					myTrackingSet.add(minion.getObjectId());
				}
			});
		}
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("checkup") && npc.getNpcId() == RANKU && !npc.isDead())
		{
			((L2MonsterInstance) npc).getMinionList().getSpawnedMinions().stream().filter(minion -> minion != null && !minion.isDead() && myTrackingSet.contains(minion.getObjectId())).forEach(minion -> {
				L2PcInstance[] players = minion.getKnownList().getKnownPlayers().values().toArray(new L2PcInstance[minion.getKnownList().getKnownPlayers().size()]);
				L2PcInstance killer = players[Rnd.get(players.length)];
				minion.reduceCurrentHp(minion.getMaxHp() / 100, killer, null);
			});
			startQuestTimer("checkup", 1000, npc, null);
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(npc.getNpcId() == MINION)
		{
			if(myTrackingSet.contains(npc.getObjectId()))
			{
				synchronized(myTrackingSet)
				{
					myTrackingSet.remove(npc.getObjectId());
				}
			}

			L2MonsterInstance master = ((L2MonsterInstance) npc).getLeader();
			if(master != null && !master.isDead())
			{
				L2MonsterInstance minion2 = MinionList.spawnMinion(master, MINION_2);
				minion2.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
			}
		}
		else if(npc.getNpcId() == RANKU)
		{
			((L2MonsterInstance) npc).getMinionList().getSpawnedMinions().stream().filter(minion -> myTrackingSet.contains(minion.getObjectId())).forEach(minion -> {
				synchronized(myTrackingSet)
				{
					myTrackingSet.remove(minion.getObjectId());
				}
			});
		}
		return super.onKill(npc, killer, isPet);
	}
}
