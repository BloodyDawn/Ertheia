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
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class AdminUnblockIp implements IAdminCommandHandler
{

	private static final Logger _log = LogManager.getLogger(AdminUnblockIp.class);

	private static final String[] ADMIN_COMMANDS = {
		"admin_unblockip"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.startsWith("admin_unblockip "))
		{
			try
			{
				String ipAddress = command.substring(16);
				if(unblockIp(ipAddress, activeChar))
				{
					activeChar.sendMessage("Removed IP " + ipAddress + " from blocklist!");
				}
			}
			catch(StringIndexOutOfBoundsException e)
			{
				// Send syntax to the user
				activeChar.sendMessage("Usage mode: //unblockip <ip>");
			}
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private boolean unblockIp(String ipAddress, L2PcInstance activeChar)
	{
		//LoginServerThread.getInstance().unBlockip(ipAddress);
		_log.log(Level.INFO, "IP removed by GM " + activeChar.getName());
		return true;
	}
}
