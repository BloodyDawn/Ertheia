package dwo.gameserver.network.game.serverpackets.packet.beautyshop;

import dwo.gameserver.datatables.xml.BeautyShopData;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.Collection;

/**
 * User: Bacek
 * Date: 17.11.12
 * Time: 12:24
 */
public class ExResponseBeautyList extends L2GameServerPacket
{
	private final int _itemType;  // itemType   0 прическа  1 лицо
	private final long _ownAdena;
	private Collection<BeautyShopData.BeautyShopList> _list;

	public ExResponseBeautyList(long ownAdena, int itemType)
	{
		_ownAdena = ownAdena;
		_itemType = itemType;
		_list = BeautyShopData.getInstance().getBeautyList(itemType).values();
	}

	@Override
	protected void writeImpl()
	{
		writeQ(_ownAdena);
		writeQ(0x00);   // ownCoin
		writeD(_itemType);
		writeD(_list.size());  // size
		for(BeautyShopData.BeautyShopList st : _list)
		{
			writeD(st._id);
			writeD(st._val);
		}
	}
}
