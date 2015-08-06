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
import dwo.gameserver.handler.NumericCommand;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.network.game.serverpackets.Say2;

/**
 * Alliance chat handler.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class ChatAlliance extends CommandHandler<Integer>
{
	@NumericCommand(9)
	public boolean allianceChat(ChatHandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();
		if(activeChar.getClan() != null)
		{
			Say2 cs = new Say2(activeChar.getObjectId(), ChatType.values()[params.getCommand()], activeChar.getName(), params.getMessage());
			activeChar.getClan().broadcastToOnlineAllyMembers(cs);
		}
		return true;
	}
}