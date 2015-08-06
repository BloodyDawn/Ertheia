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

import dwo.gameserver.handler.ChatHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.NumericCommand;
import dwo.gameserver.handler.VoicedHandlerManager;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.network.game.serverpackets.Say2;
import javolution.util.FastList;

import java.util.List;

/**
 * All chat handler.
 *
 * @author durgus
 * @author Yorie
 */
public class ChatAll extends CommandHandler<Integer>
{
	@NumericCommand(0)
	public boolean allChat(ChatHandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		// Probably voiced command
		boolean voiceUsed = false;
		if(!params.getMessage().isEmpty() && params.getMessage().charAt(0) == '.')
		{
			List<String> voiceParams = HandlerParams.parseArgs(params.getMessage());

			if(voiceParams.size() <= 0)
			{
				return false;
			}

			String command = voiceParams.get(0);

			if(command.length() <= 1)
			{
				return false;
			}

			command = command.substring(1);

			// Remove command itself from params list
			voiceParams = voiceParams.size() <= 1 ? new FastList<>() : voiceParams.subList(1, voiceParams.size());

			voiceUsed = VoicedHandlerManager.getInstance().execute(new HandlerParams<>(activeChar, command, voiceParams, null));
		}

		if(!voiceUsed)
		{
			Say2 cs = new Say2(activeChar.getObjectId(), ChatType.values()[params.getCommand()], activeChar.getAppearance().getVisibleName(), params.getMessage());

			activeChar.getKnownList().getKnownPlayers().values().stream().filter(player -> player != null && activeChar.isInsideRadius(player, 1250, false, true) && !RelationListManager.getInstance().isBlocked(player, activeChar)).forEach(player -> player.sendPacket(cs));
			activeChar.sendPacket(cs);
		}
		return true;
	}
}