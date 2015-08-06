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
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/*
 * Update: Keiichi 24.05.2012
 * Rev: 466 Glory Days
 */
public class PartyMemberPosition extends L2GameServerPacket
{
	private final L2Party _party;

	public PartyMemberPosition(L2Party party)
	{
		_party = party;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_party.getMemberCount());
		for(L2PcInstance _member : _party.getMembers())
		{
			writeD(_member.getObjectId());
			writeD(_member.getX());
			writeD(_member.getY());
			writeD(_member.getZ());
		}
	}
}
