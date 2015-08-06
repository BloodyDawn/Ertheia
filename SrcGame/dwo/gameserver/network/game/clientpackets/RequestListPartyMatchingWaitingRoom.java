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
import dwo.gameserver.network.game.serverpackets.packet.ex.ExListPartyMatchingWaitingRoom;

/**
 *
 * @author Gnacik
 *
 */
public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket
{
	private int _minLevel;
	private int _maxLevel;
	private int _page;
	private int[] _classes;

	@Override
	protected void readImpl()
	{
		_page = readD();
		_minLevel = readD();
		_maxLevel = readD();
		int size = readD();
		if(size > Byte.MAX_VALUE || size < 0)
		{
			size = 0;
		}
		_classes = new int[size];
		for(int i = 0; i < size; i++)
		{
			_classes[i] = readD();
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance _activeChar = getClient().getActiveChar();

		if(_activeChar == null)
		{
			return;
		}

		_activeChar.sendPacket(new ExListPartyMatchingWaitingRoom(_activeChar, _page, _minLevel, _maxLevel, _classes));
	}

	@Override
	public String getType()
	{
		return "[C] D0:31 RequestListPartyMatchingWaitingRoom";
	}

}