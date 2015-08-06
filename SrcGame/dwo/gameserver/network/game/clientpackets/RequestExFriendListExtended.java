/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.friend.FriendList;

/**
 * @author mrTJO & UnAfraid
 */
public class RequestExFriendListExtended extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger packet
	}

	@Override
	public void runImpl()
	{
		if(!Config.ALLOW_MAIL)
		{
			return;
		}

		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		activeChar.sendPacket(new FriendList(activeChar));
	}

	@Override
	public String getType()
	{
		return "[C] D0:87 RequestExFriendListExtended";
	}
}
