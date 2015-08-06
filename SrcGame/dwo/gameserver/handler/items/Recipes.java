package dwo.gameserver.handler.items;

import dwo.gameserver.datatables.xml.RecipeData;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.L2RecipeList;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class Recipes implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance activeChar = playable.getActingPlayer();

		if(activeChar.isInCraftMode())
		{
			activeChar.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
			return false;
		}

		L2RecipeList rp = RecipeData.getInstance().getRecipeByItemId(item.getItemId());
		if(rp == null)
		{
			activeChar.sendMessage("Нет данных для рецепта! Обратитесь к Администрации!");
			return false;
		}

		if(activeChar.getRecipeController().hasRecipe(rp.getId()))
		{
			activeChar.sendPacket(SystemMessageId.RECIPE_ALREADY_REGISTERED);
			return false;
		}

		boolean canCraft = false;
		boolean recipeLevel = false;
		boolean recipeLimit = false;
		if(rp.isDwarvenRecipe())
		{
			canCraft = activeChar.hasDwarvenCraft();
			recipeLevel = rp.getLevel() > activeChar.getDwarvenCraft();
			recipeLimit = activeChar.getRecipeController().getDwarvenRecipeBook().size() >= activeChar.getRecipeController().getDwarvenRecipeLimit();
		}
		else
		{
			canCraft = activeChar.hasCommonCraft();
			recipeLevel = rp.getLevel() > activeChar.getCommonCraft();
			recipeLimit = activeChar.getRecipeController().getCommonRecipeBook().size() >= activeChar.getRecipeController().getCommonRecipeLimit();
		}

		if(!canCraft)
		{
			activeChar.sendPacket(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT);
			return false;
		}

		if(recipeLevel)
		{
			activeChar.sendPacket(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER);
			return false;
		}

		if(recipeLimit)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER).addNumber(rp.isDwarvenRecipe() ? activeChar.getRecipeController().getDwarvenRecipeLimit() : activeChar.getRecipeController().getCommonRecipeLimit()));
			return false;
		}

		activeChar.getRecipeController().registerRecipe(rp, true);

		activeChar.destroyItem(ProcessType.RECIPE, item.getObjectId(), 1, null, false);
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ADDED).addItemName(item));
		return true;
	}
}
