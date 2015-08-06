package dwo.gameserver.handler.admincommands;

import dwo.config.Config;
import dwo.gameserver.Announcements;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.engine.guardengine.GuardHwidManager;
import dwo.gameserver.engine.guardengine.model.HackType;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

import java.util.List;

public class AdminHWID implements IAdminCommandHandler
{
	private static String[] _adminCommands = {
		"admin_hwid_ban", "admin_hwid_ban_apply"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.equals("admin_hwid_ban"))
		{
			String htmContent = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/hwid/panel.htm");
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
			npcHtmlMessage.setHtml(htmContent);

			L2Object target = activeChar.getTarget();
			if(target != null && target instanceof L2PcInstance && !target.equals(activeChar))
			{
				npcHtmlMessage.replace("%targetName%", target.getName());
			}
			else
			{
				npcHtmlMessage.replace("%targetName%", "");
			}
			activeChar.sendPacket(npcHtmlMessage);
			return true;
		}
		if(command.startsWith("admin_hwid_ban_apply"))
		{
			String playerToBanName;
			HackType banType;
			String comment;

			String[] data = command.split(" ");

			playerToBanName = data[1];

			if(data.length == 4)
			{
				banType = HackType.valueOf(data[2]);
				comment = data[3];
			}
			else
			{
				activeChar.sendMessage("Поля имени игрока, типа бана и комментария не должны быть пустыми!");
				return false;
			}

			L2PcInstance playerToBan = WorldManager.getInstance().getPlayer(playerToBanName);

			if(playerToBan == null)
			{
				activeChar.sendMessage("Игрока с таким именем нет в данный момент в игре.");
				return false;
			}
			else
			{
				L2GameClient client = playerToBan.getClient();
				String clientHWID = client.getHWID();

				GuardHwidManager.getInstance().tryToBanHWID(clientHWID, client.getConnection().getInetAddress().getHostAddress(), client.getAccountName(), banType, comment);

				// Выкидываем из игры персонажа
				playerToBan.logout();

				// Ищем и выкидываем из игры твинков забаненного персонажа
				List<L2PcInstance> twinks = GuardHwidManager.getInstance().getTwinks(clientHWID);
				if(!twinks.isEmpty())
				{
					for(L2PcInstance twink : twinks)
					{
						twink.logout();
					}
				}

				if(Config.ANNOUNCE_PUNISHMENTS)
				{
					Announcements.getInstance().announceToAll("ГМ " + activeChar.getName() + " забанил по HWID игрока " + playerToBanName);
				}
				activeChar.sendMessage("Игрок " + playerToBan + " успешно внесен в бан-лист.");
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}