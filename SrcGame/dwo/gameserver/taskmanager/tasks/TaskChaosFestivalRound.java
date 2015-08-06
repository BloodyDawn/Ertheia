package dwo.gameserver.taskmanager.tasks;

import dwo.gameserver.Announcements;
import dwo.gameserver.datatables.sql.ChaosFestivalTable;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.model.holders.ChaosFestivalEntry;
import dwo.gameserver.taskmanager.Task;
import dwo.gameserver.taskmanager.manager.TaskManager.ExecutedTask;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class TaskChaosFestivalRound extends Task
{
	public static final String NAME = "chaos_festival_round";

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		int playerId = -1;
		int collectedSigns = -1;
		boolean itsTie = false;
		for(ChaosFestivalEntry entry : ChaosFestivalTable.getInstance().getFestivalEntries().values())
		{
			if(collectedSigns < 0 || entry.getMystSigns() > collectedSigns)
			{
				playerId = entry.getPlayerId();
				collectedSigns = entry.getMystSigns();
				itsTie = false;
			}
			else if(entry.getMystSigns() == collectedSigns)
			{
				itsTie = true;
			}

			entry.setMystSigns(0);
			entry.setSkipRounds(0);
			entry.setTotalBans(0);
		}

		ChaosFestivalTable.getInstance().cleanUp();

		// Объявляем победителя цикла
		if(itsTie)
		{
			_log.log(Level.INFO, "Festival of chaos round ends with tie.");
		}
		else
		{
			if(playerId > 0)
			{
				String playerName = CharNameTable.getInstance().getNameById(playerId);
				Announcements.getInstance().announceToAll("Победитель Фестиваля Хаоса: " + playerName); // TODO Нормальный system msg
				_log.log(Level.INFO, "Festival of chaos round winner is " + playerName);
			}
			else
			{
				_log.log(Level.INFO, "Seems there was no participants in the Festival of Chaos. No winners selected.");
			}
		}

		_log.log(Level.INFO, "Chaos festival round ended successfully.");
	}
}