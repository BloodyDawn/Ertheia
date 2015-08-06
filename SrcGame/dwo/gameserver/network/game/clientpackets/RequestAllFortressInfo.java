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

import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowFortressInfo;

/**
 * @author KenM
 */
public class RequestAllFortressInfo extends L2GameClientPacket
{

	@Override
	protected void readImpl()
	{
		// trigger packet
	}

	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		if(client != null)
		{
			client.sendPacket(new ExShowFortressInfo());
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:3D RequestAllFortressInfo";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
