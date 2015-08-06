package dwo.gameserver.datatables.sql.queries.clan;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 30.05.13
 * Time: 6:29
 */

public class ClanData
{
	public static final String LOAD = "SELECT * FROM `clan_data` WHERE `clan_id`=?";
	public static final String UPDATE = "UPDATE clan_data SET leader_id=?,ally_id=?,ally_name=?,reputation_score=?,ally_penalty_expiry_time=?,ally_penalty_type=?,char_penalty_expiry_time=?,dissolving_expiry_time=? WHERE clan_id=?";
	public static final String STORE = "INSERT INTO `clan_data` (`clan_id`,`clan_name`,`clan_level`,`hasCastle`,`ally_id`,`ally_name`,`leader_id`,`crest_id`,`crest_large_id`,`ally_crest_id`) values (?,?,?,?,?,?,?,?,?,?)";
	public static final String DELETE = "DELETE FROM `clan_data` WHERE `clan_id`=?";

	public static final String CREST_UPDATE = "UPDATE `clan_data` SET `crest_id` = ? WHERE `clan_id` = ?";
	public static final String CREST_ALLY_UPDATE = "UPDATE `clan_data` SET `ally_crest_id` = ? WHERE `ally_id` = ?";
	public static final String CREST_LARGE_UPDATE = "UPDATE `clan_data` SET `crest_large_id` = ? WHERE `clan_id` = ?";

	public static final String SELECT_ONLINE_MEMBERS = "SELECT charId FROM characters WHERE clanid=? AND online=0";
}
