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
import dwo.gameserver.model.player.L2Request;
import dwo.gameserver.model.player.formation.group.PartyMatchRoom;
import dwo.gameserver.model.player.formation.group.PartyMatchRoomList;
import dwo.gameserver.network.game.components.SystemMessageId;

public class AnswerJoinPartyRoom extends L2GameClientPacket
{
	private int _response; // not tested, just guessed

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2Request request = activeChar.getRequest();
		if(request == null || !(request.getRequestPacket() instanceof RequestAskJoinPartyRoom))
		{
			return;
		}

		if(!request.isProcessingRequest())
		{
			request.onRequestResponse();
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isOutOfControl())
		{
			request.onRequestResponse();
			activeChar.sendActionFailed();
			return;
		}

		L2PcInstance requestor = request.getPartner();
		if(requestor == null)
		{
			request.onRequestResponse();
			activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			activeChar.sendActionFailed();
			return;
		}

		if(!requestor.getRequest().getPartner().equals(activeChar))
		{
			request.onRequestResponse();
			activeChar.sendActionFailed();
			return;
		}

		// отказ
		if(_response == 0)
		{
			request.onRequestResponse();
			requestor.sendPacket(SystemMessageId.PLAYER_DECLINED);
			return;
		}

		if(PartyMatchRoomList.getInstance().getPlayerRoom(activeChar) != null)
		{
			request.onRequestResponse();
			activeChar.sendActionFailed();
			return;
		}

		try
		{
			PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(requestor);
			//if (room == null || room.getType() != MatchingRoom.PARTY_MATCHING)
			if(room == null)
			{
				return;
			}

			room.addMember(activeChar);
		}
		finally
		{
			activeChar.setActiveRequester(null);
			requestor.onTransactionResponse();
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:15 AnswerJoinPartyRoom";
	}
}