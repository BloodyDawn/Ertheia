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

import dwo.gameserver.cache.CrestCache;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.pledge.ExPledgeEmblem;

/**
 * Fomat : chd
 * c: (id) 0xD0
 * h: (subid) 0x10
 * d: the crest id
 *
 * This is a trigger
 * @author -Wooden-
 *
 */
public class RequestExPledgeCrestLarge extends L2GameClientPacket
{
	private int _pledgeId;
	private int _crestId;

	@Override
	protected void readImpl()
	{
		_crestId = readD();
		_pledgeId = readD();
	}

	@Override
	protected void runImpl()
	{
		byte[][] data = CrestCache.getInstance().getPledgeCrestLarge(_crestId);

		if(data != null)
		{
			int index = 0;
			for(byte[] trimmedData : data)
			{
				sendPacket(new ExPledgeEmblem(_pledgeId, _crestId, index, trimmedData));
				index++;

				if(index > 4)
				{
					index = 0;
				}
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:10 RequestExPledgeCrestLarge";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}