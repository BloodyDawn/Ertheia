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
package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.world.zone.L2ZoneType;

/**
 * L2AbnormalZone zones give entering players abnormal effects
 * Default effect is big head
 *
 * @author durgus
 */
public class L2AbnormalZone extends L2ZoneType
{
	private int abnormal = AbnormalEffect.BIG_HEAD.getMask();
	private int special;

	public L2AbnormalZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch(name)
		{
			case "AbnormalMask":
				abnormal = Integer.parseInt(value);
				break;
			case "SpecialMask":
				special = Integer.parseInt(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		character.startAbnormalEffect(abnormal);
		character.startAbnormalEffect(special); //TODO:
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.stopAbnormalEffect(abnormal);
		character.stopAbnormalEffect(special); //TODO:
	}

	@Override
	public void onDieInside(L2Character character)
	{
		onExit(character);
	}

	@Override
	public void onReviveInside(L2Character character)
	{
		onEnter(character);
	}
}
