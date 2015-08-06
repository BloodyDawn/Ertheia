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
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.network.game.serverpackets.CastleSiegeAttackerList;

public class RequestSiegeAttackerList extends L2GameClientPacket
{
	private int _castleId;

	@Override
	protected void readImpl()
	{
		_castleId = readD();
	}

	@Override
	protected void runImpl()
	{
		Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		if(castle != null)
		{
			CastleSiegeAttackerList sal = new CastleSiegeAttackerList(castle);
			sendPacket(sal);
		}
		else
		{
			ClanHallSiegable hall = ClanHallSiegeManager.getInstance().getSiegableHall(_castleId);
			if(hall != null)
			{
				CastleSiegeAttackerList sal = new CastleSiegeAttackerList(hall);
				sendPacket(sal);
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] A2 RequestSiegeAttackerList";
	}
}
