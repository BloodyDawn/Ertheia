package dwo.gameserver.engine.logengine.filters;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 30.06.12
 * Time: 13:18
 */

public class DonateLogFilter extends Filter
{
	@Override
	public int decide(LoggingEvent loggingEvent)
	{
		if(loggingEvent.getLoggerName().equalsIgnoreCase("donate"))
		{
			return ACCEPT;
		}
		return DENY;
	}
}