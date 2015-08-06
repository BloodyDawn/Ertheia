package dwo.gameserver.network.game.serverpackets.packet.attribute;

import dwo.gameserver.model.items.Elementals;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

import java.util.List;

/*
 * Upd by Keiichi
 */

public class ExChooseInventoryAttributeItem extends L2GameServerPacket
{
	private List<L2ItemInstance> _list = new FastList<>();

	private int _stone;
	private byte _atribute;
	private int _level;
	private long _stoneCount;

	public ExChooseInventoryAttributeItem(List<L2ItemInstance> items, int stone, long stoneCount)
	{
		_list = items;
		_stone = stone;
		_stoneCount = stoneCount;

		_atribute = Elementals.getItemElement(_stone);
		if(_atribute == Elementals.NONE)
		{
			throw new IllegalArgumentException("Undefined Atribute item: " + stone);
		}
		_level = Elementals.getMaxElementLevel(_stone);
	}

	@Override
	protected void writeImpl()
	{
		// dQ d dd dd dd d [d]
		writeD(_stone);
		writeQ(_stoneCount);
		writeD(_atribute == Elementals.FIRE ? 1 : 0);    // Fire
		writeD(_atribute == Elementals.WATER ? 1 : 0);    // Water
		writeD(_atribute == Elementals.WIND ? 1 : 0);    // Wind
		writeD(_atribute == Elementals.EARTH ? 1 : 0);    // Earth
		writeD(_atribute == Elementals.HOLY ? 1 : 0);    // Holy
		writeD(_atribute == Elementals.DARK ? 1 : 0);    // Unholy
		writeD(_level);    // Item max attribute level

		writeD(_list.size());

		for(L2ItemInstance temp : _list)
		{
			writeD(temp.getObjectId());
		}
		_list.clear();

	}
}
