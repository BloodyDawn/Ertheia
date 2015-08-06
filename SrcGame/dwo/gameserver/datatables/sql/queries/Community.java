package dwo.gameserver.datatables.sql.queries;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 02.04.12
 * Time: 16:26
 */

public class Community
{
	//SELECT
	public static final String SELECT_FORUMS_BY_FORUM_ID = "SELECT * FROM forums WHERE forum_id=?";
	public static final String SELECT_TOPIC_BY_FORUM_ID = "SELECT * FROM topic WHERE topic_forum_id=? ORDER BY topic_id DESC";
	public static final String SELECT_FORUMS_BY_FORUM_PARENT = "SELECT forum_id FROM forums WHERE forum_parent=?";
	public static final String SELECT_POSTS_BY_POST_TOPIC_ID = "SELECT * FROM posts WHERE post_forum_id=? AND post_topic_id=? ORDER BY post_id ASC";
	public static final String SELECT_FORUM_BY_FORUM_TYPE = "SELECT forum_id FROM forums WHERE forum_type=0";

	//INSERT
	public static final String INSERT_FORUMS = "INSERT INTO forums (forum_id,forum_name,forum_parent,forum_post,forum_type,forum_perm,forum_owner_id) VALUES (?,?,?,?,?,?,?)";
	public static final String INSERT_POSTS = "INSERT INTO posts (post_id,post_owner_name,post_ownerid,post_date,post_topic_id,post_forum_id,post_txt) values (?,?,?,?,?,?,?)";
	public static final String INSERT_TOPIC = "INSERT INTO topic (topic_id,topic_forum_id,topic_name,topic_date,topic_ownername,topic_ownerid,topic_type,topic_reply) values (?,?,?,?,?,?,?,?)";
}
