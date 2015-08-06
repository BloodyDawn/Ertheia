package dwo.database;

import dwo.config.Config;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

public class L2DatabaseFactory
{
	static Logger _log = LogManager.getLogger(L2DatabaseFactory.class);
	private static L2DatabaseFactory _instance;
	private final Map<String, ThreadConnection> Connections = new Hashtable<>();
	private BasicDataSource _source;

	public L2DatabaseFactory(String url, String login, String pass, int poolSize, int idleTimeOut) throws SQLException
	{
		try
		{
			if(Config.DATABASE_MAX_CONNECTIONS < 2)
			{
				Config.DATABASE_MAX_CONNECTIONS = 2;
				_log.log(Level.WARN, "at least " + Config.DATABASE_MAX_CONNECTIONS + " db connections are required.");
			}

			Class.forName("com.mysql.jdbc.Driver").newInstance();

			_source = new BasicDataSource();
			_source.setDriverClassName("com.mysql.jdbc.Driver"); //loads the jdbc driver
			_source.setUrl(url);
			_source.setUsername(login);
			_source.setPassword(pass); // the settings below are optional
			_source.setDefaultAutoCommit(true);
			_source.setInitialSize(3);
			_source.setMinIdle(1);
			_source.setMaxIdle(30);
			_source.setMaxWait(-1);
			_source.setMaxActive(-1);
			_source.setValidationQuery("SELECT 1");
			_source.setRemoveAbandoned(true);
			_source.setRemoveAbandonedTimeout(120);
			_source.setTimeBetweenEvictionRunsMillis(30000);
			_source.setMaxActive(poolSize);
			_source.setMaxOpenPreparedStatements(100);
			_source.setTestWhileIdle(true); // test idle connection every 1 minute
			_source.setMaxIdle(idleTimeOut); // remove unused connection after 10 minutes

			/* Test the connection */
			_source.getConnection().close();
		}
		catch(SQLException x)
		{
			// rethrow the exception
			throw x;
		}
		catch(Exception e)
		{
			throw new SQLException("could not init DB connection:" + e);
		}
	}

	public L2DatabaseFactory() throws SQLException
	{
		this(GetUrl(), Config.DATABASE_LOGIN, Config.DATABASE_PASSWORD, Config.DATABASE_MAX_CONNECTIONS, 600);
	}

	public static String GetUrl()
	{
		String mysql;
		String USE_UTF8 = "";
		if(Config.USE_UTF8)
		{
			USE_UTF8 = "?useUnicode=true&characterEncoding=utf-8";
		}
		mysql = "jdbc:mysql://" + Config.DATABASE_HOST + '/' + Config.MYSQL_DB + USE_UTF8;
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
		ThreadConnection connection;
		if(Config.USE_DATABASE_LAYER)
		{
			String key = generateKey();
			//Пробуем получить коннект из списка уже используемых. Если для данного потока уже открыт
			//коннект - не мучаем пул коннектов, а отдаем этот коннект.
			connection = Connections.get(key);
			if(connection == null)
			{
				try
				{
					//не нашли - открываем новый
					connection = new ThreadConnection(_source.getConnection(), this);
				}
				catch(SQLException e)
				{
					_log.log(Level.WARN, "Couldn't create connection. Cause: " + e.getMessage());
				}
			}
			else
			//нашли - увеличиваем счетчик использования
			{
				connection.updateCounter();
			}

			//добавляем коннект в список
			if(connection != null)
			{
				synchronized(Connections)
				{
					Connections.put(key, connection);
				}
			}
		}
		else
		{
			connection = new ThreadConnection(_source.getConnection(), this);
		}
		return connection;
	}

	public int getBusyConnectionCount()
	{
		return _source.getNumActive();
	}

	public int getIdleConnectionCount()
	{
		return _source.getNumIdle();
	}

	public Map<String, ThreadConnection> getConnections()
	{
		return Connections;
	}

	public void shutdown()
	{
		try
		{
			_source.close();
		}
		catch(SQLException e)
		{
			_log.log(Level.INFO, "", e);
		}
		Connections.clear();
	}

	/**
	 * Генерация ключа для хранения коннекта
	 *
	 * Ключ равен хэш-коду текущего потока
	 *
	 * @return сгенерированный ключ.
	 */
	public String generateKey()
	{
		return String.valueOf(Thread.currentThread().hashCode());
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