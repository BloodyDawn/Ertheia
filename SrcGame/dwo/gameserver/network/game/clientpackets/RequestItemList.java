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

import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoInvenWeight;

public class RequestItemList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}

	@Override
	protected void runImpl()
	{
		if(getClient() != null && getClient().getActiveChar() != null && !getClient().getActiveChar().isInventoryDisabled())
		{
			ItemList il = new ItemList(getClient().getActiveChar(), true);
			sendPacket(il);
            getClient().getActiveChar().sendPacket(new ExUserInfoInvenWeight(getClient().getActiveChar()));
		}
	}

	@Override
	public String getType()
	{
		return "[C] 0F RequestItemList";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
