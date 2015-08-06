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
package dwo.gameserver.instancemanager.vehicle;

import dwo.config.Config;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2BoatInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2CharTemplate;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.zone.VehiclePathPoint;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.util.arrays.L2TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;

public class BoatManager
{
	public static final int TALKING_ISLAND = 1;
	public static final int GLUDIN_HARBOR = 2;
	public static final int RUNE_HARBOR = 3;
	private L2TIntObjectHashMap<L2BoatInstance> _boats = new L2TIntObjectHashMap<>();
	private boolean[] _docksBusy = new boolean[3];

	private BoatManager()
	{
		for(int i = 0; i < _docksBusy.length; i++)
		{
			_docksBusy[i] = false;
		}
	}

	public static BoatManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public L2BoatInstance getNewBoat(int boatId, int x, int y, int z, int heading)
	{
		if(!Config.ALLOW_BOAT)
		{
			return null;
		}

		StatsSet npcDat = new StatsSet();
		npcDat.set("npcId", boatId);
		npcDat.set("level", 0);
		npcDat.set("jClass", "boat");

		npcDat.set("str", 0);
		npcDat.set("con", 0);
		npcDat.set("dex", 0);
		npcDat.set("int", 0);
		npcDat.set("wit", 0);
		npcDat.set("men", 0);

		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseAccCombat", 38);
		npcDat.set("baseEvasRate", 38);
		npcDat.set("base_critical", 38);

		// npcDat.set("name", "");
		npcDat.set("collision_radius", 0);
		npcDat.set("collision_height", 0);
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("base_attack_range", 0);
		npcDat.set("org_mp", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("exp", 0);
		npcDat.set("sp", 0);
		npcDat.set("base_physical_attack", 0);
		npcDat.set("base_magic_attack", 0);
		npcDat.set("base_attack_speed", 0);
		npcDat.set("agro_range", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("slot_rhand", 0);
		npcDat.set("slot_lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("ground_high", 0);
		npcDat.set("ground_low", 0);
		npcDat.set("org_hp", 50000);
		npcDat.set("org_hp_regen", 3.0e-3f);
		npcDat.set("org_mp_regen", 3.0e-3f);
		npcDat.set("base_defend", 100);
		npcDat.set("base_magic_defend", 100);
		L2CharTemplate template = new L2CharTemplate(npcDat);
		L2BoatInstance boat = new L2BoatInstance(IdFactory.getInstance().getNextId(), template);
		_boats.put(boat.getObjectId(), boat);
		boat.setHeading(heading);
		boat.setXYZ(x, y, z, false);
		boat.getLocationController().spawn();
		return boat;
	}

	/**
	 * @param boatId
	 * @return
	 */
	public L2BoatInstance getBoat(int boatId)
	{
		return _boats.get(boatId);
	}

	/**
	 * Lock/unlock dock so only one ship can be docked
	 *
	 * @param h     Dock Id
	 * @param value True if dock is locked
	 */
	public void dockShip(int h, boolean value)
	{
		try
		{
			_docksBusy[h] = value;
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
		}
	}

	/**
	 * Check if dock is busy
	 *
	 * @param h Dock Id
	 * @return Trye if dock is locked
	 */
	public boolean dockBusy(int h)
	{
		try
		{
			return _docksBusy[h];
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			return false;
		}
	}

	/**
	 * Broadcast one packet in both path points
	 */
	public void broadcastPacket(VehiclePathPoint point1, VehiclePathPoint point2)
	{
		WorldManager.getInstance().forEachPlayer(new ForEachPlayerBroadcastPackets(point1, point2));
	}

	/**
	 * Broadcast several packets in both path points
	 */
	public void broadcastPackets(VehiclePathPoint point1, VehiclePathPoint point2, L2GameServerPacket... packets)
	{
		WorldManager.getInstance().forEachPlayer(new ForEachPlayerBroadcastPackets(point1, point2, packets));
	}

	private static class SingletonHolder
	{
		protected static final BoatManager _instance = new BoatManager();
	}

	private class ForEachPlayerBroadcastPackets implements TObjectProcedure<L2PcInstance>
	{
		VehiclePathPoint _point1;
		VehiclePathPoint _point2;
		L2GameServerPacket[] _packets;

		private ForEachPlayerBroadcastPackets(VehiclePathPoint point1, VehiclePathPoint point2, L2GameServerPacket... packets)
		{
			_point1 = point1;
			_point2 = point2;
			_packets = packets;
		}

		@Override
		public boolean execute(L2PcInstance player)
		{
			if(player != null)
			{
				double dx = (double) player.getX() - _point1.x;
				double dy = (double) player.getY() - _point1.y;
				if(Math.sqrt(dx * dx + dy * dy) < Config.BOAT_BROADCAST_RADIUS)
				{
					for(L2GameServerPacket p : _packets)
					{
						player.sendPacket(p);
					}
				}
				else
				{
					dx = (double) player.getX() - _point2.x;
					dy = (double) player.getY() - _point2.y;
					if(Math.sqrt(dx * dx + dy * dy) < Config.BOAT_BROADCAST_RADIUS)
					{
						for(L2GameServerPacket p : _packets)
						{
							player.sendPacket(p);
						}
					}
				}
			}
			return true;
		}
	}
}