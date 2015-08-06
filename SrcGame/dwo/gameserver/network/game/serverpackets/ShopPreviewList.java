package dwo.gameserver.network.game.serverpackets;

import dwo.config.Config;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.player.L2TradeList;
import dwo.gameserver.model.player.L2TradeList.L2TradeItem;

import java.util.Collection;

public class ShopPreviewList extends L2GameServerPacket
{
	private int _listId;
	private Collection<L2TradeItem> _list;
	private long _money;
	private int _expertise;

	public ShopPreviewList(L2TradeList list, long currentMoney, int expertiseIndex)
	{
		_listId = list.getShopId();
		_list = list.getItems();
		_money = currentMoney;
		_expertise = expertiseIndex;
	}

	public ShopPreviewList(Collection<L2TradeItem> lst, int listId, long currentMoney)
	{
		_listId = listId;
		_list = lst;
		_money = currentMoney;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xc0);    // ?
		writeC(0x13);    // ?
		writeC(0x00);    // ?
		writeC(0x00);    // ?
		writeQ(_money);        // current money
		writeD(_listId);

		int newlength = 0;
		for(L2TradeItem item : _list)
		{
			if(item.getTemplate().getCrystalType().ordinal() <= _expertise && item.getTemplate().isEquipable())
			{
				newlength++;
			}
		}
		writeH(newlength);

		// item type2
		_list.stream().filter(item -> item.getTemplate().getCrystalType().ordinal() <= _expertise && item.getTemplate().isEquipable()).forEach(item -> {
			writeD(item.getItemId());
			writeH(item.getTemplate().getType2());    // item type2

			if(item.getTemplate().getType1() == L2Item.TYPE1_ITEM_QUESTITEM_ADENA)
			{
				writeH(0x00);
			}
			else
			{
				writeH((int) item.getTemplate().getBodyPart());
			}
			writeQ(Config.WEAR_PRICE);
		});
	}
}
