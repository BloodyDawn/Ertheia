package dwo.gameserver.model.world.communitybbs.BB;

import dwo.gameserver.datatables.sql.queries.Community;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.world.communitybbs.Manager.ForumsBBSManager;
import dwo.gameserver.model.world.communitybbs.Manager.TopicBBSManager;
import dwo.gameserver.util.database.DatabaseUtils;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.List;

public class Forum
{
	//type
	public static final int ROOT = 0;
	public static final int NORMAL = 1;
	public static final int CLAN = 2;
	public static final int MEMO = 3;
	public static final int MAIL = 4;
	//perm
	public static final int INVISIBLE = 0;
	public static final int ALL = 1;
	public static final int CLANMEMBERONLY = 2;
	public static final int OWNERONLY = 3;

	private static Logger _log = LogManager.getLogger(Forum.class);
	private List<Forum> _children;
	private TIntObjectHashMap<Topic> _topic;
	private int _forumId;
	private String _forumName;
	//private int _ForumParent;
	private int _forumType;
	private int _forumPost;
	private int _forumPerm;
	private Forum _fParent;
	private int _ownerID;
	private boolean _loaded;

	/**
	 * Creates new instance of Forum. When you create new forum, use
	 * {@link ForumsBBSManager#
	 * addForum(dwo.gameserver.communitybbs.BB.Forum)} to add forum
	 * to the forums manager.
	 *
	 * @param Forumid ид форума
	 * @param FParent родительский форум
	 */
	public Forum(int Forumid, Forum FParent)
	{
		_forumId = Forumid;
		_fParent = FParent;
		_children = new FastList<>();
		_topic = new TIntObjectHashMap<>();
	}

	/**
	 * @param name
	 * @param parent
	 * @param type
	 * @param perm
	 */
	public Forum(String name, Forum parent, int type, int perm, int OwnerID)
	{
		_forumName = name;
		_forumId = ForumsBBSManager.getInstance().getANewID();
		//_ForumParent = parent.getID();
		_forumType = type;
		_forumPost = 0;
		_forumPerm = perm;
		_fParent = parent;
		_ownerID = OwnerID;
		_children = new FastList<>();
		_topic = new TIntObjectHashMap<>();
		parent._children.add(this);
		ForumsBBSManager.getInstance().addForum(this);
		_loaded = true;
	}

	/**
	 *
	 */
	private void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Community.SELECT_FORUMS_BY_FORUM_ID);
			statement.setInt(1, _forumId);
			rset = statement.executeQuery();

			if(rset.next())
			{
				_forumName = rset.getString("forum_name");
				//_ForumParent = rset.getInt("forum_parent");
				_forumPost = rset.getInt("forum_post");
				_forumType = rset.getInt("forum_type");
				_forumPerm = rset.getInt("forum_perm");
				_ownerID = rset.getInt("forum_owner_id");
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Data error on Forum " + _forumId + " : " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		ThreadConnection con2 = null;
		FiltredPreparedStatement statement2 = null;
		ResultSet rset2 = null;
		try
		{
			con2 = L2DatabaseFactory.getInstance().getConnection();
			statement2 = con2.prepareStatement(Community.SELECT_TOPIC_BY_FORUM_ID);
			statement2.setInt(1, _forumId);
			rset2 = statement2.executeQuery();

			while(rset2.next())
			{
				Topic t = new Topic(Topic.ConstructorType.RESTORE, rset2.getInt("topic_id"), rset2.getInt("topic_forum_id"), rset2.getString("topic_name"), rset2.getLong("topic_date"), rset2.getString("topic_ownername"), rset2.getInt("topic_ownerid"), rset2.getInt("topic_type"), rset2.getInt("topic_reply"));
				_topic.put(t.getID(), t);
				if(t.getID() > TopicBBSManager.getInstance().getMaxID(this))
				{
					TopicBBSManager.getInstance().setMaxID(t.getID(), this);
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Data error on Forum " + _forumId + " : " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con2, statement2, rset2);
		}
	}

	private void getChildren()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Community.SELECT_FORUMS_BY_FORUM_PARENT);
			statement.setInt(1, _forumId);
			rset = statement.executeQuery();

			while(rset.next())
			{
				Forum f = new Forum(rset.getInt("forum_id"), this);
				_children.add(f);
				ForumsBBSManager.getInstance().addForum(f);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Data error on Forum (children): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public int getTopicSize()
	{
		vload();
		return _topic.size();
	}

	public Topic getTopic(int j)
	{
		vload();
		return _topic.get(j);
	}

	public void addTopic(Topic t)
	{
		vload();
		_topic.put(t.getID(), t);
	}

	public int getID()
	{
		return _forumId;
	}

	public String getName()
	{
		vload();
		return _forumName;
	}

	public int getType()
	{
		vload();
		return _forumType;
	}

	public Forum getChildByName(String name)
	{
		vload();
		for(Forum f : _children)
		{
			if(f.getName().equals(name))
			{
				return f;
			}
		}
		return null;
	}

	public void rmTopicByID(int id)
	{
		_topic.remove(id);
	}

	public void insertIntoDb()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Community.INSERT_FORUMS);
			statement.setInt(1, _forumId);
			statement.setString(2, _forumName);
			statement.setInt(3, _fParent._forumId);
			statement.setInt(4, _forumPost);
			statement.setInt(5, _forumType);
			statement.setInt(6, _forumPerm);
			statement.setInt(7, _ownerID);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while saving new Forum to db " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void vload()
	{
		if(!_loaded)
		{
			load();
			getChildren();
			_loaded = true;
		}
	}
}