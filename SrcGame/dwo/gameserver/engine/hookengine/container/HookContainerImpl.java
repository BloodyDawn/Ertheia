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
package dwo.gameserver.engine.hookengine.container;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.engine.hookengine.IHook;
import dwo.gameserver.engine.hookengine.IHookContainer;
import dwo.gameserver.engine.hookengine.OrderedHook;
import dwo.gameserver.engine.hookengine.OrderedIterator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.inc.incolution.util.list.IncArrayList;

import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Didl
 * Container that stores {@link IHook} by event types, one event type can have 0..* {@link IHook}s,
 * yet two {@link IHook} by same getName() cannot be registered for same {@link HookType}
 */

public class HookContainerImpl implements IHookContainer
{
	protected static final Logger _log = LogManager.getLogger(HookContainerImpl.class);
	protected final ReentrantLock _lock;
	protected final IncArrayList<OrderedHook>[] _scriptEvents;

	public HookContainerImpl()
	{
		_lock = new ReentrantLock();
		_scriptEvents = new IncArrayList[HookType.values().length];
	}

	/**
	 * Adds a new {@link IHook} to this container. Not that there cannot be multiple hooks registered 
	 * for same HookType with same getName()
	 * @param eventType desired HookType
	 * @param script Hook that should be added
	 * @return added hook
	 */
	@Override
	public OrderedHook addHook(HookType eventType, IHook script)
	{
		return addHook(eventType, script, 100);
	}

	/**
	 * Adds a new {@link IHook} to this container. Not that there cannot be multiple hooks registered 
	 * for same HookType with same getName()
	 * @param eventType desired HookType
	 * @param script Hook that should be added
	 * @param order Order in which hooks will be executed. Lower = sooner, Higher = later
	 * @return added hook
	 */
	@Override
	public OrderedHook addHook(HookType eventType, IHook script, int order)
	{
		return addHook(eventType, new OrderedHook(order, script));
	}

	@Override
	public boolean removeHook(HookType eventType, IHook script)
	{
		_lock.lock();

		try
		{
			int ordinal = eventType.ordinal();
			IncArrayList<OrderedHook> scriptsByEvents = _scriptEvents[ordinal];

			if(scriptsByEvents == null)
			{
				return false;
			}

			for(OrderedHook orderedHooks : scriptsByEvents)
			{
				if(!orderedHooks.getScript().equals(script))
				{
					continue;
				}

				return scriptsByEvents.remove(orderedHooks);
			}
		}
		finally
		{
			_lock.unlock();
		}

		return false;
	}

	@Override
	public Iterable<IHook> getRegisteredHooks(HookType eventType)
	{
		IncArrayList<OrderedHook> scripts = _scriptEvents[eventType.ordinal()];
		if(scripts == null)
		{
			return null;
		}

		return () -> new OrderedIterator(scripts);
	}

	@Override
	public Collection<OrderedHook>[] getAllHooks()
	{
		return _scriptEvents;
	}

	/**
	 * Adds a new {@link OrderedHook} to this container. Not that there cannot be multiple hooks registered
	 * for same HookType with same getName()
	 * @param eventType desired HookType
	 * @param oScript Hook that should be added
	 * @return added hook
	 */
	public OrderedHook addHook(HookType eventType, OrderedHook oScript)
	{
		_lock.lock();

		try
		{
			int ordinal = eventType.ordinal();
			IncArrayList<OrderedHook> scriptsByEvents = _scriptEvents[ordinal];

			if(scriptsByEvents == null)
			{
				scriptsByEvents = new IncArrayList<>();
				scriptsByEvents.add(oScript);
				_scriptEvents[ordinal] = scriptsByEvents;
			}
			else
			{
				OrderedHook temp;
				for(int i = scriptsByEvents.size(); i-- > 0; )
				{
					temp = scriptsByEvents.getUnsafe(i);
					if(oScript.getScript().getName().equalsIgnoreCase(temp.getScript().getName()))
					{
						scriptsByEvents.setUnsafeVoid(i, oScript);
						return oScript;
					}
				}

				for(int i = 0; i < scriptsByEvents.size(); i++)
				{
					temp = scriptsByEvents.getUnsafe(i);
					if(oScript.getOrder() < temp.getOrder())
					{
						scriptsByEvents.add(i, oScript);
						return oScript;
					}
				}
				scriptsByEvents.addLast(oScript);
			}
		}
		finally
		{
			_lock.unlock();
		}

		return null;
	}
}