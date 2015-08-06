package dwo.gameserver.datatables.sql.queries.clan;

/**
 * User: GenCloud
 * Date: 17.03.2015
 * Team: La2Era Team
 */
public class СlanSubpledges
{
    public static final String INSERT_SUBPLEDGES = "INSERT INTO clan_subpledges (clan_id,sub_pledge_id,name,leader_id) values (?,?,?,?)";

    public static final String SELECT_SUBPLEDGES = "SELECT sub_pledge_id,name,leader_id FROM clan_subpledges WHERE clan_id=?";

    public static final String UPDATE_SUBPLEDGES = "UPDATE clan_subpledges SET leader_id=0 WHERE clan_subpledges.leader_id NOT IN (SELECT charId FROM characters) AND leader_id > 0;";
    public static final String UPDATE_SUBPLEDGES_1 = "UPDATE clan_subpledges SET leader_id=?, name=? WHERE clan_id=? AND sub_pledge_id=?";

    public static final String DELETE_SUBPLEDGES = "DELETE FROM clan_subpledges WHERE clan_id=?";
    public static final String DELETE_SUBPLEDGES_1 = "DELETE FROM clan_subpledges WHERE clan_subpledges.clan_id NOT IN (SELECT clan_id FROM clan_data);";
}
