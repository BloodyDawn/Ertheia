package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.friend.L2Friend;
import dwo.gameserver.network.game.serverpackets.packet.friend.L2FriendList;

public class RequestFriendDel extends L2GameClientPacket
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

		int id = CharNameTable.getInstance().getIdByName(_name);

		if(id == -1)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_NOT_ON_YOUR_FRIENDS_LIST).addString(_name));
			return;
		}

		if(!RelationListManager.getInstance().isInFriendList(activeChar, id))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_NOT_ON_YOUR_FRIENDS_LIST).addString(_name));
			return;
		}

		// Удаляем игрока из френдлиста
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(_name));

		RelationListManager.getInstance().removeFromFriendList(activeChar.getObjectId(), id);
		activeChar.sendPacket(new L2Friend(false, id));

		if(!RelationListManager.getInstance().isRelationLoaded(activeChar.getObjectId()))
		{
			RelationListManager.getInstance().restoreRelationList(activeChar.getObjectId());
		}
		RelationListManager.getInstance().removeFromFriendList(id, activeChar.getObjectId());

		L2PcInstance player = WorldManager.getInstance().getPlayer(_name);
		if(player != null)
		{
			player.sendPacket(new L2Friend(false, activeChar.getObjectId()));
			player.sendPacket(new L2FriendList(player));
		}
		activeChar.sendPacket(new L2FriendList(activeChar));

	}

	@Override
	public String getType()
	{
		return "[C] 61 RequestFriendDel";
	}
}

