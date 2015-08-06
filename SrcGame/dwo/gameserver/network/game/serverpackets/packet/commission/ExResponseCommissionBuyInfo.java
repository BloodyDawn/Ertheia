package dwo.gameserver.network.game.serverpackets.packet.commission;

import dwo.gameserver.model.holders.CommissionItemHolder;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 *  Проверил: Bacek
 *  Дата: 06.05.12
 *  Протокол: 463 (  Glory Days )
 */

public class ExResponseCommissionBuyInfo extends L2GameServerPacket
{
	private CommissionItemHolder _item;

	public ExResponseCommissionBuyInfo(CommissionItemHolder item)
	{
		_item = item;
	}

	@Override
	protected void writeImpl() //TODO переписать на writeItem()
	{
		writeD(0x01);
		writeQ(_item.getPrice());
		writeQ(_item.getLotId());
		writeD(_item.getType());
		writeD(0x00);
		writeD(_item.getItemId());
		writeQ(_item.getCount());
		writeH(_item.getType2());
		writeD((int) _item.getBodyPart());
		writeH(_item.getEnchantLevel());
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(_item.getAttackElementType());
		writeH(_item.getAttackElementPower());
		for(int i = 0; i < 6; i++)
		{
			writeH(_item.getElementDefAttr(i));
		}
		// Enchant Effects
		for(int i = 0; i < 3; i++)
		{
			writeH(_item.getEnchantEffect()[i]);
		}

		writeD(_item.getSkin());
	}
}
