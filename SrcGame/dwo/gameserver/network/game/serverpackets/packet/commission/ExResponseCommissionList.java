package dwo.gameserver.network.game.serverpackets.packet.commission;

import dwo.gameserver.model.holders.CommissionItemHolder;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 *  Проверил: Bacek
 *  Дата: 06.05.12
 *  Протокол: 463 ( Glory Days )
 */

public class ExResponseCommissionList extends L2GameServerPacket
{
	private int _type;
	private int _NumPacket;
	private List<CommissionItemHolder> _items;

	public ExResponseCommissionList(List<CommissionItemHolder> items, int NumPacket, int type)
	{
		_type = type;
		_NumPacket = NumPacket;
		_items = items;

		if(_items.isEmpty())
		{
			if(_type == 2)
			{
				_type = -2;
			}
			else if(_type == 3)
			{
				_type = -1;
			}
		}
	}

	@Override
	protected void writeImpl() //TODO переделать в writeItemInfo()
	{
		/**
		 *   Первое окно 3 есть массив при -1 пусто
		 *   Второе окно 2 есть массив при -2 пусто
		 */
		writeD(_type);
		if(_type != -1 && _type != -2)
		{
			writeD(Long.valueOf(System.currentTimeMillis() / 1000).intValue());
			writeD(_NumPacket); // дробит пакет по 120 итемов
			writeD(_items.size());
			for(CommissionItemHolder temp : _items)
			{
				writeQ(temp.getLotId());
				writeQ(temp.getPrice());
				writeD(temp.getType());

				writeD(0x03); // TODO: хз что видел 0 1 3

				writeD((int) (temp.getTimeEnd() / 1000));
				writeS(temp.getCharName());
				writeD(0x00);
				writeD(temp.getItemId());
				writeQ(temp.getCount());
				writeH(temp.getType2());
				writeD((int) temp.getBodyPart());
				writeH(temp.getEnchantLevel());
				writeH(0x00);  // isBlocked
				writeH(temp.getAttackElementType());
				writeH(temp.getAttackElementPower());

				for(int i = 0; i < 6; i++)
				{
					writeH(temp.getElementDefAttr(i));
				}

				// Enchant Effects
				for(int i = 0; i < 3; i++)
				{
					writeH(temp.getEnchantEffect()[i]);
				}

				writeD(temp.getSkin());
			}
		}
	}
}