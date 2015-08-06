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

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.ItemAuctionManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemInfo;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Forsaiken
 */
public class ItemAuction
{
	static final Logger _log = LogManager.getLogger(ItemAuctionManager.class);
	private static final long ENDING_TIME_EXTEND_5 = TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);
	private static final long ENDING_TIME_EXTEND_3 = TimeUnit.MILLISECONDS.convert(3, TimeUnit.MINUTES);

	private final int _auctionId;
	private final int _instanceId;
	private final long _startingTime;
	private final AuctionItem _auctionItem;
	private final List<ItemAuctionBid> _auctionBids;
	private final Object _auctionStateLock;
	private volatile long _endingTime;
	private volatile ItemAuctionState _auctionState;
	private volatile ItemAuctionExtendState _scheduledAuctionEndingExtendState;
	private volatile ItemAuctionExtendState _auctionEndingExtendState;

	private ItemInfo _itemInfo;

	private ItemAuctionBid _highestBid;
	private int _lastBidPlayerObjId;

	public ItemAuction(int auctionId, int instanceId, long startingTime, long endingTime, AuctionItem auctionItem)
	{
		this(auctionId, instanceId, startingTime, endingTime, auctionItem, new ArrayList<>(), ItemAuctionState.CREATED);
	}

	public ItemAuction(int auctionId, int instanceId, long startingTime, long endingTime, AuctionItem auctionItem, List<ItemAuctionBid> auctionBids, ItemAuctionState auctionState)
	{
		_auctionId = auctionId;
		_instanceId = instanceId;
		_startingTime = startingTime;
		_endingTime = endingTime;
		_auctionItem = auctionItem;
		_auctionBids = auctionBids;
		_auctionState = auctionState;
		_auctionStateLock = new Object();
		_scheduledAuctionEndingExtendState = ItemAuctionExtendState.INITIAL;
		_auctionEndingExtendState = ItemAuctionExtendState.INITIAL;

		L2ItemInstance item = _auctionItem.createNewItemInstance();
		_itemInfo = new ItemInfo(item);
		WorldManager.getInstance().removeObject(item);

		_auctionBids.stream().filter(bid -> _highestBid == null || _highestBid.getLastBid() < bid.getLastBid()).forEach(bid -> _highestBid = bid);
	}

	public ItemAuctionState getAuctionState()
	{
		ItemAuctionState auctionState;

		synchronized(_auctionStateLock)
		{
			auctionState = _auctionState;
		}

		return auctionState;
	}

	public boolean setAuctionState(ItemAuctionState expected, ItemAuctionState wanted)
	{
		synchronized(_auctionStateLock)
		{
			if(_auctionState != expected)
			{
				return false;
			}

			_auctionState = wanted;
			storeMe();
			return true;
		}
	}

	public int getAuctionId()
	{
		return _auctionId;
	}

	public int getInstanceId()
	{
		return _instanceId;
	}

	public ItemInfo getItemInfo()
	{
		return _itemInfo;
	}

	public L2ItemInstance createNewItemInstance()
	{
		return _auctionItem.createNewItemInstance();
	}

	public long getAuctionInitBid()
	{
		return _auctionItem.getAuctionInitBid();
	}

	public ItemAuctionBid getHighestBid()
	{
		return _highestBid;
	}

	public ItemAuctionExtendState getAuctionEndingExtendState()
	{
		return _auctionEndingExtendState;
	}

	public ItemAuctionExtendState getScheduledAuctionEndingExtendState()
	{
		return _scheduledAuctionEndingExtendState;
	}

	public void setScheduledAuctionEndingExtendState(ItemAuctionExtendState state)
	{
		_scheduledAuctionEndingExtendState = state;
	}

	public long getStartingTime()
	{
		return _startingTime;
	}

	public long getEndingTime()
	{
		return _endingTime;
	}

	public long getStartingTimeRemaining()
	{
		return Math.max(_endingTime - System.currentTimeMillis(), 0L);
	}

	public long getFinishingTimeRemaining()
	{
		return Math.max(_endingTime - System.currentTimeMillis(), 0L);
	}

	public void storeMe()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO item_auction (auctionId,instanceId,auctionItemId,startingTime,endingTime,auctionStateId) VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE auctionStateId=?");
			statement.setInt(1, _auctionId);
			statement.setInt(2, _instanceId);
			statement.setInt(3, _auctionItem.getAuctionItemId());
			statement.setLong(4, _startingTime);
			statement.setLong(5, _endingTime);
			statement.setByte(6, _auctionState.getStateId());
			statement.setByte(7, _auctionState.getStateId());
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.WARN, "", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public int getAndSetLastBidPlayerObjectId(int playerObjId)
	{
		int lastBid = _lastBidPlayerObjId;
		_lastBidPlayerObjId = playerObjId;
		return lastBid;
	}

	private void updatePlayerBid(ItemAuctionBid bid, boolean delete)
	{
		// TODO nBd maybe move such stuff to you db updater :D
		updatePlayerBidInternal(bid, delete);
	}

	void updatePlayerBidInternal(ItemAuctionBid bid, boolean delete)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			if(delete)
			{
				statement = con.prepareStatement("DELETE FROM item_auction_bid WHERE auctionId=? AND playerObjId=?");
				statement.setInt(1, _auctionId);
				statement.setInt(2, bid.getPlayerObjId());
			}
			else
			{
				statement = con.prepareStatement("INSERT INTO item_auction_bid (auctionId,playerObjId,playerBid) VALUES (?,?,?) ON DUPLICATE KEY UPDATE playerBid=?");
				statement.setInt(1, _auctionId);
				statement.setInt(2, bid.getPlayerObjId());
				statement.setLong(3, bid.getLastBid());
				statement.setLong(4, bid.getLastBid());
			}

			statement.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.WARN, "", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void registerBid(L2PcInstance player, long newBid)
	{
		if(player == null)
		{
			throw new NullPointerException();
		}

		if(newBid < getAuctionInitBid())
		{
			player.sendPacket(SystemMessageId.BID_PRICE_MUST_BE_HIGHER);
			return;
		}

		if(newBid > 100000000000L)
		{
			player.sendPacket(SystemMessageId.BID_CANT_EXCEED_100_BILLION);
			return;
		}

		if(getAuctionState() != ItemAuctionState.STARTED)
		{
			return;
		}

		int playerObjId = player.getObjectId();

		synchronized(_auctionBids)
		{
			if(_highestBid != null && newBid < _highestBid.getLastBid())
			{
				player.sendPacket(SystemMessageId.BID_MUST_BE_HIGHER_THAN_CURRENT_BID);
				return;
			}

			ItemAuctionBid bid = getBidFor(playerObjId);
			if(bid == null)
			{
				if(!reduceItemCount(player, newBid))
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_FOR_THIS_BID);
					return;
				}

				bid = new ItemAuctionBid(playerObjId, newBid);
				_auctionBids.add(bid);
			}
			else
			{
				if(!bid.isCanceled())
				{
					if(newBid < bid.getLastBid()) // just another check
					{
						player.sendPacket(SystemMessageId.BID_MUST_BE_HIGHER_THAN_CURRENT_BID);
						return;
					}

					if(!reduceItemCount(player, newBid - bid.getLastBid()))
					{
						player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_FOR_THIS_BID);
						return;
					}
				}
				else if(!reduceItemCount(player, newBid))
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_FOR_THIS_BID);
					return;
				}

				bid.setLastBid(newBid);
			}

			onPlayerBid(player, bid);
			updatePlayerBid(bid, false);

			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SUBMITTED_A_BID).addItemNumber(newBid));
		}
	}

	private void onPlayerBid(L2PcInstance player, ItemAuctionBid bid)
	{
		if(_highestBid == null)
		{
			_highestBid = bid;
		}
		else if(_highestBid.getLastBid() < bid.getLastBid())
		{
			L2PcInstance old = _highestBid.getPlayer();
			if(old != null)
			{
				old.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_OUTBID));
			}

			_highestBid = bid;
		}

		if(_endingTime - System.currentTimeMillis() <= 1000 * 60 * 10) // 10 minutes
		{
			switch(_auctionEndingExtendState)
			{
				case INITIAL:
					_auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_5_MIN;
					_endingTime += ENDING_TIME_EXTEND_5;
					broadcastToAllBidders(SystemMessage.getSystemMessage(SystemMessageId.BIDDER_EXISTS_AUCTION_TIME_EXTENDED_BY_5_MINUTES));
					break;
				case EXTEND_BY_5_MIN:
					if(getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId())
					{
						_auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_3_MIN;
						_endingTime += ENDING_TIME_EXTEND_3;
						broadcastToAllBidders(SystemMessage.getSystemMessage(SystemMessageId.BIDDER_EXISTS_AUCTION_TIME_EXTENDED_BY_3_MINUTES));
					}
					break;
				case EXTEND_BY_3_MIN:
					if(Config.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID > 0)
					{
						if(getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId())
						{
							_auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A;
							_endingTime += Config.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
						}
					}
					break;
				case EXTEND_BY_CONFIG_PHASE_A:
					if(getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId())
					{
						if(_scheduledAuctionEndingExtendState == ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B)
						{
							_auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B;
							_endingTime += Config.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
						}
					}
					break;
				case EXTEND_BY_CONFIG_PHASE_B:
					if(getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId())
					{
						if(_scheduledAuctionEndingExtendState == ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A)
						{
							_endingTime += Config.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
							_auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A;
						}
					}
			}
		}
	}

	public void broadcastToAllBidders(L2GameServerPacket packet)
	{
		ThreadPoolManager.getInstance().executeTask(() -> broadcastToAllBiddersInternal(packet));
	}

	public void broadcastToAllBiddersInternal(L2GameServerPacket packet)
	{
		for(int i = _auctionBids.size(); i-- > 0; )
		{
			ItemAuctionBid bid = _auctionBids.get(i);
			if(bid != null)
			{
				L2PcInstance player = bid.getPlayer();
				if(player != null)
				{
					player.sendPacket(packet);
				}
			}
		}
	}

	public boolean cancelBid(L2PcInstance player)
	{
		if(player == null)
		{
			throw new NullPointerException();
		}

		switch(getAuctionState())
		{
			case CREATED:
				return false;

			case FINISHED:
				if(_startingTime < System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(Config.ALT_ITEM_AUCTION_EXPIRED_AFTER, TimeUnit.DAYS))
				{
					return false;
				}
				else
				{
					break;
				}
		}

		int playerObjId = player.getObjectId();

		synchronized(_auctionBids)
		{
			if(_highestBid == null)
			{
				return false;
			}

			int bidIndex = getBidIndexFor(playerObjId);
			if(bidIndex == -1)
			{
				return false;
			}

			ItemAuctionBid bid = _auctionBids.get(bidIndex);
			if(bid.getPlayerObjId() == _highestBid.getPlayerObjId())
			{
				// can't return winning bid
				if(getAuctionState() == ItemAuctionState.FINISHED)
				{
					return false;
				}

				player.sendPacket(SystemMessageId.HIGHEST_BID_BUT_RESERVE_NOT_MET);
				return true;
			}

			if(bid.isCanceled())
			{
				return false;
			}

			increaseItemCount(player, bid.getLastBid());
			bid.cancelBid();

			// delete bid from database if auction already finished
			updatePlayerBid(bid, getAuctionState() == ItemAuctionState.FINISHED);

			player.sendPacket(SystemMessageId.CANCELED_BID);
		}
		return true;
	}

	public void clearCanceledBids()
	{
		if(getAuctionState() != ItemAuctionState.FINISHED)
		{
			throw new IllegalStateException("Attempt to clear canceled bids for non-finished auction");
		}

		synchronized(_auctionBids)
		{
			for(ItemAuctionBid bid : _auctionBids)
			{
				if(bid == null || !bid.isCanceled())
				{
					continue;
				}
				updatePlayerBid(bid, true);
			}
		}
	}

	private boolean reduceItemCount(L2PcInstance player, long count)
	{
		if(!player.reduceAdena(ProcessType.ITEM_AUCTION, count, player, true))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_FOR_THIS_BID);
			return false;
		}
		return true;
	}

	private void increaseItemCount(L2PcInstance player, long count)
	{
		player.addAdena(ProcessType.ITEM_AUCTION, count, player, true);
	}

	/**
	 * Returns the last bid for the given player or -1 if he did not made one yet.
	 *
	 * @param player The player that made the bid
	 * @return The last bid the player made or -1
	 */
	public long getLastBid(L2PcInstance player)
	{
		ItemAuctionBid bid = getBidFor(player.getObjectId());
		return bid != null ? bid.getLastBid() : -1L;
	}

	public ItemAuctionBid getBidFor(int playerObjId)
	{
		int index = getBidIndexFor(playerObjId);
		return index != -1 ? _auctionBids.get(index) : null;
	}

	private int getBidIndexFor(int playerObjId)
	{
		for(int i = _auctionBids.size(); i-- > 0; )
		{
			ItemAuctionBid bid = _auctionBids.get(i);
			if(bid != null && bid.getPlayerObjId() == playerObjId)
			{
				return i;
			}
		}
		return -1;
	}
}