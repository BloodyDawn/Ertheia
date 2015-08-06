package dwo.gameserver.engine.logengine;

import org.apache.log4j.xml.DOMConfigurator;

public class L2Log
{
	public static void initLogging()
	{
		DOMConfigurator.configure("./config/log4j.xml");
		System.setProperty("line.separator", "\r\n");
		System.setProperty("file.encoding", "UTF-8");
	}
}
