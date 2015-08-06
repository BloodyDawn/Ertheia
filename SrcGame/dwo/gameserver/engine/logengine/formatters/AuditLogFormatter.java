package dwo.gameserver.engine.logengine.formatters;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AuditLogFormatter
{
	private static final SimpleDateFormat dateFmt = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");

	public static String format(String message, Object[] params)
	{
		StringBuilder output = new StringBuilder();
		output.append('[');
		output.append(dateFmt.format(new Date(System.currentTimeMillis())));
		output.append(']');
		output.append(' ');
		output.append(message);
		if(params != null)
		{
			for(Object p : params)
			{
				if(p == null)
				{
					continue;
				}
				output.append(',');
				output.append(' ');
				output.append(p);
			}
		}

		return output.toString();
	}
}
