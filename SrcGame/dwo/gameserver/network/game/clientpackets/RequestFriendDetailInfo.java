package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.RelationObjectInfo;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExFriendDetailInfo;

/**
 * L2GOD Team
 * User: Yorie, ANZO
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class RequestFriendDetailInfo extends L2GameClientPacket
{
	String _charName;

	@Override
	protected void readImpl()
	{
		_charName = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(_charName.length() <= 0)
		{
			return;
		}

		RelationObjectInfo object = RelationListManager.getInstance().getRelationObject(player.getObjectId(), CharNameTable.getInstance().getIdByName(_charName));
		if(object == null)
		{
			return;
		}

		player.sendPacket(new ExFriendDetailInfo(player, object));
	}

	@Override
	public String getType()
	{
		return "[C] D0:97 RequestFriendDetailInfo";
	}
}