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
package dwo.gameserver.model.actor;

import dwo.config.Config;
import dwo.gameserver.GameTimeController;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.ai.L2CharacterAI;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.knownlist.VehicleKnownList;
import dwo.gameserver.model.actor.stat.VehicleStat;
import dwo.gameserver.model.actor.templates.L2CharTemplate;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.VehiclePathPoint;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.util.Util;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author DS
 */

public abstract class L2Vehicle extends L2Character
{
	protected final FastList<L2PcInstance> _passengers = new FastList<>();
	protected int _dockId;
	protected Location _oustLoc;
	protected VehiclePathPoint[] _currentPath;
	protected int _runState;
	private Runnable _engine;

	protected L2Vehicle(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		setIsFlying(true);
	}

	public VehiclePathPoint[] getVehiclePathPoint()
	{
		return _currentPath;
	}

	public boolean isBoat()
	{
		return false;
	}

	public boolean isAirShip()
	{
		return false;
	}

	public boolean isShuttle()
	{
		return false;
	}

	public boolean canBeControlled()
	{
		return _engine == null;
	}

	public void registerEngine(Runnable r)
	{
		_engine = r;
	}

	public void runEngine(int delay)
	{
		if(_engine != null)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(_engine, delay);
		}
	}

	public void executePath(VehiclePathPoint[] path)
	{
		_runState = 0;
		_currentPath = path;

		if(_currentPath != null && _currentPath.length > 0)
		{
			VehiclePathPoint point = _currentPath[0];
			if(point.moveSpeed > 0)
			{
				getStat().setMoveSpeed(point.moveSpeed);
			}
			if(point.rotationSpeed > 0)
			{
				getStat().setRotationSpeed(point.rotationSpeed);
			}

			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(point.x, point.y, point.z, 0));
			return;
		}
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	public boolean isInDock()
	{
		return _dockId > 0;
	}

	public void setInDock(int d)
	{
		_dockId = d;
	}

	public int getDockId()
	{
		return _dockId;
	}

	public Location getOustLoc()
	{
		return _oustLoc != null ? _oustLoc : MapRegionManager.getInstance().getTeleToLocation(this, TeleportWhereType.TOWN);
	}

	public void setOustLoc(Location loc)
	{
		_oustLoc = loc;
	}

	public void oustPlayers()
	{
		L2PcInstance player;

		// Use iterator because oustPlayer will try to remove player from _passengers
		Iterator<L2PcInstance> iter = _passengers.iterator();
		while(iter.hasNext())
		{
			player = iter.next();
			iter.remove();
			if(player != null)
			{
				oustPlayer(player);
			}
		}
	}

	public void oustPlayer(L2PcInstance player)
	{
		player.setVehicle(null);
		player.setInVehiclePosition(null);
		removePassenger(player);
	}

	public boolean addPassenger(L2PcInstance player)
	{
		if(player == null || _passengers.contains(player))
		{
			return false;
		}

		// already in other vehicle
		if(player.getVehicle() != null && !player.getVehicle().equals(this))
		{
			return false;
		}

		_passengers.add(player);
		return true;
	}

	public void removePassenger(L2PcInstance player)
	{
		try
		{
			_passengers.remove(player);
		}
		catch(Exception e)
		{
		}
	}

	public boolean isEmpty()
	{
		return _passengers.isEmpty();
	}

	public List<L2PcInstance> getPassengers()
	{
		return _passengers;
	}

	public void broadcastToPassengers(L2GameServerPacket sm)
	{
		_passengers.stream().filter(player -> player != null).forEach(player -> player.sendPacket(sm));
	}

	/**
	 * Consume ticket(s) and teleport player from boat if no correct ticket
	 *
	 * @param itemId Ticket itemId
	 * @param count  Ticket count
	 * @param oustX
	 * @param oustY
	 * @param oustZ
	 */
	public void payForRide(int itemId, int count, int oustX, int oustY, int oustZ)
	{
		Collection<L2PcInstance> passengers = getKnownList().getKnownPlayersInRadius(1000);
		if(passengers != null && !passengers.isEmpty())
		{
			L2ItemInstance ticket;
			InventoryUpdate iu;
			for(L2PcInstance player : passengers)
			{
				if(player == null)
				{
					continue;
				}
				if(player.isInBoat() && player.getBoat().equals(this))
				{
					if(itemId > 0)
					{
						ticket = player.getInventory().getItemByItemId(itemId);
						if(ticket == null || player.getInventory().destroyItem(ProcessType.NPC, ticket, count, player, this) == null)
						{
							player.sendPacket(SystemMessageId.NOT_CORRECT_BOAT_TICKET);
							player.teleToLocation(oustX, oustY, oustZ, true);
							continue;
						}
						iu = new InventoryUpdate();
						iu.addModifiedItem(ticket);
						player.sendPacket(iu);
					}
					addPassenger(player);
				}
			}
		}
	}

	@Override
	public void teleToLocation(int x, int y, int z, int heading, boolean allowRandomOffset)
	{
		if(isMoving())
		{
			stopMove(null, false);
		}

		setIsTeleporting(true);

		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

		_passengers.stream().filter(player -> player != null).forEach(player -> player.teleToLocation(x, y, z));

		getLocationController().decay();
		setXYZ(x, y, z);

		// temporary fix for heading on teleports
		if(heading != 0)
		{
			getLocationController().setHeading(heading);
		}

		onTeleported();
		revalidateZone(true);
	}

	@Override
	public void setAI(L2CharacterAI newAI)
	{
		if(_ai == null)
		{
			_ai = newAI;
		}
	}

	@Override
	public VehicleStat getStat()
	{
		return (VehicleStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		setStat(new VehicleStat(this));
	}

	@Override
	public void updateAbnormalEffect()
	{
	}

	@Override
	public boolean updatePosition(int gameTicks)
	{
		boolean result = super.updatePosition(gameTicks);

		_passengers.stream().filter(player -> player != null && player.getVehicle().equals(this)).forEach(player -> {
			player.getLocationController().setXYZ(getX(), getY(), getZ());
			player.revalidateZone(false);
		});

		return result;
	}

	@Override
	public void stopMove(Location pos, boolean updateKnownObjects)
	{
		_move = null;
		if(pos != null)
		{
			setXYZ(pos.getX(), pos.getY(), pos.getZ());
			setHeading(pos.getHeading());
			revalidateZone(true);
		}

		if(Config.MOVE_BASED_KNOWNLIST && updateKnownObjects)
		{
			getKnownList().findObjects();
		}
	}

	@Override
	public boolean moveToNextRoutePoint()
	{
		_move = null;

		if(_currentPath != null)
		{
			_runState++;
			if(_runState < _currentPath.length)
			{
				VehiclePathPoint point = _currentPath[_runState];
				if(!isMovementDisabled())
				{
					if(point.moveSpeed == 0)
					{
						teleToLocation(point.x, point.y, point.z, point.rotationSpeed, false);
						_currentPath = null;
					}
					else
					{
						if(point.moveSpeed > 0)
						{
							getStat().setMoveSpeed(point.moveSpeed);
						}
						if(point.rotationSpeed > 0)
						{
							getStat().setRotationSpeed(point.rotationSpeed);
						}

						MoveData m = new MoveData();
						m.disregardingGeodata = false;
						m.onGeodataPathIndex = -1;
						m._xDestination = point.x;
						m._yDestination = point.y;
						m._zDestination = point.z;
						m._heading = 0;

						double dx = point.x - getX();
						double dy = point.y - getY();
						double distance = Math.sqrt(dx * dx + dy * dy);
						if(distance > 1) // vertical movement heading check
						{
							setHeading(Util.calculateHeadingFrom(getX(), getY(), point.x, point.y));
						}

						m._moveStartTime = GameTimeController.getInstance().getGameTicks();
						_move = m;

						GameTimeController.getInstance().registerMovingObject(this);
						return true;
					}
				}
			}
			else
			{
				_currentPath = null;
			}
		}

		runEngine(10);
		return false;
	}

	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public boolean onDelete()
	{
		_engine = null;

		try
		{
			if(isMoving())
			{
				stopMove(null);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Failed stopMove().", e);
		}

		try
		{
			oustPlayers();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Failed oustPlayers().", e);
		}

		L2WorldRegion oldRegion = getLocationController().getWorldRegion();

		try
		{
			getLocationController().decay();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Failed decayMe().", e);
		}

		if(oldRegion != null)
		{
			oldRegion.removeFromZones(this);
		}

		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Failed cleaning knownlist.", e);
		}

		// Remove L2Object object from _allObjects of L2World
		WorldManager.getInstance().removeObject(this);

		return super.onDelete();
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new VehicleKnownList(this));
	}

	@Override
	public int getLevel()
	{
		return 0;
	}

	@Override
	public boolean isWalker()
	{
		return true;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	public class AIAccessor extends L2Character.AIAccessor
	{
		@Override
		public void detachAI()
		{
		}
	}
}