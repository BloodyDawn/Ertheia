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
import dwo.gameserver.model.player.formation.group.L2CommandChannel;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.party.ExMultiPartyCommandChannelInfo;

/**
 * Command channel commands.
 *
 * @author Chris
 * @author Yorie
 */
public class Channel extends CommandHandler<Integer>
{
	@NumericCommand(93)
	public boolean channelDelete(HandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(activeChar.isInParty())
		{
			if(activeChar.getParty().isLeader(activeChar) && activeChar.getParty().isInCommandChannel() && activeChar.getParty().getCommandChannel().getLeader().equals(activeChar))
			{
				L2CommandChannel channel = activeChar.getParty().getCommandChannel();
				channel.broadcastMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED);
				channel.disbandChannel();
				return true;
			}
		}
		return true;
	}

	@NumericCommand(96)
	public boolean channelLeave(HandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(!activeChar.isInParty() || !activeChar.getParty().isLeader(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_LEAVE_CHANNEL);
			return false;
		}

		if(activeChar.getParty().isInCommandChannel())
		{
			L2CommandChannel channel = activeChar.getParty().getCommandChannel();
			L2Party party = activeChar.getParty();
			channel.removeParty(party);
			party.getLeader().sendPacket(SystemMessageId.LEFT_COMMAND_CHANNEL);

			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_LEFT_COMMAND_CHANNEL);
			sm.addPcName(party.getLeader());
			channel.broadcastPacket(sm);
			return true;
		}

		return true;
	}

	@NumericCommand(97)
	public boolean channelListUpdate(HandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(activeChar.getParty() == null || activeChar.getParty().getCommandChannel() == null)
		{
			return false;
		}

		L2CommandChannel channel = activeChar.getParty().getCommandChannel();
		activeChar.sendPacket(new ExMultiPartyCommandChannelInfo(channel));
		return true;
	}
}
