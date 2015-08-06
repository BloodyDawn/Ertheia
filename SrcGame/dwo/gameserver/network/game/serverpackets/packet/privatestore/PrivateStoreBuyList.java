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
package dwo.gameserver.network.game.serverpackets.packet.privatestore;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.TradeItem;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PrivateStoreBuyList extends L2GameServerPacket
{
	private int _objId;
	private long _playerAdena;
	private TradeItem[] _items;

	public PrivateStoreBuyList(L2PcInstance player, L2PcInstance storePlayer)
	{
		_objId = storePlayer.getObjectId();
		_playerAdena = player.getAdenaCount();
		storePlayer.getSellList().updateItems(); // Update SellList for case inventory content has changed
		_items = storePlayer.getBuyList().getAvailableItems(player.getInventory());
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objId);
		writeQ(_playerAdena);
		writeD(70); // Glory Days - видел пока что 70, не знаю что значит.
		writeD(_items.length);

		for(TradeItem item : _items)
		{
			writeItemInfo(item);
			//writeD(item.getObjectId());

			writeD(0x01);
			writeQ(item.getPrice());
			writeQ(item.getItem().getReferencePrice() << 1);
			writeQ(item.getStoreCount());
		}
	}
}
