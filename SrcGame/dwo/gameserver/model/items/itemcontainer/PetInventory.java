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

import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance.ItemLocation;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.base.type.L2EtcItemType;

public class PetInventory extends Inventory
{
	private final L2PetInstance _owner;

	public PetInventory(L2PetInstance owner)
	{
		_owner = owner;
	}

	@Override
	public L2PetInstance getOwner()
	{
		return _owner;
	}

	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.PET;
	}

	@Override
	public int getOwnerId()
	{
		// gets the L2PcInstance-owner's ID
		int id;
		try
		{
			id = _owner.getOwner().getObjectId();
		}
		catch(NullPointerException e)
		{
			return 0;
		}
		return id;
	}

	@Override
	public boolean validateCapacity(long slots)
	{
		return _items.size() + slots <= _owner.getInventoryLimit();
	}

	@Override
	public boolean validateWeight(long weight)
	{
		return _totalWeight + weight <= _owner.getMaxLoad();
	}

	public boolean validateCapacity(L2ItemInstance item)
	{
		int slots = 0;

		if(!(item.isStackable() && getItemByItemId(item.getItemId()) != null) && item.getItemType() != L2EtcItemType.HERB)
		{
			slots++;
		}

		return validateCapacity(slots);
	}

	public boolean validateWeight(L2ItemInstance item, long count)
	{
		int weight = 0;
		L2Item template = ItemTable.getInstance().getTemplate(item.getItemId());
		if(template == null)
		{
			return false;
		}
		weight += count * template.getWeight();
		return validateWeight(weight);
	}

	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.PET_EQUIP;
	}

	/**
	 * Refresh the weight of equipment loaded
	 */
	@Override
	protected void refreshWeight()
	{
		super.refreshWeight();
		_owner.updateAndBroadcastStatus(1);
	}

	@Override
	public void restore()
	{
		super.restore();
		// check for equiped items from other pets
		_items.stream().filter(L2ItemInstance::isEquipped).forEach(item -> {
			if(!item.getItem().checkCondition(item, _owner, _owner, false))
			{
				unEquipItemInSlot(item.getLocationSlot());
			}
		});
	}

	public void transferItemsToOwner()
	{
		for(L2ItemInstance item : _items)
		{
			_owner.transferItem(ProcessType.PETTRANSFER, item.getObjectId(), item.getCount(), _owner.getOwner().getInventory(), _owner.getOwner(), _owner);
		}
	}
}
