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

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.Location;

public class ObserverEnd extends L2GameServerPacket
{
	private L2PcInstance _activeChar;

	public ObserverEnd(L2PcInstance observer)
	{
		_activeChar = observer;
	}

	@Override
	protected void writeImpl()
	{
		Location loc = _activeChar.getLocationController().getMemorizedLocation();

		if(loc != null)
		{
			writeD(loc.getX());
			writeD(loc.getY());
			writeD(loc.getZ());
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
	}
}
