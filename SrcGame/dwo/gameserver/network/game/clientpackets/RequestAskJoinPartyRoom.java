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

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.PartyMatchRoom;
import dwo.gameserver.model.player.formation.group.PartyMatchRoomList;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.party.ExAskJoinPartyRoom;

/**
 * Format: (ch) S
 *
 * @author -Wooden-
 */
public class RequestAskJoinPartyRoom extends L2GameClientPacket
{
	private String _name; // not tested, just guessed

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		L2PcInstance targetPlayer = WorldManager.getInstance().getPlayer(_name);

		if(targetPlayer == null || targetPlayer.equals(player))
		{
			player.sendActionFailed();
			return;
		}

		if(player.isProcessingRequest())
		{
			player.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			return;
		}

		if(targetPlayer.isProcessingRequest())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addCharName(targetPlayer));
			return;
		}

		if(PartyMatchRoomList.getInstance().getPlayerRoom(targetPlayer) != null)
		{
			return;
		}

		PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(player);

		//if (room == null || room.getType() != MatchingRoom.PARTY_MATCHING)
		if(room == null)
		{
			return;
		}

		if(!room.getOwner().equals(player))
		{
			player.sendPacket(SystemMessageId.ONLY_ROOM_LEADER_CAN_INVITE);
			return;
		}

		if(room.getMembersCount() >= room.getMaxMembers())
		{
			player.sendPacket(SystemMessageId.PARTY_ROOM_FULL);
			return;
		}

		if(!player.getRequest().setRequest(targetPlayer, this))
		{
			return;
		}

		targetPlayer.sendPacket(new ExAskJoinPartyRoom(player.getName(), room.getTitle()));
		targetPlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_SENT_AN_INVITATION_TO_ROOM_S2).addCharName(player).addString(room.getTitle()));
	}

	@Override
	public String getType()
	{
		return "[C] D0:14 RequestAskJoinPartyRoom";
	}
}