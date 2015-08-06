package dwo.gameserver.network.login.gameserverpackets;

import dwo.gameserver.util.network.BaseSendablePacket;

/**
 * @author -Wooden-
 */

public class ChangeAccessLevel extends BaseSendablePacket
{
	public ChangeAccessLevel(String player, int access)
	{
		writeC(0x04);
		writeD(access);
		writeS(player);
	}

	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}