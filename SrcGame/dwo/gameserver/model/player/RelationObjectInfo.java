package dwo.gameserver.model.player;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.player.formation.clan.L2Clan;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class RelationObjectInfo
{
	private final int _objectId;
	private final String _name;
	private final boolean _isOnline;
	private final int _level;
	private final int _classId;
	private final String _clanName;
	private final String _allyName;
	private final long _createTime;
	private final long _loginTime;

	public RelationObjectInfo(L2PcInstance player)
	{
		_objectId = player.getObjectId();
		_name = player.getName();
		_isOnline = player.isOnline();
		_level = player.getLevel();
		_classId = player.getClassId().getId();
		L2Clan clan = player.getClan();
		if(clan != null)
		{
			_clanName = clan.getName();
			_allyName = clan.getAllyName();
		}
		else
		{
			_clanName = "";
			_allyName = "";
		}
		_createTime = player.getCreateDate().getTimeInMillis();
		_loginTime = player.getLastAccess();
	}

	public RelationObjectInfo(ResultSet rset) throws SQLException
	{
		_objectId = rset.getInt("friendId");
		_name = rset.getString("char_name");
		_isOnline = rset.getBoolean("online");
		_level = rset.getInt("level");
		_classId = ClassId.values()[rset.getInt("classid")].getId();
		String clanName = rset.getString("clan_name");
		String allyName = rset.getString("ally_name");
		_clanName = clanName != null ? rset.getString("clan_name") : "";
		_allyName = allyName != null ? rset.getString("ally_name") : "";
		_createTime = rset.getLong("createdate");
		_loginTime = rset.getLong("lastAccess");
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public String getName()
	{
		return _name;
	}

	public boolean isOnline()
	{
		return _isOnline;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getClassId()
	{
		return _classId;
	}

	public String getClanName()
	{
		return _clanName;
	}

	public String getAllyName()
	{
		return _allyName;
	}

	public long getCreateTime()
	{
		return _createTime;
	}

	public long getLoginTime()
	{
		return _loginTime;
	}
}
