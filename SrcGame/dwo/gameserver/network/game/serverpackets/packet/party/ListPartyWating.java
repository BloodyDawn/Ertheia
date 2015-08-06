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
package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.PartyMatchRoom;
import dwo.gameserver.model.player.formation.group.PartyMatchRoomList;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Gnacik
 *
 */
public class ListPartyWating extends L2GameServerPacket
{
	private Collection<PartyMatchRoom> _rooms;
	private int _fullSize;

	public ListPartyWating(int region, boolean allLevels, int page, L2PcInstance activeChar)
	{
		int first = page - 1 << 6;
		int firstNot = page << 6;
		_rooms = new ArrayList<>();

		int i = 0;
		List<PartyMatchRoom> temp = PartyMatchRoomList.getInstance().getMatchingRooms(0, region, allLevels, activeChar);
		_fullSize = temp.size();
		for(PartyMatchRoom room : temp)
		{
			if(i < first || i >= firstNot)
			{
				continue;
			}
			_rooms.add(room);
			i++;
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_fullSize);
		writeD(_rooms.size());

		for(PartyMatchRoom room : _rooms)
		{
			writeD(room.getId()); //room id
			writeS(room.getTitle()); // room name
			writeD(room.getLocation());
			writeD(room.getMinLvl()); //min level
			writeD(room.getMaxLvl()); //max level
			writeD(room.getMaxMembers()); //max members coun
			writeS(room.getOwner() == null ? "none" : room.getOwner().getName());

			Collection<L2PcInstance> players = room.getMembers();
			writeD(players.size()); //members count
			for(L2PcInstance player : players)
			{
				writeD(player.getActiveClassId());
				writeS(player.getName());
			}
		}
	}
}
