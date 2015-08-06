package dwo.gameserver.network.game.serverpackets.packet.henna;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2HennaInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class HennaUnequipList extends L2GameServerPacket
{
	private L2PcInstance _player;

	public HennaUnequipList(L2PcInstance player)
	{
		_player = player;
	}

	private int getHennaUsedSlots()
	{
		int _slots = 0;
		switch(_player.getHennaEmptySlots())
		{
			case 0:
				_slots = 4;
				break;
			case 1:
				_slots = 3;
				break;
			case 2:
				_slots = 2;
				break;
			case 3:
				_slots = 1;
				break;
            case 4:
                _slots = 0;
                break;
		}

		return _slots;
	}

	@Override
	protected void writeImpl()
	{
		writeQ(_player.getAdenaCount());
		writeD(0x00);
		writeD(getHennaUsedSlots());

		for(int i = 1; i <= 4; i++)
		{
			L2HennaInstance henna = _player.getHenna(i);
			if(henna != null)
			{
				writeD(henna.getSymbolId());
				writeD(henna.getItemIdDye());
				writeD(henna.getAmountDyeRequire() / 2);
				writeD(0x00);
				writeD(henna.getPrice() / 5);
				writeD(0x00);
				writeD(0x01);
			}
		}
	}
}