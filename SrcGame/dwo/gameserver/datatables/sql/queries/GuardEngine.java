package dwo.gameserver.datatables.sql.queries;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 01.07.13
 * Time: 17:56
 */
public class GuardEngine
{
	public static final String INSERT_BAN = "INSERT INTO `hwid_ban` (`hwid`, `account`, `ip`, `hackType`, `comment`, `banStart`, `banEnd`) values (?,?,?,?,?,?,?)";
	public static final String DELETE_BAN = "DELETE FROM `hwid_ban` WHERE `hwid`=?";
	public static final String LOAD_BAN = "SELECT * FROM `hwid_ban`";
}