package dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch;

import dwo.gameserver.instancemanager.ClanSearchManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ClanSearchClanHolder;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeRecruitBoardDetail;

/**
 * User: Bacek
 * Date: 01.02.13
 * Time: 19:12
 */
public class RequestPledgeRecruitBoardDetail extends L2GameClientPacket
{
	private int _clanId;

	@Override
	protected void readImpl()
	{
		_clanId = readD();  // Обжект ид запрошимаемого клана ( ид клана !!)
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
		//		player.sendMessage("RequestPledgeRecruitBoardDetail");

		ClanSearchClanHolder clan = ClanSearchManager.getInstance().getClan(_clanId);

		if(clan == null)
		{
			return;
		}

		player.sendPacket(new ExPledgeRecruitBoardDetail(clan));
	}

	@Override
	public String getType()
	{
		return "[C] D0:E3 RequestPledgeRecruitBoardDetail";
	}
}
