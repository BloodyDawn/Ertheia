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

import dwo.gameserver.handler.AdminCommandHandler;
import dwo.gameserver.handler.IActionHandler;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;

public class L2SummonActionShift implements IActionHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if(activeChar.isGM())
		{
			if(!activeChar.getTarget().equals(target))
			{

				// Send a ServerMode->Client packet MyTargetSelected to the
				// L2PcInstance activeChar
				activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), 0));

				// Set the target of the L2PcInstance activeChar
				activeChar.setTarget(target);
			}

			// Send a ServerMode->Client packet ValidateLocation to correct the
			// L2PcInstance position and heading on the client
			activeChar.sendPacket(new ValidateLocation((L2Character) target));

			IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler("admin_summon_info");
			if(ach != null)
			{
				ach.useAdminCommand("admin_summon_info", activeChar);
			}
		}
		return true;
	}

	@Override
	public Class<? extends L2Object> getInstanceType()
	{
		return L2Summon.class;
	}
}