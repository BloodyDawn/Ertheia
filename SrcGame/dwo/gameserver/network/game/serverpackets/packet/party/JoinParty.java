package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class JoinParty extends L2GameServerPacket
{
	private int _response;

	public JoinParty(int response)
	{
		_response = response;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_response);
	}
}
