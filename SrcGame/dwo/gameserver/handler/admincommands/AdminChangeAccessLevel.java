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

import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.database.DatabaseUtils;

import java.sql.SQLException;

/**
 * This class handles following admin commands:
 * - changelvl = change a character's access level
 * Can be used for character ban (as opposed to regular //ban that affects accounts)
 * or to grant mod/GM privileges ingame
 *
 * @version $Revision: 1.1.2.2.2.3 $ $Date: 2005/04/11 10:06:00 $
 */
public class AdminChangeAccessLevel implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_changelvl"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		handleChangeLevel(command, activeChar);
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	/**
	 * If no character name is specified, tries to change GM's target access
	 * level. Else if a character name is provided, will try to reach it either
	 * from L2World or from a database ThreadConnection.
	 *
	 * @param command
	 * @param activeChar
	 */
	private void handleChangeLevel(String command, L2PcInstance activeChar)
	{
		String[] parts = command.split(" ");
		if(parts.length == 2)
		{
			try
			{
				int lvl = Integer.parseInt(parts[1]);
				if(activeChar.getTarget() instanceof L2PcInstance)
				{
					onLineChange(activeChar, (L2PcInstance) activeChar.getTarget(), lvl);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //changelvl <target_new_level> | <player_name> <new_level>");
			}
		}
		else if(parts.length == 3)
		{
			String name = parts[1];
			int lvl = Integer.parseInt(parts[2]);
			L2PcInstance player = WorldManager.getInstance().getPlayer(name);
			if(player != null)
			{
				onLineChange(activeChar, player, lvl);
			}
			else
			{
				ThreadConnection con = null;
				FiltredPreparedStatement statement = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement(Characters.UPDATE_CHAR_ACCESSLEVEL_NAME);
					statement.setInt(1, lvl);
					statement.setString(2, name);
					statement.execute();
					int count = statement.getUpdateCount();
					if(count == 0)
					{
						activeChar.sendMessage("Character not found or access level unaltered.");
					}
					else
					{
						activeChar.sendMessage("Character's access level is now set to " + lvl);
					}
				}
				catch(SQLException e)
				{
					activeChar.sendMessage("SQLException while changing character's access level");
				}
				finally
				{
					DatabaseUtils.closeDatabaseCS(con, statement);
				}
			}
		}
	}

	/**
	 * @param activeChar
	 * @param player
	 * @param lvl
	 */
	private void onLineChange(L2PcInstance activeChar, L2PcInstance player, int lvl)
	{
		player.setAccessLevel(lvl);
		if(lvl >= 0)
		{
			player.sendMessage("Your access level has been changed to " + lvl);
		}
		else
		{
			player.sendMessage("Your character has been banned. Bye.");
			player.logout();
		}
		activeChar.sendMessage("Character's access level is now set to " + lvl + ". Effects won't be noticeable until next session.");
	}
}
