package dwo.gameserver.engine.logengine.filters;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 02.05.12
 * Time: 0:04
 */

public class LameguardLogFilter extends Filter
{
	@Override
	public int decide(LoggingEvent loggingEvent)
	{
		if(loggingEvent.getLoggerName().equalsIgnoreCase("lameguard"))
		{
			return ACCEPT;
		}
		return DENY;
	}
}
