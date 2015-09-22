package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.friend.L2Friend;
import dwo.gameserver.network.game.serverpackets.packet.friend.L2FriendList;

public class RequestAnswerFriendInvite extends L2GameClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		readC(); // Ertheia unk
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player != null)
		{
			L2PcInstance requestor = player.getActiveRequester();
			if(requestor == null)
			{
				return;
			}

			if(_response == 1)
			{
				RelationListManager.getInstance().addToFriendList(player.getObjectId(), requestor.getObjectId());
				RelationListManager.getInstance().addToFriendList(requestor.getObjectId(), player.getObjectId());
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND);
				requestor.sendPacket(msg);

				//Player added to your friendlist
				msg = SystemMessage.getSystemMessage(SystemMessageId.S1_ADDED_TO_FRIENDS);
				msg.addString(player.getName());
				requestor.sendPacket(msg);

				//has joined as friend.
				msg = SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_AS_FRIEND);
				msg.addString(requestor.getName());
				player.sendPacket(msg);

				//Send notificacions for both player in order to show them online
				player.sendPacket(new L2FriendList(player));
				requestor.sendPacket(new L2FriendList(requestor));
				player.sendPacket(new L2Friend(true, requestor.getObjectId()));
				requestor.sendPacket(new L2Friend(true, player.getObjectId()));
			}
			else
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_INVITE_A_FRIEND);
				requestor.sendPacket(msg);
			}

			player.setActiveRequester(null);
			requestor.onTransactionResponse();
		}
	}

	@Override
	public String getType()
	{
		return "[C] 5F RequestAnswerFriendInvite";
	}
}
