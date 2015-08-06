package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author devScarlet & mrTJO
 */

public class ShowXMasSeal extends L2GameServerPacket
{
	private int _itemId;

	public ShowXMasSeal(int itemId)
	{
		_itemId = itemId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_itemId);
	}
}
