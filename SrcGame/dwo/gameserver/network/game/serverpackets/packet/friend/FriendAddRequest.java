package dwo.gameserver.network.game.serverpackets.packet.friend;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class FriendAddRequest extends L2GameServerPacket
{
	private String _requestorName;

	public FriendAddRequest(String requestorName)
	{
		_requestorName = requestorName;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_requestorName);
		writeD(0);
	}
}
