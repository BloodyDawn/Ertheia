package dwo.gameserver.network.game.serverpackets.packet.friend;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author Tempy
 */

public class L2FriendSay extends L2GameServerPacket
{
	private String _sender;
	private String _receiver;
	private String _message;

	public L2FriendSay(String sender, String reciever, String message)
	{
		_sender = sender;
		_receiver = reciever;
		_message = message;
	}

	@Override
	protected void writeImpl()
	{
		writeD(0); // ??
		writeS(_receiver);
		writeS(_sender);
		writeS(_message);
	}
}
