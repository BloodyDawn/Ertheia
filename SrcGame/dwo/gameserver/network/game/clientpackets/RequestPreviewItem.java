package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.BuyListData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Armor;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.base.type.L2ArmorType;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.L2TradeList;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.ShopPreviewInfo;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoEquipSlot;
import dwo.gameserver.util.Util;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.log4j.Level;

import java.util.Collection;

/**
 ** @author Gnacik
 */

public class RequestPreviewItem extends L2GameClientPacket
{
	private L2PcInstance player;
	private TIntIntHashMap _item_list;
	private int _unk;
	private int _listId;
	private int _count;
	private int[] _items;

	@Override
	protected void readImpl()
	{
		_unk = readD();
		_listId = readD();
		_count = readD();

		if(_count < 0)
		{
			_count = 0;
		}
		if(_count > 100)
		{
			return; // prevent too long lists
		}

		// Create _items table that will contain all ItemID to Wear
		_items = new int[_count];

		// Fill _items table with all ItemID to Wear
		for(int i = 0; i < _count; i++)
		{
			_items[i] = readD();
		}
	}

	@Override
	protected void runImpl()
	{
		if(_items == null)
		{
			return;
		}

		// Get the current player and return if null
		player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.BUY_ITEM))
		{
			player.sendMessage("Вы покупаете слишком быстро.");
			return;
		}

		// If Alternate rule Reputation punishment is set to true, forbid Wear to player with Karma
		if(player.hasBadReputation())
		{
			return;
		}

		// Check current target of the player and the INTERACTION_DISTANCE
		L2Object target = player.getTarget();
		if(target == null || !player.isInsideRadius(target, L2Npc.INTERACTION_DISTANCE, false, false))
		{
			return;
		}

		if(_count < 1 || _listId >= 4000000)
		{
			player.sendActionFailed();
			return;
		}

		L2TradeList list = null;

		// Get the current merchant targeted by the player
		L2Npc merchant = (L2Npc) target;
		if(merchant == null)
		{
			_log.log(Level.WARN, getClass().getName() + " Null merchant!");
			return;
		}

		Collection<L2TradeList> lists = BuyListData.getInstance().getBuyLists(merchant.getNpcId());

		if(lists == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + _listId, Config.DEFAULT_PUNISH);
			return;
		}

		for(L2TradeList tradeList : lists)
		{
			if(tradeList.getShopId() == _listId)
			{
				list = tradeList;
			}
		}

		if(list == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + _listId, Config.DEFAULT_PUNISH);
			return;
		}

		long totalPrice = 0;
		_listId = list.getShopId();
		_item_list = new TIntIntHashMap();

		for(int i = 0; i < _count; i++)
		{
			int itemId = _items[i];

			if(!list.containsItemId(itemId))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + _listId + " and item_id " + itemId, Config.DEFAULT_PUNISH);
				return;
			}

			L2Item template = ItemTable.getInstance().getTemplate(itemId);
			if(template == null)
			{
				continue;
			}

			int slot = Inventory.getPaperdollIndex(template.getBodyPart());
			if(slot < 0)
			{
				continue;
			}

			if(template instanceof L2Weapon)
			{
				if(player.getRace().ordinal() == 5)
				{
					if(template.getItemType() == L2WeaponType.NONE)
					{
						continue;
					}
					else if(template.getItemType() == L2WeaponType.RAPIER || template.getItemType() == L2WeaponType.CROSSBOW || template.getItemType() == L2WeaponType.ANCIENTSWORD)
					{
						continue;
					}
				}
			}
			else if(template instanceof L2Armor)
			{
				if(player.getRace().ordinal() == 5)
				{
					if(template.getItemType() == L2ArmorType.HEAVY || template.getItemType() == L2ArmorType.MAGIC)
					{
						continue;
					}
				}
			}

			if(_item_list.containsKey(slot))
			{
				player.sendPacket(SystemMessageId.YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME);
				return;
			}
			_item_list.put(slot, itemId);

			totalPrice += Config.WEAR_PRICE;
			if(totalPrice > PcInventory.MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + PcInventory.MAX_ADENA + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}
		}

		// Charge buyer and add tax to castle treasury if not owned by npc clan because a Try On is not Free
		if(totalPrice < 0 || !player.reduceAdena(ProcessType.WEAR, totalPrice, player.getLastFolkNPC(), true))
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}

		if(!_item_list.isEmpty())
		{
			player.sendPacket(new ShopPreviewInfo(_item_list));
			// Schedule task
			ThreadPoolManager.getInstance().scheduleGeneral(new RemoveWearItemsTask(), Config.WEAR_DELAY * 1000);
		}
	}

	@Override
	public String getType()
	{
		return "[C] C7 RequestPreviewItem";
	}

	private class RemoveWearItemsTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				player.sendPacket(SystemMessageId.NO_LONGER_TRYING_ON);
				player.sendPacket(new ExUserInfoEquipSlot(player));
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}
}