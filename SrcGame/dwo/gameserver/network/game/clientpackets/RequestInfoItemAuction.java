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
package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.ItemAuctionManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemauction.ItemAuction;
import dwo.gameserver.model.items.itemauction.ItemAuctionInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExItemAuctionInfo;

public class RequestInfoItemAuction extends L2GameClientPacket
{
	private int _instanceId;

	@Override
	protected void readImpl()
	{
		_instanceId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getItemAuction().tryPerformAction(FloodAction.ITEM_AUCTION_SHOW))
		{
			return;
		}

		ItemAuctionInstance instance = ItemAuctionManager.getInstance().getManagerInstance(_instanceId);
		if(instance == null)
		{
			return;
		}

		ItemAuction auction = instance.getCurrentAuction();
		if(auction == null)
		{
			return;
		}

		activeChar.updateLastItemAuctionRequest();
		activeChar.sendPacket(new ExItemAuctionInfo(true, auction, instance.getNextAuction()));
	}

	@Override
	public String getType()
	{
		return "[C] D0:3A RequestBidItemAuction";
	}
}