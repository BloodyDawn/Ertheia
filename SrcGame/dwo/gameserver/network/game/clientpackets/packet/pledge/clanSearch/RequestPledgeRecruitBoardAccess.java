package dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch;

import dwo.gameserver.instancemanager.ClanSearchManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ClanSearchClanHolder;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeDraftListSearch.ClanSearchListType;

/**
 * User: Bacek
 * Date: 01.02.13
 * Time: 19:09
 */
public class RequestPledgeRecruitBoardAccess extends L2GameClientPacket
{
	private int _pledgeAccess;
	private ClanSearchListType _searchType;
	private String _title;
	private String _desc;

	@Override
	protected void readImpl()
	{
		_pledgeAccess = readD();
		_searchType = ClanSearchListType.getType(readD());
		_title = readS();
		_desc = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null || player.getClan() == null)
		{
			return;
		}

		if(player.getClan() == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(4031));
			return;
		}

		if(_title.isEmpty())
		{
			return;
		}

		if(player.getClan().getLeader().getObjectId() != player.getObjectId())
		{
			player.sendPacket(SystemMessage.getSystemMessage(4031));
			return;
		}

		// Зарегистрировать клан можно только имея соответствующие привилегии
		if((player.getClanPrivileges() & L2Clan.CP_CL_JOIN_CLAN) != L2Clan.CP_CL_JOIN_CLAN)
		{
			player.sendPacket(SystemMessage.getSystemMessage(4031));
			return;
		}

		// Ограничения на текст
		if(_title.length() > 64)
		{
			_title = _title.substring(0, 63);
		}

		if(_desc.length() > 256)
		{
			_desc = _desc.substring(0, 255);
		}

		if(ClanSearchManager.getInstance().addClan(new ClanSearchClanHolder(player.getClan().getClanId(), _searchType, _title, _desc)))
		{
			player.sendPacket(SystemMessage.getSystemMessage(4039));
		}
		else
		{
			SystemMessage message = SystemMessage.getSystemMessage(4038);
			player.sendPacket(message);
		}

		//	if(player.isGM())
		//		player.sendMessage("RequestPledgeRecruitBoardAccess pledgeAccess: "+ _pledgeAccess +" searchType: "+ _searchType +" title: "+ _title +" desc: "+ _desc);

	}

	@Override
	public String getType()
	{
		return "[C] D0:E2 RequestPledgeRecruitBoardAccess";
	}
}
