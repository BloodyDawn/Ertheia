package dwo.gameserver.network.game.serverpackets.packet.henna;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2HennaInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class HennaEquipList extends L2GameServerPacket
{
	private L2PcInstance _player;
	private L2HennaInstance[] _hennaEquipList;

	public HennaEquipList(L2PcInstance player, L2HennaInstance[] hennaEquipList)
	{
		_player = player;
		_hennaEquipList = hennaEquipList;
	}

	@Override
	protected void writeImpl()
	{
		writeQ(_player.getAdenaCount()); //activeChar current amount of adena
		writeD(3); //available equip slot
		writeD(_hennaEquipList.length);

		for(L2HennaInstance temp : _hennaEquipList)
		{
			// Player must have at least one dye in inventory
			// to be able to see the henna that can be applied with it.
			if(_player.getInventory().getItemByItemId(temp.getItemIdDye()) != null)
			{
				writeD(temp.getSymbolId()); //symbolId
				writeD(temp.getItemIdDye()); //itemId of dye
				writeQ(temp.getAmountDyeRequire()); //amount of dye require
				writeQ(temp.getPrice()); //amount of adena required
				writeD(0x01); //meet the requirement or not
                writeD(0x00);
			}
			else
			{
				writeD(0x00);
				writeD(0x00);
				writeQ(0x00);
				writeQ(0x00);
				writeD(0x00);
                writeD(0x00);
			}
		}
	}
}
