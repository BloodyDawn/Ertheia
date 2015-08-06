package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class AskJoinAlliance extends L2GameServerPacket
{
	private String _requestorName;
	private int _requestorObjId;

	public AskJoinAlliance(int requestorObjId, String requestorName)
	{
		_requestorName = requestorName;
		_requestorObjId = requestorObjId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_requestorObjId);
		writeS(_requestorName);
	}
}
