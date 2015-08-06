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

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.player.formation.group.PartyLootType;

/**
 *
 * @author JIV
 */
public class RequestPartyLootingModify extends L2GameClientPacket
{
	private byte _mode;

	@Override
	protected void readImpl()
	{
		_mode = (byte) readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(_mode < 0 || _mode > PartyLootType.ITEM_ORDER_SPOIL.ordinal())
		{
			return;
		}
		L2Party party = activeChar.getParty();
		if(party == null || !party.isLeader(activeChar) || _mode == party.getLootDistribution().ordinal())
		{
			return;
		}
		party.requestLootChange(PartyLootType.values()[_mode]);
	}

	@Override
	public String getType()
	{
		return "[C] D0:78 RequestPartyLootModification";
	}
}
