package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.holders.ItemChanceHolder;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 28.05.2011
 * Time: 12:48:29
 */

public class ExGetCrystalizingEstimation extends L2GameServerPacket
{
	private List<ItemChanceHolder> _products;

	public ExGetCrystalizingEstimation(List<ItemChanceHolder> products)
	{
		_products = products;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_products.size());
		for(ItemChanceHolder item : _products)
		{
			writeD(item.getId());
			writeQ(item.getCount());
			writeF(item.getChance());
		}
	}
}