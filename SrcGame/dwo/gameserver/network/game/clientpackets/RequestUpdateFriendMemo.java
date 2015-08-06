package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.RelationObjectInfo;

/**
 * L2GOD Team
 * User: ANZO, Yorie
 * Date: 18.10.11
 * Time: 15:36
 */

public class RequestUpdateFriendMemo extends L2GameClientPacket
{
	private String _text;
	private String _friendName;

	@Override
	protected void readImpl()
	{
		_friendName = readS();
		readH();
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

		RelationObjectInfo friend = RelationListManager.getInstance().getRelationObject(player.getObjectId(), CharNameTable.getInstance().getIdByName(_friendName));
		if(friend == null)
		{
			return;
		}

		RelationListManager.getInstance().updateRelationNote(player.getObjectId(), friend.getObjectId(), _text);
	}

	@Override
	public String getType()
	{
		return "[C] D0:98 RequestUpdateFriendMemo";
	}
}
