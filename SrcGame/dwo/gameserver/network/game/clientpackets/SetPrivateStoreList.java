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
package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.model.player.TradeList;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExPrivateStoreWholeMsg;
import dwo.gameserver.network.game.serverpackets.packet.privatestore.PrivateStoreManageList;
import dwo.gameserver.network.game.serverpackets.packet.privatestore.PrivateStoreMsg;
import dwo.gameserver.taskmanager.manager.AttackStanceTaskManager;
import dwo.gameserver.util.Util;

import static dwo.gameserver.model.items.itemcontainer.PcInventory.MAX_ADENA;

public class SetPrivateStoreList extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 20; // length of the one item

	private boolean _packageSale;
	private Item[] _items;

	@Override
	protected void readImpl()
	{
		_packageSale = readD() == 1;
		int count = readD();
		if(count < 1 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}

		_items = new Item[count];
		for(int i = 0; i < count; i++)
		{
			int itemId = readD();
			long cnt = readQ();
			long price = readQ();

			if(itemId < 1 || cnt < 1 || price < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new Item(itemId, cnt, price);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(_items == null)
		{
			player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
			player.setPrivateStoreType(PlayerPrivateStoreType.NONE);
			player.broadcastUserInfo();
			return;
		}

		if(!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		if(AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) || player.isInDuel())
		{
			player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
			player.sendPacket(new PrivateStoreManageList(player, _packageSale));
			player.sendActionFailed();
			return;
		}

		if(player.isInsideZone(L2Character.ZONE_NOSTORE))
		{
			player.sendPacket(new PrivateStoreManageList(player, _packageSale));
			player.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			player.sendActionFailed();
			return;
		}

		// Check maximum number of allowed slots for pvt shops
		if(_items.length > player.getPrivateSellStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageList(player, _packageSale));
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}

		TradeList tradeList = player.getSellList();
		tradeList.clear();
		tradeList.setPackaged(_packageSale);

		long totalCost = player.getAdenaCount();
		for(Item i : _items)
		{
			if(!i.addToTradeList(tradeList))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to set price more than " + MAX_ADENA + " adena in Private Store - Sell.", Config.DEFAULT_PUNISH);
				return;
			}

			totalCost += i.getPrice();
			if(totalCost > MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to set total price more than " + MAX_ADENA + " adena in Private Store - Sell.", Config.DEFAULT_PUNISH);
				return;
			}
		}

		player.sitDown();
		if(_packageSale)
		{
			player.setPrivateStoreType(PlayerPrivateStoreType.SELL_PACKAGE);
		}
		else
		{
			player.setPrivateStoreType(PlayerPrivateStoreType.SELL);
		}

		player.broadcastUserInfo();

		if(_packageSale)
		{
			player.broadcastPacket(new ExPrivateStoreWholeMsg(player));
		}
		else
		{
			player.broadcastPacket(new PrivateStoreMsg(player));
		}
	}

	@Override
	public String getType()
	{
		return "[C] 74 SetPrivateStoreListSell";
	}

	private static class Item
	{
		private final int _itemId;
		private final long _count;
		private final long _price;

		public Item(int id, long num, long pri)
		{
			_itemId = id;
			_count = num;
			_price = pri;
		}

		public boolean addToTradeList(TradeList list)
		{
			if(MAX_ADENA / _count < _price)
			{
				return false;
			}

			list.addItem(_itemId, _count, _price);
			return true;
		}

		public long getPrice()
		{
			return _count * _price;
		}
	}
}
