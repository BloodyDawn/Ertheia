package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

public class ExReceiveShowPostFriend extends L2GameServerPacket
{
	private L2PcInstance _requestor;

	public ExReceiveShowPostFriend(L2PcInstance requestor)
	{
		_requestor = requestor;
	}

	@Override
	protected void writeImpl()
	{
		List<Integer> list = RelationListManager.getInstance().getPostFriendList(_requestor.getObjectId());
		writeD(list.size());
		for(int objId : list)
		{
			String name = CharNameTable.getInstance().getNameById(objId);
			if(name == null)
			{
				writeS("");
			}
			else
			{
				writeS(name);
			}
		}
	}
}
