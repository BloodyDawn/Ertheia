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

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.DynamicQuestManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

/**
 * @author Yorie
 */
public class AdminGameCampain implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_campain_list_info", "admin_campain_list_start", "admin_campain_list_end", "admin_campain_start",
		"admin_campain_end", "admin_campain_config"
	};
	private L2PcInstance _activeChar;

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		String[] args = command.split(" ");
		String commandName = args[0];

		switch(commandName)
		{
			case "admin_campain_list_info":
				showCampainList("INFO");
				break;
			case "admin_campain_list_start":
				showCampainList("START");
				break;
			case "admin_campain_list_end":
				showCampainList("END");
				break;
			case "admin_campain_start":
			{
				if(args.length < 2)
				{
					return false;
				}

				int taskId = Integer.parseInt(args[1]);
				startCampain(taskId);
				break;
			}
			case "admin_campain_end":
				if(args.length < 2)
				{
					return false;
				}

				int taskId = Integer.parseInt(args[1]);
				endCampain(taskId);
				break;
			case "admin_campain_config":
				if(args.length < 3)
				{
					return false;
				}

				String config = args[1];
				String value = args[2];

				configureCampain(config, value);
				break;
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void showCampainList(String mode)
	{
		if(_activeChar == null)
		{
			return;
		}

		String pattern = "";
		String head = "";
		String html = "";
		String template = HtmCache.getInstance().getHtm(_activeChar.getLang(), "mods/admin/game_campain_list.htm");
		int titleLimit = 0;

		switch(mode)
		{
			case "INFO":
				head = "<tr><td><font color=\"LEVEL\">Название</font></td><td><font color=\"LEVEL\">Очки</font></td><td width=20><font color=\"LEVEL\">Уч</font></td><td><font color=\"LEVEL\">Статус</font></td></tr>";
				pattern = "<tr><td>{{CAMPAIN_NAME}}</td><td>{{COLLECTED_POINTS}}</td><td>{{MEMBERS}}</td><td>{{STATUS}}</td></tr>";
				titleLimit = 15;
				break;
			case "START":
				head = "<tr><td><font color=\"LEVEL\">Название</font></td><td></td>";
				pattern = "<tr><td width=350>{{CAMPAIN_NAME}}</td><td><button value=\"Запустить\" action=\"bypass -h admin_campain_start {{TASK_ID}}\" width=82 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
				break;
			case "END":
				head = "<tr><td><font color=\"LEVEL\">Название</font></td><td></td>";
				pattern = "<tr><td width=350>{{CAMPAIN_NAME}}</td><td><button value=\"Завершить\" action=\"bypass -h admin_campain_end {{TASK_ID}}\" width=82 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
				break;
			default:
				_activeChar.sendMessage("Неверный режим кампании.");
				break;
		}

		if(pattern != null && pattern.isEmpty())
		{
			_activeChar.sendMessage("Неверный режим кампании.");
		}
		else
		{
			for(DynamicQuest quest : DynamicQuestManager.getInstance().getAllQuests().values())
			{
				if("START".equalsIgnoreCase(mode) && quest.isStarted() || "END".equalsIgnoreCase(mode) && !quest.isStarted())
				{
					continue;
				}

				String newPattern = pattern;
				String questName = quest.getTemplate().getQuestTitle();
				if(titleLimit > 0 && questName.length() > titleLimit)
				{
					questName = questName.substring(0, 15) + "...";
				}
				String status = quest.isStarted() ? "<font color=\"00ff00\">Начата</font>" : "<font color=\"ff0000\">Завершена</font>";

				html += newPattern.
					replace("{{CAMPAIN_NAME}}", questName).
					replace("{{COLLECTED_POINTS}}", Integer.toString(quest.getCollectedPoints()) + '/' + Integer.toString(quest.getTemplate().getPoints())).
					replace("{{MEMBERS}}", Integer.toString(quest.getAllParticipiants().size())).
					replace("{{STATUS}}", status).
					replace("{{TASK_ID}}", Integer.toString(quest.getTemplate().getTaskId()));
			}
			template = template.replace("{{CAMPAIN_LIST}}", html).replace("{{HEAD}}", head);
			_activeChar.sendPacket(new NpcHtmlMessage(5, template));
		}
	}

	private void startCampain(int id)
	{
		if(_activeChar == null)
		{
			return;
		}

		DynamicQuest quest = DynamicQuestManager.getInstance().getQuestByTaskId(id);

		if(quest == null)
		{
			_activeChar.sendMessage("Кампания не найдена.");
			return;
		}

		if(!quest.getTemplate().isCampain() && DynamicQuestManager.getStepId(quest.getTemplate().getTaskId()) != 1)
		{
			_activeChar.sendMessage("Зоновые квесты можно начинать только с первого этапа.");
			return;
		}

		if(quest.isStarted())
		{
			_activeChar.sendMessage("Кампания уже начата.");
			return;
		}

		if(!quest.getTemplate().isCampain())
		{
			DynamicQuestManager.getInstance().getAllQuests().values().stream().filter(zoneQuest -> zoneQuest.getQuestId() == quest.getQuestId() && zoneQuest.isStarted()).forEach(zoneQuest -> {
				zoneQuest.endQuest(false);
				_activeChar.sendMessage("Завершен квест " + zoneQuest.getTemplate().getQuestTitle());
			});
		}

		quest.startQuest();

		_activeChar.sendMessage("Запущена кампания " + quest.getTemplate().getQuestTitle());

		showCampainList("INFO");
	}

	private void endCampain(int id)
	{
		if(_activeChar == null)
		{
			return;
		}

		DynamicQuest quest = DynamicQuestManager.getInstance().getQuestByTaskId(id);

		if(quest == null)
		{
			_activeChar.sendMessage("Кампания не найдена.");
			return;
		}

		if(!quest.isStarted())
		{
			_activeChar.sendMessage("Кампания уже завершена.");
			return;
		}

		quest.endQuest(false);

		_activeChar.sendMessage("Завершена кампания " + quest.getTemplate().getQuestTitle());

		showCampainList("INFO");
	}

	private void configureCampain(String config, String value)
	{
		switch(config)
		{
			case "gm_point_bonus":
				boolean conf = "on".equalsIgnoreCase(value);
				DynamicQuestManager.setGmPointBonus(conf);

				if(conf)
				{
					_activeChar.sendMessage("Режим кампании: бонус очков ГМам включен.");
				}
				else
				{
					_activeChar.sendMessage("Режим кампании: бонус очков ГМам выключен.");
				}

				break;
			default:
				_activeChar.sendMessage("Неизвестная настройка.");
				break;
		}
	}
}
