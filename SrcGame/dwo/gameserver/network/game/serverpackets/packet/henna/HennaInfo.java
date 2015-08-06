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
package dwo.gameserver.network.game.serverpackets.packet.henna;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2HennaInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class HennaInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final L2HennaInstance[] _hennas = new L2HennaInstance[3];
	private int _count;

	public HennaInfo(L2PcInstance player)
	{
		_activeChar = player;

		int j = 0;
		for(int i = 0; i < 3; i++)
		{
			L2HennaInstance henna = _activeChar.getHenna(i + 1);
			if(henna != null)
			{
				_hennas[j++] = henna;
			}
		}
		_count = j;
	}

	@Override
	protected void writeImpl()
	{
		writeC(_activeChar.getHennaStatINT()); //equip INT
		writeC(_activeChar.getHennaStatSTR()); //equip STR
		writeC(_activeChar.getHennaStatCON()); //equip CON
		writeC(_activeChar.getHennaStatMEN()); //equip MEM
		writeC(_activeChar.getHennaStatDEX()); //equip DEX
		writeC(_activeChar.getHennaStatWIT()); //equip WIT
        writeC(_activeChar.getHennaStatLUC()); //equip LUC
        writeC(_activeChar.getHennaStatCHA()); //equip CHA
		writeD(4 - _activeChar.getHennaEmptySlots());
		writeD(_count); //size
		for(int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(0x01);
		}
        writeD(0x00);
        writeD(0x00);
        writeD(0x00);
	}
}
