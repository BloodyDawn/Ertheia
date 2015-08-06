package dwo.gameserver.engine.logengine.filters;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 20.05.12
 * Time: 19:52
 */

public class VoteLogFilter extends Filter
{
	@Override
	public int decide(LoggingEvent loggingEvent)
	{
		if(loggingEvent.getLoggerName().equalsIgnoreCase("vote"))
		{
			return ACCEPT;
		}
		return DENY;
	}
}