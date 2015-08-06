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
package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * Dialog with input field<br>
 * type 0 = char name (Selection screen)<br>
 * type 1 = clan name
 *
 * @author JIV
 *
 */
public class ExNeedToChangeName extends L2GameServerPacket
{
	private int type;
	private int subType;
	private String name;

	public ExNeedToChangeName(int type, int subType, String name)
	{
		this.type = type;
		this.subType = subType;
		this.name = name;
	}

	@Override
	protected void writeImpl()
	{
		writeD(type);
		writeD(subType);
		writeS(name);
	}
}
