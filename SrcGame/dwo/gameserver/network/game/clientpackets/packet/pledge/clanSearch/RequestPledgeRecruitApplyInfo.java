package dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch;

import dwo.gameserver.instancemanager.ClanSearchManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeRecruitApplyInfo;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 19:26
 */
public class RequestPledgeRecruitApplyInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		ExPledgeRecruitApplyInfo.ClanSearchWindowStatusType status = ExPledgeRecruitApplyInfo.ClanSearchWindowStatusType.STATUS_TYPE_DEFAULT;
		if(player.getClan() != null && player.isClanLeader() && ClanSearchManager.getInstance().isClanRegistered(player.getClanId()))
		{
			status = ExPledgeRecruitApplyInfo.ClanSearchWindowStatusType.STATUS_TYPE_ORDER_LIST;
		}
		else if(player.getClan() == null && ClanSearchManager.getInstance().isWaiterRegistered(player.getObjectId()))
		{
			status = ExPledgeRecruitApplyInfo.ClanSearchWindowStatusType.STATUS_TYPE_WAITING;
		}

		//	if(player.isGM())
		//		player.sendMessage("RequestPledgeRecruitApplyInfo");

		player.sendPacket(new ExPledgeRecruitApplyInfo(status));
	}

	@Override
	public String getType()
	{
		return "[C] D0:EB RequestPledgeRecruitApplyInfo";
	}
}
