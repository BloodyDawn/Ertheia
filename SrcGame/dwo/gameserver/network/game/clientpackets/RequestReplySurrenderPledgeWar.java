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
import dwo.gameserver.model.player.formation.clan.ClanWar;
import org.apache.log4j.Level;

public class RequestReplySurrenderPledgeWar extends L2GameClientPacket
{
	private String _reqName;
	private int _answer;

	@Override
	protected void readImpl()
	{
		_reqName = readS();
		_answer = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		L2PcInstance requestor = activeChar.getActiveRequester();
		if(requestor == null)
		{
			return;
		}

		if(_answer == 1)
		{
			requestor.deathPenalty(false, false, false);
			ClanWar war = requestor.getClan().getClanWar(activeChar.getClan());
			if(war != null)
			{
				war.setPeriod(ClanWar.ClanWarPeriod.PEACE);
			}
		}
		else
		{
			_log.log(Level.WARN, getClass().getSimpleName() + ": Missing implementation for answer: " + _answer + " and name: " + _reqName + '!');
		}
		activeChar.onTransactionRequest(requestor);
	}

	@Override
	public String getType()
	{
		return "[C] 52 RequestReplySurrenderPledgeWar";
	}
}