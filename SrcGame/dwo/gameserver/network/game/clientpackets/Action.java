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
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExAbnormalStatusUpdateFromTarget;
import org.apache.log4j.Level;

public class Action extends L2GameClientPacket
{
	// cddddc
	private int _objectId;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _actionId;

	@Override
	protected void readImpl()
	{
		_objectId = readD(); // Target object Identifier
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_actionId = readC(); // Action identifier : 0-Simple click, 1-Shift click
	}

	@Override
	protected void runImpl()
	{
		// Get the current L2PcInstance of the player
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}

		if(activeChar.getObserverController().isObserving())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
			activeChar.sendActionFailed();
			return;
		}

		L2Object obj;

		if(activeChar.getTargetId() == _objectId)
		{
			obj = activeChar.getTarget();
		}
		else
		{
			obj = activeChar.isInAirShip() && activeChar.getAirShip().getHelmObjectId() == _objectId ? activeChar.getAirShip() : WorldManager.getInstance().findObject(_objectId);
		}

		// If object requested does not exist, add warn msg into logs
		if(obj == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		// Players can't interact with objects in the other instances
		// except from multiverse
		if(obj.getInstanceId() != activeChar.getInstanceId() && activeChar.getInstanceId() != -1)
		{
			activeChar.sendActionFailed();
			return;
		}

		// Only GMs can directly interact with invisible characters
		if(obj instanceof L2PcInstance && ((L2PcInstance) obj).getAppearance().getInvisible() && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}

		// Check if the target is valid, if the player haven't a shop or isn't the requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...)
		if(activeChar.getActiveRequester() == null)
		{
			switch(_actionId)
			{
				case 0:
					obj.onAction(activeChar);
					if(obj instanceof L2Character)
					{
						activeChar.sendPacket(new ExAbnormalStatusUpdateFromTarget((L2Character) obj, activeChar.isAwakened()));
					}
					break;
				case 1:
					if(!activeChar.isGM() && !(obj.isNpc() && Config.ALT_GAME_VIEWNPC))
					{
						obj.onAction(activeChar, false);
					}
					else
					{
						obj.onActionShift(activeChar);
					}
					break;
				default:
					// Invalid action detected (probably client cheating), log this
					_log.log(Level.WARN, "Character: " + activeChar.getName() + " requested invalid action: " + _actionId);
					activeChar.sendActionFailed();
					break;
			}
		}
		else
		{
			// Actions prohibited when in trade
			activeChar.sendActionFailed();
		}
	}

	@Override
	public String getType()
	{
		return "[C] 04 Action";
	}
}
