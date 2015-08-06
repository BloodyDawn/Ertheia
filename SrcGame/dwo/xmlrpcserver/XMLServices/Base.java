package dwo.xmlrpcserver.XMLServices;

import com.google.gson.Gson;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;

/**
 * L2GOD Team
 * User: Yorie
 * Date: 16.03.12
 * Time: xx:xx
 */

abstract class Base
{
	protected static final Logger log = LogManager.getLogger(Base.class);
	protected static final Logger logDonate = LogManager.getLogger("donate");

	protected final Gson jsonObject;
	protected ThreadConnection conn;
	protected FiltredPreparedStatement statement;
	protected ResultSet resultSet;

	protected Base()
	{
		jsonObject = new Gson();
	}

	protected void databaseClose(boolean closeResultSet)
	{
		if(closeResultSet)
		{
			DatabaseUtils.closeDatabaseCSR(conn, statement, resultSet);
		}
		else
		{
			DatabaseUtils.closeDatabaseCS(conn, statement);
		}
	}

	public <T> String json(T data)
	{
		return jsonObject.toJson(data);
	}
}
