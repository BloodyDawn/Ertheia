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

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class RequestExOustFromMPCC extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance target = WorldManager.getInstance().getPlayer(_name);
		L2PcInstance activeChar = getClient().getActiveChar();

		if(target != null && target.isInParty() && activeChar.isInParty() && activeChar.getParty().isInCommandChannel() && target.getParty().isInCommandChannel() && activeChar.getParty().getCommandChannel().getLeader().equals(activeChar) && activeChar.getParty().getCommandChannel().equals(target.getParty().getCommandChannel()))
		{
			if(activeChar.equals(target))
			{
				return;
			}

			target.getParty().getCommandChannel().removeParty(target.getParty());

			target.getParty().broadcastPacket(SystemMessageId.DISMISSED_FROM_COMMAND_CHANNEL);

			// check if CC has not been canceled
			if(activeChar.getParty().isInCommandChannel())
			{
				activeChar.getParty().getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_DISMISSED_FROM_COMMAND_CHANNEL).addString(target.getParty().getLeader().getName()));
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:0F RequestExOustFromMPCC";
	}
}