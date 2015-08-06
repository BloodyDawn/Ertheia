package dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch;

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeRecruitInfo;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 19:25
 */
public class RequestPledgeRecruitInfo extends L2GameClientPacket
{
	private int _clanId;

	@Override
	protected void readImpl()
	{
		_clanId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		//	if(player.isGM())
		//		player.sendMessage("RequestPledgeRecruitInfo clanId: "+ _clanId);

		L2Clan clan = ClanTable.getInstance().getClan(_clanId);

		if(clan == null)
		{
			return;
		}

		player.sendPacket(new ExPledgeRecruitInfo(clan));
	}

	@Override
	public String getType()
	{
		return "[C] D0:E0 RequestPledgeRecruitInfo";
	}
}
