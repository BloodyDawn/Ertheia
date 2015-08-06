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

import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.PackageSendableList;

/**
 * @author -Wooden-
 * @author UnAfraid
 * Thanks mrTJO
 */
public class RequestPackageSendableItemList extends L2GameClientPacket
{
	private int _objectID;

	@Override
	protected void readImpl()
	{
		_objectID = readD();
	}

	@Override
	public void runImpl()
	{
		L2ItemInstance[] items = getClient().getActiveChar().getInventory().getAvailableItems(true, false, true);
		sendPacket(new PackageSendableList(items, _objectID));
	}

	@Override
	public String getType()
	{
		return "[C] 9E RequestPackageSendableItemList";
	}
}
