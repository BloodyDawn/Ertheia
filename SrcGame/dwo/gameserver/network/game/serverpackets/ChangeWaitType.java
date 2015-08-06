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

import dwo.gameserver.model.actor.L2Character;

public class ChangeWaitType extends L2GameServerPacket
{
	public static final int WT_SITTING = 0;
	public static final int WT_STANDING = 1;
	public static final int WT_START_FAKEDEATH = 2;
	public static final int WT_STOP_FAKEDEATH = 3;
	private int _charObjId;
	private int _moveType;
	private int _x;
	private int _y;
	private int _z;

	public ChangeWaitType(L2Character character, int newMoveType)
	{
		_charObjId = character.getObjectId();
		_moveType = newMoveType;

		_x = character.getX();
		_y = character.getY();
		_z = character.getZ();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		writeD(_moveType);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
