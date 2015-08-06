package dwo.loginserver.network.gameservercon.gameserverpackets;

import dwo.util.network.BaseRecievePacket;

public class RequestTempBan extends BaseRecievePacket
{
	String _accountName;
	String _ip;
	long _banTime;

	public RequestTempBan(byte[] decrypt)
	{
		super(decrypt);
		_accountName = readS();
		_ip = readS();
		_banTime = readQ();
	}

	public String getAccountName()
	{
		return _accountName;
	}

	public String getIp()
	{
		return _ip;
	}

	public long getBanTime()
	{
		return _banTime;
	}
}