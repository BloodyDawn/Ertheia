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
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.network.game.serverpackets.CastleSiegeDefenderList;

public class RequestSiegeDefenderList extends L2GameClientPacket
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
		if(castle == null)
		{
			return;
		}
		CastleSiegeDefenderList sdl = new CastleSiegeDefenderList(castle);
		sendPacket(sdl);
	}

	@Override
	public String getType()
	{
		return "[C] A3 RequestSiegeDefenderList";
	}
}
