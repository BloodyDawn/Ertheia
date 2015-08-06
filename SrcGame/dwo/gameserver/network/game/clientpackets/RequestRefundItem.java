package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.BuyListData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.L2TradeList;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.packet.tradelist.ExBuySellList;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoInvenWeight;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;

import java.util.Collection;

import static dwo.gameserver.model.actor.L2Npc.INTERACTION_DISTANCE;

public class RequestRefundItem extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 4; // length of the one item

	private int _listId;
	private int[] _items;

	@Override
	protected void readImpl()
	{
		_listId = readD();
		int count = readD();
		if(count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}

		_items = new int[count];
		for(int i = 0; i < count; i++)
		{
			_items[i] = readD();
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

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.REFUND_ITEM))
		{
			player.sendMessage("Вы используете возврат слишком часто.");
			return;
		}

		if(_items == null || !player.hasRefund())
		{
			player.sendActionFailed();
			return;
		}

		L2Object target = player.getTarget();
		if(target == null || player.getInstanceId() != target.getInstanceId() || !player.isInsideRadius(target, INTERACTION_DISTANCE, true, false))
		{
			player.sendActionFailed();
			return;
		}

		L2Npc merchant = (L2Npc) target;

		L2TradeList list = null;
		double townTaxRate = merchant.getAdenaTownTaxRate(ProcessType.BUY);
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

		long weight = 0;
		long adena = 0;
		long slots = 0;

		L2ItemInstance[] refund = player.getRefund().getItems();
		int[] objectIds = new int[_items.length];

		for(int i = 0; i < _items.length; i++)
		{
			int idx = _items[i];
			if(idx < 0 || idx >= refund.length)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent invalid refund index", Config.DEFAULT_PUNISH);
				return;
			}

			// Проверяем на дубликаты по индексам
			for(int j = i + 1; j < _items.length; j++)
			{
				if(idx == _items[j])
				{
					Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent duplicate refund index", Config.DEFAULT_PUNISH);
					return;
				}
			}

			L2ItemInstance item = refund[idx];
			L2Item template = item.getItem();
			objectIds[i] = item.getObjectId();

			// Проверяем на дубликаты по ObjectID предметов
			for(int j = 0; j < i; j++)
			{
				if(objectIds[i] == objectIds[j])
				{
					Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " has duplicate items in refund list", Config.DEFAULT_PUNISH);
					return;
				}
			}

			long count = item.getCount();
			weight += count * template.getWeight();
			adena += (count * template.getReferencePrice()) * (1 + townTaxRate);
			if(!template.isStackable())
			{
				slots += count;
			}
			else if(player.getItemsCount(template.getItemId()) == 0)
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

		if(adena < 0 || !player.reduceAdena(ProcessType.REFUND, adena, player.getLastFolkNPC(), false))
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			player.sendActionFailed();
			return;
		}

		for(int i = 0; i < _items.length; i++)
		{
			L2ItemInstance item = player.getRefund().transferItem(ProcessType.REFUND, objectIds[i], Long.MAX_VALUE, player.getInventory(), player, player.getLastFolkNPC());
			if(item == null)
			{
				_log.log(Level.WARN, "Error refunding object for char " + player.getName() + " (newitem == null)");
			}
		}

		// Обновляем вес инвентаря
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);

		// Обновляем игроку байлист
        player.sendPacket(new ExUserInfoInvenWeight(player));
		player.sendPacket(new ExBuySellList(player, list, ProcessType.BUY, townTaxRate, false, player.getAdenaCount()));
		player.sendPacket(new ExBuySellList(player, list, ProcessType.SELL, townTaxRate, true, player.getAdenaCount()));
	}

	@Override
	public String getType()
	{
		return "[C] D0:75 RequestRefundItem";
	}
}