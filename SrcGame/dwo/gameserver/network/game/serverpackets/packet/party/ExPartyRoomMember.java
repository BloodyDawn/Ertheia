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

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.PartyMatchRoom;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.Map;

/**
 * @author Gnacik
 */
public class ExPartyRoomMember extends L2GameServerPacket
{
	private final PartyMatchRoom _room;
	private final int _mode;

	public ExPartyRoomMember(L2PcInstance player, PartyMatchRoom room, int mode)
	{
		_room = room;
		_mode = mode;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_mode);
		writeD(_room.getMembersCount());
		for(L2PcInstance _member : _room.getMembers())
		{
			writeD(_member.getObjectId());
			writeS(_member.getName());
			writeD(_member.getActiveClassId());
			writeD(_member.getLevel());
			writeD(MapRegionManager.getInstance().getMapRegion(_member.getLoc()).getBbs());

			if(_room.getOwner().equals(_member))
			{
				writeD(1); // Хозяин комнаты
			}
			else
			{
				if(_room.getOwner().isInParty() && _member.isInParty() && _room.getOwner().getParty().getLeaderObjectId() == _member.getParty().getLeaderObjectId())
				{
					writeD(2);  // участник
				}
				else
				{
					writeD(0);  // кандидат
				}
			}

			// Пройденные зоны
			Map<Integer, Long> instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(_member.getObjectId());
			writeD(instanceTimes.size());
			instanceTimes.keySet().forEach(this::writeD);
		}
	}
}