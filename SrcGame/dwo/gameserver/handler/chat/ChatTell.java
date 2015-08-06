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
package dwo.gameserver.handler.chat;

import dwo.config.Config;
import dwo.gameserver.handler.ChatHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.NumericCommand;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.Say2;

/**
 * Tell (private message) chat handler.
 *
 * @author durgus
 * @author Yorie
 */
public class ChatTell extends CommandHandler<Integer>
{
	@NumericCommand(2)
	public boolean tellChat(ChatHandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(activeChar.isChatBanned())
		{
			activeChar.sendPacket(SystemMessageId.CHATTING_PROHIBITED);
			return false;
		}

		if(Config.JAIL_DISABLE_CHAT && activeChar.isInJail() && !activeChar.isGM())
		{
			activeChar.sendPacket(SystemMessageId.CHATTING_PROHIBITED);
			return false;
		}

		if(params.getTarget() == null)
		{
			return false;
		}

		L2PcInstance receiver;

		receiver = WorldManager.getInstance().getPlayer(params.getTarget());

        if(receiver != null && !receiver.isSilenceMode(activeChar.getObjectId()))
		{
			if(Config.JAIL_DISABLE_CHAT && receiver.isInJail() && !activeChar.isGM())
			{
				activeChar.sendMessage("Player is in jail.");
				return false;
			}
			if(receiver.isChatBanned())
			{
				activeChar.sendPacket(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
				return false;
			}
			if(receiver.getClient() == null || receiver.getClient().isDetached())
			{
				activeChar.sendMessage("Player is in offline mode.");
				return false;
			}
			if(RelationListManager.getInstance().isBlocked(receiver, activeChar))
			{
				activeChar.sendPacket(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
			}
			else
			{
				// Allow reciever to send PMs to this char, which is in silence mode.
				if(Config.SILENCE_MODE_EXCLUDE && activeChar.isSilenceMode())
				{
					activeChar.addSilenceModeExcluded(receiver.getObjectId());
				}
				receiver.sendPacket(new Say2(activeChar, receiver, activeChar.getName(), ChatType.values()[params.getCommand()], params.getMessage()));
				activeChar.sendPacket(new Say2(activeChar, receiver, activeChar.getName(), ChatType.values()[params.getCommand()], params.getMessage()));
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
		}

		return true;
	}
}