package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.network.game.serverpackets.packet.changeattribute.ExChangeAttributeItemList;
import javolution.util.FastList;

import java.util.List;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 21.09.11
 * Time: 17:14
 */

public class ChangeAttribute implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		List<L2ItemInstance> _items = new FastList<>();

		for(L2ItemInstance items : playable.getInventory().getItems())
		{
			if(items.getAttackElementPower() > 0)
			{
				CrystalGrade grade = items.getItem().getItemGrade();
				switch(item.getItemId())
				{
					case 33502:
						if(grade == CrystalGrade.S || grade == CrystalGrade.S80)
						{
							_items.add(items);
						}
						break;
					case 33833:
						if(grade == CrystalGrade.S)
						{
							_items.add(items);
						}
						break;
					case 33834:
						if(grade == CrystalGrade.S80)
						{
							_items.add(items);
						}
						break;
					case 33835:
						if(grade == CrystalGrade.R)
						{
							_items.add(items);
						}
						break;
					case 33836:
						if(grade == CrystalGrade.R95)
						{
							_items.add(items);
						}
						break;
					case 33837:
						if(grade == CrystalGrade.R99)
						{
							_items.add(items);
						}
						break;
					case 35749:
						if(grade == CrystalGrade.R || grade == CrystalGrade.R95 || grade == CrystalGrade.R99)
						{
							_items.add(items);
						}
						break;
				}
			}
		}
		playable.sendPacket(new ExChangeAttributeItemList(_items, item.getObjectId()));
		return true;
	}
}
