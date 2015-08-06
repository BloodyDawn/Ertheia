package dwo.gameserver.network.login.gameserverpackets;

import dwo.gameserver.util.network.BaseSendablePacket;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 03.11.11
 * Time: 10:01
 */

public class RegisterAccOrUpdate extends BaseSendablePacket
{
	public RegisterAccOrUpdate(String login, String password, int access)
	{
		writeC(0x0B);
		writeS(login);
		writeS(password);
		writeD(access);
	}

	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
