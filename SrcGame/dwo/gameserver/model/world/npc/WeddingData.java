/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.world.npc;

import dwo.gameserver.datatables.sql.queries.mod.Wedding;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.Calendar;

/**
 * @author evill33t
 */
public class WeddingData
{
	private static final Logger _log = LogManager.getLogger(WeddingData.class);

	private int _id;
	private int _player1Id;
	private int _player2Id;
	private boolean _married;
	private Calendar _affiancedDate;
	private Calendar _weddingDate;

	public WeddingData(int coupleId)
	{
		_id = coupleId;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Wedding.LOAD);
			statement.setInt(1, _id);
			rs = statement.executeQuery();

			while(rs.next())
			{
				_player1Id = rs.getInt("player1Id");
				_player2Id = rs.getInt("player2Id");
				_married = rs.getBoolean("married");

				_affiancedDate = Calendar.getInstance();
				_affiancedDate.setTimeInMillis(rs.getLong("affianceDate"));

				_weddingDate = Calendar.getInstance();
				_weddingDate.setTimeInMillis(rs.getLong("weddingDate"));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: Couple.load(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	public WeddingData(L2PcInstance player1, L2PcInstance player2)
	{
		int _tempPlayer1Id = player1.getObjectId();
		int _tempPlayer2Id = player2.getObjectId();

		_player1Id = _tempPlayer1Id;
		_player2Id = _tempPlayer2Id;

		_affiancedDate = Calendar.getInstance();
		_affiancedDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());

		_weddingDate = Calendar.getInstance();
		_weddingDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			_id = IdFactory.getInstance().getNextId();
			statement = con.prepareStatement(Wedding.INSERT);
			statement.setInt(1, _id);
			statement.setInt(2, _player1Id);
			statement.setInt(3, _player2Id);
			statement.setBoolean(4, false);
			statement.setLong(5, _affiancedDate.getTimeInMillis());
			statement.setLong(6, _weddingDate.getTimeInMillis());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not create couple: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void marry()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Wedding.UPDATE);
			statement.setBoolean(1, true);
			_weddingDate = Calendar.getInstance();
			statement.setLong(2, _weddingDate.getTimeInMillis());
			statement.setInt(3, _id);
			statement.execute();
			_married = true;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not marry: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void divorce()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Wedding.DELETE);
			statement.setInt(1, _id);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: Couple.divorce(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public int getId()
	{
		return _id;
	}

	public int getPlayer1Id()
	{
		return _player1Id;
	}

	public int getPlayer2Id()
	{
		return _player2Id;
	}

	public boolean getMaried()
	{
		return _married;
	}

	public Calendar getAffiancedDate()
	{
		return _affiancedDate;
	}

	public Calendar getWeddingDate()
	{
		return _weddingDate;
	}
}
