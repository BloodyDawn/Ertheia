package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author chris_00
 */

public class ExAskJoinMPCC extends L2GameServerPacket
{
	private String _requestorName;

	public ExAskJoinMPCC(String requestorName)
	{
		_requestorName = requestorName;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_requestorName);
	}
}
