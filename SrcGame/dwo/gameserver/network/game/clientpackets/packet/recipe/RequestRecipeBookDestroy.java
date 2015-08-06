/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.network.game.clientpackets.packet.recipe;

import dwo.gameserver.datatables.xml.RecipeData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.L2RecipeList;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.recipeshop.RecipeBookItemList;

public class RequestRecipeBookDestroy extends L2GameClientPacket
{
	private int _recipeID;

	/**
	 * Unknown Packet:ad
	 * 0000: ad 02 00 00 00
	 */
	@Override
	protected void readImpl()
	{
		_recipeID = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.DESTROY_ITEM))
		{
			return;
		}

		L2RecipeList rp = RecipeData.getInstance().getRecipeList(_recipeID);
		if(rp == null)
		{
			return;
		}
		activeChar.getRecipeController().removeRecipe(_recipeID);

		RecipeBookItemList response = new RecipeBookItemList(rp.isDwarvenRecipe(), activeChar.getMaxMp());
		if(rp.isDwarvenRecipe())
		{
			response.addRecipes(activeChar.getRecipeController().getDwarvenRecipeBook().values());
		}
		else
		{
			response.addRecipes(activeChar.getRecipeController().getCommonRecipeBook().values());
		}

		activeChar.sendPacket(response);
	}

	@Override
	public String getType()
	{
		return "[C] AD RequestRecipeBookDestroy";
	}
}