package dwo.gameserver.taskmanager.tasks;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.Mail;
import dwo.gameserver.model.player.mail.MailMessage;
import dwo.gameserver.taskmanager.Task;
import dwo.gameserver.taskmanager.TaskTypes;
import dwo.gameserver.taskmanager.manager.TaskManager;
import dwo.gameserver.taskmanager.manager.TaskManager.ExecutedTask;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Nyaran
 */

public class TaskBirthday extends Task
{
	private static final String NAME = "birthday";

	private static final Calendar _today = Calendar.getInstance();

	private int _count;

	private void checkBirthday(int year, int month, int day)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_CHAR_CREATEDATE);
			statement.setString(1, "%-" + getNum(month + 1) + '-' + getNum(day));

			rset = statement.executeQuery();
			while(rset.next())
			{
				int playerId = rset.getInt("charId");
				Calendar createDate = Calendar.getInstance();
				createDate.setTime(rset.getDate("createDate"));

				int age = year - createDate.get(Calendar.YEAR);

				if(age <= 0) // Player births this year
				{
					continue;
				}

				String text = Config.ALT_BIRTHDAY_MAIL_TEXT;

				if(text.contains("$c1"))
				{
					text = text.replace("$c1", CharNameTable.getInstance().getNameById(playerId));
				}
				if(text.contains("$s1"))
				{
					text = text.replace("$s1", String.valueOf(age));
				}

				MailMessage msg = new MailMessage(playerId, Config.ALT_BIRTHDAY_MAIL_SUBJECT, text, "Алегрия");

				Mail attachments = msg.createAttachments();
				attachments.addItem(ProcessType.CONSUME, Config.ALT_BIRTHDAY_GIFT, 1, null, null);

				msg.sendMessage();
				_count++;
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "TaskBirthday: Error checking birthdays. ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		// If character birthday is 29-Feb and year isn't leap, send gift on 28-feb
		GregorianCalendar calendar = new GregorianCalendar();
		if(month == Calendar.FEBRUARY && day == 28 && !calendar.isLeapYear(_today.get(Calendar.YEAR)))
		{
			checkBirthday(year, Calendar.FEBRUARY, 29);
		}
	}

	private String getNum(int num)
	{
		if(num <= 9)
		{
			return "0" + num;
		}
		return String.valueOf(num);
	}

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
		Calendar lastExecDate = Calendar.getInstance();
		long lastActivation = task.getLastActivation();

		if(lastActivation > 0)
		{
			lastExecDate.setTimeInMillis(lastActivation);
		}

		String rangeDate = '[' + Util.getDateString(lastExecDate.getTime()) + "] - [" + Util.getDateString(_today.getTime()) + ']';

		for(; !_today.before(lastExecDate); lastExecDate.add(Calendar.DATE, 1))
		{
			checkBirthday(lastExecDate.get(Calendar.YEAR), lastExecDate.get(Calendar.MONTH), lastExecDate.get(Calendar.DATE));
		}

		_log.log(Level.INFO, "TaskBirthday: " + _count + " gifts sent. " + rangeDate);
	}
}

