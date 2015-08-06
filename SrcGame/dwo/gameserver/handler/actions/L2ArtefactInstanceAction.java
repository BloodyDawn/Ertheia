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
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2ArtefactInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;

public class L2ArtefactInstanceAction implements IActionHandler
{
	/**
	 * Manage actions when a player click on the L2ArtefactInstance.<BR>
	 * <BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the L2NpcInstance as target of the L2PcInstance player (if
	 * necessary)</li> <li>Send a ServerMode->Client packet MyTargetSelected to the
	 * L2PcInstance player (display the select window)</li> <li>Send a
	 * ServerMode->Client packet ValidateLocation to correct the L2NpcInstance
	 * position and heading on the client</li><BR>
	 * <BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packet : Action, AttackRequest</li><BR>
	 * <BR>
	 */
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if(!((L2Npc) target).canTarget(activeChar))
		{
			return false;
		}

		if(!activeChar.getTarget().equals(target))
		{
			// Send a ServerMode->Client packet MyTargetSelected to the L2PcInstance activeChar
			MyTargetSelected my = new MyTargetSelected(target.getObjectId(), 0);
			activeChar.sendPacket(my);

			// Set the target of the L2PcInstance activeChar
			activeChar.setTarget(target);

			// Send a ServerMode->Client packet ValidateLocation to correct the L2ArtefactInstance position and heading on the client
			activeChar.sendPacket(new ValidateLocation((L2Character) target));
		}
		else if(interact)
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if(!((L2Npc) target).canInteract(activeChar))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
			}
		}
		return true;
	}

	@Override
	public Class<? extends L2Object> getInstanceType()
	{
		return L2ArtefactInstance.class;
	}
}