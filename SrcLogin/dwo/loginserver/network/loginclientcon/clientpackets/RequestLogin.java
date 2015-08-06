package dwo.loginserver.network.loginclientcon.clientpackets;

import dwo.config.Config;
import dwo.loginserver.GameServerTable;
import dwo.loginserver.L2LoginClient;
import dwo.loginserver.LoginController;
import dwo.loginserver.network.loginclientcon.serverpackets.AccountKicked;
import dwo.loginserver.network.loginclientcon.serverpackets.LoginFail;
import dwo.loginserver.network.loginclientcon.serverpackets.LoginOk;
import dwo.loginserver.network.loginclientcon.serverpackets.ServerList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 01.01.12
 * Time: 21:52
 */

public class RequestLogin extends L2LoginClientPacket
{
	private static Logger _log = LogManager.getLogger(RequestLogin.class);

	private byte[] _raw = new byte[128];

	private String _user;
	private String _password;
	private int _ncotp;

	public String getPassword()
	{
		return _password;
	}

	public String getUser()
	{
		return _user;
	}

	public int getOneTimePassword()
	{
		return _ncotp;
	}

	@Override
	public boolean readImpl()
	{
		if(_buf.remaining() >= 128)
		{
			readB(_raw);
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public void run()
	{
		byte[] decrypted;
		L2LoginClient client = getClient();
		try
		{
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, client.getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(_raw, 0x00, 0x80);
		}
		catch(GeneralSecurityException e)
		{
			_log.log(Level.ERROR, "", e);
			return;
		}

		try
		{
			_user = new String(decrypted, 0x5E, 14).trim().toLowerCase();
			_password = new String(decrypted, 0x6C, 16).trim();
			_ncotp = decrypted[0x7c];
			_ncotp |= decrypted[0x7d] << 8;
			_ncotp |= decrypted[0x7e] << 16;
			_ncotp |= decrypted[0x7f] << 24;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
			return;
		}

		LoginController lc = LoginController.getInstance();
		LoginController.AuthLoginResult result = lc.tryAuthLogin(_user, _password, client);
		switch(result)
		{
			case AUTH_SUCCESS:
				client.setAccount(_user);
				client.setState(L2LoginClient.LoginClientState.AUTHED_LOGIN);
				client.setSessionKey(lc.assignSessionKeyToClient(_user, client));
				lc.getCharactersOnAccount(_user);
				if(Config.SHOW_LICENCE)
				{
					client.sendPacket(new LoginOk(getClient().getSessionKey()));
				}
				else
				{
					getClient().sendPacket(new ServerList(getClient()));
				}
				break;
			case INVALID_PASSWORD:
				client.close(LoginFail.LoginFailReason.REASON_USER_OR_PASS_WRONG);
				break;
			case ACCOUNT_BANNED:
				client.close(new AccountKicked(AccountKicked.AccountKickedReason.REASON_PERMANENTLY_BANNED));
				break;
			case ALREADY_ON_LS:
				L2LoginClient oldClient;
				if((oldClient = lc.getAuthedClient(_user)) != null)
				{
					// kick the other client
					oldClient.close(LoginFail.LoginFailReason.REASON_ACCOUNT_IN_USE);
					lc.removeAuthedLoginClient(_user);
				}
				// kick also current client
				client.close(LoginFail.LoginFailReason.REASON_ACCOUNT_IN_USE);
				break;
			case ALREADY_ON_GS:
				GameServerTable.GameServerInfo gsi;
				if((gsi = lc.getAccountOnGameServer(_user)) != null)
				{
					client.close(LoginFail.LoginFailReason.REASON_ACCOUNT_IN_USE);

					// kick from there
					if(gsi.isAuthed())
					{
						gsi.getGameServerThread().kickPlayer(_user);
					}
				}
				break;
		}
	}
}
