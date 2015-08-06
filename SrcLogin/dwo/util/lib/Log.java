package dwo.util.lib;

import dwo.util.StackTrace;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log
{
	private static final Logger _log = LogManager.getLogger(Log.class);

	public static void add(String text, String cat)
	{
		String date = new SimpleDateFormat("yy.MM.dd H:mm:ss").format(new Date());
		String curr = new SimpleDateFormat("yyyy-MM-dd-").format(new Date());
		new File("log/game").mkdirs();
		FileWriter save = null;

		try
		{
			File file = new File("log/game/" + (curr != null ? curr : "") + (cat != null ? cat : "unk") + ".txt");
			save = new FileWriter(file, true);
			String out = '[' + date + "] " + text + '\n';
			save.write(out);
		}
		catch(IOException e)
		{
			_log.log(Level.WARN, "Error saving logfile: ", e);
		}
		finally
		{
			try
			{
				save.close();
			}
			catch(Exception e)
			{
				StackTrace.displayStackTraceInformation(e);
			}
		}
	}
}
