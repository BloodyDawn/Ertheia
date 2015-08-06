package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.items.base.L2PremiumItem;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.Map;

public class ExGetPremiumItemList extends L2GameServerPacket
{
	private Map<Integer, L2PremiumItem> _premiumItemMap;

	public ExGetPremiumItemList(Map<Integer, L2PremiumItem> premiumItemMap)
	{
		_premiumItemMap = premiumItemMap;
	}

	@Override
	protected void writeImpl()
	{
		if(_premiumItemMap.isEmpty())
		{
			writeD(0);
		}
		else
		{
			writeD(_premiumItemMap.size());
			for(Map.Entry<Integer, L2PremiumItem> entry : _premiumItemMap.entrySet())
			{
				writeQ(entry.getKey());
				writeD(entry.getValue().getItemId());
				writeQ(entry.getValue().getCount());
				writeD(0x01);
				writeS(entry.getValue().getSender());
			}
		}
	}
}