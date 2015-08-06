package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.friend.FriendAddRequest;

public class RequestFriendInvite extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}

		L2PcInstance friend = WorldManager.getInstance().getPlayer(_name);

		SystemMessage sm;

		// can't use friend invite for locating invisible characters
		if(friend == null || !friend.isOnline() || friend.getAppearance().getInvisible() || friend.getOlympiadController().isParticipating())
		{
			//Target is not found in the game.
			activeChar.sendPacket(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
			return;
		}
		if(friend.equals(activeChar))
		{
			//You cannot add yourself to your own friend list.
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
			return;
		}
		if(RelationListManager.getInstance().isBlocked(activeChar, friend))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.BLOCKED_C1);
			sm.addCharName(friend);
			activeChar.sendPacket(sm);
			return;
		}
		if(RelationListManager.getInstance().isBlocked(friend, activeChar))
		{
			activeChar.sendMessage("Вы в блоклисте у цели.");
			return;
		}

		if(RelationListManager.getInstance().isInFriendList(activeChar, friend))
		{
			// Player already is in your friendlist
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
			return;
		}

		if(friend.isProcessingRequest())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			sm.addString(_name);
		}
		else
		{
			// requets to become friend
			activeChar.onTransactionRequest(friend);
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_REQUESTED_C1_TO_BE_FRIEND);
			sm.addString(_name);
			FriendAddRequest ajf = new FriendAddRequest(activeChar.getName());
			friend.sendPacket(ajf);
		}

		activeChar.sendPacket(sm);
	}

	@Override
	public String getType()
	{
		return "[C] 5E RequestFriendInvite";
	}
}