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

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;

/**
 *
 * @author KenM
 */
public class RequestDispel extends L2GameClientPacket
{
	private int _objectId;
	private int _skillId;
	private int _skillLevel;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_skillId = readD();
		_skillLevel = readD();
	}

	@Override
	protected void runImpl()
	{
		if(_skillId <= 0 || _skillLevel <= 0)
		{
			return;
		}

		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLevel);
		if(skill == null)
		{
			return;
		}
		if(!skill.canBeDispeled() || skill.isStayAfterDeath() || skill.isDebuff())
		{
			return;
		}
		// При отсутствии транформации у скила, лист содержит только один "0"
		if(skill.getTransformId().getFirst() != 0)
		{
			return;
		}
		if(skill.isDance() && !Config.DANCE_CANCEL_BUFF)
		{
			return;
		}
		if(activeChar.getObjectId() == _objectId)
		{
			activeChar.stopSkillEffects(_skillId);
		}
		else
		{
			if(!activeChar.getPets().isEmpty())
			{
				activeChar.getPets().stream().filter(pet -> pet.getObjectId() == _objectId).forEach(pet -> pet.stopSkillEffects(_skillId));
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:4E RequestDispel";
	}
}
