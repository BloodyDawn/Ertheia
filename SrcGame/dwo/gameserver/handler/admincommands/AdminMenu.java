package dwo.gameserver.handler.admincommands;

import dwo.config.Config;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.components.SystemMessageId;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.StringTokenizer;

public class AdminMenu implements IAdminCommandHandler
{
	private static final Logger _log = LogManager.getLogger(AdminMenu.class);

	private static final String[] ADMIN_COMMANDS = {
		"admin_teleport_character_to_menu", "admin_recall_char_menu", "admin_recall_party_menu",
		"admin_recall_clan_menu", "admin_goto_char_menu", "admin_kick_menu", "admin_kill_menu",
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.startsWith("admin_teleport_character_to_menu"))
		{
			String[] data = command.split(" ");
			if(data.length == 5)
			{
				String playerName = data[1];
				L2PcInstance player = WorldManager.getInstance().getPlayer(playerName);
				if(player != null)
				{
					teleportCharacter(player, Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4]), activeChar, "Admin is teleporting you.");
				}
				showMainPage(activeChar, player);
			}
			showMainPage(activeChar, null);
		}
		else if(command.startsWith("admin_recall_char_menu"))
		{
			try
			{
				String targetName = command.substring(23);
				L2PcInstance player = WorldManager.getInstance().getPlayer(targetName);
				teleportCharacter(player, activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar, "Admin is teleporting you.");
			}
			catch(StringIndexOutOfBoundsException e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
		else if(command.startsWith("admin_recall_party_menu"))
		{
			int x = activeChar.getX();
			int y = activeChar.getY();
			int z = activeChar.getZ();
			try
			{
				String targetName = command.substring(24);
				L2PcInstance player = WorldManager.getInstance().getPlayer(targetName);
				if(player == null)
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return true;
				}
				if(!player.isInParty())
				{
					activeChar.sendMessage("Player is not in party.");
					teleportCharacter(player, x, y, z, activeChar, "Admin is teleporting you.");
					return true;
				}
				for(L2PcInstance pm : player.getParty().getMembers())
				{
					teleportCharacter(pm, x, y, z, activeChar, "Your party is being teleported by an Admin.");
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
		else if(command.startsWith("admin_recall_clan_menu"))
		{
			int x = activeChar.getX();
			int y = activeChar.getY();
			int z = activeChar.getZ();
			try
			{
				String targetName = command.substring(23);
				L2PcInstance player = WorldManager.getInstance().getPlayer(targetName);
				if(player == null)
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return true;
				}
				L2Clan clan = player.getClan();
				if(clan == null)
				{
					activeChar.sendMessage("Player is not in a clan.");
					teleportCharacter(player, x, y, z, activeChar, "Admin is teleporting you.");
					return true;
				}
				for(L2PcInstance member : clan.getOnlineMembers(0))
				{
					teleportCharacter(member, x, y, z, activeChar, "Your clan is being teleported by an Admin.");
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
		else if(command.startsWith("admin_goto_char_menu"))
		{
			try
            {
                String targetName = command.substring(20);
                L2PcInstance player = WorldManager.getInstance().getPlayer(targetName);
                if (player == null) {
                    activeChar.sendMessage("Такого игрока не существует.");
                }
                else
                {
                    activeChar.getInstanceController().setInstanceId(player.getInstanceId());
                    teleportToCharacter(activeChar, player);
                }
            }
            catch (StringIndexOutOfBoundsException e)
            {
                _log.error("", e);
            }
		}
		else if(command.equals("admin_kill_menu"))
		{
			handleKill(activeChar);
		}
		else if(command.startsWith("admin_kick_menu"))
		{
			StringTokenizer st = new StringTokenizer(command);
			if(st.countTokens() > 1)
			{
				st.nextToken();
				String player = st.nextToken();
				L2PcInstance plyr = WorldManager.getInstance().getPlayer(player);
				String text;
				if(plyr != null)
				{
					plyr.logout();
					text = "You kicked " + plyr.getName() + " from the game.";
				}
				else
				{
					text = "Player " + player + " was not found in the game.";
				}
				activeChar.sendMessage(text);
				showMainPage(activeChar, plyr);
			}
			else
			{
				showMainPage(activeChar, null);
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void handleKill(L2PcInstance activeChar)
	{
		handleKill(activeChar, null);
	}

	private void handleKill(L2PcInstance activeChar, String player)
	{
		L2Object obj = activeChar.getTarget();
		L2Character target = (L2Character) obj;
		if(player != null)
		{
			L2PcInstance plyr = WorldManager.getInstance().getPlayer(player);
			if(plyr != null)
			{
				target = plyr;
			}
			activeChar.sendMessage("You killed " + plyr.getName());
		}
		if(target != null)
		{
			if(target instanceof L2PcInstance)
			{
				target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar, null);
				showMainPage(activeChar, (L2PcInstance) target);
				return;
			}
			else if(Config.CHAMPION_ENABLE && target.isChampion())
			{
				target.reduceCurrentHp(target.getMaxHp() * Config.CHAMPION_HP + 1, activeChar, null);
			}
			else
			{
				target.reduceCurrentHp(target.getMaxHp() + 1, activeChar, null);
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
		AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
	}

	private void teleportCharacter(L2PcInstance player, int x, int y, int z, L2PcInstance activeChar, String message)
	{
		if(player != null)
		{
			player.sendMessage(message);
			player.teleToLocation(x, y, z, true);
		}
		showMainPage(activeChar, player);
	}

	private void teleportToCharacter(L2PcInstance activeChar, L2Object target)
	{
		L2PcInstance player = null;
		if(target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		if(player.getObjectId() == activeChar.getObjectId())
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
		}
		else
		{
			activeChar.getInstanceController().setInstanceId(player.getInstanceId());
			activeChar.teleToLocation(player.getX(), player.getY(), player.getZ(), true);
			activeChar.sendMessage("You're teleporting yourself to character " + player.getName());
		}
		showMainPage(activeChar, player);
	}

	/**
	 * @param activeChar
	 */
	private void showMainPage(L2PcInstance activeChar, L2PcInstance player)
	{
		if(player != null)
		{
			AdminEditChar.gatherCharacterInfo(activeChar, player, "charinfo.htm");
		}
		else
		{
			AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
		}
	}
}
