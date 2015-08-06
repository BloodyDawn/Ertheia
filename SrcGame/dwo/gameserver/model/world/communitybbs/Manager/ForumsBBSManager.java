package dwo.gameserver.model.world.communitybbs.Manager;

import dwo.gameserver.datatables.sql.queries.Community;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.communitybbs.BB.Forum;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.sql.ResultSet;
import java.util.List;

public class ForumsBBSManager extends BaseBBSManager
{
	private List<Forum> _table;
	private int _lastid = 1;

	private ForumsBBSManager()
	{
		_table = new FastList<>();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Community.SELECT_FORUM_BY_FORUM_TYPE);
			rset = statement.executeQuery();
			while(rset.next())
			{
				int forumId = rset.getInt("forum_id");
				Forum f = new Forum(forumId, null);
				addForum(f);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Data error on Forum (root): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public static ForumsBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void initRoot()
	{
		for(Forum f : _table)
		{
			f.vload();
		}
		_log.log(Level.INFO, "Loaded " + _table.size() + " forums. Last forum id used: " + _lastid);
	}

	public void addForum(Forum ff)
	{
		if(ff == null)
		{
			return;
		}

		_table.add(ff);

		if(ff.getID() > _lastid)
		{
			_lastid = ff.getID();
		}
	}

	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}

	public Forum getForumByName(String Name)
	{
		for(Forum f : _table)
		{
			if(f.getName().equals(Name))
			{
				return f;
			}
		}

		return null;
	}

	public Forum createNewForum(String name, Forum parent, int type, int perm, int oid)
	{
		Forum forum = new Forum(name, parent, type, perm, oid);
		forum.insertIntoDb();
		return forum;
	}

	public int getANewID()
	{
		return ++_lastid;
	}

	public Forum getForumByID(int idf)
	{
		for(Forum f : _table)
		{
			if(f.getID() == idf)
			{
				return f;
			}
		}
		return null;
	}

	private static class SingletonHolder
	{
		protected static final ForumsBBSManager _instance = new ForumsBBSManager();
	}
}