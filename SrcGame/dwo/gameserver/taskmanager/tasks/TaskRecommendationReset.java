package dwo.gameserver.taskmanager.tasks;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.taskmanager.Task;
import dwo.gameserver.taskmanager.TaskTypes;
import dwo.gameserver.taskmanager.manager.TaskManager;
import dwo.gameserver.taskmanager.manager.TaskManager.ExecutedTask;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;

public class TaskRecommendationReset extends Task
{
	private static final String NAME = "recommendations";

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
	public void onTimeElapsed(ExecutedTask task)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			// Сбрасываем для офлайн-персонажей
			con = L2DatabaseFactory.getInstance().getConnection();

			// Все права на рекомендацию, полученные за день, к 6:30 утра снова откатываются до 20.
			statement = con.prepareStatement("UPDATE character_recommendation SET rec_left=?, rec_have=0 WHERE rec_have <= 20");
			statement.setInt(1, 20);
			statement.executeUpdate();

			// Кроме того, количество рекомендаций (оценка), полученных от других пользователей, к 6 часам 30 минутам утра уменьшается на 20.
			statement = con.prepareStatement("UPDATE character_recommendation SET rec_left=?, rec_have=GREATEST(rec_have-20,0) WHERE rec_have > 20");
			statement.setInt(1, 20);
			statement.executeUpdate();

			// Сбрасываем для онлайн-персонажей
			for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
			{
				player.resetRecommendationData();
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "TaskRecommendationReset: Could not reset Recommendations System: " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		_log.log(Level.INFO, "TaskRecommendationReset: Completed.");
	}
}
