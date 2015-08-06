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
package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.PartyExitReason;
import dwo.gameserver.model.player.formation.group.PartyMatchRoom;
import dwo.gameserver.model.player.formation.group.PartyMatchRoomList;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.party.JoinParty;
import dwo.gameserver.network.game.serverpackets.packet.pledge.ExManagePartyRoomMember;

public class RequestAnswerJoinParty extends L2GameClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		L2PcInstance requestor = player.getActiveRequester();
		if(requestor == null)
		{
			return;
		}

		requestor.sendPacket(new JoinParty(_response));

		if(_response == 1)
		{
			if(requestor.isInParty())
			{
				if(requestor.getParty().getMemberCount() >= 7)
				{
					player.sendPacket(SystemMessageId.PARTY_FULL);
					requestor.sendPacket(SystemMessageId.PARTY_FULL);
					return;
				}
			}
			player.joinParty(requestor.getParty());

			if(requestor.isInPartyMatchRoom() && player.isInPartyMatchRoom())
			{
				PartyMatchRoomList list = PartyMatchRoomList.getInstance();
				if(list != null && list.getPlayerRoomId(requestor) == list.getPlayerRoomId(player))
				{
					PartyMatchRoom room = list.getPlayerRoom(requestor);
					if(room != null)
					{
						ExManagePartyRoomMember packet = new ExManagePartyRoomMember(player, room, 1);
						room.getMembers().stream().filter(member -> member != null).forEach(member -> member.sendPacket(packet));
					}
				}
			}
			else if(requestor.isInPartyMatchRoom() && !player.isInPartyMatchRoom())
			{
				PartyMatchRoomList list = PartyMatchRoomList.getInstance();
				if(list != null)
				{
					PartyMatchRoom room = list.getPlayerRoom(requestor);
					if(room != null)
					{
						room.addMember(player);
						ExManagePartyRoomMember packet = new ExManagePartyRoomMember(player, room, 1);
						room.getMembers().stream().filter(member -> member != null).forEach(member -> member.sendPacket(packet));
						player.setPartyRoom(room.getId());
						//player.setPartyMatching(1);
						player.broadcastUserInfo();
					}
				}
			}
		}
		else if(_response == -1)
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_SET_TO_REFUSE_PARTY_REQUEST).addPcName(player));

			//activate garbage collection if there are no other members in party (happens when we were creating new one)
			if(requestor.isInParty() && requestor.getParty().getMemberCount() == 1)
			{
				requestor.getParty().removePartyMember(requestor, PartyExitReason.NONE);
			}
		}
		else // 0
		{
			//requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PLAYER_DECLINED)); FIXME: Done in client?

			//activate garbage collection if there are no other members in party (happens when we were creating new one)
			if(requestor.isInParty() && requestor.getParty().getMemberCount() == 1)
			{
				requestor.getParty().removePartyMember(requestor, PartyExitReason.NONE);
			}
		}

		if(requestor.isInParty())
		{
			requestor.getParty().setPendingInvitation(false); // if party is null, there is no need of decreasing
		}

		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}

	@Override
	public String getType()
	{
		return "[C] 2A RequestAnswerJoinParty";
	}
}
