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
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;

import java.util.StringTokenizer;

public class AdminKick implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_kick", "admin_kick_non_gm"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.startsWith("admin_kick"))
		{
			StringTokenizer st = new StringTokenizer(command);
			if(st.countTokens() > 1)
			{
				st.nextToken();
				String player = st.nextToken();
				L2PcInstance plyr = WorldManager.getInstance().getPlayer(player);
				if(plyr != null)
				{
					plyr.logout();
					activeChar.sendMessage("You kicked " + plyr.getName() + " from the game.");
				}
			}
		}
		if(command.startsWith("admin_kick_non_gm"))
		{
			int counter = 0;
			for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
			{
				if(!player.isGM())
				{
					counter++;
					player.logout();
				}
			}
			activeChar.sendMessage("Kicked " + counter + " players");
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
