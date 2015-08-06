package dwo.gameserver.datatables.sql.queries.clan;

/**
 * User: Bacek
 * Date: 19.01.13
 * Time: 22:50
 */
public class ClanWars
{
	public static final String SELECT_CLAN_WARS = "SELECT attacker_clan, opposing_clan, period, period_start_time, last_kill_time, attackers_kill_counter, opposers_kill_counter FROM clan_wars";

	public static final String REPLACE_CLAN_WARS = "REPLACE INTO clan_wars (attacker_clan, opposing_clan, period, period_start_time, last_kill_time, attackers_kill_counter, opposers_kill_counter) VALUES(?,?,?,?,?,?,?)";

	public static final String DELETE_CLAN_WARS = "DELETE FROM clan_wars WHERE attacker_clan=? AND opposing_clan=?";
	public static final String DELETE_CLAN_WARS_1 = "DELETE FROM clan_wars WHERE attacker_clan=? OR opposing_clan=?";
	public static final String DELETE_CLAN_WARS_2 = "DELETE FROM clan_wars WHERE clan_wars.attacker_clan NOT IN (SELECT clan_id FROM clan_data);";
	public static final String DELETE_CLAN_WARS_3 = "DELETE FROM clan_wars WHERE clan_wars.opposing_clan NOT IN (SELECT clan_id FROM clan_data);";
}
