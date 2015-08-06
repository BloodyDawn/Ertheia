package dwo.gameserver.datatables.sql.queries;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.04.13
 * Time: 13:14
 */

public class Items
{
	public static final String UPDATE = "UPDATE items SET `owner_id` = ?,`count` = ?,`loc` = ?,`loc_data` = ?,`enchant_level` = ?,`custom_type1` = ?,`custom_type2` = ?,`mana_left` = ?,`time` = ?,`skin_id` = ? WHERE `object_id` = ?";
	public static final String INSERT = "INSERT INTO items (`owner_id`,`item_id`,`count`,`loc`,`loc_data`,`enchant_level`,`object_id`,`custom_type1`,`custom_type2`,`mana_left`,`time`,`skin_id`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String DELETE = "DELETE FROM `items` WHERE `object_id`=?";

	public static final String ITEM_ATTRIBUTES_REMOVE = "DELETE FROM `item_attributes` WHERE `itemId` = ?";
	public static final String ITEM_ATTRIBUTES_LOAD = "SELECT `augAttributes`,`augSkillId`,`augSkillLevel` FROM `item_attributes` WHERE `itemId`=?";
	public static final String ITEM_ATTRIBUTES_UPDATE = "REPLACE INTO `item_attributes` VALUES(?,?,?,?)";

	public static final String ITEM_ELEMENTALS_LOAD = "SELECT `elemType`,`elemValue` FROM `item_elementals` WHERE `itemId`=?";
	public static final String ITEM_ELEMENTALS_REMOVE = "DELETE FROM `item_elementals` WHERE `itemId` = ?";
	public static final String ITEM_ELEMENTALS_REMOVE_BY_TYPE = "DELETE FROM `item_elementals` WHERE `itemId` = ? AND `elemType` = ?";
	public static final String ITEM_ELEMENTALS_ADD = "INSERT INTO `item_elementals` VALUES(?,?,?)";
}