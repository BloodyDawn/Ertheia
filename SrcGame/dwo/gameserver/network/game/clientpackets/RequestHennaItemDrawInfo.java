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

import dwo.gameserver.datatables.xml.HennaTable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2Henna;
import dwo.gameserver.model.items.base.instance.L2HennaInstance;
import dwo.gameserver.network.game.serverpackets.packet.henna.HennaItemInfo;

public class RequestHennaItemDrawInfo extends L2GameClientPacket
{
	private int _symbolId;

	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2Henna template = HennaTable.getInstance().getTemplate(_symbolId);
		if(template == null)
		{
			return;
		}

		L2HennaInstance henna = new L2HennaInstance(template);
		activeChar.sendPacket(new HennaItemInfo(henna, activeChar));
	}

	@Override
	public String getType()
	{
		return "[C] BB RequestHennaItemDrawInfo";
	}
}
