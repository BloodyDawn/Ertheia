package dwo.gameserver.datatables.sql.queries;

import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;

import java.util.List;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class WorldStatistic
{
	public static final String LOAD_СURRENT_PLAYER_STATS = "SELECT `ws`.* FROM `world_statistic` `ws`" + "JOIN `characters` `c` ON `c`.`charId` = `ws`.`charId`";
	public static final String LOAD_СURRENT_CLAN_STATS = "SELECT `ws`.* FROM `world_statistic` `ws`" + "JOIN `clan_data` `c` ON `c`.`clan_id` = `ws`.`charId`";
	public static final String CLEAN_GENERAL_TOP = "TRUNCATE TABLE `world_statistic_result_general`";
	public static final String CLEAN_MONTHLY_TOP = "TRUNCATE TABLE `world_statistic_result_monthly`";
	public static final String INSERT_STATS = "INSERT INTO `world_statistic`(`charId`) VALUES (?)";
	public static final String INSERT_GENERAL_STATS = "INSERT INTO `world_statistic_general`(`charId`) VALUES (?)";
	public static final String LOAD_GENERAL_PLAYER_TOP = "SELECT `wsrg`.* FROM `world_statistic_result_general` `wsrg` " +
		"JOIN `characters` `c` ON `c`.`charId` = `wsrg`.`charId` " +
		"ORDER BY `place`";
	public static final String LOAD_GENERAL_CLAN_TOP = "SELECT `wsrg`.* FROM `world_statistic_result_general` `wsrg` " +
		"JOIN `clan_data` `c` ON `c`.`clan_id` = `wsrg`.`charId` " +
		"ORDER BY `place`";
	public static final String LOAD_MONTHLY_PLAYER_TOP = "SELECT * FROM `world_statistic_result_monthly` `wsrm`" +
		"JOIN `characters` `c` ON `c`.`charId` = `wsrm`.`charId` " +
		" ORDER BY `place`";
	public static final String LOAD_MONTHLY_CLAN_TOP = "SELECT * FROM `world_statistic_result_monthly` `wsrm`" +
		"JOIN `clan_data` `c` ON `c`.`clan_id` = `wsrm`.`charId` " +
		" ORDER BY `place`";
	public static final String CLEAN_STATISTIC_STATUES_DATA = "TRUNCATE TABLE `world_statistic_statues`";
	public static final String INSERT_STATUE_DATA = "INSERT INTO `world_statistic_statues` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	public static final String GET_CHARACTER_ITEMS = "SELECT * FROM `items` WHERE `loc` = 'PAPERDOLL' AND `owner_id` = ?";
	public static final String LOAD_STATUES_DATA = "SELECT `wss`.*, `ch`.`char_name` FROM `world_statistic_statues` `wss` LEFT JOIN `characters` `ch` ON `ch`.`charId` = `wss`.`char_id`";

	/**
	 * Update: устанавливает всем полям в месячной статистике значение 0.
	 * @return
	 */
	public static String cleanupStats()
	{
		StringBuilder builder = new StringBuilder("UPDATE world_statistic SET ");

		for(String field : CategoryType.getAllFields())
		{
			builder.append(field).append("=?,");
		}

		return builder.toString().substring(0, builder.length() - 1);
	}

	/**
	 * Update: Обновляет месячную статистику для игрока.
	 * @return
	 */
	public static String updateStats(boolean isClanStatistic)
	{
		StringBuilder builder = new StringBuilder("UPDATE world_statistic SET ");

		List<String> fields = isClanStatistic ? CategoryType.getAllClanFields() : CategoryType.getAllFields();

		for(String field : fields)
		{
			builder.append(field).append("=?,");
		}

		return builder.substring(0, builder.length() - 1) + " WHERE charId=?";
	}

	/**
	 * Update: Обновляет таблицу общей статистики за все время. Данные суммируются с данными из месячной статистики.
	 * @return
	 */
	public static String updateGeneralStats()
	{
		StringBuilder builder = new StringBuilder("UPDATE `world_statistic_general` AS `wsg`, `world_statistic` AS `ws` SET");

		for(String field : CategoryType.getAllFields())
		{
			builder.append(" `wsg`.`").append(field).append("` = `wsg`.`").append(field).append("` + `ws`.`").append(field).append("`,");
		}

		return builder.substring(0, builder.length() - 1) + " WHERE `ws`.`charId` = `wsg`.`charId` AND `wsg`.`charId` IN (SELECT `charId` FROM `characters` `c` WHERE `c`.`charId` = `wsg`.`charId`)";
	}

	/**
	 * Update: Обновляет таблицу общей статистики за все время. Данные суммируются с данными из месячной статистики.
	 * @return
	 */
	public static String updateGeneralClanStats()
	{
		StringBuilder builder = new StringBuilder("UPDATE `world_statistic_general` AS `wsg`, `world_statistic` AS `ws` SET");

		for(String field : CategoryType.getAllFields())
		{
			builder.append(" `wsg`.`").append(field).append("` = `wsg`.`").append(field).append("` + `ws`.`").append(field).append("`,");
		}

		return builder.substring(0, builder.length() - 1) + " WHERE `ws`.`charId` = `wsg`.`charId` AND `wsg`.`charId` IN (SELECT `clan_id` FROM `clan_data` `c` WHERE `c`.`clan_id` = `wsg`.`charId`)";
	}

	public static String nullStatistic()
	{
		StringBuilder builder = new StringBuilder("UPDATE `world_statistic` AS `ws` SET");

		for(String field : CategoryType.getAllFields())
		{
			builder.append(" `ws`.`").append(field).append("` = 0,");
		}
		return builder.substring(0, builder.length() - 1);
	}

	/**
	 * Insert: Записи топа статистики общей/месячной статистики с ограничением на количество записей.
	 * @param isGeneralTop Общий топ?
	 * @param rowsCount Сколько записей нужно сгенерировать.
	 * @return
	 */
	public static String insertTop(boolean isGeneralTop, int rowsCount)
	{
		StringBuilder builder = isGeneralTop ? new StringBuilder("INSERT INTO `world_statistic_result_general` VALUES ") : new StringBuilder("INSERT INTO `world_statistic_result_monthly` VALUES ");

		for(int i = 0; i < rowsCount; ++i)
		{
			builder.append("(?, ?, ?, ?, ?, ?),");
		}

		return builder.substring(0, builder.length() - 1);
	}

	/**
	 * Select: топ игроков по определенному стату из общей или месячной статистики.
	 *
	 * @param stat Показатель игрока, по которому нужно сделать выборку.
	 * @param isGeneralTop Общий топ?
	 * @return
	 */
	public static String selectPlayerTop(String stat, boolean isGeneralTop)
	{
		String tableName = isGeneralTop ? "world_statistic_general" : "world_statistic";
		return "SELECT `characters`.`charId`, `characters`.`char_name` AS `charName`, `" + tableName + "`.`" + stat + "` " +
			"FROM `" + tableName + "` " +
			"JOIN `characters` ON `characters`.`charId` = `" + tableName + "`.`charId` " +
			"WHERE `" + tableName + "`.`" + stat + "` > 0 ORDER BY `" + tableName + "`.`" + stat + "` DESC LIMIT " + WorldStatisticsManager.TOP_PLAYER_LIMIT;
	}

	/**
	 * Select: топ кланов по определенному стату из общей или месячной статистики.
	 *
	 * @param stat Показатель игрока, по которому нужно сделать выборку.
	 * @param isGeneralTop Общий топ?
	 * @return
	 */
	public static String selectClanTop(String stat, boolean isGeneralTop)
	{
		String tableName = isGeneralTop ? "world_statistic_general" : "world_statistic";
		return "SELECT `clan_data`.`clan_id` as `charId`, `clan_data`.`clan_name` AS `charName`, `" + tableName + "`.`" + stat + "` " +
			"FROM `" + tableName + "` " +
			"JOIN `clan_data` ON `clan_data`.`clan_id` = `" + tableName + "`.`charId` " +
			"WHERE `" + tableName + "`.`" + stat + "` > 0 ORDER BY `" + tableName + "`.`" + stat + "` DESC LIMIT " + WorldStatisticsManager.TOP_PLAYER_LIMIT;
	}

	/**
	 * Выборка из топов.
	 *
	 * @param category Категория
	 * @param isGeneralTop Топ за все время или за месяц?
	 * @return
	 */
	public static String selectTopResults(CategoryType category, boolean isGeneralTop)
	{
		String tableName = isGeneralTop ? "world_statistic_result_general" : "world_statistic_result_monthly";

		return "SELECT * FROM `" + tableName + "` WHERE `categoryId` = " + category.getClientId() + " AND `subCategoryId` = " + category.getSubcat();
	}
}
