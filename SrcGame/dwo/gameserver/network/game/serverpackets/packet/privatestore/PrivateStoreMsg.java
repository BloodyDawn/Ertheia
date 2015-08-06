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
package dwo.gameserver.network.game.serverpackets.packet.privatestore;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PrivateStoreMsg extends L2GameServerPacket
{
	private int _objId;
	private String _storeMsg;

	public PrivateStoreMsg(L2PcInstance player)
	{
		_objId = player.getObjectId();
		if(player.getSellList() != null)
		{
			_storeMsg = player.getSellList().getTitle();
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objId);
		writeS(_storeMsg);
	}
}
