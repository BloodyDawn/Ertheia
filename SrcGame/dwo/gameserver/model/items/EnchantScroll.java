package dwo.gameserver.model.items;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 18.11.2011
 * Time: 20:42:41
 */

import dwo.config.Config;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.stats.StatsSet;

import java.util.List;

public class EnchantScroll extends EnchantItem
{
	private final boolean _isBlessed;
	private final boolean _isSafe;

	/**
	 * @param set
	 * @param items
	 */
	public EnchantScroll(StatsSet set, List<Integer> items)
	{
		super(set, items);

		_isBlessed = set.getBool("isBlessed", false);
		_isSafe = set.getBool("isSafe", false);
	}

	/**
	 * @return true for blessed scrolls
	 */
	public boolean isBlessed()
	{
		return _isBlessed;
	}

	/**
	 * @return true for safe-enchant scrolls (enchant level will remain on failure)
	 */
	public boolean isSafe()
	{
		return _isSafe;
	}

	/**
	 * @param enchantItem
	 * @param supportItem
	 * @return
	 */
	public boolean isValid(L2ItemInstance enchantItem, EnchantItem supportItem)
	{
		// blessed scrolls can't use support items
		if(supportItem != null && (!supportItem.isValid(enchantItem) || _isBlessed))
		{
			return false;
		}

		return isValid(enchantItem);
	}

	/**
	 * @param enchantItem
	 * @param supportItem
	 * @return
	 */
	public double getChance(L2ItemInstance enchantItem, EnchantItem supportItem)
	{
		if(!isValid(enchantItem, supportItem))
		{
			return -1;
		}

		boolean fullBody = enchantItem.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR;
		if(enchantItem.getEnchantLevel() < Config.ENCHANT_SAFE_MAX || fullBody && enchantItem.getEnchantLevel() < Config.ENCHANT_SAFE_MAX_FULL)
		{
			return 100;
		}

		double chance = _chanceAdd;

		if(supportItem != null && !_isBlessed)
		{
			chance *= supportItem.getChanceAdd();
		}

		return chance;
	}
}

