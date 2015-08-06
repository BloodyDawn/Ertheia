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
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.Level;

public class RequestSurrenderPledgeWar extends L2GameClientPacket
{
	private String _pledgeName;
	private L2Clan _clan;
	private L2PcInstance _activeChar;

	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}

	@Override
	protected void runImpl()
	{
		_activeChar = getClient().getActiveChar();
		if(_activeChar == null)
		{
			return;
		}
		_clan = _activeChar.getClan();
		if(_clan == null)
		{
			return;
		}
		L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);

		if(clan == null)
		{
			_activeChar.sendPacket(SystemMessageId.getSystemMessageId(1568));
			_activeChar.sendActionFailed();
			return;
		}

		_log.log(Level.INFO, "RequestSurrenderPledgeWar by " + getClient().getActiveChar().getClan().getName() + " with " + _pledgeName);

		if(!_clan.isAtWarWith(clan.getClanId()))
		{
			_activeChar.sendMessage("You aren't at war with this clan.");
			_activeChar.sendActionFailed();
			return;
		}

		_activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN).addString(_pledgeName));
		_activeChar.deathPenalty(false, false, false);
		ClanWar war = _clan.getClanWar(clan);
		if(war != null)
		{
			war.setPeriod(ClanWar.ClanWarPeriod.PEACE);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 51 RequestSurrenderPledgeWar";
	}
}