package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class RequestExDeletePostFriendForPostBox extends L2GameClientPacket
{
	private String _playerName;

	@Override
	protected void readImpl()
	{
		_playerName = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		int playerId = CharNameTable.getInstance().getIdByName(_playerName);
		if(playerId > 0 && RelationListManager.getInstance().isInPostFriendList(activeChar, playerId))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESFULLY_DELETED_FROM_CONTACT_LIST);
			sm.addString(_playerName);
			activeChar.sendPacket(sm);
			RelationListManager.getInstance().removeFromPostFriendList(activeChar.getObjectId(), playerId);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:85 RequestExDeleteContactFromContactList";
	}
}
