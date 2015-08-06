package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class RequestFriendList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}

		SystemMessage sm;

		// ======<Friend List>======
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_HEADER);

		L2PcInstance friend = null;
		for(int id : RelationListManager.getInstance().getFriendList(activeChar.getObjectId()))
		{
			// int friendId = rset.getInt("friendId");
			String friendName = CharNameTable.getInstance().getNameById(id);

			if(friendName == null)
			{
				continue;
			}

			friend = WorldManager.getInstance().getPlayer(friendName);

			if(friend == null || !friend.isOnline())
			{
				// (Currently: Offline)
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_OFFLINE);
				sm.addString(friendName);
			}
			else
			{
				// (Currently: Online)
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ONLINE);
				sm.addString(friendName);
			}

			activeChar.sendPacket(sm);
		}

		// =========================
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
	}

	@Override
	public String getType()
	{
		return "[C] 60 RequestFriendList";
	}
}
