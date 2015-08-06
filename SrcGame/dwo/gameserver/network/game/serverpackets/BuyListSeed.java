/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.instancemanager.castle.CastleManorManager.SeedProduction;
import javolution.util.FastList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Format: c ddh[hdddhhd]
 * c - id (0xE8)
 *
 * d - money
 * d - manor id
 * h - size
 * [
 * h - item type 1
 * d - object id
 * d - item id
 * d - count
 * h - item type 2
 * h
 * d - price
 * ]
 *
 * @author l3x
 */

public class BuyListSeed extends L2GameServerPacket
{
	private int _manorId;
	private List<Seed> _list;
	private long _money;

	public BuyListSeed(long currentMoney, int castleId, List<SeedProduction> seeds)
	{
		_money = currentMoney;
		_manorId = castleId;

		if(seeds != null && !seeds.isEmpty())
		{
			_list = new FastList<>();
			_list.addAll(seeds.stream().filter(s -> s.getCanProduce() > 0 && s.getPrice() > 0).map(s -> new Seed(s.getId(), s.getCanProduce(), s.getPrice())).collect(Collectors.toList()));
		}
	}

	@Override
	protected void writeImpl()
	{
		writeQ(_money); // current money
        writeD(0x00); //TODO
		writeD(_manorId); // manor id

		if(_list != null && !_list.isEmpty())
		{
			writeH(_list.size()); // list length
			for(Seed s : _list)
			{
                writeC(0x00); //mask
                writeD(s._itemId);
				writeD(s._itemId);
				writeC(0xFF);
				writeQ(s._count); // item count
				writeC(0x05); // Custom Type 2
				writeC(0x00); // Custom Type 1
				writeH(0x00); // Equipped
				writeQ(0x00); // Body Part
				writeH(0x00); // Enchant
				writeD(-1); // Mana
				writeD(-9999); // Time
				writeC(0x01);

				writeQ(s._price); // price
			}
			_list.clear();
		}
		else
		{
			writeH(0x00);
		}

	}

	private static class Seed
	{
		public final int _itemId;
		public final long _count;
		public final long _price;

		public Seed(int itemId, long count, long price)
		{
			_itemId = itemId;
			_count = count;
			_price = price;
		}
	}
}
