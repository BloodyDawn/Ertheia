package dwo.gameserver.network.game.serverpackets.packet.recipeshop;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class RecipeShopItemInfo extends L2GameServerPacket
{
	private L2PcInstance _player;
	private int _recipeId;

	public RecipeShopItemInfo(L2PcInstance player, int recipeId)
	{
		_player = player;
		_recipeId = recipeId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_player.getObjectId());
		writeD(_recipeId);
		writeD((int) _player.getCurrentMp());
		writeD(_player.getMaxMp());
		writeD(0xffffffff);
	}
}
