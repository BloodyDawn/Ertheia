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

import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.handler.ActionHandler;
import dwo.gameserver.handler.ActionShiftHandler;
import dwo.gameserver.handler.IActionHandler;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.controller.object.InstanceController;
import dwo.gameserver.model.actor.controller.object.LocationController;
import dwo.gameserver.model.actor.controller.object.PolyController;
import dwo.gameserver.model.actor.controller.object.RestrictionController;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.actor.knownlist.ObjectKnownList;
import dwo.gameserver.model.world.zone.Location;
import javolution.util.FastMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Mother class of all objects in the world which ones is it possible.
 *
 * @author L2J
 * @author Yorie
 */
public abstract class L2Object
{
	private static final Map<String, Class<? extends L2Object>> gameObjectTypes = new FastMap<>();
	protected LocationController _locationController;
	protected RestrictionController _restrictionController;
	protected InstanceController _instanceController;
	private ObjectKnownList _knownList;
	private String _name;
	private int _objectId;
	private PolyController _polyController;

	protected L2Object(int objectId)
	{
		_objectId = objectId;
		initKnownList();
	}

	/**
	 * @return List of all subtypes of L2Object.
	 * @see L2Object#getGameObjectTypeByName(String)
	 */
	public static Map<String, Class<? extends L2Object>> listGameObjectTypes()
	{
		return gameObjectTypes;
	}

	/**
	 * Fetches generic information about subclasses of L2Object.
	 * The subclasses are game objects, so, this feature can be used for determining available types of game objects.
	 * The basic usage is, for example, in data pack when setting up some abstract type, constraints and etc.
	 *
	 * @param typeName Game object type name.
	 * @return Class extensible from L2Object. If not found will return null.
	 */
	@Nullable
	public static Class<? extends L2Object> getGameObjectTypeByName(String typeName)
	{
		String actorPackage = "dwo.gameserver.model.actor.";
		String actorInstancePackage = "dwo.gameserver.model.actor.instance.";

		if(!gameObjectTypes.containsKey(typeName))
		{
			try
			{
				gameObjectTypes.put(typeName, Class.forName(actorPackage + typeName).asSubclass(L2Object.class));
			}
			catch(Exception ignored)
			{
				try
				{
					gameObjectTypes.put(typeName, Class.forName(actorInstancePackage + typeName).asSubclass(L2Object.class));
				}
				catch(Exception ignore)
				{
				}
			}
		}

		return gameObjectTypes.get(typeName);
	}

	/**
	 * Checks if current object is type of @type.
	 * This feature should be used in concepts of OOP when trying to know, what kind of type this object is.
	 *
	 * For example, when some class field or method parameter is of type L2Object, then you can't be sure that it's, for example, L2PcInstance.
	 * Then you can check it by calling this method in such way:
	 *      if (object.is(L2PcInstance.class)) {}
	 *
	 * Yeah, this is analog of such code:
	 *      if (object instanceof L2PcInstance) {}
	 * but be sure - using of instanceof everywhere is not very good practice.
	 * Moreover, old instance type was removed and replaced by this code.
	 *
	 * @param type Type to check for.
	 * @return True if this object is instance of @type.
	 */
	public boolean is(Class<? extends L2Object> type)
	{
		return type.isInstance(this);
	}

	public void onAction(L2PcInstance player)
	{
		onAction(player, true);
	}

	public void onAction(L2PcInstance player, boolean interact)
	{
		IActionHandler handler = ActionHandler.getInstance().getHandler(getClass());
		if(handler != null)
		{
			handler.action(player, this, interact);
		}

		player.sendActionFailed();
	}

	public void onActionShift(L2PcInstance player)
	{
		IActionHandler handler = ActionShiftHandler.getInstance().getHandler(getClass());
		if(handler != null)
		{
			handler.action(player, this, true);
		}

		player.sendActionFailed();
	}

	public void onForcedAttack(L2PcInstance player)
	{
		player.sendActionFailed();
	}

	/**
	 * Called when current object being spawned.
	 */
	public void onSpawn()
	{
	}

	/**
	 * Called when object becomes invisible for other objects.
	 */
	public boolean onDecay()
	{
		return true;
	}

	/**
	 * Called when object deleted from world.
	 */
	public boolean onDelete()
	{
		return true;
	}

	/**
	 * Placeholder method for setting up object coords.
	 * @param x X coord.
	 * @param y Y coord.
	 * @param z Z coord.
	 */
	public void setXYZ(int x, int y, int z)
	{
		getLocationController().setXYZ(x, y, z);
	}

	/**
	 * Placeholder method for setting up object coords and visibility.
	 * @param x X coord.
	 * @param y Y coord.
	 * @param z Z coord.
	 * @param visibility Is object will being visible or not.
	 */
	public void setXYZ(int x, int y, int z, boolean visibility)
	{
		getLocationController().setXYZ(x, y, z, visibility);
	}

	/**
	 * Placeholder method for setting up object coords and visibility.
	 * @param loc Location wrapper representing coords.
	 * @param visibility Is object will being visible or not.
	 */
	public void setXYZ(Location loc, boolean visibility)
	{
		getLocationController().setXYZ(loc.getX(), loc.getY(), loc.getZ(), visibility);
	}

	/**
	 * @return X coord of current object position.
	 */
	public int getX()
	{
		return getLocationController().getX();
	}

	/**
	 * @return Y coord of current object position.
	 */
	public int getY()
	{
		return getLocationController().getY();
	}

	/**
	 * @return Z coord of current object position.
	 */
	public int getZ()
	{
		return getLocationController().getZ();
	}

	/**
	 * @return Location wrapper for current object position.
	 */
	public Location getLoc()
	{
		return getLocationController().getLoc();
	}

	/**
	 * Placeholder method for usability of often used method.
	 * Returns ID of instance where object is.
	 * @see @InstanceController
	 */
	public int getInstanceId()
	{
		return getInstanceController().getInstanceId();
	}

	/**
	 * Placeholder method of location controller.
	 * To set up visibility, use getLocationController().setVisible() method - there's no placeholder for this action.
	 * @return True if object is visible.
	 */
	public boolean isVisible()
	{
		return getLocationController().isVisible();
	}

	/**
	 * Renews object ID.
	 */
	public void refreshID()
	{
		WorldManager.getInstance().removeObject(this);
		IdFactory.getInstance().releaseId(_objectId);
		_objectId = IdFactory.getInstance().getNextId();
	}

	public abstract boolean isAutoAttackable(L2Character attacker);

	/**
	 * @return List of known objects that are probably in vision radius or comes to some circumstances.
	 */
	public ObjectKnownList getKnownList()
	{
		return _knownList;
	}

	protected void setKnownList(ObjectKnownList value)
	{
		_knownList = value;
	}

	/**
	 * Initializes the KnownList of the L2Object,
	 * is overwritten in classes that require a different knownlist Type.
	 * <p/>
	 * Removes the need for instanceof checks.
	 */
	protected void initKnownList()
	{
		_knownList = new ObjectKnownList(this);
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String value)
	{
		_name = value;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	/**
	 * Polymorphic controller allows object types polymorphism (for example, player can be NPC).
	 * @return Polymorphic controller.
	 */
	public PolyController getPolyController()
	{
		if(_polyController == null)
		{
			_polyController = new PolyController(this);
		}

		return _polyController;
	}

	/**
	 * Casts this object to player instance if possible.
	 * Should be always called with isPlayer() method.
	 * In another ways of usage NP exception may be produced.
	 *
	 * @return L2PcInstance of this object.
	 */
	public L2PcInstance getActingPlayer()
	{
		return null;
	}

	/**
	 * Sends the ServerMode->Client info packet for the object.
	 */
	public void sendInfo(L2PcInstance activeChar)
	{

	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ':' + getName() + '[' + _objectId + ']';
	}

	/**
	 * @return True if this object is instance of {@link L2Character}.
	 */
	public boolean isCharacter()
	{
		return false;
	}

	/**
	 * @return L2Character instance of this object.
	 */
	public L2Character getCharacter()
	{
		return null;
	}

	/**
	 * @return True if current object can be attacked.
	 */
	public boolean isAttackable()
	{
		return false;
	}

	/**
	 * @return {@code true} if object is instance of {@link L2Attackable}.
	 */
	public boolean isL2Attackable()
	{
		return false;
	}

	/**
	 * @return L2Attackable instance of this object.
	 */
	@Nullable
	public L2Attackable getAttackable()
	{
		return null;
	}

	/**
	 * @return {@code true} if object is instance of {@link L2PcInstance}
	 */
	public boolean isPlayer()
	{
		return false;
	}

	/**
	 * @return {@code true} if object is instance of {@link L2Playable}
	 */
	public boolean isPlayable()
	{
		return false;
	}

	/**
	 * @return {@code true} if object is instance of {@link L2Summon}
	 */
	public boolean isSummon()
	{
		return false;
	}

	/**
	 * @return L2Summon instance of this object.
	 */
	@Nullable
	public L2Summon getSummonInstance()
	{
		return null;
	}

	/**
	 * @return True if this object is instance of {@link L2PetInstance}.
	 */
	public boolean isPet()
	{
		return false;
	}

	/**
	 * @return L2Pet instance of this object.
	 */
	@Nullable
	public L2PetInstance getPetInstance()
	{
		return null;
	}

	/**
	 * @return {@code true} if object is instance of {@link L2Npc}
	 */
	public boolean isNpc()
	{
		return false;
	}

	/**
	 * @return L2Npc instance of this object.
	 */
	@Nullable
	public L2Npc getNpcInstance()
	{
		return null;
	}

	/**
	 * @return {@code true} if object is instance of {@link L2DoorInstance}
	 */
	public boolean isDoor()
	{
		return false;
	}

	/**
	 * @return {@code true} if object is instance of {@link L2MonsterInstance}
	 */
	public boolean isMonster()
	{
		return false;
	}

	/**
	 * @return Restriction controller.
	 */
	public RestrictionController getRestrictionController()
	{
		if(_restrictionController == null)
		{
			_restrictionController = new RestrictionController(this);
		}

		return _restrictionController;
	}

	/**
	 * @return Location controller.
	 */
	public LocationController getLocationController()
	{
		if(_locationController == null)
		{
			_locationController = new LocationController(this);
		}

		return _locationController;
	}

	/**
	 * @return Instance controller.
	 */
	public InstanceController getInstanceController()
	{
		if(_instanceController == null)
		{
			_instanceController = new InstanceController(this);
		}

		return _instanceController;
	}
}