package dwo.gameserver.engine.databaseengine;

import dwo.config.Config;
import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * <p>При работе с пулами коннектов иногда возникает ситуация - когда выбираешь весь пул до предела и
 * при этом коннекты не закрываются а требуется получить еще один коннект. В этом случае программа
 * зависает. Так бывает если в процессе выполнения одного запроса при переборке результатов вызывается
 * другая функция, которая также берет коннект из базы данных. Таких вложений может быть много. И коннекты
 * не отпускаются, пока не выполнятся самые глубокие запросы. DBCP и C3P0 висли при этом - опробовано на
 * практике.
 * </p>
 * <p>Для того чтобы избежать этой коллизии пишется оболочка для коннекта, которой коннект
 * делегирует все свои методы. Эта оболочка хранится в локальном пуле коннектов и если коннект запрашивается
 * в потоке - для которого был уже открыт коннект и еще не закрыт, то возвращаем его.
 * </p>
 * Эту возможность можно отключить выставив в настройках сервера UseDatabaseLayer = false;
 */

public class L2DatabaseFactory
{
    private static final Logger _log = LogManager.getLogger(L2DatabaseFactory.class);
    private static L2DatabaseFactory _instance;
    private SharedPoolDataSource _source;

	public L2DatabaseFactory() throws SQLException
	{
		try
		{
			if(Config.DATABASE_MAX_CONNECTIONS < 2)
			{
				Config.DATABASE_MAX_CONNECTIONS = 2;
				_log.log(Level.WARN, "at least " + Config.DATABASE_MAX_CONNECTIONS + " db connections are required.");
			}

            DriverAdapterCPDS adapterCPDS = new DriverAdapterCPDS();
            adapterCPDS.setDriver("com.mysql.jdbc.Driver");
            adapterCPDS.setUrl(getDatabaseUrl());
            adapterCPDS.setUser(Config.DATABASE_LOGIN);
            adapterCPDS.setPassword(Config.DATABASE_PASSWORD);

            _source = new SharedPoolDataSource();
            _source.setConnectionPoolDataSource(adapterCPDS);
            _source.setMaxActive(Config.DATABASE_MAX_CONNECTIONS);
            _source.setMaxIdle(Config.DATABASE_MAX_CONNECTIONS);
            _source.setNumTestsPerEvictionRun(Config.DATABASE_MAX_CONNECTIONS);
            _source.setTimeBetweenEvictionRunsMillis((int) Config.CONNECTION_CLOSE_TIME);
            _source.setMinEvictableIdleTimeMillis(((int) Config.CONNECTION_CLOSE_TIME));

			/* Test the connection */
			_source.getConnection().close();
		}
		catch(Exception e)
		{
            _log.error("could not init DB connection:", e);
        }
    }

    public static String getDatabaseUrl()
    {
        String mysql;
        String USE_UTF8 = "";
        if (Config.USE_UTF8)
        {
            USE_UTF8 = "?useUnicode=true&characterEncoding=utf-8";
        }
        mysql = "jdbc:mysql://" + Config.DATABASE_HOST + ':' + Config.DATABASE_PORT + '/' + Config.MYSQL_DB + USE_UTF8;
        return mysql;
    }

	public static L2DatabaseFactory getInstance() throws SQLException
	{
		if(_instance == null)
		{
			_instance = new L2DatabaseFactory();
		}
		return _instance;
	}

	public ThreadConnection getConnection() throws SQLException
	{
        return new ThreadConnection(_source.getConnection());
    }

	public void shutdown()
	{
		try
		{
            _source.close();
        }
        catch (Exception e)
        {
            _log.log(Level.ERROR, "", e);
        }
    }

	public String prepQuerySelect(String[] fields, String tableName, String whereClause, boolean returnOnlyTopRecord)
	{
		String msSqlTop1 = "";
		String mySqlTop1 = "";
		if(returnOnlyTopRecord)
		{
			mySqlTop1 = " Limit 1 ";
		}
		return "SELECT " + msSqlTop1 + safetyString(fields) + " FROM " + tableName + " WHERE " + whereClause + mySqlTop1;
	}

	public String safetyString(String... whatToCheck)
	{
		// NOTE: Use brace as a safty precaution just incase name is a reserved word
		char braceLeft;
		char braceRight;

		braceLeft = '`';
		braceRight = '`';

		int length = 0;

		for(String word : whatToCheck)
		{
			length += word.length() + 4;
		}

		StringBuilder sbResult = new StringBuilder(length);

		for(String word : whatToCheck)
		{
			if(sbResult.length() > 0)
			{
				sbResult.append(", ");
			}

			sbResult.append(braceLeft);
			sbResult.append(word);
			sbResult.append(braceRight);
		}

		return sbResult.toString();
	}
}