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
package dwo.gameserver.model.world.npc;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class CombatFlag
{
	//private static final Logger _log = LogManager.getLogger(CombatFlag.class);

	public int playerId;
	public L2ItemInstance itemInstance;
	protected L2PcInstance _player;
	private L2ItemInstance _item;
	private Location _location;
	private int _itemId;

	private int _heading;
	private int _fortId;

	// =========================================================
	// Constructor
	public CombatFlag(int fort_id, int x, int y, int z, int heading, int item_id)
	{
		_fortId = fort_id;
		_location = new Location(x, y, z, heading);
		_heading = heading;
		_itemId = item_id;
	}

	public void spawnMe()
	{
		synchronized(this)
		{
			// Init the dropped L2ItemInstance and add it in the world as a visible object at the position where mob was last
			L2ItemInstance i = ItemTable.getInstance().createItem(ProcessType.COMBATFLAG, _itemId, 1, null, null);
			i.dropMe(null, _location.getX(), _location.getY(), _location.getZ());
			itemInstance = i;
		}
	}

	public void unSpawnMe()
	{
		synchronized(this)
		{
			if(_player != null)
			{
				dropIt();
			}
			if(itemInstance != null)
			{
				itemInstance.getLocationController().decay();
			}
		}
	}

	public boolean activate(L2PcInstance player, L2ItemInstance item)
	{
		if(player.isMounted())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION));
			return false;
		}

		// Player holding it data
		_player = player;
		playerId = _player.getObjectId();
		itemInstance = null;

		// Equip with the weapon
		_item = item;
		_player.getInventory().equipItem(_item);
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
		sm.addItemName(_item);
		_player.sendPacket(sm);

		// Refresh inventory
		if(Config.FORCE_INVENTORY_UPDATE)
		{
			_player.sendPacket(new ItemList(_player, false));
		}
		else
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(_item);
			_player.sendPacket(iu);
		}

		// Refresh player stats
		_player.broadcastUserInfo();
		_player.setCombatFlagEquipped(true);
		return true;
	}

	public void dropIt()
	{
		// Reset player stats
		_player.setCombatFlagEquipped(false);
		long slot = _player.getInventory().getSlotFromItem(_item);
		_player.getInventory().unEquipItemInBodySlot(slot);
		_player.destroyItem(ProcessType.COMBATFLAG, _item, null, true);
		_item = null;
		_player.broadcastUserInfo();
		_player = null;
		playerId = 0;
	}
}
