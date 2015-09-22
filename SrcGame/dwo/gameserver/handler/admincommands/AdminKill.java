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
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.StringTokenizer;

/**
 * This class handles following admin commands:
 * - kill = kills target L2Character
 * - kill_monster = kills target non-player
 * - kill <radius> = If radius is specified, then ALL players only in that
 * radius will be killed.
 * - kill_monster <radius> = If radius is specified, then ALL non-players only
 * in that radius will be killed.
 *
 * @version $Revision: 1.2.4.5 $ $Date: 2007/07/31 10:06:06 $
 */
public class AdminKill implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_kill", "admin_kill_monster"
	};
	private static Logger _log = LogManager.getLogger(AdminKill.class);

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.startsWith("admin_kill"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command

			if(st.hasMoreTokens())
			{
				String firstParam = st.nextToken();
				L2PcInstance plyr = WorldManager.getInstance().getPlayer(firstParam);
				if(plyr != null)
				{
					if(st.hasMoreTokens())
					{
						try
						{
							int radius = Integer.parseInt(st.nextToken());
							for(L2Character knownChar : plyr.getKnownList().getKnownCharactersInRadius(radius))
							{
								if(knownChar instanceof L2ControllableMobInstance || knownChar.equals(activeChar))
								{
									continue;
								}

								kill(activeChar, knownChar);
							}

							activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
							return true;
						}
						catch(NumberFormatException e)
						{
							activeChar.sendMessage("Invalid radius.");
							return false;
						}
					}
					else
					{
						kill(activeChar, plyr);
					}
				}
				else
				{
					try
					{
						int radius = Integer.parseInt(firstParam);

						for(L2Character knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
						{
							if(knownChar instanceof L2ControllableMobInstance || knownChar.equals(activeChar))
							{
								continue;
							}
							kill(activeChar, knownChar);
						}

						activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
						return true;
					}
					catch(NumberFormatException e)
					{
						activeChar.sendMessage("Usage: //kill <player_name | radius>");
						return false;
					}
				}
			}
			else
			{
				L2Object obj = activeChar.getTarget();
				if(obj instanceof L2ControllableMobInstance || !(obj instanceof L2Character))
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				else
				{
					kill(activeChar, (L2Character) obj);
				}
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void kill(L2PcInstance activeChar, L2Character target)
	{
		if(target instanceof L2PcInstance)
		{
			if(!target.isGM())
			{
				target.stopAllEffects(); // e.g. invincibility effect
			}
			target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar, null);
		}
		else
		{
			boolean targetIsInvul = false;
			if(target.isInvul())
			{
				targetIsInvul = true;
				target.setIsInvul(false);
			}

			target.reduceCurrentHp(target.getMaxHp() + 1, activeChar, null);

			if(targetIsInvul)
			{
				target.setIsInvul(true);
			}
		}
		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "GM: " + activeChar.getName() + '(' + activeChar.getObjectId() + ')' + " killed character " + target.getObjectId());
		}
	}
}
