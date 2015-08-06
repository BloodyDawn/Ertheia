package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * Format: ch S
 * @author KenM
 */

public class ExAskJoinPartyRoom extends L2GameServerPacket
{
	private String _charName;
	private String _roomName;

	public ExAskJoinPartyRoom(String charName, String roomName)
	{
		_charName = charName;
		_roomName = roomName;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_charName);
		writeS(_roomName);
	}
}
