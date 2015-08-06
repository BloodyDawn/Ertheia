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
package dwo.gameserver.model.items.multisell;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import javolution.util.FastList;

import java.util.ArrayList;

public class PreparedListContainer extends ListContainer
{
	private int _npcObjectId;

	public PreparedListContainer(ListContainer template, boolean showAll, L2PcInstance player, L2Npc npc)
	{
		super(template.getListId());
		_keepEnchant = template.isKeepEnchant();
		_allowAugmentedItems = template.isAllowAugmentedItems();
		_allowElementalItems = template.isAllowElementalItems();
		_chanceBuy = template.isChanceBuy();
		_taxFree = template.isTaxFree();
		double taxRate = 0;

		if(npc != null)
		{
			_npcObjectId = npc.getObjectId();
			if(_taxFree && npc.getIsInTown() && npc.getCastle().getOwnerId() > 0)
			{
				taxRate = npc.getCastle().getTaxRate();
			}
		}

		if(!showAll || template.isKeepEnchant())
		{
			if(player == null)
			{
				return;
			}

			L2ItemInstance[] items;
			items = template.isKeepEnchant() ? player.getInventory().getUniqueItemsByEnchantLevel(false, false, false) : player.getInventory().getUniqueItems(false, false, false);

			// size is not known - using FastList
			_entries = new FastList<>();

			if(showAll)
			{
				for(Entry ent : template.getEntries())
				{
					_entries.add(new PreparedEntry(ent, null, _taxFree, false, false, false, taxRate));
				}
			}

			for(L2ItemInstance item : items)
			{
				// only do the matchup on equipable items that are not currently equipped
				// so for each appropriate item, produce a set of entries for the multisell list.
				if(!item.isEquipped())
				{
					// loop through the entries to see which ones we wish to include
					for(Entry ent : template.getEntries())
					{
						// check ingredients of this entry to see if it's an entry we'd like to include.
						for(Ingredient ing : ent.getIngredients())
						{
							if(item.getItemId() == ing.getItemId())
							{

								if(showAll && item.getEnchantLevel() == 0)
								{
									break;
								}

								if(!_allowAugmentedItems && item.isAugmented())
								{
									break;
								}

								_entries.add(new PreparedEntry(ent, item, _taxFree, _keepEnchant, _allowAugmentedItems, _allowElementalItems, taxRate));
								break; // next entry
							}
							if(!template.isShowAll())
							{
								break; // Проверяется только первый Ingredient
							}
						}
					}
				}
			}
		}
		else
		{
			_entries = new ArrayList<>(template.getEntries().size());
			for(Entry ent : template.getEntries())
			{
				_entries.add(new PreparedEntry(ent, null, _taxFree, false, false, false, taxRate));
			}
		}
	}

	public boolean checkNpcObjectId(int npcObjectId)
	{
		return _npcObjectId == 0 || _npcObjectId == npcObjectId;
	}
}