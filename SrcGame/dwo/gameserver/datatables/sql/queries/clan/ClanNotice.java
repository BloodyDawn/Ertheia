package dwo.gameserver.datatables.sql.queries.clan;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.05.13
 * Time: 9:53
 */

public class ClanNotice
{
	public static final String DELETE_CLAN_NOTICE = "DELETE FROM `clan_notices` WHERE `clan_id`=?";
	public static final String LOAD_CLAN_NOTICE = "SELECT `enabled`,`notice` FROM `clan_notices` WHERE `clan_id`=?";
	public static final String SAVE_CLAN_NOTICE = "INSERT INTO `clan_notices` (`clan_id`,`notice`,`enabled`) values (?,?,?) ON DUPLICATE KEY UPDATE `notice`=?,`enabled`=?";
}
