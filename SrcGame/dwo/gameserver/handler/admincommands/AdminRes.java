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
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2ControllableMobInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.taskmanager.manager.DecayTaskManager;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class AdminRes implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_res", "admin_res_monster"
	};
	private static Logger _log = LogManager.getLogger(AdminRes.class);

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.startsWith("admin_res "))
		{
			handleRes(activeChar, command.split(" ")[1]);
		}
		else if(command.equals("admin_res"))
		{
			handleRes(activeChar);
		}
		else if(command.startsWith("admin_res_monster "))
		{
			handleNonPlayerRes(activeChar, command.split(" ")[1]);
		}
		else if(command.equals("admin_res_monster"))
		{
			handleNonPlayerRes(activeChar);
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void handleRes(L2PcInstance activeChar)
	{
		handleRes(activeChar, null);
	}

	private void handleRes(L2PcInstance activeChar, String resParam)
	{
		L2Object obj = activeChar.getTarget();

		if(resParam != null)
		{
			// Check if a player name was specified as a param.
			L2PcInstance plyr = WorldManager.getInstance().getPlayer(resParam);

			if(plyr != null)
			{
				obj = plyr;
			}
			else
			{
				// Otherwise, check if the param was a radius.
				try
				{
					int radius = Integer.parseInt(resParam);

					activeChar.getKnownList().getKnownPlayersInRadius(radius).forEach(this::doResurrect);

					activeChar.sendMessage("Resurrected all players within a " + radius + " unit radius.");
					return;
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("Enter a valid player name or radius.");
					return;
				}
			}
		}

		if(obj == null)
		{
			obj = activeChar;
		}

		if(obj instanceof L2ControllableMobInstance)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		doResurrect((L2Character) obj);

		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "GM: " + activeChar.getName() + '(' + activeChar.getObjectId() + ") resurrected character " + obj.getObjectId());
		}
	}

	private void handleNonPlayerRes(L2PcInstance activeChar)
	{
		handleNonPlayerRes(activeChar, "");
	}

	private void handleNonPlayerRes(L2PcInstance activeChar, String radiusStr)
	{
		L2Object obj = activeChar.getTarget();

		try
		{
			int radius = 0;

			if(!radiusStr.isEmpty())
			{
				radius = Integer.parseInt(radiusStr);

				activeChar.getKnownList().getKnownCharactersInRadius(radius).stream().filter(knownChar -> !(knownChar instanceof L2PcInstance) && !(knownChar instanceof L2ControllableMobInstance)).forEach(this::doResurrect);

				activeChar.sendMessage("Resurrected all non-players within a " + radius + " unit radius.");
			}
		}
		catch(NumberFormatException e)
		{
			activeChar.sendMessage("Enter a valid radius.");
			return;
		}

		if(obj instanceof L2PcInstance || obj instanceof L2ControllableMobInstance)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		doResurrect((L2Character) obj);
	}

	private void doResurrect(L2Character targetChar)
	{
		if(!targetChar.isDead())
		{
			return;
		}

		// If the target is a player, then restore the XP lost on death.
		if(targetChar instanceof L2PcInstance)
		{
			((L2PcInstance) targetChar).restoreExp(100.0);
		}// If the target is an NPC, then abort it's auto decay and respawn.
		else
		{
			DecayTaskManager.getInstance().cancelDecayTask(targetChar);
		}

		targetChar.doRevive();
	}
}
