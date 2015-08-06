package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.attribute.ExChooseInventoryAttributeItem;
import javolution.util.FastList;

import java.util.List;

/**
 * @author Keiichi
 */

public class EnchantAttribute implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance activeChar = (L2PcInstance) playable;
		if(activeChar.isCastingNow())
		{
			return false;
		}

		if(activeChar.isEnchanting())
		{
			activeChar.sendPacket(SystemMessageId.ENCHANTMENT_ALREADY_IN_PROGRESS);
			return false;
		}
		List<L2ItemInstance> _items = new FastList<>();
		for(L2ItemInstance items : playable.getInventory().getItems())
		{
			CrystalGrade grade = items.getItem().getItemGrade();

			/* Только от S до R99 можно атрибутить! */
			switch(grade)
			{
				case S:
				case S80:
				case S84:
				case R:
				case R95:
				case R99:
					_items.add(items);
					break;
			}
		}
		activeChar.setActiveEnchantAttrItem(item);
		activeChar.sendPacket(new ExChooseInventoryAttributeItem(_items, item.getItemId(), item.getCount()));
		return true;
	}
}
