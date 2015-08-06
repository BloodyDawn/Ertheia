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

import dwo.gameserver.network.game.serverpackets.packet.pledge.AllianceCrest;

public class RequestAllyCrest extends L2GameClientPacket
{
	private int _crestId;

	/**
	 * packet type id 0x88 format: cd
	 */
	@Override
	protected void readImpl()
	{
		_crestId = readD();
	}

	@Override
	protected void runImpl()
	{
		sendPacket(new AllianceCrest(_crestId));
	}

	@Override
	public String getType()
	{
		return "[C] 88 RequestAllyCrest";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
