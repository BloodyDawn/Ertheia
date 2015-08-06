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

public class StartRotating extends L2GameClientPacket
{
	private int _degree;
	private int _side;

	@Override
	protected void readImpl()
	{
		_degree = readD();
		_side = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		dwo.gameserver.network.game.serverpackets.StartRotating br;
		if(activeChar.isInAirShip() && activeChar.getAirShip().isCaptain(activeChar))
		{
			br = new dwo.gameserver.network.game.serverpackets.StartRotating(activeChar.getAirShip().getObjectId(), _degree, _side, 0);
			activeChar.getAirShip().broadcastPacket(br);
		}
		else
		{
			br = new dwo.gameserver.network.game.serverpackets.StartRotating(activeChar.getObjectId(), _degree, _side, 0);
			activeChar.broadcastPacket(br);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 4A StartRotating";
	}
}