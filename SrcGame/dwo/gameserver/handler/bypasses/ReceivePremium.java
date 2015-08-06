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
package dwo.gameserver.handler.bypasses;

import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExGetPremiumItemList;

/**
 * Premium commands handler.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class ReceivePremium extends CommandHandler<String>
{
	@TextCommand
	public boolean receivePremium(BypassHandlerParams params)
	{
		L2PcInstance activeChar = params.getPlayer();
		if(!params.getTarget().isNpc())
		{
			return false;
		}

		if(activeChar.getPremiumItemList().isEmpty())
		{
			activeChar.sendPacket(SystemMessageId.THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND);
			return false;
		}

		activeChar.sendPacket(new ExGetPremiumItemList(activeChar.getPremiumItemList()));

		return true;
	}
}