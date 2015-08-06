package dwo.gameserver.model.world.worldstat;

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.holders.WorldStatisticHolder;
import dwo.gameserver.model.holders.WorldStatisticStatueHolder;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.items.base.proptypes.SoulshotGrade;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.util.arrays.L2ArrayList;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 14.12.11
 * Time: 2:30
 */
public class WorldStatisticsManager
{
	/**
	 * Сколько игроков должно быть в топе?
	 */
	public static final int TOP_PLAYER_LIMIT = 100;
	private static final Logger _log = LogManager.getLogger(WorldStatisticsManager.class);
	/**
	 * Map[ObjectId] => Ordered List
	 */
	private static FastMap<Integer, WorldStatistic> _currentPlayerStatsMonthly = new FastMap<Integer, WorldStatistic>().shared();
	private static FastMap<Integer, WorldClanStatistic> _currentClanStatsMonthly = new FastMap<Integer, WorldClanStatistic>().shared();
	/**
	 * Map[CategoryId][SubcategoryId][ObjectId] => Ordered List
	 */
	private static FastMap<Integer, Map<Integer, Map<Integer, WorldStatisticHolder>>> _monthlyStats = new FastMap<Integer, Map<Integer, Map<Integer, WorldStatisticHolder>>>().shared();
	private static FastMap<Integer, Map<Integer, Map<Integer, WorldStatisticHolder>>> _generalStats = new FastMap<Integer, Map<Integer, Map<Integer, WorldStatisticHolder>>>().shared();
	private static FastMap<Integer, Map<Integer, Map<Integer, WorldStatisticHolder>>> _monthlyClanStats = new FastMap<Integer, Map<Integer, Map<Integer, WorldStatisticHolder>>>().shared();
	private static FastMap<Integer, Map<Integer, Map<Integer, WorldStatisticHolder>>> _generalClanStats = new FastMap<Integer, Map<Integer, Map<Integer, WorldStatisticHolder>>>().shared();
	private static FastMap<Integer, WorldStatisticStatueHolder> _statues = new FastMap<>();
	private static List<L2Spawn> _statueSpawn = new L2ArrayList<>();

	private WorldStatisticsManager()
	{
		_currentPlayerStatsMonthly.clear();
		_monthlyStats.clear();
		_generalStats.clear();
		_currentClanStatsMonthly.clear();
		loadStats();
	}

	public static WorldStatisticsManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void reLoad()
	{
		_currentPlayerStatsMonthly.clear();
		_currentClanStatsMonthly.clear();
		_monthlyStats.clear();
		_generalStats.clear();
		_monthlyClanStats.clear();
		_generalClanStats.clear();
		loadStats();
	}

	/**
	 * Загрузка всей статистики во время старта сервера
	 * TODO: Здраствуй, дядя "Не знаю паттернов проектирования"
	 */
	private void loadStats()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.LOAD_СURRENT_PLAYER_STATS);
			rset = statement.executeQuery();
			while(rset.next())
			{
				loadCurrentStat(rset);
			}

			statement = con.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.LOAD_СURRENT_CLAN_STATS);
			rset = statement.executeQuery();
			while(rset.next())
			{
				loadCurrentClanStat(rset);
			}

			statement = con.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.LOAD_GENERAL_PLAYER_TOP);
			rset = statement.executeQuery();
			while(rset.next())
			{
				loadTop(rset, true, false);
			}

			statement = con.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.LOAD_GENERAL_CLAN_TOP);
			rset = statement.executeQuery();
			while(rset.next())
			{
				loadTop(rset, true, true);
			}

			statement = con.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.LOAD_MONTHLY_PLAYER_TOP);
			rset = statement.executeQuery();
			while(rset.next())
			{
				loadTop(rset, false, false);
			}

			statement = con.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.LOAD_MONTHLY_CLAN_TOP);
			rset = statement.executeQuery();
			while(rset.next())
			{
				loadTop(rset, false, true);
			}

			statement = con.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.LOAD_STATUES_DATA);
			rset = statement.executeQuery();
			while(rset.next())
			{
				loadStatues(rset);
			}

			_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _currentPlayerStatsMonthly.size() + " current Characters World Statistics.");
			_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _currentClanStatsMonthly.size() + " current Clan World Statistics.");
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "WorldStatisticsManager: Failed loading World Statistics.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	private void loadStatues(ResultSet rset)
	{
		try
		{
			while(rset.next())
			{
				int catId = rset.getInt("cat_id");
				_statues.put(catId, new WorldStatisticStatueHolder(rset));
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.WARN, "Failed loading world statistic statues data", e);
		}
	}

	public WorldStatisticStatueHolder getStatue(CategoryType type)
	{
		return _statues.containsKey(type.getClientId()) ? _statues.get(type.getClientId()) : null;
	}

	private void loadTop(ResultSet rset, boolean isGeneralTop, boolean isClanStat) throws SQLException
	{
		int categoryId = rset.getInt("categoryId");
		int subcategoryId = rset.getInt("subCategoryId");
		short place = rset.getShort("place");
		int charId = rset.getInt("charId");
		String charName = rset.getString("char_name");
		long statValue = rset.getLong("statValue");

		Map<Integer, Map<Integer, Map<Integer, WorldStatisticHolder>>> container = isGeneralTop ? isClanStat ? _generalClanStats : _generalStats : isClanStat ? _monthlyClanStats : _monthlyStats;

		if(!container.containsKey(categoryId))
		{
			container.put(categoryId, new HashMap<>());
		}

		if(!container.get(categoryId).containsKey(subcategoryId))
		{
			container.get(categoryId).put(subcategoryId, new FastMap<>());
		}

		if(isClanStat)
		{
			L2Clan clan = ClanTable.getInstance().getClan(charId);
			if(clan != null)
			{
				container.get(categoryId).get(subcategoryId).put(charId, new WorldStatisticHolder(categoryId, subcategoryId, place, charId, charName, statValue, clan.getCrestId()));
			}
		}
		else
		{
			container.get(categoryId).get(subcategoryId).put(charId, new WorldStatisticHolder(categoryId, subcategoryId, place, charId, charName, statValue));
		}
	}

	private void loadCurrentClanStat(ResultSet rset) throws SQLException
	{
		WorldClanStatistic stat;
		int clanId;

		clanId = rset.getInt("charId");

		stat = new WorldClanStatistic();
		stat.setClanId(clanId);

		// CategoryGeneral
		stat.setClanMembersCount(rset.getLong(CategoryType.MEMBERS_COUNT.getFieldName()));
		stat.setClanInvitesCount(rset.getLong(CategoryType.INVITED_COUNT.getFieldName()));
		stat.setClanLeavedCount(rset.getLong(CategoryType.LEAVED_COUNT.getFieldName()));
		stat.setClanAdenaCount(rset.getLong(CategoryType.ADENA_COUNT_IN_WH.getFieldName()));
		stat.setClanPvpCount(rset.getLong(CategoryType.ALL_CLAN_PVP_COUNT.getFieldName()));
		stat.setClanWarWinCount(rset.getLong(CategoryType.CLAN_WAR_WIN_COUNT.getFieldName()));

		stat.setNeedToUpdate(false);

		_currentClanStatsMonthly.put(clanId, stat);
	}

	private void loadCurrentStat(ResultSet rset) throws SQLException
	{
		WorldStatistic stat;
		int charId;

		charId = rset.getInt("charId");

		stat = new WorldStatistic();
		stat.setCharId(charId);

		// CategoryGeneral
		stat.setExpAdded(rset.getLong(CategoryType.EXP_ADDED.getFieldName()));
		stat.setAdenaAdded(rset.getLong(CategoryType.ADENA_ADDED.getFieldName()));
		stat.setTimePlayed(rset.getLong(CategoryType.TIME_PLAYED.getFieldName()));
		stat.setTimeInBattle(rset.getLong(CategoryType.TIME_IN_BATTLE.getFieldName()));
		stat.setTimeInParty(rset.getLong(CategoryType.TIME_IN_PARTY.getFieldName()));
		stat.setTimeInFullParty(rset.getLong(CategoryType.TIME_IN_FULLPARTY.getFieldName()));

		stat.setWeaponEnchantMax(CrystalGrade.D, rset.getLong(CategoryType.WEAPON_ENCHANT_MAX_D.getFieldName()));
		stat.setWeaponEnchantMax(CrystalGrade.C, rset.getLong(CategoryType.WEAPON_ENCHANT_MAX_C.getFieldName()));
		stat.setWeaponEnchantMax(CrystalGrade.B, rset.getLong(CategoryType.WEAPON_ENCHANT_MAX_B.getFieldName()));
		stat.setWeaponEnchantMax(CrystalGrade.A, rset.getLong(CategoryType.WEAPON_ENCHANT_MAX_A.getFieldName()));
		stat.setWeaponEnchantMax(CrystalGrade.S, rset.getLong(CategoryType.WEAPON_ENCHANT_MAX_S.getFieldName()));
		stat.setWeaponEnchantMax(CrystalGrade.S80, rset.getLong(CategoryType.WEAPON_ENCHANT_MAX_S80.getFieldName()));
		stat.setWeaponEnchantMax(CrystalGrade.R, rset.getLong(CategoryType.WEAPON_ENCHANT_MAX_R.getFieldName()));
		stat.setWeaponEnchantMax(CrystalGrade.R95, rset.getLong(CategoryType.WEAPON_ENCHANT_MAX_R95.getFieldName()));
		stat.setWeaponEnchantMax(CrystalGrade.R99, rset.getLong(CategoryType.WEAPON_ENCHANT_MAX_R99.getFieldName()));

		stat.setArmorEnchantMax(CrystalGrade.D, rset.getLong(CategoryType.ARMOR_ENCHANT_MAX_D.getFieldName()));
		stat.setArmorEnchantMax(CrystalGrade.C, rset.getLong(CategoryType.ARMOR_ENCHANT_MAX_C.getFieldName()));
		stat.setArmorEnchantMax(CrystalGrade.B, rset.getLong(CategoryType.ARMOR_ENCHANT_MAX_B.getFieldName()));
		stat.setArmorEnchantMax(CrystalGrade.A, rset.getLong(CategoryType.ARMOR_ENCHANT_MAX_A.getFieldName()));
		stat.setArmorEnchantMax(CrystalGrade.S, rset.getLong(CategoryType.ARMOR_ENCHANT_MAX_S.getFieldName()));
		stat.setArmorEnchantMax(CrystalGrade.S80, rset.getLong(CategoryType.ARMOR_ENCHANT_MAX_S80.getFieldName()));
		stat.setArmorEnchantMax(CrystalGrade.R, rset.getLong(CategoryType.ARMOR_ENCHANT_MAX_R.getFieldName()));
		stat.setArmorEnchantMax(CrystalGrade.R95, rset.getLong(CategoryType.ARMOR_ENCHANT_MAX_R95.getFieldName()));
		stat.setArmorEnchantMax(CrystalGrade.R99, rset.getLong(CategoryType.ARMOR_ENCHANT_MAX_R99.getFieldName()));

		stat.setWeaponEnchantTry(CrystalGrade.D, rset.getLong(CategoryType.WEAPON_ENCHANT_TRY_D.getFieldName()));
		stat.setWeaponEnchantTry(CrystalGrade.C, rset.getLong(CategoryType.WEAPON_ENCHANT_TRY_C.getFieldName()));
		stat.setWeaponEnchantTry(CrystalGrade.B, rset.getLong(CategoryType.WEAPON_ENCHANT_TRY_B.getFieldName()));
		stat.setWeaponEnchantTry(CrystalGrade.A, rset.getLong(CategoryType.WEAPON_ENCHANT_TRY_A.getFieldName()));
		stat.setWeaponEnchantTry(CrystalGrade.S, rset.getLong(CategoryType.WEAPON_ENCHANT_TRY_S.getFieldName()));
		stat.setWeaponEnchantTry(CrystalGrade.S80, rset.getLong(CategoryType.WEAPON_ENCHANT_TRY_S80.getFieldName()));
		stat.setWeaponEnchantTry(CrystalGrade.R, rset.getLong(CategoryType.WEAPON_ENCHANT_TRY_R.getFieldName()));
		stat.setWeaponEnchantTry(CrystalGrade.R95, rset.getLong(CategoryType.WEAPON_ENCHANT_TRY_R95.getFieldName()));
		stat.setWeaponEnchantTry(CrystalGrade.R99, rset.getLong(CategoryType.WEAPON_ENCHANT_TRY_R99.getFieldName()));

		stat.setArmorEnchantTry(CrystalGrade.D, rset.getLong(CategoryType.ARMOR_ENCHANT_TRY_D.getFieldName()));
		stat.setArmorEnchantTry(CrystalGrade.C, rset.getLong(CategoryType.ARMOR_ENCHANT_TRY_C.getFieldName()));
		stat.setArmorEnchantTry(CrystalGrade.B, rset.getLong(CategoryType.ARMOR_ENCHANT_TRY_B.getFieldName()));
		stat.setArmorEnchantTry(CrystalGrade.A, rset.getLong(CategoryType.ARMOR_ENCHANT_TRY_A.getFieldName()));
		stat.setArmorEnchantTry(CrystalGrade.S, rset.getLong(CategoryType.ARMOR_ENCHANT_TRY_S.getFieldName()));
		stat.setArmorEnchantTry(CrystalGrade.S80, rset.getLong(CategoryType.ARMOR_ENCHANT_TRY_S80.getFieldName()));
		stat.setArmorEnchantTry(CrystalGrade.R, rset.getLong(CategoryType.ARMOR_ENCHANT_TRY_R.getFieldName()));
		stat.setArmorEnchantTry(CrystalGrade.R95, rset.getLong(CategoryType.ARMOR_ENCHANT_TRY_R95.getFieldName()));
		stat.setArmorEnchantTry(CrystalGrade.R99, rset.getLong(CategoryType.ARMOR_ENCHANT_TRY_R99.getFieldName()));

		stat.setSsConsumed(SoulshotGrade.SS_D, rset.getLong(CategoryType.SS_CONSUMED_D.getFieldName()));
		stat.setSsConsumed(SoulshotGrade.SS_C, rset.getLong(CategoryType.SS_CONSUMED_C.getFieldName()));
		stat.setSsConsumed(SoulshotGrade.SS_B, rset.getLong(CategoryType.SS_CONSUMED_B.getFieldName()));
		stat.setSsConsumed(SoulshotGrade.SS_A, rset.getLong(CategoryType.SS_CONSUMED_A.getFieldName()));
		stat.setSsConsumed(SoulshotGrade.SS_S, rset.getLong(CategoryType.SS_CONSUMED_S.getFieldName()));
		stat.setSsConsumed(SoulshotGrade.SS_R, rset.getLong(CategoryType.SS_CONSUMED_R.getFieldName()));

		stat.setSsConsumed(SoulshotGrade.SS_D, rset.getLong(CategoryType.SPS_CONSUMED_D.getFieldName()));
		stat.setSsConsumed(SoulshotGrade.SS_C, rset.getLong(CategoryType.SPS_CONSUMED_C.getFieldName()));
		stat.setSsConsumed(SoulshotGrade.SS_B, rset.getLong(CategoryType.SPS_CONSUMED_B.getFieldName()));
		stat.setSsConsumed(SoulshotGrade.SS_A, rset.getLong(CategoryType.SPS_CONSUMED_A.getFieldName()));
		stat.setSsConsumed(SoulshotGrade.SS_S, rset.getLong(CategoryType.SPS_CONSUMED_S.getFieldName()));
		stat.setSsConsumed(SoulshotGrade.SS_R, rset.getLong(CategoryType.SPS_CONSUMED_R.getFieldName()));

		stat.setPrivateSellCount(rset.getLong(CategoryType.PRIVATE_SELL_COUNT.getFieldName()));
		stat.setQuestsCompleted(rset.getLong(CategoryType.QUESTS_COMPLETED.getFieldName()));
		stat.setResurrectedCharCount(rset.getLong(CategoryType.RESURRECTED_CHAR_COUNT.getFieldName()));
		stat.setResurrectedByOtherCount(rset.getLong(CategoryType.RESURRECTED_BY_OTHER_COUNT.getFieldName()));
		stat.setDieCount(rset.getLong(CategoryType.DIE_COUNT.getFieldName()));

		// CategoryMonster
		stat.setMonstersKilled(rset.getLong(CategoryType.MONSTERS_KILLED.getFieldName()));
		stat.setExpFromMonsters(rset.getLong(CategoryType.EXP_FROM_MONSTERS.getFieldName()));
		stat.setMaxDamageToMonster(rset.getLong(CategoryType.DAMAGE_TO_MONSTERS_MAX.getFieldName()));
		stat.setAllDamageToMonster(rset.getLong(CategoryType.DAMAGE_TO_MONSTERS.getFieldName()));
		stat.setAllDamageFromMonster(rset.getLong(CategoryType.DAMAGE_FROM_MONSTERS.getFieldName()));
		stat.setKilledByMonsterCount(rset.getLong(CategoryType.KILLED_BY_MONSTER_COUNT.getFieldName()));

		stat.setRaidKilled(25774, rset.getLong(CategoryType.EPIC_BOSS_KILLS_25774.getFieldName()));
		stat.setRaidKilled(25785, rset.getLong(CategoryType.EPIC_BOSS_KILLS_25785.getFieldName()));
		stat.setRaidKilled(29195, rset.getLong(CategoryType.EPIC_BOSS_KILLS_29195.getFieldName()));
		stat.setRaidKilled(25779, rset.getLong(CategoryType.EPIC_BOSS_KILLS_25779.getFieldName()));
		stat.setRaidKilled(25866, rset.getLong(CategoryType.EPIC_BOSS_KILLS_25866.getFieldName()));
		stat.setRaidKilled(29194, rset.getLong(CategoryType.EPIC_BOSS_KILLS_29194.getFieldName()));
		stat.setRaidKilled(29218, rset.getLong(CategoryType.EPIC_BOSS_KILLS_29218.getFieldName()));
		stat.setRaidKilled(29213, rset.getLong(CategoryType.EPIC_BOSS_KILLS_29213.getFieldName()));
		stat.setRaidKilled(29196, rset.getLong(CategoryType.EPIC_BOSS_KILLS_29196.getFieldName()));
		stat.setRaidKilled(25867, rset.getLong(CategoryType.EPIC_BOSS_KILLS_25867.getFieldName()));
		stat.setRaidKilled(29212, rset.getLong(CategoryType.EPIC_BOSS_KILLS_29212.getFieldName()));
		stat.setRaidKilled(29197, rset.getLong(CategoryType.EPIC_BOSS_KILLS_29197.getFieldName()));

		// CategoryPvp
		stat.setPkCount(rset.getLong(CategoryType.PK_COUNT.getFieldName()));
		stat.setPvpCount(rset.getLong(CategoryType.PVP_COUNT.getFieldName()));
		stat.setKilledByPkCount(rset.getLong(CategoryType.KILLED_BY_PK_COUNT.getFieldName()));
		stat.setKilledInPvpCount(rset.getLong(CategoryType.KILLED_IN_PVP_COUNT.getFieldName()));
		stat.setMaxDamageToPc(rset.getLong(CategoryType.DAMAGE_TO_PC_MAX.getFieldName()));
		stat.setAllDamageToPc(rset.getLong(CategoryType.DAMAGE_TO_PC.getFieldName()));
		stat.setAllDamageFromPc(rset.getLong(CategoryType.DAMAGE_FROM_PC.getFieldName()));

		stat.setNeedToUpdate(false);

		_currentPlayerStatsMonthly.put(charId, stat);
	}

	/**
	 * Обновление определенной статистики клана в памяти.
	 *
	 * @param clanId ObjectID клана.
	 * @param category категория.
	 * @param subCategory подкатегория.
	 * @param valueAdd значение, на которое статистика возрастет или текущее значение.
	 */
	public void updateClanStat(int clanId, CategoryType category, Object subCategory, long valueAdd)
	{
		WorldClanStatistic statistic = _currentClanStatsMonthly.get(clanId);
		if(statistic == null)
		{
			statistic = createStatisticForNewClan(clanId);
		}
		switch(category)
		{
			case MEMBERS_COUNT:
				if(statistic.getClanMembersCount() < valueAdd)
				{
					statistic.setClanMembersCount(valueAdd);
				}
				break;
			case INVITED_COUNT:
				statistic.setClanInvitesCount(valueAdd);
				break;
			case LEAVED_COUNT:
				statistic.setClanLeavedCount(valueAdd);
				break;
			case REPUTATION_COUNT:
				statistic.setReputationCount(valueAdd);
				break;
			case ADENA_COUNT_IN_WH:
				statistic.setClanAdenaCount(valueAdd);
				break;
			case ALL_CLAN_PVP_COUNT:
				statistic.setClanPvpCount(valueAdd);
				break;
			case CLAN_WAR_WIN_COUNT:
				statistic.setClanWarWinCount(valueAdd);
				break;
		}
		_currentClanStatsMonthly.put(clanId, statistic);
	}

	/**
	 * Обновление определенной статистики игрока в памяти.
	 *
	 * @param charId ObjectID игрока
	 * @param category категория
	 * @param subCategory подкатегория
	 * @param valueAdd значение, на которое статистика возрастет
	 */
	public void updateStat(int charId, CategoryType category, Object subCategory, long valueAdd)
	{
		WorldStatistic statistic = _currentPlayerStatsMonthly.get(charId);
		if(statistic == null)
		{
			statistic = createStatisticForNewPlayer(charId);
		}
		switch(category)
		{
			// GENERAL
			case EXP_ADDED: // DONE
				statistic.setExpAdded(valueAdd);
				break;
			case ADENA_ADDED: // DONE
				statistic.setAdenaAdded(valueAdd);
				break;
			case TIME_PLAYED: // DONE
				statistic.setTimePlayed(valueAdd);
				break;
			case TIME_IN_BATTLE: // TODO
				statistic.setTimeInBattle(valueAdd);
				break;
			case TIME_IN_PARTY: // DONE
				statistic.setTimeInParty(valueAdd);
				break;
			case TIME_IN_FULLPARTY: // DONE
				statistic.setTimeInFullParty(valueAdd);
				break;
			case WEAPON_ENCHANT_MAX: // DONE
			case WEAPON_ENCHANT_TRY: // DONE
				if(subCategory instanceof CrystalGrade)
				{
					statistic.addWeaponEnchantTry((CrystalGrade) subCategory);
					statistic.setWeaponEnchantMax((CrystalGrade) subCategory, valueAdd);
				}
				break;
			case ARMOR_ENCHANT_TRY: // DONE
			case ARMOR_ENCHANT_MAX: // DONE
				if(subCategory instanceof CrystalGrade)
				{
					statistic.addArmorEnchantTry((CrystalGrade) subCategory);
					statistic.setArmorEnchantMax((CrystalGrade) subCategory, valueAdd);
				}
				break;
			case PRIVATE_SELL_COUNT: // DONE
				statistic.setPrivateSellCount(valueAdd);
				break;
			case QUESTS_COMPLETED: // DONE
				statistic.setQuestsCompleted(valueAdd);
				break;
			case SS_CONSUMED: // DONE
				if(subCategory instanceof SoulshotGrade)
				{
					statistic.setSsConsumed((SoulshotGrade) subCategory, valueAdd);
				}
				break;
			case SPS_CONSUMED: // DONE
				if(subCategory instanceof SoulshotGrade)
				{
					statistic.setSpsConsumed((SoulshotGrade) subCategory, valueAdd);
				}
				break;
			case RESURRECTED_CHAR_COUNT: // DONE
				statistic.setResurrectedCharCount(valueAdd);
				break;
			case RESURRECTED_BY_OTHER_COUNT: // DONE
				statistic.setResurrectedByOtherCount(valueAdd);
				break;
			case DIE_COUNT: // DONE
				statistic.setDieCount(valueAdd);
				break;
			//MONSTER
			case MONSTERS_KILLED: // DONE
				statistic.setMonstersKilled(valueAdd);
				break;
			case EXP_FROM_MONSTERS: // DONE
				statistic.setExpFromMonsters(valueAdd);
				break;
			case DAMAGE_TO_MONSTERS_MAX: // DONE
			case DAMAGE_TO_MONSTERS:
				statistic.notifyDamageToMonster(valueAdd);
				break;
			case DAMAGE_FROM_MONSTERS: // DONE
				statistic.setAllDamageFromMonster(valueAdd);
				break;
			case KILLED_BY_MONSTER_COUNT: // DONE
				statistic.setKilledByMonsterCount(valueAdd);
				break;

			// RAID
			case EPIC_BOSS_KILLS: // DONE
				if(subCategory instanceof Integer)
				{
					statistic.setRaidKilled((Integer) subCategory, valueAdd);
				}
				break;

			// PVP
			case PK_COUNT: // DONE
				statistic.setPkCount(valueAdd);
				break;
			case PVP_COUNT: // DONE
				statistic.setPvpCount(valueAdd);
				break;
			case KILLED_IN_PVP_COUNT: // DONE
				statistic.setKilledInPvpCount(valueAdd);
				break;
			case DAMAGE_TO_PC_MAX: // DONE
			case DAMAGE_TO_PC:
				statistic.notifyDamageToPc(valueAdd);
				break;
			case DAMAGE_FROM_PC:  // DONE
				statistic.setAllDamageFromPc(valueAdd);
				break;
		}
		_currentPlayerStatsMonthly.put(charId, statistic);
	}

	/**
	 * Обновление статов в базе для всех персонажей.
	 * Используется таском.
	 */
	public void updateAllStatsInDb()
	{
		for(WorldStatistic stat : _currentPlayerStatsMonthly.values())
		{
			stat.updateStatsInDb();
		}

		for(WorldClanStatistic stat : _currentClanStatsMonthly.values())
		{
			stat.updateStatsInDb();
		}
	}

	/**
	 * @return всю текущую статистику на сервере
	 */
	public FastMap<Integer, WorldStatistic> getAllCurrentStatistics()
	{
		return _currentPlayerStatsMonthly;
	}

	/**
	 * @return всю текущую статистику на сервере по кланам.
	 */
	public Collection<WorldClanStatistic> getAllCurrentClanStatistics()
	{
		return _currentClanStatsMonthly.values();
	}

	/**
	 * @return Получаем всю статистику игрока за месяц
	 */
	public WorldStatistic getCurrentStatisticsForPlayerMonthly(int objId)
	{
		return _currentPlayerStatsMonthly.get(objId);
	}

	/**
	 * @return Получаем всю статистику клана за месяц
	 */
	public WorldClanStatistic getCurrentStatisticsForClanMonthly(int objId)
	{
		return _currentClanStatsMonthly.get(objId);
	}

	/**
	 * @return Получаем всю статистику игрока за все время
	 */
	public long getStatisticsForPlayerGeneral(int CategoryId, int SubcategoryId, int objectId)
	{
		try
		{
			return _generalStats.get(CategoryId).get(SubcategoryId).get(objectId).getValue();
		}
		catch(Exception ignored)
		{
			return 0;
		}
	}

	/**
	 * @return Получаем всю статистику игрока за все время
	 */
	public long getStatisticsForClanGeneral(int CategoryId, int SubcategoryId, int objectId)
	{
		try
		{
			return _generalClanStats.get(CategoryId).get(SubcategoryId).get(objectId).getValue();
		}
		catch(Exception ignored)
		{
			return 0;
		}
	}

	/**
	 * Получаем список статистики в топе.
	 *
	 * @param cat Категория.
	 * @param isGeneral Общая или месячная статистика?
	 * @return Упорядоченный по возрастанию места список статистики.
	 */
	public Collection<WorldStatisticHolder> getStatisticTop(CategoryType cat, boolean isGeneral)
	{
		Map<Integer, Map<Integer, Map<Integer, WorldStatisticHolder>>> container = isGeneral ? cat.isClanStatistic() ? _generalClanStats : _generalStats : cat.isClanStatistic() ? _monthlyClanStats : _monthlyStats;

		return container.containsKey(cat.getClientId()) && container.get(cat.getClientId()).containsKey(cat.getSubcat()) ? container.get(cat.getClientId()).get(cat.getSubcat()).values() : null;
	}

	/**
	 * Создает статистику дял нового персонажа
	 * @param charId Object Id персонажа
	 * @return созданную статистику
	 */
	public WorldStatistic createStatisticForNewPlayer(int charId)
	{
		WorldStatistic statistic = new WorldStatistic();
		statistic.setCharId(charId);
		statistic.insertStatsInDb();
		_currentPlayerStatsMonthly.put(charId, statistic);
		return statistic;
	}

	/**
	 * Создает статистику дял нового клана
	 * @param clanId Object Id клана
	 * @return созданную статистику
	 */
	public WorldClanStatistic createStatisticForNewClan(int clanId)
	{
		if(!_currentClanStatsMonthly.containsKey(clanId))
		{
			WorldClanStatistic statistic = new WorldClanStatistic();
			statistic.setClanId(clanId);
			statistic.insertStatsInDb();
			_currentClanStatsMonthly.put(clanId, statistic);
			return statistic;
		}
		return _currentClanStatsMonthly.get(clanId);
	}

	/*
	 	Составляем массив для отправки клиенту для личной статистики.
	 */
	public List<StatisticContainer> getStatUser(int objectId, int clanId)
	{
		List<StatisticContainer> listStat = new ArrayList<>();
		WorldStatistic monthlyStatistic = getCurrentStatisticsForPlayerMonthly(objectId);

		//Обычный вид
		listStat.add(new StatisticContainer(CategoryType.EXP_ADDED, 0, monthlyStatistic.getExpAdded(), getStatisticsForPlayerGeneral(CategoryType.EXP_ADDED.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.ADENA_ADDED, 0, monthlyStatistic.getAdenaAdded(), getStatisticsForPlayerGeneral(CategoryType.ADENA_ADDED.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.TIME_PLAYED, 0, monthlyStatistic.getTimePlayed(), getStatisticsForPlayerGeneral(CategoryType.TIME_PLAYED.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.TIME_IN_BATTLE, 0, monthlyStatistic.getTimeInBattle(), getStatisticsForPlayerGeneral(CategoryType.TIME_IN_BATTLE.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.TIME_IN_PARTY, 0, monthlyStatistic.getTimeInParty(), getStatisticsForPlayerGeneral(CategoryType.TIME_IN_PARTY.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.TIME_IN_FULLPARTY, 0, monthlyStatistic.getTimeInFullParty(), getStatisticsForPlayerGeneral(CategoryType.TIME_IN_FULLPARTY.getClientId(), 0, objectId)));

		for(CrystalGrade grade : CrystalGrade.values())
		{
			if(grade != CrystalGrade.S84)
			{
				listStat.add(new StatisticContainer(CategoryType.WEAPON_ENCHANT_MAX, grade.ordinal(), monthlyStatistic.getWeaponEnchantMax(grade), getStatisticsForPlayerGeneral(CategoryType.WEAPON_ENCHANT_MAX.getClientId(), grade.ordinal(), objectId)));
				listStat.add(new StatisticContainer(CategoryType.ARMOR_ENCHANT_MAX, grade.ordinal(), monthlyStatistic.getArmorEnchantMax(grade), getStatisticsForPlayerGeneral(CategoryType.ARMOR_ENCHANT_MAX.getClientId(), grade.ordinal(), objectId)));
				listStat.add(new StatisticContainer(CategoryType.WEAPON_ENCHANT_TRY, grade.ordinal(), monthlyStatistic.getWeaponEnchantTry(grade), getStatisticsForPlayerGeneral(CategoryType.WEAPON_ENCHANT_TRY.getClientId(), grade.ordinal(), objectId)));
				listStat.add(new StatisticContainer(CategoryType.ARMOR_ENCHANT_TRY, grade.ordinal(), monthlyStatistic.getArmorEnchantTry(grade), getStatisticsForPlayerGeneral(CategoryType.ARMOR_ENCHANT_TRY.getClientId(), grade.ordinal(), objectId)));
			}
		}

		listStat.add(new StatisticContainer(CategoryType.PRIVATE_SELL_COUNT, 0, monthlyStatistic.getPrivateSellCount(), getStatisticsForPlayerGeneral(CategoryType.PRIVATE_SELL_COUNT.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.QUESTS_COMPLETED, 0, monthlyStatistic.getQuestsCompleted(), getStatisticsForPlayerGeneral(CategoryType.QUESTS_COMPLETED.getClientId(), 0, objectId)));

		listStat.add(new StatisticContainer(CategoryType.RESURRECTED_CHAR_COUNT, 0, monthlyStatistic.getResurrectedByOtherCount(), getStatisticsForPlayerGeneral(CategoryType.RESURRECTED_CHAR_COUNT.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.RESURRECTED_BY_OTHER_COUNT, 0, monthlyStatistic.getResurrectedCharCount(), getStatisticsForPlayerGeneral(CategoryType.RESURRECTED_BY_OTHER_COUNT.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.DIE_COUNT, 0, monthlyStatistic.getDieCount(), getStatisticsForPlayerGeneral(CategoryType.DIE_COUNT.getClientId(), 0, objectId)));

		listStat.add(new StatisticContainer(CategoryType.SS_CONSUMED_D, 1, monthlyStatistic.getSsConsumed(SoulshotGrade.SS_D), getStatisticsForPlayerGeneral(CategoryType.SS_CONSUMED_D.getClientId(), 1, objectId)));
		listStat.add(new StatisticContainer(CategoryType.SPS_CONSUMED_D, 1, monthlyStatistic.getSpsConsumed(SoulshotGrade.SS_D), getStatisticsForPlayerGeneral(CategoryType.SPS_CONSUMED_D.getClientId(), 1, objectId)));
		listStat.add(new StatisticContainer(CategoryType.SS_CONSUMED_C, 2, monthlyStatistic.getSsConsumed(SoulshotGrade.SS_C), getStatisticsForPlayerGeneral(CategoryType.SS_CONSUMED_C.getClientId(), 2, objectId)));
		listStat.add(new StatisticContainer(CategoryType.SPS_CONSUMED_C, 2, monthlyStatistic.getSpsConsumed(SoulshotGrade.SS_C), getStatisticsForPlayerGeneral(CategoryType.SPS_CONSUMED_C.getClientId(), 2, objectId)));
		listStat.add(new StatisticContainer(CategoryType.SS_CONSUMED_B, 3, monthlyStatistic.getSsConsumed(SoulshotGrade.SS_B), getStatisticsForPlayerGeneral(CategoryType.SS_CONSUMED_B.getClientId(), 3, objectId)));
		listStat.add(new StatisticContainer(CategoryType.SPS_CONSUMED_B, 3, monthlyStatistic.getSpsConsumed(SoulshotGrade.SS_B), getStatisticsForPlayerGeneral(CategoryType.SPS_CONSUMED_B.getClientId(), 3, objectId)));
		listStat.add(new StatisticContainer(CategoryType.SS_CONSUMED_A, 4, monthlyStatistic.getSsConsumed(SoulshotGrade.SS_A), getStatisticsForPlayerGeneral(CategoryType.SS_CONSUMED_A.getClientId(), 4, objectId)));
		listStat.add(new StatisticContainer(CategoryType.SPS_CONSUMED_A, 4, monthlyStatistic.getSpsConsumed(SoulshotGrade.SS_A), getStatisticsForPlayerGeneral(CategoryType.SPS_CONSUMED_A.getClientId(), 4, objectId)));
		listStat.add(new StatisticContainer(CategoryType.SS_CONSUMED_S, 5, monthlyStatistic.getSsConsumed(SoulshotGrade.SS_S), getStatisticsForPlayerGeneral(CategoryType.SS_CONSUMED_S.getClientId(), 5, objectId)));
		listStat.add(new StatisticContainer(CategoryType.SPS_CONSUMED_S, 5, monthlyStatistic.getSpsConsumed(SoulshotGrade.SS_S), getStatisticsForPlayerGeneral(CategoryType.SPS_CONSUMED_S.getClientId(), 5, objectId)));
		listStat.add(new StatisticContainer(CategoryType.SS_CONSUMED_R, 8, monthlyStatistic.getSsConsumed(SoulshotGrade.SS_R), getStatisticsForPlayerGeneral(CategoryType.SS_CONSUMED_R.getClientId(), 8, objectId)));
		listStat.add(new StatisticContainer(CategoryType.SPS_CONSUMED_R, 8, monthlyStatistic.getSpsConsumed(SoulshotGrade.SS_R), getStatisticsForPlayerGeneral(CategoryType.SPS_CONSUMED_R.getClientId(), 8, objectId)));

		//Вид Охотничьего Угодья
		/*
					1003 144  25563  33812 //Максимальный урон по NPC 144-Заклинатель Иса
					15 144  25563  33812 //Максимальный урон по NPC  144-Заклинатель Иса
					16 144  23495904  32605322 //Накопленный урон по NPC 144-Заклинатель Иса
					1004 144  23495904  32534506 //Накопленный урон по NPC 144-Заклинатель Иса
					17 144  1551190  2735247 //Накопленный ущерб от NPC 144-Заклинатель Иса
					1005 144  1551190  2532498 //Накопленный ущерб от NPC 144-Заклинатель Иса
						DAMAGE_TO_MONSTERS_MAX(1003),	// 1003 TODO: Разбитие по категориям 4-ых проф (от 139 до 146)
						DAMAGE_TO_MONSTERS(1004),		// 1004 TODO: Разбитие по категориям 4-ых проф (от 139 до 146)
						DAMAGE_FROM_MONSTERS(1005),		// 1005 TODO: Разбитие по категориям 4-ых проф (от 139 до 146)

				  */

		listStat.add(new StatisticContainer(CategoryType.MONSTERS_KILLED, 0, monthlyStatistic.getMonstersKilled(), getStatisticsForPlayerGeneral(CategoryType.MONSTERS_KILLED.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.EXP_FROM_MONSTERS, 0, monthlyStatistic.getExpFromMonsters(), getStatisticsForPlayerGeneral(CategoryType.EXP_FROM_MONSTERS.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.KILLED_BY_MONSTER_COUNT, 0, monthlyStatistic.getKilledByMonsterCount(), getStatisticsForPlayerGeneral(CategoryType.KILLED_BY_MONSTER_COUNT.getClientId(), 0, objectId)));

		//Вид Рейда
		for(int npcId : new int[]{25774, 25785, 29195, 25779, 25866, 29194, 29218, 29213, 29196, 25867, 29212, 29197})
		{
			listStat.add(new StatisticContainer(CategoryType.EPIC_BOSS_KILLS, 1000000 + npcId, monthlyStatistic.getEpicKilled(npcId), getStatisticsForPlayerGeneral(CategoryType.EPIC_BOSS_KILLS.getClientId(), npcId, objectId)));
		}

		// Вид ПВП
		listStat.add(new StatisticContainer(CategoryType.KILLED_BY_PK_COUNT, 0, monthlyStatistic.getKilledByPkCount(), getStatisticsForPlayerGeneral(CategoryType.KILLED_BY_PK_COUNT.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.KILLED_IN_PVP_COUNT, 0, monthlyStatistic.getKilledInPvpCount(), getStatisticsForPlayerGeneral(CategoryType.KILLED_IN_PVP_COUNT.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.PK_COUNT, 0, monthlyStatistic.getPkCount(), getStatisticsForPlayerGeneral(CategoryType.PK_COUNT.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.PVP_COUNT, 0, monthlyStatistic.getPvpCount(), getStatisticsForPlayerGeneral(CategoryType.PVP_COUNT.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.DAMAGE_TO_PC, 0, monthlyStatistic.getAllDamageToPc(), getStatisticsForPlayerGeneral(CategoryType.DAMAGE_TO_PC.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.DAMAGE_FROM_PC, 0, monthlyStatistic.getAllDamageFromPc(), getStatisticsForPlayerGeneral(CategoryType.DAMAGE_FROM_PC.getClientId(), 0, objectId)));
		listStat.add(new StatisticContainer(CategoryType.DAMAGE_FROM_PC, 0, monthlyStatistic.getMaxDamageToPc(), getStatisticsForPlayerGeneral(CategoryType.DAMAGE_FROM_PC.getClientId(), 0, objectId)));

		// Вид клана
		if(clanId > 0)
		{
			WorldClanStatistic clanMonthlyStatistic = getCurrentStatisticsForClanMonthly(clanId);
			if(clanMonthlyStatistic != null)
			{
				listStat.add(new StatisticContainer(CategoryType.MEMBERS_COUNT, 0, clanMonthlyStatistic.getClanMembersCount(), getStatisticsForClanGeneral(CategoryType.MEMBERS_COUNT.getClientId(), 0, clanId)));
				listStat.add(new StatisticContainer(CategoryType.INVITED_COUNT, 0, clanMonthlyStatistic.getClanInvitesCount(), getStatisticsForClanGeneral(CategoryType.INVITED_COUNT.getClientId(), 0, clanId)));
				listStat.add(new StatisticContainer(CategoryType.LEAVED_COUNT, 0, clanMonthlyStatistic.getClanLeavedCount(), getStatisticsForClanGeneral(CategoryType.LEAVED_COUNT.getClientId(), 0, clanId)));
				listStat.add(new StatisticContainer(CategoryType.REPUTATION_COUNT, 0, clanMonthlyStatistic.getReputationCount(), getStatisticsForClanGeneral(CategoryType.REPUTATION_COUNT.getClientId(), 0, clanId)));
				listStat.add(new StatisticContainer(CategoryType.ADENA_COUNT_IN_WH, 0, clanMonthlyStatistic.getClanAdenaCount(), getStatisticsForClanGeneral(CategoryType.ADENA_COUNT_IN_WH.getClientId(), 0, clanId)));
				listStat.add(new StatisticContainer(CategoryType.ALL_CLAN_PVP_COUNT, 0, clanMonthlyStatistic.getClanPvpCount(), getStatisticsForClanGeneral(CategoryType.ALL_CLAN_PVP_COUNT.getClientId(), 0, clanId)));
				listStat.add(new StatisticContainer(CategoryType.CLAN_WAR_WIN_COUNT, 0, clanMonthlyStatistic.getClanWarWinCount(), getStatisticsForClanGeneral(CategoryType.CLAN_WAR_WIN_COUNT.getClientId(), 0, clanId)));
			}
		}
		return listStat;
	}

	/**
	 * Очищаем статы временной таблицы. Устанавливает всем полям значение 0.<br />
	 * <b>Запускать после обновления общей статистики и топа.</b>
	 */
	public void cleanTemporaryStats()
	{
		ThreadConnection conn = null;
		FiltredPreparedStatement statement = null;
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();
			statement = conn.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.cleanupStats());

			for(int i = statement.getParameterMetaData().getParameterCount(); i > 0; --i)
			{
				statement.setLong(i, 0);
			}

			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.error("WorldStatisticManage: Cannot clean up temporary table.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(conn, statement);
		}
	}

	/**
	 * Обновляет общую статистику сервера за все время для всех игроков.<br />
	 * <b>Нужно запускать перед обнулением месячной статистики.</b>
	 */
	public void calculateGeneralStatistic()
	{
		ThreadConnection conn = null;
		FiltredPreparedStatement statement = null;
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();
			statement = conn.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.updateGeneralStats());
			statement.executeUpdate();

			statement = conn.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.updateGeneralClanStats());
			statement.executeUpdate();

			statement = conn.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.nullStatistic());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.error("WorldStatisticManage: Cannot update global server statistic table.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(conn, statement);
		}
	}

	public void calculateAllResults(boolean withGeneralStatistic)
	{
		calculateGeneralStatisticTop(false);

		if(withGeneralStatistic)
		{
			calculateGeneralStatisticTop(true);
		}
	}

	/**
	 * Расчет общей/месячной статистики сервера.
	 * Сначала берутся все существующие в ядре категории, у которых не задано поле для БД.
	 * Затем для поля выбирается топ 100 игроков и эти данные записываются в world_statistic_result_(general|monthly).
	 *
	 * @param isGeneralTop Считать общую или месячную статистику?
	 */
	public void calculateGeneralStatisticTop(boolean isGeneralTop)
	{
		ThreadConnection conn = null;
		FiltredPreparedStatement statement = null;
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();
			if(isGeneralTop)
			{
				statement = conn.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.CLEAN_GENERAL_TOP);
				calculateGeneralStatistic();
			}
			else
			{
				statement = conn.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.CLEAN_MONTHLY_TOP);
			}
			statement.execute();

			if(!isGeneralTop)
			{
				statement = conn.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.CLEAN_STATISTIC_STATUES_DATA);
				statement.execute();
			}

			for(CategoryType cat : CategoryType.values())
			{
				if(cat.getFieldName().isEmpty())
				{
					continue;
				}

				int category = cat.getClientId();
				int subcat = cat.getSubcat();
				String field = cat.getFieldName();

				for(byte k = 0; k < 2; ++k)
				{
					List<Integer> charIds = new FastList(TOP_PLAYER_LIMIT);
					List<String> charNames = new FastList(TOP_PLAYER_LIMIT);
					List<Long> values = new FastList(TOP_PLAYER_LIMIT);
					if(k == 0)
					{
						statement = conn.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.selectPlayerTop(field, isGeneralTop));
					}
					else if(k == 1)
					{
						statement = conn.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.selectClanTop(field, isGeneralTop));
					}

					ResultSet result = statement.executeQuery();

					while(result.next())
					{
						charIds.add(result.getInt("charId"));
						charNames.add(result.getString("charName"));
						values.add(result.getLong(field));
					}

					if(!charIds.isEmpty())
					{
						statement = conn.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.insertTop(isGeneralTop, charIds.size()));

						int offset = 0;
						for(int i = 0, j = charIds.size(); i < j; ++i)
						{
							statement.setInt(++offset, category); // Cat ID
							statement.setInt(++offset, subcat); // Subcat
							statement.setInt(++offset, i + 1); // Place
							statement.setInt(++offset, charIds.get(i)); // Char ID
							statement.setString(++offset, charNames.get(i)); // Char name
							statement.setLong(++offset, values.get(i)); // Stat value
						}
						statement.execute();
						statement.clearParameters();
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.error("WorldStatisticManage: Cannot calculate general statistic top.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(conn, statement);
		}
	}

	private static class SingletonHolder
	{
		protected static final WorldStatisticsManager _instance = new WorldStatisticsManager();
	}
}