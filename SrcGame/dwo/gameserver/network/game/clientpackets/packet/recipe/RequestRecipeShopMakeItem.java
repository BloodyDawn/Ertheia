package dwo.gameserver.network.game.clientpackets.packet.recipe;

import dwo.gameserver.datatables.xml.RecipeData;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.util.Util;

public class RequestRecipeShopMakeItem extends L2GameClientPacket
{
	private int _id;
	private int _recipeId;
	private long _unknow;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_recipeId = readD();
		_unknow = readQ();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getManufacture().tryPerformAction(FloodAction.RECIPE_CREATE))
		{
			return;
		}

		L2PcInstance manufacturer = WorldManager.getInstance().getPlayer(_id);
		if(manufacturer == null)
		{
			return;
		}

		if(manufacturer.getInstanceId() != activeChar.getInstanceId() && activeChar.getInstanceId() != -1)
		{
			return;
		}

		if(activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			activeChar.sendMessage("Нельзя создавать вещи во время торговли.");
			return;
		}
		if(manufacturer.getPrivateStoreType() != PlayerPrivateStoreType.MANUFACTURE)
		{
			//activeChar.sendMessage("Cannot make items while trading");
			return;
		}

		if(activeChar.isInCraftMode() || manufacturer.isInCraftMode())
		{
			activeChar.sendMessage("Вы уже находитесь в режиме создания вещей");
			return;
		}
		if(Util.checkIfInRange(150, activeChar, manufacturer, true))
		{
			RecipeData.getInstance().requestManufactureItem(manufacturer, _recipeId, activeChar);
		}
	}

	@Override
	public String getType()
	{
		return "[C] B6 RequestRecipeShopMakeItem";
	}
}
