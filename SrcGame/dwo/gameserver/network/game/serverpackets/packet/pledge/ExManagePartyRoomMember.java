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
package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.PartyMatchRoom;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.Map;

/**
 * @author Gnacik
 *
 * Mode :
 * 		0 - add
 * 		1 - modify
 * 		2 - quit
 */
public class ExManagePartyRoomMember extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final PartyMatchRoom _room;
	private final int _mode;

	public ExManagePartyRoomMember(L2PcInstance player, PartyMatchRoom room, int mode)
	{
		_activeChar = player;
		_room = room;
		_mode = mode;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_mode);
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getName());
		writeD(_activeChar.getActiveClassId());
		writeD(_activeChar.getLevel());
		writeD(MapRegionManager.getInstance().getMapRegion(_activeChar.getLoc()).getBbs());
		if(_room.getOwner().equals(_activeChar))
		{
			writeD(1);
		}
		else
		{
			if(_room.getOwner().isInParty() && _activeChar.isInParty() && _room.getOwner().getParty().getLeaderObjectId() == _activeChar.getParty().getLeaderObjectId())
			{
				writeD(2);
			}
			else
			{
				writeD(0);
			}
		}

		// Пройденные зоны
		Map<Integer, Long> instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(_activeChar.getObjectId());
		writeD(instanceTimes.size());
		instanceTimes.keySet().forEach(this::writeD);
	}
}