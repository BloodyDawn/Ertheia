package dwo.gameserver.instancemanager;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.type.L2EtcItemType;
import dwo.gameserver.util.arrays.L2FastList;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ItemsOnGroundManager
{
	static final Logger _log = LogManager.getLogger(ItemsOnGroundManager.class);
	private final StoreInDb _task = new StoreInDb();
	protected List<L2ItemInstance> _items = new L2FastList<>(true);

	private ItemsOnGroundManager()
	{
		if(Config.SAVE_DROPPED_ITEM_INTERVAL > 0)
		{
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(_task, Config.SAVE_DROPPED_ITEM_INTERVAL, Config.SAVE_DROPPED_ITEM_INTERVAL);
		}
		load();
	}

	public static ItemsOnGroundManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private void load()
	{
		// If SaveDroppedItem is false, may want to delete all items previously stored to avoid add old items on reactivate
		if(!Config.SAVE_DROPPED_ITEM && Config.CLEAR_DROPPED_ITEM_TABLE)
		{
			emptyTable();
		}

		if(!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}

		// if DestroyPlayerDroppedItem was previously  false, items curently protected will be added to ItemsAutoDestroy
		if(Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				String str = null;
				if(!Config.DESTROY_EQUIPABLE_PLAYER_ITEM) //Recycle misc. items only
				{
					str = "UPDATE itemsonground SET drop_time=? WHERE drop_time=-1 and equipable=0";
				}
				else if(Config.DESTROY_EQUIPABLE_PLAYER_ITEM) //Recycle all items including equip-able
				{
					str = "UPDATE itemsonground SET drop_time=? WHERE drop_time=-1";
				}
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(str);
				statement.setLong(1, System.currentTimeMillis());
				statement.execute();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error while updating table ItemsOnGround " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}

		//Add items to world
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		L2ItemInstance item;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable FROM itemsonground");
			int count = 0;
			rset = statement.executeQuery();
			while(rset.next())
			{
				item = new L2ItemInstance(rset.getInt(1), rset.getInt(2));
				WorldManager.getInstance().storeObject(item);
				if(item.isStackable() && rset.getInt(3) > 1) //this check and..
				{
					item.setCount(rset.getInt(3));
				}
				if(rset.getInt(4) > 0) // this, are really necessary?
				{
					item.setEnchantLevel(rset.getInt(4));
				}
				item.getLocationController().setXYZ(rset.getInt(5), rset.getInt(6), rset.getInt(7));
				item.setDropTime(rset.getLong(8));
				item.setProtected(rset.getLong(8) == -1);
				item.getLocationController().setVisible(true);
				WorldManager.getInstance().addVisibleObject(item, item.getLocationController().getWorldRegion());
				_items.add(item);
				count++;
				// add to ItemsAutoDestroy only items not protected
				if(!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
				{
					if(rset.getLong(8) > -1)
					{
						if(Config.AUTODESTROY_ITEM_AFTER > 0 && item.getItemType() != L2EtcItemType.HERB || Config.HERB_AUTO_DESTROY_TIME > 0 && item.getItemType() == L2EtcItemType.HERB)
						{
							ItemsOnGroundAutoDestroyManager.getInstance().addItem(item);
						}
					}
				}
			}
			if(count > 0)
			{
				_log.log(Level.INFO, "ItemsOnGroundManager: restored " + count + " items.");
			}
			else
			{
				_log.log(Level.INFO, "ItemsOnGroundManager: Initializing");
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while loading ItemsOnGround " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
			if(Config.EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD)
			{
				emptyTable();
			}
		}
	}

	public void save(L2ItemInstance item)
	{
		if(!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		_items.add(item);
	}

	public void removeObject(L2ItemInstance item)
	{
		if(Config.SAVE_DROPPED_ITEM)
		{
			_items.remove(item);
		}
	}

	public void saveInDb()
	{
		_task.run();
	}

	public void emptyTable()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM itemsonground");
			statement.execute();
		}
		catch(Exception e1)
		{
			_log.log(Level.ERROR, "Error while cleaning table ItemsOnGround " + e1.getMessage(), e1);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private static class SingletonHolder
	{
		protected static final ItemsOnGroundManager _instance = new ItemsOnGroundManager();
	}

	protected class StoreInDb extends Thread
	{
		@Override
		public void run()
		{
			synchronized(this)
			{
				if(!Config.SAVE_DROPPED_ITEM)
				{
					return;
				}

				emptyTable();

				if(_items.isEmpty())
				{
					return;
				}

				ThreadConnection con = null;
				FiltredPreparedStatement statement = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("INSERT INTO itemsonground(object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable) VALUES(?,?,?,?,?,?,?,?,?)");

					for(L2ItemInstance item : _items)
					{
						if(item == null)
						{
							continue;
						}

						if(CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
						{
							continue; // Cursed Items not saved to ground, prevent double save
						}

						try
						{
							statement.setInt(1, item.getObjectId());
							statement.setInt(2, item.getItemId());
							statement.setLong(3, item.getCount());
							statement.setInt(4, item.getEnchantLevel());
							statement.setInt(5, item.getX());
							statement.setInt(6, item.getY());
							statement.setInt(7, item.getZ());
							statement.setLong(8, item.isProtected() ? -1 : item.getDropTime()); //item is protected or AutoDestroyed
							statement.setLong(9, item.isEquipable() ? 1 : 0); //set equip-able
							statement.execute();
							statement.clearParameters();
						}
						catch(Exception e)
						{
							_log.log(Level.ERROR, "Error while inserting into table ItemsOnGround: " + e.getMessage(), e);
						}
					}
				}
				catch(SQLException e)
				{
					_log.log(Level.ERROR, "SQL error while storing items on ground: " + e.getMessage(), e);
				}
				finally
				{
					DatabaseUtils.closeDatabaseCS(con, statement);
				}
				_items.clear();
			}
		}
	}
}