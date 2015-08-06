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
import dwo.gameserver.network.game.serverpackets.packet.privatestore.PrivateStoreBuyManageList;
import dwo.gameserver.network.game.serverpackets.packet.privatestore.PrivateStoreBuyMsg;
import dwo.gameserver.taskmanager.manager.AttackStanceTaskManager;
import dwo.gameserver.util.Util;

import static dwo.gameserver.model.items.itemcontainer.PcInventory.MAX_ADENA;

public class SetPrivateStoreListBuy extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 40; // length of the one item

	private Item[] _items;
	private int _count;
	private int _itemId;
	private long _countPrice;
	private long _price;
	private int _mana;
	private int _unk0;
	private int _unk1;
	private int _unk2;
	private int _unk3;
	private int _unk4;

	@Override
	protected void readImpl()
	{
		_count = readD();

		if(_count < 1 || _count > Config.MAX_ITEM_IN_PACKET || _count * BATCH_LENGTH > _buf.remaining())
		{
			return;
		}

		_items = new Item[_count];
		for(int i = 0; i < _count; i++)
		{
			_itemId = readD();
			_unk0 = readD();
			_countPrice = readQ();
			_price = readQ();

			if(_itemId < 1 || _countPrice < 1 || _price < 0)
			{
				_items = null;
				return;
			}

			_mana = readD(); // 65534
			_unk1 = readD(); // Unk
			_unk2 = readD(); // Unk
			_unk3 = readD(); // Unk
			_unk4 = readD(); // Unk

			_items[i] = new Item(_itemId, _countPrice, _price);
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
			player.sendPacket(new PrivateStoreBuyManageList(player));
			player.sendActionFailed();
			return;
		}

		if(player.isInsideZone(L2Character.ZONE_NOSTORE))
		{
			player.sendPacket(new PrivateStoreBuyManageList(player));
			player.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			player.sendActionFailed();
			return;
		}

		TradeList tradeList = player.getBuyList();
		tradeList.clear();

		// Check maximum number of allowed slots for pvt shops
		if(_items.length > player.getPrivateBuyStoreLimit())
		{
			player.sendPacket(new PrivateStoreBuyManageList(player));
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}

		long totalCost = 0;
		for(Item i : _items)
		{
			if(!i.addToTradeList(tradeList))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to set price more than " + MAX_ADENA + " adena in Private Store - Buy.", Config.DEFAULT_PUNISH);
				return;
			}

			totalCost += i.getPrice();
			if(totalCost > MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to set total price more than " + MAX_ADENA + " adena in Private Store - Buy.", Config.DEFAULT_PUNISH);
				return;
			}
		}

		// Check for available funds
		if(totalCost > player.getAdenaCount())
		{
			player.sendPacket(new PrivateStoreBuyManageList(player));
			player.sendPacket(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY);
			return;
		}

		player.sitDown();
		player.setPrivateStoreType(PlayerPrivateStoreType.BUY);
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreBuyMsg(player));
	}

	@Override
	public String getType()
	{
		return "[C] 91 SetPrivateStoreListBuy";
	}

	private static class Item
	{
		private final int _itemId;
		private final long _count;
		private final long _price;

		public Item(int id, long count, long price)
		{
			_itemId = id;
			_count = count;
			_price = price;
		}

		public boolean addToTradeList(TradeList list)
		{
			if(MAX_ADENA / _count < _price)
			{
				return false;
			}

			list.addItemByItemId(_itemId, _count, _price);
			return true;
		}

		public long getPrice()
		{
			return _count * _price;
		}
	}
}
