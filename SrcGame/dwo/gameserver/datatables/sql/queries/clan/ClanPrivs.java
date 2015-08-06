package dwo.gameserver.datatables.sql.queries.clan;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 30.05.13
 */

public class ClanPrivs
{
	public static final String LOAD = "SELECT `privs`,`rank`,`party` FROM `clan_privs` WHERE `clan_id`=?";
	public static final String DELETE = "DELETE FROM `clan_privs` WHERE `clan_id`=?";
	public static final String INSERT = "INSERT INTO `clan_privs` (`clan_id`,`rank`,`party`,`privs`) VALUES (?,?,?,?)";
	public static final String INSERT_DUPLICATE = "INSERT INTO `clan_privs` (`clan_id`,`rank`,`party`,`privs`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE `privs` = ?";
}
