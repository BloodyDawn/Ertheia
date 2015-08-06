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
package dwo.gameserver.handler;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 *
 * @author UnAfraid
 */
public interface ITargetTypeHandler
{
	Logger _log = LogManager.getLogger(ITargetTypeHandler.class);

	L2Object[] _emptyTargetList = new L2Object[0];

	L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target);

	Enum<L2TargetType> getTargetType();
}
