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
package dwo.gameserver.handler.voiced;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.datatables.xml.AdminTable;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerPunishLevel;

import java.util.List;

/**
 * Chat ban functions handler.
 *
 * @author L2J
 * @author Yorie
 */
public class ChatAdmin extends CommandHandler<String>
{
	@TextCommand
	public boolean banChat(HandlerParams<String> params)
	{
		L2PcInstance activeChar = params.getPlayer();
		List<String> adParams = params.getArgs();

		if(!AdminTable.getInstance().hasAccess(params.getCommand(), activeChar.getAccessLevel()))
		{
			return false;
		}

		if(adParams.size() < 1)
		{
			activeChar.sendMessage("Usage: .banchat name [minutes]");
			return true;
		}

		String name;
		int time;
		try
		{
			name = adParams.get(0);
			time = adParams.size() > 1 ? Math.max(0, Integer.parseInt(adParams.get(1))) : 0;
		}
		catch(NumberFormatException e)
		{
			activeChar.sendMessage("Invalid ban time!");
			return false;
		}

		int objId = CharNameTable.getInstance().getIdByName(name);
		if(objId > 0)
		{
			L2PcInstance player = WorldManager.getInstance().getPlayer(objId);
			if(player == null || !player.isOnline())
			{
				activeChar.sendMessage("Player not online!");
				return false;
			}

			if(player.getPunishLevel() != PlayerPunishLevel.NONE)
			{
				activeChar.sendMessage("Player is already punished!");
				return false;
			}

			if(player.equals(activeChar))
			{
				activeChar.sendMessage("You can't ban yourself!");
				return false;
			}

			if(player.isGM())
			{
				activeChar.sendMessage("You can't ban GM!");
				return false;
			}

			if(AdminTable.getInstance().hasAccess(params.getCommand(), player.getAccessLevel()))
			{
				activeChar.sendMessage("You can't ban moderator!");
				return false;
			}

			player.setPunishLevel(PlayerPunishLevel.CHAT, time);
			player.sendMessage("Chat banned by moderator " + activeChar.getName());

			if(time > 0)
			{
				activeChar.sendMessage("Player " + player.getName() + " chat banned for " + time + " minutes.");
			}
			else
			{
				activeChar.sendMessage("Player " + player.getName() + " chat banned forever.");
			}
		}
		else
		{
			activeChar.sendMessage("Player not found!");
			return false;
		}

		return true;
	}

	@TextCommand
	public boolean unBanChat(HandlerParams<String> params)
	{
		L2PcInstance activeChar = params.getPlayer();
		List<String> adParams = params.getArgs();

		if(adParams.isEmpty())
		{
			activeChar.sendMessage("Usage: .unbanchat name");
			return true;
		}

		String name = adParams.get(0);

		int objId = CharNameTable.getInstance().getIdByName(name);
		if(objId > 0)
		{
			L2PcInstance player = WorldManager.getInstance().getPlayer(objId);
			if(player == null || !player.isOnline())
			{
				activeChar.sendMessage("Player is not online!");
				return false;
			}

			if(player.getPunishLevel() != PlayerPunishLevel.CHAT)
			{
				activeChar.sendMessage("Player is not chat banned!");
				return false;
			}

			player.setPunishLevel(PlayerPunishLevel.NONE, 0);

			activeChar.sendMessage("Player " + player.getName() + " chat unbanned.");
			player.sendMessage("Chat unbanned by moderator " + activeChar.getName());
		}
		else
		{
			activeChar.sendMessage("Player not found !");
			return false;
		}

		return true;
	}

	@Override
	public boolean isActive()
	{
		return Config.CHAT_ADMIN;
	}
}
