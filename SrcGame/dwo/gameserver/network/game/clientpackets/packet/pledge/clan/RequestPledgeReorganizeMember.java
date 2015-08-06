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
import dwo.gameserver.util.network.BaseRecievePacket;

/**
 * Format: (ch) dSdS
 * @author  -Wooden-
 */
public class RequestPledgeReorganizeMember extends L2GameClientPacket
{
	private int _isMemberSelected;
	private String _memberName;
	private int _newPledgeType;
	private String _selectedMember;

	@Override
	protected void readImpl()
	{
		_isMemberSelected = readD();
		_memberName = readS();
		_newPledgeType = readD();
		_selectedMember = readS();
	}

	/**
	 * @see BaseRecievePacket.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		if(_isMemberSelected == 0)
		{
			return;
		}

		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2Clan clan = activeChar.getClan();
		if(clan == null)
		{
			return;
		}

		if((activeChar.getClanPrivileges() & L2Clan.CP_CL_MANAGE_RANKS) != L2Clan.CP_CL_MANAGE_RANKS)
		{
			return;
		}

		L2ClanMember member1 = clan.getClanMember(_memberName);
		if(member1 == null || member1.getObjectId() == clan.getLeaderId())
		{
			return;
		}

		L2ClanMember member2 = clan.getClanMember(_selectedMember);
		if(member2 == null || member2.getObjectId() == clan.getLeaderId())
		{
			return;
		}

		int oldPledgeType = member1.getPledgeType();
		if(oldPledgeType == _newPledgeType)
		{
			return;
		}

		member1.setPledgeType(_newPledgeType);
		member2.setPledgeType(oldPledgeType);
		clan.broadcastClanStatus();
	}

	/**
	 * @see dwo.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[C] D0:2C RequestPledgeReorganizeMember";
	}
}
