package dwo.gameserver.network.game.serverpackets.packet.friend;

import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class FriendStatus extends L2GameServerPacket
{
	private boolean _online;
	private int _objid;
	private String _name;

	public FriendStatus(int objId)
	{
		_objid = objId;
		_name = CharNameTable.getInstance().getNameById(objId);
		_online = WorldManager.getInstance().getPlayer(objId) != null;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_online ? 1 : 0);
		writeD(_objid);
		writeS(_name);
	}
}
