package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.BuyListData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.L2TradeList;
import dwo.gameserver.model.player.L2TradeList.L2TradeItem;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.packet.tradelist.ExBuySellList;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoInvenWeight;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;

import java.util.Collection;

import static dwo.gameserver.model.actor.L2Npc.INTERACTION_DISTANCE;
import static dwo.gameserver.model.items.itemcontainer.PcInventory.MAX_ADENA;

public class RequestBuyItem extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 12; // length of the one item

	private int _shopId;
	private ItemHolder[] _items;

	@Override
	protected void readImpl()
	{
		_shopId = readD();
		int count = readD();
		if(count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}

		_items = new ItemHolder[count];
		for(int i = 0; i < count; i++)
		{
			int itemId = readD();
			long cnt = readQ();
			if(itemId < 1 || cnt < 1)
			{
				_items = null;
				return;
			}
			_items[i] = new ItemHolder(itemId, cnt);
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

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.BUY_ITEM))
		{
			player.sendMessage("Вы покупаете слишком часто.");
			return;
		}

		if(_items == null || player.hasBadReputation())
		{
			player.sendActionFailed();
			return;
		}

		L2Object target = player.getTarget();
		if(target == null || !player.isInsideRadius(target, INTERACTION_DISTANCE, true, false) || player.getInstanceId() != target.getInstanceId())
		{
			player.sendActionFailed();
			return;
		}

		L2Npc merchant = (L2Npc) target;

		L2TradeList list = null;

		double castleTaxRate = merchant.getAdenaCastleTaxRate(ProcessType.BUY);
		double townTaxRate = merchant.getAdenaTownTaxRate(ProcessType.BUY);

		Collection<L2TradeList> lists = BuyListData.getInstance().getBuyLists(merchant.getNpcId());

		if(lists == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + _shopId, Config.DEFAULT_PUNISH);
			return;
		}

		// Находим нужный нам трейд-лист у NPC
		for(L2TradeList tradeList : lists)
		{
			if(tradeList.getShopId() == _shopId)
			{
				list = tradeList;
			}
		}

		if(list == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + _shopId, Config.DEFAULT_PUNISH);
			return;
		}

		long subTotal = 0;

		// Проверяем валидность покупаемых предметов и инвентарь игрока
		long slots = 0;
		long weight = 0;
		for(ItemHolder item : _items)
		{
			long price;

			L2TradeItem tradeItem = list.getItemById(item.getId());
			if(tradeItem == null)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + _shopId + " and item_id " + item.getId(), Config.DEFAULT_PUNISH);
				return;
			}

			L2Item template = ItemTable.getInstance().getTemplate(item.getId());
			if(template == null)
			{
				continue;
			}

			if(!template.isStackable() && item.getCount() > 1)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase invalid quantity of items at the same time.", Config.DEFAULT_PUNISH);
				player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}

			price = list.getPriceForItemId(item.getId());

			if(price < 0)
			{
				_log.log(Level.WARN, "ERROR, no price found .. wrong buylist ??");
				player.sendActionFailed();
				return;
			}

			if(tradeItem.hasLimitedStock())
			{
				// Попытка купить больше итемов, чем осталось у НПЦ
				if(item.getCount() > tradeItem.getCurrentCount())
				{
					return;
				}
			}

			if(MAX_ADENA / item.getCount() < price)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + MAX_ADENA + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}

			// Сначала считаем сколько будет стоить 1 предмет с учетом всех налогов, потом множим на количество
			price *= 1 + castleTaxRate + townTaxRate;
			subTotal += item.getCount() * price;
			if(subTotal > MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + MAX_ADENA + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}

			weight += item.getCount() * template.getWeight();
			if(!template.isStackable())
			{
				slots += item.getCount();
			}
			else if(player.getInventory().getItemByItemId(item.getId()) == null)
			{
				slots++;
			}
		}

		if(weight > Integer.MAX_VALUE || weight < 0 || !player.getInventory().validateWeight((int) weight))
		{
			player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			player.sendActionFailed();
			return;
		}

		if(slots > Integer.MAX_VALUE || slots < 0 || !player.getInventory().validateCapacity((int) slots))
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			player.sendActionFailed();
			return;
		}

		// Charge buyer and add tax to castle treasury if not owned by npc clan
		if(subTotal < 0 || !player.reduceAdena(ProcessType.BUY, subTotal, player.getLastFolkNPC(), false))
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			player.sendActionFailed();
			return;
		}

		// Производим покупку
		for(ItemHolder buyedItem : _items)
		{
			L2TradeItem tradeItem = list.getItemById(buyedItem.getId());
			if(tradeItem == null)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + _shopId + " and item_id " + buyedItem.getId(), Config.DEFAULT_PUNISH);
				continue;
			}

			if(tradeItem.hasLimitedStock())
			{
				if(tradeItem.decreaseCount(buyedItem.getCount()))
				{
					player.getInventory().addItem(ProcessType.BUY, buyedItem.getId(), buyedItem.getCount(), player, merchant);
				}
			}
			else
			{
				player.getInventory().addItem(ProcessType.BUY, buyedItem.getId(), buyedItem.getCount(), player, merchant);
			}
		}

		// Забираем налог в казну замка
		if(castleTaxRate > 0)
		{
			merchant.getCastle().addToTreasury((long) (subTotal * castleTaxRate));
		}

		// Обновляем вес инвентаря
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);

		// Обновляем игроку байлист
        player.sendPacket(new ExUserInfoInvenWeight(player));
		player.sendPacket(new ExBuySellList(player, list, ProcessType.BUY, castleTaxRate + townTaxRate, false, player.getAdenaCount()));
		player.sendPacket(new ExBuySellList(player, list, ProcessType.SELL, castleTaxRate + townTaxRate, true, player.getAdenaCount()));
	}

	@Override
	public String getType()
	{
		return "[C] 1F RequestBuyItem";
	}
}