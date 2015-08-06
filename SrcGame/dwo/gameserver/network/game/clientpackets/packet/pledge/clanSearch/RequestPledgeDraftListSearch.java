package dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeDraftListSearch;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 19:26
 */
public class RequestPledgeDraftListSearch extends L2GameClientPacket
{
	private int _minLevel;                     // Миниманый уровень игрока     1-99  дефолт 1   ( фильтр )
	private int _maxLevel;                    // Максимаотный уровень игрока   1-99  дефолт 99  ( фильтр )
	private ClanSearchPlayerRoleType _role;                  // Роль 1-10  1 - Воин 2 - Мистик  и тд.. ( TODO поже дам список ид проф для каждого !!)
	private String _charName;            // Имя игрока ( фильтр )
	private ClanSearchPlayerSortType _sortType;                  // сортировка по столбцам  0 не сортировать не по какому столбцу
	private ClanSearchPlayerSortOrder _sortOrder;                   // 1 - по возрастанию ( A->W ) 2 по убыванию ( W->A ) 0 не сортировать для КОНКРЕТНОГО столбца  ( поле выше )

	@Override
	protected void readImpl()
	{
		_minLevel = Math.max(0, Math.min(readD(), 99));
		_maxLevel = Math.max(0, Math.min(readD(), 99));
		_role = ClanSearchPlayerRoleType.valueOf(readD());

		_charName = readS().trim().toLowerCase();
		if(_charName.length() > 255)
		{
			_charName = _charName.substring(0, 255);
		}

		_sortType = ClanSearchPlayerSortType.valueOf(readD());
		_sortOrder = ClanSearchPlayerSortOrder.valueOf(readD());
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getClanSearch().tryPerformAction(FloodAction.CLAN_BOARD_DRAFT_SEARCH))
		{
			return;
		}

		//	if(player.isGM())
		//		player.sendMessage("RequestPledgeDraftListSearch _unk: "+ _minLevel +" _unk1: "+ _maxLevel +" _unk1: "+ _role +" _unk_str: "+ _charName +" _unk3: "+ _sortType +" _unk4: "+ _sortOrder);

		player.sendPacket(new ExPledgeDraftListSearch(new ClanWaiterSearchParams(_minLevel, _maxLevel, _role, _charName, _sortType, _sortOrder)));
	}

	@Override
	public String getType()
	{
		return "[C] D0:E9 RequestPledgeDraftListSearch";
	}
	// 1 по столбцу "Имя"
	// 2 по столбцу "карма" ( ESearchListType )
	// 3 по столбцу "Роль"   1-10  ( см выше )
	// 4 по столбцу "уровень"

	public static enum ClanSearchPlayerSortType
	{
		SORT_TYPE_NONE,
		SORT_TYPE_NAME,
		SORT_TYPE_SEARCH_TYPE,
		SORT_TYPE_ROLE,
		SORT_TYPE_LEVEL;

		public static ClanSearchPlayerSortType valueOf(int value)
		{
			if(value < ClanSearchPlayerSortType.values().length)
			{
				return ClanSearchPlayerSortType.values()[value];
			}

			return SORT_TYPE_NONE;
		}
	}

	public static enum ClanSearchPlayerSortOrder
	{
		NONE,
		ASC,
		DESC;

		public static ClanSearchPlayerSortOrder valueOf(int value)
		{
			switch(value)
			{
				case 0:
					return NONE;
				case 1:
					return ASC;
				case 2:
					return DESC;
			}
			return NONE;
		}
	}

	public static enum ClanSearchPlayerRoleType
	{
		ANY(new int[]{}),
		FIGHTER(new int[]{0, 1, 4, 7, 18, 19, 22, 31, 35, 44, 45, 47, 53, 54, 56, 123, 124, 125, 126}),
		MYSTIC(new int[]{49, 50, 38, 39, 42, 25, 26, 29, 10, 11, 15}),
		MELEE_FIGHTER(new int[]{
			2, 3, 46, 48, 57, 127, 128, 129, 88, 89, 113, 114, 118, 131, 132, 133, 140, 152, 153, 154, 155, 156, 157
		}),
		MELEE_FIGHTER2(new int[]{8, 23, 36, 55, 93, 101, 108, 117, 141, 158, 159, 160, 161}),
		RANGE_FIGHTER(new int[]{9, 24, 37, 130, 92, 102, 134, 109, 142, 162, 163, 164, 165}),
		DEFENSE_FIGHTER(new int[]{5, 6, 20, 33, 90, 91, 99, 106, 139, 148, 149, 150, 151}),
		SUPPORT_FIGHTER(new int[]{21, 34, 135, 100, 107, 136, 144, 171, 172, 173, 174, 175}),
		MAGICIAN(new int[]{12, 13, 27, 40, 94, 95, 103, 110, 143, 166, 167, 168, 169, 170}),
		HEALER(new int[]{16, 17, 30, 43, 52, 51, 97, 98, 105, 112, 116, 115, 146, 179, 180, 181}),
		SUMMONER(new int[]{14, 28, 41, 96, 104, 111, 145, 176, 177, 178});

		private final int[] _classIds;

		ClanSearchPlayerRoleType(int[] classIds)
		{
			_classIds = classIds;
		}

		public static ClanSearchPlayerRoleType valueOf(int value)
		{
			if(value >= ClanSearchPlayerRoleType.values().length)
			{
				return ANY;
			}

			return values()[value];
		}

		public boolean isClassRold(int classId)
		{
			if(this == ANY)
			{
				return true;
			}

			for(int roleClassId : _classIds)
			{
				if(roleClassId == classId)
				{
					return true;
				}
			}
			return false;
		}
	}

	public static class ClanWaiterSearchParams
	{
		private final int _minLevel;
		private final int _maxLevel;
		private final ClanSearchPlayerRoleType _role;
		private final String _charName;
		private final ClanSearchPlayerSortType _sortType;
		private final ClanSearchPlayerSortOrder _sortOrder;

		public ClanWaiterSearchParams(int minLevel, int maxLevel, ClanSearchPlayerRoleType role, String charName, ClanSearchPlayerSortType sortType, ClanSearchPlayerSortOrder sortOrder)
		{
			_minLevel = minLevel;
			_maxLevel = maxLevel;
			_role = role;
			_charName = charName;
			_sortType = sortType;
			_sortOrder = sortOrder;
		}

		public int getMinLevel()
		{
			return _minLevel;
		}

		public int getMaxLevel()
		{
			return _maxLevel;
		}

		public ClanSearchPlayerRoleType getRole()
		{
			return _role;
		}

		public String getCharName()
		{
			return _charName;
		}

		public ClanSearchPlayerSortType getSortType()
		{
			return _sortType;
		}

		public ClanSearchPlayerSortOrder getSortOrder()
		{
			return _sortOrder;
		}
	}
}
