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

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.RaidBossSpawnManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;

public class AdminDelete implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_delete", "admin_del_obj"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.equals("admin_delete"))
		{
			handleDelete(activeChar);
		}
		else if(command.startsWith("admin_del_obj"))
		{
			String[] commandSplit = command.split(" ");
			int obj = Integer.parseInt(commandSplit[1]);
			handleDeleteObj(activeChar, obj);
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	// TODO: add possibility to delete any L2Object (except L2PcInstance)

	private void handleDelete(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if(obj instanceof L2Npc)
		{
			L2Npc target = (L2Npc) obj;
			target.getLocationController().delete();

			L2Spawn spawn = target.getSpawn();
			if(spawn != null)
			{
				spawn.stopRespawn();

				if(RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()))
				{
					RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
				}
				else
				{
					SpawnTable.getInstance().deleteSpawn(spawn);
				}
			}

			activeChar.sendMessage("Deleted " + target.getName() + " from " + target.getObjectId() + '.');
		}
		else
		{
			activeChar.sendMessage("Incorrect target.");
		}
	}

	private void handleDeleteObj(L2PcInstance activeChar, int obj)
	{
		try
		{
			L2Object object = WorldManager.getInstance().findObject(obj);
			if(object instanceof L2Npc)
			{
				L2Npc npc = (L2Npc) object;
				npc.getLocationController().delete();

				L2Spawn spawn = npc.getSpawn();
				if(spawn != null)
				{
					spawn.stopRespawn();

					if(RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()))
					{
						RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
					}
					else
					{
						SpawnTable.getInstance().deleteSpawn(spawn);
					}
				}

				activeChar.sendMessage("Deleted " + npc.getName() + " from " + npc.getObjectId() + '.');
			}
			else
			{
				activeChar.sendMessage("Incorrect target (handleDeleteObj)");
			}
		}
		catch(Exception e)
		{
			activeChar.sendMessage("Error L2Object (handleDeleteObj)");
		}
	}
}
