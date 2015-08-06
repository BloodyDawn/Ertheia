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

import dwo.config.Config;
import dwo.gameserver.datatables.xml.RecipeData;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.L2ManufactureItem;
import dwo.gameserver.model.player.L2ManufactureList;
import dwo.gameserver.model.player.L2RecipeList;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.recipeshop.RecipeShopMsg;
import dwo.gameserver.taskmanager.manager.AttackStanceTaskManager;
import dwo.gameserver.util.Util;

import static dwo.gameserver.model.items.itemcontainer.PcInventory.MAX_ADENA;

public class RequestRecipeShopListSet extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 12; // length of the one item

	private Recipe[] _items;

	@Override
	protected void readImpl()
	{
		int count = readD();
		if(count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}

		_items = new Recipe[count];
		for(int i = 0; i < count; i++)
		{
			int id = readD();
			long cost = readQ();
			if(cost < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new Recipe(id, cost);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(_items == null)
		{
			player.setPrivateStoreType(PlayerPrivateStoreType.NONE);
			player.broadcastUserInfo();
			return;
		}

		if(AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) || player.isInDuel())
		{
			player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
			player.sendActionFailed();
			return;
		}

		if(player.isInsideZone(L2Character.ZONE_NOSTORE))
		{
			player.sendPacket(SystemMessageId.NO_PRIVATE_WORKSHOP_HERE);
			player.sendActionFailed();
			return;
		}

		L2ManufactureList createList = new L2ManufactureList();

		for(Recipe i : _items)
		{
			L2RecipeList list = RecipeData.getInstance().getRecipeList(i.getRecipeId());

			if(!player.getRecipeController().hasRecipe(list))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Player " + player.getName() + " of account " + player.getAccountName() + " tried to set recipe which he dont have.", Config.DEFAULT_PUNISH);
				return;
			}

			if(!i.addToList(createList))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to set price more than " + MAX_ADENA + " adena in Private Manufacture.", Config.DEFAULT_PUNISH);
				return;
			}
		}

		createList.setStoreName(player.getCreateList() != null ? player.getCreateList().getStoreName() : "");
		player.setCreateList(createList);

		player.setPrivateStoreType(PlayerPrivateStoreType.MANUFACTURE);
		player.sitDown();
		player.broadcastUserInfo();
		player.sendPacket(new RecipeShopMsg(player));
		player.broadcastPacket(new RecipeShopMsg(player));
	}

	@Override
	public String getType()
	{
		return "[C] B2 RequestRecipeShopListSet";
	}

	private static class Recipe
	{
		private final int _recipeId;
		private final long _cost;

		public Recipe(int id, long c)
		{
			_recipeId = id;
			_cost = c;
		}

		public boolean addToList(L2ManufactureList list)
		{
			if(_cost > MAX_ADENA)
			{
				return false;
			}

			list.add(new L2ManufactureItem(_recipeId, _cost));
			return true;
		}

		public int getRecipeId()
		{
			return _recipeId;
		}
	}
}
