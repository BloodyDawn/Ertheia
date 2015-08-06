package dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch;

import dwo.gameserver.instancemanager.ClanSearchManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ClanSearchPlayerHolder;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeDraftListSearch;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeRecruitApplyInfo;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 19:25
 */
public class RequestPledgeWaitingApply extends L2GameClientPacket
{
	private ExPledgeDraftListSearch.ClanSearchListType _searchType;
	private int _clanId;
	private String _desc;

	@Override
	protected void readImpl()
	{
		_searchType = ExPledgeDraftListSearch.ClanSearchListType.getType(readD());
		_clanId = readD();
		_desc = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		//	if (player.isGM())
		//		player.sendMessage("RequestPledgeWaitingApply searchType: " + _searchType + " _clanId: " + _clanId+ " desc: " + _desc);

		if(player.getClan() != null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(4031));
			return;
		}

		ClanSearchPlayerHolder holder = new ClanSearchPlayerHolder(player.getObjectId(), player.getName(), player.getLevel(), player.getBaseClassId(), _clanId, _searchType, _desc);
		if(ClanSearchManager.getInstance().addPlayer(holder))
		{
			player.sendPacket(new ExPledgeRecruitApplyInfo(ExPledgeRecruitApplyInfo.ClanSearchWindowStatusType.STATUS_TYPE_WAITING));
		}
		else
		{
			SystemMessage message = SystemMessage.getSystemMessage(4038);
			player.sendPacket(message);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:E4 RequestPledgeWaitingApply";
	}
}
