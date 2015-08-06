package dwo.gameserver.handler.admincommands;

import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

import java.util.List;
import java.util.Map;

public class AdminQuest implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_script_load", "admin_show_quests"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}
		// script load should NOT be used in place of reload.  If a script is already loaded
		// successfully, quest_reload ought to be used.  The script_load command should only
		// be used for scripts that failed to load altogether (eg. due to errors) or that
		// did not at all exist during server boot.  Using script_load to re-load a previously
		// loaded script may cause unpredictable script flow, minor loss of data, and more.
		// This provides a way to load new scripts without having to reboot the server.
		if(command.startsWith("admin_script_load"))
		{
			QuestManager.getInstance().reloadAllQuests();
			activeChar.sendMessage("Скрипты были успешно перезагружены.");
		}
		else if(command.startsWith("admin_show_quests"))
		{
			if(activeChar.getTarget() == null)
			{
				activeChar.sendMessage("Сперва выберите цель.");
			}
			else if(!activeChar.getTarget().isNpc())
			{
				activeChar.sendMessage("Неверная цель.");
			}
			else
			{
				L2Npc npc = L2Npc.class.cast(activeChar.getTarget());
				NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
				msg.setFile(activeChar.getLang(), "mods/admin/npc-quests.htm");
				StringBuilder sb = new StringBuilder();
				for(Map.Entry<Quest.QuestEventType, List<Quest>> quests : npc.getTemplate().getEventQuests().entrySet())
				{
					int questId = -1;
					for(Quest quest : quests.getValue())
					{
						questId = quest.getQuestId();
						if(quest.getQuestId() > 10000)
						{
							questId -= 5000;
						}
						else if(questId == 146)
						{
							questId = 640;
						}

						// Custom-квесты
						if(questId < 0)
						{
							sb.append("<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">").append(quest.getName()).append("</font></td></tr></table></td></tr>");
						}
						// Retail-квесты
						else
						{
							sb.append("<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">[").append(quest.getQuestId()).append("] <fstring>").append(questId).append("01").append("</fstring>").append("</font></td></tr></table></td></tr>");
						}
					}
					sb.append("<tr><td colspan=\"4\"><table width=270 border=0><tr><td width=270>").append(quests.getKey()).append("</td></tr></table></td></tr>");
				}
				msg.replace("%quests%", sb.toString());
				msg.replace("%tmplid%", Integer.toString(npc.getTemplate().getNpcId()));
				activeChar.sendPacket(msg);
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
