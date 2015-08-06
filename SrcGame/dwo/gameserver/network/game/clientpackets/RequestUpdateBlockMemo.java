package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.RelationObjectInfo;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExBlockDefailInfo;

/**
 * L2GOD Team
 * User: ANZO, Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class RequestUpdateBlockMemo extends L2GameClientPacket
{
	String _blockedName;
	String _text;

	@Override
	protected void readImpl()
	{
		_blockedName = readS();
		_text = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(_text.length() > 50)
		{
			return;
		}

		RelationObjectInfo friend = RelationListManager.getInstance().getRelationObject(player.getObjectId(), CharNameTable.getInstance().getIdByName(_blockedName));
		if(friend == null)
		{
			return;
		}

		RelationListManager.getInstance().updateRelationNote(player.getObjectId(), friend.getObjectId(), _text);
		player.sendPacket(new ExBlockDefailInfo(_blockedName, _text));
	}

	@Override
	public String getType()
	{
		return "[C] D0:99 RequestUpdateBlockMemo";
	}
}
