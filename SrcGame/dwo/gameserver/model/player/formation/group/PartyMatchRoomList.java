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
package dwo.gameserver.model.player.formation.group;

import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.party.ExClosePartyRoom;
import javolution.util.FastMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Gnacik
 */
public class PartyMatchRoomList
{
	private int _maxid = 1;
	private Map<Integer, PartyMatchRoom> _rooms;

	private PartyMatchRoomList()
	{
		_rooms = new FastMap<>();
	}

	public void addPartyMatchRoom(int id, PartyMatchRoom room)
	{
		synchronized(this)
		{
			_rooms.put(id, room);
			_maxid++;
		}
	}

	public void deleteRoom(int id)
	{
		for(L2PcInstance _member : getRoom(id).getMembers())
		{
			if(_member == null)
			{
				continue;
			}

			_member.sendPacket(new ExClosePartyRoom());
			_member.sendPacket(SystemMessageId.PARTY_ROOM_DISBANDED);

			_member.setPartyRoom(0);
			//_member.setPartyMatching(0);
			_member.broadcastUserInfo();
		}
		_rooms.remove(id);
	}

	public PartyMatchRoom getRoom(int id)
	{
		return _rooms.get(id);
	}

	public PartyMatchRoom[] getRooms()
	{
		return _rooms.values().toArray(new PartyMatchRoom[_rooms.size()]);
	}

	public int getPartyMatchRoomCount()
	{
		return _rooms.size();
	}

	public int getMaxId()
	{
		return _maxid;
	}

	public PartyMatchRoom getPlayerRoom(L2PcInstance player)
	{
		for(PartyMatchRoom _room : _rooms.values())
		{
			for(L2PcInstance member : _room.getMembers())
			{
				if(member.equals(player))
				{
					return _room;
				}
			}
		}
		return null;
	}

	public List<PartyMatchRoom> getMatchingRooms(int type, int region, boolean allLevels, L2PcInstance activeChar)
	{
		List<PartyMatchRoom> res = new ArrayList<>();
		for(PartyMatchRoom room : _rooms.values())
		{
			if(region > 0 && room.getLocation() != region)
			{
				continue;
			}
			if(region == -2 && room.getLocation() != MapRegionManager.getInstance().getMapRegion(activeChar.getLoc()).getBbs())
			{
				continue;
			}
			if(!allLevels && (room.getMinLvl() > activeChar.getLevel() || room.getMaxLvl() < activeChar.getLevel()))
			{
				continue;
			}
			res.add(room);
		}
		return res;
	}

	public int getPlayerRoomId(L2PcInstance player)
	{
		for(PartyMatchRoom _room : _rooms.values())
		{
			for(L2PcInstance member : _room.getMembers())
			{
				if(member.equals(player))
				{
					return _room.getId();
				}
			}
		}
		return -1;
	}	public static PartyMatchRoomList getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final PartyMatchRoomList _instance = new PartyMatchRoomList();
	}
}