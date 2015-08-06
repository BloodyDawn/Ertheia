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
package dwo.gameserver.model.items.itemauction;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Forsaiken
 */
public class ItemAuctionBid
{
	private final int _playerObjId;
	private long _lastBid;

	public ItemAuctionBid(int playerObjId, long lastBid)
	{
		_playerObjId = playerObjId;
		_lastBid = lastBid;
	}

	public int getPlayerObjId()
	{
		return _playerObjId;
	}

	public long getLastBid()
	{
		return _lastBid;
	}

	void setLastBid(long lastBid)
	{
		_lastBid = lastBid;
	}

	void cancelBid()
	{
		_lastBid = -1;
	}

	boolean isCanceled()
	{
		return _lastBid <= 0;
	}

	L2PcInstance getPlayer()
	{
		return WorldManager.getInstance().getPlayer(_playerObjId);
	}
}