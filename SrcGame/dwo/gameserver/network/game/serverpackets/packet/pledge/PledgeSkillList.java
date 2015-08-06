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

import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PledgeSkillList extends L2GameServerPacket
{
	private final L2Skill[] _skills;
	private final SubPledgeSkill[] _subSkills;

	public PledgeSkillList(L2Clan clan)
	{
		_skills = clan.getAllSkills();
		_subSkills = clan.getAllSubSkills();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_skills.length);
		writeD(_subSkills.length); // squad skill lenght
		for(L2Skill sk : _skills)
		{
			writeD(sk.getDisplayId());
			writeD(sk.getLevel());
		}
		for(SubPledgeSkill sk : _subSkills)
		{
			writeD(sk.subType); // clan Sub-unit types
			writeD(sk.skillId);
			writeD(sk.skillLvl);
		}
	}

	public static class SubPledgeSkill
	{
		int subType;
		int skillId;
		int skillLvl;

		public SubPledgeSkill(int subType, int skillId, int skillLvl)
		{
			this.subType = subType;
			this.skillId = skillId;
			this.skillLvl = skillLvl;
		}
	}
}