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
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.ItemAuctionManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance.ItemLocation;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.database.DatabaseUtils;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemAuctionInstance
{
	static final Logger _log = LogManager.getLogger(ItemAuctionInstance.class);
	private static final long START_TIME_SPACE = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
	private static final long FINISH_TIME_SPACE = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);
	/**
	 * Cached comparator to avoid initialization on each loop run.
	 */
	private static final Comparator<ItemAuction> itemAuctionComparator = (o1, o2) -> Long.valueOf(o2.getStartingTime()).compareTo(o1.getStartingTime());
	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss dd.MM.yy");
	private final int _instanceId;
	private final AtomicInteger _auctionIds;
	private final TIntObjectHashMap<ItemAuction> _auctions;
	private final ArrayList<AuctionItem> _items;
	private final AuctionDateGenerator _dateGenerator;

	private ItemAuction _currentAuction;
	private ItemAuction _nextAuction;
	private ScheduledFuture<?> _stateTask;

	public ItemAuctionInstance(int instanceId, AtomicInteger auctionIds, Node node)
	{
		_instanceId = instanceId;
		_auctionIds = auctionIds;
		_auctions = new TIntObjectHashMap<>();
		_items = new ArrayList<>();

		NamedNodeMap nanode = node.getAttributes();
		StatsSet generatorConfig = new StatsSet();
		for(int i = nanode.getLength(); i-- > 0; )
		{
			Node n = nanode.item(i);
			if(n != null)
			{
				generatorConfig.set(n.getNodeName(), n.getNodeValue());
			}
		}

		_dateGenerator = new AuctionDateGenerator(generatorConfig);

		for(Node na = node.getFirstChild(); na != null; na = na.getNextSibling())
		{
			try
			{
				if("item".equalsIgnoreCase(na.getNodeName()))
				{
					NamedNodeMap naa = na.getAttributes();
					int auctionItemId = Integer.parseInt(naa.getNamedItem("auctionItemId").getNodeValue());
					int auctionLenght = Integer.parseInt(naa.getNamedItem("auctionLenght").getNodeValue());
					long auctionInitBid = Integer.parseInt(naa.getNamedItem("auctionInitBid").getNodeValue());

					int itemId = Integer.parseInt(naa.getNamedItem("itemId").getNodeValue());
					int itemCount = Integer.parseInt(naa.getNamedItem("itemCount").getNodeValue());

					if(auctionLenght < 1)
					{
						throw new IllegalArgumentException("auctionLenght < 1 for instanceId: " + _instanceId + ", itemId " + itemId);
					}

					StatsSet itemExtra = new StatsSet();
					AuctionItem item = new AuctionItem(auctionItemId, auctionLenght, auctionInitBid, itemId, itemCount, itemExtra);

					if(!item.checkItemExists())
					{
						throw new IllegalArgumentException("Item with id " + itemId + " not found");
					}

					for(AuctionItem tmp : _items)
					{
						if(tmp.getAuctionItemId() == auctionItemId)
						{
							throw new IllegalArgumentException("Dublicated auction item id " + auctionItemId);
						}
					}

					_items.add(item);

					for(Node nb = na.getFirstChild(); nb != null; nb = nb.getNextSibling())
					{
						if("extra".equalsIgnoreCase(nb.getNodeName()))
						{
							NamedNodeMap nab = nb.getAttributes();
							for(int i = nab.getLength(); i-- > 0; )
							{
								Node n = nab.item(i);
								if(n != null)
								{
									itemExtra.set(n.getNodeName(), n.getNodeValue());
								}
							}
						}
					}
				}
			}
			catch(IllegalArgumentException e)
			{
				_log.log(Level.ERROR, "ItemAuctionInstance: Failed loading auction item", e);
			}
		}

		if(_items.isEmpty())
		{
			throw new IllegalArgumentException("No items defined");
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT auctionId FROM item_auction WHERE instanceId=?");
			statement.setInt(1, _instanceId);
			rset = statement.executeQuery();

			while(rset.next())
			{
				int auctionId = rset.getInt(1);
				try
				{
					ItemAuction auction = loadAuction(auctionId);
					if(auction != null)
					{
						_auctions.put(auctionId, auction);
					}
					else
					{
						ItemAuctionManager.deleteAuction(auctionId);
					}
				}
				catch(SQLException e)
				{
					_log.log(Level.ERROR, "ItemAuctionInstance: Failed loading auction: " + auctionId, e);
				}
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "L2ItemAuctionInstance: Failed loading auctions.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		_log.log(Level.INFO, "L2ItemAuctionInstance: Loaded " + _items.size() + " item(s) and registered " + _auctions.size() + " auction(s) for instance " + _instanceId + '.');
		checkAndSetCurrentAndNextAuction();
	}

	public ItemAuction getCurrentAuction()
	{
		return _currentAuction;
	}

	public ItemAuction getNextAuction()
	{
		return _nextAuction;
	}

	public void shutdown()
	{
		ScheduledFuture<?> stateTask = _stateTask;
		if(stateTask != null)
		{
			stateTask.cancel(false);
		}
	}

	private AuctionItem getAuctionItem(int auctionItemId)
	{
		for(int i = _items.size(); i-- > 0; )
		{
			AuctionItem item = _items.get(i);
			if(item.getAuctionItemId() == auctionItemId)
			{
				return item;
			}
		}
		return null;
	}

	void checkAndSetCurrentAndNextAuction()
	{
		ItemAuction[] auctions = _auctions.values(new ItemAuction[0]);

		ItemAuction currentAuction = null;
		ItemAuction nextAuction = null;

		switch(auctions.length)
		{
			case 0:
				nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
				break;

			case 1:
				switch(auctions[0].getAuctionState())
				{
					case CREATED:
						if(auctions[0].getStartingTime() < System.currentTimeMillis() + START_TIME_SPACE)
						{
							currentAuction = auctions[0];
							nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
						}
						else
						{
							nextAuction = auctions[0];
						}
						break;

					case STARTED:
						currentAuction = auctions[0];
						nextAuction = createAuction(Math.max(currentAuction.getEndingTime() + FINISH_TIME_SPACE, System.currentTimeMillis() + START_TIME_SPACE));
						break;

					case FINISHED:
						currentAuction = auctions[0];
						nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
						break;

					default:
						throw new IllegalArgumentException();
				}
				break;

			default:
				Arrays.sort(auctions, itemAuctionComparator);

				// just to make sure we won`t skip any auction because of little different times
				long currentTime = System.currentTimeMillis();

				for(ItemAuction auction : auctions)
				{
					if(auction.getAuctionState() == ItemAuctionState.STARTED)
					{
						currentAuction = auction;
						break;
					}
					else if(auction.getStartingTime() <= currentTime)
					{
						currentAuction = auction;
						break; // only first
					}
				}

				for(ItemAuction auction : auctions)
				{
					if(auction.getStartingTime() > currentTime && !currentAuction.equals(auction))
					{
						nextAuction = auction;
						break;
					}
				}

				if(nextAuction == null)
				{
					nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
				}
				break;
		}

		_auctions.put(nextAuction.getAuctionId(), nextAuction);

		_currentAuction = currentAuction;
		_nextAuction = nextAuction;

		if(currentAuction != null && currentAuction.getAuctionState() != ItemAuctionState.FINISHED)
		{
			if(currentAuction.getAuctionState() == ItemAuctionState.STARTED)
			{
				setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleAuctionTask(currentAuction), Math.max(currentAuction.getEndingTime() - System.currentTimeMillis(), 0L)));
			}
			else
			{
				setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleAuctionTask(currentAuction), Math.max(currentAuction.getStartingTime() - System.currentTimeMillis(), 0L)));
			}
			_log.log(Level.INFO, "L2ItemAuctionInstance: Schedule current auction " + currentAuction.getAuctionId() + " for instance " + _instanceId);
		}
		else
		{
			setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleAuctionTask(nextAuction), Math.max(nextAuction.getStartingTime() - System.currentTimeMillis(), 0L)));
			_log.log(Level.INFO, "L2ItemAuctionInstance: Schedule next auction " + nextAuction.getAuctionId() + " on " + DATE_FORMAT.format(new Date(nextAuction.getStartingTime())) + " for instance " + _instanceId);
		}
	}

	public ItemAuction getAuction(int auctionId)
	{
		return _auctions.get(auctionId);
	}

	public ItemAuction[] getAuctionsByBidder(int bidderObjId)
	{
		ItemAuction[] auctions = getAuctions();
		ArrayList<ItemAuction> stack = new ArrayList<>(auctions.length);
		for(ItemAuction auction : getAuctions())
		{
			if(auction.getAuctionState() != ItemAuctionState.CREATED)
			{
				ItemAuctionBid bid = auction.getBidFor(bidderObjId);
				if(bid != null)
				{
					stack.add(auction);
				}
			}
		}
		return stack.toArray(new ItemAuction[stack.size()]);
	}

	public ItemAuction[] getAuctions()
	{
		ItemAuction[] auctions;

		synchronized(_auctions)
		{
			auctions = _auctions.values(new ItemAuction[0]);
		}

		return auctions;
	}

	void onAuctionFinished(ItemAuction auction)
	{
		auction.broadcastToAllBiddersInternal(SystemMessage.getSystemMessage(SystemMessageId.S1_AUCTION_ENDED).addNumber(auction.getAuctionId()));

		ItemAuctionBid bid = auction.getHighestBid();
		if(bid != null)
		{
			L2ItemInstance item = auction.createNewItemInstance();
			L2PcInstance player = bid.getPlayer();
			if(player != null)
			{
				player.getWarehouse().addItem(ProcessType.ITEM_AUCTION, item, null, null);
				player.sendPacket(SystemMessageId.WON_BID_ITEM_CAN_BE_FOUND_IN_WAREHOUSE);

				_log.log(Level.INFO, "L2ItemAuctionInstance: ClanHallAuctionEngine " + auction.getAuctionId() + " has finished. Highest bid by " + player.getName() + " for instance " + _instanceId);
			}
			else
			{
				item.setOwnerId(bid.getPlayerObjId());
				item.setLocation(ItemLocation.WAREHOUSE);
				item.updateDatabase();
				WorldManager.getInstance().removeObject(item);

				_log.log(Level.INFO, "L2ItemAuctionInstance: ClanHallAuctionEngine " + auction.getAuctionId() + " has finished. Highest bid by " + CharNameTable.getInstance().getNameById(bid.getPlayerObjId()) + " for instance " + _instanceId);
			}

			// Clean all canceled bids
			auction.clearCanceledBids();
		}
		else
		{
			_log.log(Level.INFO, "L2ItemAuctionInstance: ClanHallAuctionEngine " + auction.getAuctionId() + " has finished. There have not been any bid for instance " + _instanceId);
		}
	}

	void setStateTask(ScheduledFuture<?> future)
	{
		ScheduledFuture<?> stateTask = _stateTask;
		if(stateTask != null)
		{
			stateTask.cancel(false);
		}

		_stateTask = future;
	}

	private ItemAuction createAuction(long after)
	{
		AuctionItem auctionItem = _items.get(Rnd.get(_items.size()));
		long startingTime = _dateGenerator.nextDate(after);
		long endingTime = startingTime + TimeUnit.MILLISECONDS.convert(auctionItem.getAuctionLength(), TimeUnit.MINUTES);
		ItemAuction auction = new ItemAuction(_auctionIds.getAndIncrement(), _instanceId, startingTime, endingTime, auctionItem);
		auction.storeMe();
		return auction;
	}

	private ItemAuction loadAuction(int auctionId) throws SQLException
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT auctionItemId,startingTime,endingTime,auctionStateId FROM item_auction WHERE auctionId=?");
			statement.setInt(1, auctionId);
			rset = statement.executeQuery();

			if(!rset.next())
			{
				_log.log(Level.WARN, "ItemAuctionInstance: ClanHallAuctionEngine data not found for auction: " + auctionId);
				return null;
			}

			int auctionItemId = rset.getInt(1);
			long startingTime = rset.getLong(2);
			long endingTime = rset.getLong(3);
			byte auctionStateId = rset.getByte(4);
			DatabaseUtils.closeStatement(statement);
			DatabaseUtils.closeResultSet(rset);

			if(startingTime >= endingTime)
			{
				_log.log(Level.WARN, "ItemAuctionInstance: Invalid starting/ending paramaters for auction: " + auctionId);
				return null;
			}

			AuctionItem auctionItem = getAuctionItem(auctionItemId);
			if(auctionItem == null)
			{
				_log.log(Level.WARN, "ItemAuctionInstance: AuctionItem: " + auctionItemId + ", not found for auction: " + auctionId);
				return null;
			}

			ItemAuctionState auctionState = ItemAuctionState.stateForStateId(auctionStateId);
			if(auctionState == null)
			{
				_log.log(Level.WARN, "ItemAuctionInstance: Invalid auctionStateId: " + auctionStateId + ", for auction: " + auctionId);
				return null;
			}

			if(auctionState == ItemAuctionState.FINISHED && startingTime < System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(Config.ALT_ITEM_AUCTION_EXPIRED_AFTER, TimeUnit.DAYS))
			{
				_log.log(Level.INFO, "ItemAuctionInstance: Clearing expired auction: " + auctionId);
				statement = con.prepareStatement("DELETE FROM item_auction WHERE auctionId=?");
				statement.setInt(1, auctionId);
				statement.execute();
				DatabaseUtils.closeStatement(statement);

				statement = con.prepareStatement("DELETE FROM item_auction_bid WHERE auctionId=?");
				statement.setInt(1, auctionId);
				statement.execute();
				DatabaseUtils.closeStatement(statement);
				return null;
			}

			statement = con.prepareStatement("SELECT playerObjId,playerBid FROM item_auction_bid WHERE auctionId=?");
			statement.setInt(1, auctionId);
			rset = statement.executeQuery();

			ArrayList<ItemAuctionBid> auctionBids = new ArrayList<>();

			while(rset.next())
			{
				int playerObjId = rset.getInt(1);
				long playerBid = rset.getLong(2);
				ItemAuctionBid bid = new ItemAuctionBid(playerObjId, playerBid);
				auctionBids.add(bid);
			}

			return new ItemAuction(auctionId, _instanceId, startingTime, endingTime, auctionItem, auctionBids, auctionState);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	private class ScheduleAuctionTask implements Runnable
	{
		private final ItemAuction _auction;

		public ScheduleAuctionTask(ItemAuction auction)
		{
			_auction = auction;
		}

		@Override
		public void run()
		{
			try
			{
				runImpl();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "L2ItemAuctionInstance: Failed scheduling auction " + _auction.getAuctionId(), e);
			}
		}

		private void runImpl()
		{
			ItemAuctionState state = _auction.getAuctionState();
			switch(state)
			{
				case CREATED:
					if(!_auction.setAuctionState(state, ItemAuctionState.STARTED))
					{
						throw new IllegalStateException("Could not set auction state: " + ItemAuctionState.STARTED + ", expected: " + state);
					}

					_log.log(Level.INFO, "L2ItemAuctionInstance: ClanHallAuctionEngine " + _auction.getAuctionId() + " has started for instance " + _auction.getInstanceId());
					checkAndSetCurrentAndNextAuction();
					break;

				case STARTED:
					switch(_auction.getAuctionEndingExtendState())
					{
						case EXTEND_BY_5_MIN:
							if(_auction.getScheduledAuctionEndingExtendState() == ItemAuctionExtendState.INITIAL)
							{
								_auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_5_MIN);
								setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
							break;

						case EXTEND_BY_3_MIN:
							if(_auction.getScheduledAuctionEndingExtendState() != ItemAuctionExtendState.EXTEND_BY_3_MIN)
							{
								_auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_3_MIN);
								setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
							break;

						case EXTEND_BY_CONFIG_PHASE_A:
							if(_auction.getScheduledAuctionEndingExtendState() != ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B)
							{
								_auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B);
								setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
							break;

						case EXTEND_BY_CONFIG_PHASE_B:
							if(_auction.getScheduledAuctionEndingExtendState() != ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A)
							{
								_auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A);
								setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
					}

					if(!_auction.setAuctionState(state, ItemAuctionState.FINISHED))
					{
						throw new IllegalStateException("Could not set auction state: " + ItemAuctionState.FINISHED + ", expected: " + state);
					}

					onAuctionFinished(_auction);
					checkAndSetCurrentAndNextAuction();
					break;

				default:
					throw new IllegalStateException("Invalid state: " + state);
			}
		}
	}
}