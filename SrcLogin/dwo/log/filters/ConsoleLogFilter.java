package dwo.log.filters;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class ConsoleLogFilter extends Filter
{

	String[] _loggers = {
		"chat", "Pdamage", "Mdamage", "audit", "item", "enchantItem", "enchantSkill", "olympiad"
	};

	@Override
	public int decide(LoggingEvent loggingEvent)
	{
		for(String logger : _loggers)
		{
			if(loggingEvent.getLoggerName().equalsIgnoreCase(logger))
			{
				return DENY;
			}
		}
		return ACCEPT;
	}
}