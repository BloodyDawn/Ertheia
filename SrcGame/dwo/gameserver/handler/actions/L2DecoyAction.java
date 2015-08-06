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
package dwo.gameserver.handler.actions;

import dwo.gameserver.handler.IActionHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Decoy;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;

public class L2DecoyAction implements IActionHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		// Aggression target lock effect
		if(activeChar.isLockedTarget() && !activeChar.getLockedTarget().equals(target))
		{
			activeChar.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
			return false;
		}

		activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), activeChar.getLevel() - ((L2Character) target).getLevel()));
		activeChar.setTarget(target);

		return true;
	}

	@Override
	public Class<? extends L2Object> getInstanceType()
	{
		return L2Decoy.class;
	}
}