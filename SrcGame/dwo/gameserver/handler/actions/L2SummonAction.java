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
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.packet.pet.PetStatusShow;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;
import org.apache.log4j.Level;

public class L2SummonAction implements IActionHandler
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

		if(activeChar.equals(((L2Summon) target).getOwner()) && activeChar.getTarget() == target)
		{
			activeChar.sendPacket(new PetStatusShow((L2Summon) target));
			activeChar.sendActionFailed();
		}
		else if(activeChar.getTarget() != target)
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "new target selected:" + target.getObjectId());
			}

			activeChar.sendPacket(new ValidateLocation((L2Character) target));

			// sends HP/MP status of the summon to other characters
			StatusUpdate su = new StatusUpdate(target);
			su.addAttribute(StatusUpdate.CUR_HP, (int) ((L2Character) target).getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, ((L2Character) target).getMaxHp());
			activeChar.sendPacket(su);

			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), activeChar.getLevel() - ((L2Character) target).getLevel()));

			activeChar.setTarget(target);

		}
		else if(interact)
		{
			activeChar.sendPacket(new ValidateLocation((L2Character) target));
			if(target.isAutoAttackable(activeChar))
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
			else
			{
				// This Action Failed packet avoids activeChar getting stuck
				// when clicking three or more times
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
		return true;
	}

	@Override
	public Class<? extends L2Object> getInstanceType()
	{
		return L2Summon.class;
	}
}
