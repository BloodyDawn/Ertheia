package dwo.log;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

/**
 * Just a factory to createloggers as default loggers.
 *
 * @author SoulKeeper
 */
public class ThrowableAsMessageAwareFactory implements LoggerFactory
{

	/**
	 * Creates new logger with given name
	 *
	 * @param name new logger's name
	 * @return new logger instance
	 */
	@Override
	public Logger makeNewLoggerInstance(String name)
	{
		return new ThrowableAsMessageLogger(name);
	}
}