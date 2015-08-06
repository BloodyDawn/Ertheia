package dwo.gameserver.network.game.serverpackets.packet.pet;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import org.apache.log4j.Level;

public class PetItemList extends L2GameServerPacket
{
	private L2PetInstance _activeChar;

	public PetItemList(L2PetInstance character)
	{
		_activeChar = character;
		if(Config.DEBUG)
		{
			L2ItemInstance[] items = _activeChar.getInventory().getItems();
			for(L2ItemInstance temp : items)
			{
				_log.log(Level.DEBUG, "item:" + temp.getItem().getName() +
					" type1:" + temp.getItem().getType1() + " type2:" + temp.getItem().getType2());
			}
		}
	}

	@Override
	protected void writeImpl()
	{
		L2ItemInstance[] items = _activeChar.getInventory().getItems();
		int count = items.length;

		writeH(count);

		for(L2ItemInstance temp : items)
		{
			writeItemInfo(temp);
		}
	}
}
