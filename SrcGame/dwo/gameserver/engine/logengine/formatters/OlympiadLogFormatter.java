package dwo.gameserver.engine.logengine.formatters;

import dwo.gameserver.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OlympiadLogFormatter
{
	private static final SimpleDateFormat dateFmt = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");

	public static String format(String message, Object[] params)
	{
		StringBuilder output = StringUtil.startAppend(30 + message.length() + (params == null ? 0 : params.length * 10), dateFmt.format(new Date(System.currentTimeMillis())), ",", message);
		if(params != null)
		{
			for(Object p : params)
			{
				if(p == null)
				{
					continue;
				}
				StringUtil.append(output, ",", p.toString());
			}
		}
		return output.toString();
	}
}
