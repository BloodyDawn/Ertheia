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
package dwo.gameserver;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.datatables.xml.AdminTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.L2GameClient.GameClientState;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.LoginFail;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExLoginVitalityEffectInfo;
import dwo.gameserver.network.game.serverpackets.packet.lobby.CharacterSelectionInfo;
import dwo.gameserver.network.login.gameserverpackets.AuthRequest;
import dwo.gameserver.network.login.gameserverpackets.BlockAddress;
import dwo.gameserver.network.login.gameserverpackets.BlowFishKey;
import dwo.gameserver.network.login.gameserverpackets.ChangeAccessLevel;
import dwo.gameserver.network.login.gameserverpackets.PlayerAuthRequest;
import dwo.gameserver.network.login.gameserverpackets.PlayerInGame;
import dwo.gameserver.network.login.gameserverpackets.PlayerLogout;
import dwo.gameserver.network.login.gameserverpackets.PlayerTracert;
import dwo.gameserver.network.login.gameserverpackets.RegisterAccOrUpdate;
import dwo.gameserver.network.login.gameserverpackets.ReplyCharacters;
import dwo.gameserver.network.login.gameserverpackets.ServerStatus;
import dwo.gameserver.network.login.gameserverpackets.TempBan;
import dwo.gameserver.network.login.gameserverpackets.UnblockAddress;
import dwo.gameserver.network.login.loginserverpackets.AuthResponse;
import dwo.gameserver.network.login.loginserverpackets.BanInfo;
import dwo.gameserver.network.login.loginserverpackets.InitLS;
import dwo.gameserver.network.login.loginserverpackets.KickPlayer;
import dwo.gameserver.network.login.loginserverpackets.LoginServerFail;
import dwo.gameserver.network.login.loginserverpackets.PlayerAuthResponse;
import dwo.gameserver.network.login.loginserverpackets.RequestCharacters;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.crypt.NewCrypt;
import dwo.gameserver.util.database.DatabaseUtils;
import dwo.gameserver.util.network.BaseSendablePacket;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoginServerThread extends Thread
{
	protected static final Logger _log = LogManager.getLogger(LoginServerThread.class);

	private static final int REVISION = 0x0105;
	private final String _hostname;
	private final int _port;
	private final int _gamePort;
	private final boolean _acceptAlternate;
	private final boolean _reserveHost;
	private final List<WaitingClient> _waitingClients;
	private final FastMap<String, L2GameClient> _accountsInGameServer = new FastMap<>();
	private final String[] _subnets;
	private final String[] _hosts;
	private RSAPublicKey _publicKey;
	private Socket _loginSocket;
	private InputStream _in;
	private OutputStream _out;
	/**
	 * The BlowFish engine used to encrypt packets<br>
	 * It is first initialized with a unified key:<br>
	 * "_;v.]05-31!|+-%xT!^[$\00"<br>
	 * <br>
	 * and then after handshake, with a new key sent by<br>
	 * loginserver during the handshake. This new key is stored<br>
	 * in {@link #_blowfishKey}
	 */
	private NewCrypt _blowfish;
	private byte[] _blowfishKey;
	private byte[] _hexID;
	private int _requestID;
	private int _serverID;
	private int _maxPlayer;
	private int _status;
	private String _serverName;

	private LoginServerThread()
	{
		super("LoginServerThread");
		_port = Config.GAME_SERVER_LOGIN_PORT;
		_gamePort = Config.PORT_GAME;
		_hostname = Config.GAME_SERVER_LOGIN_HOST;
		_hexID = Config.HEX_ID;
		if(_hexID == null)
		{
			_requestID = Config.REQUEST_ID;
			_hexID = Util.generateHex(16);
		}
		else
		{
			_requestID = Config.SERVER_ID;
		}
		_acceptAlternate = Config.ACCEPT_ALTERNATE_ID;
		_reserveHost = Config.RESERVE_HOST_ON_LOGIN;
		_subnets = Config.GAME_SERVER_SUBNETS;
		_hosts = Config.GAME_SERVER_HOSTS;
		_waitingClients = new FastList<>();
		_accountsInGameServer.shared();
		_maxPlayer = Config.MAXIMUM_ONLINE_USERS;
	}

	public static LoginServerThread getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void run()
	{
		while(!isInterrupted())
		{
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			try
			{
				// ThreadConnection
				_log.log(Level.INFO, "Connecting to login on " + _hostname + ':' + _port);
				_loginSocket = new Socket(_hostname, _port);
				_in = _loginSocket.getInputStream();
				_out = new BufferedOutputStream(_loginSocket.getOutputStream());

				// init Blowfish
				_blowfishKey = Util.generateHex(40);
				_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
				while(!isInterrupted())
				{
					lengthLo = _in.read();
					lengthHi = _in.read();
					length = (lengthHi << 8) + lengthLo;

					if(lengthHi < 0)
					{
						_log.log(Level.INFO, "LoginServerThread: Login terminated the connection.");
						break;
					}

					byte[] incoming = new byte[length - 2];

					int receivedBytes = 0;
					int newBytes = 0;
					int left = length - 2;
					while(newBytes != -1 && receivedBytes < length - 2)
					{
						newBytes = _in.read(incoming, receivedBytes, left);
						receivedBytes += newBytes;
						left -= newBytes;
					}

					if(receivedBytes != length - 2)
					{
						_log.log(Level.WARN, "Incomplete Packet is sent to the server, closing connection.(LS)");
						break;
					}

					// decrypt if we have a key
					byte[] decrypt = _blowfish.decrypt(incoming);
					checksumOk = NewCrypt.verifyChecksum(decrypt);

					if(!checksumOk)
					{
						_log.log(Level.WARN, "Incorrect packet checksum, ignoring packet (LS)");
						break;
					}

					if(Config.DEBUG)
					{
						_log.log(Level.DEBUG, "[C]\n" + Util.printData(decrypt));
					}

					int packetType = decrypt[0] & 0xff;
					switch(packetType)
					{
						case 0x00:
							InitLS init = new InitLS(decrypt);
							if(Config.DEBUG)
							{
								_log.log(Level.DEBUG, "Init received");
							}
							if(init.getRevision() != REVISION)
							{
								// TODO: revision mismatch
								_log.log(Level.WARN, "/!\\ Revision mismatch between LS and GS /!\\");
								break;
							}
							try
							{
								KeyFactory kfac = KeyFactory.getInstance("RSA");
								BigInteger modulus = new BigInteger(init.getRSAKey());
								RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
								_publicKey = (RSAPublicKey) kfac.generatePublic(kspec1);
								if(Config.DEBUG)
								{
									_log.log(Level.DEBUG, "RSA key set up");
								}
							}

							catch(GeneralSecurityException e)
							{
								_log.log(Level.ERROR, "Troubles while init the public key send by login");
								break;
							}
							// send the blowfish key through the rsa encryption
							sendPacket(new BlowFishKey(_blowfishKey, _publicKey));
							if(Config.DEBUG)
							{
								_log.log(Level.DEBUG, "Sent new blowfish key");
							}
							// now, only accept paket with the new encryption
							_blowfish = new NewCrypt(_blowfishKey);
							if(Config.DEBUG)
							{
								_log.log(Level.DEBUG, "Changed blowfish key");
							}
							AuthRequest ar = new AuthRequest(_requestID, _acceptAlternate, _hexID, _gamePort, _reserveHost, _maxPlayer, _subnets, _hosts);
							sendPacket(ar);
							if(Config.DEBUG)
							{
								_log.log(Level.DEBUG, "Sent AuthRequest to login");
							}
							break;
						case 0x01:
							LoginServerFail lsf = new LoginServerFail(decrypt);
							_log.log(Level.INFO, "Damn! Registeration Failed: " + lsf.getReasonString());
							// login will close the ThreadConnection here
							break;
						case 0x02:
							AuthResponse aresp = new AuthResponse(decrypt);
							_serverID = aresp.getServerId();
							_serverName = aresp.getServerName();
							Config.saveHexid(_serverID, Util.hexToString(_hexID));
							_log.log(Level.INFO, "Registered on login as Server " + _serverID + " : " + _serverName);
							ServerStatus st = new ServerStatus();
							if(Config.SERVER_LIST_BRACKET)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.OFF);
							}
							st.addAttribute(ServerStatus.SERVER_TYPE, Config.SERVER_LIST_TYPE);
							if(Config.SERVER_GMONLY)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
							}
							if(Config.SERVER_LIST_AGE == 15)
							{
								st.addAttribute(ServerStatus.SERVER_AGE, ServerStatus.SERVER_AGE_15);
							}
							else if(Config.SERVER_LIST_AGE == 18)
							{
								st.addAttribute(ServerStatus.SERVER_AGE, ServerStatus.SERVER_AGE_18);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_AGE, ServerStatus.SERVER_AGE_ALL);
							}
							sendPacket(st);
							if(WorldManager.getInstance().getAllPlayersCount() > 0)
							{
								FastList<String> playerList = new FastList<>();
								for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
								{
									playerList.add(player.getAccountName());
								}
								sendPacket(new PlayerInGame(playerList));
							}
							break;
						case 0x03:
							PlayerAuthResponse par = new PlayerAuthResponse(decrypt);
							String account = par.getAccount();
							WaitingClient wcToRemove = null;
							synchronized(_waitingClients)
							{
								for(WaitingClient wc : _waitingClients)
								{
									if(wc.account.equals(account))
									{
										wcToRemove = wc;
									}
								}
							}
							if(wcToRemove != null)
							{
								if(par.isAuthed())
								{
									if(Config.DEBUG)
									{
										_log.log(Level.DEBUG, "Login accepted player " + wcToRemove.account + " waited(" + (GameTimeController.getInstance().getGameTicks() - wcToRemove.timestamp) + "ms)");
									}
									PlayerInGame pig = new PlayerInGame(par.getAccount());
									sendPacket(pig);
									wcToRemove.gameClient.setState(GameClientState.AUTHED);
									wcToRemove.gameClient.setSessionId(wcToRemove.session);
									CharacterSelectionInfo cl = new CharacterSelectionInfo(wcToRemove.account, wcToRemove.gameClient.getSessionId().playOkID1);
									wcToRemove.gameClient.getConnection().sendPacket(cl);
									wcToRemove.gameClient.getConnection().sendPacket(new ExLoginVitalityEffectInfo(wcToRemove.account));
									wcToRemove.gameClient.setCharSelection(cl.getCharInfo());
								}
								else
								{
									_log.log(Level.WARN, "Session key is not correct. Closing connection for account " + wcToRemove.account + '.');
									wcToRemove.gameClient.close(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
									_accountsInGameServer.remove(wcToRemove.account);
								}
								_waitingClients.remove(wcToRemove);
							}
							break;
						case 0x04:
							KickPlayer kp = new KickPlayer(decrypt);
							doKickPlayer(kp.getAccount());
							break;
						case 0x05:
							doAnnounceBanInfo(new BanInfo(decrypt));
							break;
						case 0x06:
							RequestCharacters rc = new RequestCharacters(decrypt);
							getCharsOnServer(rc.getAccount());
							break;
					}
				}
			}
			catch(UnknownHostException e)
			{
				_log.log(Level.ERROR, "LoginServer not available, unknown host...");
			}
			catch(SocketException e)
			{
				_log.log(Level.ERROR, "LoginServer not available, trying to reconnect...");
			}
			catch(IOException e)
			{
				_log.log(Level.ERROR, "Disconnected from Login, Trying to reconnect: " + e.getMessage(), e);
			}
			finally
			{
				try
				{
					if(_loginSocket != null)
					{
						_loginSocket.close();
					}
					if(isInterrupted())
					{
						return;
					}
				}
				catch(Exception e)
				{
					// Ignored
				}
			}

			try
			{
				Thread.sleep(5000); // 5 seconds tempo.
			}
			catch(InterruptedException e)
			{
				return; // never swallow an interrupt!
			}
		}
	}

	public void addWaitingClientAndSendRequest(String acc, L2GameClient client, SessionKey key)
	{
		synchronized(_waitingClients)
		{
			_waitingClients.add(new WaitingClient(acc, client, key));
		}
		try
		{
			sendPacket(new PlayerAuthRequest(acc, key));
		}
		catch(IOException e)
		{
			_log.log(Level.ERROR, "Error while sending player auth request", e);
		}
	}

	public void removeWaitingClient(L2GameClient client)
	{
		WaitingClient toRemove = null;
		synchronized(_waitingClients)
		{
			for(WaitingClient c : _waitingClients)
			{
				if(c.gameClient.equals(client))
				{
					toRemove = c;
				}
			}
			if(toRemove != null)
			{
				_waitingClients.remove(toRemove);
			}
		}
	}

	public void sendLogout(String account)
	{
		if(account == null)
		{
			return;
		}

		try
		{
			sendPacket(new PlayerLogout(account));
		}
		catch(IOException e)
		{
			_log.log(Level.ERROR, "Error while sending logout packet to login");
		}
		finally
		{
			_accountsInGameServer.remove(account);
		}
	}

	public void addGameServerLogin(String account, L2GameClient client)
	{
		_accountsInGameServer.put(account, client);
	}

	public void sendAccessLevel(String account, int level)
	{
		try
		{
			sendPacket(new ChangeAccessLevel(account, level));
		}
		catch(IOException e)
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "", e);
			}
		}
	}

	/**
	 * Запрос на регистрацию или апдейт аккаунта Логин серверу
	 * @param login логин
	 * @param password пароль
	 * @param level уровень доступа
	 */
	public void sendRegOrUpdate(String login, String password, int level)
	{
		try
		{
			sendPacket(new RegisterAccOrUpdate(login, password, level));
		}
		catch(IOException e)
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "", e);
			}
		}
	}

	public void sendClientTracert(String account, String[] adress)
	{
		try
		{
			sendPacket(new PlayerTracert(account, adress[0], adress[1], adress[2], adress[3], adress[4]));
		}
		catch(IOException e)
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "", e);
			}
		}
	}

	public void sendTempBan(String account, String ip, long time)
	{
		TempBan ban = new TempBan(account, ip, time);
		try
		{
			sendPacket(ban);
		}
		catch(IOException e)
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, e);
			}
		}
	}

	public void doKickPlayer(String account)
	{
		L2GameClient client = _accountsInGameServer.get(account);
		if(client != null)
		{
			client.setAditionalClosePacket(SystemMessage.getSystemMessage(SystemMessageId.ANOTHER_LOGIN_WITH_ACCOUNT));
			client.closeNow();
		}
	}

	/**
	 * @param sl
	 * @throws IOException
	 */
	private void sendPacket(BaseSendablePacket sl) throws IOException
	{
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "[S]\n" + Util.printData(data));
		}
		data = _blowfish.crypt(data);

		int len = data.length + 2;
		synchronized(_out) // avoids tow threads writing in the mean time
		{
			_out.write(len & 0xff);
			_out.write(len >> 8 & 0xff);
			_out.write(data);
			_out.flush();
		}
	}

	/**
	 * @return Returns the maxPlayer.
	 */
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}

	/**
	 * @param maxPlayer The maxPlayer to set.
	 */
	public void setMaxPlayer(int maxPlayer)
	{
		sendServerStatus(ServerStatus.MAX_PLAYERS, maxPlayer);
		_maxPlayer = maxPlayer;
	}

	public void sendServerStatus(int id, int value)
	{
		ServerStatus ss = new ServerStatus();
		ss.addAttribute(id, value);
		try
		{
			sendPacket(ss);
		}
		catch(IOException e)
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "", e);
			}
		}
	}

	/**
	 * Send Server Type Config to LS
	 */
	public void sendServerType()
	{
		ServerStatus ss = new ServerStatus();
		ss.addAttribute(ServerStatus.SERVER_TYPE, Config.SERVER_LIST_TYPE);
		try
		{
			sendPacket(ss);
		}
		catch(IOException e)
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "", e);
			}
		}
	}

	public String getStatusString()
	{
		return ServerStatus.STATUS_STRING[_status];
	}

	public boolean isBracketShown()
	{
		return Config.SERVER_LIST_BRACKET;
	}

	/**
	 * @return Returns the serverName.
	 */
	public String getServerName()
	{
		return _serverName;
	}

	public void setServerStatus(int status)
	{
		switch(status)
		{
			case ServerStatus.STATUS_AUTO:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
				_status = status;
				break;
			case ServerStatus.STATUS_DOWN:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_DOWN);
				_status = status;
				break;
			case ServerStatus.STATUS_FULL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_FULL);
				_status = status;
				break;
			case ServerStatus.STATUS_GM_ONLY:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
				_status = status;
				break;
			case ServerStatus.STATUS_GOOD:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GOOD);
				_status = status;
				break;
			case ServerStatus.STATUS_NORMAL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_NORMAL);
				_status = status;
				break;
			default:
				throw new IllegalArgumentException("Status does not exists:" + status);
		}
	}

	public void doAnnounceBanInfo(BanInfo info)
	{
		String message;

		switch(info.getStatus())
		{
			case ALREADY_BANNED:
				message = "IP " + info.getAddress() + " is already banned";
				break;
			case BAN_SUCCESSFUL:
				message = "IP " + info.getAddress() + " has been successfuly banned";
				break;
			case BAN_UNSUCCESSFUL:
				message = "Error while banning IP " + info.getAddress();
				break;
			case UNBAN_SUCCESSFUL:
				message = "IP " + info.getAddress() + " has been successfuly removed from banlist";
				break;
			case UNBAN_UNSUCCESSFUL:
				message = "Error while removing IP " + info.getAddress() + " from banlist";
				break;
			default:
				message = "Unexpected BanInfo answer!";
				break;
		}

		AdminTable.getInstance().broadcastMessageToGMs(message);
	}

	public void sendBlockAddress(String address, long expiration)
	{
		try
		{
			sendPacket(new BlockAddress(address, expiration));
		}
		catch(IOException e)
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, e);
			}
		}
	}

	public void sendUnblockAddress(String address)
	{
		try
		{
			sendPacket(new UnblockAddress(address));
		}
		catch(IOException e)
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, e);
			}
		}
	}

	private void getCharsOnServer(String account)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		int chars = 0;
		List<Long> charToDel = new ArrayList<>();
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_CHARACTERS_DELETE_TIME);
			statement.setString(1, account);
			rset = statement.executeQuery();
			while(rset.next())
			{
				chars++;
				long delTime = rset.getLong("deletetime");
				if(delTime != 0)
				{
					charToDel.add(delTime);
				}
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Exception: getCharsOnServer: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		ReplyCharacters rec = new ReplyCharacters(account, chars, charToDel);
		try
		{
			sendPacket(rec);
		}
		catch(IOException e)
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "", e);
			}
		}
	}

	public static class SessionKey
	{
		public int playOkID1;
		public int playOkID2;
		public int loginOkID1;
		public int loginOkID2;
		public int clientKey;

		public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
		{
			clientKey = -1;
			playOkID1 = playOK1;
			playOkID2 = playOK2;
			loginOkID1 = loginOK1;
			loginOkID2 = loginOK2;
		}

		@Override
		public String toString()
		{
			return "PlayOk: " + playOkID1 + ' ' + playOkID2 + " LoginOk:" + loginOkID1 + ' ' + loginOkID2;
		}
	}

	private static class WaitingClient
	{
		public int timestamp;
		public String account;
		public L2GameClient gameClient;
		public SessionKey session;

		public WaitingClient(String acc, L2GameClient client, SessionKey key)
		{
			account = acc;
			timestamp = GameTimeController.getInstance().getGameTicks();
			gameClient = client;
			session = key;
		}
	}

	private static class SingletonHolder
	{
		protected static final LoginServerThread _instance = new LoginServerThread();
	}
}