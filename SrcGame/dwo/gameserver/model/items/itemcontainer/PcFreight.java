package dwo.gameserver.model.items.itemcontainer;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance.ItemLocation;
import dwo.gameserver.model.skills.stats.Stats;

public class PcFreight extends ItemContainer
{
	private final L2PcInstance _owner;
	private final int _ownerId;

	public PcFreight(int object_id)
	{
		_owner = null;
		_ownerId = object_id;
		restore();
	}

	public PcFreight(L2PcInstance owner)
	{
		_owner = owner;
		_ownerId = owner.getObjectId();
	}

	@Override
	public L2PcInstance getOwner()
	{
		return _owner;
	}

	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.FREIGHT;
	}

	/**
	 * @return the quantity of items in the inventory
	 */
	@Override
	public String getName()
	{
		return "Freight";
	}

	@Override
	public int getOwnerId()
	{
		return _ownerId;
	}

	@Override
	public void refreshWeight()
	{
	}

	@Override
	public boolean validateCapacity(long slots)
	{
		int curSlots = _owner == null ? Config.ALT_FREIGHT_SLOTS : Config.ALT_FREIGHT_SLOTS + (int) _owner.getStat().calcStat(Stats.FREIGHT_LIM, 0, null, null);
		return getSize() + slots <= curSlots;
	}
}