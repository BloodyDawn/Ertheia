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

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.L2ManufactureList;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.recipeshop.RecipeShopManageList;

public class RequestRecipeShopManageList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if(player.isAlikeDead())
		{
			player.sendActionFailed();
			return;
		}
		if(player.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			player.setPrivateStoreType(PlayerPrivateStoreType.NONE);
			player.broadcastUserInfo();
			if(player.isSitting())
			{
				player.standUp();
			}
		}
		if(player.getCreateList() == null)
		{
			player.setCreateList(new L2ManufactureList());
		}

		player.sendPacket(new RecipeShopManageList(player, true));
		
		/*
		int privatetype=player.getPrivateStoreType();
		if (privatetype == 0)
		{
			if (player.getWaitType() !=1)
			{
				player.setWaitType(1);
				player.sendPacket(new ChangeWaitType (player,1));
				player.broadcastPacket(new ChangeWaitType (player,1));
			}

			if (player.getTradeList() == null)
			{
				player.setTradeList(new L2TradeList(0));
			}
			if (player.getSellList() == null)
			{
				player.setSellList(new ArrayList());
			}
			player.getTradeList().updateSellList(player,player.getSellList());
			player.setPrivateStoreType(2);
			player.sendPacket(new PrivateSellListSell(client.getActiveChar()));
			player.sendPacket(new UserInfo(player));
			player.broadcastPacket(new UserInfo(player));

		}

		if (privatetype == 1)
		{
			player.setPrivateStoreType(2);
			player.sendPacket(new PrivateSellListSell(client.getActiveChar()));
			player.sendPacket(new ChangeWaitType (player,1));
			player.broadcastPacket(new ChangeWaitType (player,1));


		}*/

	}

	@Override
	public String getType()
	{
		return "[C] B0 RequestRecipeShopManageList";
	}
}
