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
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.player.formation.group.PartyLootType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * Support for /partyinfo command
 *
 * @author Tempy
 * @author Yorie
 */
public class PartyInfo extends CommandHandler<Integer>
{
	@NumericCommand(81)
	public boolean partyInfo(HandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(!activeChar.isInParty())
		{
			activeChar.sendPacket(SystemMessageId.PARTY_INFORMATION);
			activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
			return false;
		}

		L2Party playerParty = activeChar.getParty();
		int memberCount = playerParty.getMemberCount();
		PartyLootType lootDistribution = playerParty.getLootDistribution();
		String partyLeader = playerParty.getMembers().get(0).getName();

		activeChar.sendPacket(SystemMessageId.PARTY_INFORMATION);

		switch(lootDistribution)
		{
			case ITEM_LOOTER:
				activeChar.sendPacket(SystemMessageId.LOOTING_FINDERS_KEEPERS);
				break;
			case ITEM_ORDER:
				activeChar.sendPacket(SystemMessageId.LOOTING_BY_TURN);
				break;
			case ITEM_ORDER_SPOIL:
				activeChar.sendPacket(SystemMessageId.LOOTING_BY_TURN_INCLUDE_SPOIL);
				break;
			case ITEM_RANDOM:
				activeChar.sendPacket(SystemMessageId.LOOTING_RANDOM);
				break;
			case ITEM_RANDOM_SPOIL:
				activeChar.sendPacket(SystemMessageId.LOOTING_RANDOM_INCLUDE_SPOIL);
				break;
		}

		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_LEADER_C1).addString(partyLeader));

		activeChar.sendMessage("Members: " + memberCount + "/7");

		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
		return true;
	}
}
