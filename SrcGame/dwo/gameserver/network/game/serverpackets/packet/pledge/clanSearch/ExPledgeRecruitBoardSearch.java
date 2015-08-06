package dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch;

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.instancemanager.ClanSearchManager;
import dwo.gameserver.model.holders.ClanSearchClanHolder;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch.RequestPledgeRecruitBoardSearch.ClanSearchParams;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 18:26
 *
 * http://bladensoul.ru/scrupload/i/a03e3f.png
 *
 * Impl by Yorie
 */
public class ExPledgeRecruitBoardSearch extends L2GameServerPacket
{
	/**
	 * Лимит записей на одну страницу поиска.
	 */
	private static final int PAGINATION_LIMIT = 12;

	/**
	 * Текущая страница.
	 */
	private final ClanSearchParams _params;

	/**
	 *
	 * @param params Параметры поиска.
	 */
	public ExPledgeRecruitBoardSearch(ClanSearchParams params)
	{
		_params = params;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_params.getCurrentPage());      // текушая страница
		writeD(ClanSearchManager.getInstance().getPageCount(PAGINATION_LIMIT));      // всего старинц

		List<ClanSearchClanHolder> clans = ClanSearchManager.getInstance().listClans(PAGINATION_LIMIT, _params);
		int size = clans.size();

		writeD(size);

		for(ClanSearchClanHolder clanHolder : clans)
		{
			writeD(clanHolder.getClanId());
			writeD(0x00);
		}

		for(ClanSearchClanHolder clanHolder : clans)
		{
			L2Clan clan = ClanTable.getInstance().getClan(clanHolder.getClanId());

			writeD(clan.getCrestId());
			writeD(clan.getAllyCrestId());

			writeS(clan.getName()); // clan name
			writeS(clan.getLeader().getName()); // leader name

			writeD(clan.getLevel()); // clanLevel
			writeD(clan.getMembersCount()); // memberCount
			writeD(ExPledgeDraftListSearch.ClanSearchListType.getIndex(clanHolder.getSearchType())); // ESearchListType

			writeS(clanHolder.getTitle()); // title
		}
	}

}
