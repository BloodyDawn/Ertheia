package dwo.gameserver.datatables.sql;

import dwo.config.Config;
import dwo.gameserver.LoginServerThread;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.TradeItem;
import dwo.gameserver.model.player.L2ManufactureItem;
import dwo.gameserver.model.player.L2ManufactureList;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.L2GameClient.GameClientState;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.Calendar;

public class OfflineTradersTable
{
	// SQL DEFINITIONS
	private static final String SAVE_OFFLINE_STATUS = "INSERT INTO character_offline_trade (`charId`,`time`,`type`,`title`) VALUES (?,?,?,?)";
	private static final String SAVE_ITEMS = "INSERT INTO character_offline_trade_items (`charId`,`item`,`count`,`price`) VALUES (?,?,?,?)";
	private static final String CLEAR_OFFLINE_TABLE = "DELETE FROM character_offline_trade";
	private static final String CLEAR_OFFLINE_TABLE_ITEMS = "DELETE FROM character_offline_trade_items";
	private static final String LOAD_OFFLINE_STATUS = "SELECT * FROM character_offline_trade";
	private static final String LOAD_OFFLINE_ITEMS = "SELECT * FROM character_offline_trade_items WHERE charId = ?";
	private static Logger _log = LogManager.getLogger(OfflineTradersTable.class);

	public static void storeOffliners()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement stm = null;
		FiltredPreparedStatement stm_items = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			stm = con.prepareStatement(CLEAR_OFFLINE_TABLE);
			stm.execute();
			DatabaseUtils.closeStatement(stm);
			stm = con.prepareStatement(CLEAR_OFFLINE_TABLE_ITEMS);
			stm.execute();
			DatabaseUtils.closeStatement(stm);

			con.setAutoCommit(false); // avoid halfway done
			stm = con.prepareStatement(SAVE_OFFLINE_STATUS);
			stm_items = con.prepareStatement(SAVE_ITEMS);

			// TextBuilder items = TextBuilder.newInstance();
			for(L2PcInstance pc : WorldManager.getInstance().getAllPlayersArray())
			{
				try
				{
					if(pc.getPrivateStoreType() != PlayerPrivateStoreType.NONE && (pc.getClient() == null || pc.getClient().isDetached()))
					{
						stm.setInt(1, pc.getObjectId()); // Char Id
						stm.setLong(2, pc.getOfflineStartTime());
						stm.setInt(3, pc.getPrivateStoreType().ordinal()); // store type
						String title = null;

						switch(pc.getPrivateStoreType())
						{
							case BUY:
								if(!Config.OFFLINE_TRADE_ENABLE)
								{
									continue;
								}
								title = pc.getBuyList().getTitle();
								for(TradeItem i : pc.getBuyList().getItems())
								{
									stm_items.setInt(1, pc.getObjectId());
									stm_items.setInt(2, i.getItem().getItemId());
									stm_items.setLong(3, i.getCount());
									stm_items.setLong(4, i.getPrice());
									stm_items.executeUpdate();
									stm_items.clearParameters();
								}
								break;
							case SELL:
							case SELL_PACKAGE:
								if(!Config.OFFLINE_TRADE_ENABLE)
								{
									continue;
								}
								title = pc.getSellList().getTitle();
								for(TradeItem i : pc.getSellList().getItems())
								{
									stm_items.setInt(1, pc.getObjectId());
									stm_items.setInt(2, i.getObjectId());
									stm_items.setLong(3, i.getCount());
									stm_items.setLong(4, i.getPrice());
									stm_items.executeUpdate();
									stm_items.clearParameters();
								}
								break;
							case MANUFACTURE:
								if(!Config.OFFLINE_CRAFT_ENABLE)
								{
									continue;
								}
								title = pc.getCreateList().getStoreName();
								for(L2ManufactureItem i : pc.getCreateList().getList())
								{
									stm_items.setInt(1, pc.getObjectId());
									stm_items.setInt(2, i.getRecipeId());
									stm_items.setLong(3, 0);
									stm_items.setLong(4, i.getCost());
									stm_items.executeUpdate();
									stm_items.clearParameters();
								}
						}
						stm.setString(4, title);
						stm.executeUpdate();
						stm.clearParameters();
					}
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "OfflineTradersTable[storeTradeItems()]: Error while saving offline trader: " + pc.getObjectId() + ' ' + e, e);
				}
			}
			DatabaseUtils.closeStatement(stm);
			_log.log(Level.INFO, "Offline traders stored.");
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "OfflineTradersTable[storeTradeItems()]: Error while saving offline traders: " + e, e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, stm_items);
		}
	}

	public static void restoreOfflineTraders()
	{
		_log.log(Level.INFO, "Loading offline traders...");
		ThreadConnection con = null;
		FiltredPreparedStatement stm = null;
		FiltredPreparedStatement stm_items = null;
		ResultSet rs = null;
		ResultSet items = null;
		int nTraders = 0;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			stm = con.prepareStatement(LOAD_OFFLINE_STATUS);
			rs = stm.executeQuery();
			while(rs.next())
			{
				long time = rs.getLong("time");
				if(Config.OFFLINE_MAX_DAYS > 0)
				{
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(time);
					cal.add(Calendar.DAY_OF_YEAR, Config.OFFLINE_MAX_DAYS);
					if(cal.getTimeInMillis() <= System.currentTimeMillis())
					{
						continue;
					}
				}

				PlayerPrivateStoreType type = PlayerPrivateStoreType.values()[rs.getInt("type")];
				if(type == PlayerPrivateStoreType.NONE)
				{
					continue;
				}

				L2PcInstance player = null;

				try
				{
					L2GameClient client = new L2GameClient(null);
					client.setDetached(true);
					player = L2PcInstance.load(rs.getInt("charId"));
					client.setActiveChar(player);
					player.setOnlineStatus(true, false);
					client.setAccountName(player.getAccountNamePlayer());
					client.setState(GameClientState.IN_GAME);
					player.setClient(client);
					player.setOfflineStartTime(time);
					player.getLocationController().spawn(player.getX(), player.getY(), player.getZ());
					LoginServerThread.getInstance().addGameServerLogin(player.getAccountName(), client);
					stm_items = con.prepareStatement(LOAD_OFFLINE_ITEMS);
					stm_items.setInt(1, player.getObjectId());
					items = stm_items.executeQuery();

					switch(type)
					{
						case BUY:
							while(items.next())
							{
								if(player.getBuyList().addItemByItemId(items.getInt(2), items.getLong(3), items.getLong(4)) == null)
								{
									throw new NullPointerException();
								}
							}
							player.getBuyList().setTitle(rs.getString("title"));
							break;
						case SELL:
						case SELL_PACKAGE:
							while(items.next())
							{
								if(player.getSellList().addItem(items.getInt(2), items.getLong(3), items.getLong(4)) == null)
								{
									throw new NullPointerException();
								}
							}
							player.getSellList().setTitle(rs.getString("title"));
							player.getSellList().setPackaged(type == PlayerPrivateStoreType.SELL_PACKAGE);
							break;
						case MANUFACTURE:
							L2ManufactureList createList = new L2ManufactureList();
							while(items.next())
							{
								createList.add(new L2ManufactureItem(items.getInt(2), items.getLong(4)));
							}
							player.setCreateList(createList);
							player.getCreateList().setStoreName(rs.getString("title"));
							break;
					}
					DatabaseUtils.closeResultSet(items);
					DatabaseUtils.closeStatement(stm_items);

					player.sitDown();
					if(Config.OFFLINE_SET_NAME_COLOR)
					{
						player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
					}
					player.setPrivateStoreType(type);
					player.setOnlineStatus(true, true);
					player.restoreEffects();
					player.broadcastUserInfo();
					nTraders++;
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "OfflineTradersTable[loadOffliners()]: Error loading trader: " + player, e);
					if(player != null)
					{
						player.getLocationController().delete();
					}
				}
			}
			DatabaseUtils.closeResultSet(rs);
			DatabaseUtils.closeStatement(stm);
			_log.log(Level.INFO, "Loaded: " + nTraders + " offline trader(s)");
			stm = con.prepareStatement(CLEAR_OFFLINE_TABLE);
			stm.execute();
			DatabaseUtils.closeStatement(stm);
			stm = con.prepareStatement(CLEAR_OFFLINE_TABLE_ITEMS);
			stm.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "OfflineTradersTable[loadOffliners()]: Error while loading offline traders: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, stm);
		}
	}
}
