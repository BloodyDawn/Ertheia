package dwo.loginserver.network.loginclientcon.clientpackets;

import dwo.config.Config;
import dwo.loginserver.LoginController;
import dwo.loginserver.SessionKey;
import dwo.loginserver.network.loginclientcon.serverpackets.LoginFail.LoginFailReason;
import dwo.loginserver.network.loginclientcon.serverpackets.PlayFail.PlayFailReason;
import dwo.loginserver.network.loginclientcon.serverpackets.PlayOk;

/**
 * Fromat is ddc
 * d: first part of session id
 * d: second part of session id
 * c: server ID
 */
public class RequestServerLogin extends L2LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	private int _serverId;

	public int getSessionKey1()
	{
		return _skey1;
	}

	public int getSessionKey2()
	{
		return _skey2;
	}

	public int getServerID()
	{
		return _serverId;
	}

	@Override
	public boolean readImpl()
	{
		if(_buf.remaining() >= 9)
		{
			_skey1 = readD();
			_skey2 = readD();
			_serverId = readC();
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
		SessionKey sk = getClient().getSessionKey();

		// if we didnt showed the license we cant check these values
		if(!Config.SHOW_LICENCE || sk.checkLoginPair(_skey1, _skey2))
		{
			if(LoginController.getInstance().isLoginPossible(getClient(), _serverId))
			{
				getClient().setJoinedGS(true);
				getClient().sendPacket(new PlayOk(sk));
			}
			else
			{
				getClient().close(PlayFailReason.REASON_SERVER_OVERLOADED);
			}
		}
		else
		{
			getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
		}
	}
}
