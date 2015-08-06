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
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.Location;
import dwo.scripts.npc.town.MammonsMoving;

/**
 * Admin Command Handler for Mammon NPCs
 *
 * @author Tempy
 */
public class AdminMammon implements IAdminCommandHandler
{

	private static final String[] ADMIN_COMMANDS = {
		"admin_mammon_find", "admin_mammon_to_other_town"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(command.equalsIgnoreCase("admin_mammon_find"))
		{
			Location loc = MammonsMoving.getMammonCoords();
			activeChar.teleToLocation(loc);
		}
		else if(command.equalsIgnoreCase("admin_mammon_to_other_town"))
		{
			MammonsMoving.toOtherTown();
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
