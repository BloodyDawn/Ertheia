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
import dwo.gameserver.network.game.serverpackets.packet.ex.ExReceiveOlympiad;

/**
 * Format: (ch)d
 * d: unknown (always 0?)
 *
 * @author mrTJO
 */
public class RequestExOlympiadMatchListRefresh extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		activeChar.sendPacket(new ExReceiveOlympiad());
	}

	@Override
	public String getType()
	{
		return "[C] D0:88 RequestExOlympiadMatchListRefresh";
	}
}