package dwo.gameserver.taskmanager.tasks;

import dwo.gameserver.GameServerShutdown;
import dwo.gameserver.taskmanager.Task;
import dwo.gameserver.taskmanager.manager.TaskManager.ExecutedTask;

/**
 * @author Layane
 */

public class TaskRestart extends Task
{
	public static final String NAME = "restart";

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		GameServerShutdown handler = new GameServerShutdown(Integer.parseInt(task.getParams()[2]), true);
		handler.start();
	}
}
