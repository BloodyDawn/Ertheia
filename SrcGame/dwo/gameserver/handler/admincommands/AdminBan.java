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

import dwo.config.Config;
import dwo.gameserver.Announcements;
import dwo.gameserver.LoginServerThread;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerPunishLevel;
import dwo.gameserver.model.world.communitybbs.Manager.RegionBBSManager;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.database.DatabaseUtils;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.StringTokenizer;

/**
 * This class handles following admin commands:
 * - ban_acc <account_name> = changes account access level to -100 and logs him off. If no account is specified target's account is used.
 * - ban_char <char_name> = changes a characters access level to -100 and logs him off. If no character is specified target is used.
 * - ban_chat <char_name> <duration> = chat bans a character for the specified duration. If no name is specified the target is chat banned indefinitely.
 * - unban_acc <account_name> = changes account access level to 0.
 * - unban_char <char_name> = changes specified characters access level to 0.
 * - unban_chat <char_name> = lifts chat ban from specified player. If no player name is specified current target is used.
 * - jail charname [penalty_time] = jails character. Time specified in minutes. For ever if no time is specified.
 * - unjail charname = Unjails player, teleport him to Floran.
 *
 * @version $Revision: 1.1.6.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminBan implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_ban", // returns ban commands
		"admin_ban_acc", "admin_ban_char", "admin_ban_chat", "admin_unban", // returns unban commands
		"admin_unban_acc", "admin_unban_char", "admin_unban_chat", "admin_jail", "admin_unjail", "admin_ban_ip",
		"admin_unban_ip"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		String player = "";
		int duration = -1;
		L2PcInstance targetPlayer = null;

		if(st.hasMoreTokens())
		{
			player = st.nextToken();
			targetPlayer = WorldManager.getInstance().getPlayer(player);

			if(st.hasMoreTokens())
			{
				try
				{
					duration = Integer.parseInt(st.nextToken());
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage("Invalid number format used: " + nfe);
					return false;
				}
			}
		}
		else
		{
			if(activeChar.getTarget() != null && activeChar.getTarget() instanceof L2PcInstance)
			{
				targetPlayer = (L2PcInstance) activeChar.getTarget();
			}
		}

		if(targetPlayer != null && targetPlayer.equals(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
			return false;
		}

		if(command.startsWith("admin_ban ") || command.equalsIgnoreCase("admin_ban"))
		{
			activeChar.sendMessage("Available ban commands: //ban_acc, //ban_char, //ban_chat");
			return false;
		}
		if(command.startsWith("admin_ban_acc"))
		{
			// May need to check usage in admin_ban_menu as well.

			if(targetPlayer == null && player.isEmpty())
			{
				activeChar.sendMessage("Usage: //ban_acc <account_name> (if none, target char's account gets banned)");
				return false;
			}
			else if(targetPlayer == null)
			{
				LoginServerThread.getInstance().sendAccessLevel(player, -100);
				activeChar.sendMessage("Ban request sent for account " + player);
				if(Config.ANNOUNCE_PUNISHMENTS)
				{
					Announcements.getInstance().announceToAll("ГМ " + activeChar.getName() + " забанил аккаунт игроку " + player);
				}
			}
			else
			{
				targetPlayer.setPunishLevel(PlayerPunishLevel.ACC, 0);
				activeChar.sendMessage("Account " + targetPlayer.getAccountName() + " banned.");
				if(Config.ANNOUNCE_PUNISHMENTS)
				{
					Announcements.getInstance().announceToAll("ГМ " + activeChar.getName() + " забанил акаунт игроку " + targetPlayer.getName());
				}
			}
		}
		else if(command.startsWith("admin_ban_char"))
		{
			if(targetPlayer == null && player.isEmpty())
			{
				activeChar.sendMessage("Usage: //ban_char <char_name> (if none, target char is banned)");
				return false;
			}
			else
			{
				if(Config.ANNOUNCE_PUNISHMENTS)
				{
					Announcements.getInstance().announceToAll("ГМ " + activeChar.getName() + " забанил персонажа " + targetPlayer.getName());
				}
				return changeCharAccessLevel(targetPlayer, player, activeChar, -100);
			}
		}
		else if(command.startsWith("admin_ban_chat"))
		{
			if(targetPlayer == null && player.isEmpty())
			{
				activeChar.sendMessage("Usage: //ban_chat <char_name> [penalty_minutes]");
				return false;
			}
			if(targetPlayer != null)
			{
				if(targetPlayer.getPunishLevel().value() > 0)
				{
					activeChar.sendMessage(targetPlayer.getName() + " is already jailed or banned.");
					return false;
				}
				String banLengthStr = "";

				targetPlayer.setPunishLevel(PlayerPunishLevel.CHAT, duration);
				if(duration > 0)
				{
					banLengthStr = " for " + duration + " minutes";
				}
				activeChar.sendMessage(targetPlayer.getName() + " is now chat banned" + banLengthStr + '.');
				if(Config.ANNOUNCE_PUNISHMENTS)
				{
					Announcements.getInstance().announceToAll("ГМ " + activeChar.getName() + " забанил чат игроку " + targetPlayer.getName() + " на " + duration + " минут(у).");
				}
			}
			else
			{
				banChatOfflinePlayer(activeChar, player, duration, true);
			}
		}
		else if(command.startsWith("admin_unban_chat"))
		{
			if(targetPlayer == null && player.isEmpty())
			{
				activeChar.sendMessage("Usage: //unban_chat <char_name>");
				return false;
			}
			if(targetPlayer != null)
			{
				if(targetPlayer.isChatBanned())
				{
					targetPlayer.setPunishLevel(PlayerPunishLevel.NONE, 0);
					activeChar.sendMessage(targetPlayer.getName() + "'s chat ban has now been lifted.");
				}
				else
				{
					activeChar.sendMessage(targetPlayer.getName() + " is not currently chat banned.");
				}
			}
			else
			{
				banChatOfflinePlayer(activeChar, player, 0, false);
			}
		}
		else if(command.startsWith("admin_unban ") || command.equalsIgnoreCase("admin_unban"))
		{
			activeChar.sendMessage("Available unban commands: //unban_acc, //unban_char, //unban_chat");
			return false;
		}
		else if(command.startsWith("admin_unban_acc"))
		{
			// Need to check admin_unban_menu command as well in AdminMenu.java handler.

			if(targetPlayer != null)
			{
				activeChar.sendMessage(targetPlayer.getName() + " is currently online so must not be banned.");
				return false;
			}
			else if(!player.isEmpty())
			{
				LoginServerThread.getInstance().sendAccessLevel(player, 0);
				activeChar.sendMessage("Unban request sent for account " + player);
			}
			else
			{
				activeChar.sendMessage("Usage: //unban_acc <account_name>");
				return false;
			}
		}
		else if(command.startsWith("admin_unban_char"))
		{
			if(targetPlayer == null && player.isEmpty())
			{
				activeChar.sendMessage("Usage: //unban_char <char_name>");
				return false;
			}
			else if(targetPlayer != null)
			{
				activeChar.sendMessage(targetPlayer.getName() + " is currently online so must not be banned.");
				return false;
			}
			else
			{
				return changeCharAccessLevel(null, player, activeChar, 0);
			}
		}
		else if(command.startsWith("admin_jail"))
		{
			if(targetPlayer == null && player.isEmpty())
			{
				activeChar.sendMessage("Usage: //jail <charname> [penalty_minutes] (if no name is given, selected target is jailed indefinitely)");
				return false;
			}
			if(targetPlayer != null)
			{
				if(targetPlayer.isFlyingMounted())
				{
					targetPlayer.untransform(true);
				}
				targetPlayer.setPunishLevel(PlayerPunishLevel.JAIL, duration);
				activeChar.sendMessage("Character " + targetPlayer.getName() + " jailed for " + (duration > 0 ? duration + " minutes." : "ever!"));
				if(Config.ANNOUNCE_PUNISHMENTS)
				{
					Announcements.getInstance().announceToAll("ГМ " + activeChar.getName() + " посадил в тюрьму " + targetPlayer.getName() + '.');
				}
			}
			else
			{
				jailOfflinePlayer(activeChar, player, duration);
				if(Config.ANNOUNCE_PUNISHMENTS)
				{
					Announcements.getInstance().announceToAll("ГМ " + activeChar.getName() + " посадил в тюрьму " + player);
				}
			}
		}
		else if (command.startsWith("admin_unjail"))
		{
			if ((targetPlayer == null) && player.isEmpty())
			{
				activeChar.sendMessage("Usage: //unjail <charname> (If no name is given target is used)");
				return false;
			}
			else if (targetPlayer != null)
			{
				targetPlayer.setPunishLevel(PlayerPunishLevel.NONE, 0);
				activeChar.sendMessage("Character " + targetPlayer.getName() + " removed from jail");
			}
			else
			{
				unjailOfflinePlayer(activeChar, player);
			}
		}
		else if(command.startsWith("admin_ban_ip"))
		{
			if(player.isEmpty())
			{
				activeChar.sendMessage("Usage: //ban_ip <charname>");
				return false;
			}
			else
			{
				LoginServerThread.getInstance().sendAccessLevel(player, -100);
				L2PcInstance BanPlayer = WorldManager.getInstance().getPlayer(player);
				if(BanPlayer != null)
				{
					LoginServerThread.getInstance().sendBlockAddress(BanPlayer.getClient().getConnection().getInetAddress().getHostAddress(), 0L);
					activeChar.sendMessage("Ban request sent for account/ip " + player);
					if(Config.ANNOUNCE_PUNISHMENTS)
					{
						Announcements.getInstance().announceToAll("ГМ " + activeChar.getName() + " забанил по ip игрока " + player);
					}
				}
				else
				{
					activeChar.sendMessage("Не найден игрок с именем: " + player);
				}
			}
		}
		else if(command.startsWith("admin_unban_ip"))
		{
			if(player.isEmpty())
			{
				activeChar.sendMessage("Usage: //ban_ip <charname>");
				return false;
			}
			else
			{
				if(st.hasMoreTokens())
				{
					String address = st.nextToken();
					try
					{
						InetAddress.getByName(address);
						LoginServerThread.getInstance().sendUnblockAddress(address);
					}
					catch(Exception e)
					{
						activeChar.sendMessage("Usage mode: //unban_ip <ip>");
					}
				}
				else
				{
					activeChar.sendMessage("Usage mode: //unban_ip <ip>");
				}
			}
		}
		else if(command.startsWith("admin_ban_chat"))
		{
			if(targetPlayer == null && player.isEmpty())
			{
				activeChar.sendMessage("Usage: //ban_chat <char_name> [penalty_minutes]");
				return false;
			}
			if(targetPlayer != null)
			{
				if(targetPlayer.getPunishLevel().value() > 0)
				{
					activeChar.sendMessage(targetPlayer.getName() + " is already jailed or banned.");
					return false;
				}
				String banLengthStr = "";

				targetPlayer.setPunishLevel(PlayerPunishLevel.CHAT, duration);
				if(duration > 0)
				{
					banLengthStr = " for " + duration + " minutes";
				}
				activeChar.sendMessage(targetPlayer.getName() + " is now chat banned" + banLengthStr + '.');
				if(Config.ANNOUNCE_PUNISHMENTS)
				{
					Announcements.getInstance().announceToAll("ГМ " + activeChar.getName() + " забанил чат игроку " + targetPlayer.getName() + " на " + duration + " минут(у).");
				}
			}
			else
			{
				banChatOfflinePlayer(activeChar, player, duration, true);
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void banChatOfflinePlayer(L2PcInstance activeChar, String name, int delay, boolean ban)
	{
		int level = 0;
		long value = 0L;
		if(ban)
		{
			level = PlayerPunishLevel.CHAT.value();
			value = delay > 0 ? delay * 60000L : 60000L;
		}
		else
		{
			level = PlayerPunishLevel.NONE.value();
			value = 0L;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Characters.UPDATE_CHAR_NAME);
			statement.setInt(1, level);
			statement.setLong(2, value);
			statement.setString(3, name);

			statement.execute();
			int count = statement.getUpdateCount();

			if(count == 0)
			{
				activeChar.sendMessage("Character not found!");
			}
			else if(ban)
			{
				activeChar.sendMessage("Character " + name + " chat-banned for " + (delay > 0 ? delay + " minutes." : "ever!"));
			}
			else
			{
				activeChar.sendMessage("Character " + name + "'s chat-banned lifted");
			}
		}
		catch(SQLException e)
		{
			activeChar.sendMessage("SQLException while chat-banning player");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void jailOfflinePlayer(L2PcInstance activeChar, String name, int delay)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Characters.UPDATE_CHAR_X_Y_Z_NAME);
			statement.setInt(1, -114356);
			statement.setInt(2, -249645);
			statement.setInt(3, -2984);
			statement.setInt(4, PlayerPunishLevel.JAIL.value());
			statement.setLong(5, delay > 0 ? delay * 60000L : 0L);
			statement.setString(6, name);

			statement.execute();
			int count = statement.getUpdateCount();

			if(count == 0)
			{
				activeChar.sendMessage("Character not found!");
			}
			else
			{
				activeChar.sendMessage("Character " + name + " jailed for " + (delay > 0 ? delay + " minutes." : "ever!"));
			}
		}
		catch(SQLException e)
		{
			activeChar.sendMessage("SQLException while jailing player");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void unjailOfflinePlayer(L2PcInstance activeChar, String name)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.UPDATE_CHAR_X_Y_Z_NAME);
			statement.setInt(1, 17836);
			statement.setInt(2, 170178);
			statement.setInt(3, -3507);
			statement.setInt(4, 0);
			statement.setLong(5, 0L);
			statement.setString(6, name);
			statement.execute();
			int count = statement.getUpdateCount();
			if(count == 0)
			{
				activeChar.sendMessage("Character not found!");
			}
			else
			{
				activeChar.sendMessage("Character " + name + " removed from jail");
			}
		}
		catch(SQLException e)
		{
			activeChar.sendMessage("SQLException while jailing player");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private boolean changeCharAccessLevel(L2PcInstance targetPlayer, String player, L2PcInstance activeChar, int lvl)
	{
		if(targetPlayer != null)
		{
			targetPlayer.setAccessLevel(lvl);
			targetPlayer.sendMessage("Your character has been banned. Goodbye.");
			targetPlayer.logout();
			RegionBBSManager.getInstance().changeCommunityBoard();
			activeChar.sendMessage("The character " + targetPlayer.getName() + " has now been banned.");
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
				statement.setString(2, player);
				statement.execute();
				int count = statement.getUpdateCount();
				if(count == 0)
				{
					activeChar.sendMessage("Character not found or access level unaltered.");
					return false;
				}
				else
				{
					activeChar.sendMessage(player + " now has an access level of " + lvl);
				}
			}
			catch(SQLException e)
			{
				activeChar.sendMessage("SQLException while changing character's access level");
				return false;
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
		return true;
	}
}