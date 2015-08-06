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
package dwo.gameserver.handler.bypasses;

import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.instancemanager.castle.CastleSiegeManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.ItemList;
import org.apache.log4j.Level;

/**
 * Observation command handler.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class Observation extends CommandHandler<String>
{
	private static void doObserve(BypassHandlerParams params)
	{
		L2PcInstance player = params.getPlayer();
		long cost = Long.parseLong(params.getArgs().get(0));
		int x = Integer.parseInt(params.getArgs().get(1));
		int y = Integer.parseInt(params.getArgs().get(2));
		int z = Integer.parseInt(params.getArgs().get(3));

		if(player.reduceAdena(ProcessType.CONSUME, cost, params.getTarget(), true))
		{
			// enter mode
			player.getObserverController().enter(new Location(x, y, z));
			player.sendPacket(new ItemList(player, false));
		}
		player.sendActionFailed();
	}

	@TextCommand
	public boolean observeSiege(BypassHandlerParams params)
	{
		if(!(params.getTarget() instanceof L2Npc))
		{
			return false;
		}

		try
		{
			int x = Integer.parseInt(params.getArgs().get(1));
			int y = Integer.parseInt(params.getArgs().get(2));
			int z = Integer.parseInt(params.getArgs().get(3));
			if(CastleSiegeManager.getInstance().getSiege(x, y, z) != null)
			{
				doObserve(params);
			}
			else
			{
				params.getPlayer().sendPacket(SystemMessageId.ONLY_VIEW_SIEGE);
			}
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, "Exception in " + getClass().getSimpleName(), e);
			return false;
		}
		return true;
	}

	@TextCommand
	public boolean observeOracle(BypassHandlerParams params)
	{
		if(!(params.getTarget() instanceof L2Npc))
		{
			return false;
		}

		try
		{
			doObserve(params);
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, "Exception in " + getClass().getSimpleName(), e);
			return false;
		}
		return true;
	}

	@TextCommand
	public boolean observe(BypassHandlerParams params)
	{
		if(!(params.getTarget() instanceof L2Npc))
		{
			return false;
		}

		try
		{
			doObserve(params);
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, "Exception in " + getClass().getSimpleName(), e);
			return false;
		}
		return true;
	}
}