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

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.olympiad.Olympiad;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExReceiveOlympiad;

/**
 * format ch
 * c: (id) 0xD0
 * h: (subid) 0x13
 * @author -Wooden-
 *
 */
public class RequestOlympiadMatchList extends L2GameClientPacket
{

	@Override
	protected void readImpl()
	{
		// trigger packet
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(Olympiad.getInstance().inCompPeriod())
		{
			activeChar.sendPacket(new ExReceiveOlympiad());
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:13 RequestOlympiadMatchList";
	}
}