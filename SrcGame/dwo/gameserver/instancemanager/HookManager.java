package dwo.gameserver.instancemanager;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.engine.hookengine.IHook;
import dwo.gameserver.engine.hookengine.IHookContainer;
import dwo.gameserver.engine.hookengine.container.HookContainerImpl;
import dwo.gameserver.engine.hookengine.impl.skills.SkillHook;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Global Hook manager serves as place where all global hooks should be stored. Manager notifies these hooks about triggered events.
 * To add hook call, check notifyEvent/checkEvent methods
 * Also check {@link HookType} and {@link IHook}
 */

public class HookManager extends HookContainerImpl
{
	protected static final Logger _log = LogManager.getLogger(HookManager.class);

	private HookManager()
	{
		SkillHook hk = new SkillHook();

		addHook(HookType.ON_SKILL_ADD, hk);
		addHook(HookType.ON_DAYNIGHT_CHANGE, hk);
		addHook(HookType.ON_INVENTORY_ADD, hk);
		addHook(HookType.ON_INVENTORY_DELETE, hk);
	}

	public static HookManager getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * ASYNCHRONOUSLY notifies global hooks and container specified by additionalContainer parameter about event.
	 * @param type Type of hooks that will be notified
	 * @param additionalContainer Can be null! Another container that also will be notified, for example player's hook container
	 * @param args Args needed for calling can be found in corresponding hook method in {@link IHook}
	 */
	public void notifyEventAsync(HookType type, IHookContainer additionalContainer, Object... args)
	{
		ThreadPoolManager.getInstance().executeTask(new AsyncEventExecutor(type, additionalContainer, args));
	}

	/**
	 * Notifies global hooks and container specified by additionalContainer parameter about HookType event.
	 * @param type Type of hooks that will be notified
	 * @param additionalContainer Can be null! Another container that also will be notified, for example player's hook container
	 * @param args Args needed for calling can be found in corresponding hook method in {@link IHook}
	 */
	public void notifyEvent(HookType type, IHookContainer additionalContainer, Object... args)
	{
		notifyEventIteration(type, this, args);
		notifyEventIteration(type, additionalContainer, args);
	}

	private void notifyEventIteration(HookType type, IHookContainer cont, Object... args)
	{
		if(cont == null)
		{
			return;
		}

		Iterable<IHook> hooks = cont.getRegisteredHooks(type);

		if(hooks == null)
		{
			return;
		}

		for(IHook hook : hooks)
		{
			try
			{
				type.getInvokingMethod().invoke(hook, args);
			}
			catch(Exception e)
			{
				StringBuilder temp = new StringBuilder();
				for(Object arg : args)
				{
					temp.append(' ').append(arg).append(' ');
				}
				_log.log(Level.ERROR, "hook: " + hook + " args: " + temp + " method: " + type.getInvokingMethod(), e);
			}
		}
	}

	/**
	 * Notifies global hooks and container specified by additionalContainer parameter about HookType event.
	 * Returns immediately when returnCondition == return value of hook (for example when some hook confirms that player
	 * is invulnerable and returnCondition is true, then iteration will end instantly and true is returned, because there isn't
	 * really a need to check any further
	 * @param type Type of hooks that will be notified
	 * @param additionalContainer Can be null! Another container that also will be notified, for example player's hook container
	 * @param returnCondition When any hook returns a value that equals this parameter, return returnCondition; is called
	 * @param args Args needed for calling can be found in corresponding hook method in {@link IHook}
	 * @return Result of checks
	 */
	public boolean checkEvent(HookType type, IHookContainer additionalContainer, boolean returnCondition, Object... args)
	{
		return checkEventIteration(type, this, returnCondition, args) && checkEventIteration(type, additionalContainer, returnCondition, args);
	}

	private boolean checkEventIteration(HookType type, IHookContainer cont, boolean returnCondition, Object... args)
	{
		if(cont == null)
		{
			return !returnCondition;
		}

		Iterable<IHook> hooks = cont.getRegisteredHooks(type);

		if(hooks == null)
		{
			return !returnCondition;
		}

		for(IHook hook : hooks)
		{
			try
			{
				Object result = type.getInvokingMethod().invoke(hook, args);

				if(result.equals(returnCondition))
				{
					return returnCondition;
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}

		return !returnCondition;
	}

	private static class SingletonHolder
	{
		protected static final HookManager _instance = new HookManager();
	}

	private class AsyncEventExecutor implements Runnable
	{
		HookType type;
		IHookContainer container;
		Object[] args;

		public AsyncEventExecutor(HookType type, IHookContainer additionalContainer, Object... args)
		{
			this.type = type;
			container = additionalContainer;
			this.args = args;
		}

		@Override
		public void run()
		{
			notifyEventIteration(type, HookManager.this, args);
			notifyEventIteration(type, container, args);
		}
	}
}
