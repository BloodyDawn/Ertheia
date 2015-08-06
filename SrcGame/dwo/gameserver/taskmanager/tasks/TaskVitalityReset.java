package dwo.gameserver.taskmanager.tasks;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.stat.PcStat;
import dwo.gameserver.model.holders.VitalityHolder;
import dwo.gameserver.taskmanager.Task;
import dwo.gameserver.taskmanager.TaskTypes;
import dwo.gameserver.taskmanager.manager.TaskManager;
import dwo.gameserver.taskmanager.manager.TaskManager.ExecutedTask;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;

import java.util.Calendar;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 14.02.12
 * Time: 1:47
 */

public class TaskVitalityReset extends Task
{
	public static final String NAME = "vitality_reset";

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
		int currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

		if(currentDayOfWeek == Calendar.WEDNESDAY)
		{
			// Сбрасываем виталити для оффлайн персонажей
			DatabaseUtils.executeStatementQuick(Characters.VITALITY_CLEAR);

			// Сбрасываем виталити для оналайн персонажей
			for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
			{
				for(VitalityHolder holder : player.getVitalityData().values())
				{
					holder.setVitalityItems(Config.VITALITY_ITEMS_WEEKLY_LIMIT);
					holder.setVitalityPoints(PcStat.MAX_VITALITY_POINTS);
				}
				player.saveVitalityData();
			}
			_log.log(Level.INFO, "TaskVitalityReset: Vitality data renewed successfully.");
		}
		else
		{
			_log.log(Level.INFO, "TaskVitalityReset: Not Wednesday. Skipping task.");
		}
	}
}
