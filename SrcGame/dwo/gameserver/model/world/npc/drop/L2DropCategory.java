package dwo.gameserver.model.world.npc.drop;

import dwo.config.Config;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.util.Rnd;

import java.util.ArrayList;
import java.util.List;

public class L2DropCategory
{
	private final List<L2DropData> _drops;
	private final int _categoryType;
	private int _categoryChance; // a sum of chances for calculating if an item will be dropped from this category
	private int _categoryBalancedChance; // sum for balancing drop selection inside categories in high rate servers

	public L2DropCategory(int categoryType)
	{
		_categoryType = categoryType;
		_drops = new ArrayList<>();
		_categoryChance = 0;
		_categoryBalancedChance = 0;
	}

	public void addDropData(L2DropData drop, boolean raid)
	{
		boolean found = false;

		if(drop.isQuestDrop())
		{
			//if (_questDrops == null)
			//	_questDrops = new FastList<L2DropData>(0);
			//_questDrops.add(drop);
		}
		else
		{
			if(Config.CUSTOM_DROPLIST_TABLE)
			{
				// If the drop exists is replaced
				for(L2DropData d : _drops)
				{
					if(d.getItemId() == drop.getItemId())
					{
						d.setMinDrop(drop.getMinDrop());
						d.setMaxDrop(drop.getMaxDrop());
						if(d.getChance() != drop.getChance())
						{
							// Re-calculate Chance
							_categoryBalancedChance -= Math.min(d.getChance() * Config.RATE_DROP_ITEMS, L2DropData.MAX_CHANCE);
							d.setChance(drop.getChance());
							_categoryBalancedChance += Math.min(d.getChance() * Config.RATE_DROP_ITEMS, L2DropData.MAX_CHANCE);
						}
						found = true;
						break;
					}
				}
			}

			if(!found)
			{
				_drops.add(drop);
				// for drop selection inside a category: max 100 % chance for getting an item, scaling all values to that.
				_categoryBalancedChance += Math.min(drop.getChance() * (raid ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS), L2DropData.MAX_CHANCE);
			}
		}
	}

	public void setCategoryChance(int chance, boolean raid)
	{
		_categoryChance = Math.min((int) (chance * (raid ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS)), L2DropData.MAX_CHANCE);
	}

	public List<L2DropData> getItems()
	{
		return _drops;
	}

	public void clearAllDrops()
	{
		_drops.clear();
	}

	public boolean isSweep()
	{
		return _categoryType == -1;
	}

	public boolean isAdena()
	{
		return _categoryType == 0;
	}

	// this returns the chance for the category to be visited in order to check if
	// drops might come from it.  Category -1 (spoil) must always be visited
	// (but may return 0 or many drops)

	public int getCategoryChance()
	{
		return _categoryType >= 0 ? _categoryChance : L2DropData.MAX_CHANCE;
	}

	public int getCategoryBalancedChance()
	{
		return _categoryType >= 0 ? _categoryBalancedChance : L2DropData.MAX_CHANCE;
	}

	public int getCategoryType()
	{
		return _categoryType;
	}

	/**
	 * useful for seeded conditions...the category will attempt to drop only among
	 * items that are allowed to be dropped when a mob is seeded.
	 * Previously, this only included adena.  According to sh1ny, sealstones are also
	 * acceptable drops.
	 * if no acceptable drops are in the category, nothing will be dropped.
	 * otherwise, it will check for the item's chance to drop and either drop
	 * it or drop nothing.
	 *
	 * @return acceptable drop when mob is seeded, if it exists.  Null otherwise.
	 */
	public L2DropData dropSeedAllowedDropsOnly()
	{
		synchronized(this)
		{
			List<L2DropData> drops = new ArrayList<>();
			int subCatChance = 0;
			for(L2DropData drop : _drops)
			{
				if(drop.getItemId() == PcInventory.ADENA_ID || drop.getItemId() == 6360 || drop.getItemId() == 6361 || drop.getItemId() == 6362)
				{
					drops.add(drop);
					subCatChance += drop.getChance();
				}
			}

			// among the results choose one.
			if(subCatChance >= 0)
			{
				int randomIndex = Rnd.get(subCatChance);
				int sum = 0;
				for(L2DropData drop : drops)
				{
					sum += drop.getChance();

					if(sum > randomIndex)       // drop this item and exit the function
					{
						drops.clear();
						drops = null;
						return drop;
					}
				}
			}
			// since it is still within category, only drop one of the acceptable drops from the results.
			return null;
		}
	}

	/**
	 * ONE of the drops in this category is to be dropped now.
	 * to see which one will be dropped, weight all items' chances such that
	 * their sum of chances equals MAX_CHANCE.
	 * since the individual drops have their base chance, we also ought to use the
	 * base category chance for the weight.  So weight = MAX_CHANCE/basecategoryDropChance.
	 * Then get a single random number within this range.  The first item
	 * (in order of the list) whose contribution to the sum makes the
	 * sum greater than the random number, will be dropped.
	 * <p/>
	 * Edited: How _categoryBalancedChance works in high rate servers:
	 * Let's say item1 has a drop chance (when considered alone, without category) of
	 * 1 % * RATE_DROP_ITEMS and item2 has 20 % * RATE_DROP_ITEMS, and the server's
	 * RATE_DROP_ITEMS is for example 50x. Without this balancer, the relative chance inside
	 * the category to select item1 to be dropped would be 1/26 and item2 25/26, no matter
	 * what rates are used. In high rate servers people usually consider the 1 % individual
	 * drop chance should become higher than this relative chance (1/26) inside the category,
	 * since having the both items for example in their own categories would result in having
	 * a drop chance for item1 50 % and item2 1000 %. _categoryBalancedChance limits the
	 * individual chances to 100 % max, making the chance for item1 to be selected from this
	 * category 50/(50+100) = 1/3 and item2 100/150 = 2/3.
	 * This change doesn't affect calculation when drop_chance * RATE_DROP_ITEMS < 100 %,
	 * meaning there are no big changes for low rate servers and no changes at all for 1x
	 * servers.
	 *
	 * @return selected drop from category, or null if nothing is dropped.
	 */
	public L2DropData dropOne()
	{
		synchronized(this)
		{
			int randomIndex = Rnd.get(1, 1000000);
			int sum = 0;
			for(L2DropData drop : _drops)
			{
				sum += Math.min(drop.getChance(), L2DropData.MAX_CHANCE);

				if(sum >= randomIndex)       // drop this item and exit the function
				{
					return drop;
				}
			}
			return null;
		}
	}

	// Валидация дропа
	public boolean validate()
	{
		int chanceSum = 0; // сумма шансов группы
		for(L2DropData d : _drops)
		{
			chanceSum += d.getChance();
		}
		if(chanceSum <= L2DropData.MAX_CHANCE) // всё в порядке?
		{
			return true;
		}
		double mod = L2DropData.MAX_CHANCE / chanceSum;
		for(L2DropData d : _drops)
		{
			double chance = d.getChance() * mod; // коррекция шанса группы
			d.setChance(chance);
			setCategoryChance(L2DropData.MAX_CHANCE, false);
		}
		return false;
	}
}