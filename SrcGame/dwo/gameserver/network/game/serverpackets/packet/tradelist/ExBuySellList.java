package dwo.gameserver.network.game.serverpackets.packet.tradelist;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.L2TradeList;
import dwo.gameserver.model.player.L2TradeList.L2TradeItem;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.ArrayList;
import java.util.List;

public class ExBuySellList extends L2GameServerPacket
{
	private final ProcessType _type;
	private List<L2TradeItem> _buyList = new ArrayList<>();
	private L2ItemInstance[] _sellList;
	private L2ItemInstance[] _refundList;
	private boolean _done;
	private long _money;
	private int _shopId;
	private double _taxRate;

	public ExBuySellList(L2PcInstance player, L2TradeList list, ProcessType type, double taxRate, boolean done, long currentMoney)
	{
		for(L2TradeItem item : list.getItems())
		{
			if(item.hasLimitedStock() && item.getCurrentCount() <= 0)
			{
				continue;
			}

			_buyList.add(item);
		}

		_sellList = player.getInventory().getAvailableItems(false, false, false);

		if(player.hasRefund())
		{
			_refundList = player.getRefund().getItems();
		}

		_done = done;
		_money = currentMoney;
		_shopId = list.getShopId();
		_taxRate = taxRate;
		_type = type;
	}

	@Override
	protected void writeImpl()
	{
		if(_buyList != null && !_buyList.isEmpty() && _type == ProcessType.BUY)
		{
			writeD(0x00);        // 0 магазин
			writeQ(_money);        // money
			writeD(_shopId);    // Номер магазина у текущего NPC
			writeD(0x00);        // увеличивая число закрываем ячейки

			writeH(_buyList.size());
			for(L2TradeItem item : _buyList)
            {
                writeItemInfo(item);

                if((item.getItemId() >= 3960) && (item.getItemId() <= 4026))
                {
                    writeQ((long) (item.getPrice() * 1 * (1 + _taxRate))); //todo вывести в конфиг рейт на стоимость гвардов замка
                }
                else
                {
                    writeQ((long) (item.getPrice() * (1 + _taxRate)));
                }
            }
        }

		if(_sellList != null && _sellList.length > 0 && _type == ProcessType.SELL)
		{
			writeD(0x01); // 1 продажа
			writeD(0x00); // увеличивая число закрываем ячейки
			writeH(_sellList.length);  // размер
			for(L2ItemInstance item : _sellList)
			{
				writeItemInfo(item);
				writeQ((long) (item.getItem().getReferencePrice() - item.getItem().getReferencePrice() * _taxRate));
			}
		}
		else
		{
			writeD(0x01); // 1 продажа
			writeD(0x00); // увеличивая число закрываем ячейки
			writeH(0x00); // размер
		}

		/* Вот эту ахинею перенести в пакет который вроде как предназначен спициально для возврата итемов ExRefundList */
		if(_refundList != null && _refundList.length > 0)
		{
			writeH(_refundList.length);
			int idx = 0;
			for(L2ItemInstance item : _refundList)
			{
				writeItemInfo(item);
				writeD(idx++);
				writeQ(item.getItem().getReferencePrice() / 2 * item.getCount());
			}
		}

		writeC(_done ? 0x01 : 0x00);
	}
}