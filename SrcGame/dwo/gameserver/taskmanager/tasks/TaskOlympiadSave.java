package dwo.gameserver.taskmanager.tasks;

import dwo.gameserver.model.world.olympiad.Olympiad;
import dwo.gameserver.taskmanager.Task;
import dwo.gameserver.taskmanager.TaskTypes;
import dwo.gameserver.taskmanager.manager.TaskManager;
import dwo.gameserver.taskmanager.manager.TaskManager.ExecutedTask;
import org.apache.log4j.Level;

/**
 * @author godson
 */

public class TaskOlympiadSave extends Task
{
	public static final String NAME = "olympiad_save";

	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "900000", "1800000", "");
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		if(Olympiad.getInstance().inCompPeriod())
		{
			Olympiad.getInstance().saveOlympiadStatus();
			_log.log(Level.INFO, "TaskOlympiadSave: Data updated.");
		}
	}
}
