package dwo.gameserver.handler;

import dwo.gameserver.handler.actions.L2ArtefactInstanceAction;
import dwo.gameserver.handler.actions.L2DecoyAction;
import dwo.gameserver.handler.actions.L2DoorInstanceAction;
import dwo.gameserver.handler.actions.L2ItemInstanceAction;
import dwo.gameserver.handler.actions.L2NpcAction;
import dwo.gameserver.handler.actions.L2PcInstanceAction;
import dwo.gameserver.handler.actions.L2PetInstanceAction;
import dwo.gameserver.handler.actions.L2StaticObjectInstanceAction;
import dwo.gameserver.handler.actions.L2SummonAction;
import dwo.gameserver.handler.actions.L2TrapAction;
import dwo.gameserver.model.actor.L2Object;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ActionHandler implements IHandler<IActionHandler, Class<? extends L2Object>>
{
	protected static Logger _log = LogManager.getLogger(ActionHandler.class);

	private final Map<Class<? extends L2Object>, IActionHandler> _actions;

	private ActionHandler()
	{
		_actions = new HashMap<>();
		registerHandler(new L2ArtefactInstanceAction());
		registerHandler(new L2DecoyAction());
		registerHandler(new L2DoorInstanceAction());
		registerHandler(new L2ItemInstanceAction());
		registerHandler(new L2NpcAction());
		registerHandler(new L2PcInstanceAction());
		registerHandler(new L2PetInstanceAction());
		registerHandler(new L2StaticObjectInstanceAction());
		registerHandler(new L2SummonAction());
		registerHandler(new L2TrapAction());
		_log.log(Level.INFO, "Loaded " + size() + "  Action Handlers");
	}

	public static ActionHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void registerHandler(IActionHandler handler)
	{
		_actions.put(handler.getInstanceType(), handler);
	}

	@Override
	public void removeHandler(IActionHandler handler)
	{
		synchronized(this)
		{
			_actions.remove(handler.getInstanceType());
		}
	}

	@Override
	public IActionHandler getHandler(Class<? extends L2Object> type)
	{
		IActionHandler result = null;
		for(Class<?> cls = type; cls != null && !cls.equals(Object.class); cls = cls.getSuperclass())
		{
			if(_actions.containsKey(cls))
			{
				result = _actions.get(cls);
				break;
			}
		}

		return result;
	}

	@Override
	public int size()
	{
		return _actions.size();
	}

	private static class SingletonHolder
	{
		protected static final ActionHandler _instance = new ActionHandler();
	}
}