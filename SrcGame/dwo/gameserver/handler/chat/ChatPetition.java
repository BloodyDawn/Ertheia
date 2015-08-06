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

import dwo.gameserver.handler.ChatHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.NumericCommand;
import dwo.gameserver.instancemanager.PetitionManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 * Petition chat handler.
 *
 * @author durgus
 * @author Yorie
 */
public class ChatPetition extends CommandHandler<Integer>
{
	@NumericCommand(6)
	public boolean petitionChat(ChatHandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();
		if(!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_IN_PETITION_CHAT);
			return false;
		}

		PetitionManager.getInstance().sendActivePetitionMessage(activeChar, params.getMessage());

		return true;
	}

	@NumericCommand(7)
	public boolean petitionChatAlias(ChatHandlerParams<Integer> params)
	{
		return petitionChat(params);
	}
}