package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.model.player.formation.clan.RankPrivs;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PledgePowerGradeList extends L2GameServerPacket
{
	private RankPrivs[] _privs;

	public PledgePowerGradeList(RankPrivs[] privs)
	{
		_privs = privs;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_privs.length);
		for(RankPrivs temp : _privs)
		{
			writeD(temp.getRank());
			writeD(temp.getParty());
		}
	}
}
