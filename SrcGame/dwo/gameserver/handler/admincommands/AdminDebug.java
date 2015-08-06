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

import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;
import dwo.gameserver.taskmanager.tasks.TaskChaosFestivalRound;
import dwo.scripts.instances.ChaosFestival;

public class AdminDebug implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_debug", "admin_world_stat_recalc", "admin_world_stat_reload", "admin_world_stat_save",
		"admin_chaos_fest_start", "admin_chaos_fest_save"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		String[] commandSplit = command.split(" ");
		if(ADMIN_COMMANDS[0].equalsIgnoreCase(commandSplit[0]))
		{
			L2Object target;
			if(commandSplit.length > 1)
			{
				target = WorldManager.getInstance().getPlayer(commandSplit[1].trim());
				if(target == null)
				{
					activeChar.sendMessage("Игрок не найден.");
					return true;
				}
			}
			else
			{
				target = activeChar.getTarget();
			}

			if(target instanceof L2Character)
			{
				setDebug(activeChar, (L2Character) target);
			}
			else
			{
				setDebug(activeChar, activeChar);
			}
		}
		else if(command.startsWith("admin_world_stat_recalc"))
		{
			if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
			{
				String param = command.substring(ADMIN_COMMANDS[1].length()).trim();
				recalcWorldStatistics(param.equals("general"));
				activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "World Statistic: reCalced!");
			}
		}
		else if(command.startsWith("admin_world_stat_reload"))
		{
			if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
			{
				WorldStatisticsManager.getInstance().reLoad();
				activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "World Statistic: reloaded!");
			}
		}
		else if(command.startsWith("admin_world_stat_save"))
		{
			if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
			{
				WorldStatisticsManager.getInstance().updateAllStatsInDb();
				activeChar.sendChatMessage(0, ChatType.ALL, "SYS", "World Statistic: Data saved successfully!");
			}
		}
		else if(command.startsWith("admin_chaos_fest_start"))
		{
			ChaosFestival.getInstance().testStartFestival();
		}
		else if(command.startsWith("admin_chaos_fest_save"))
		{
			ChaosFestival.getInstance().testSaveData();
		}
		else if(command.startsWith("admin_chaos_fest_end"))
		{
			TaskChaosFestivalRound task = new TaskChaosFestivalRound();
			task.initializate();
			task.onTimeElapsed(null);
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void recalcWorldStatistics(boolean withGeneralStatistic)
	{
		WorldStatisticsManager.getInstance().calculateAllResults(withGeneralStatistic);
	}

	private void setDebug(L2PcInstance activeChar, L2Character target)
	{
		if(target.isDebug())
		{
			target.setDebug(null);
			activeChar.sendMessage("Stop debugging " + target.getName());
		}
		else
		{
			target.setDebug(activeChar);
			activeChar.sendMessage("Start debugging " + target.getName());
		}
	}
}