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
package dwo.gameserver.network.game.clientpackets.packet.pledge.clan;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeReceivePowerInfo;

/**
 * Format: (ch) dS
 * @author  -Wooden-
 *
 */
public class RequestPledgeMemberPowerInfo extends L2GameClientPacket
{
	private int _pledgeId;
	private String _player;

	@Override
	protected void readImpl()
	{
		_pledgeId = readD();
		_player = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		//do we need powers to do that??
		L2Clan clan = activeChar.getClan();
		if(clan == null)
		{
			return;
		}
		L2ClanMember member = clan.getClanMember(_player);
		if(member == null)
		{
			return;
		}
		activeChar.sendPacket(new PledgeReceivePowerInfo(member));
	}

	@Override
	public String getType()
	{
		return "[C] D0:14 RequestPledgeMemberPowerInfo";
	}
}