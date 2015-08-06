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

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.network.game.serverpackets.CastleSiegeDefenderList;

public class RequestConfirmSiegeWaitingList extends L2GameClientPacket
{
	private int _approved;
	private int _castleId;
	private int _clanId;

	@Override
	protected void readImpl()
	{
		_castleId = readD();
		_clanId = readD();
		_approved = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		// Check if the player has a clan
		if(activeChar.getClan() == null)
		{
			return;
		}

		Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		if(castle == null)
		{
			return;
		}

		// Check if leader of the clan who owns the castle?
		if(castle.getOwnerId() != activeChar.getClanId() || !activeChar.isClanLeader())
		{
			return;
		}

		L2Clan clan = ClanTable.getInstance().getClan(_clanId);
		if(clan == null)
		{
			return;
		}

		if(!castle.getSiege().getIsRegistrationOver())
		{
			if(_approved == 1)
			{
				if(castle.getSiege().checkIsDefenderWaiting(clan))
				{
					castle.getSiege().approveSiegeDefenderClan(_clanId);
				}
				else
				{
					return;
				}
			}
			else
			{
				if(castle.getSiege().checkIsDefenderWaiting(clan) || castle.getSiege().checkIsDefender(clan))
				{
					castle.getSiege().removeSiegeClan(_clanId);
				}
			}
		}

		//Update the defender list
		activeChar.sendPacket(new CastleSiegeDefenderList(castle));
	}

	@Override
	public String getType()
	{
		return "[C] A5 RequestConfirmSiegeWaitingList";
	}
}
