package dwo.gameserver.taskmanager.tasks;

import dwo.gameserver.instancemanager.GlobalVariablesManager;
import dwo.gameserver.taskmanager.Task;
import dwo.gameserver.taskmanager.TaskTypes;
import dwo.gameserver.taskmanager.manager.TaskManager;
import dwo.gameserver.taskmanager.manager.TaskManager.ExecutedTask;
import org.apache.log4j.Level;

/**
 * @author Gigiikun
 */

public class TaskGlobalVariablesSave extends Task
{
	public static final String NAME = "global_varibales_save";

	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "500000", "1800000", "");
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		GlobalVariablesManager.getInstance().saveVars();
		_log.log(Level.INFO, "TaskGlobalVariablesSave: Data updated successfully.");
	}
}