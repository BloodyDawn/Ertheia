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

/**
 * Format: (c) ddd
 * d: dx
 * d: dy
 * d: dz
 *
 * @author -Wooden-
 */
public class MoveWithDelta extends L2GameClientPacket
{
	private int _dx;
	private int _dy;
	private int _dz;

	@Override
	protected void readImpl()
	{
		_dx = readD();
		_dy = readD();
		_dz = readD();
	}

	@Override
	protected void runImpl()
	{
		// TODO this
	}

	@Override
	public String getType()
	{
		return "[C] 0x41 MoveWithDelta";
	}
}
