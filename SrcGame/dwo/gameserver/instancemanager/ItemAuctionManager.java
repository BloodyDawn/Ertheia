package dwo.gameserver.instancemanager;

import dwo.config.Config;
import dwo.config.FilePath;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.items.itemauction.ItemAuctionInstance;
import dwo.gameserver.util.crypt.datapack.CryptUtil;
import dwo.gameserver.util.database.DatabaseUtils;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Forsaiken
 */
public class ItemAuctionManager
{
	private static final Logger _log = LogManager.getLogger(ItemAuctionManager.class);
	private final TIntObjectHashMap<ItemAuctionInstance> _managerInstances;
	private final AtomicInteger _auctionIds;

	private ItemAuctionManager()
	{
		_managerInstances = new TIntObjectHashMap<>();
		_auctionIds = new AtomicInteger(1);

		if(!Config.ALT_ITEM_AUCTION_ENABLED)
		{
			_log.log(Level.INFO, "ItemAuctionManager: Disabled by config.");
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT auctionId FROM item_auction ORDER BY auctionId DESC LIMIT 0, 1");
			rset = statement.executeQuery();
			if(rset.next())
			{
				_auctionIds.set(rset.getInt(1) + 1);
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "ItemAuctionManager: Failed loading auctions.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		File file = FilePath.ITEM_AUCTION_MANAGER;
		if(!file.exists())
		{
			_log.log(Level.WARN, "ItemAuctionManager: Missing ItemAuctions.xml!");
			return;
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);

		try
		{
			Document doc = factory.newDocumentBuilder().parse(CryptUtil.decryptOnDemand(file), file.getAbsolutePath());
			for(Node na = doc.getFirstChild(); na != null; na = na.getNextSibling())
			{
				if("list".equalsIgnoreCase(na.getNodeName()))
				{
					for(Node nb = na.getFirstChild(); nb != null; nb = nb.getNextSibling())
					{
						if("instance".equalsIgnoreCase(nb.getNodeName()))
						{
							NamedNodeMap nab = nb.getAttributes();
							int instanceId = Integer.parseInt(nab.getNamedItem("id").getNodeValue());

							if(_managerInstances.containsKey(instanceId))
							{
								throw new Exception("Dublicated instanceId " + instanceId);
							}

							ItemAuctionInstance instance = new ItemAuctionInstance(instanceId, _auctionIds, nb);
							_managerInstances.put(instanceId, instance);
						}
					}
				}
			}
			_log.log(Level.INFO, "ItemAuctionManager: Loaded " + _managerInstances.size() + " instance(s).");
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "ItemAuctionManager: Failed loading auctions from xml.", e);
		}
	}

	public static ItemAuctionManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public static void deleteAuction(int auctionId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM item_auction WHERE auctionId=?");
			statement.setInt(1, auctionId);
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("DELETE FROM item_auction_bid WHERE auctionId=?");
			statement.setInt(1, auctionId);
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "L2ItemAuctionManagerInstance: Failed deleting auction: " + auctionId, e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void shutdown()
	{
		for(ItemAuctionInstance instance : _managerInstances.values(new ItemAuctionInstance[0]))
		{
			instance.shutdown();
		}
	}

	public ItemAuctionInstance getManagerInstance(int instanceId)
	{
		return _managerInstances.get(instanceId);
	}

	public int getNextAuctionId()
	{
		return _auctionIds.getAndIncrement();
	}

	private static class SingletonHolder
	{
		protected static final ItemAuctionManager _instance = new ItemAuctionManager();
	}
}