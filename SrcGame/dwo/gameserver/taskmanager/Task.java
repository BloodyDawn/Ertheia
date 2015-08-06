package dwo.gameserver.taskmanager;

import dwo.config.Config;
import dwo.gameserver.taskmanager.manager.TaskManager.ExecutedTask;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.ScheduledFuture;

/**
 * @author Layane
 */

public abstract class Task
{
	public static Logger _log = LogManager.getLogger(Task.class);

	public void initializate()
	{
		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "Task" + getName() + " inializate");
		}
	}

	public ScheduledFuture<?> launchSpecial(ExecutedTask instance)
	{
		return null;
	}

	public abstract String getName();

	public abstract void onTimeElapsed(ExecutedTask task);

	public void onDestroy()
	{
	}
}
