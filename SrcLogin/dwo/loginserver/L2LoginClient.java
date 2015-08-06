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

import dwo.loginserver.network.loginclientcon.serverpackets.L2LoginServerPacket;
import dwo.loginserver.network.loginclientcon.serverpackets.LoginFail;
import dwo.loginserver.network.loginclientcon.serverpackets.LoginFail.LoginFailReason;
import dwo.loginserver.network.loginclientcon.serverpackets.PlayFail;
import dwo.loginserver.network.loginclientcon.serverpackets.PlayFail.PlayFailReason;
import dwo.util.Rnd;
import dwo.util.StackTrace;
import dwo.util.crypt.LoginCrypt;
import dwo.util.crypt.ScrambledKeyPair;
import dwo.util.mmocore.MMOClient;
import dwo.util.mmocore.MMOConnection;
import dwo.util.mmocore.SendablePacket;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a client connected into the LoginServer
 *
 * @author KenM
 */
public class L2LoginClient extends MMOClient<MMOConnection<L2LoginClient>>
{
	private static Logger _log = LogManager.getLogger(L2LoginClient.class);
	private LoginClientState _state;
	// Crypt
	private LoginCrypt _loginCrypt;
	private ScrambledKeyPair _scrambledPair;
	private byte[] _blowfishKey;
	private String _account;
	private int _accessLevel;
	private int _lastServer;
	private SessionKey _sessionKey;
	private int _sessionId;
	private boolean _joinedGS;
	private Map<Integer, Integer> _charsOnServers;
	private Map<Integer, long[]> _charsToDelete;
	private long _connectionStartTime;

	/**
	 * @param con
	 */
	public L2LoginClient(MMOConnection<L2LoginClient> con)
	{
		super(con);
		_state = LoginClientState.CONNECTED;
		_scrambledPair = LoginController.getInstance().getScrambledRSAKeyPair();
		_blowfishKey = LoginController.getInstance().getBlowfishKey();
		_sessionId = Rnd.nextInt();
		_connectionStartTime = System.currentTimeMillis();
		_loginCrypt = new LoginCrypt();
		_loginCrypt.setKey(_blowfishKey);
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		boolean ret;
		try
		{
			ret = _loginCrypt.decrypt(buf.array(), buf.position(), size);
		}
		catch(IOException e)
		{
			StackTrace.displayStackTraceInformation(e);
			getConnection().close((SendablePacket<L2LoginClient>) null);
			return false;
		}

        /** 603 protocol version */
		/*if(!ret)
		{
			byte[] dump = new byte[size];
			System.arraycopy(buf.array(), buf.position(), dump, 0, size);
			_log.log(Level.WARN, "Wrong checksum from client: " + this);
			getConnection().close((SendablePacket<L2LoginClient>) null);
		}*/
		return ret;
	}

	@Override
	public boolean encrypt(ByteBuffer buf, int size)
	{
		int offset = buf.position();
		try
		{
			size = _loginCrypt.encrypt(buf.array(), offset, size);
		}
		catch(IOException e)
		{
			_log.log(Level.WARN, "Error during encrypt: " + e);
			return false;
		}

		buf.position(offset + size);
		return true;
	}

	@Override
	public void onDisconnection()
	{
		if(!hasJoinedGS() || _connectionStartTime + LoginController.LOGIN_TIMEOUT < System.currentTimeMillis())
		{
			LoginController.getInstance().removeAuthedLoginClient(_account);
		}
	}

	@Override
	protected void onForcedDisconnection()
	{
		// Empty
	}

	public LoginClientState getState()
	{
		return _state;
	}

	public void setState(LoginClientState state)
	{
		_state = state;
	}

	public byte[] getBlowfishKey()
	{
		return _blowfishKey;
	}

	public byte[] getScrambledModulus()
	{
		return _scrambledPair._scrambledModulus;
	}

	public RSAPrivateKey getRSAPrivateKey()
	{
		return (RSAPrivateKey) _scrambledPair._pair.getPrivate();
	}

	public String getAccount()
	{
		return _account;
	}

	public void setAccount(String account)
	{
		_account = account;
	}

	public int getAccessLevel()
	{
		return _accessLevel;
	}

	public void setAccessLevel(int accessLevel)
	{
		_accessLevel = accessLevel;
	}

	public int getLastServer()
	{
		return _lastServer;
	}

	public void setLastServer(int lastServer)
	{
		_lastServer = lastServer;
	}

	public int getSessionId()
	{
		return _sessionId;
	}

	public boolean hasJoinedGS()
	{
		return _joinedGS;
	}

	public void setJoinedGS(boolean val)
	{
		_joinedGS = val;
	}

	public SessionKey getSessionKey()
	{
		return _sessionKey;
	}

	public void setSessionKey(SessionKey sessionKey)
	{
		_sessionKey = sessionKey;
	}

	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}

	public void sendPacket(L2LoginServerPacket lsp)
	{
		getConnection().sendPacket(lsp);
	}

	public void close(LoginFailReason reason)
	{
		getConnection().close(new LoginFail(reason));
	}

	public void close(PlayFailReason reason)
	{
		getConnection().close(new PlayFail(reason));
	}

	public void close(L2LoginServerPacket lsp)
	{
		getConnection().close(lsp);
	}

	public void setCharsOnServ(int servId, int chars)
	{
		if(_charsOnServers == null)
		{
			_charsOnServers = new HashMap<>();
		}
		_charsOnServers.put(servId, chars);
	}

	public Map<Integer, Integer> getCharsOnServ()
	{
		return _charsOnServers;
	}

	public void serCharsWaitingDelOnServ(int servId, long[] charsToDel)
	{
		if(_charsToDelete == null)
		{
			_charsToDelete = new HashMap<>();
		}
		_charsToDelete.put(servId, charsToDel);
	}

	public Map<Integer, long[]> getCharsWaitingDelOnServ()
	{
		return _charsToDelete;
	}

	@Override
	public String toString()
	{
		InetAddress address = getConnection().getInetAddress();
		return _state == LoginClientState.AUTHED_LOGIN ? '[' + _account + " (" + (address == null ? "disconnected" : address.getHostAddress()) + ")]" : '[' + (address == null ? "disconnected" : address.getHostAddress()) + ']';
	}

	public enum LoginClientState
	{
		CONNECTED, AUTHED_GG, AUTHED_LOGIN
	}
}