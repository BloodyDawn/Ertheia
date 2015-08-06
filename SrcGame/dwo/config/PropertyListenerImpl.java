package dwo.config;

import jfork.nproperty.IPropertyListener;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.xx
 * Time: xx:xx
 */

public class PropertyListenerImpl implements IPropertyListener
{
	protected static final Logger _log = LogManager.getLogger(PropertyListenerImpl.class);

	@Override
	public void onStart(String path)
	{
		_log.log(Level.INFO, "Loading: " + path + " -> " + getClass().getSimpleName());
	}

	@Override
	public void onPropertyMiss(String name)
	{
		_log.log(Level.WARN, "Missing property for key - " + name);
	}

	@Override
	public void onDone(String path)
	{

	}

	@Override
	public void onInvalidPropertyCast(String name, String value)
	{
		_log.log(Level.WARN, "Invalid config property -> " + name + ". Failed to store value " + value);
	}
}
