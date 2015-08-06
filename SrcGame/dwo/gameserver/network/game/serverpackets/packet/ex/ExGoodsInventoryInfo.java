package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.items.base.L2PremiumItem;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.Map;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 23.06.12
 * Time: 12:47
 */

public class ExGoodsInventoryInfo extends L2GameServerPacket
{
	private Map<Integer, L2PremiumItem> _premiumItemMap;

	public ExGoodsInventoryInfo(Map<Integer, L2PremiumItem> premiumItemMap)
	{
		_premiumItemMap = premiumItemMap;
	}

	@Override
	protected void writeImpl()
	{
		if(_premiumItemMap.isEmpty())
		{
			writeH(0x00);
		}
		else
		{
			writeH(_premiumItemMap.size());
			for(Map.Entry<Integer, L2PremiumItem> entry : _premiumItemMap.entrySet())
			{
				writeQ(entry.getKey());
				writeC(0x00);    // goodsType  0 - берем из датки GoodsIcon /  1 -  берем из обычного места
				writeD(10003);    // goodsIconID  ид иконки
				writeS(entry.getValue().getSender());      // goodsName
				writeS(entry.getValue().getSenderMessage());      // goodsDesc
				writeQ(0x00);    // goodsDate  время до удаления итема
				writeC(0x02);    // goodsCondition  0 1 - COLOR_DEFAULT  2 - COLOR_YELLOW
				writeC(0x00);    // goodsGift

				writeS(null);  // goodsSender
				writeS(null);  // goodsSenderMessage

				writeH(0x01); // size
				writeD(entry.getValue().getItemId());
				writeD((int) entry.getValue().getCount());
			}
		}
	}
}