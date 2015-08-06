package dwo.gameserver.engine.logengine.filters;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class PDamageLogFilter extends Filter
{
	@Override
	public int decide(LoggingEvent loggingEvent)
	{
		if(loggingEvent.getLoggerName().equalsIgnoreCase("Pdamage"))
		{
			return ACCEPT;
		}
		return DENY;
	}
}