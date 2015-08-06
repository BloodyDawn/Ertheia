package dwo.gameserver.network.game.serverpackets.packet.recipeshop;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class RecipeShopMsg extends L2GameServerPacket
{
	private L2PcInstance _activeChar;

	public RecipeShopMsg(L2PcInstance player)
	{
		_activeChar = player;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getCreateList().getStoreName());//_activeChar.getTradeList().getSellStoreName());
	}
}
