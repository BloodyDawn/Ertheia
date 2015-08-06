package dwo.gameserver.network.game.clientpackets.packet.recipe;

import dwo.gameserver.datatables.xml.RecipeData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;

public class RequestRecipeItemMakeSelf extends L2GameClientPacket
{
	private int _id;

	@Override
	protected void readImpl()
	{
		_id = readD();
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

		if(activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			activeChar.sendMessage("Нельзя создавать предметы во время торговли.");
			return;
		}

		if(activeChar.isInCraftMode())
		{
			activeChar.sendMessage("Вы уже находитесь в режиме создания вещей.");
			return;
		}

		RecipeData.getInstance().requestMakeItem(activeChar, _id);
	}

	@Override
	public String getType()
	{
		return "[C] AF RequestRecipeItemMakeSelf";
	}
}
