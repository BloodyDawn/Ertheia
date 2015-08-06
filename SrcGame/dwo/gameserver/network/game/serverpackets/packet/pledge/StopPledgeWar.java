package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class StopPledgeWar extends L2GameServerPacket
{
	private String _pledgeName;
	private String _playerName;

	public StopPledgeWar(String pledge, String charName)
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