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

import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.network.game.serverpackets.MagicSkillLaunched;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class AdminPackets implements IAdminCommandHandler
{

	private static final String[] ADMIN_COMMANDS = {
		"admin_scene", "admin_usm", "admin_magic_skill_launch", "admin_magic_skill_use", "admin_add_abnormal_effect",
	};
	public static Logger _log = LogManager.getLogger(AdminPackets.class);

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.startsWith(ADMIN_COMMANDS[0]))
		{
			try
			{
				int sceneId = Integer.parseInt(command.substring(command.indexOf(' ') + 1));

				startScenePlayer(activeChar, sceneId);
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Cannot start scene player!");
			}
		}
		else if(command.startsWith(ADMIN_COMMANDS[1]))
		{
			try
			{
				int movieId = Integer.parseInt(command.substring(command.indexOf(' ') + 1));

				showUsmVideo(activeChar, movieId);
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Cannot start scene player!");
			}
		}
		else if(command.startsWith(ADMIN_COMMANDS[2]))
		{
			try
			{
				L2Object target = activeChar.getTarget();

				if(!(target instanceof L2Character))
				{
					return false;
				}

				String[] params = command.split(" ");

				if(params.length < 3)
				{
					return false;
				}

				int skillId = Integer.parseInt(params[1]);
				int skillLvl = Integer.parseInt(params[2]);

				activeChar.sendPacket(new MagicSkillLaunched((L2Character) target, skillId, skillLvl));
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Cannot emulate magic skill launched packet!");
			}
		}
		else if(command.startsWith(ADMIN_COMMANDS[3]))
		{
			try
			{
				L2Object target = activeChar.getTarget();

				if(!(target instanceof L2Character))
				{
					return false;
				}

				String[] params = command.split(" ");

				if(params.length < 3)
				{
					return false;
				}

				int skillId = Integer.parseInt(params[1]);
				int skillLvl = Integer.parseInt(params[2]);

				activeChar.sendPacket(new MagicSkillUse((L2Character) target, (L2Character) target, SkillTable.getInstance().getInfo(skillId, skillLvl)));
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Cannot emulate magic skill use!");
			}
		}
		else if(command.startsWith(ADMIN_COMMANDS[4]))
		{
			try
			{
				L2Object target = activeChar.getTarget();

				if(!(target instanceof L2Character))
				{
					return false;
				}

				int abnormalEffect = Integer.parseInt(command.substring(ADMIN_COMMANDS[4].length() + 1));

				((L2Character) target).startAbnormalEffect(abnormalEffect);
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Cannot add abnormal effect!");
			}
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void showUsmVideo(L2PcInstance activeChar, int movieId)
	{
		activeChar.showUsmVideo(movieId);
	}

	private void startScenePlayer(L2PcInstance activeChar, int sceneId)
	{
		activeChar.showQuestMovie(sceneId);
	}
}
