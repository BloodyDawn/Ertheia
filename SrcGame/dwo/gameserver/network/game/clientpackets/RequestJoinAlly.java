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
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pledge.AskJoinAlliance;

public class RequestJoinAlly extends L2GameClientPacket
{
	private int _id;

	@Override
	protected void readImpl()
	{
		_id = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2PcInstance ob = WorldManager.getInstance().getPlayer(_id);

		if(ob == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}

		if(activeChar.getClan() == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
			return;
		}

		L2Clan clan = activeChar.getClan();
		if(!clan.checkAllyJoinCondition(activeChar, ob))
		{
			return;
		}
		if(!activeChar.getRequest().setRequest(ob, this))
		{
			return;
		}

		ob.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_CLAN_LEADER_OF_S1_REQUESTED_ALLIANCE).addString(activeChar.getClan().getAllyName()).addString(activeChar.getName()));
		AskJoinAlliance aja = new AskJoinAlliance(activeChar.getObjectId(), activeChar.getClan().getName());
		ob.sendPacket(aja);
	}

	@Override
	public String getType()
	{
		return "[C] 82 RequestJoinAlly";
	}
}

