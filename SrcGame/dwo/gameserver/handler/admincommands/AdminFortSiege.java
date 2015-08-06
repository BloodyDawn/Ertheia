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
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.util.StringUtil;

import java.util.List;
import java.util.StringTokenizer;

/**
 * This class handles all siege commands:
 * Todo: change the class name, and neaten it up
 */
public class AdminFortSiege implements IAdminCommandHandler
{
	//private static Logger _log = LogManager.getLogger(AdminFortSiege.class);

	private static final String[] ADMIN_COMMANDS = {
		"admin_fortsiege", "admin_add_fortattacker", "admin_list_fortsiege_clans", "admin_clear_fortsiege_list",
		"admin_spawn_fortdoors", "admin_endfortsiege", "admin_startfortsiege", "admin_setfort", "admin_removefort"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken(); // Get actual command

		// Get fort
		Fort fort = null;
		int fortId = 0;
		if(st.hasMoreTokens())
		{
			fortId = Integer.parseInt(st.nextToken());
			fort = FortManager.getInstance().getFortById(fortId);
		}
		// Get fort
		if(fort == null || fortId == 0)
		// No fort specified
		{
			showFortSelectPage(activeChar);
		}
		else
		{
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			if(target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}

			if(command.equalsIgnoreCase("admin_add_fortattacker"))
			{
				if(player == null)
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				}
				else
				{
					if(fort.getSiege().checkIfCanRegister(player))
					{
						fort.getSiege().registerAttacker(player, true);
					}
				}
			}
			else if(command.equalsIgnoreCase("admin_clear_fortsiege_list"))
			{
				fort.getSiege().clearSiegeClan();
			}
			else if(command.equalsIgnoreCase("admin_endfortsiege"))
			{
				fort.getSiege().endSiege();
			}
			else if(command.equalsIgnoreCase("admin_list_fortsiege_clans"))
			{
				activeChar.sendMessage("Not implemented yet.");
			}
			else if(command.equalsIgnoreCase("admin_setfort"))
			{
				if(player == null || player.getClan() == null)
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				}
				else
				{
					fort.setOwner(player.getClan(), false);
				}
			}
			else if(command.equalsIgnoreCase("admin_removefort"))
			{
				L2Clan clan = fort.getOwnerClan();
				if(clan != null)
				{
					fort.removeOwner();
				}
				else
				{
					activeChar.sendMessage("Unable to remove fort");
				}
			}
			else if(command.equalsIgnoreCase("admin_spawn_fortdoors"))
			{
				fort.resetDoors();
			}
			else if(command.equalsIgnoreCase("admin_startfortsiege"))
			{
				fort.getSiege().startSiege();
			}

			showFortSiegePage(activeChar, fort);
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void showFortSelectPage(L2PcInstance activeChar)
	{
		int i = 0;
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "mods/admin/forts.htm");

		List<Fort> forts = FortManager.getInstance().getForts();
		StringBuilder cList = new StringBuilder(forts.size() * 100);

		for(Fort fort : forts)
		{
			if(fort != null)
			{
				StringUtil.append(cList, "<td fixwidth=90><a action=\"bypass -h admin_fortsiege ", String.valueOf(fort.getFortId()), "\">", fort.getName(), " id: ", String.valueOf(fort.getFortId()), "</a></td>");
				i++;
			}

			if(i > 2)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}

		adminReply.replace("%forts%", cList.toString());
		activeChar.sendPacket(adminReply);
	}

	private void showFortSiegePage(L2PcInstance activeChar, Fort fort)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "mods/admin/fort.htm");
		adminReply.replace("%fortName%", fort.getName());
		adminReply.replace("%fortId%", String.valueOf(fort.getFortId()));
		activeChar.sendPacket(adminReply);
	}
}
