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

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.player.formation.group.PartyExitReason;
import dwo.gameserver.model.player.formation.group.PartyMatchRoom;
import dwo.gameserver.model.player.formation.group.PartyMatchRoomList;
import dwo.gameserver.network.game.serverpackets.packet.party.ExClosePartyRoom;
import dwo.gameserver.network.game.serverpackets.packet.party.ExPartyRoomMember;
import dwo.gameserver.network.game.serverpackets.packet.party.PartyRoomInfo;

public class RequestWithDrawalParty extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		//trigger
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(!HookManager.getInstance().checkEvent(HookType.ON_PARTY_LEAVE, null, false, player))
		{
			return;
		}

		L2Party party = player.getParty();

		if(party != null)
		{
			party.removePartyMember(player, PartyExitReason.LEFT);

			if(player.isInPartyMatchRoom())
			{
				PartyMatchRoom _room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
				if(_room != null)
				{
					player.sendPacket(new PartyRoomInfo(player, _room));
					player.sendPacket(new ExPartyRoomMember(player, _room, 0));
					player.sendPacket(new ExClosePartyRoom());

					_room.deleteMember(player);
				}
				player.setPartyRoom(0);
				//player.setPartyMatching(0);
				player.broadcastUserInfo();
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] 44 RequestWithDrawalParty";
	}
}
