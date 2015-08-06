package dwo.gameserver.taskmanager.manager;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.util.Broadcast;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.List;

/**
 * @author nBd
 */

public class AutoAnnounceTaskManager
{
	protected static final Logger _log = LogManager.getLogger(AutoAnnounceTaskManager.class);

	protected List<AutoAnnouncement> _announces = new FastList<>();

	private int _nextId = 1;

	private AutoAnnounceTaskManager()
	{
		restore();
	}

	public static AutoAnnounceTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public List<AutoAnnouncement> getAutoAnnouncements()
	{
		return _announces;
	}

	public void restore()
	{
		if(!_announces.isEmpty())
		{
			for(AutoAnnouncement a : _announces)
			{
				a.stopAnnounce();
			}

			_announces.clear();
		}

		ThreadConnection conn = null;
		FiltredPreparedStatement statement = null;
		ResultSet data = null;
		int count = 0;
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();
			statement = conn.prepareStatement("SELECT id, initial, delay, cycle, memo FROM auto_announcements");
			data = statement.executeQuery();
			while(data.next())
			{
				int id = data.getInt("id");
				long initial = data.getLong("initial");
				long delay = data.getLong("delay");
				int repeat = data.getInt("cycle");
				String memo = data.getString("memo");
				String[] text = memo.split("/n");
				ThreadPoolManager.getInstance().scheduleGeneral(new AutoAnnouncement(id, delay, repeat, text), initial);
				count++;
				if(_nextId <= id)
				{
					_nextId = id + 1;
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "AutoAnnoucements: Failed to load announcements data.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(conn, statement, data);
		}
		_log.log(Level.INFO, "AutoAnnoucements: Loaded " + count + " Auto Annoucement Data.");
	}

	public void addAutoAnnounce(long initial, long delay, int repeat, String memo)
	{
		ThreadConnection conn = null;
		FiltredPreparedStatement statement = null;

		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();
			statement = conn.prepareStatement("INSERT INTO auto_announcements (id, initial, delay, cycle, memo) VALUES (?,?,?,?,?)");
			statement.setInt(1, _nextId);
			statement.setLong(2, initial);
			statement.setLong(3, delay);
			statement.setInt(4, repeat);
			statement.setString(5, memo);
			statement.execute();

			String[] text = memo.split("/n");
			ThreadPoolManager.getInstance().scheduleGeneral(new AutoAnnouncement(_nextId++, delay, repeat, text), initial);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "AutoAnnoucements: Failed to add announcements data.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(conn, statement);
		}
	}

	public void deleteAutoAnnounce(int index)
	{
		ThreadConnection conn = null;
		FiltredPreparedStatement statement = null;

		try
		{
			AutoAnnouncement a = _announces.get(index);
			a.stopAnnounce();

			conn = L2DatabaseFactory.getInstance().getConnection();
			statement = conn.prepareStatement("DELETE FROM auto_announcements WHERE id = ?");
			statement.setInt(1, a._id);
			statement.execute();

			_announces.remove(index);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "AutoAnnoucements: Failed to delete announcements data.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(conn, statement);
		}
	}

	public void announce(String text)
	{
		Broadcast.announceToOnlinePlayers(text);
	}

	private static class SingletonHolder
	{
		protected static final AutoAnnounceTaskManager _instance = new AutoAnnounceTaskManager();
	}

	public class AutoAnnouncement implements Runnable
	{
		private int _id;
		private long _delay;
		private int _repeat = -1;
		private String[] _memo;
		private boolean _stopped;

		public AutoAnnouncement(int id, long delay, int repeat, String[] memo)
		{
			_id = id;
			_delay = delay;
			_repeat = repeat;
			_memo = memo;
			if(!_announces.contains(this))
			{
				_announces.add(this);
			}
		}

		public String[] getMemo()
		{
			return _memo;
		}

		public void stopAnnounce()
		{
			_stopped = true;
		}

		@Override
		public void run()
		{
			if(!_stopped && _repeat != 0)
			{
				_log.log(Level.INFO, "AutoAnnounce: announce send to all online players.");
				for(String text : _memo)
				{
					announce(text);
				}

				if(_repeat > 0)
				{
					_repeat--;
				}
				ThreadPoolManager.getInstance().scheduleGeneral(this, _delay);
			}
			else
			{
				stopAnnounce();
			}
		}
	}
}
