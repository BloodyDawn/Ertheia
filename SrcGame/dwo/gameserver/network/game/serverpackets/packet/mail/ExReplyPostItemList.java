package dwo.gameserver.network.game.serverpackets.packet.mail;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExReplyPostItemList extends L2GameServerPacket
{
	L2PcInstance _activeChar;
	private L2ItemInstance[] _itemList;

	public ExReplyPostItemList(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		_itemList = _activeChar.getInventory().getAvailableItems(true, false, false);
	}

	@Override
	protected void writeImpl()
	{
		writeD(_itemList.length);
		for(L2ItemInstance item : _itemList)
		{
			writeItemInfo(item);
		}

	}
}
