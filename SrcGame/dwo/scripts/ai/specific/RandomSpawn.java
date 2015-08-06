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
package dwo.scripts.ai.specific;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * This class manages the spawn of NPC's, having several random spawn points.
 * @author GKR
 */
public class RandomSpawn extends Quest
{
	private static final TIntObjectHashMap<int[][]> SPAWN_POINTS = new TIntObjectHashMap<>();

	static
	{
		// Keltas
		SPAWN_POINTS.put(22341, new int[][]{
			{
				-27136, 250938, -3523
			}, {
			-29658, 252897, -3523
		}, {
			-27237, 251943, -3527
		}, {
			-28868, 250113, -3479
		}
		});
		// Keymaster
		SPAWN_POINTS.put(22361, new int[][]{
			{
				14091, 250533, -1940
			}, {
			15762, 252440, -2015
		}, {
			19836, 256212, -2090
		}, {
			21940, 254107, -2010
		}, {
			17299, 252943, -2015
		}
		});
		// Typhoon
		SPAWN_POINTS.put(25539, new int[][]{
			{
				-20641, 255370, -3235
			}, {
			-16157, 250993, -3058
		}, {
			-18269, 250721, -3151
		}, {
			-16532, 254864, -3223
		}, {
			-19055, 253489, -3440
		}, {
			-9684, 254256, -3148
		}, {
			-6209, 251924, -3189
		}, {
			-10547, 251359, -2929
		}, {
			-7254, 254997, -3261
		}, {
			-4883, 253171, -3322
		}
		});
		// Mutated Elpy
		SPAWN_POINTS.put(25604, new int[][]{
			{
				-46080, 246368, -14183
			}, {
			-44816, 246368, -14183
		}, {
			-44224, 247440, -14184
		}, {
			-44896, 248464, -14183
		}, {
			-46064, 248544, -14183
		}, {
			-46720, 247424, -14183
		}
		});
	}

	public RandomSpawn()
	{

		for(int npcId : SPAWN_POINTS.keys())
		{
			addSpawnId(npcId);
		}
	}

	public static void main(String[] args)
	{
		new RandomSpawn();
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(!npc.isTeleporting())
		{
			int[][] spawnlist = SPAWN_POINTS.get(npc.getNpcId());
			int[] spawn = spawnlist[Rnd.get(spawnlist.length)];
			if(!npc.isInsideRadius(spawn[0], spawn[1], spawn[2], 200, false, false))
			{
				npc.getSpawn().setLocx(spawn[0]);
				npc.getSpawn().setLocy(spawn[1]);
				npc.getSpawn().setLocz(spawn[2]);
				ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(npc, spawn), 100);
			}
		}

		return super.onSpawn(npc);
	}

	private static class Teleport implements Runnable
	{
		private final L2Npc _npc;
		private final int[] _coords;

		public Teleport(L2Npc npc, int[] coords)
		{
			_npc = npc;
			_coords = coords;
		}

		@Override
		public void run()
		{
			_npc.teleToLocation(_coords[0], _coords[1], _coords[2]);
		}
	}
}
