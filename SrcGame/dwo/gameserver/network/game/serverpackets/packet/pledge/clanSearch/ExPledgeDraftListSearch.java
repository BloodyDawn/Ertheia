package dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch;

import dwo.gameserver.instancemanager.ClanSearchManager;
import dwo.gameserver.model.holders.ClanSearchPlayerHolder;
import dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch.RequestPledgeDraftListSearch;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 18:27
 *
 * http://bladensoul.ru/scrupload/i/4f11b8.png
 */
public class ExPledgeDraftListSearch extends L2GameServerPacket
{
	private final RequestPledgeDraftListSearch.ClanWaiterSearchParams _params;

	public ExPledgeDraftListSearch(RequestPledgeDraftListSearch.ClanWaiterSearchParams params)
	{
		_params = params;
	}

	@Override
	protected void writeImpl()
	{
		List<ClanSearchPlayerHolder> list = ClanSearchManager.getInstance().listWaiters(_params);
		writeD(list.size());

		for(ClanSearchPlayerHolder waiter : list)
		{
			writeD(waiter.getCharId());
			writeS(waiter.getCharName());
			writeD(ClanSearchListType.getIndex(waiter.getSearchType()));
			writeD(waiter.getCharClassId()); // ClassId
			writeD(waiter.getCharLevel()); // Level
		}
	}

	public enum ClanSearchListType
	{
		SLT_FRIEND_LIST,
		SLT_PLEDGE_MEMBER_LIST,
		SLT_ADDITIONAL_FRIEND_LIST,
		SLT_ADDITIONAL_LIST,
		SLT_ANY;

		public static ClanSearchListType getType(int value)
		{
			return value == -1 || value >= ClanSearchListType.values().length ? SLT_ANY : ClanSearchListType.values()[value];
		}

		public static int getIndex(ClanSearchListType type)
		{
			switch(type)
			{
				case SLT_ANY:
					return -1;
			}

			return type.ordinal();
		}
	}
}
