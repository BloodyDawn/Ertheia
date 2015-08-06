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
import dwo.gameserver.model.player.formation.group.PartyMatchRoom;
import dwo.gameserver.model.player.formation.group.PartyMatchRoomList;
import dwo.gameserver.model.player.formation.group.PartyMatchWaitingList;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.party.ExPartyRoomMember;
import dwo.gameserver.network.game.serverpackets.packet.party.ListPartyWating;
import dwo.gameserver.network.game.serverpackets.packet.party.PartyRoomInfo;

public class RequestPartyMatchConfig extends L2GameClientPacket
{
	private int _page;
	private int _region;
	private int _allLevels;

	@Override
	protected void readImpl()
	{
		_page = readD();    //
		_region = readD();    // 0 to 15, or -1
		_allLevels = readD();        // 1 -> all levels, 0 -> only levels matching my level
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance _activeChar = getClient().getActiveChar();

		if(_activeChar == null)
		{
			return;
		}

		if(!_activeChar.isInPartyMatchRoom() && _activeChar.getParty() != null && !_activeChar.getParty().getLeader().equals(_activeChar))
		{
			_activeChar.sendPacket(SystemMessageId.CANT_VIEW_PARTY_ROOMS);
			_activeChar.sendActionFailed();
			return;
		}

		if(_activeChar.isInPartyMatchRoom())
		{
			// If Player is in Room show him room, not list
			PartyMatchRoomList _list = PartyMatchRoomList.getInstance();
			if(_list == null)
			{
				return;
			}

			PartyMatchRoom _room = _list.getPlayerRoom(_activeChar);
			if(_room == null)
			{
				return;
			}

			_activeChar.sendPacket(new PartyRoomInfo(_activeChar, _room));
			_activeChar.sendPacket(new ExPartyRoomMember(_activeChar, _room, 2));

			_activeChar.setPartyRoom(_room.getId());
			//_activeChar.setPartyMatching(1);
			_activeChar.broadcastUserInfo();
		}
		else
		{
			// Add to waiting list
			PartyMatchWaitingList.getInstance().addPlayer(_activeChar);

			// Send Room list
			ListPartyWating matchList = new ListPartyWating(_region, _allLevels == 1, _page, _activeChar);

			_activeChar.sendPacket(matchList);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 7F RequestPartyMatchConfig";
	}
}
