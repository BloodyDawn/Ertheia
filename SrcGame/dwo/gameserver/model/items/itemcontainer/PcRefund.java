package dwo.gameserver.model.items.itemcontainer;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance.ItemLocation;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import org.apache.log4j.Level;

/**
 * @author DS
 */

public class PcRefund extends ItemContainer
{
	private final L2PcInstance _owner;

	public PcRefund(L2PcInstance owner)
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
		return ItemLocation.REFUND;
	}

	@Override
	public String getName()
	{
		return "Refund";
	}

	@Override
	protected void addItem(L2ItemInstance item)
	{
		super.addItem(item);
		try
		{
			if(getSize() > 12)
			{
				L2ItemInstance removedItem = _items.remove(0);
				if(removedItem != null)
				{
					ItemTable.getInstance().destroyItem(ProcessType.REFUND, removedItem, _owner, null);
					removedItem.updateDatabase(true);
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "addItem()", e);
		}
	}

	@Override
	public void refreshWeight()
	{
	}

	@Override
	public void deleteMe()
	{
		try
		{
			_items.stream().filter(item -> item != null).forEach(item -> {
				ItemTable.getInstance().destroyItem(ProcessType.REFUND, item, _owner, null);
				item.updateDatabase(true);
			});
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}
		_items.clear();
	}

	@Override
	public void restore()
	{
	}
}