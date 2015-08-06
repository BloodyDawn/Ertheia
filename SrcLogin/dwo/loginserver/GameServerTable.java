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
package dwo.loginserver;

import dwo.database.DatabaseUtils;
import dwo.database.FiltredPreparedStatement;
import dwo.database.L2DatabaseFactory;
import dwo.database.ThreadConnection;
import dwo.loginserver.network.gameservercon.gameserverpackets.ServerStatus;
import dwo.util.IPSubnet;
import dwo.util.Rnd;
import dwo.util.StackTrace;
import javolution.io.UTF8StreamReader;
import javolution.util.FastMap;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReaderImpl;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author KenM
 */

public class GameServerTable
{
	// RSA Config
	private static final int KEYS_SIZE = 10;
	private static Logger _log = LogManager.getLogger(GameServerTable.class);
	private static GameServerTable _instance;
	// Server Names Config
	private static Map<Integer, String> _serverNames = new FastMap<>();
	// Game Server Table
	private final Map<Integer, GameServerInfo> _gameServerTable = new FastMap<Integer, GameServerInfo>().shared();
	private KeyPair[] _keyPairs;

	public GameServerTable() throws SQLException, NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		loadServerNames();
		_log.log(Level.INFO, "Loaded " + _serverNames.size() + " server names");

		loadRegisteredGameServers();
		_log.log(Level.INFO, "Loaded " + _gameServerTable.size() + " registered Game Servers");

		loadRSAKeys();
		_log.log(Level.INFO, "Cached " + _keyPairs.length + " RSA keys for Game Server communication.");
	}

	public static void load() throws SQLException, GeneralSecurityException
	{
		synchronized(GameServerTable.class)
		{
			if(_instance == null)
			{
				_instance = new GameServerTable();
			}
			else
			{
				throw new IllegalStateException("Load can only be invoked a single time.");
			}
		}
	}

	public static GameServerTable getInstance()
	{
		return _instance;
	}

	private void loadRSAKeys() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4);
		keyGen.initialize(spec);

		_keyPairs = new KeyPair[KEYS_SIZE];
		for(int i = 0; i < KEYS_SIZE; i++)
		{
			_keyPairs[i] = keyGen.genKeyPair();
		}
	}

	private void loadServerNames()
	{
		InputStream in = null;
		try
		{
			in = new FileInputStream(new File(".", "config/servername.xml"));
			XMLStreamReaderImpl xpp = new XMLStreamReaderImpl();
			xpp.setInput(new UTF8StreamReader().setInput(in));
			for(int e = xpp.getEventType(); e != XMLStreamConstants.END_DOCUMENT; e = xpp.next())
			{
				if(e == XMLStreamConstants.START_ELEMENT)
				{
					if(xpp.getLocalName().toString().equals("server"))
					{
						Integer id = Integer.valueOf(xpp.getAttributeValue(null, "id").toString());
						String name = xpp.getAttributeValue(null, "name").toString();
						_serverNames.put(id, name);
					}
				}
			}
		}
		catch(FileNotFoundException e)
		{
			_log.log(Level.WARN, "servername.xml could not be loaded: file not found");
		}
		catch(XMLStreamException e)
		{
			StackTrace.displayStackTraceInformation(e);
		}
		finally
		{
			try
			{
				in.close();
			}
			catch(Exception e)
			{
			}
		}
	}

	private void loadRegisteredGameServers() throws SQLException
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		int id;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM gameservers");
			rset = statement.executeQuery();
			GameServerInfo gsi;
			while(rset.next())
			{
				id = rset.getInt("server_id");
				gsi = new GameServerInfo(id, stringToHex(rset.getString("hexid")));
				_gameServerTable.put(id, gsi);
			}
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public Map<Integer, GameServerInfo> getRegisteredGameServers()
	{
		return _gameServerTable;
	}

	public GameServerInfo getRegisteredGameServerById(int id)
	{
		return _gameServerTable.get(id);
	}

	public boolean hasRegisteredGameServerOnId(int id)
	{
		return _gameServerTable.containsKey(id);
	}

	public boolean registerWithFirstAvaliableId(GameServerInfo gsi)
	{
		// avoid two servers registering with the same "free" id
		synchronized(_gameServerTable)
		{
			for(Entry<Integer, String> entry : _serverNames.entrySet())
			{
				if(!_gameServerTable.containsKey(entry.getKey()))
				{
					_gameServerTable.put(entry.getKey(), gsi);
					gsi.setId(entry.getKey());
					return true;
				}
			}
		}
		return false;
	}

	public boolean register(int id, GameServerInfo gsi)
	{
		// avoid two servers registering with the same id
		synchronized(_gameServerTable)
		{
			if(!_gameServerTable.containsKey(id))
			{
				_gameServerTable.put(id, gsi);
				gsi.setId(id);
				return true;
			}
		}
		return false;
	}

	public void registerServerOnDB(GameServerInfo gsi)
	{
		registerServerOnDB(gsi.getHexId(), gsi.getId(), gsi.getExternalHost());
	}

	public void registerServerOnDB(byte[] hexId, int id, String externalHost)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)");
			statement.setString(1, hexToString(hexId));
			statement.setInt(2, id);
			statement.setString(3, externalHost);
			statement.executeUpdate();

			register(id, new GameServerInfo(id, hexId));
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "SQL error while saving gameserver.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public String getServerNameById(int id)
	{
		return _serverNames.get(id);
	}

	public Map<Integer, String> getServerNames()
	{
		return _serverNames;
	}

	public KeyPair getKeyPair()
	{
		return _keyPairs[Rnd.nextInt(10)];
	}

	private byte[] stringToHex(String string)
	{
		return new BigInteger(string, 16).toByteArray();
	}

	private String hexToString(byte[] hex)
	{
		if(hex == null)
		{
			return "null";
		}
		return new BigInteger(hex).toString(16);
	}

	public static class GameServerInfo
	{
		// auth
		private int _id;
		private byte[] _hexId;
		private boolean _isAuthed;

		// status
		private GameServerThread _gst;
		private int _status;

		// network
		private List<GameServerAddress> _addrs = new ArrayList<>(5);
		private int _port;

		// config
		private boolean _isPvp = true;
		private int _serverType;
		private int _ageLimit;
		private boolean _isShowingBrackets;
		private int _maxPlayers;

		public GameServerInfo(int id, byte[] hexId, GameServerThread gst)
		{
			_id = id;
			_hexId = hexId;
			_gst = gst;
			_status = ServerStatus.STATUS_DOWN;
		}

		public GameServerInfo(int id, byte[] hexId)
		{
			this(id, hexId, null);
		}

		public int getId()
		{
			return _id;
		}

		public void setId(int id)
		{
			_id = id;
		}

		public byte[] getHexId()
		{
			return _hexId;
		}

		public boolean isAuthed()
		{
			return _isAuthed;
		}

		public void setAuthed(boolean isAuthed)
		{
			_isAuthed = isAuthed;
		}

		public GameServerThread getGameServerThread()
		{
			return _gst;
		}

		public void setGameServerThread(GameServerThread gst)
		{
			_gst = gst;
		}

		public int getStatus()
		{
			return _status;
		}

		public void setStatus(int status)
		{
			_status = status;
		}

		public int getCurrentPlayerCount()
		{
			if(_gst == null)
			{
				return 0;
			}
			return _gst.getPlayerCount();
		}

		public String getExternalHost()
		{
			try
			{
				return getServerAddress(InetAddress.getByName("0.0.0.0"));
			}
			catch(Exception e)
			{

			}
			return null;
		}

		public int getPort()
		{
			return _port;
		}

		public void setPort(int port)
		{
			_port = port;
		}

		public int getMaxPlayers()
		{
			return _maxPlayers;
		}

		public void setMaxPlayers(int maxPlayers)
		{
			_maxPlayers = maxPlayers;
		}

		public boolean isPvp()
		{
			return _isPvp;
		}

		public int getAgeLimit()
		{
			return _ageLimit;
		}

		public void setAgeLimit(int val)
		{
			_ageLimit = val;
		}

		public int getServerType()
		{
			return _serverType;
		}

		public void setServerType(int val)
		{
			_serverType = val;
		}

		public boolean isShowingBrackets()
		{
			return _isShowingBrackets;
		}

		public void setShowingBrackets(boolean val)
		{
			_isShowingBrackets = val;
		}

		public void setDown()
		{
			_isAuthed = false;
			_port = 0;
			_gst = null;
			_status = ServerStatus.STATUS_DOWN;
		}

		public void addServerAddress(String subnet, String addr) throws UnknownHostException
		{
			_addrs.add(new GameServerAddress(subnet, addr));
		}

		public String getServerAddress(InetAddress addr)
		{
			for(GameServerAddress a : _addrs)
			{
				if(a.equals(addr))
				{
					return a.getServerAddress();
				}
			}
			return null; // should not happens
		}

		public String[] getServerAddresses()
		{
			String[] result = new String[_addrs.size()];
			for(int i = 0; i < result.length; i++)
			{
				result[i] = _addrs.get(i).toString();
			}

			return result;
		}

		public void clearServerAddresses()
		{
			_addrs.clear();
		}

		private class GameServerAddress extends IPSubnet
		{
			private String _serverAddress;

			public GameServerAddress(String subnet, String address) throws UnknownHostException
			{
				super(subnet);
				_serverAddress = address;
			}

			public String getServerAddress()
			{
				return _serverAddress;
			}

			@Override
			public String toString()
			{
				return _serverAddress + super.toString();
			}
		}
	}
}
