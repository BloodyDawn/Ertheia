package dwo.gameserver.network.game.serverpackets.packet.friend;

import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author JIV
 */

public class L2Friend extends L2GameServerPacket
{
	private boolean _action;
	private boolean _online;
	private int _objid;
	private String _name;

	public L2Friend(boolean action, int objId)
	{
		_action = action;
		_objid = objId;
		_name = CharNameTable.getInstance().getNameById(objId);
		_online = WorldManager.getInstance().getPlayer(objId) != null;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_action ? 1 : 3); // 1-add 3-remove
		writeD(_objid);
		writeS(_name);
		writeD(_online ? 1 : 0);
		writeD(_online ? _objid : 0);
	}
}
