package dwo.gameserver.handler.admincommands;

import dwo.config.Config;
import dwo.config.events.ConfigEvents;
import dwo.config.main.ConfigCharacter;
import dwo.config.main.ConfigFeature;
import dwo.config.main.ConfigFloodProtector;
import dwo.config.main.ConfigGeneral;
import dwo.config.main.ConfigGrandBoss;
import dwo.config.main.ConfigIDFactory;
import dwo.config.main.ConfigMMO;
import dwo.config.main.ConfigNPC;
import dwo.config.main.ConfigOlympiad;
import dwo.config.main.ConfigPvP;
import dwo.config.main.ConfigRates;
import dwo.config.main.ConfigSiege;
import dwo.config.mods.ConfigBanking;
import dwo.config.mods.ConfigChampion;
import dwo.config.mods.ConfigChars;
import dwo.config.mods.ConfigChat;
import dwo.config.mods.ConfigCustom;
import dwo.config.mods.ConfigGraciaSeeds;
import dwo.config.mods.ConfigOfflineTrade;
import dwo.config.mods.ConfigWedding;
import dwo.config.network.ConfigCommunityServer;
import dwo.config.network.ConfigGameServer;
import dwo.config.network.ConfigHexid;
import dwo.config.network.ConfigIPConfig;
import dwo.config.security.ConfigProtectionAdmin;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.xml.*;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class AdminReload implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_reload", "admin_reload_config"
	};
	private static Logger _log = LogManager.getLogger(AdminReload.class);

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		String[] cmd = command.split(" ");
		if(cmd[0].equals("admin_reload_config"))
		{
			if(cmd.length == 2)
			{
				String op = cmd[1];
				try
				{
					if(op.equals("all"))
					{
						Config.loadAll();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All configs reloaded!");
					}
					else if(op.equals("all_main"))
					{
						Config.loadMainConfigs();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Main configs reloaded!");
					}
					else if(op.equals("all_mods"))
					{
						Config.loadModsConfigs();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Mods configs reloaded!");
					}
					else if(op.equals("all_network"))
					{
						Config.loadNetworkConfigs();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Network configs reloaded!");
					}
					else if(op.equals("all_scripts"))
					{
						Config.loadScriptConfigs();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Scripts configs reloaded!");
					}
					else if(op.equals("all_security"))
					{
						Config.loadSecurityConfigs();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Security configs reloaded!");
					}
					else if(op.equalsIgnoreCase("Events"))
					{
						ConfigEvents.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Events configs reloaded!");
					}
					// Main Folder
					else if(op.equalsIgnoreCase("Character"))
					{
						ConfigCharacter.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Character configs reloaded!");
					}
					else if(op.equalsIgnoreCase("ChatFilter"))
					{
						ObsceneFilterTable.getInstance().load();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All ChatFilter configs reloaded!");
					}
					else if(op.equalsIgnoreCase("Feature"))
					{
						ConfigFeature.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Feature configs reloaded!");
					}
					else if(op.equalsIgnoreCase("FloodProtector"))
					{
						ConfigFloodProtector.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All FloodProtector configs reloaded!");
					}
					else if(op.equalsIgnoreCase("General"))
					{
						ConfigGeneral.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All General configs reloaded!");
					}
					else if(op.equalsIgnoreCase("GrandBoss"))
					{
						ConfigGrandBoss.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All GrandBoss configs reloaded!");
					}
					else if(op.equalsIgnoreCase("IDFactory"))
					{
						ConfigIDFactory.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All IDFactory configs reloaded!");
					}
					else if(op.equalsIgnoreCase("MMO"))
					{
						ConfigMMO.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All MMO configs reloaded!");
					}
					else if(op.equalsIgnoreCase("NPC"))
					{
						ConfigNPC.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All NPC configs reloaded!");
					}
					else if(op.equalsIgnoreCase("Olympiad"))
					{
						ConfigOlympiad.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Olympiad configs reloaded!");
					}
					else if(op.equalsIgnoreCase("PvP"))
					{
						ConfigPvP.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All PvP configs reloaded!");
					}
					else if(op.equalsIgnoreCase("Rates"))
					{
						ConfigRates.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Rates configs reloaded!");
					}
					else if(op.equalsIgnoreCase("CastleSiegeEngine"))
					{
						ConfigSiege.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All CastleSiegeEngine configs reloaded!");
					}
					// Mods Folder
					else if(op.equalsIgnoreCase("Banking"))
					{
						ConfigBanking.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Banking configs reloaded!");
					}
					else if(op.equalsIgnoreCase("Champion"))
					{
						ConfigChampion.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Champion configs reloaded!");
					}
					else if(op.equalsIgnoreCase("Chars"))
					{
						ConfigChars.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Chars configs reloaded!");
					}
					else if(op.equalsIgnoreCase("Chat"))
					{
						ConfigChat.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Chat configs reloaded!");
					}
					else if(op.equalsIgnoreCase("Custom"))
					{
						ConfigCustom.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Custom configs reloaded!");
					}
					else if(op.equalsIgnoreCase("GraciaSeeds"))
					{
						ConfigGraciaSeeds.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All GraciaSeeds configs reloaded!");
					}
					else if(op.equalsIgnoreCase("OfflineTrade"))
					{
						ConfigOfflineTrade.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All OfflineTrade configs reloaded!");
					}
					else if(op.equalsIgnoreCase("Wedding"))
					{
						ConfigWedding.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Wedding configs reloaded!");
					}
					// Network Folder
					else if(op.equalsIgnoreCase("CommunityServer"))
					{
						ConfigCommunityServer.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All CommunityServer configs reloaded!");
					}
					else if(op.equalsIgnoreCase("GameServerStartup"))
					{
						ConfigGameServer.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All GameServerStartup configs reloaded!");
					}
					else if(op.equalsIgnoreCase("Hexid"))
					{
						ConfigHexid.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Hexid configs reloaded!");
					}
					else if(op.equalsIgnoreCase("IPConfig"))
					{
						ConfigIPConfig.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All IPConfig configs reloaded!");
					}
					// Security Folder
					else if(op.equalsIgnoreCase("ProtectionAdmin"))
					{
						ConfigProtectionAdmin.loadConfig();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All ProtectionAdmin configs reloaded!");
					}
					else
					{
						activeChar.sendMessage("This command does not exist!");
						activeChar.sendMessage("Usage: //reload_config <Name File>");
						AdminHelpPage.showHelpPage(activeChar, "reload_config_01.htm");
					}
				}
				catch(Exception e)
				{
					AdminHelpPage.showHelpPage(activeChar, "reload_config_01.htm");
					activeChar.sendMessage("An error occured while reloading config " + op + " !");
					_log.log(Level.ERROR, "An error occured while reloading config " + cmd[0] + ' ' + op + ": " + e);
				}
			}
			else
			{
				AdminHelpPage.showHelpPage(activeChar, "reload_config_01.htm");
			}
		}
		else if(cmd[0].equals("admin_reload"))
		{
			if(cmd.length == 2)
			{
				String op = cmd[1];
				try
				{
					if(op.equalsIgnoreCase("access"))
					{
						AdminTable.getInstance().load();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "Access Rights have been reloaded!");
					}
					else if(op.equalsIgnoreCase("config"))
					{
						Config.load();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Config Settings have been reloaded!");
					}
					else if(op.equalsIgnoreCase("htm"))
					{
						HtmCache.getInstance().reload();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "Cache[HTML]: " + HtmCache.getInstance().getLoadedFiles() + " files loaded!");
					}
					else if(op.equalsIgnoreCase("item"))
					{
						ItemTable.getInstance().reload();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "Item Templates have been reloaded!");
					}
					else if(op.equalsIgnoreCase("multisell"))
					{
						MultiSellData.getInstance().load();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Multisells have been reloaded!");
					}
					else if(op.equalsIgnoreCase("npc"))
					{
						NpcTable.getInstance().load();
						QuestManager.getInstance().reloadAllQuests();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All NPCs have been reloaded!");
					}
					else if(op.equals("npc_only"))
					{
						NpcTable.getInstance().load();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All NPCs have been reloaded");
					}
					else if(op.equalsIgnoreCase("npcwalkers"))
					{
						NpcWalkerRoutesData.getInstance().load();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "NPC Walker Routes have been reloaded!");
					}
					else if(op.equalsIgnoreCase("jumproute"))
					{
						CharJumpRoutesTable.getInstance().load();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "Character Jump Routes have been reloaded!");
					}
					else if(op.equalsIgnoreCase("quests"))
					{
						QuestManager.getInstance().reloadAllQuests();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Quests have been reloaded!");
					}
					else if(op.equalsIgnoreCase("skill"))
					{
						SkillTable.getInstance().reload();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "All Skills have been reloaded!");
					}
					else if(op.equalsIgnoreCase("teleport"))
					{
						TeleportListTable.getInstance().load();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "Teleport Locations have been reloaded");
					}
					else if(op.equalsIgnoreCase("zone"))
					{
						ZoneManager.getInstance().reload();
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "Zones have been reloaded");
					}
					else if(op.startsWith("prime"))
					{
						//PrimeShopTable.getInstance().load(); TODO
						activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "PrimeShop items have been reloaded");
					}
                    else if(op.startsWith("spawn"))
                    {
                        SpawnTable.getInstance().reload();
                        activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "SpawnTable npc's have been reloaded");
                    }
					else
					{
						activeChar.sendMessage("This command does not exist!");
						activeChar.sendMessage("Usage: //reload <access|config|door|htm|instancemanager|item|modsbuffer|multisell|npc|npcwalkers|quests|skill|teleport>");
						AdminHelpPage.showHelpPage(activeChar, "reload.htm");
					}
				}
				catch(Exception e)
				{
					AdminHelpPage.showHelpPage(activeChar, "reload.htm");
					activeChar.sendMessage("An error occured while reloading " + op + " !");
					_log.log(Level.ERROR, "An error occured while reloading " + cmd[0] + ' ' + op + ": " + e);
				}
			}
			else
			{
				AdminHelpPage.showHelpPage(activeChar, "reload.htm");
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
