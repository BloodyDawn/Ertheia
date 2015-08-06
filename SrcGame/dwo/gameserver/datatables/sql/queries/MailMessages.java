package dwo.gameserver.datatables.sql.queries;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 25.04.13
 * Time: 21:07
 */

public class MailMessages
{
	public static final String INSERT = "INSERT INTO `messages` (messageId, senderId, receiverId, subject, content, expiration, reqAdena, hasAttachments, isUnread, isDeletedBySender, isDeletedByReceiver, isFourStars, isNews, type, itemId, sellAdena, enchantLvl, elementals) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	public static final String SELECT = "SELECT * FROM `messages` ORDER BY `expiration`";
	public static final String ADD = "DELETE FROM `messages` WHERE `messageId` = ?";

	public static final String DELETE_ATTACHMENTS = "UPDATE `messages` SET `hasAttachments` = 'false' WHERE `messageId` = ?";

	public static final String MARK_AS_READED = "UPDATE `messages` SET `isUnread` = 'false' WHERE `messageId` = ?";
	public static final String MARK_AS_DELETED_BY_SENDER = "UPDATE `messages` SET `isDeletedBySender` = 'true' WHERE `messageId` = ?";
	public static final String MARK_AS_DELETED_BY_RECIEVER = "UPDATE `messages` SET `isDeletedByReceiver` = 'true' WHERE `messageId` = ?";
}