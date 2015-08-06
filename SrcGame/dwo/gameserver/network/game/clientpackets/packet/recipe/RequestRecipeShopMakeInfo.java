package dwo.gameserver.network.game.clientpackets.packet.recipe;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.recipeshop.RecipeShopItemInfo;

public class RequestRecipeShopMakeInfo extends L2GameClientPacket
{
	private int _playerObjectId;
	private int _recipeId;

	@Override
	protected void readImpl()
	{
		_playerObjectId = readD();
		_recipeId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		L2PcInstance shop = WorldManager.getInstance().getPlayer(_playerObjectId);
		if(shop == null || shop.getPrivateStoreType() != PlayerPrivateStoreType.MANUFACTURE)
		{
			return;
		}

		player.sendPacket(new RecipeShopItemInfo(shop, _recipeId));
	}

	@Override
	public String getType()
	{
		return "[C] B5 RequestRecipeShopMakeInfo";
	}
}
