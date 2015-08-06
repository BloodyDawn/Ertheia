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
package dwo.gameserver.handler.chat;

import dwo.config.Config;
import dwo.gameserver.handler.ChatHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.NumericCommand;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.game.serverpackets.Say2;
import org.apache.log4j.Level;

/**
 * Shout chat handler.
 *
 * @author durgus
 * @author Yorie
 */
public class ChatShout extends CommandHandler<Integer>
{
	@NumericCommand(1)
	public boolean shoutChat(ChatHandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		try
		{
			Say2 cs = new Say2(activeChar.getObjectId(), ChatType.values()[params.getCommand()], activeChar.getName(), params.getMessage());

			L2PcInstance[] pls = WorldManager.getInstance().getAllPlayersArray();

			if(Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("on") || Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("gm") && activeChar.isGM())
			{
				int region = MapRegionManager.getInstance().getMapRegion(activeChar.getLoc()).getLocId();
				for(L2PcInstance player : pls)
				{
					if(region == MapRegionManager.getInstance().getMapRegion(player.getLoc()).getLocId() && !RelationListManager.getInstance().isBlocked(player, activeChar) && player.getInstanceId() == activeChar.getInstanceId())
					{
						player.sendPacket(cs);
					}
				}
			}
			else if(Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("global"))
			{
				if(!activeChar.isGM() && !activeChar.getFloodProtectors().getGlobalChat().tryPerformAction(FloodAction.CHAT_GLOBAL))
				{
					activeChar.sendMessage("Do not spam shout channel.");
					return false;
				}

				for(L2PcInstance player : pls)
				{
					if(!RelationListManager.getInstance().isBlocked(player, activeChar))
					{
						player.sendPacket(cs);
					}
				}
			}
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, getClass().getSimpleName() + ": Error while global chatting: Player:" + activeChar.getName() + " Location: " + activeChar.getLoc());
		}

		return true;
	}
}
