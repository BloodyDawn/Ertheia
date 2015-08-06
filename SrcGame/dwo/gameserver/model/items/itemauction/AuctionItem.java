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
package dwo.gameserver.model.items.itemauction;

import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.L2Augmentation;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.stats.StatsSet;

/**
 * @author Forsaiken
 */
public class AuctionItem
{
	private final int _auctionItemId;
	private final int _auctionLength;
	private final long _auctionInitBid;

	private final int _itemId;
	private final long _itemCount;
	private final StatsSet _itemExtra;

	public AuctionItem(int auctionItemId, int auctionLength, long auctionInitBid, int itemId, long itemCount, StatsSet itemExtra)
	{
		_auctionItemId = auctionItemId;
		_auctionLength = auctionLength;
		_auctionInitBid = auctionInitBid;

		_itemId = itemId;
		_itemCount = itemCount;
		_itemExtra = itemExtra;
	}

	public boolean checkItemExists()
	{
		L2Item item = ItemTable.getInstance().getTemplate(_itemId);
		return item != null;
	}

	public int getAuctionItemId()
	{
		return _auctionItemId;
	}

	public int getAuctionLength()
	{
		return _auctionLength;
	}

	public long getAuctionInitBid()
	{
		return _auctionInitBid;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public long getItemCount()
	{
		return _itemCount;
	}

	public L2ItemInstance createNewItemInstance()
	{
		L2ItemInstance item = ItemTable.getInstance().createItem(ProcessType.ITEM_AUCTION, _itemId, _itemCount, null, null);

		item.setEnchantLevel(item.getDefaultEnchantLevel());

		int augmentationId = _itemExtra.getInteger("augmentation_id", 0);
		if(augmentationId != 0)
		{
			int augmentationSkillId = _itemExtra.getInteger("augmentation_skill_id", 0);
			int augmentationSkillLevel = _itemExtra.getInteger("augmentation_skill_lvl", 0);
			item.setAugmentation(new L2Augmentation(augmentationId, augmentationSkillId, augmentationSkillLevel));
		}

		return item;
	}
}