package dwo.gameserver.engine.databaseengine.idfactory;

import dwo.config.Config;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;

public class StackIDFactory extends IdFactory
{
	private int _curOID;
	private int _tempOID;

	private Stack<Integer> _freeOIDStack = new Stack<>();

	protected StackIDFactory()
	{
		_curOID = FIRST_OID;
		_tempOID = FIRST_OID;

		ThreadConnection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			//con.createStatement().execute("drop table if exists tmp_obj_id");

			int[] tmp_obj_ids = extractUsedObjectIDTable();
			if(tmp_obj_ids.length > 0)
			{
				_curOID = tmp_obj_ids[tmp_obj_ids.length - 1];
			}
			_log.log(Level.INFO, "Max Id = " + _curOID);

			int N = tmp_obj_ids.length;
			for(int idx = 0; idx < N; idx++)
			{
				N = insertUntil(tmp_obj_ids, idx, N, con);
			}

			_curOID++;
			_log.log(Level.INFO, "IdFactory: Next usable Object ID is: " + _curOID);
			_initialized = true;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "ID Factory could not be initialized correctly:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeConnection(con);
		}
	}

	public static IdFactory getInstance()
	{
		return _instance;
	}

	private int insertUntil(int[] tmp_obj_ids, int idx, int N, ThreadConnection con) throws SQLException
	{
		int id = tmp_obj_ids[idx];
		if(id == _tempOID)
		{
			_tempOID++;
			return N;
		}
		// check these IDs not present in DB
		if(Config.BAD_ID_CHECKING)
		{
			for(String check : ID_CHECKS)
			{
				FiltredPreparedStatement ps = con.prepareStatement(check);
				ps.setInt(1, _tempOID);
				//ps.setInt(1, _curOID);
				ps.setInt(2, id);
				ResultSet rs = ps.executeQuery();
				while(rs.next())
				{
					int badId = rs.getInt(1);
					_log.log(Level.ERROR, "Bad ID " + badId + " in DB found by: " + check);
					throw new RuntimeException();
				}
				rs.close();
				ps.close();
			}
		}

		//int hole = id - _curOID;
		int hole = id - _tempOID;
		if(hole > N - idx)
		{
			hole = N - idx;
		}
		for(int i = 1; i <= hole; i++)
		{
			//log.log(Level.INFO, "Free ID added " + (_tempOID));
			_freeOIDStack.push(_tempOID);
			_tempOID++;
			//_curOID++;
		}
		if(hole < N - idx)
		{
			_tempOID++;
		}
		return N - hole;
	}

	@Override
	public int getNextId()
	{
		synchronized(this)
		{
			int id;
			if(_freeOIDStack.empty())
			{
				id = _curOID;
				_curOID += 1;
			}
			else
			{
				id = _freeOIDStack.pop();
			}
			return id;
		}
	}

	/**
	 * return a used Object ID back to the pool
	 *
	 * @param id object ID
	 */
	@Override
	public void releaseId(int id)
	{
		synchronized(this)
		{
			_freeOIDStack.push(id);
		}
	}

	@Override
	public int size()
	{
		return FREE_OBJECT_ID_SIZE - _curOID + FIRST_OID + _freeOIDStack.size();
	}
}