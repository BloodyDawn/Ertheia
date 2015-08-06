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
package dwo.gameserver.model.items.itemcontainer;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance.ItemLocation;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;

import java.sql.ResultSet;

/**
 * @author DS
 */
public class Mail extends ItemContainer
{
	private final int _ownerId;
	private int _messageId;

	public Mail(int objectId, int messageId)
	{
		_ownerId = objectId;
		_messageId = messageId;
	}

	@Override
	public L2PcInstance getOwner()
	{
		return null;
	}

	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.MAIL;
	}

	@Override
	public String getName()
	{
		return "Mail";
	}

	@Override
	public int getOwnerId()
	{
		return _ownerId;
	}

	@Override
	protected void addItem(L2ItemInstance item)
	{
		super.addItem(item);
		item.setLocation(getBaseLocation(), _messageId);
	}

	@Override
	public void updateDatabase()
	{
		_items.stream().filter(item -> item != null).forEach(item -> item.updateDatabase(true));
	}

	@Override
	public void restore()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time, skin_id FROM items WHERE owner_id=? AND loc=? AND loc_data=?");
			statement.setInt(1, _ownerId);
			statement.setString(2, getBaseLocation().name());
			statement.setInt(3, _messageId);
			ResultSet inv = statement.executeQuery();

			L2ItemInstance item;
			while(inv.next())
			{
				item = L2ItemInstance.restoreFromDb(_ownerId, inv);
				if(item == null)
				{
					continue;
				}

				WorldManager.getInstance().storeObject(item);

				// If stackable item is found just add to current quantity
				if(item.isStackable() && getItemByItemId(item.getItemId()) != null)
				{
					addItem(ProcessType.RESTORE, item, null, null);
				}
				else
				{
					addItem(item);
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "could not restore container:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

    /*
      * Allow saving of the items without owner
      */

	public int getMessageId()
	{
		return _messageId;
	}

	public void setNewMessageId(int messageId)
	{
		_messageId = messageId;
		for(L2ItemInstance item : _items)
		{
			if(item == null)
			{
				continue;
			}
			item.setLocation(getBaseLocation(), messageId);
		}
		updateDatabase();
	}

	public void returnToWh(ItemContainer wh)
	{
		for(L2ItemInstance item : _items)
		{
			if(item == null)
			{
				continue;
			}
			if(wh == null)
			{
				item.setLocation(ItemLocation.WAREHOUSE);
			}
			else
			{
				transferItem(ProcessType.EXPIRE, item.getObjectId(), item.getCount(), wh, null, null);
			}
		}
	}
}