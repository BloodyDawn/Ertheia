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
import dwo.gameserver.model.player.formation.group.PartyLootType;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PartySmallWindowAdd extends L2GameServerPacket
{
	private final L2PcInstance _member;
	private final int _leaderId;
	private final PartyLootType _distribution;

	public PartySmallWindowAdd(L2PcInstance member, L2Party party)
	{
		_member = member;
		_leaderId = party.getLeaderObjectId();
		_distribution = party.getLootDistribution();
	}

	//GOD c ddd S dddddddddddddd
	@Override
	protected void writeImpl()
	{
		writeD(_leaderId); // c3
		writeD(_distribution.ordinal());//writeD(0x04); ?? //c3
		writeD(_member.getObjectId());
		writeS(_member.getName());
		writeD((int) _member.getCurrentCp()); //c4
		writeD(_member.getMaxCp()); //c4
		writeD(_member.getVitalityDataForCurrentClassIndex().getVitalityPoints()); // GoD Vitality
		writeD((int) _member.getCurrentHp());
		writeD(_member.getMaxVisibleHp());
		writeD((int) _member.getCurrentMp());
		writeD(_member.getMaxMp());
		writeC(_member.getLevel());
		writeH(_member.getClassId().getId());
		writeC(_member.getAppearance().getSex() ? 1 : 0);
		writeH(_member.getRace().ordinal());
	}
}
