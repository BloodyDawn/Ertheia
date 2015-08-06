package dwo.gameserver.handler;

import dwo.gameserver.handler.actions.L2DoorInstanceActionShift;
import dwo.gameserver.handler.actions.L2ItemInstanceActionShift;
import dwo.gameserver.handler.actions.L2NpcActionShift;
import dwo.gameserver.handler.actions.L2PcInstanceActionShift;
import dwo.gameserver.handler.actions.L2StaticObjectInstanceActionShift;
import dwo.gameserver.handler.actions.L2SummonActionShift;
import dwo.gameserver.model.actor.L2Object;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ActionShiftHandler implements IHandler<IActionHandler, Class<? extends L2Object>>
{
	protected static Logger _log = LogManager.getLogger(ActionShiftHandler.class);

	private final Map<Class<? extends L2Object>, IActionHandler> _actionsShift;

	private ActionShiftHandler()
	{
		_actionsShift = new HashMap<>();
		registerHandler(new L2DoorInstanceActionShift());
		registerHandler(new L2ItemInstanceActionShift());
		registerHandler(new L2NpcActionShift());
		registerHandler(new L2PcInstanceActionShift());
		registerHandler(new L2StaticObjectInstanceActionShift());
		registerHandler(new L2SummonActionShift());
		_log.log(Level.INFO, "Loaded " + size() + " Action Shift Handlers");
	}

	public static ActionShiftHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void registerHandler(IActionHandler handler)
	{
		_actionsShift.put(handler.getInstanceType(), handler);
	}

	@Override
	public void removeHandler(IActionHandler handler)
	{
		synchronized(this)
		{
			_actionsShift.remove(handler.getInstanceType());
		}
	}

	@Override
	public IActionHandler getHandler(Class<? extends L2Object> type)
	{
		IActionHandler result = null;
		for(Class<?> cls = type; cls != null && !cls.equals(Object.class); cls = cls.getSuperclass())
		{
			if(_actionsShift.containsKey(cls))
			{
				result = _actionsShift.get(cls);
				break;
			}
		}

		return result;
	}

	@Override
	public int size()
	{
		return _actionsShift.size();
	}

	private static class SingletonHolder
	{
		protected static final ActionShiftHandler _instance = new ActionShiftHandler();
	}
}