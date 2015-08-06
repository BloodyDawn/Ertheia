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
import dwo.gameserver.instancemanager.HellboundManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

import java.util.StringTokenizer;

/**
 * @author DS, Gladicek
 */
public class AdminHellbound implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_hellbound_setlevel", "admin_hellbound"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null)
		{
			return false;
		}

		if(command.startsWith(ADMIN_COMMANDS[0])) // setlevel
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int level = Integer.parseInt(st.nextToken());
				if(level < 0 || level > 11)
				{
					throw new NumberFormatException();
				}
				HellboundManager.getInstance().setLevel(level);
				activeChar.sendMessage("Hellbound level set to " + level);
				return true;
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //hellbound_setlevel 0-11");
				return false;
			}
		}
		if(command.startsWith(ADMIN_COMMANDS[1])) // Admin menu by Gladicek
		{
			showMenu(activeChar);
			return true;
		}
		return false;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void showMenu(L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar.getLang(), "mods/admin/hellbound.htm");
		html.replace("%hbstage%", String.valueOf(HellboundManager.getInstance().getLevel()));
		html.replace("%trust%", String.valueOf(HellboundManager.getInstance().getTrust()));
		html.replace("%maxtrust%", String.valueOf(HellboundManager.getInstance().getMaxTrust()));
		html.replace("%mintrust%", String.valueOf(HellboundManager.getInstance().getMinTrust()));
		activeChar.sendPacket(html);
	}
}
