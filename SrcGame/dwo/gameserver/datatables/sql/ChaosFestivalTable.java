package dwo.gameserver.datatables.sql;

import dwo.gameserver.datatables.sql.queries.ChaosFestival;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ChaosFestivalEntry;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chaos Festival players table.
 *
 * @author Yorie
 */
public class ChaosFestivalTable
{
	private static final Logger _log = LogManager.getLogger(ChaosFestivalTable.class);

	private final Map<Integer, ChaosFestivalEntry> _festivalEntries = new ConcurrentHashMap<>();

	public static ChaosFestivalTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private ChaosFestivalEntry loadEntry(int playerId)
	{
		if(_festivalEntries.containsKey(playerId))
		{
			return _festivalEntries.get(playerId);
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(ChaosFestival.LOAD_CHAOS_FESTIVAL_ENTRY);
			statement.setInt(1, playerId);
			rset = statement.executeQuery();

			ChaosFestivalEntry entry;
			if(rset.next())
			{
				entry = new ChaosFestivalEntry(playerId, rset.getInt("myst_signs"), rset.getInt("skip_rounds"), rset.getInt("total_bans"));
				_festivalEntries.put(playerId, entry);
			}
			else
			{
				entry = new ChaosFestivalEntry(playerId);
			}
			return entry;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not load character chaos festival data: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		return null;
	}

	public void cleanUp()
	{
		for(ChaosFestivalEntry entry : _festivalEntries.values())
		{
			if(entry.isNeedUpdate())
			{
				ThreadConnection con = null;
				FiltredPreparedStatement statement = null;
				ResultSet rset = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement(ChaosFestival.UPDATE_INFO);
					statement.setInt(1, entry.getMystSigns());
					statement.setInt(2, entry.getSkipRounds());
					statement.setInt(3, entry.getTotalBans());
					statement.setInt(4, entry.getPlayerId());
					statement.executeUpdate();

					_log.log(Level.INFO, getClass().getSimpleName() + ": Chaos Festival logs saved successfully.");
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "Could not save character chaos festival data! " + e.getMessage(), e);
				}
				finally
				{
					DatabaseUtils.closeDatabaseCSR(con, statement, rset);
				}
			}
		}
	}

	public ChaosFestivalEntry getEntry(L2PcInstance player)
	{
		return getEntry(player.getObjectId());
	}

	public ChaosFestivalEntry getEntry(int playerId)
	{
		return loadEntry(playerId);
	}

	public Map<Integer, ChaosFestivalEntry> getFestivalEntries()
	{
		return _festivalEntries;
	}

	private static class SingletonHolder
	{
		protected static final ChaosFestivalTable _instance = new ChaosFestivalTable();
	}
}
