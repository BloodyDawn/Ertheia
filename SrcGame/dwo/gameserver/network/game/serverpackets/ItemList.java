package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExAdenaInvenCount;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestItemList;
import javolution.util.FastList;

public class ItemList extends L2GameServerPacket
{
	private PcInventory _inventory;
	private L2ItemInstance[] _items;
	private boolean _showWindow;
	private int length;
	private FastList<L2ItemInstance> questItems;

	public ItemList(L2PcInstance cha, boolean showWindow)
	{
		_inventory = cha.getInventory();
		_items = cha.getInventory().getItems();
		_showWindow = showWindow;
		questItems = FastList.newInstance();
		for(int i = 0; i < _items.length; i++)
		{
			if(_items[i] != null && _items[i].isQuestItem())
			{
				questItems.add(_items[i]); // add to questinv
				_items[i] = null; // remove from list
			}
			else
			{
				length++; // increase size
			}
		}
	}

    @Override
    public void runImpl()
    {
        getClient().sendPacket(new ExQuestItemList(questItems, getClient().getActiveChar().getInventory()));
        getClient().sendPacket(new ExAdenaInvenCount(getClient().getActiveChar()));
    }

	@Override
	protected void writeImpl()
	{
		writeH(_showWindow ? 0x01 : 0x00);
		writeH(length);

		for(L2ItemInstance temp : _items)
		{
			if(temp == null || temp.getItem() == null)
			{
				continue;
			}
			writeItemInfo(temp);
		}

		writeInventoryBlock(_inventory);
	}
}
