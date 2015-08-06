package dwo.gameserver.instancemanager;

import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.RelationData;
import dwo.gameserver.model.player.RelationObjectInfo;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

public class RelationListManager
{
	private TIntObjectHashMap<RelationData> _relationList;

	public RelationListManager()
	{
		_relationList = new TIntObjectHashMap<>();
	}

	public static RelationListManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void restoreRelationList(int listOwnerId)
	{
		if(!_relationList.containsKey(listOwnerId))
		{
			_relationList.put(listOwnerId, new RelationData(listOwnerId));
		}
	}

	public boolean isRelationLoaded(int listOwnerId)
	{
		return _relationList.containsKey(listOwnerId);
	}

	// Block list functions
	private void addToBlockList(int player, int target)
	{
		synchronized(this)
		{
			_relationList.get(player).addToBlockList(target);
		}
	}

	private void removeFromBlockList(int player, int target)
	{
		synchronized(this)
		{
			_relationList.get(player).removeFromBlockList(target);
		}
	}

	public boolean isInBlockList(int player, int target)
	{
		if(!_relationList.containsKey(player))
		{
			restoreRelationList(player);
		}
		return _relationList.get(player).isInBlockList(target);
	}

	private boolean isBlockAll(L2PcInstance player)
	{
		if(player == null)
		{
			return false;
		}
		return player.getMessageRefusal();
	}

	public boolean isBlocked(L2PcInstance player, L2PcInstance target)
	{
		return isBlockAll(player) || isInBlockList(player.getObjectId(), target.getObjectId());
	}

	public List<Integer> getBlockList(int player)
	{
		return _relationList.get(player).getBlockList();
	}

	public void addToBlockList(L2PcInstance listOwner, int target)
	{
		if(listOwner == null)
		{
			return;
		}

		String charName = CharNameTable.getInstance().getNameById(target);

		if(_relationList.get(listOwner.getObjectId()).isFriend(target))
		{
			listOwner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST).addString(charName));
			return;
		}
		if(isInBlockList(listOwner.getObjectId(), target))
		{
			listOwner.sendMessage("Уже находится в блоклисте.");
			return;
		}

		addToBlockList(listOwner.getObjectId(), target);

		listOwner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST).addString(charName));

		L2PcInstance player = WorldManager.getInstance().getPlayer(target);

		if(player != null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST).addString(listOwner.getName()));
		}
	}

	public void removeFromBlockList(L2PcInstance listOwner, int target)
	{
		if(listOwner == null)
		{
			return;
		}

		SystemMessage sm;

		String charName = CharNameTable.getInstance().getNameById(target);

		if(!isInBlockList(listOwner.getObjectId(), target))
		{
			listOwner.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}

		removeFromBlockList(listOwner.getObjectId(), target);

		listOwner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST).addString(charName));
	}

	public void sendBlockListToOwner(L2PcInstance listOwner)
	{
		int i = 1;
		listOwner.sendPacket(SystemMessageId.BLOCK_LIST_HEADER);
		for(int playerId : _relationList.get(listOwner.getObjectId()).getBlockList())
		{
			listOwner.sendMessage(i++ + ". " + CharNameTable.getInstance().getNameById(playerId));
		}
		listOwner.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
	}

	// FriendList functions
	public void addToFriendList(int player, int target)
	{
		synchronized(this)
		{
			_relationList.get(player).addToFriendList(target);
		}
	}

	public void removeFromFriendList(int player, int target)
	{
		synchronized(this)
		{
			if(!_relationList.containsKey(player))
			{
				restoreRelationList(player);
			}
			_relationList.get(player).removeFromFriendList(target);
		}
	}

	public boolean isInFriendList(L2PcInstance player, int targetId)
	{
		return _relationList.get(player.getObjectId()).isFriend(targetId);
	}

	public boolean isInFriendList(L2PcInstance player, L2PcInstance target)
	{
		return _relationList.get(player.getObjectId()).isFriend(target.getObjectId());
	}

	public List<Integer> getFriendList(int player)
	{
		RelationData _data = _relationList.get(player);
		if(_data == null)
		{
			restoreRelationList(player);
		}

		return _relationList.get(player).getFriendList();
	}

	// PostFriendList functions
	public void addToPostFriendList(int player, int target)
	{
		synchronized(this)
		{
			_relationList.get(player).addToPostFriendList(target);
		}
	}

	public void removeFromPostFriendList(int player, int target)
	{
		synchronized(this)
		{
			_relationList.get(player).removeFromPostFriendList(target);
		}
	}

	public boolean isInPostFriendList(L2PcInstance player, int targetId)
	{
		return _relationList.get(player.getObjectId()).isPostFriend(targetId);
	}

	public List<Integer> getPostFriendList(int player)
	{
		return _relationList.get(player).getPostFriendList();
	}

	public String getRelationNote(int player, int objectId)
	{
		RelationData data = _relationList.get(player);
		if(data == null)
		{
			return "";
		}

		return data.getRelationNote(objectId);
	}

	public void updateRelationNote(int player, int objectId, String note)
	{
		RelationData data = _relationList.get(player);
		if(data == null)
		{
			return;
		}

		data.updateRelationNote(objectId, note);
	}

	public RelationObjectInfo getRelationObject(int player, int target)
	{
		if(player <= 0 || target <= 0)
		{
			return null;
		}

		return _relationList.get(player).getRelationObject(target);
	}

	private static class SingletonHolder
	{
		protected static final RelationListManager _instance = new RelationListManager();
	}
}