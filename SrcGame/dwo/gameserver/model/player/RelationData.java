package dwo.gameserver.model.player;

import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelationData
{
	public static final int BLOCK = 0x0001;
	public static final int FRIEND = 0x0002;
	public static final int POSTFRIEND = 0x0004;
	private static Logger _log = LogManager.getLogger(RelationData.class.getName());
	private static HashMap<Integer, RelationObjectInfo> _relationInfo = new HashMap();

	private final int _ownerId;
	private List<Integer> _blockList;
	private List<Integer> _friendList;
	private Map<Integer, String> _friendNotes;
	private List<Integer> _postFriendList;

	public RelationData(int ownerId)
	{
		_ownerId = ownerId;
		_blockList = new FastList();
		_friendList = new FastList();
		_friendNotes = new HashMap();
		_postFriendList = new FastList();
		loadLists();
	}

	private void loadLists()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_CHARACTER_RELATIONS_BY_CHARID_RELATION);
			statement.setInt(1, _ownerId);
			rset = statement.executeQuery();

			int friendId;
			int relation;
			String note;
			while(rset.next())
			{
				friendId = rset.getInt("friendId");
				relation = rset.getInt("relation");
				note = rset.getString("note");
				if(friendId == _ownerId)
				{
					continue;
				}
				if((relation & BLOCK) != 0)
				{
					_blockList.add(friendId);
				}
				if((relation & FRIEND) != 0)
				{
					_friendList.add(friendId);
				}
				if((relation & POSTFRIEND) != 0)
				{
					_postFriendList.add(friendId);
				}
				_friendNotes.put(friendId, note);
			}

			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error found in " + _ownerId + " relationList while loading: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	private void updateInDB(int targetId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		int relation = 0;
		if(_blockList.contains(targetId))
		{
			relation |= BLOCK;
		}
		if(_friendList.contains(targetId))
		{
			relation |= FRIEND;
		}
		if(_postFriendList.contains(targetId))
		{
			relation |= POSTFRIEND;
		}

		String note = "";
		if(_friendNotes.containsKey(targetId))
		{
			note = _friendNotes.get(targetId);
		}

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			if(relation == 0)
			{
				statement = con.prepareStatement(Characters.DELETE_CHARACTER_RELATIONS_BY_CHARID);
				statement.setInt(1, _ownerId);
				statement.setInt(2, targetId);
			}
			else
			{
				statement = con.prepareStatement(Characters.INSERT_CHARACTER_RELATIONS_RELATION);
				statement.setInt(1, _ownerId);
				statement.setInt(2, targetId);
				statement.setInt(3, relation);
				statement.setString(4, note);
				statement.setInt(5, relation);
				statement.setString(6, note);
			}
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not update player relation lists: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	// Block List Functions
	public void addToBlockList(int target)
	{
		_blockList.add(target);
		updateInDB(target);
	}

	public void removeFromBlockList(int target)
	{
		_blockList.remove(Integer.valueOf(target));
		updateInDB(target);
	}

	public boolean isInBlockList(int target)
	{
		return _blockList.contains(target);
	}

	public List<Integer> getBlockList()
	{
		return _blockList;
	}

	// Friend List Functions
	public void addToFriendList(int target)
	{
		_friendList.add(target);
		updateInDB(target);
	}

	public void removeFromFriendList(int target)
	{
		_friendList.remove(Integer.valueOf(target));
		updateInDB(target);
	}

	public boolean isFriend(int target)
	{
		return _friendList.contains(target);
	}

	public List<Integer> getFriendList()
	{
		return _friendList;
	}

	// Friend List Functions
	public void addToPostFriendList(int target)
	{
		_postFriendList.add(target);
		updateInDB(target);
	}

	public void removeFromPostFriendList(int target)
	{
		_postFriendList.remove(Integer.valueOf(target));
		updateInDB(target);
	}

	public boolean isPostFriend(int target)
	{
		return _postFriendList.contains(target);
	}

	public List<Integer> getPostFriendList()
	{
		return _postFriendList;
	}

	public String getRelationNote(int objectId)
	{
		if(_friendNotes.containsKey(objectId))
		{
			return _friendNotes.get(objectId);
		}

		return "";
	}

	public void updateRelationNote(int objectId, String note)
	{
		if(_friendNotes.containsKey(objectId) && _friendNotes.get(objectId).equals(note))
		{
			return;
		}

		_friendNotes.put(objectId, note);
		updateInDB(objectId);
	}

	/**
	 * Loads relation object information for displaying in friend/ignore details window.
	 *
	 * @param objectId Friend/Ignored object ID.
	 * @return
	 */
	public RelationObjectInfo getRelationObject(int objectId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		L2PcInstance player = WorldManager.getInstance().getPlayer(objectId);
		if(player != null)
		{
			return new RelationObjectInfo(player);
		}

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT cr.friendId, c.char_name, c.online, c.level, c.classid, c.createDate, c.lastAccess, cd.clan_name, cd.ally_name " +
				"FROM character_relations cr " +
				"JOIN characters c ON c.charId = cr.friendId " +
				"LEFT JOIN clan_data cd ON cd.clan_id = c.clanid " +
				"WHERE cr.charId = ? AND cr.friendId = ?");
			statement.setInt(1, _ownerId);
			statement.setInt(2, objectId);
			rset = statement.executeQuery();

			if(rset.next())
			{
				return new RelationObjectInfo(rset);
			}

			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error found in " + _ownerId + " relationList while loading relation object: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		return null;
	}
}