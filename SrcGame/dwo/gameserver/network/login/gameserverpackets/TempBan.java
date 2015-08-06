package dwo.gameserver.network.login.gameserverpackets;

import dwo.gameserver.util.network.BaseSendablePacket;

public class TempBan extends BaseSendablePacket
{
	public TempBan(String accountName, String ip, long time)
	{
		writeC(0x0A);
		writeS(accountName);
		writeS(ip);
		writeQ(System.currentTimeMillis() + time * 60000);
	}

	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
