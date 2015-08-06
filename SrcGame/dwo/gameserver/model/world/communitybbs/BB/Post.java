/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.world.communitybbs.BB;

import dwo.gameserver.datatables.sql.queries.Community;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.world.communitybbs.Manager.PostBBSManager;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.List;

/**
 * @author Maktakien
 */
public class Post
{
	private static Logger _log = LogManager.getLogger(Post.class);
	private List<CPost> _post;

	public Post(String _PostOwner, int _PostOwnerID, long date, int tid, int _PostForumID, String txt)
	{
		_post = new FastList<>();
		CPost cp = new CPost();
		cp.postId = 0;
		cp.postOwner = _PostOwner;
		cp.postOwnerId = _PostOwnerID;
		cp.postDate = date;
		cp.postTopicId = tid;
		cp.postForumId = _PostForumID;
		cp.postTxt = txt;
		_post.add(cp);
		insertindb(cp);

	}

	public Post(Topic t)
	{
		_post = new FastList<>();
		load(t);
	}

	public void insertindb(CPost cp)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Community.INSERT_POSTS);
			statement.setInt(1, cp.postId);
			statement.setString(2, cp.postOwner);
			statement.setInt(3, cp.postOwnerId);
			statement.setLong(4, cp.postDate);
			statement.setInt(5, cp.postTopicId);
			statement.setInt(6, cp.postForumId);
			statement.setString(7, cp.postTxt);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "Error while saving new Post to db " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

	}

	public CPost getCPost(int id)
	{
		int i = 0;
		for(CPost cp : _post)
		{
			if(i++ == id)
			{
				return cp;
			}
		}
		return null;
	}

	public void deleteme(Topic t)
	{
		PostBBSManager.getInstance().delPostByTopic(t);
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM posts WHERE post_forum_id=? AND post_topic_id=?");
			statement.setInt(1, t.getForumID());
			statement.setInt(2, t.getID());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while deleting post: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void load(Topic t)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Community.SELECT_POSTS_BY_POST_TOPIC_ID);
			statement.setInt(1, t.getForumID());
			statement.setInt(2, t.getID());
			rset = statement.executeQuery();
			while(rset.next())
			{
				CPost cp = new CPost();
				cp.postId = rset.getInt("post_id");
				cp.postOwner = rset.getString("post_owner_name");
				cp.postOwnerId = rset.getInt("post_ownerid");
				cp.postDate = rset.getLong("post_date");
				cp.postTopicId = rset.getInt("post_topic_id");
				cp.postForumId = rset.getInt("post_forum_id");
				cp.postTxt = rset.getString("post_txt");
				_post.add(cp);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Data error on Post " + t.getForumID() + '/' + t.getID() + " : " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void updatetxt(int i)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			CPost cp = getCPost(i);
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE posts SET post_txt=? WHERE post_id=? AND post_topic_id=? AND post_forum_id=?");
			statement.setString(1, cp.postTxt);
			statement.setInt(2, cp.postId);
			statement.setInt(3, cp.postTopicId);
			statement.setInt(4, cp.postForumId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while saving new Post to db " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public static class CPost
	{
		public int postId;
		public String postOwner;
		public int postOwnerId;
		public long postDate;
		public int postTopicId;
		public int postForumId;
		public String postTxt;
	}
}
