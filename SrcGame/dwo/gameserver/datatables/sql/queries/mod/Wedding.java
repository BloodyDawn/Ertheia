package dwo.gameserver.datatables.sql.queries.mod;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.04.13
 * Time: 13:42
 */
public class Wedding
{
	public static final String LOAD_INDEXES = "SELECT `id` FROM `mods_wedding` ORDER BY `id`";
	public static final String LOAD = "SELECT * FROM `mods_wedding` WHERE `id` = ?";
	public static final String INSERT = "INSERT INTO `mods_wedding` (`id`, `player1Id`, `player2Id`, `married`, `affianceDate`, `weddingDate`) VALUES (?, ?, ?, ?, ?, ?)";
	public static final String UPDATE = "UPDATE `mods_wedding` SET `married` = ?, `weddingDate` = ? WHERE `id` = ?";
	public static final String DELETE = "DELETE FROM `mods_wedding` WHERE `id`=?";
}
