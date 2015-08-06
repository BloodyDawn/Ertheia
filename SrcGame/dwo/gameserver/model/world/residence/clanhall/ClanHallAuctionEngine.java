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
package dwo.gameserver.model.world.residence.clanhall;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.instancemanager.AuctionManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.database.DatabaseUtils;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.Calendar;

import static dwo.gameserver.model.items.itemcontainer.PcInventory.ADENA_ID;
import static dwo.gameserver.model.items.itemcontainer.PcInventory.MAX_ADENA;

public class ClanHallAuctionEngine
{
	protected static final Logger _log = LogManager.getLogger(ClanHallAuctionEngine.class);
	private static final String[] ItemTypeName = {
		"ClanHall"
	};
	private int _id;
	private long _endDate;
	private int _highestBidderId;
	private String _highestBidderName = "";
	private long _highestBidderMaxBid;
	private int _itemId;
	private String _itemName = "";
	private int _itemObjectId;
	private long _itemQuantity;
	private String _itemType = "";
	private int _sellerId;
	private String _sellerClanName = "";
	private String _sellerName = "";
	private long _currentBid;
	private long _startingBid;
	private TIntObjectHashMap<Bidder> _bidders = new TIntObjectHashMap<>();

	/**
	 * Constructor
	 * @param auctionId
	 */
	public ClanHallAuctionEngine(int auctionId)
	{
		_id = auctionId;
		load();
		startAutoTask();
	}

	public ClanHallAuctionEngine(int itemId, L2Clan Clan, long delay, long bid, String name)
	{
		_id = itemId;
		_endDate = System.currentTimeMillis() + delay;
		_itemId = itemId;
		_itemName = name;
		_itemType = "ClanHall";
		_sellerId = Clan.getLeaderId();
		_sellerName = Clan.getLeaderName();
		_sellerClanName = Clan.getName();
		_startingBid = bid;
	}

	public static String getItemTypeName(ItemTypeEnum value)
	{
		return ItemTypeName[value.ordinal()];
	}

	/**
	 * Load auctions
	 */
	private void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("Select * from auction where id = ?");
			statement.setInt(1, _id);
			rs = statement.executeQuery();

			while(rs.next())
			{
				_currentBid = rs.getLong("currentBid");
				_endDate = rs.getLong("endDate");
				_itemId = rs.getInt("itemId");
				_itemName = rs.getString("itemName");
				_itemObjectId = rs.getInt("itemObjectId");
				_itemType = rs.getString("itemType");
				_sellerId = rs.getInt("sellerId");
				_sellerClanName = rs.getString("sellerClanName");
				_sellerName = rs.getString("sellerName");
				_startingBid = rs.getLong("startingBid");
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: ClanHallAuctionEngine.load(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
			loadBid();
		}
	}

	/** Load bidders **/
	private void loadBid()
	{
		_highestBidderId = 0;
		_highestBidderName = "";
		_highestBidderMaxBid = 0;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT bidderId, bidderName, maxBid, clan_name, time_bid FROM auction_bid WHERE auctionId = ? ORDER BY maxBid DESC");
			statement.setInt(1, _id);
			rs = statement.executeQuery();

			while(rs.next())
			{
				if(rs.isFirst())
				{
					_highestBidderId = rs.getInt("bidderId");
					_highestBidderName = rs.getString("bidderName");
					_highestBidderMaxBid = rs.getLong("maxBid");
				}
				_bidders.put(rs.getInt("bidderId"), new Bidder(rs.getString("bidderName"), rs.getString("clan_name"), rs.getLong("maxBid"), rs.getLong("time_bid")));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: ClanHallAuctionEngine.loadBid(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	/** Task Manage */
	private void startAutoTask()
	{
		long currentTime = System.currentTimeMillis();
		long taskDelay = 0;
		if(_endDate <= currentTime)
		{
			_endDate = currentTime + 7 * 24 * 60 * 60 * 1000;
			saveAuctionDate();
		}
		else
		{
			taskDelay = _endDate - currentTime;
		}
		ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(), taskDelay);
	}

	/** Save Auction Data End */
	private void saveAuctionDate()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("Update auction set endDate = ? where id = ?");
			statement.setLong(1, _endDate);
			statement.setInt(2, _id);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: saveAuctionDate(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Set a bid
	 * @param bidder
	 * @param bid
	 */
	public void setBid(L2PcInstance bidder, long bid)
	{
		synchronized(this)
		{
			long requiredAdena = bid;
			if(_highestBidderName.equals(bidder.getClan().getLeaderName()))
			{
				requiredAdena = bid - _highestBidderMaxBid;
			}
			if(_highestBidderId > 0 && bid > _highestBidderMaxBid || _highestBidderId == 0 && bid >= _startingBid)
			{
				if(takeItem(bidder, requiredAdena))
				{
					updateInDB(bidder, bid);
					bidder.getClan().setAuctionBiddedAt(_id, true);
					return;
				}
			}
			if(bid < _startingBid || bid <= _highestBidderMaxBid)
			{
				bidder.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BID_PRICE_MUST_BE_HIGHER));
			}
		}
	}

	/**
	 * Return Item in WHC
	 * @param Clan
	 * @param quantity
	 * @param penalty
	 */
	private void returnItem(String Clan, long quantity, boolean penalty)
	{
		if(penalty)
		{
			quantity *= 0.9; //take 10% tax fee if needed
		}

		// avoid overflow on return
		long limit = MAX_ADENA - ClanTable.getInstance().getClanByName(Clan).getWarehouse().getAdenaCount();
		quantity = Math.min(quantity, limit);

		ClanTable.getInstance().getClanByName(Clan).getWarehouse().addItem(ProcessType.OUTBIDDED, ADENA_ID, quantity, null, null);
	}

	/**
	 * Take Item in WHC
	 * @param bidder
	 * @param quantity
	 * @return
	 */
	private boolean takeItem(L2PcInstance bidder, long quantity)
	{
		if(bidder.getClan() != null && bidder.getClan().getWarehouse().getAdenaCount() >= quantity)
		{
			bidder.getClan().getWarehouse().destroyItemByItemId(ProcessType.BUY, ADENA_ID, quantity, bidder, bidder);
			return true;
		}
		bidder.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_IN_CWH);
		return false;
	}

	/**
	 * Update auction in DB
	 * @param bidder
	 * @param bid
	 */
	private void updateInDB(L2PcInstance bidder, long bid)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			if(_bidders.get(bidder.getClanId()) != null)
			{
				statement = con.prepareStatement("UPDATE auction_bid SET bidderId=?, bidderName=?, maxBid=?, time_bid=? WHERE auctionId=? AND bidderId=?");
				statement.setInt(1, bidder.getClanId());
				statement.setString(2, bidder.getClan().getLeaderName());
				statement.setLong(3, bid);
				statement.setLong(4, System.currentTimeMillis());
				statement.setInt(5, _id);
				statement.setInt(6, bidder.getClanId());
				statement.execute();
			}
			else
			{
				statement = con.prepareStatement("INSERT INTO auction_bid (id, auctionId, bidderId, bidderName, maxBid, clan_name, time_bid) VALUES (?, ?, ?, ?, ?, ?, ?)");
				statement.setInt(1, IdFactory.getInstance().getNextId());
				statement.setInt(2, _id);
				statement.setInt(3, bidder.getClanId());
				statement.setString(4, bidder.getName());
				statement.setLong(5, bid);
				statement.setString(6, bidder.getClan().getName());
				statement.setLong(7, System.currentTimeMillis());
				statement.execute();
				if(WorldManager.getInstance().getPlayer(_highestBidderName) != null)
				{
					WorldManager.getInstance().getPlayer(_highestBidderName).sendMessage("You have been out bidded");
				}
			}
			_highestBidderId = bidder.getClanId();
			_highestBidderMaxBid = bid;
			_highestBidderName = bidder.getClan().getLeaderName();
			if(_bidders.get(_highestBidderId) == null)
			{
				_bidders.put(_highestBidderId, new Bidder(_highestBidderName, bidder.getClan().getName(), bid, Calendar.getInstance().getTimeInMillis()));
			}
			else
			{
				_bidders.get(_highestBidderId).setBid(bid);
				_bidders.get(_highestBidderId).setTimeBid(Calendar.getInstance().getTimeInMillis());
			}
			bidder.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BID_IN_CLANHALL_AUCTION));
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: ClanHallAuctionEngine.updateInDB(L2PcInstance bidder, int bid): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/** Remove bids */
	private void removeBids()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=?");
			statement.setInt(1, _id);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: ClanHallAuctionEngine.deleteFromDB(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		for(Bidder b : _bidders.values(new Bidder[0]))
		{
			if(ClanTable.getInstance().getClanByName(b.getClanName()).getClanhallId() == 0)
			{
				returnItem(b.getClanName(), b.getBid(), true); // 10 % tax
			}
			else
			{
				if(WorldManager.getInstance().getPlayer(b.getName()) != null)
				{
					WorldManager.getInstance().getPlayer(b.getName()).sendMessage("Congratulation you have won ClanHall!");
				}
			}
			ClanTable.getInstance().getClanByName(b.getClanName()).setAuctionBiddedAt(0, true);
		}
		_bidders.clear();
	}

	/** Remove auctions */
	public void deleteAuctionFromDB()
	{
		AuctionManager.getInstance().getAuctions().remove(this);
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM auction WHERE itemId=?");
			statement.setInt(1, _itemId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: ClanHallAuctionEngine.deleteFromDB(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/** End of auction */
	public void endAuction()
	{
		if(ClanHallManager.getInstance().loaded())
		{
			if(_highestBidderId == 0 && _sellerId == 0)
			{
				startAutoTask();
				return;
			}
			if(_highestBidderId == 0 && _sellerId > 0)
			{
				/** If seller haven't sell ClanHall, auction removed,
				 *  THIS MUST BE CONFIRMED */
				int aucId = AuctionManager.getInstance().getAuctionIndex(_id);
				AuctionManager.getInstance().getAuctions().remove(aucId);
				return;
			}
			if(_sellerId > 0)
			{
				returnItem(_sellerClanName, _highestBidderMaxBid, true);
				returnItem(_sellerClanName, ClanHallManager.getInstance().getAuctionableHallById(_itemId).getLease(), false);
			}
			deleteAuctionFromDB();
			L2Clan Clan = ClanTable.getInstance().getClanByName(_bidders.get(_highestBidderId).getClanName());
			_bidders.remove(_highestBidderId);
			Clan.setAuctionBiddedAt(0, true);
			removeBids();
			ClanHallManager.getInstance().setOwner(_itemId, Clan);
		}
		else
		{
			/** Task waiting ClanHallManager is loaded every 3s */
			ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(), 3000);
		}
	}

	/**
	 * Cancel bid
	 * @param bidder
	 */
	public void cancelBid(int bidder)
	{
		synchronized(this)
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();

				statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=? AND bidderId=?");
				statement.setInt(1, _id);
				statement.setInt(2, bidder);
				statement.execute();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception: ClanHallAuctionEngine.cancelBid(String bidder): " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
			returnItem(_bidders.get(bidder).getClanName(), _bidders.get(bidder).getBid(), true);
			ClanTable.getInstance().getClanByName(_bidders.get(bidder).getClanName()).setAuctionBiddedAt(0, true);
			_bidders.clear();
			loadBid();
		}
	}

	/** Cancel auction */
	public void cancelAuction()
	{
		deleteAuctionFromDB();
		removeBids();
	}

	/** Confirm an auction */
	public void confirmAuction()
	{
		AuctionManager.getInstance().getAuctions().add(this);
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("INSERT INTO auction (id, sellerId, sellerName, sellerClanName, itemType, itemId, itemObjectId, itemName, itemQuantity, startingBid, currentBid, endDate) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, _id);
			statement.setInt(2, _sellerId);
			statement.setString(3, _sellerName);
			statement.setString(4, _sellerClanName);
			statement.setString(5, _itemType);
			statement.setInt(6, _itemId);
			statement.setInt(7, _itemObjectId);
			statement.setString(8, _itemName);
			statement.setLong(9, _itemQuantity);
			statement.setLong(10, _startingBid);
			statement.setLong(11, _currentBid);
			statement.setLong(12, _endDate);
			statement.execute();
			loadBid();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: ClanHallAuctionEngine.load(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Get var auction
	 */
	public int getId()
	{
		return _id;
	}

	public long getCurrentBid()
	{
		return _currentBid;
	}

	public long getEndDate()
	{
		return _endDate;
	}

	public int getHighestBidderId()
	{
		return _highestBidderId;
	}

	public String getHighestBidderName()
	{
		return _highestBidderName;
	}

	public long getHighestBidderMaxBid()
	{
		return _highestBidderMaxBid;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public String getItemName()
	{
		return _itemName;
	}

	public int getItemObjectId()
	{
		return _itemObjectId;
	}

	public long getItemQuantity()
	{
		return _itemQuantity;
	}

	public String getItemType()
	{
		return _itemType;
	}

	public int getSellerId()
	{
		return _sellerId;
	}

	public String getSellerName()
	{
		return _sellerName;
	}

	public String getSellerClanName()
	{
		return _sellerClanName;
	}

	public long getStartingBid()
	{
		return _startingBid;
	}

	public TIntObjectHashMap<Bidder> getBidders()
	{
		return _bidders;
	}

	public static enum ItemTypeEnum
	{
		ClanHall
	}

	public static class Bidder
	{
		private String _name;  //TODO replace with objid
		private String _clanName;
		private long _bid;
		private Calendar _timeBid;

		public Bidder(String name, String clanName, long bid, long timeBid)
		{
			_name = name;
			_clanName = clanName;
			_bid = bid;
			_timeBid = Calendar.getInstance();
			_timeBid.setTimeInMillis(timeBid);
		}

		public String getName()
		{
			return _name;
		}

		public String getClanName()
		{
			return _clanName;
		}

		public long getBid()
		{
			return _bid;
		}

		public void setBid(long bid)
		{
			_bid = bid;
		}

		public Calendar getTimeBid()
		{
			return _timeBid;
		}

		public void setTimeBid(long timeBid)
		{
			_timeBid.setTimeInMillis(timeBid);
		}
	}

	/**
	 * Task Sheduler for endAuction
	 */
	public class AutoEndTask implements Runnable
	{

		@Override
		public void run()
		{
			try
			{
				endAuction();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}
}