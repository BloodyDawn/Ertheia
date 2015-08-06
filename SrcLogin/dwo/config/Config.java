package dwo.config;

import dwo.util.L2Properties;
import dwo.util.StackTrace;
import dwo.util.Tools;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Config
{
	// MMO.ini
	public static final String MMO_CONFIG = "./config/main/MMO.ini";
	// LoginServer.ini
	public static final String LOGIN_SERVER_CONFIG = "./config/network/LoginServer.ini";
	// Telnet.ini
	public static final String TELNET_CONFIG = "./config/network/Telnet.ini";
	// Xmlrpc.ini
	public static final String XMLRPC_CONFIG = "./config/xmlrpc/server.ini";
	public static final String XMLRPC_SERVICES_CONFIG = "./config/xmlrpc/services.ini";
	protected static final Logger _log = LogManager.getLogger(Config.class);
	public static int MMO_SELECTOR_SLEEP_TIME;
	public static int MMO_MAX_SEND_PER_PASS;
	public static int MMO_MAX_READ_PER_PASS;
	public static int MMO_HELPER_BUFFER_COUNT;
	public static String LOGIN_BIND_ADDRESS;
	public static int PORT_LOGIN;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static boolean LOG_LOGIN_CONTROLLER;
	public static boolean SHOW_LICENCE;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	public static boolean DEBUG;
	public static String MYSQL_DB;
	public static String DATABASE_HOST;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MAX_CONNECTIONS;
	public static boolean USE_UTF8;
	public static boolean USE_DATABASE_LAYER;
	public static String GAME_SERVER_LOGIN_HOST;
	public static int GAME_SERVER_LOGIN_PORT;
	public static boolean USE_ONLY_CMD_AUTH;
	public static boolean USE_OTHER_HASH;
	public static String OTHER_PASSWORD_HASH;
	public static String[] OTHER_LEGACY_PASSWORD_HAS;
	public static boolean IS_TELNET_ENABLED;
	public static int STATUS_PORT;
	public static String STATUS_PW;
	public static String LIST_OF_HOSTS;
	public static boolean XMLRPC_ENABLED;
	public static String XMLRPC_HOST;
	public static int XMLRPC_PORT;

	public static void load()
	{
		Tools.printSection("Loading: Login Server");
		Tools.printSection("Network");

		_log.log(Level.INFO, "Loading: " + LOGIN_SERVER_CONFIG);
		try
		{
			L2Properties properties = new L2Properties(LOGIN_SERVER_CONFIG);
			GAME_SERVER_LOGIN_HOST = properties.getProperty("LoginHostname", "*");
			GAME_SERVER_LOGIN_PORT = Integer.parseInt(properties.getProperty("LoginPort", "9013"));
			LOGIN_BIND_ADDRESS = properties.getProperty("LoginserverHostname", "*");
			PORT_LOGIN = Integer.parseInt(properties.getProperty("LoginserverPort", "2106"));
			DEBUG = Boolean.parseBoolean(properties.getProperty("Debug", "false"));
			ACCEPT_NEW_GAMESERVER = Boolean.parseBoolean(properties.getProperty("AcceptNewGameServer", "true"));
			LOGIN_TRY_BEFORE_BAN = Integer.parseInt(properties.getProperty("LoginTryBeforeBan", "10"));
			LOGIN_BLOCK_AFTER_BAN = Integer.parseInt(properties.getProperty("LoginBlockAfterBan", "600"));
			LOG_LOGIN_CONTROLLER = Boolean.parseBoolean(properties.getProperty("LogLoginController", "true"));
			MYSQL_DB = properties.getProperty("LoginDbName", "l2god_login");
			DATABASE_HOST = properties.getProperty("LoginDbHost", "127.0.0.1");
			DATABASE_LOGIN = properties.getProperty("LoginDbUser", "root");
			DATABASE_PASSWORD = properties.getProperty("LoginDbPass", "root");
			DATABASE_MAX_CONNECTIONS = Integer.parseInt(properties.getProperty("LoginDbCon", "10"));
			USE_UTF8 = Boolean.parseBoolean(properties.getProperty("LoginDbUtf8", "true"));

			SHOW_LICENCE = Boolean.parseBoolean(properties.getProperty("ShowLicence", "false"));
			AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(properties.getProperty("AutoCreateAccounts", "true"));
			FLOOD_PROTECTION = Boolean.parseBoolean(properties.getProperty("EnableFloodProtection", "true"));
			FAST_CONNECTION_LIMIT = Integer.parseInt(properties.getProperty("FastConnectionLimit", "15"));
			NORMAL_CONNECTION_TIME = Integer.parseInt(properties.getProperty("NormalConnectionTime", "700"));
			FAST_CONNECTION_TIME = Integer.parseInt(properties.getProperty("FastConnectionTime", "350"));
			MAX_CONNECTION_PER_IP = Integer.parseInt(properties.getProperty("MaxConnectionPerIP", "50"));
			USE_ONLY_CMD_AUTH = Boolean.parseBoolean(properties.getProperty("UseOnlyCmdLogin", "false"));

			USE_OTHER_HASH = Boolean.parseBoolean(properties.getProperty("UseOtherHash", "false"));
			OTHER_PASSWORD_HASH = properties.getProperty("OtherPasswordHash", "whirlpool2");
			OTHER_LEGACY_PASSWORD_HAS = properties.getProperty("OtherLegacyPasswordHash", "sha1").split(";");
		}
		catch(Exception e)
		{
			StackTrace.displayStackTraceInformation(e);
			throw new Error("Failed to Load " + LOGIN_SERVER_CONFIG + " File.");
		}

		_log.log(Level.INFO, "Loading: " + TELNET_CONFIG);
		try
		{
			L2Properties properties = new L2Properties(TELNET_CONFIG);
			IS_TELNET_ENABLED = Boolean.parseBoolean(properties.getProperty("EnableTelnet", "False"));
			STATUS_PORT = Integer.parseInt(properties.getProperty("StatusPort", "12345"));
			STATUS_PW = properties.getProperty("StatusPW", "");
			LIST_OF_HOSTS = properties.getProperty("ListOfHosts", "127.0.0.1,localhost,::1");
		}
		catch(Exception e)
		{
			StackTrace.displayStackTraceInformation(e);
			throw new Error("Failed to Load " + TELNET_CONFIG + " File.");
		}

		Tools.printSection("Main");

		_log.log(Level.INFO, "Loading: " + MMO_CONFIG);
		try
		{
			L2Properties properties = new L2Properties(MMO_CONFIG);
			MMO_SELECTOR_SLEEP_TIME = Integer.parseInt(properties.getProperty("SleepTime", "20"));
			MMO_MAX_SEND_PER_PASS = Integer.parseInt(properties.getProperty("MaxSendPerPass", "12"));
			MMO_MAX_READ_PER_PASS = Integer.parseInt(properties.getProperty("MaxReadPerPass", "12"));
			MMO_HELPER_BUFFER_COUNT = Integer.parseInt(properties.getProperty("HelperBufferCount", "20"));
		}
		catch(Exception e)
		{
			StackTrace.displayStackTraceInformation(e);
			throw new Error("Failed to Load " + MMO_CONFIG + " File.");
		}

		Tools.printSection("Optional");

		_log.log(Level.INFO, "Loading: " + XMLRPC_CONFIG);
		try
		{
			L2Properties properties = new L2Properties(XMLRPC_CONFIG);
			XMLRPC_ENABLED = Boolean.parseBoolean(properties.getProperty("XmlRpcServerEnabled", "true"));
			XMLRPC_HOST = properties.getProperty("Host", "localhost");
			XMLRPC_PORT = Integer.parseInt(properties.getProperty("Port", "7000"));
		}
		catch(Exception e)
		{
			StackTrace.displayStackTraceInformation(e);
			throw new Error("Failed to Load " + XMLRPC_CONFIG + " File.");
		}
	}
}
