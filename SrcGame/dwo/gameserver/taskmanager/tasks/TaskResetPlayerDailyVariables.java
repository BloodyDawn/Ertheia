package dwo.gameserver.taskmanager.tasks;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.taskmanager.Task;
import dwo.gameserver.taskmanager.TaskTypes;
import dwo.gameserver.taskmanager.manager.TaskManager;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 08.03.13
 * Time: 19:53
 */
public class TaskResetPlayerDailyVariables extends Task
{
	private static final String NAME = "daily_player_variables_daily_reset";

	private static final String[] VARIABLES_TO_RESET = {"newbieBonus", "destructionEnergy"};

	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void onTimeElapsed(TaskManager.ExecutedTask task)
	{
		// Сбрасываем переменные для офлайн персонажей
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_variables WHERE name=?");
			for(String var : VARIABLES_TO_RESET)
			{
				statement.setString(1, var);
				statement.execute();
				statement.clearParameters();
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "TaskResetPlayerDailyVariables: Error while reset player daily variable: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		// Сбрасываем переменные для оналайн персонажей
		for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
		{
			for(String var : VARIABLES_TO_RESET)
			{
				player.getVariablesController().unset(var);
			}
		}
		_log.log(Level.INFO, "TaskResetPlayerDailyVariables: Variables cleared.");
	}
}
