package dwo.gameserver.network.game.serverpackets.packet.commission;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 *  Проверил: Bacek
 *  Дата: 06.05.12
 *  Протокол: 463 (  Glory Days )
 */

public class ExResponseCommissionBuyItem extends L2GameServerPacket
{
	private int _ok;
	private int _itemId;
	private int _enchantLevel;
	private long _Amount;

	public ExResponseCommissionBuyItem()
	{
	}

	public ExResponseCommissionBuyItem(int ok, int itemId, long Amount, int enchantLevel)
	{
		_ok = ok;
		_itemId = itemId;
		_Amount = Amount;
		_enchantLevel = enchantLevel;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_ok); // 1 покупаем 0 ошибка
		if(_ok == 1)
		{
			writeD(_enchantLevel);
			writeD(_itemId);
			writeQ(_Amount);
		}
	}
}
