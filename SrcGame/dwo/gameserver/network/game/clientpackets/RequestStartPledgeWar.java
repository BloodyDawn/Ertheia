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
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.ClanWar;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class RequestStartPledgeWar extends L2GameClientPacket
{
	private String _pledgeName;
	private L2Clan _clan;
	private L2PcInstance player;

	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}

	@Override
	protected void runImpl()
	{
		player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		_clan = player.getClan();
		if(_clan == null)
		{
			return;
		}

		L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);

		if(!_clan.checkClanWarDeclareCondition(player, clan))
		{
			return;
		}

		ClanWar war = _clan.getClanWar(clan);

		if(war != null)
		{
			if(war.getPeriod() == ClanWar.ClanWarPeriod.PEACE)
			{
				player.sendPacket(SystemMessage.getSystemMessage(247).addString(clan.getName()));
			}
			else
			{
				war.accept(_clan);
			}
		}
		else
		{
			new ClanWar(player.getClanId(), clan.getClanId(), ClanWar.ClanWarPeriod.NEW, (int) (System.currentTimeMillis() / 1000), 0, 0, 0);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 4D RequestStartPledgewar";
	}
}
