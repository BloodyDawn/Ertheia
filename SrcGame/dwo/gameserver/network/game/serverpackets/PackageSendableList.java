package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;

/**
 * @author -Wooden-
 * @author UnAfraid
 * Thanks mrTJO
 */
public class PackageSendableList extends L2GameServerPacket
{
	private final L2ItemInstance[] _items;
	private final int _playerObjId;

	public PackageSendableList(L2ItemInstance[] items, int playerObjId)
	{
		_items = items;
		_playerObjId = playerObjId;
	}

	@Override
	protected void writeImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		writeD(_playerObjId);
		writeQ(activeChar.getAdenaCount());
		writeD(_items.length);
		for(L2ItemInstance item : _items)
		{
			writeItemInfo(item);
			writeD(item.getObjectId()); // object id THE REAL ONE
		}
	}
}
