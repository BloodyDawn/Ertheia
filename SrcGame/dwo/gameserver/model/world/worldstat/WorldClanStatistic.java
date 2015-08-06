package dwo.gameserver.model.world.worldstat;

import dwo.gameserver.datatables.sql.queries.WorldStatistic;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 14.12.11
 * Time: 2:55
 */

public class WorldClanStatistic
{
	private static final Logger _log = LogManager.getLogger(WorldClanStatistic.class);

	protected int _clanId;

	// Кланы
	protected long _clanMembersCount;
	protected long _clanInvitesCount;
	protected long _clanLeavedCount;
	protected long _clanReputationCount;
	protected long _clanAdenaAddedInWh;
	protected long _clanPvpCount;
	protected long _clanWarWinCount;

	private boolean _isUpdateNeeded = true;

	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}

	public boolean isNeedToUpdate()
	{
		return _isUpdateNeeded;
	}

	public void setNeedToUpdate(boolean val)
	{
		_isUpdateNeeded = val;
	}

	/**
	 * Обновление статистики в базе данных
	 */
	public void updateStatsInDb()
	{
		if(!_isUpdateNeeded)
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(WorldStatistic.updateStats(true));

			statement.setLong(1, _clanMembersCount);
			statement.setLong(2, _clanInvitesCount);
			statement.setLong(3, _clanLeavedCount);
			statement.setLong(4, _clanReputationCount);
			statement.setLong(5, _clanAdenaAddedInWh);
			statement.setLong(6, _clanPvpCount);
			statement.setLong(7, _clanWarWinCount);

			statement.setLong(8, _clanId);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "WorldStatisticsManager: Failed update char World Statistics.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			_isUpdateNeeded = false;
		}
	}

	/**
	 * Создание пустой записи статистики для клана
	 */
	public void insertStatsInDb()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(WorldStatistic.INSERT_STATS);
			statement.setLong(1, _clanId);
			statement.execute();

			statement = con.prepareStatement(WorldStatistic.INSERT_GENERAL_STATS);
			statement.setLong(1, _clanId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "WorldStatisticsManager: Failed inserting clan World Statistics.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			_isUpdateNeeded = false;
		}
	}

	public long getClanMembersCount()
	{
		return _clanMembersCount;
	}

	public void setClanMembersCount(long count)
	{
		_clanMembersCount = count;
		_isUpdateNeeded = true;
	}

	public long getClanInvitesCount()
	{
		return _clanInvitesCount;
	}

	public void setClanInvitesCount(long count)
	{
		if(!validateLong(_clanInvitesCount, count))
		{
			return;
		}

		_clanInvitesCount += count;
		_isUpdateNeeded = true;
	}

	public long getClanLeavedCount()
	{
		return _clanLeavedCount;
	}

	public void setClanLeavedCount(long count)
	{
		if(!validateLong(_clanLeavedCount, count))
		{
			return;
		}

		_clanLeavedCount += count;
		_isUpdateNeeded = true;
	}

	public long getReputationCount()
	{
		return _clanReputationCount;
	}

	public void setReputationCount(long count)
	{
		if(!validateLong(_clanReputationCount, count))
		{
			return;
		}

		_clanReputationCount += count;
		_isUpdateNeeded = true;
	}

	public long getClanAdenaCount()
	{
		return _clanAdenaAddedInWh;
	}

	public void setClanAdenaCount(long count)
	{
		if(!validateLong(_clanAdenaAddedInWh, count))
		{
			return;
		}

		_clanAdenaAddedInWh += count;
		_isUpdateNeeded = true;
	}

	public long getClanPvpCount()
	{
		return _clanPvpCount;
	}

	public void setClanPvpCount(long count)
	{
		if(!validateLong(_clanPvpCount, count))
		{
			return;
		}

		_clanPvpCount += count;
		_isUpdateNeeded = true;
	}

	public long getClanWarWinCount()
	{
		return _clanWarWinCount;
	}

	public void setClanWarWinCount(long count)
	{
		if(!validateLong(_clanWarWinCount, count))
		{
			return;
		}

		_clanWarWinCount += count;
		_isUpdateNeeded = true;
	}

	public boolean validateLong(long oldValue, long addValue)
	{
		return Long.MAX_VALUE - oldValue >= addValue;
	}
}