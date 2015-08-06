package dwo.gameserver.taskmanager.tasks;

import dwo.gameserver.datatables.sql.AccountShareDataTable;
import dwo.gameserver.taskmanager.Task;
import dwo.gameserver.taskmanager.TaskTypes;
import dwo.gameserver.taskmanager.manager.TaskManager;
import dwo.gameserver.taskmanager.manager.TaskManager.ExecutedTask;
import org.apache.log4j.Level;

public class TaskAccountDataSave extends Task
{
	public static final String NAME = "account_data_save";

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
		long startTime = System.currentTimeMillis();
		AccountShareDataTable.getInstance().updateInDb();
		_log.log(Level.INFO, "AccountDataTable: Data updated successfully.(" + (System.currentTimeMillis() - startTime) / 1000 + " seconds)");
	}
}
