package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExConfirmAddingPostFriend;

public class RequestExAddPostFriendForPostBox extends L2GameClientPacket
{
	private static final int MAX_POST_FRIEND_LIST_SIZE = 100;
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

		byte confirmType = ExConfirmAddingPostFriend.SUCCESS;
		int playerId = CharNameTable.getInstance().getIdByName(_playerName);
		if(playerId <= 0)
		{
			confirmType = ExConfirmAddingPostFriend.NOT_EXISTS;
		}
		else if(RelationListManager.getInstance().getPostFriendList(activeChar.getObjectId()).size() >= MAX_POST_FRIEND_LIST_SIZE)
		{
			confirmType = ExConfirmAddingPostFriend.MAX_REACHED;
		}
		else if(RelationListManager.getInstance().isInPostFriendList(activeChar, playerId))
		{
			confirmType = ExConfirmAddingPostFriend.ALREADY_EXISTS;
		}

		if(confirmType == ExConfirmAddingPostFriend.SUCCESS)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESSFULLY_ADDED_TO_CONTACT_LIST);
			sm.addString(_playerName);
			activeChar.sendPacket(sm);
			RelationListManager.getInstance().addToPostFriendList(activeChar.getObjectId(), playerId);
		}
		activeChar.sendPacket(new ExConfirmAddingPostFriend(_playerName, confirmType));
	}

	@Override
	public String getType()
	{
		return "[C] D0:84 RequestExAddContactToContactList";
	}
}
