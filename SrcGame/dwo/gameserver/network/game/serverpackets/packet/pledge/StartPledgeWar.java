package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class StartPledgeWar extends L2GameServerPacket
{
	private String _pledgeName;
	private String _playerName;

	public StartPledgeWar(String pledge, String charName)
	{
		_pledgeName = pledge;
		_playerName = charName;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_playerName);
		writeS(_pledgeName);
	}
}