package dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch;

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.instancemanager.ClanSearchManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ClanSearchClanHolder;
import dwo.gameserver.model.holders.ClanSearchPlayerHolder;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 18:27
 * http://bladensoul.ru/scrupload/i/945dca.png
 *
 * Impl by Yorie
 */
public class ExPledgeWaitingListApplied extends L2GameServerPacket
{
	private int _clanId;
	private String _clanName = "";
	private String _leaderName = "";
	private int _clanLevel;
	private int _memberCount;
	private ExPledgeDraftListSearch.ClanSearchListType _searchType = ExPledgeDraftListSearch.ClanSearchListType.SLT_ANY;
	private String _title = "";
	private String _desc = "";

	public ExPledgeWaitingListApplied(ClanSearchPlayerHolder playerHolder)
	{
		if(playerHolder != null)
		{
			ClanSearchClanHolder clanHolder = ClanSearchManager.getInstance().getClan(playerHolder.getPrefferedClanId());

			if(clanHolder != null)
			{
				L2Clan clan = ClanTable.getInstance().getClan(clanHolder.getClanId());

				if(clan != null)
				{
					_clanId = clanHolder.getClanId();
					_clanName = clan.getName();
					_leaderName = clan.getLeaderName();
					_clanLevel = clan.getLevel();
					_memberCount = clan.getMembersCount();
					_searchType = clanHolder.getSearchType();
					_title = clanHolder.getTitle();
					_desc = clanHolder.getDesc();
				}
			}
		}
	}

	/**
	 * Для отправки пакета игроку, находящемуся в списке ожидания.
	 * @param player
	 */
	public ExPledgeWaitingListApplied(L2PcInstance player)
	{
		this(ClanSearchManager.getInstance().getWaiter(player.getObjectId()));
	}

	@Override
	protected void writeImpl()
	{
		// dSSdddSS
		writeD(_clanId); // clanId        1611661332
		writeS(_clanName); // clanName        YellowSubmarine
		writeS(_leaderName); // unk 		   	   ???? ( есть на скрине )
		writeD(_clanLevel); // clanLevel	   10
		writeD(_memberCount); // memberCount   31
		writeD(ExPledgeDraftListSearch.ClanSearchListType.getIndex(_searchType)); // ESearchListType
		writeS(_title);   // Какое то описание   // ??? ???? ?? на скрине
		writeS(_desc);   // еше описание        // на скрине ghgfhfgh
	}
}
