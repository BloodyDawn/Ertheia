package dwo.loginserver.network.gameservercon.gameserverpackets;

import dwo.util.network.BaseRecievePacket;

public class UnblockAddress extends BaseRecievePacket
{
	private String _address;

	public UnblockAddress(byte[] decrypt)
	{
		super(decrypt);
		_address = readS();
	}

	public String getAddress()
	{
		return _address;
	}
}
