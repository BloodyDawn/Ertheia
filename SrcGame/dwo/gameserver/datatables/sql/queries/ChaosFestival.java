package dwo.gameserver.datatables.sql.queries;

/**
 * Chaos Festival database queries.
 *
 * @author Yorie
 */
public class ChaosFestival
{
	public static final String LOAD_CHAOS_FESTIVAL_ENTRY = "SELECT * FROM `character_chaos_festival` WHERE `player_id` = ? LIMIT 1";
	public static final String UPDATE_INFO = "UPDATE `character_chaos_festival` SET `myst_signs` = ?, `skip_rounds` = ?, `total_bans` = 0 WHERE `player_id` = ? LIMIT 1";
	public static final String CLEAR_OLD_LOGS = "DELETE FROM `character_chaos_festival` WHERE `player_id` NOT IN (SELECT `charId` FROM `characters`)";
}