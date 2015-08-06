package dwo.gameserver.network.game.serverpackets.packet.primeshop;

import dwo.gameserver.model.items.primeshop.PrimeShopGroup;
import dwo.gameserver.model.items.primeshop.PrimeShopItem;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExBR_ProductInfo extends L2GameServerPacket
{
	private final PrimeShopGroup _item;

	public ExBR_ProductInfo(PrimeShopGroup item)
	{
        _item = item;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_item.getBrId());
        writeD(_item.getPrice());
        writeD(_item.getItems().size());
        for (PrimeShopItem item : _item.getItems())
        {
            writeD(item.getId());
            writeD((int) item.getCount());
            writeD(item.getWeight());
            writeD(item.isTradable());
        }
	}
}
