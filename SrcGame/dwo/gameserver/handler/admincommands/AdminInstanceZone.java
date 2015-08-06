package dwo.gameserver.handler.admincommands;

import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.util.StringUtil;
import javolution.util.FastList;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class AdminInstanceZone implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_instancezone", "admin_instancezone_clear", "admin_instancezone_party_clear"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.startsWith("admin_instancezone_clear"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");

				st.nextToken();
				L2PcInstance player = WorldManager.getInstance().getPlayer(st.nextToken());
				if(player == null)
				{
					return false;
				}
				int instanceId = Integer.parseInt(st.nextToken());
				String name = InstanceManager.getInstance().getInstanceIdName(instanceId);
				InstanceManager.getInstance().deleteInstanceTime(player.getObjectId(), instanceId);
				activeChar.sendMessage("Инстанс зона " + name + " сброшена для игрока " + player.getName());
				player.sendMessage("Администрация сбросила инстанс " + name + " для Вашего персонажа.");

				return true;
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Failed clearing instance time: " + e.getMessage());
				activeChar.sendMessage("Синтаксис: //instancezone_clear <playername> [instanceId]");
				return false;
			}
		}
		if(command.startsWith("admin_instancezone_party_clear"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");

				st.nextToken();
				L2PcInstance player = WorldManager.getInstance().getPlayer(st.nextToken());
				if(player == null)
				{
					return false;
				}
				int instanceId = Integer.parseInt(st.nextToken());
				String name = InstanceManager.getInstance().getInstanceIdName(instanceId);
				StringBuilder sb = new StringBuilder();
				sb.append("<html><head><title>Сброс штрафов инстанса для группы</title></head><body><center>Список игроков, для которых сбрасываем инстансы:</center><br>");
				List<L2PcInstance> players = null;
				if(player.isInParty())
				{
					players = player.getParty().isInCommandChannel() ? player.getParty().getCommandChannel().getMembers() : player.getParty().getMembers();
				}
				else
				{
					players = new FastList<>();
					players.add(player);
				}

				for(L2PcInstance pl : players)
				{
					InstanceManager.getInstance().deleteInstanceTime(pl.getObjectId(), instanceId);
					sb.append("<font color=\"LEVEL\">").append(pl.getName()).append("</font><br1>");
					if(pl.getObjectId() != activeChar.getObjectId())
					{
						pl.sendMessage("Админ сбросил штраф инстанса " + name + " для Вашего персонажа.");
					}
				}
				sb.append("</body></html>");
				activeChar.sendPacket(new NpcHtmlMessage(0, sb.toString()));
				return true;
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Ошибка сброса инстанса: " + e.getMessage());
				activeChar.sendMessage("Синатаксис: //instancezone_clear <playername> [instanceId]");
				return false;
			}
		}
		if(command.startsWith("admin_instancezone"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			command = st.nextToken();

			if(st.hasMoreTokens())
			{
				L2PcInstance player = null;
				String playername = st.nextToken();

				try
				{
					player = WorldManager.getInstance().getPlayer(playername);
				}
				catch(Exception e)
				{
				}

				if(player != null)
				{
					display(player, activeChar);
				}
				else
				{
					activeChar.sendMessage("Игрок " + playername + " не в игре.");
					activeChar.sendMessage("Синтаксис: //instancezone [playername]");
					return false;
				}
			}
			else if(activeChar.getTarget() != null)
			{
				if(activeChar.getTarget() instanceof L2PcInstance)
				{
					display((L2PcInstance) activeChar.getTarget(), activeChar);
				}
			}
			else
			{
				display(activeChar, activeChar);
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void display(L2PcInstance player, L2PcInstance activeChar)
	{
		Map<Integer, Long> instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(activeChar.getObjectId());

		StringBuilder html = StringUtil.startAppend(500 + instanceTimes.size() * 200, "<html><center><table width=260><tr>" +
			"<td width=40><button value=\"Главная\" action=\"bypass -h admin_admin\" width=40 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" +
			"<td width=180><center>Инстансы персонажа</center></td>" +
			"<td width=40><button value=\"Назад\" action=\"bypass -h admin_current_player\" width=40 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" +
			"</tr></table><br><font color=\"LEVEL\">Instances for ", player.getName(), "</font><center><br>" +
			"<table>" +
			"<tr><td width=150>имя</td><td width=50>Время</td><td width=70>Действие</td></tr>");

		for(Map.Entry<Integer, Long> integerLongEntry : instanceTimes.entrySet())
		{
			int hours = 0;
			int minutes = 0;
			long remainingTime = (integerLongEntry.getValue() - System.currentTimeMillis()) / 1000;
			if(remainingTime > 0)
			{
				hours = (int) (remainingTime / 3600);
				minutes = (int) (remainingTime % 3600 / 60);
			}

			StringUtil.append(html, "<tr><td>", InstanceManager.getInstance().getInstanceIdName(integerLongEntry.getKey()), "</td><td>", String.valueOf(hours), ":", String.valueOf(minutes), "</td><td><button value=\"Очистить\" action=\"bypass -h admin_instancezone_clear ", player.getName(), " ", String.valueOf(integerLongEntry.getKey()), "\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		}

		StringUtil.append(html, "</table></html>");

		NpcHtmlMessage ms = new NpcHtmlMessage(1);
		ms.setHtml(html.toString());

		activeChar.sendPacket(ms);
	}
}