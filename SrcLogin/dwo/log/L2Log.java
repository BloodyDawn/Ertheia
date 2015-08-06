package dwo.log;

import org.apache.log4j.xml.DOMConfigurator;

import java.io.File;

public class L2Log
{
	public static void initLogging()
	{
		new File("log").mkdirs();
		new File("log/java").mkdirs();
		DOMConfigurator.configure("./config/log4j.xml");
		System.setProperty("line.separator", "\r\n");
		System.setProperty("file.encoding", "UTF-8");
	}
}
