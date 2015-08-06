package dwo.gameserver.network.game.serverpackets.packet.commission;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

import java.util.List;

/**
 * L2GOD Team
 * User: Bacek, Yorie
 * Date: 20.07.2011
 * Time: 19:39:39
 */

public class ExResponseCommissionItemList extends L2GameServerPacket
{
	private List<L2ItemInstance> _itemList = new FastList();

	public ExResponseCommissionItemList(L2PcInstance _activeChar)
	{
		for(L2ItemInstance item : _activeChar.getInventory().getAvailableItems(false, false, false))
		{
			if(isValidItem(item))
			{
				_itemList.add(item);
			}
		}
	}

	private boolean isValidItem(L2ItemInstance item)
	{
		if(!item.isSellable() || !item.isTradeable())
		{
			return false;
		}

		if(item.getItemId() == 57)
		{
			return false;
		}

		return !item.isEquipped();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_itemList.size());
		_itemList.forEach(this::writeItemInfo);
	}
}