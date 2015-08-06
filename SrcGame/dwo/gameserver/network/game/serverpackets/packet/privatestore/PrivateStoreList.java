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

public class PrivateStoreList extends L2GameServerPacket
{
	private int _objId;
	private long _playerAdena;
	private boolean _packageSale;
	private TradeItem[] _items;

	// player's private shop
	public PrivateStoreList(L2PcInstance player, L2PcInstance storePlayer)
	{
		_objId = storePlayer.getObjectId();
		_playerAdena = player.getAdenaCount();
		_items = storePlayer.getSellList().getItems();
		_packageSale = storePlayer.getSellList().isPackaged();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objId);
		writeD(_packageSale ? 1 : 0);
		writeQ(_playerAdena);
		writeD(0x00); // TODO: GoD
		writeD(_items.length);
		for(TradeItem item : _items)
		{
			writeItemInfo(item);
			writeQ(item.getPrice());
			writeQ(item.getItem().getReferencePrice() << 1);
		}
	}
}
