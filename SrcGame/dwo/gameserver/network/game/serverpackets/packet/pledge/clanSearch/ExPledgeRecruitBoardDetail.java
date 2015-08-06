package dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch;

import dwo.gameserver.model.holders.ClanSearchClanHolder;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 18:26
 *
 * http://bladensoul.ru/scrupload/i/d370b3.png
 *
 * Impl by Yorie
 */
public class ExPledgeRecruitBoardDetail extends L2GameServerPacket
{

	private final ClanSearchClanHolder _clan;

	public ExPledgeRecruitBoardDetail(ClanSearchClanHolder clan)
	{
		_clan = clan;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_clan.getClanId());
		writeD(_clan.getSearchType().ordinal());
		writeS(_clan.getTitle());
		writeS(_clan.getDesc());
	}
}
