package dwo.gameserver.model.world.communitybbs.BB;

import dwo.gameserver.datatables.sql.queries.Community;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.world.communitybbs.Manager.TopicBBSManager;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Topic
{
	public static final int MORMAL = 0;
	public static final int MEMO = 1;
	private static Logger _log = LogManager.getLogger(Topic.class);
	private int _id;
	private int _forumId;
	private String _topicName;
	private long _date;
	private String _ownerName;
	private int _ownerId;
	private int _type;
	private int _cReply;

	public Topic(ConstructorType ct, int id, int fid, String name, long date, String oname, int oid, int type, int Creply)
	{
		_id = id;
		_forumId = fid;
		_topicName = name;
		_date = date;
		_ownerName = oname;
		_ownerId = oid;
		_type = type;
		_cReply = Creply;
		TopicBBSManager.getInstance().addTopic(this);

		if(ct == ConstructorType.CREATE)
		{

			insertindb();
		}
	}

	public void insertindb()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Community.INSERT_TOPIC);
			statement.setInt(1, _id);
			statement.setInt(2, _forumId);
			statement.setString(3, _topicName);
			statement.setLong(4, _date);
			statement.setString(5, _ownerName);
			statement.setInt(6, _ownerId);
			statement.setInt(7, _type);
			statement.setInt(8, _cReply);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while saving new Topic to db " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

	}

	public int getID()
	{
		return _id;
	}

	public int getForumID()
	{
		return _forumId;
	}

	public String getName()
	{
		return _topicName;
	}

	public String getOwnerName()
	{
		return _ownerName;
	}

	public void deleteme(Forum f)
	{
		TopicBBSManager.getInstance().delTopic(this);
		f.rmTopicByID(_id);
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM topic WHERE topic_id=? AND topic_forum_id=?");
			statement.setInt(1, _id);
			statement.setInt(2, f.getID());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while deleting topic: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public long getDate()
	{
		return _date;
	}

	public enum ConstructorType
	{
		RESTORE,
		CREATE
	}
}
