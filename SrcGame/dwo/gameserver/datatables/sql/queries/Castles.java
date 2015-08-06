package dwo.gameserver.datatables.sql.queries;

public class Castles
{
	public static final String LOAD = "SELECT * FROM castle WHERE id = ?";

	public static final String LOAD_OWNER_CLANID = "SELECT `clan_id` FROM `clan_data` WHERE `hasCastle` = ?";
	public static final String DELETE_OWNER_CLANID = "UPDATE `clan_data` SET `hasCastle`= 0 WHERE `hasCastle`=?";
	public static final String UPDATE_OWNER_CLANID = "UPDATE `clan_data` SET `hasCastle`=? WHERE `clan_id`=?";

	public static final String INSERT_SIEGE_CLAN = "INSERT INTO `siege_clans` (`clan_id`,`castle_id`,`type`,`castle_owner`) VALUES (?,?,?,0)";
	public static final String UPDATE_SIEGE_CLAN = "UPDATE `siege_clans` SET `type` = ? WHERE `castle_id` = ? AND `clan_id` = ?";
	public static final String LOAD_SIEGE_CLANS = "SELECT `clan_id`,`type` FROM `siege_clans` WHERE `castle_id`=?";
	public static final String LOAD_SIEGE_CLAN = "SELECT `clan_id` FROM `siege_clans` WHERE `clan_id`=? AND `castle_id`=?";
	public static final String DELETE_SIEGE_CLAN = "DELETE FROM `siege_clans` WHERE `clan_id`=?";
	public static final String DELETE_SIEGE_CLAN_FROM_CASTLE = "DELETE FROM `siege_clans` WHERE `castle_id`=? AND `clan_id`=?";
	public static final String DELETE_SIEGE_CLANS = "DELETE FROM `siege_clans` WHERE `castle_id`=?";
	public static final String DELETE_SIEGE_CLANS_WAITING = "DELETE FROM `siege_clans` WHERE `castle_id`=? and `type`= 2";

	public static final String MANOR_DELETE_PRODUCTION = "DELETE FROM `castle_manor_production` WHERE `castle_id` = ?";
	public static final String MANOR_DELETE_PRODUCTION_PERIOD = "DELETE FROM `castle_manor_production` WHERE `castle_id` = ? AND `period` = ?";
	public static final String MANOR_DELETE_PROCURE = "DELETE FROM `castle_manor_procure` WHERE `castle_id` = ?";
	public static final String MANOR_DELETE_PROCURE_PERIOD = "DELETE FROM `castle_manor_procure` WHERE `castle_id` = ? AND `period` = ?";
	public static final String UPDATE_CROP = "UPDATE `castle_manor_procure` SET `can_buy` = ? WHERE `crop_id` = ? AND `castle_id` = ? AND `period` = ?";
	public static final String UPDATE_SEED = "UPDATE `castle_manor_production` SET `can_produce` = ? WHERE `seed_id` = ? AND `castle_id` = ? AND `period` = ?";
	public static final String UPDATE_SIDE = "UPDATE `castle` SET `side` = ? WHERE `id` = ?";
	public static final String UPDATE_TREASURY = "UPDATE `castle` SET `treasury` = ? WHERE `id` = ?";
	public static final String UPDATE_NPC_STRING = "UPDATE `castle` SET `showNpcCrest` = ? WHERE `id` = ?";
	public static final String UPDATE_SIEGE_DATES = "UPDATE `castle` SET `siegeDate` = ?, `regTimeEnd` = ?, `regTimeOver` = ?  WHERE `id` = ?";

	// Функции замка
	public static final String FUNCTION_INSERT = "INSERT INTO `castle_functions` (`castle_id`, `deco_id`, `endTime`) VALUES (?, ?, ?)";
	public static final String FUNCTION_UPDATE = "UPDATE `castle_functions` SET `endTime` = ? WHERE `castle_id` = ? AND `deco_id` = ?";
	public static final String FUNCTION_SELECT = "SELECT `deco_id`, `endTime` FROM `castle_functions` WHERE `castle_id` = ?";
	public static final String FUNCTION_DELETE = "DELETE FROM `castle_functions` WHERE `castle_id` = ? AND `deco_id` = ?";

	// Апгрейд дверей
	public static final String DOORUPGRADE_SELECT = "SELECT `id`, `lv` FROM `castle_doorupgrade` WHERE `castleId` = ?";
	public static final String DOORUPGRADE_DELETE = "DELETE FROM `castle_doorupgrade` WHERE `castleId`=?";
	public static final String DOORUPGRADE_INSERTUPDATE = "INSERT INTO `castle_doorupgrade` (`castleId`, `id`, `lv`) values (?,?,?) ON DUPLICATE UPDATE `lv` = ?";

	// Апгрейд ловушек
	public static final String TRAPUPGRADE_SELECT = "SELECT * FROM `castle_trapupgrade` WHERE `castleId`=?";
	public static final String TRAPUPGRADE_REPLACE = "REPLACE INTO `castle_trapupgrade` (`castleId`, `towerIndex`, `level`) values (?,?,?)";
	public static final String TRAPUPGRADE_DELETE = "DELETE FROM `castle_trapupgrade` WHERE `castleId`=?";
}