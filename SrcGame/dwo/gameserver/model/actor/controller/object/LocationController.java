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
package dwo.gameserver.model.actor.controller.object;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.geometry.Point3D;
import org.apache.log4j.Level;

/**
 * L2Object location controller.
 * Manages object position, heading, world region, spawn/decay, etc.
 *
 * @author L2J
 * @author Yorie
 */
public class LocationController extends L2ObjectController
{
	private boolean isVisible;
	private int heading;
	private Point3D position;
	/**
	 * Object localization: Used for items/templates that are seen in the world.
	 */
	private L2WorldRegion region;

	public LocationController(L2Object object)
	{
		super(object);
		setWorldRegion(WorldManager.getInstance().getRegion(getWorldPosition()));
	}

	public int getHeading()
	{
		return heading;
	}

	public void setHeading(int heading)
	{
		this.heading = heading;
	}

	/**
	 * @return The X coord of the L2Object.
	 */
	public int getX()
	{
		return getWorldPosition().getX();
	}

	/**
	 * Sets up X coord of current object.
	 */
	public void setX(int x)
	{
		getWorldPosition().setX(x);
	}

	/**
	 * @return The y position of the L2Object.
	 */
	public int getY()
	{
		return getWorldPosition().getY();
	}

	/**
	 * Sets up Y coord of current object.
	 */
	public void setY(int value)
	{
		getWorldPosition().setY(value);
	}

	/**
	 * @return the z position of the L2Object.
	 */
	public int getZ()
	{
		return getWorldPosition().getZ();
	}

	/**
	 * Sets up Z coord of current object.
	 */
	public void setZ(int value)
	{
		getWorldPosition().setZ(value);
	}

	/**
	 * @return Location wrapper for current object position.
	 */
	public Location getLoc()
	{
		return new Location(getX(), getY(), getZ(), heading);
	}

	/**
	 * Sets the (x, y, z) position of the L2Object and, if necessary, modify its world region.
	 */
	public void setXYZ(int x, int y, int z)
	{
		setWorldPosition(x, y, z);

		try
		{
			updateWorldRegion();
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, "Object Id at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").", e);
			onBadCoords();
		}
	}

	/**
	 * Sets up new world position for current object.
	 * @param loc Location wrapper containing new coords.
	 */
	public void setXYZ(Location loc)
	{
		setXYZ(loc.getX(), loc.getY(), loc.getZ());
	}

	/**
	 * Called on setXYZ exception.
	 */
	protected void onBadCoords()
	{
	}

	/**
	 * Sets the x,y,z position of the L2Object and if necessary modify its world region.
	 * Last parameter used to tell about object visibility.
	 * @param visibility True if object should become visible.
	 */
	public void setXYZ(int x, int y, int z, boolean visibility)
	{
		if(x > WorldManager.MAP_MAX_X)
		{
			x = WorldManager.MAP_MAX_X - 5000;
		}

		if(x < WorldManager.MAP_MIN_X)
		{
			x = WorldManager.MAP_MIN_X + 5000;
		}

		if(y > WorldManager.MAP_MAX_Y)
		{
			y = WorldManager.MAP_MAX_Y - 5000;
		}

		if(y < WorldManager.MAP_MIN_Y)
		{
			y = WorldManager.MAP_MIN_Y + 5000;
		}

		setVisible(visibility);
		setXYZ(x, y, z);
	}

	/**
	 * Checks if current object changed its region, if so, updates references.
	 */
	public void updateWorldRegion()
	{
		if(!isVisible)
		{
			return;
		}

		L2WorldRegion newRegion = WorldManager.getInstance().getRegion(getWorldPosition());
		L2WorldRegion oldRegion = region;
		if(!newRegion.equals(oldRegion))
		{
			if(oldRegion != null)
			{
				oldRegion.removeVisibleObject(object);
			}

			setWorldRegion(newRegion);

			// Add the L2Object spawn to _visibleObjects and if necessary to all players of its L2WorldRegion
			newRegion.addVisibleObject(object);
		}
	}

	/**
	 * @return Point 3D representing current object position.
	 */
	public Point3D getWorldPosition()
	{
		if(position == null)
		{
			position = new Point3D(0, 0, 0);
		}

		return position;
	}

	protected void setWorldPosition(Point3D newPosition)
	{
		setWorldPosition(newPosition.getX(), newPosition.getY(), newPosition.getZ());
	}

	protected void setWorldPosition(int x, int y, int z)
	{
		getWorldPosition().setXYZ(x, y, z);
	}

	public L2WorldRegion getWorldRegion()
	{
		return region;
	}

	protected void setWorldRegion(L2WorldRegion value)
	{
		region = value;
	}

	/**
	 * Remove a L2Object from the world.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the L2Object from the world</li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND ServerMode->Client packets to players</B></FONT><BR><BR>
	 * <p/>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> _worldRegion != null <I>(L2Object is visible at the beginning)</I></li><BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Delete NPC/PC or Unsummon</li><BR><BR>
	 */
	public void decay()
	{
		if(object.onDecay())
		{
			removeFromRegion();
		}
	}

	/**
	 * Does same thing as decay() method, but calls onDelete event
	 * meaning that object should be removed from world in force mode ignoring any circumstances.
	 */
	public void delete()
	{
		if(object.onDelete())
		{
			removeFromRegion();
		}
	}

	protected void removeFromRegion()
	{
		L2WorldRegion reg = region;

		synchronized(this)
		{
			setVisible(false);
			setWorldRegion(null);
		}

		// this can synchronize on others instances, so it's out of
		// synchronized, to avoid deadlocks
		// Remove the L2Object from the world
		WorldManager.getInstance().removeVisibleObject(object, reg);
		WorldManager.getInstance().removeObject(object);
	}

	/**
	 * Init the position of a L2Object spawn and add it in the world as a visible object.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the x,y,z position of the L2Object spawn and update its _worldregion </li>
	 * <li>Add the L2Object spawn in the _allobjects of L2World </li>
	 * <li>Add the L2Object spawn to _visibleObjects of its L2WorldRegion</li>
	 * <li>Add the L2Object spawn in the world as a <B>visible</B> object</li><BR><BR>
	 * <p/>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> _worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Create Door</li>
	 * <li> Spawn : Monster, Minion, CTs, Summon...</li><BR>
	 */
	public void spawn()
	{
		synchronized(this)
		{
			// Set the x,y,z position of the L2Object spawn and update its _worldregion
			setVisible(true);
			setWorldRegion(WorldManager.getInstance().getRegion(getWorldPosition()));

			// Add the L2Object spawn in the _allobjects of L2World
			WorldManager.getInstance().storeObject(object);

			// Add the L2Object spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
			region.addVisibleObject(object);
		}

		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		// Add the L2Object spawn in the world as a visible object
		WorldManager.getInstance().addVisibleObject(object, region);

		object.onSpawn();
	}

	public void spawn(int x, int y, int z)
	{
		synchronized(this)
		{
			// Set the x,y,z position of the L2Object spawn and update its _worldregion
			setVisible(true);

			if(x > WorldManager.MAP_MAX_X)
			{
				x = WorldManager.MAP_MAX_X - 5000;
			}

			if(x < WorldManager.MAP_MIN_X)
			{
				x = WorldManager.MAP_MIN_X + 5000;
			}

			if(y > WorldManager.MAP_MAX_Y)
			{
				y = WorldManager.MAP_MAX_Y - 5000;
			}

			if(y < WorldManager.MAP_MIN_Y)
			{
				y = WorldManager.MAP_MIN_Y + 5000;
			}

			setWorldPosition(x, y, z);
			setWorldRegion(WorldManager.getInstance().getRegion(getWorldPosition()));

			// Add the L2Object spawn in the _allobjects of L2World
			WorldManager.getInstance().storeObject(object);

			// these can synchronize on others instancies, so they're out of
			// synchronized, to avoid deadlocks

			// Add the L2Object spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
			region.addVisibleObject(object);
		}

		// Add the L2Object spawn in the world as a visible object
		WorldManager.getInstance().addVisibleObject(object, region);

		object.onSpawn();
	}

	/**
	 * @return True if current object is visible to other objects.
	 */
	public boolean isVisible()
	{
		return isVisible;
	}

	/**
	 * Sets up object visibility. If object should be hidden, this will automatically disappear for another objects.
	 * @param value Visibility value.
	 */
	public void setVisible(boolean value)
	{
		isVisible = value;
		if(!isVisible)
		{
			setWorldRegion(null);
		}
	}
}
