package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author KenM
 * edit 	Bacek
 */

public class ExRpItemLink extends L2GameServerPacket
{
	private final L2ItemInstance _item;

	public ExRpItemLink(L2ItemInstance item)
	{
		_item = item;
	}

	@Override
	protected void writeImpl()
	{
		writeItemInfo(_item);
	}
}
