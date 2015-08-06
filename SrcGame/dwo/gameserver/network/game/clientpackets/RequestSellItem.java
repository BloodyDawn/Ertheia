package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.BuyListData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.L2TradeList;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.packet.tradelist.ExBuySellList;
import dwo.gameserver.util.Util;

import java.util.Collection;

import static dwo.gameserver.model.actor.L2Npc.INTERACTION_DISTANCE;
import static dwo.gameserver.model.items.itemcontainer.PcInventory.MAX_ADENA;

public class RequestSellItem extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 16; // length of the one item

	private int _listId;
	private ItemHolder[] _items;

	@Override
	protected void readImpl()
	{
		_listId = readD();
		int count = readD();
		if(count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}

		_items = new ItemHolder[count];
		for(int i = 0; i < count; i++)
		{
			int objectId = readD();
			int itemId = readD();
			long cnt = readQ();
			if(objectId < 1 || itemId < 1 || cnt < 1)
			{
				_items = null;
				return;
			}
			_items[i] = new ItemHolder(objectId, cnt);
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

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.SELL_ITEM))
		{
			player.sendMessage("Вы продаете слишком часто.");
			return;
		}

		if(_items == null || player.hasBadReputation())
		{
			player.sendActionFailed();
			return;
		}

		L2Object target = player.getTarget();

		if(target == null || !player.isInsideRadius(target, INTERACTION_DISTANCE, true, false) || player.getInstanceId() != target.getInstanceId() || !(target instanceof L2Npc))
		{
			player.sendActionFailed();
			return;
		}

		L2Npc merchant = (L2Npc) target;

		double townTaxRate = merchant.getAdenaTownTaxRate(ProcessType.SELL);
		double castleTaxRate = merchant.getAdenaCastleTaxRate(ProcessType.SELL);

		L2TradeList list = null;
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

		// Приступаем к продаже предметов
		for(ItemHolder sellItem : _items)
		{
			L2ItemInstance item = player.checkItemManipulation(sellItem.getId(), sellItem.getCount(), ProcessType.SELL);
			if(item == null || !item.isSellable())
			{
				continue;
			}

			// Сначала считаем сколько будет стоить 1 предмет с учетом всех налогов, потом множим на количество
			long price = (long) (item.getReferencePrice() - item.getReferencePrice() * (castleTaxRate + townTaxRate));

			if (Config.REDUCE_ITEM_PRICE_ON_SELL && item.getReferencePrice() > 10)
				price = 10;

			totalPrice += sellItem.getCount() * price;
			if(MAX_ADENA / sellItem.getCount() < price || totalPrice > MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + MAX_ADENA + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}

			// Добавляем предмет в инвентарь возврата
			player.getInventory().transferItem(ProcessType.SELL, sellItem.getId(), sellItem.getCount(), player.getRefund(), player, merchant);
		}

		player.addAdena(ProcessType.SELL, totalPrice, merchant, false);

		// Забираем налог в казну замка
		if(castleTaxRate > 0)
		{
			merchant.getCastle().addToTreasury((long) (totalPrice * castleTaxRate));
		}

		// Обновляем вес инвентаря
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);

		// Обновляем игроку байлист
		player.sendPacket(new ExBuySellList(player, list, ProcessType.BUY, townTaxRate, false, player.getAdenaCount()));
		player.sendPacket(new ExBuySellList(player, list, ProcessType.SELL, townTaxRate, true, player.getAdenaCount()));
	}

	@Override
	public String getType()
	{
		return "[C] 1E RequestSellItem";
	}
}