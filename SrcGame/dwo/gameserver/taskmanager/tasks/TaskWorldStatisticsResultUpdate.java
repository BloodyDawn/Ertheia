package dwo.gameserver.taskmanager.tasks;

import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;
import dwo.gameserver.taskmanager.Task;
import dwo.gameserver.taskmanager.TaskTypes;
import dwo.gameserver.taskmanager.manager.TaskManager;
import org.apache.log4j.Level;

import java.util.Calendar;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 23.12.11
 * Time: 8:43
 */

public class TaskWorldStatisticsResultUpdate extends Task
{
	public static final String NAME = "world_statistics_result_update";

	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "400000", "1800000", "");
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void onTimeElapsed(TaskManager.ExecutedTask task)
	{
		long startTime = System.currentTimeMillis();
		Calendar current = Calendar.getInstance();
		WorldStatisticsManager.getInstance().calculateAllResults(current.get(Calendar.DAY_OF_MONTH) == 1);
		_log.log(Level.INFO, "World Statistic Manager: Statistic results updated successfully (" + (System.currentTimeMillis() - startTime) / 1000 + " seconds)");
	}
}
