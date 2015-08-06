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
package dwo.scripts.hellbound;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.instancemanager.HellboundManager;
import dwo.gameserver.instancemanager.WalkingManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import javolution.util.FastMap;

import java.util.Map;

/**
 * @author GKR
 */
public class AnomicFoundry extends Quest
{
	private static final int LESSER_EVIL = 22398;
	private static final int GREATER_EVIL = 22399;
	// npcId, x, y, z, heading, max count
	private static int[][] SPAWNS = {
		{
			LESSER_EVIL, 27883, 248613, -3209, -13248, 5
		}, {
		LESSER_EVIL, 26142, 246442, -3216, 7064, 5
	}, {
		LESSER_EVIL, 27335, 246217, -3668, -7992, 5
	}, {
		LESSER_EVIL, 28486, 245913, -3698, 0, 10
	}, {
		GREATER_EVIL, 28684, 244118, -3700, -22560, 10
	}
	};
	private static final int respawnMin = 20000;
	private static final int respawnMax = 300000;
	private static int LABORER = 22396;
	private static int FOREMAN = 22397;
	private final int[] _spawned = {
		0, 0, 0, 0, 0
	};
	private final Map<Integer, Integer> _atkIndex = new FastMap<>();
	private int respawnTime = 60000;

	public AnomicFoundry()
	{

		addAggroRangeEnterId(LABORER);
		addAttackId(LABORER);
		addKillId(LABORER);
		addKillId(LESSER_EVIL);
		addKillId(GREATER_EVIL);
		addSpawnId(LABORER);
		addSpawnId(LESSER_EVIL);
		addSpawnId(GREATER_EVIL);

		startQuestTimer("make_spawn_1", respawnTime, null, null);
	}

	private static int getSpawnGroup(L2Npc npc)
	{
		int coordX = npc.getSpawn().getLocx();
		int coordY = npc.getSpawn().getLocy();
		int npcId = npc.getNpcId();

		for(int i = 0; i < 5; i++)
		{
			if(SPAWNS[i][0] == npcId && SPAWNS[i][1] == coordX && SPAWNS[i][2] == coordY)
			{
				return i;
			}
		}
		return -1;
	}

	private static int getRoute(L2Npc npc)
	{
		int ret = getSpawnGroup(npc);

		return ret >= 0 ? ret + 6 : -1;
	}

	private static void requestHelp(L2Npc requester, L2PcInstance agressor, int range, int helperId)
	{
		for(L2Spawn spawn : SpawnTable.getInstance().getSpawns(helperId))
		{
			L2MonsterInstance monster = (L2MonsterInstance) spawn.getLastSpawn();
			if(monster != null && agressor != null && !monster.isDead() && monster.isInsideRadius(requester, range, true, false) && !agressor.isDead())
			{
				monster.addDamageHate(agressor, 0, 1000);
			}
		}
	}

	public static void main(String[] args)
	{
		new AnomicFoundry();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		int atkIndex = _atkIndex.containsKey(npc.getObjectId()) ? _atkIndex.get(npc.getObjectId()) : 0;
		if(atkIndex == 0)
		{
			npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), NpcStringId.ENEMY_INVASION_HURRY_UP));
			cancelQuestTimer("return_laborer", npc, null);
			startQuestTimer("return_laborer", 60000, npc, null);

			if(respawnTime > respawnMin)
			{
				respawnTime -= 5000;
			}
			else if(respawnTime <= respawnMin && getQuestTimer("reset_respawn_time", null, null) == null)
			{
				startQuestTimer("reset_respawn_time", 600000, null, null);
			}
		}

		if(Rnd.get(10000) < 2000)
		{
			atkIndex++;
			_atkIndex.put(npc.getObjectId(), atkIndex);
			requestHelp(npc, attacker, 1000 * atkIndex, FOREMAN);
			requestHelp(npc, attacker, 1000 * atkIndex, LESSER_EVIL);
			requestHelp(npc, attacker, 1000 * atkIndex, GREATER_EVIL);

			if(Rnd.get(10) < 1)
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(npc.getX() + Rnd.get(-800, 800), npc.getY() + Rnd.get(-800, 800), npc.getZ(), npc.getHeading()));
			}
		}

		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("make_spawn_1"))
		{
			if(HellboundManager.getInstance().getLevel() >= 10)
			{
				int idx = Rnd.get(3);
				if(_spawned[idx] < SPAWNS[idx][5])
				{
					addSpawn(SPAWNS[idx][0], SPAWNS[idx][1], SPAWNS[idx][2], SPAWNS[idx][3], SPAWNS[idx][4], false, 0, false);
					respawnTime += 10000;
				}
				startQuestTimer("make_spawn_1", respawnTime, null, null);
			}
		}
		else if(event.equalsIgnoreCase("make_spawn_2"))
		{
			if(_spawned[4] < SPAWNS[4][5])
			{
				addSpawn(SPAWNS[4][0], SPAWNS[4][1], SPAWNS[4][2], SPAWNS[4][3], SPAWNS[4][4], false, 0, false);
			}
		}
		else if(event.equalsIgnoreCase("return_laborer"))
		{
			if(npc != null && !npc.isDead())
			{
				((L2Attackable) npc).returnHome();
			}
		}
		else if(event.equalsIgnoreCase("reset_respawn_time"))
		{
			respawnTime = 60000;
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(getSpawnGroup(npc) >= 0)
		{
			_spawned[getSpawnGroup(npc)]--;
			SpawnTable.getInstance().deleteSpawn(npc.getSpawn());
		}
		else if(npc.getNpcId() == LABORER)
		{
			if(Rnd.get(10000) < 8000)
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), NpcStringId.PROCESS_SHOULDNT_BE_DELAYED_BECAUSE_OF_ME));
				if(respawnTime < respawnMax)
				{
					respawnTime += 10000;
				}
				else if(respawnTime >= respawnMax && getQuestTimer("reset_respawn_time", null, null) == null)
				{
					startQuestTimer("reset_respawn_time", 600000, null, null);
				}
			}
			_atkIndex.remove(npc.getObjectId());
		}

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(!npc.isTeleporting())
		{
			SpawnTable.getInstance().addNewSpawn(npc.getSpawn());
			if(getSpawnGroup(npc) >= 0)
			{
				_spawned[getSpawnGroup(npc)]++;
			}

			if(npc.getNpcId() == LABORER)
			{
				npc.setIsNoRndWalk(true);
			}
		}

		if(getSpawnGroup(npc) >= 0 && getSpawnGroup(npc) <= 2)
		{
			if(npc.isTeleporting())
			{
				_spawned[getSpawnGroup(npc)]--;
				SpawnTable.getInstance().deleteSpawn(npc.getSpawn());
				npc.scheduleDespawn(100);
				if(_spawned[3] < SPAWNS[3][5])
				{
					addSpawn(SPAWNS[3][0], SPAWNS[3][1], SPAWNS[3][2], SPAWNS[3][3], SPAWNS[3][4], false, 0, false);
				}
			}
			else
			{
				WalkingManager.getInstance().startMoving(npc, getRoute(npc));
			}
		}

		else if(getSpawnGroup(npc) == 3)
		{
			if(npc.isTeleporting())
			{
				// Announcements.getInstance().announceToAll("Greater spawn is added");
				startQuestTimer("make_spawn_2", respawnTime << 1, null, null);
				_spawned[3]--;
				SpawnTable.getInstance().deleteSpawn(npc.getSpawn());
				npc.scheduleDespawn(100);
			}
			else
			{
				WalkingManager.getInstance().startMoving(npc, getRoute(npc));
			}
		}

		else if(getSpawnGroup(npc) == 4 && !npc.isTeleporting())
		{
			WalkingManager.getInstance().startMoving(npc, getRoute(npc));
		}

		return super.onSpawn(npc);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(Rnd.get(10000) < 2000)
		{
			requestHelp(npc, player, 500, FOREMAN);
			requestHelp(npc, player, 500, LESSER_EVIL);
			requestHelp(npc, player, 500, GREATER_EVIL);
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
}
