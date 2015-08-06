package dwo.gameserver.instancemanager;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.sql.queries.clan.ClanSearch;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.holders.ClanSearchClanHolder;
import dwo.gameserver.model.holders.ClanSearchPlayerHolder;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch.RequestPledgeDraftListSearch;
import dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch.RequestPledgeRecruitBoardSearch;
import dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch.RequestPledgeRecruitBoardSearch.ClanSearchParams;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeDraftListSearch.ClanSearchListType;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Менеджер хранения и управления данными по поиску клана и рекрутерам.
 *
 * @author Yorie
 */
public class ClanSearchManager
{
	public static final long CLAN_LOCK_TIME = 60 * 5 * 1000;
	public static final long WAITER_LOCK_TIME = 60 * 5 * 1000;
	public static final long APPLICANT_LOCK_TIME = 60 * 5 * 1000;
	private static final Logger _log = LogManager.getLogger(ClanSearchManager.class);
	/**
	 * Список зарегистрированных кланов для поиска игроков.
	 */
	private static final List<ClanSearchClanHolder> _registeredClans = new FastList<ClanSearchClanHolder>().shared();
	/**
	 * Список зарегистрированных игроков, отправивших сообщение клану.
	 */
	private static final Map<Integer, List<ClanSearchPlayerHolder>> _applicantPlayers = new FastMap<Integer, List<ClanSearchPlayerHolder>>().shared();
	/**
	 * Список игроков, ищущих клан.
	 */
	private static final List<ClanSearchPlayerHolder> _waitingPlayers = new FastList<ClanSearchPlayerHolder>().shared();
	/**
	 * Таск для сохранения данных по крону.
	 */
	private static final ClanSearchTask _scheduledTaskExecutor = new ClanSearchTask();

	public ClanSearchManager()
	{
		load();
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(_scheduledTaskExecutor, 60 * 60 * 1000, 60 * 60 * 1000);
	}

	public static ClanSearchManager getInstance()
	{
		return SingletonHolder.instance;
	}

	public void load()
	{
		_log.info("Loading clan search data...");

		ThreadConnection conn = null;
		FiltredPreparedStatement st = null;
		ResultSet set = null;
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();
			st = conn.prepareStatement(ClanSearch.LOAD_CLANS);
			set = st.executeQuery();

			while(set.next())
			{
				try
				{
					int clanId = set.getInt("clan_id");
					ClanSearchListType searchType = ClanSearchListType.valueOf(set.getString("search_type"));
					String title = set.getString("title");
					String desc = set.getString("desc");

					ClanSearchClanHolder clan = new ClanSearchClanHolder(clanId, searchType, title, desc);

					// Клан удалили руками?О_о
					if(ClanTable.getInstance().getClan(clanId) == null)
					{
						_scheduledTaskExecutor.scheduleClanForRemoval(clanId);
					}
					else
					{
						addClan(clan);
					}
				}
				catch(Exception e)
				{
					_log.error("Failed to load Clan Search Engine clan row.", e);
				}
			}

			DatabaseUtils.closeDatabaseSR(st, set);

			st = conn.prepareStatement(ClanSearch.LOAD_APPLICANTS);
			set = st.executeQuery();

			while(set.next())
			{
				try
				{
					int charId = set.getInt("char_id");
					String charName = set.getString("char_name");
					int charLevel = set.getInt("char_level");
					int charClassId = set.getInt("char_class_id");
					int prefferedClanId = set.getInt("preffered_clan_id");
					ClanSearchListType searchType = ClanSearchListType.valueOf(set.getString("search_type"));
					String desc = set.getString("desc");

					ClanSearchPlayerHolder player = new ClanSearchPlayerHolder(charId, charName, charLevel, charClassId, prefferedClanId, searchType, desc);

					addPlayer(player);
				}
				catch(Exception e)
				{
					_log.error("Failed to load Clan Search Engine applicant.", e);
				}
			}

			st = conn.prepareStatement(ClanSearch.LOAD_WAITERS);
			set = st.executeQuery();

			while(set.next())
			{
				try
				{
					int charId = set.getInt("char_id");
					String charName = set.getString("char_name");
					int charLevel = set.getInt("char_level");
					int charClassId = set.getInt("char_class_id");
					ClanSearchListType searchType = ClanSearchListType.valueOf(set.getString("search_type"));

					ClanSearchPlayerHolder player = new ClanSearchPlayerHolder(charId, charName, charLevel, charClassId, searchType);

					addPlayer(player);
				}
				catch(Exception e)
				{
					_log.error("Failed to load Clan Search Engine waiter.", e);
				}
			}
		}
		catch(SQLException e)
		{
			_log.error("Failed to load Clan Search Engine clan list.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(conn, st, set);
		}

		_log.info("Loaded " + _registeredClans.size() + " registered clans.");
		_log.info("Loaded " + _applicantPlayers.size() + " registered players.");
	}

	/**
	 * Возвращает кол-во страниц зарегистрированных кланов в соответствии с paginationLimit кол-вом записей на страницу.
	 *
	 * @param paginationLimit Количество записей на страницу.
	 * @return Кол-во страниц.
	 */
	public int getPageCount(int paginationLimit)
	{
		return _registeredClans.size() / paginationLimit + (_registeredClans.size() % paginationLimit == 0 ? 0 : 1);
	}

	@Nullable
	public ClanSearchClanHolder getClan(int clanId)
	{
		for(ClanSearchClanHolder clan : _registeredClans)
		{
			if(clan.getClanId() == clanId)
			{
				return clan;
			}
		}
		return null;
	}

	/**
	 * Возвращает список зарегистрированных кланов на странице page в соответствии с paginationLimit кол-вом на одной странице.
	 *
	 * @param paginationLimit Кол-во записей на одной странице.
	 * @param params Параметры поиска.
	 * @return Список кланов на странице.
	 */
	public List<ClanSearchClanHolder> listClans(int paginationLimit, ClanSearchParams params)
	{
		List<ClanSearchClanHolder> clanList = new FastList<>();
		int page = Math.min(params.getCurrentPage(), getPageCount(paginationLimit));

		for(int i = 0; i < paginationLimit; ++i)
		{
			int currentIndex = i + page * paginationLimit;

			if(currentIndex >= _registeredClans.size())
			{
				break;
			}

			ClanSearchClanHolder clanHolder = _registeredClans.get(currentIndex);
			L2Clan clan = ClanTable.getInstance().getClan(clanHolder.getClanId());

			if(clan == null)
			{
				continue;
			}

			// Фильтр по уровню клана
			if(params.getClanLevel() >= 0 && clan.getLevel() != params.getClanLevel())
			{
				continue;
			}

			// Фильтр по типу поиска клана
			if(params.getSearchType() != ClanSearchListType.SLT_ANY && clanHolder.getSearchType() != params.getSearchType())
			{
				continue;
			}

			// Фильтр по имени объекта
			if(!params.getName().isEmpty())
			{
				if(params.getTargetType() == RequestPledgeRecruitBoardSearch.ClanSearchTargetType.TARGET_TYPE_LEADER_NAME)
				{
					if(!clan.getLeaderName().contains(params.getName()))
					{
						continue;
					}
				}
				else if(params.getTargetType() == RequestPledgeRecruitBoardSearch.ClanSearchTargetType.TARGET_TYPE_CLAN_NAME)
				{
					if(!clan.getName().contains(params.getName()))
					{
						continue;
					}
				}
			}

			clanList.add(clanHolder);
		}

		// Сортировка
		if(params.getSortOrder() != RequestPledgeRecruitBoardSearch.ClanSearchSortOrder.SORT_ORDER_NONE)
		{
			Collections.sort(clanList, (clanHolderLeft, clanHolderRight) -> {
				L2Clan clanLeft = ClanTable.getInstance().getClan(clanHolderLeft.getClanId());
				L2Clan clanRight = ClanTable.getInstance().getClan(clanHolderRight.getClanId());
				int left = 0;
				int right = 0;

				if(params.getSortOrder() == RequestPledgeRecruitBoardSearch.ClanSearchSortOrder.SORT_ORDER_ASC)
				{
					left = 1;
					right = -1;
				}
				else if(params.getSortOrder() == RequestPledgeRecruitBoardSearch.ClanSearchSortOrder.SORT_ORDER_DESC)
				{
					left = -1;
					right = 1;
				}

				switch(params.getSortType())
				{
					case SORT_TYPE_CLAN_LEVEL:
						return clanLeft.getLevel() > clanRight.getLevel() ? left : right;
					case SORT_TYPE_CLAN_NAME:
						return clanLeft.getName().compareTo(clanRight.getName()) == 1 ? left : right;
					case SORT_TYPE_LEADER_NAME:
						return clanLeft.getLeaderName().compareTo(clanRight.getName()) == 1 ? left : right;
					case SORT_TYPE_MEMBER_COUNT:
						return clanLeft.getMembersCount() > clanRight.getMembersCount() ? left : right;
					case SORT_TYPE_SEARCH_LIST_TYPE:
						return clanHolderLeft.getSearchType().ordinal() > clanHolderRight.getSearchType().ordinal() ? left : right;
				}

				return 0;
			});
		}

		return clanList;
	}

	/**
	 * Определяет, зарегистрирован ли указанный клан в поиске кланов.
	 * @param clanId ID клана.
	 * @return True, если клан зарегистрирован.
	 */
	public boolean isClanRegistered(int clanId)
	{
		for(ClanSearchClanHolder clan : _registeredClans)
		{
			if(clan.getClanId() == clanId)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Добавляет клан в список поиска.
	 * @param clan Клан.
	 */
	public boolean addClan(ClanSearchClanHolder clan)
	{
		if(_scheduledTaskExecutor.isClanLocked(clan.getClanId()))
		{
			return false;
		}

		ClanSearchClanHolder existedClan = getClan(clan.getClanId());
		if(existedClan != null)
		{
			existedClan.setSearchType(clan.getSearchType());
			existedClan.setTitle(clan.getTitle());
			existedClan.setDesc(clan.getDesc());
			_scheduledTaskExecutor.scheduleClanForAddition(existedClan);
			return true;
		}
		else
		{
			_registeredClans.add(clan);
			_scheduledTaskExecutor.scheduleClanForAddition(clan);
			return true;
		}
	}

	public void removeClan(ClanSearchClanHolder clan)
	{
		if(_registeredClans.contains(clan))
		{
			_registeredClans.remove(clan);
			// Lock clan for 30 minutes
			_scheduledTaskExecutor.lockClan(clan.getClanId(), CLAN_LOCK_TIME);
		}
	}

	@Nullable
	public ClanSearchPlayerHolder getWaiter(int charId)
	{
		for(ClanSearchPlayerHolder player : _waitingPlayers)
		{
			if(player.getCharId() == charId)
			{
				return player;
			}
		}
		return null;
	}

	/**
	 * Находит любую запись заявщика вступления в любой клан по его ID.
	 * @param charId ID персонажа.
	 * @return
	 */
	@Nullable
	public ClanSearchPlayerHolder findAnyApplicant(int charId)
	{
		for(Map.Entry<Integer, List<ClanSearchPlayerHolder>> integerListEntry : _applicantPlayers.entrySet())
		{
			for(ClanSearchPlayerHolder playerHolder : integerListEntry.getValue())
			{
				if(playerHolder.getCharId() == charId)
				{
					return playerHolder;
				}
			}
		}
		return null;
	}

	@Nullable
	public ClanSearchPlayerHolder getApplicant(int clanId, int charId)
	{
		if(!_applicantPlayers.containsKey(clanId))
		{
			return null;
		}

		for(ClanSearchPlayerHolder player : _applicantPlayers.get(clanId))
		{
			if(player.getCharId() == charId)
			{
				return player;
			}
		}
		return null;
	}

	public boolean isApplicantRegistered(int clanId, int playerId)
	{
		if(!_applicantPlayers.containsKey(clanId))
		{
			return false;
		}

		for(ClanSearchPlayerHolder player : _applicantPlayers.get(clanId))
		{
			if(player.getCharId() == playerId)
			{
				return true;
			}
		}

		return false;
	}

	public boolean isWaiterRegistered(int playerId)
	{
		for(ClanSearchPlayerHolder player : _waitingPlayers)
		{
			if(player.getCharId() == playerId)
			{
				return true;
			}
		}

		return false;
	}

	public boolean addPlayer(ClanSearchPlayerHolder player)
	{
		if(player.isApplicant())
		{
			if(_scheduledTaskExecutor.isApplicantLocked(player.getCharId()))
			{
				return false;
			}

			if(!isApplicantRegistered(player.getPrefferedClanId(), player.getCharId()))
			{
				if(!_applicantPlayers.containsKey(player.getPrefferedClanId()))
				{
					_applicantPlayers.put(player.getPrefferedClanId(), new FastList<>(1));
				}

				_applicantPlayers.get(player.getPrefferedClanId()).add(player);
				_scheduledTaskExecutor.scheduleApplicantForAddition(player);
				return true;
			}
		}
		else
		{
			if(_scheduledTaskExecutor.isWaiterLocked(player.getCharId()))
			{
				return false;
			}

			if(!isWaiterRegistered(player.getCharId()))
			{
				_waitingPlayers.add(player);
				_scheduledTaskExecutor.scheduleWaiterForAddition(player);
			}
		}
		return false;
	}

	public void removeApplicant(int clanId, int charId)
	{
		if(_applicantPlayers.containsKey(clanId))
		{
			for(ClanSearchPlayerHolder player : _applicantPlayers.get(clanId))
			{
				if(player.getCharId() == charId)
				{
					_scheduledTaskExecutor.scheduleApplicantForRemoval(charId);
					_applicantPlayers.get(clanId).remove(player);
					_scheduledTaskExecutor.lockApplicant(charId, APPLICANT_LOCK_TIME);
					return;
				}
			}
		}
	}

	public List<ClanSearchPlayerHolder> listWaiters(RequestPledgeDraftListSearch.ClanWaiterSearchParams params)
	{
		List<ClanSearchPlayerHolder> list = new FastList<>();

		for(ClanSearchPlayerHolder playerHolder : _waitingPlayers)
		{
			if(playerHolder.getCharLevel() < params.getMinLevel() || playerHolder.getCharLevel() > params.getMaxLevel())
			{
				continue;
			}

			if(!params.getRole().isClassRold(playerHolder.getCharClassId()))
			{
				continue;
			}

			if(params.getCharName() != null && !params.getCharName().isEmpty() && !playerHolder.getCharName().toLowerCase().contains(params.getCharName()))
			{
				continue;
			}

			list.add(playerHolder);
		}

		Collections.sort(list, (playerLeft, playerRight) -> {
			int left = 0;
			int right = 0;

			if(params.getSortOrder() == RequestPledgeDraftListSearch.ClanSearchPlayerSortOrder.ASC)
			{
				left = -1;
				right = 1;
			}
			else if(params.getSortOrder() == RequestPledgeDraftListSearch.ClanSearchPlayerSortOrder.DESC)
			{
				left = 1;
				right = -1;
			}

			switch(params.getSortType())
			{
				case SORT_TYPE_NONE:
					return 0;
				case SORT_TYPE_LEVEL:
					return playerLeft.getCharLevel() > playerRight.getCharLevel() ? left : right;
				case SORT_TYPE_NAME:
					return playerLeft.getCharName().compareTo(playerRight.getCharName()) == -1 ? left : right;
				case SORT_TYPE_ROLE:
					return playerLeft.getCharClassId() > playerRight.getCharClassId() ? left : right;
				case SORT_TYPE_SEARCH_TYPE:
					return playerLeft.getSearchType().ordinal() > playerRight.getSearchType().ordinal() ? left : right;
			}
			return 0;
		});

		return list;
	}

	@Nullable
	public List<ClanSearchPlayerHolder> listApplicants(int clanId)
	{
		return _applicantPlayers.get(clanId);
	}

	public boolean removeWaiter(int charId)
	{
		for(ClanSearchPlayerHolder playerHolder : _waitingPlayers)
		{
			if(playerHolder.getCharId() == charId)
			{
				_waitingPlayers.remove(playerHolder);
				_scheduledTaskExecutor.lockWaiter(charId, WAITER_LOCK_TIME);
				return true;
			}
		}
		return false;
	}

	public int getClanLockTime(int clanId)
	{
		return (int) (_scheduledTaskExecutor.getClanLockTime(clanId) / 1000 / 60);
	}

	public int getWaiterLockTime(int charId)
	{
		return (int) (_scheduledTaskExecutor.getWaiterLockTime(charId) / 1000 / 60);
	}

	public int getApplicantLockTime(int charId)
	{
		return (int) (_scheduledTaskExecutor.getApplicantLockTime(charId) / 1000 / 60);
	}

	public void save()
	{
		_scheduledTaskExecutor.run();
	}

	public void shutdown()
	{
		save();
	}

	/**
	 * Распределенный таск для обновления данных в БД.
	 */
	private static class ClanSearchTask implements Runnable
	{
		/**
		 * Новые кланы на сохранение.
		 */
		private static final Map<Integer, ClanSearchClanHolder> _newClans = new FastMap<Integer, ClanSearchClanHolder>().shared();

		/**
		 * Новые ожидающие игроки на сохранение.
		 */
		private static final Map<Integer, ClanSearchPlayerHolder> _newWaiters = new FastMap<Integer, ClanSearchPlayerHolder>().shared();

		/**
		 * Новые игроки, отправившие заявку в клан, на сохранение.
		 */
		private static final Map<Integer, ClanSearchPlayerHolder> _newApplicants = new FastMap<Integer, ClanSearchPlayerHolder>().shared();

		/**
		 * Кланы на удаление из БД.
		 */
		private static final List<Integer> _removalClans = new FastList<Integer>().shared();

		/**
		 * Персонажи (записавшиеся в список ожидания) на удаление из БД.
		 */
		private static final List<Integer> _removalWaiters = new FastList<Integer>().shared();

		/**
		 * Персонажи (отправившие заявку в клан) на удаление из БД.
		 */
		private static final List<Integer> _removalApplicants = new FastList<Integer>().shared();

		/**
		 * Заблокированные для регистрации кланы (обычно на 30 минут).
		 */
		private static final Map<Integer, Long> _clanLocks = new FastMap<Integer, Long>().shared();

		/**
		 * Заблокированные для заявок игроки (обычно 30 минут).
		 */
		private static final Map<Integer, Long> _applicantLocks = new FastMap<Integer, Long>().shared();

		/**
		 * Заблокированные для списка ожидания игроки (обычно 30 минут).
		 */
		private static final Map<Integer, Long> _waiterLocks = new FastMap<Integer, Long>().shared();

		public void scheduleClanForRemoval(int clanId)
		{
			_removalClans.add(clanId);
		}

		public void scheduleWaiterForRemoval(int playerId)
		{
			_removalWaiters.add(playerId);
		}

		public void scheduleApplicantForRemoval(int playerId)
		{
			_removalApplicants.add(playerId);
		}

		public void scheduleClanForAddition(ClanSearchClanHolder clan)
		{
			_newClans.put(clan.getClanId(), clan);
		}

		public void scheduleWaiterForAddition(ClanSearchPlayerHolder player)
		{
			_newWaiters.put(player.getCharId(), player);
		}

		public void scheduleApplicantForAddition(ClanSearchPlayerHolder player)
		{
			_newApplicants.put(player.getCharId(), player);
		}

		@Override
		public void run()
		{
			ThreadConnection conn = null;
			FiltredPreparedStatement st = null;

			try
			{
				conn = L2DatabaseFactory.getInstance().getConnection();
				for(ClanSearchClanHolder clanHolder : _newClans.values())
				{
					st = conn.prepareStatement(ClanSearch.ADD_CLAN);
					st.setInt(1, clanHolder.getClanId());
					st.setString(2, clanHolder.getSearchType().name());
					st.setString(3, clanHolder.getTitle());
					st.setString(4, clanHolder.getDesc());
					st.setString(5, clanHolder.getSearchType().name());
					st.setString(6, clanHolder.getTitle());
					st.setString(7, clanHolder.getDesc());

					st.executeUpdate();
				}
			}
			catch(Exception e)
			{
				failed(e);
			}

			int offset = 0;

			if(!_newWaiters.isEmpty())
			{
				try
				{
					st = conn.prepareStatement(ClanSearch.getAddWaitingPlayerQuery(_newWaiters.size()));
					for(ClanSearchPlayerHolder playerHolder : _newWaiters.values())
					{
						st.setInt(++offset, playerHolder.getCharId());
						st.setString(++offset, playerHolder.getCharName());
						st.setInt(++offset, playerHolder.getCharLevel());
						st.setInt(++offset, playerHolder.getCharClassId());
						st.setString(++offset, playerHolder.getSearchType().name());
					}
					st.executeUpdate();
				}
				catch(Exception e)
				{
					failed(e);
				}
			}

			if(!_newApplicants.isEmpty())
			{
				try
				{
					offset = 0;
					st = conn.prepareStatement(ClanSearch.getAddApplicantPlayerQuery(_newApplicants.size()));
					for(ClanSearchPlayerHolder playerHolder : _newApplicants.values())
					{
						st.setInt(++offset, playerHolder.getCharId());
						st.setInt(++offset, playerHolder.getPrefferedClanId());
						st.setString(++offset, playerHolder.getCharName());
						st.setInt(++offset, playerHolder.getCharLevel());
						st.setInt(++offset, playerHolder.getCharClassId());
						st.setString(++offset, playerHolder.getSearchType().name());
						st.setString(++offset, playerHolder.getDesc());
					}
					st.executeUpdate();
				}
				catch(Exception e)
				{
					failed(e);
				}
			}

			if(!_removalClans.isEmpty())
			{
				try
				{
					offset = 0;
					st = conn.prepareStatement(ClanSearch.getRemoveClanQuery(_removalClans.size()));
					for(int clanId : _removalClans)
					{
						st.setInt(++offset, clanId);
					}
					st.executeUpdate();
				}
				catch(Exception e)
				{
					failed(e);
				}

				// Удалим заявки в кланы, которые удалены из списка.
				try
				{
					offset = 0;
					FiltredPreparedStatement substatement = conn.prepareStatement(ClanSearch.getRemoveClanApplicants(_removalClans.size()));
					for(int clanId : _removalClans)
					{
						substatement.setInt(++offset, clanId);
					}
					substatement.executeUpdate();
				}
				catch(Exception e)
				{
					failed(e);
				}
			}

			if(!_removalWaiters.isEmpty())
			{
				try
				{
					offset = 0;
					st = conn.prepareStatement(ClanSearch.getRemoveWaiterQuery(_removalWaiters.size()));
					for(int playerId : _removalWaiters)
					{
						st.setInt(++offset, playerId);
					}
					st.executeUpdate();
				}
				catch(Exception e)
				{
					failed(e);
				}
			}

			if(!_removalApplicants.isEmpty())
			{
				try
				{
					offset = 0;
					st = conn.prepareStatement(ClanSearch.getRemoveApplicantQuery(_removalApplicants.size()));
					for(Integer charId : _removalApplicants)
					{
						st.setInt(++offset, charId);
					}
					st.executeUpdate();
				}
				catch(Exception e)
				{
					failed(e);
				}
			}

			try
			{
				st = conn.prepareStatement(ClanSearch.CLEAN_CLANS);
				st.executeUpdate();
			}
			catch(Exception e)
			{
				failed(e);
			}

			try
			{
				st = conn.prepareStatement(ClanSearch.CLEAN_APPLICANTS);
				st.executeUpdate();
			}
			catch(Exception e)
			{
				failed(e);
			}

			try
			{
				st = conn.prepareStatement(ClanSearch.CLEAN_WAITERS);
				st.executeUpdate();
			}
			catch(SQLException e)
			{
				failed(e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(conn, st);
			}

			_newClans.clear();
			_newWaiters.clear();
			_newApplicants.clear();
			_removalClans.clear();
			_removalApplicants.clear();
			_removalWaiters.clear();
		}

		private void failed(Exception e)
		{
			_log.error("Failed to update database for clan search system.", e);
		}

		/**
		 * Блокирует возможность клана добавиться в список на @unlockTime милисекунд.
		 * @param clanId ID клана.
		 * @param lockTime Время блокировки в милисекундах.
		 */
		public void lockClan(int clanId, long lockTime)
		{
			_clanLocks.put(clanId, System.currentTimeMillis() + lockTime);
			ThreadPoolManager.getInstance().scheduleGeneral(() -> _clanLocks.remove(clanId), lockTime);
		}

		public boolean isClanLocked(int clanId)
		{
			return _clanLocks.containsKey(clanId);
		}

		public long getClanLockTime(int clanId)
		{
			return _clanLocks.containsKey(clanId) ? Math.max(0, System.currentTimeMillis() - _clanLocks.get(clanId)) : 0;
		}

		public void lockWaiter(int charId, long lockTime)
		{
			_waiterLocks.put(charId, System.currentTimeMillis() + lockTime);
			ThreadPoolManager.getInstance().scheduleGeneral(() -> _waiterLocks.remove(charId), lockTime);
		}

		public boolean isWaiterLocked(int charId)
		{
			return _waiterLocks.containsKey(charId);
		}

		public long getWaiterLockTime(int clanId)
		{
			return _waiterLocks.containsKey(clanId) ? Math.max(0, System.currentTimeMillis() - _waiterLocks.get(clanId)) : 0;
		}

		public void lockApplicant(int charId, long lockTime)
		{
			_applicantLocks.put(charId, System.currentTimeMillis() + lockTime);
			ThreadPoolManager.getInstance().scheduleGeneral(() -> _applicantLocks.remove(charId), lockTime);
		}

		public boolean isApplicantLocked(int charId)
		{
			return _applicantLocks.containsKey(charId);
		}

		public long getApplicantLockTime(int clanId)
		{
			return _applicantLocks.containsKey(clanId) ? Math.max(0, System.currentTimeMillis() - _applicantLocks.get(clanId)) : 0;
		}
	}

	private static class SingletonHolder
	{
		protected static final ClanSearchManager instance = new ClanSearchManager();
	}
}
