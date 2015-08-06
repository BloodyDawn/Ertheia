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
package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.variation.ExPutIntensiveResultForVariationMake;

/**
 * Fromat(ch) dd
 * @author  -Wooden-
 */
public class RequestConfirmRefinerItem extends AbstractRefinePacket
{
	private int _targetItemObjId;
	private int _refinerItemObjId;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		if(targetItem == null)
		{
			return;
		}

		L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		if(refinerItem == null)
		{
			return;
		}

		if(!isValid(activeChar, targetItem, refinerItem))
		{
			activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		int refinerItemId = refinerItem.getItem().getItemId();
		CrystalGrade grade = targetItem.getItem().getItemGrade();
		LifeStone ls = getLifeStone(refinerItemId);

		// Сейчас при аугментации у грейда C есть два типа гемов - ищем первый попашийся в инвентаре
		int gemStoneId = 0;
		int[] availableGems = getGemStoneId(grade);
		if(availableGems != null)
		{
			for(int id : getGemStoneId(grade))
			{
				if(activeChar.getInventory().getAllItemsByItemId(id) != null)
				{
					gemStoneId = id;
					break;
				}
			}
		}

		int gemStoneCount = getGemStoneCount(grade, ls.getGrade());

		activeChar.sendPacket(new ExPutIntensiveResultForVariationMake(_refinerItemObjId, refinerItemId, gemStoneId, gemStoneCount));
	}

	@Override
	public String getType()
	{
		return "[C] D0:2A RequestConfirmRefinerItem";
	}
}
