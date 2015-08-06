package dwo.gameserver.network.game.serverpackets.packet.privatestore;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.TradeItem;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PrivateStoreBuyManageList extends L2GameServerPacket
{
	private int _objId;
	private long _playerAdena;
	private L2ItemInstance[] _itemList;
	private TradeItem[] _buyList;

	public PrivateStoreBuyManageList(L2PcInstance player)
	{
		_objId = player.getObjectId();
		_playerAdena = player.getAdenaCount();
		_itemList = player.getInventory().getUniqueItems(false, true);
		_buyList = player.getBuyList().getItems();
	}

	// Rev: 466 Glory Days
	@Override
	protected void writeImpl()
	{
		//section 1
		writeD(_objId);
		writeQ(_playerAdena);

		//section2
		writeD(_itemList.length); // Список итемов в инвентаре.
		for(L2ItemInstance item : _itemList)
		{
			writeItemInfo(item);
			writeQ(item.getItem().getReferencePrice() << 1);
		}

		//section 3
		writeD(_buyList.length); // Список всех итемов выставленных на покупку.
		for(TradeItem item : _buyList)
		{
			writeItemInfo(item);
			writeQ(item.getPrice());
			writeQ(item.getItem().getReferencePrice() << 1);
			writeQ(item.getCount());
		}
	}
}
