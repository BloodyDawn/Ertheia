package dwo.xmlrpcserver.XMLServices;

import com.google.gson.Gson;
import dwo.database.DatabaseUtils;
import dwo.database.FiltredPreparedStatement;
import dwo.database.ThreadConnection;
import org.apache.log4j.Logger;

import java.sql.ResultSet;

/**
 * Base for all XML-RPC services.
 *
 * @author Yorie
 * Date: 16.03.12
 */
abstract class Base
{
	protected static final Logger log = Logger.getLogger(Base.class);
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

	protected <T> String json(T data)
	{
		return jsonObject.toJson(data);
	}
}
