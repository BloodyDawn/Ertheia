package dwo.gameserver.network.game.serverpackets.packet.variation;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExPutItemResultForVariationCancel extends L2GameServerPacket
{
	private int _itemObjId;
	private int _itemId;
	private int _itemAug1;
	private int _itemAug2;
	private int _price;

	public ExPutItemResultForVariationCancel(L2ItemInstance item, int price)
	{
		_itemObjId = item.getObjectId();
		_itemId = item.getItemId();
		_price = price;
		_itemAug1 = (short) item.getAugmentation().getAugmentationId();
		_itemAug2 = item.getAugmentation().getAugmentationId() >> 16;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_itemObjId);
		writeD(_itemId);
		writeD(_itemAug1);
		writeD(_itemAug2);
		writeQ(_price);
		writeD(0x01);
	}
}
