package dwo.loginserver.network.gameservercon.gameserverpackets;

import dwo.util.network.BaseRecievePacket;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 03.11.11
 * Time: 9:58
 */

public class AddOrUpdateAccount extends BaseRecievePacket
{
	private String _login;
	private String _password;
	private int _level;

	public AddOrUpdateAccount(byte[] decrypt)
	{
		super(decrypt);
		_login = readS();
		_password = readS();
		_level = readD();
	}

	public String getLogin()
	{
		return _login;
	}

	public String getPassword()
	{
		return _password;
	}

	public long getAccessLevel()
	{
		return _level;
	}
}