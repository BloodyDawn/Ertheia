package dwo.gameserver.datatables.sql.queries;

/**
 * L2GOD Team
 * User: Yorie
 * Date: 17.04.12
 * Time: xx:xx
 */

public class Servitors
{
	public static final String CLEAN_CHARACTER_SUMMON_EFFECTS = "DELETE FROM `character_summon_skills_save` WHERE `owner_id` = ?";
}
