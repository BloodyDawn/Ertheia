package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PledgeShowMemberListDelete extends L2GameServerPacket
{
	private String _player;

	public PledgeShowMemberListDelete(String playerName)
	{
		_player = playerName;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_player);
	}
}
