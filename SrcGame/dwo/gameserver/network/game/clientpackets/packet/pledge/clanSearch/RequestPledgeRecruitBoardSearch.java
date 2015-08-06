package dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeDraftListSearch;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeRecruitBoardSearch;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 19:25
 */
public class RequestPledgeRecruitBoardSearch extends L2GameClientPacket
{
	private ClanSearchParams _params;

	@Override
	protected void readImpl()
	{
		_params = new ClanSearchParams(readD(), ExPledgeDraftListSearch.ClanSearchListType.getType(readD()), ClanSearchTargetType.valueOf(readD()), readS(), ClanSearchClanSortType.valueOf(readD()), ClanSearchSortOrder.valueOf(readD()), readD());
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getClanSearch().tryPerformAction(FloodAction.CLAN_BOARD_SEARCH))
		{
			return;
		}

		player.sendPacket(new ExPledgeRecruitBoardSearch(_params));

		/*
		if(player.isGM())
			player.sendMessage("RequestPledgeRecruitBoardSearch" +
					" clanLevel: " + _params.getClanLevel() +
					" searchListType: " + _params.getSearchType() +
					" targetType: " + _params.getTargetType() +
					" targetName: " + _params.getName() +
					" sortType: " + _params.getSortType() +
					" sortOrder: " + _params.getSortOrder() +
					" currentPage: " + _params.getCurrentPage());
		*/
	}

	@Override
	public String getType()
	{
		return "[C] D0:E1 RequestPledgeRecruitBoardSearch";
	}

	/**
	 * Тип поиска.
	 */
	public static enum ClanSearchTargetType
	{
		TARGET_TYPE_LEADER_NAME,
		TARGET_TYPE_CLAN_NAME;

		public static ClanSearchTargetType valueOf(int value)
		{
			switch(value)
			{
				case 0:
					return TARGET_TYPE_LEADER_NAME;
				default:
					return TARGET_TYPE_CLAN_NAME;
			}
		}
	}

	/**
	 * Тип сортировки.
	 */
	public static enum ClanSearchClanSortType
	{
		SORT_TYPE_NONE,
		SORT_TYPE_CLAN_NAME,
		SORT_TYPE_LEADER_NAME,
		SORT_TYPE_MEMBER_COUNT,
		SORT_TYPE_CLAN_LEVEL,
		SORT_TYPE_SEARCH_LIST_TYPE;

		public static ClanSearchClanSortType valueOf(int value)
		{
			if(value < ClanSearchClanSortType.values().length)
			{
				return ClanSearchClanSortType.values()[value];
			}

			return SORT_TYPE_NONE;
		}
	}

	public static enum ClanSearchSortOrder
	{
		SORT_ORDER_NONE,
		SORT_ORDER_ASC,
		SORT_ORDER_DESC;

		public static ClanSearchSortOrder valueOf(int value)
		{
			switch(value)
			{
				case 1:
					return SORT_ORDER_ASC;
				case 2:
					return SORT_ORDER_DESC;
				default:
					return SORT_ORDER_NONE;
			}
		}
	}

	public static class ClanSearchParams
	{
		private final int _clanLevel;    // 0-11   -1 все    Сортировка по уровню клана
		private final ExPledgeDraftListSearch.ClanSearchListType _listType;         // по ESearchListType  Сортировка по типу  -1 все
		private final ClanSearchTargetType _targetType;                   // 0 - глова  1 - клан
		private final String _targetName;             // Имя клана или гловы ( определяется пунктом выше )

		private final ClanSearchClanSortType _sortType;                   // сортировка по столбцам  0 не сортировать не по какому столбцу
		// 1 по столбцу "клан"
		// 2 по столбцу "Глова"
		// 3 по столбцу "группа" ( количество игроков в клане )
		// 4 по столбцу "уровень"
		// 5 по столбцу "карма" ( ESearchListType )

		private final ClanSearchSortOrder _sortOrder;                   // 1 - по возрастанию ( A->W ) 2 по убыванию ( W->A ) 0 не сортировать для КОНКРЕТНОГО столбца  ( поле выше )

		private final int _currentPage;                  // текушая страница

		public ClanSearchParams(int clanLevel, ExPledgeDraftListSearch.ClanSearchListType searchListType, ClanSearchTargetType targetType, String targetName, ClanSearchClanSortType sortType, ClanSearchSortOrder sortOrder, int currentPage)
		{
			_clanLevel = clanLevel;
			_listType = searchListType;
			_targetType = targetType;
			_targetName = targetName;
			_sortType = sortType;
			_sortOrder = sortOrder;
			_currentPage = Math.max(0, currentPage - 1);
		}

		public int getClanLevel()
		{
			return _clanLevel;
		}

		public ExPledgeDraftListSearch.ClanSearchListType getSearchType()
		{
			return _listType;
		}

		public ClanSearchTargetType getTargetType()
		{
			return _targetType;
		}

		public String getName()
		{
			return _targetName;
		}

		public ClanSearchClanSortType getSortType()
		{
			return _sortType;
		}

		public ClanSearchSortOrder getSortOrder()
		{
			return _sortOrder;
		}

		public int getCurrentPage()
		{
			return _currentPage;
		}
	}
}
