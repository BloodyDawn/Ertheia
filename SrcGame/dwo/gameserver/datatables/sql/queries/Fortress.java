package dwo.gameserver.datatables.sql.queries;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 23.02.13
 * Time: 1:45
 */

public class Fortress
{
	public static final String SELECT = "SELECT * FROM `fort` WHERE `id` = ?";
	public static final String UPDATE = "UPDATE `fort` SET `siegeDate`=?, `lastOwnedTime`=?, `owner`=?, `state`=?, `castleId`=?, `supplyLvL`=? WHERE `id`=?";

	// Функции форта
	public static final String FUNCTION_SELECT = "SELECT `deco_id`, `endTime` FROM `fort_functions` WHERE `fort_id` = ?";
	public static final String FUNCTION_DELETE = "DELETE FROM `fort_functions` WHERE `fort_id` = ? AND `deco_id` = ?";
	public static final String FUNCTION_INSERT = "INSERT INTO `fort_functions` (`fort_id`, `deco_id`, `endTime`) VALUES (?, ?, ?)";
	public static final String FUNCTION_UPDATE = "UPDATE `fort_functions` SET `endTime`= ? WHERE `fort_id`= ? AND `deco_id`= ?";

	// Апгрейд форта
	public static final String FACILITY_UPDATE = "UPDATE `fort_facility` SET `facility_level`=? WHERE `fortId` = ? AND `facility_type`=?";
	public static final String FACILITY_INSERT = "INSERT INTO `fort_facility` (`fort_id`, `facility_type`, `facility_level`) VALUES (?, ?, ?)";
	public static final String FACILITY_DELETE = "DELETE FROM `fort_facility` WHERE `fort_id`=?";
	public static final String FACILITY_SELECT = "SELECT `facility_type`, `facility_level` FROM `fort_facility` WHERE `fort_id`=?";
}