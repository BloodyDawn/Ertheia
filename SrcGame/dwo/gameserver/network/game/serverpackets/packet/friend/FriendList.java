package dwo.gameserver.network.game.serverpackets.packet.friend;

import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.sql.ResultSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mrTJO & UnAfraid
 */

public class FriendList extends L2GameServerPacket
{
	private final List<FriendInfo> _info;

	public FriendList(L2PcInstance player)
	{
		_info = new FastList<>(RelationListManager.getInstance().getFriendList(player.getObjectId()).size());
		_info.addAll(RelationListManager.getInstance().getFriendList(player.getObjectId()).stream().map(FriendInfo::new).collect(Collectors.toList()));
	}

	@Override
	protected void writeImpl()
	{
		writeD(_info.size());
		for(FriendInfo info : _info)
		{
			writeD(0x00); // character id
			writeS(info.getName());
			writeD(info.isOnline());
			writeD(info.isOnline() > 0 ? info.getObjectId() : 0);
			writeD(info.getLevel());
			writeD(info.getClassId());
			writeH(0x00); // unknown
		}
	}

	private static class FriendInfo
	{
		private final int _objectId;
		private String _name;
		private int _level;
		private boolean _online;
		private int _classId;

		public FriendInfo(int objId)
		{
			_objectId = objId;
			L2PcInstance player = WorldManager.getInstance().getPlayer(objId);
			if(player != null)
			{
				_name = player.getName();
				_online = player.isOnline();
				_level = player.getLevel();
				_classId = player.getClassId().getId();
			}
			else
			{
				FiltredPreparedStatement statement = null;
				ThreadConnection con = null;
				ResultSet rset = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement(Characters.SELECT_CHARACTERS_LEVEL_CLASSID_CHAR_NAME);
					statement.setInt(1, _objectId);
					rset = statement.executeQuery();
					if(rset.next())
					{
						_name = rset.getString("char_name");
						_classId = rset.getInt("classid");
						_level = rset.getInt("level");
						_online = false;
					}
					rset.close();
					statement.close();
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, e.getMessage(), e);
				}
				finally
				{
					DatabaseUtils.closeDatabaseCSR(con, statement, rset);
				}
			}
		}

		public int getObjectId()
		{
			return _objectId;
		}

		public String getName()
		{
			return _name;
		}

		public int getLevel()
		{
			return _level;
		}

		public int isOnline()
		{
			return _online ? 0x01 : 0x00;
		}

		public int getClassId()
		{
			return _classId;
		}
	}
}
