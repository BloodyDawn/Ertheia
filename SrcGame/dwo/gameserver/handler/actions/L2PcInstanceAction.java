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

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.handler.IActionHandler;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;

public class L2PcInstanceAction implements IActionHandler
{
	/**
	 * Manage actions when a player click on this L2PcInstance.<BR><BR>
	 * <p/>
	 * <B><U> Actions on first click on the L2PcInstance (Select it)</U> :</B><BR><BR>
	 * <li>Set the target of the player</li>
	 * <li>Send a ServerMode->Client packet MyTargetSelected to the player (display the select window)</li><BR><BR>
	 * <p/>
	 * <B><U> Actions on second click on the L2PcInstance (Follow it/Attack it/Intercat with it)</U> :</B><BR><BR>
	 * <li>Send a ServerMode->Client packet MyTargetSelected to the player (display the select window)</li>
	 * <li>If target L2PcInstance has a Private Store, notify the player AI with AI_INTENTION_INTERACT</li>
	 * <li>If target L2PcInstance is autoAttackable, notify the player AI with AI_INTENTION_ATTACK</li><BR><BR>
	 * <li>If target L2PcInstance is NOT autoAttackable, notify the player AI with AI_INTENTION_FOLLOW</li><BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : Action, AttackRequest</li><BR><BR>
	 *
	 * @param activeChar The player that start an action on target L2PcInstance
	 */
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if(!EventManager.onAction(activeChar, target.getObjectId()))
		{
			return false;
		}

		// Check if the L2PcInstance is confused
		if(activeChar.isOutOfControl())
		{
			return false;
		}

		if(target instanceof L2PcInstance && !((L2PcInstance) target).isTargetable())
		{
			return false;
		}

		// Aggression target lock effect
		if(activeChar.isLockedTarget() && !activeChar.getLockedTarget().equals(target))
		{
			activeChar.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
			return false;
		}

		// Check if the activeChar already target this L2PcInstance
		if(activeChar.getTarget() != target)
		{
			// Send a ServerMode->Client packet MyTargetSelected to the activeChar
			// The color to display in the select window is White
			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), 0));

			// Set the target of the activeChar
			activeChar.setTarget(target);

			if(!activeChar.equals(target))
			{
				activeChar.sendPacket(new ValidateLocation((L2Character) target));
			}
		}
		else if(interact)
		{
			if(!activeChar.equals(target))
			{
				activeChar.sendPacket(new ValidateLocation((L2Character) target));
			}
			// Check if this L2PcInstance has a Private Store
			if(((L2PcInstance) target).getPrivateStoreType() == PlayerPrivateStoreType.NONE)
			{
				// Check if this L2PcInstance is autoAttackable
				if(target.isAutoAttackable(activeChar))
				{
					// activeChar with lvl < 21 can't attack a cursed weapon holder
					// And a cursed weapon holder  can't attack activeChars with lvl < 21
					if(((L2PcInstance) target).isCursedWeaponEquipped() && activeChar.getLevel() < 21 || activeChar.isCursedWeaponEquipped() && ((L2Character) target).getLevel() < 21)
					{
						activeChar.sendActionFailed();
					}
					else
					{
						if(Config.GEODATA_ENABLED)
						{
							if(GeoEngine.getInstance().canSeeTarget(activeChar, target))
							{
								activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
								activeChar.onActionRequest();
							}
						}
						else
						{
							activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
							activeChar.onActionRequest();
						}
					}
				}
				else
				{
					// This Action Failed packet avoids activeChar getting stuck when clicking three or more times
					activeChar.sendActionFailed();
					if(Config.GEODATA_ENABLED)
					{
						if(GeoEngine.getInstance().canSeeTarget(activeChar, target))
						{
							activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
						}
					}
					else
					{
						activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
					}
				}
			}
			else
			{
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
			}
		}
		return true;
	}

	@Override
	public Class<? extends L2Object> getInstanceType()
	{
		return L2PcInstance.class;
	}
}
