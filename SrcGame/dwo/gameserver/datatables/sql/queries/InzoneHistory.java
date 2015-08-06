package dwo.gameserver.datatables.sql.queries;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.xx
 * Time: xx:xx
 */

public class InzoneHistory
{
	public static final String GET_PARTY_INZONE_HISTORY = "SELECT * FROM character_inzone_history ORDER BY char_id";

	public static final String ADD_PARTY_INZONE_HISTORY = "INSERT INTO party_inzone_history (party_id, char_id, char_class_id, instance_id, instance_use_time, instance_status ) VALUES (?, ?, ?, ?, ?, ?)";

	public static final String GET_CHARACTER_INZONE_HISTORY = "SELECT * FROM party_inzone_history ORDER BY party_id";

	public static final String ADD_CHARACTER_INZONE_HISTORY = "INSERT INTO character_inzone_history (char_id, party_id) VALUES (?, ?)";

	public static final String GET_CHARACTER_HISTORY = "SELECT `pih`.* FROM `character_inzone_history` `cih`" +
		"LEFT JOIN `party_inzone_history` `pih` ON `pih`.`partyId` = `cih`.`partyId`" +
		"WHERE `cih`.`charId` = ? AND `pih`.`charId` != ?";
}
