package dwo.loginserver;

import dwo.Server;
import dwo.config.Config;
import dwo.database.L2DatabaseFactory;
import dwo.status.Status;
import dwo.util.StackTrace;
import dwo.util.mmocore.SelectorConfig;
import dwo.util.mmocore.SelectorThread;
import dwo.xmlrpcserver.XMLRPCServer;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;

public class LoginServer
{
	public static final int PROTOCOL_REV = 0x0105;

	private Logger _log = LogManager.getLogger(LoginServer.class);
	private GameServerListener _gameServerListener;
	private SelectorThread<L2LoginClient> _selectorThread;
	private Status _statusServer;

	private LoginServer()
	{
		load();
	}

	public static LoginServer getInstance()
	{
		return SingletonHolder._instance;
	}

	private void load()
	{
		Server.serverMode = Server.MODE_LOGINSERVER;

		// Prepare Database
		try
		{
			L2DatabaseFactory.getInstance();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Failed initializing database. Reason: " + e.getMessage());
		}

		try
		{
			LoginController.load();
		}
		catch(GeneralSecurityException e)
		{
			_log.log(Level.ERROR, "Failed initializing LoginController. Reason: " + e.getMessage(), e);
			System.exit(1);
		}

		try
		{
			GameServerTable.load();
		}
		catch(GeneralSecurityException | SQLException e)
		{
			_log.log(Level.ERROR, "Failed to load GameServerTable. Reason: " + e.getMessage(), e);
			System.exit(1);
		}

		loadBanFile();

		InetAddress bindAddress = null;
		if(!Config.LOGIN_BIND_ADDRESS.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS);
			}
			catch(UnknownHostException e)
			{
				_log.log(Level.ERROR, "The LoginServer bind address is invalid, using all avaliable IPs. Reason: " + e.getMessage(), e);
			}
		}

		SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;

		L2LoginPacketHandler lph = new L2LoginPacketHandler();
		SelectorHelper sh = new SelectorHelper();
		try
		{
			_selectorThread = new SelectorThread<>(sc, sh, lph, sh, sh);
		}
		catch(IOException e)
		{
			_log.log(Level.ERROR, "Failed to open Selector. Reason: " + e.getMessage(), e);
			System.exit(1);
		}

		if(Config.IS_TELNET_ENABLED)
		{
			try
			{
				_statusServer = new Status(Server.serverMode);
				_statusServer.start();
			}
			catch(IOException e)
			{
				_log.log(Level.ERROR, "Failed to start the Telnet Server. Reason: " + e.getMessage(), e);
			}
		}
		else
		{
			_log.log(Level.INFO, "Telnet server is currently disabled.");
		}

		try
		{
			_gameServerListener = new GameServerListener();
			_gameServerListener.start();
			_log.log(Level.INFO, "Listening for GameServers on " + Config.GAME_SERVER_LOGIN_HOST + ':' + Config.GAME_SERVER_LOGIN_PORT);
		}
		catch(IOException e)
		{
			_log.log(Level.ERROR, "Failed to start the Game Server Listener. Reason: " + e.getMessage(), e);
			System.exit(1);
		}

		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_LOGIN);
		}
		catch(IOException e)
		{
			_log.log(Level.ERROR, "Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		_selectorThread.start();
		_log.log(Level.INFO, "Login Server ready on " + (bindAddress == null ? "*" : bindAddress.getHostAddress()) + ':' + Config.PORT_LOGIN);

		if(Config.XMLRPC_ENABLED)
		{
			XMLRPCServer.getInstance();
		}
	}

	private void loadBanFile()
	{
		File bannedFile = new File("./config/banned_ip.cfg");
		if(bannedFile.exists() && bannedFile.isFile())
		{
			FileInputStream fis;
			try
			{
				fis = new FileInputStream(bannedFile);
			}
			catch(FileNotFoundException e)
			{
				_log.log(Level.ERROR, "Failed to load banned IPs file (" + bannedFile.getName() + ") for reading. Reason: " + e.getMessage(), e);
				return;
			}

			LineNumberReader reader = null;
			String line;
			String[] parts;
			try
			{
				reader = new LineNumberReader(new InputStreamReader(fis));

				while((line = reader.readLine()) != null)
				{
					line = line.trim();
					// check if this line isnt a comment line
					if(!line.isEmpty() && line.charAt(0) != '#')
					{
						// split comments if any
						parts = line.split("#", 2);

						// discard comments in the line, if any
						line = parts[0];

						parts = line.split(" ");

						String address = parts[0];

						long duration = 0;

						if(parts.length > 1)
						{
							try
							{
								duration = Long.parseLong(parts[1]);
							}
							catch(NumberFormatException e)
							{
								_log.log(Level.ERROR, "Skipped: Incorrect ban duration (" + parts[1] + ") on (" + bannedFile.getName() + "). Line: " + reader.getLineNumber());
								continue;
							}
						}

						try
						{
							LoginController.getInstance().addBanForAddress(address, duration);
						}
						catch(UnknownHostException e)
						{
							_log.log(Level.ERROR, "Skipped: Invalid address (" + parts[0] + ") on (" + bannedFile.getName() + "). Line: " + reader.getLineNumber());
						}
					}
				}
			}
			catch(IOException e)
			{
				_log.log(Level.ERROR, "Error while reading the bans file (" + bannedFile.getName() + "). Details: " + e.getMessage(), e);
			}
			finally
			{
				try
				{
					reader.close();
				}
				catch(Exception e)
				{
					StackTrace.displayStackTraceInformation(e);
				}

				try
				{
					fis.close();
				}
				catch(Exception e)
				{
					StackTrace.displayStackTraceInformation(e);
				}
			}
			_log.log(Level.INFO, "Loaded " + LoginController.getInstance().getBannedIps().size() + " IP Bans.");
		}
		else
		{
			_log.log(Level.WARN, "IP Bans file (" + bannedFile.getName() + ") is missing or is a directory, skipped.");
		}
	}

	public GameServerListener getGameServerListener()
	{
		return _gameServerListener;
	}

	public void shutdown(boolean restart)
	{
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}

	public Status getStatusServer()
	{
		return _statusServer;
	}

	private static class SingletonHolder
	{
		protected static final LoginServer _instance = new LoginServer();
	}
}
