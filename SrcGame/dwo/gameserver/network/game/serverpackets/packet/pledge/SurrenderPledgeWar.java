package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class SurrenderPledgeWar extends L2GameServerPacket
{
	private String _pledgeName;
	private String _playerName;

	public SurrenderPledgeWar(String pledge, String charName)
	{
		_pledgeName = pledge;
		_playerName = charName;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_pledgeName);
		writeS(_playerName);
	}
}