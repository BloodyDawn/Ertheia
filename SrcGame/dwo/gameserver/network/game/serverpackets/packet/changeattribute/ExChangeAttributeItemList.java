package dwo.gameserver.network.game.serverpackets.packet.changeattribute;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

import java.util.List;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 21.09.11
 * Time: 16:51
 */

public class ExChangeAttributeItemList extends L2GameServerPacket
{
	private List<L2ItemInstance> _items = new FastList<>();
	private int _stone;

	public ExChangeAttributeItemList(List<L2ItemInstance> items, int stone)
	{
		_items = items;
		_stone = stone;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_stone);
		writeD(_items.size());
		_items.forEach(this::writeItemInfo);
		_items.clear();
	}
}
