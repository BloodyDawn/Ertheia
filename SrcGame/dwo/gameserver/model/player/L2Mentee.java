package dwo.gameserver.model.player;

import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;

public class L2Mentee
{
	private static final Logger _log = LogManager.getLogger(L2Mentee.class);

	private final int _objectId;
	private String _name;
	private int _classId;
	private int _currentLevel;

	public L2Mentee(int objectId)
	{
		_objectId = objectId;
		load();
	}

	public void load()
	{
		L2PcInstance player = getPlayerInstance();
		if(player == null) // Только если игрок оффлайн
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			ResultSet rset = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(Characters.SELECT_CHARACTERS_LEVEL_CLASSID_CHAR_NAME_BASE_CLASS);
				statement.setInt(1, _objectId);
				rset = statement.executeQuery();
				if(rset.next())
				{
					_name = rset.getString("char_name");
					_classId = rset.getInt("base_class");
					_currentLevel = rset.getInt("level");
				}
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
		else
		{
			_name = player.getName();
			_classId = player.getBaseClassId();
			_currentLevel = player.getLevel();
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

	public int getClassId()
	{
		if(isOnline())
		{
			if(getPlayerInstance().getClassId().getId() != _classId)
			{
				_classId = getPlayerInstance().getClassId().getId();
			}
		}
		return _classId;
	}

	public int getLevel()
	{
		if(isOnline())
		{
			if(getPlayerInstance().getLevel() != _currentLevel)
			{
				_currentLevel = getPlayerInstance().getLevel();
			}
		}
		return _currentLevel;
	}

	public L2PcInstance getPlayerInstance()
	{
		return WorldManager.getInstance().getPlayer(_objectId);
	}

	public boolean isOnline()
	{
		return getPlayerInstance() != null && getPlayerInstance().isOnlineInt() == 1;
	}

	public int isOnlineInt()
	{
		return isOnline() ? getPlayerInstance().isOnlineInt() : 0x00;
	}

	public void sendPacket(L2GameServerPacket packet)
	{
		if(isOnline())
		{
			getPlayerInstance().sendPacket(packet);
		}
	}
}
