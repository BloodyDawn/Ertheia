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

import dwo.gameserver.engine.geodataengine.PathFinding;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.Location;

import java.util.concurrent.TimeUnit;

public class AdminPathNode implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_pf_info", "admin_pf_find", "admin_pf_reset", "admin_pf_clear"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		switch(command)
		{
			case "admin_pf_info":
				int allocatedBuffers = PathFinding.getInstance().getAllocatedBuffers();
				int pathsComputedFound = Math.max(PathFinding.getInstance().getPathsComputedFound(), 1);
				int pathsComputedNotFound = Math.max(PathFinding.getInstance().getPathsComputedNotFound(), 1);
				int pathsComputed = pathsComputedFound + pathsComputedNotFound;

				long totalComputingTimeNanosFound = PathFinding.getInstance().getTotalComputingTimeNanosFound();
				long totalComputingTimeNanosFoundBuild = PathFinding.getInstance().getTotalComputingTimeNanosFoundBuild();
				long totalComputingTimeNanosNotFound = PathFinding.getInstance().getTotalComputingTimeNanosNotFound();
				long totalComputingTimeNanos = totalComputingTimeNanosFound + totalComputingTimeNanosFoundBuild + totalComputingTimeNanosNotFound;

				long totalComputingTimeMillisFound = TimeUnit.MILLISECONDS.convert(totalComputingTimeNanosFound, TimeUnit.NANOSECONDS);
				long totalComputingTimeMillisFoundBuild = TimeUnit.MILLISECONDS.convert(totalComputingTimeNanosFoundBuild, TimeUnit.NANOSECONDS);
				long totalComputingTimeMillisNotFound = TimeUnit.MILLISECONDS.convert(totalComputingTimeNanosNotFound, TimeUnit.NANOSECONDS);
				long totalComputingTimeMillis = TimeUnit.MILLISECONDS.convert(totalComputingTimeNanos, TimeUnit.NANOSECONDS);

				long averageTimeNanosFound = totalComputingTimeNanosFound / pathsComputedFound % 1000000;
				long averageTimeMillisFound = TimeUnit.MILLISECONDS.convert(totalComputingTimeNanosFound / pathsComputedFound, TimeUnit.NANOSECONDS);

				long averageTimeNanosFoundBuild = totalComputingTimeNanosFoundBuild / pathsComputedFound % 1000000;
				long averageTimeMillisFoundBuild = TimeUnit.MILLISECONDS.convert(totalComputingTimeNanosFoundBuild / pathsComputedFound, TimeUnit.NANOSECONDS);

				long averageTimeNanosNotFound = totalComputingTimeNanosNotFound / pathsComputedNotFound % 1000000;
				long averageTimeMillisNotFound = TimeUnit.MILLISECONDS.convert(totalComputingTimeNanosNotFound / pathsComputedNotFound, TimeUnit.NANOSECONDS);

				long averageTimeNanos = totalComputingTimeNanos / pathsComputed % 1000000;
				long averageTimeMillis = TimeUnit.MILLISECONDS.convert(totalComputingTimeNanos / pathsComputed, TimeUnit.NANOSECONDS);

				activeChar.sendMessage("Total allocated buffers: " + allocatedBuffers);
				activeChar.sendMessage("Total paths computed: " + pathsComputed);
				activeChar.sendMessage("Total computing time: " + (int) (totalComputingTimeMillis / 1000) + "s, " + (int) (totalComputingTimeMillis % 1000) + "ms");
				activeChar.sendMessage("Avg. computing time: " + averageTimeMillis + "ms, " + averageTimeNanos + "ns");

				activeChar.sendMessage("Total found paths computed: " + pathsComputedFound);
				activeChar.sendMessage("Total found paths compute time: " + (int) (totalComputingTimeMillisFound / 1000) + "s, " + (int) (totalComputingTimeMillisFound % 1000) + "ms");
				activeChar.sendMessage("Avg. found paths computing time: " + averageTimeMillisFound + "ms, " + averageTimeNanosFound + "ns");

				activeChar.sendMessage("Total build paths compute time: " + (int) (totalComputingTimeMillisFoundBuild / 1000) + "s, " + (int) (totalComputingTimeMillisFoundBuild % 1000) + "ms");
				activeChar.sendMessage("Avg. build paths computing time: " + averageTimeMillisFoundBuild + "ms, " + averageTimeNanosFoundBuild + "ns");

				activeChar.sendMessage("Total not found paths computed: " + pathsComputedNotFound);
				activeChar.sendMessage("Total not found paths compute time: " + (int) (totalComputingTimeMillisNotFound / 1000) + "s, " + (int) (totalComputingTimeMillisNotFound % 1000) + "ms");
				activeChar.sendMessage("Avg. not found paths computing time: " + averageTimeMillisNotFound + "ms, " + averageTimeNanosNotFound + "ns");
				break;
			case "admin_pf_find":
				L2Object target = activeChar.getTarget();
				if(target != null)
				{
					Location[] path = PathFinding.getInstance().findPath(activeChar.getX(), activeChar.getY(), (short) activeChar.getZ(), target.getX(), target.getY(), (short) target.getZ(), true, activeChar.getInstanceId());
					if(path == null || path.length < 2)
					{
						activeChar.sendMessage("No Route!");
						return true;
					}
					activeChar.sendMessage("Found: " + path.length + ", instance: " + activeChar.getInstanceId());
					for(int i = 0; i < path.length; i++)
					{
						Location loc = path[i];
						activeChar.sendMessage(i + " x: " + loc.getX() + ", y: " + loc.getY() + ", z: " + loc.getZ());
					}
				}
				else
				{
					activeChar.sendMessage("No Target!");
				}
				break;
			case "admin_pf_reset":
				PathFinding.getInstance().resetStats();
				activeChar.sendMessage("Stats reseted");
				break;
			case "admin_pf_clear":
				PathFinding.getInstance().deleteBuffers();
				activeChar.sendMessage("Buffers deleted.");
				break;
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
