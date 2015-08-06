package dwo.gameserver.util.arrays;

import dwo.gameserver.model.actor.L2Object;

import java.util.Iterator;

public class WorldObjectMap<T extends L2Object> extends L2ObjectMap<T>
{
	L2TIntObjectHashMap<T> _objectMap = new L2TIntObjectHashMap<>();

	@Override
	public int size()
	{
		return _objectMap.size();
	}

	@Override
	public boolean isEmpty()
	{
		return _objectMap.isEmpty();
	}

	@Override
	public void clear()
	{
		_objectMap.clear();
	}

	@Override
	public void put(T obj)
	{
		if(obj != null)
		{
			_objectMap.put(obj.getObjectId(), obj);
		}
	}

	@Override
	public void remove(T obj)
	{
		if(obj != null)
		{
			_objectMap.remove(obj.getObjectId());
		}
	}

	@Override
	public T get(int id)
	{
		return _objectMap.get(id);
	}

	@Override
	public boolean contains(T obj)
	{
		if(obj == null)
		{
			return false;
		}
		return _objectMap.get(obj.getObjectId()) != null;
	}

	@Override
	public Iterator<T> iterator()
	{
		return _objectMap.valueCollection().iterator();
	}
}
