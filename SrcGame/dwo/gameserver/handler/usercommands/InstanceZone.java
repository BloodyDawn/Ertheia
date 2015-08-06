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
package dwo.gameserver.handler.usercommands;

import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.NumericCommand;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

import java.util.Map;

/**
 * Instance zone info command handler.
 *
 * @author nille02
 * @author Yorie
 */
public class InstanceZone extends CommandHandler<Integer>
{
	@NumericCommand(114)
	public boolean instancZone(HandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(activeChar);
		if(world != null && world.templateId >= 0)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_CURRENTLY_INUSE_S1);
			sm.addString(InstanceManager.getInstance().getInstanceIdName(world.templateId));
			activeChar.sendPacket(sm);
		}

		Map<Integer, Long> instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(activeChar.getObjectId());
		boolean firstMessage = true;

		if(instanceTimes != null)
		{
			for(Map.Entry<Integer, Long> integerLongEntry : instanceTimes.entrySet())
			{
				long remainingTime = (integerLongEntry.getValue() - System.currentTimeMillis()) / 1000;
				if(remainingTime > 60)
				{
					if(firstMessage)
					{
						firstMessage = false;
						activeChar.sendPacket(SystemMessageId.INSTANCE_ZONE_TIME_LIMIT);
					}
					int hours = (int) (remainingTime / 3600);
					int minutes = (int) (remainingTime % 3600 / 60);
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
					sm.addString(InstanceManager.getInstance().getInstanceIdName(integerLongEntry.getKey()));
					sm.addNumber(hours);
					sm.addNumber(minutes);
					activeChar.sendPacket(sm);
				}
				else
				{
					InstanceManager.getInstance().deleteInstanceTime(activeChar.getObjectId(), integerLongEntry.getKey());
				}
			}
		}

		if(firstMessage)
		{
			activeChar.sendPacket(SystemMessageId.NO_INSTANCEZONE_TIME_LIMIT);
		}

		return true;
	}
}
