package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class JoinPledge extends L2GameServerPacket
{
	private int _pledgeId;

	public JoinPledge(int pledgeId)
	{
		_pledgeId = pledgeId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_pledgeId);
	}
}
