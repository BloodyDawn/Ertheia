package dwo.gameserver.model.items.itemcontainer;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance.ItemLocation;

public class PcWarehouse extends Warehouse
{
	private L2PcInstance _owner;

	public PcWarehouse(L2PcInstance owner)
	{
		_owner = owner;
	}

	@Override
	public L2PcInstance getOwner()
	{
		return _owner;
	}

	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.WAREHOUSE;
	}

	@Override
	public String getName()
	{
		return "Warehouse";
	}

	@Override
	public boolean validateCapacity(long slots)
	{
		return _items.size() + slots <= _owner.getWareHouseLimit();
	}
}