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
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.network.game.serverpackets.ConfirmDlg;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.StaticObject;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;

public class L2DoorInstanceAction implements IActionHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		// Check if the L2PcInstance already target the L2NpcInstance
		if(!activeChar.getTarget().equals(target))
		{

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance activeChar
			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), 0));

			// Set the target of the L2PcInstance activeChar
			activeChar.setTarget(target);

			StaticObject su;
			L2DoorInstance door = (L2DoorInstance) target;
			// send HP amount if doors are inside castle/fortress zone
			su = door.getCastle() != null && door.getCastle().getCastleId() > 0 || door.getFort() != null && door.getFort().getFortId() > 0 || door.getClanHall() != null && door.getClanHall().isSiegableHall() && !door.isCommanderDoor() ? new StaticObject(door, true) : new StaticObject(door, false);

			activeChar.sendPacket(su);

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			activeChar.sendPacket(new ValidateLocation(door));
		}
		else if(interact)
		{
			L2DoorInstance door = (L2DoorInstance) target;
			//            MyTargetSelected my = new MyTargetSelected(getObjectId(), activeChar.getLevel());
			//            activeChar.sendPacket(my);
			if(target.isAutoAttackable(activeChar))
			{
				if(Math.abs(activeChar.getZ() - target.getZ()) < 400) // this max heigth difference might need some tweaking
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			}
			else if(activeChar.getClan() != null && door.getClanHall() != null && activeChar.getClanId() == door.getClanHall().getOwnerId())
			{
				if(!door.isInsideRadius(activeChar, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
				}
				else if(!door.getClanHall().isSiegableHall() || !((ClanHallSiegable) door.getClanHall()).isInSiege())
				{
					activeChar.gatesRequest(door);
					if(door.isOpened())
					{
						activeChar.sendPacket(new ConfirmDlg(1141));
					}
					else
					{
						activeChar.sendPacket(new ConfirmDlg(1140));
					}
				}
			}
			else if(activeChar.getClan() != null && door.getFort() != null && activeChar.getClan().equals(door.getFort().getOwnerClan()) && door.isUnlockable() && !door.getFort().getSiege().isInProgress())
			{
				if(((L2Character) target).isInsideRadius(activeChar, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					activeChar.gatesRequest(door);
					if(door.isOpened())
					{
						activeChar.sendPacket(new ConfirmDlg(1141));
					}
					else
					{
						activeChar.sendPacket(new ConfirmDlg(1140));
					}
				}
				else
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
				}
			}
		}
		return true;
	}

	@Override
	public Class<? extends L2Object> getInstanceType()
	{
		return L2DoorInstance.class;
	}
}
