package dwo.gameserver.engine.logengine.filters;

import org.apache.log4j.Level;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 08.11.11
 * Time: 7:45
 */

public class DebuggingFilter extends Filter
{
	@Override
	public int decide(LoggingEvent loggingEvent)
	{
		if(loggingEvent.getLevel().equals(Level.DEBUG))
		{
			return ACCEPT;
		}
		return DENY;
	}
}
