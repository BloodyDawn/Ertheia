package dwo.gameserver.model.actor.knownlist;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import dwo.gameserver.util.Util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectKnownList
{
	private final L2Object _activeObject;
	private volatile Map<Integer, L2Object> _knownObjects;

	public ObjectKnownList(L2Object activeObject)
	{
		_activeObject = activeObject;
	}

	public boolean addKnownObject(L2Object object)
	{
		if(object == null)
		{
			return false;
		}

		// Instance -1 is for GMs that can see everything on all instances
		if(getActiveObject().getInstanceId() != -1 && object.getInstanceId() != getActiveObject().getInstanceId())
		{
			return false;
		}

		// Check if the object is an L2PcInstance in ghost mode
		if(object.isPlayer() && object.getActingPlayer().getAppearance().isGhost())
		{
			return false;
		}

		// Check if already know object
		if(knowsObject(object))
		{
			return false;
		}

		// Check if object is not inside distance to watch object
		if(!Util.checkIfInShortRadius(getDistanceToWatchObject(object), getActiveObject(), object, true))
		{
			return false;
		}

		return getKnownObjects().put(object.getObjectId(), object) == null;
	}

	public boolean knowsObject(L2Object object)
	{
		if(object == null)
		{
			return false;
		}

		return getActiveObject().equals(object) || getKnownObjects().containsKey(object.getObjectId());
	}

	/**
	 * Remove all L2Object from _knownObjects
	 */
	public void removeAllKnownObjects()
	{
		getKnownObjects().clear();
	}

	public boolean removeKnownObject(L2Object object)
	{
		return removeKnownObject(object, false);
	}

	protected boolean removeKnownObject(L2Object object, boolean forget)
	{
		if(object == null)
		{
			return false;
		}

		if(forget) // on forget objects removed from list by iterator
		{
			return true;
		}

		return getKnownObjects().remove(object.getObjectId()) != null;
	}

	// used only in Config.MOVE_BASED_KNOWNLIST and does not support guards seeing
	// moving monsters
	public void findObjects()
	{
		L2WorldRegion region = getActiveObject().getLocationController().getWorldRegion();
		if(region == null)
		{
			return;
		}

		if(getActiveObject().isPlayable())
		{
			for(L2WorldRegion regi : region.getSurroundingRegions()) // offer members of this and surrounding regions
			{
				regi.getVisibleObjects().values().stream().filter(_object -> !_object.equals(getActiveObject())).forEach(_object -> {
					addKnownObject(_object);
					if(_object instanceof L2Character)
					{
						_object.getKnownList().addKnownObject(getActiveObject());
					}
				});
			}
		}
		else if(getActiveObject() instanceof L2Character)
		{
			region.getSurroundingRegions().stream().filter(L2WorldRegion::isActive).forEach(regi -> regi.getVisiblePlayable().values().stream().filter(_object -> !_object.equals(getActiveObject())).forEach(this::addKnownObject));
		}
	}

	// Remove invisible and too far L2Object from _knowObject and if necessary from _knownPlayers of the L2Character

	public void forgetObjects(boolean fullCheck)
	{
		// Go through knownObjects
		Collection<L2Object> objs = getKnownObjects().values();
		Iterator<L2Object> oIter = objs.iterator();
		L2Object object;
		while(oIter.hasNext())
		{
			object = oIter.next();
			if(object == null)
			{
				oIter.remove();
				continue;
			}

			if(!fullCheck && !object.isPlayable())
			{
				continue;
			}

			// Remove all objects invisible or too far
			if(!object.isVisible() || !Util.checkIfInShortRadius(getDistanceToForgetObject(object), getActiveObject(), object, true))
			{
				oIter.remove();
				removeKnownObject(object, true);
			}
		}
	}

	public L2Object getActiveObject()
	{
		return _activeObject;
	}

	public int getDistanceToForgetObject(L2Object object)
	{
		return 0;
	}

	public int getDistanceToWatchObject(L2Object object)
	{
		return 0;
	}

	/**
	 * @return the _knownObjects containing all L2Object known by the L2Character.
	 */
	public final Map<Integer, L2Object> getKnownObjects()
	{
		if (_knownObjects == null)
		{
			synchronized (this)
			{
				if (_knownObjects == null)
				{
					_knownObjects = new ConcurrentHashMap<>();
				}
			}
		}
		return _knownObjects;
	}
}
