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

import dwo.config.Config;
import dwo.database.DatabaseUtils;
import dwo.database.FiltredPreparedStatement;
import dwo.database.L2DatabaseFactory;
import dwo.database.ThreadConnection;
import dwo.loginserver.BanList.BanStatus;
import dwo.loginserver.GameServerTable.GameServerInfo;
import dwo.loginserver.network.gameservercon.gameserverpackets.AddOrUpdateAccount;
import dwo.loginserver.network.gameservercon.gameserverpackets.BlockAddress;
import dwo.loginserver.network.gameservercon.gameserverpackets.BlowFishKey;
import dwo.loginserver.network.gameservercon.gameserverpackets.ChangeAccessLevel;
import dwo.loginserver.network.gameservercon.gameserverpackets.GameServerAuth;
import dwo.loginserver.network.gameservercon.gameserverpackets.PlayerAuthRequest;
import dwo.loginserver.network.gameservercon.gameserverpackets.PlayerInGame;
import dwo.loginserver.network.gameservercon.gameserverpackets.PlayerLogout;
import dwo.loginserver.network.gameservercon.gameserverpackets.PlayerTracert;
import dwo.loginserver.network.gameservercon.gameserverpackets.ReplyCharacters;
import dwo.loginserver.network.gameservercon.gameserverpackets.RequestTempBan;
import dwo.loginserver.network.gameservercon.gameserverpackets.ServerStatus;
import dwo.loginserver.network.gameservercon.gameserverpackets.UnblockAddress;
import dwo.loginserver.network.gameservercon.loginserverpackets.AuthResponse;
import dwo.loginserver.network.gameservercon.loginserverpackets.BanInfo;
import dwo.loginserver.network.gameservercon.loginserverpackets.InitLS;
import dwo.loginserver.network.gameservercon.loginserverpackets.KickPlayer;
import dwo.loginserver.network.gameservercon.loginserverpackets.LoginServerFail;
import dwo.loginserver.network.gameservercon.loginserverpackets.PlayerAuthResponse;
import dwo.loginserver.network.gameservercon.loginserverpackets.RequestCharacters;
import dwo.util.StackTrace;
import dwo.util.Util;
import dwo.util.crypt.NewCrypt;
import dwo.util.crypt.PasswordHash;
import dwo.util.network.BaseSendablePacket;
import javolution.util.FastSet;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author -Wooden-
 * @author KenM
 */

public class GameServerThread extends Thread
{
	protected static final Logger _log = LogManager.getLogger(GameServerThread.class);
	private Socket _connection;
	private InputStream _in;
	private OutputStream _out;
	private RSAPublicKey _publicKey;
	private RSAPrivateKey _privateKey;
	private NewCrypt _blowfish;
	private byte[] _blowfishKey;

	private String _connectionIp;

	private GameServerInfo _gsi;

	/** Authed Clients on a GameServer */
	private Set<String> _accountsOnGameServer = new FastSet<>();

	private String _connectionIPAddress;

	public GameServerThread(Socket con)
	{
		_connection = con;
		_connectionIp = con.getInetAddress().getHostAddress();
		try
		{
			_in = _connection.getInputStream();
			_out = new BufferedOutputStream(_connection.getOutputStream());
		}
		catch(IOException e)
		{
			StackTrace.displayStackTraceInformation(e);
		}
		KeyPair pair = GameServerTable.getInstance().getKeyPair();
		_privateKey = (RSAPrivateKey) pair.getPrivate();
		_publicKey = (RSAPublicKey) pair.getPublic();
		_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
		setName(getClass().getSimpleName() + '-' + getId() + '@' + _connectionIp);
		start();
	}

	/**
	 * @param ipAddress
	 * @return
	 */
	public static boolean isBannedGameserverIP(String ipAddress)
	{
		return false;
	}

	public static void onReceiveAddOrUpdateAccount(byte[] data)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			AddOrUpdateAccount _data = new AddOrUpdateAccount(data);
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE accounts (login, password, accessLevel) VALUES (?,?,?)");
			statement.setString(1, _data.getLogin());
			statement.setString(2, PasswordHash.encrypt(_data.getPassword()));
			statement.setString(3, String.valueOf(_data.getAccessLevel()));
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error during onReceiveAddOrUpdateAccount: " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	@Override
	public void run()
	{
		_connectionIPAddress = _connection.getInetAddress().getHostAddress();
		if(isBannedGameserverIP(_connectionIPAddress))
		{
			_log.log(Level.INFO, "GameServerRegistration: IP Address " + _connectionIPAddress + " is on Banned IP list.");
			forceClose(LoginServerFail.REASON_IP_BANNED);
			// ensure no further processing for this connection
			return;
		}

		InitLS startPacket = new InitLS(_publicKey.getModulus().toByteArray());
		try
		{
			sendPacket(startPacket);

			int lengthHi;
			int lengthLo;
			int length;
			boolean checksumOk;
			while(true)
			{
				lengthLo = _in.read();
				lengthHi = _in.read();
				length = (lengthHi << 8) + lengthLo;

				if(lengthHi < 0 || _connection.isClosed())
				{
					_log.log(Level.WARN, "LoginServerThread: Login terminated the connection.");
					break;
				}

				byte[] data = new byte[length - 2];

				int receivedBytes = 0;
				int newBytes = 0;
				int left = length - 2;
				while(newBytes != -1 && receivedBytes < length - 2)
				{
					newBytes = _in.read(data, receivedBytes, left);
					receivedBytes += newBytes;
					left -= newBytes;
				}

				if(receivedBytes != length - 2)
				{
					_log.log(Level.WARN, "Incomplete Packet is sent to the server, closing connection.(LS)");
					break;
				}

				// decrypt if we have a key
				data = _blowfish.decrypt(data);
				checksumOk = NewCrypt.verifyChecksum(data);
				if(!checksumOk)
				{
					_log.log(Level.WARN, "Incorrect packet checksum, closing connection (LS)");
					return;
				}

				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "[C]\n" + Util.printData(data));
				}

				int packetType = data[0] & 0xFF;
				switch(packetType)
				{
					case 0x00:
						onReceiveBlowfishKey(data);
						break;
					case 0x01:
						onGameServerAuth(data);
						break;
					case 0x02:
						onReceivePlayerInGame(data);
						break;
					case 0x03:
						onReceivePlayerLogOut(data);
						break;
					case 0x04:
						onReceiveChangeAccessLevel(data);
						break;
					case 0x05:
						onReceivePlayerAuthRequest(data);
						break;
					case 0x06:
						onReceiveServerStatus(data);
						break;
					case 0x07:
						onReceivePlayerTracert(data);
						break;
					case 0x08:
						onReceivePlayerOnServer(data);
						break;
					case 0x10:
						onReceiveBlockAddress(data);
						break;
					case 0x11:
						onReceiveUnblockAddress(data);
						break;
					case 0x0A:
						onReceiveTempBan(data);
						break;
					case 0x0B:
						onReceiveAddOrUpdateAccount(data);
						break;
					default:
						_log.log(Level.WARN, "Unknown Opcode (" + Integer.toHexString(packetType).toUpperCase() + ") from GameServer, closing connection.");
						forceClose(LoginServerFail.NOT_AUTHED);
				}
			}
		}
		catch(IOException e)
		{
			String serverName = getServerId() != -1 ? "[" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) : '(' + _connectionIPAddress + ')';
			String msg = "GameServer " + serverName + ": Connection lost: " + e.getMessage();
			_log.log(Level.ERROR, msg);
			broadcastToTelnet(msg);
		}
		finally
		{
			if(isAuthed())
			{
				_gsi.setDown();
				_log.log(Level.INFO, "Server [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) + " is now set as disconnected");
			}
			LoginServer.getInstance().getGameServerListener().removeGameServer(this);
			LoginServer.getInstance().getGameServerListener().removeFloodProtection(_connectionIp);
		}
	}

	private void onReceiveBlowfishKey(byte[] data)
	{
		BlowFishKey bfk = new BlowFishKey(data, _privateKey);
		_blowfishKey = bfk.getKey();
		_blowfish = new NewCrypt(_blowfishKey);
		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "New BlowFish key received, Blowfih Engine initialized:");
		}
	}

	private void onGameServerAuth(byte[] data) throws IOException
	{
		GameServerAuth gsa = new GameServerAuth(data);
		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "Auth request received");
		}
		handleRegProcess(gsa);
		if(isAuthed())
		{
			AuthResponse ar = new AuthResponse(_gsi.getId());
			sendPacket(ar);
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "Authed: id: " + _gsi.getId());
			}
			broadcastToTelnet("GameServer [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) + " is connected");
		}
	}

	private void onReceivePlayerInGame(byte[] data)
	{
		if(isAuthed())
		{
			PlayerInGame pig = new PlayerInGame(data);
			List<String> newAccounts = pig.getAccounts();
			for(String account : newAccounts)
			{
				_accountsOnGameServer.add(account);
				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "Account " + account + " logged in GameServer: [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()));
				}

				broadcastToTelnet("Account " + account + " logged in GameServer " + getServerId());
			}
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}

	private void onReceivePlayerLogOut(byte[] data)
	{
		if(isAuthed())
		{
			PlayerLogout plo = new PlayerLogout(data);
			_accountsOnGameServer.remove(plo.getAccount());
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "Player " + plo.getAccount() + " logged out from gameserver [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()));
			}

			broadcastToTelnet("Player " + plo.getAccount() + " disconnected from GameServer " + getServerId());
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}

	private void onReceiveChangeAccessLevel(byte[] data)
	{
		if(isAuthed())
		{
			ChangeAccessLevel cal = new ChangeAccessLevel(data);
			LoginController.getInstance().setAccountAccessLevel(cal.getAccount(), cal.getLevel());
			_log.log(Level.INFO, "Changed " + cal.getAccount() + " access level to " + cal.getLevel());
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}

	private void onReceivePlayerAuthRequest(byte[] data) throws IOException
	{
		if(isAuthed())
		{
			PlayerAuthRequest par = new PlayerAuthRequest(data);
			PlayerAuthResponse authResponse;
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "auth request received for Player " + par.getAccount());
			}
			SessionKey key = LoginController.getInstance().getKeyForAccount(par.getAccount());
			if(key != null && key.equals(par.getKey()))
			{
				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "auth request: OK");
				}
				LoginController.getInstance().removeAuthedLoginClient(par.getAccount());
				authResponse = new PlayerAuthResponse(par.getAccount(), true);
			}
			else
			{
				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "auth request: NO");
					_log.log(Level.DEBUG, "session key from self: " + key);
					_log.log(Level.DEBUG, "session key sent: " + par.getKey());
				}
				authResponse = new PlayerAuthResponse(par.getAccount(), false);
			}
			sendPacket(authResponse);
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}

	private void onReceiveServerStatus(byte[] data)
	{
		if(isAuthed())
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "ServerStatus received");
			}
			new ServerStatus(data, getServerId()); // will do the actions by itself
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}

	private void onReceivePlayerTracert(byte[] data)
	{
		if(isAuthed())
		{
			PlayerTracert plt = new PlayerTracert(data);
			LoginController.getInstance().setAccountLastTracert(plt.getAccount(), plt.getPcIp(), plt.getFirstHop(), plt.getSecondHop(), plt.getThirdHop(), plt.getFourthHop());
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "Saved " + plt.getAccount() + " last tracert");
			}
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}

	private void handleRegProcess(GameServerAuth gameServerAuth)
	{
		GameServerTable gameServerTable = GameServerTable.getInstance();

		int id = gameServerAuth.getDesiredID();
		byte[] hexId = gameServerAuth.getHexID();

		GameServerInfo gsi = gameServerTable.getRegisteredGameServerById(id);
		// is there a gameserver registered with this id?
		if(gsi != null)
		{
			// does the hex id match?
			if(Arrays.equals(gsi.getHexId(), hexId))
			{
				// check to see if this GS is already connected
				synchronized(gsi)
				{
					if(gsi.isAuthed())
					{
						forceClose(LoginServerFail.REASON_ALREADY_LOGGED8IN);
					}
					else
					{
						attachGameServerInfo(gsi, gameServerAuth);
					}
				}
			}
			else
			{
				// there is already a server registered with the desired id and
				// different hex id
				// try to register this one with an alternative id
				if(Config.ACCEPT_NEW_GAMESERVER && gameServerAuth.acceptAlternateID())
				{
					gsi = new GameServerInfo(id, hexId, this);
					if(gameServerTable.registerWithFirstAvaliableId(gsi))
					{
						attachGameServerInfo(gsi, gameServerAuth);
						gameServerTable.registerServerOnDB(gsi);
					}
					else
					{
						forceClose(LoginServerFail.REASON_NO_FREE_ID);
					}
				}
				else
				{
					// server id is already taken, and we cant get a new one for
					// you
					forceClose(LoginServerFail.REASON_WRONG_HEXID);
				}
			}
		}
		else
		{
			// can we register on this id?
			if(Config.ACCEPT_NEW_GAMESERVER)
			{
				gsi = new GameServerInfo(id, hexId, this);
				if(gameServerTable.register(id, gsi))
				{
					attachGameServerInfo(gsi, gameServerAuth);
					gameServerTable.registerServerOnDB(gsi);
				}
				else
				{
					// some one took this ID meanwhile
					forceClose(LoginServerFail.REASON_ID_RESERVED);
				}
			}
			else
			{
				forceClose(LoginServerFail.REASON_WRONG_HEXID);
			}
		}
	}

	public boolean hasAccountOnGameServer(String account)
	{
		return _accountsOnGameServer.contains(account);
	}

	public int getPlayerCount()
	{
		return _accountsOnGameServer.size();
	}

	/**
	 * Attachs a GameServerInfo to this Thread <li>Updates the GameServerInfo
	 * values based on GameServerAuth packet</li> <li><b>Sets the GameServerInfo
	 * as Authed</b></li>
	 *
	 * @param gsi
	 *            The GameServerInfo to be attached.
	 * @param gameServerAuth
	 *            The server info.
	 */
	private void attachGameServerInfo(GameServerInfo gsi, GameServerAuth gameServerAuth)
	{
		_gsi = gsi;
		gsi.setGameServerThread(this);
		gsi.setPort(gameServerAuth.getPort());
		setGameHosts(gameServerAuth.getHosts());
		gsi.setMaxPlayers(gameServerAuth.getMaxPlayers());
		gsi.setAuthed(true);
	}

	private void forceClose(int reason)
	{
		LoginServerFail lsf = new LoginServerFail(reason);
		try
		{
			sendPacket(lsf);
		}
		catch(IOException e)
		{
			_log.log(Level.ERROR, "GameServerThread: Failed kicking banned server. Reason: " + e.getMessage());
		}

		try
		{
			_connection.close();
		}
		catch(IOException e)
		{
			_log.log(Level.ERROR, "GameServerThread: Failed disconnecting banned server, server already disconnected.");
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
			_log.log(Level.DEBUG, "[S] " + sl.getClass().getSimpleName() + ":\n" + Util.printData(data));
		}
		data = _blowfish.crypt(data);

		int len = data.length + 2;
		synchronized(_out)
		{
			_out.write(len & 0xff);
			_out.write(len >> 8 & 0xff);
			_out.write(data);
			_out.flush();
		}
	}

	private void broadcastToTelnet(String msg)
	{
		if(LoginServer.getInstance().getStatusServer() != null)
		{
			LoginServer.getInstance().getStatusServer().sendMessageToTelnets(msg);
		}
	}

	public void kickPlayer(String account)
	{
		KickPlayer kp = new KickPlayer(account);
		try
		{
			sendPacket(kp);
		}
		catch(IOException e)
		{
			StackTrace.displayStackTraceInformation(e);
		}
	}

	/**
	 * @param hosts
	 *            The gameHost to set.
	 */
	public void setGameHosts(String[] hosts)
	{
		_log.log(Level.INFO, "Updated Gameserver [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) + " IP's:");

		_gsi.clearServerAddresses();
		for(int i = 0; i < hosts.length; i += 2)
		{
			try
			{
				_gsi.addServerAddress(hosts[i], hosts[i + 1]);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Couldn't resolve hostname \"" + e + '"');
			}
		}

		for(String s : _gsi.getServerAddresses())
		{
			_log.log(Level.INFO, s);
		}
	}

	/**
	 * @return Returns the isAuthed.
	 */
	public boolean isAuthed()
	{
		return _gsi != null && _gsi.isAuthed();
	}

	public GameServerInfo getGameServerInfo()
	{
		return _gsi;
	}

	public void setGameServerInfo(GameServerInfo gsi)
	{
		_gsi = gsi;
	}

	/**
	 * @return Returns the connectionIpAddress.
	 */
	public String getConnectionIpAddress()
	{
		return _connectionIPAddress;
	}

	private int getServerId()
	{
		if(_gsi != null)
		{
			return _gsi.getId();
		}
		return -1;
	}

	private void onReceiveBlockAddress(byte[] data)
	{
		if(isAuthed())
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "BlockAddress received");
			}

			BlockAddress ba = new BlockAddress(data);
			BanStatus status;

			try
			{
				if(LoginController.getInstance().isBannedAddress(ba.getAddress()))
				{
					status = BanStatus.ALREADY_BANNED;
				}

				else
				{
					status = LoginController.getInstance().addBanForAddress(ba.getAddress(), ba.getExpiration()) ? BanStatus.BAN_SUCCESSFUL : BanStatus.BAN_UNSUCCESSFUL;
				}

				sendPacket(new BanInfo(ba.getAddress(), status));
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error during banning IP " + ba.getAddress() + " :" + e);
			}
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}

	private void onReceiveUnblockAddress(byte[] data)
	{
		if(isAuthed())
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "UnblockAddress received");
			}

			BanStatus status;
			UnblockAddress ba = new UnblockAddress(data);

			status = LoginController.getInstance().removeBanForAddress(ba.getAddress()) ? BanStatus.UNBAN_SUCCESSFUL : BanStatus.UNBAN_UNSUCCESSFUL;

			try
			{
				sendPacket(new BanInfo(ba.getAddress(), status));
			}
			catch(IOException e)
			{
				_log.log(Level.ERROR, "Error during sending BanInfo: " + e);
			}
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}

	private void onReceiveTempBan(byte[] data)
	{
		RequestTempBan ban = new RequestTempBan(data);
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE accounts SET banTime=? WHERE login=?");
			statement.setString(1, Long.toString(ban.getBanTime()));
			statement.setString(2, ban.getAccountName());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error during onReceiveTempBan: " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		try
		{
			LoginController.getInstance().addBanForAddress(ban.getIp(), ban.getBanTime());
		}
		catch(UnknownHostException e)
		{
			_log.log(Level.ERROR, "Error during addBanForAddress: " + e);
		}
	}

	private void onReceivePlayerOnServer(byte[] data)
	{
		if(isAuthed())
		{
			ReplyCharacters rec = new ReplyCharacters(data);
			LoginController.getInstance().setCharactersOnServer(rec.getAccountName(), rec.getCharsOnServer(), rec.getTimeToDelForChars(), getServerId());
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}

	public void requestCharacters(String account)
	{
		RequestCharacters rc = new RequestCharacters(account);
		try
		{
			sendPacket(rc);
		}
		catch(IOException e)
		{
			_log.log(Level.ERROR, "Error during requestCharacters: " + e);
		}
	}
}
