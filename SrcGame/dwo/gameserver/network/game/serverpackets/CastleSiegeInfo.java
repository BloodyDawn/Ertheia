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
package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.clanhall.ClanHall;
import org.apache.log4j.Level;

import java.util.Calendar;

/**
 * Shows the CastleSiegeEngine Info<BR>
 * <BR>
 * packet type id 0xc9<BR>
 * format: cdddSSdSdd<BR>
 * <BR>
 * c = c9<BR>
 * d = CastleID<BR>
 * d = Show Owner Controls (0x00 default || >=0x02(mask?) owner)<BR>
 * d = Owner ClanID<BR>
 * S = Owner ClanName<BR>
 * S = Owner Clan LeaderName<BR>
 * d = Owner AllyID<BR>
 * S = Owner AllyName<BR>
 * d = current time (seconds)<BR>
 * d = CastleSiegeEngine time (seconds) (0 for selectable)<BR>
 * d = (UNKNOW) CastleSiegeEngine Time Select Related?
 *
 * @author KenM
 */
public class CastleSiegeInfo extends L2GameServerPacket
{
	private Castle _castle;
	private ClanHall _hall;

	public CastleSiegeInfo(Castle castle)
	{
		_castle = castle;
	}

	public CastleSiegeInfo(ClanHall hall)
	{
		_hall = hall;
	}

	@Override
	protected void writeImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(_castle != null)
		{
			writeD(_castle.getCastleId());

			int ownerId = _castle.getOwnerId();

			writeD(ownerId == activeChar.getClanId() && activeChar.isClanLeader() ? 0x01 : 0x00);
			writeD(ownerId);
			if(ownerId > 0)
			{
				L2Clan owner = ClanTable.getInstance().getClan(ownerId);
				if(owner != null)
				{
					writeS(owner.getName());        // Clan Name
					writeS(owner.getLeaderName());  // Clan Leader Name
					writeD(owner.getAllyId());      // Ally ID
					writeS(owner.getAllyName());    // Ally Name
				}
				else
				{
					_log.log(Level.WARN, "Null owner for castle: " + _castle.getName());
				}
			}
			else
			{
				writeS("");  // Clan Name
				writeS("");     // Clan Leader Name
				writeD(0);      // Ally ID
				writeS("");     // Ally Name
			}

			writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000));
			writeD((int) (_castle.getSiege().getSiegeDate().getTimeInMillis() / 1000));
			writeD(0x00); //number of choices?
		}
		else
		{
			writeD(_hall.getId());

			int ownerId = _hall.getOwnerId();

			writeD(ownerId == activeChar.getClanId() && activeChar.isClanLeader() ? 0x01 : 0x00);
			writeD(ownerId);
			if(ownerId > 0)
			{
				L2Clan owner = ClanTable.getInstance().getClan(ownerId);
				if(owner != null)
				{
					writeS(owner.getName());        // Clan Name
					writeS(owner.getLeaderName());  // Clan Leader Name
					writeD(owner.getAllyId());      // Ally ID
					writeS(owner.getAllyName());    // Ally Name
				}
				else
				{
					_log.log(Level.WARN, "Null owner for siegable hall: " + _hall.getName());
				}
			}
			else
			{
				writeS("");  // Clan Name
				writeS("");     // Clan Leader Name
				writeD(0);      // Ally ID
				writeS("");     // Ally Name
			}

			writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000));
			writeD((int) (ClanHallSiegeManager.getInstance().getSiegableHall(_hall.getId()).getNextSiegeTime() / 1000));
			writeD(0x00); //number of choices?
		}
	}
}
