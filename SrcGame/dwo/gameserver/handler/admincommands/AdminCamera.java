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
package dwo.gameserver.handler.admincommands;

import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.SpecialCamera;

public class AdminCamera implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_camera"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		try
		{
			L2Character target = (L2Character) activeChar.getTarget();
			String[] com = command.split(" ");

			target.broadcastPacket(new SpecialCamera(target.getObjectId(), Integer.parseInt(com[1]), Integer.parseInt(com[2]), Integer.parseInt(com[3]), Integer.parseInt(com[4]), Integer.parseInt(com[5]), Integer.parseInt(com[6]), Integer.parseInt(com[7]), Integer.parseInt(com[8]), Integer.parseInt(com[9])));
		}
		catch(Exception e)
		{
			activeChar.sendMessage("Usage: //camera dist yaw pitch time duration turn rise widescreen unknown");
			return false;
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}