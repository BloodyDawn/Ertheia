package dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch;

import dwo.gameserver.instancemanager.ClanSearchManager;
import dwo.gameserver.model.holders.ClanSearchPlayerHolder;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 18:27
 */
public class ExPledgeWaitingList extends L2GameServerPacket
{
	private final int _clanId;

	public ExPledgeWaitingList(int clanId)
	{
		_clanId = clanId;
	}

	@Override
	protected void writeImpl()
	{
		List<ClanSearchPlayerHolder> players = ClanSearchManager.getInstance().listApplicants(_clanId);
		if(players != null)
		{
			writeD(players.size());
			for(ClanSearchPlayerHolder playerHolder : players)
			{
				writeD(playerHolder.getCharId());
				writeS(playerHolder.getCharName());
				writeD(playerHolder.getCharClassId());
				writeD(playerHolder.getCharLevel());
			}
		}
		else
		{
			writeD(0x00);
		}
	}
}
