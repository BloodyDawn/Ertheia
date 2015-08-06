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
import dwo.gameserver.model.player.formation.group.PartyMatchRoom;
import dwo.gameserver.model.player.formation.group.PartyMatchRoomList;
import dwo.gameserver.network.game.serverpackets.Say2;

/**
 * Party & channel chat handler.
 *
 * @author durgus
 * @author Yorie
 */

public class ChatParty extends CommandHandler<Integer>
{
	/**
	 * Usual party chat.
	 * @param params Message params.
	 */
	@NumericCommand(3)
	public boolean partyChat(ChatHandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(activeChar.isInParty())
		{
			Say2 cs = new Say2(activeChar.getObjectId(), ChatType.values()[params.getCommand()], activeChar.getName(), params.getMessage());
			activeChar.getParty().broadcastPacket(cs);
		}

		return true;
	}

	/**
	 * Party matching chat room.
	 * @param params Message params.
	 */
	@NumericCommand(14)
	public boolean partyMatchChat(ChatHandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(activeChar.isInPartyMatchRoom())
		{
			PartyMatchRoom _room = PartyMatchRoomList.getInstance().getPlayerRoom(activeChar);
			if(_room != null)
			{
				Say2 cs = new Say2(activeChar.getObjectId(), ChatType.values()[params.getCommand()], activeChar.getName(), params.getMessage());
				for(L2PcInstance _member : _room.getMembers())
				{
					_member.sendPacket(cs);
				}
			}
		}

		return true;
	}

	/**
	 * Channel commander chat only.
	 * @param params Message params.
	 */
	@NumericCommand(15)
	public boolean channelCommanderChat(ChatHandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(activeChar.isInParty())
		{
			if(activeChar.getParty().isInCommandChannel() && activeChar.getParty().getCommandChannel().getLeader().equals(activeChar))
			{
				Say2 cs = new Say2(activeChar.getObjectId(), ChatType.values()[params.getCommand()], activeChar.getName(), params.getMessage());
				activeChar.getParty().getCommandChannel().broadcastPacket(cs);
			}
		}

		return true;
	}

	/**
	 * Party chat in command channel circumstances for party leader only.
	 * @param params Message params.
	 */
	@NumericCommand(16)
	public boolean channelPartyLeaderChat(ChatHandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(activeChar.isInParty())
		{
			if(activeChar.getParty().isInCommandChannel() && activeChar.getParty().isLeader(activeChar))
			{
				Say2 cs = new Say2(activeChar.getObjectId(), ChatType.values()[params.getCommand()], activeChar.getName(), params.getMessage());
				activeChar.getParty().getCommandChannel().broadcastPacket(cs);
			}
		}

		return true;
	}
}