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

import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.CastleSiegeInfo;

public class RequestJoinSiege extends L2GameClientPacket
{
	private int _castleId;
	private int _isAttacker;
	private int _isJoining;

	@Override
	protected void readImpl()
	{
		_castleId = readD();
		_isAttacker = readD();
		_isJoining = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if((activeChar.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) != L2Clan.CP_CS_MANAGE_SIEGE)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		L2Clan clan = activeChar.getClan();
		if(clan == null)
		{
			return;
		}

		Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		if(castle != null)
		{
			if(_isJoining == 1)
			{
				if(System.currentTimeMillis() < clan.getDissolvingExpiryTime())
				{
					activeChar.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
					return;
				}
				if(_isAttacker == 1)
				{
					castle.getSiege().registerAttacker(activeChar);
				}
				else
				{
					castle.getSiege().registerDefender(activeChar);
				}
			}
			else
			{
				castle.getSiege().removeSiegeClan(activeChar);
			}
			castle.getSiege().listRegisteredClans(activeChar);
		}

		ClanHallSiegable hall = ClanHallSiegeManager.getInstance().getSiegableHall(_castleId);
		if(hall != null)
		{
			if(_isJoining == 1)
			{
				if(System.currentTimeMillis() < clan.getDissolvingExpiryTime())
				{
					activeChar.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
					return;
				}
				ClanHallSiegeManager.getInstance().registerClan(clan, hall, activeChar);
			}
			else
			{
				ClanHallSiegeManager.getInstance().unRegisterClan(clan, hall);
			}
			activeChar.sendPacket(new CastleSiegeInfo(hall));
		}
	}

	@Override
	public String getType()
	{
		return "[C] A4 RequestJoinSiege";
	}
}