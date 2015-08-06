package dwo.gameserver.datatables.sql.queries.clan;

/**
 * User: Bacek
 * Date: 19.01.13
 * Time: 23:12
 */
public class ClanSkills
{
	public static final String INSERT_CLAN_SKILLS = "INSERT INTO clan_skills (clan_id,skill_id,skill_level,skill_name,sub_pledge_id) VALUES (?,?,?,?,?)";

	public static final String SELECT_CLAN_SKILLS = "SELECT skill_id,skill_level,sub_pledge_id FROM clan_skills WHERE clan_id=?";

	public static final String UPDATE_CLAN_SKILLS = "UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?";

	public static final String DELETE_CLAN_SKILLS = "DELETE FROM clan_skills WHERE clan_skills.clan_id NOT IN (SELECT clan_id FROM clan_data);";
	public static final String DELETE_CLAN_SKILLS_1 = "DELETE FROM clan_skills WHERE clan_id=?";
}
