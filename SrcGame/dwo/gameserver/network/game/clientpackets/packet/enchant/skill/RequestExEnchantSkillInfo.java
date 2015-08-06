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
package dwo.gameserver.network.game.clientpackets.packet.enchant.skill;

import dwo.gameserver.datatables.xml.EnchantSkillGroupsTable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.enchant.skill.ExEnchantSkillInfo;

public class RequestExEnchantSkillInfo extends L2GameClientPacket
{
	private int _skillId;
	private int _skillLvl;

	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}

	@Override
	protected void runImpl()
	{
		if(_skillId <= 0 || _skillLvl <= 0) // minimal sanity check
		{
			return;
		}

		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}

		if(activeChar.getLevel() < 76)
		{
			return;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		if(skill == null || skill.getId() != _skillId)
		{
			return;
		}

		if(EnchantSkillGroupsTable.getInstance().getSkillEnchantmentBySkillId(_skillId) == null)
		{
			return;
		}

		int playerSkillLvl = activeChar.getSkillLevel(_skillId);
		if(playerSkillLvl == -1 || playerSkillLvl != _skillLvl)
		{
			return;
		}

		activeChar.sendPacket(new ExEnchantSkillInfo(_skillId, _skillLvl));
	}

	@Override
	public String getType()
	{
		return "[C] D0:0E RequestExEnchantSkillInfo";
	}
}