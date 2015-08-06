/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.player.formation.group.PartyLootType;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PartySmallWindowAll extends L2GameServerPacket
{
	private L2Party _party;
	private L2PcInstance _exclude;
	private int _LeaderOID;
	private PartyLootType _dist;

	public PartySmallWindowAll(L2PcInstance exclude, L2Party party)
	{
		_exclude = exclude;
		_party = party;
		_LeaderOID = _party.getLeaderObjectId();
		_dist = _party.getLootDistribution();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_LeaderOID);
		writeD(_dist.ordinal());
		writeD(_party.getMemberCount() - 1);

		// c4
		// c4
		//GOD Vitality
		// TODO: Пол персонажа
		// T2.3
		// T2.3
		// кол-во вызванных петов. Нужен новый метод который будет возвращать кол-во вызванных петов.
		_party.getMembers().stream().filter(member -> member != null && !member.equals(_exclude)).forEach(member -> {
			writeD(member.getObjectId());
			writeS(member.getName());

			writeD((int) member.getCurrentCp()); // c4
			writeD(member.getMaxCp()); // c4
			writeD((int) member.getCurrentHp());
			writeD(member.getMaxVisibleHp());
			writeD((int) member.getCurrentMp());
			writeD(member.getMaxMp());
			writeD(member.getVitalityDataForCurrentClassIndex().getVitalityPoints()); //GOD Vitality
			writeD(member.getLevel());
			writeD(member.getClassId().getId());
			writeD(0x00); // TODO: Пол персонажа
			writeD(member.getRace().ordinal());
			writeD(0x00); // T2.3
			writeD(0x00); // T2.3
			writeD(0x00);
			if(member.getPets().isEmpty())
			{
				writeD(0x00);
			}
			else
			{
				writeD(member.getPets().size());   // кол-во вызванных петов. Нужен новый метод который будет возвращать кол-во вызванных петов.
				for(L2Summon pet : member.getPets())
				{
					writeD(pet.getObjectId());
					writeD(pet.getNpcId() + 1000000);
					writeD(pet.getSummonType());
					writeS(pet.getName());
					writeD((int) pet.getCurrentHp());
					writeD(pet.getMaxHp());
					writeD((int) pet.getCurrentMp());
					writeD(pet.getMaxMp());
					writeD(pet.getLevel());
				}
			}
		});
	}
}
