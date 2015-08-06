package dwo.gameserver.engine.hookengine;

import java.util.Collection;

/**
 * Classes implementing this interface should provide a way how to store hooks and a way how to retrieve them
 */

public interface IHookContainer
{
	OrderedHook addHook(HookType eventType, IHook script);

	OrderedHook addHook(HookType eventType, IHook script, int order);

	boolean removeHook(HookType eventType, IHook script);

	Iterable<IHook> getRegisteredHooks(HookType eventType);

	Collection<OrderedHook>[] getAllHooks();
}
