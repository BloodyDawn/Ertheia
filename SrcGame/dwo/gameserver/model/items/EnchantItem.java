package dwo.gameserver.model.items;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 18.11.2011
 * Time: 20:45:25
 */

import dwo.config.Config;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.skills.stats.StatsSet;

import java.util.List;

public class EnchantItem
{
	protected final int _id;
	protected final boolean _isWeapon;
	protected final CrystalGrade _grade;
	protected final int _maxEnchantLevel;
	protected final double _chanceAdd;
	protected final List<Integer> _itemIds;

	/**
	 * @param set
	 * @param items
	 */
	public EnchantItem(StatsSet set, List<Integer> items)
	{
		_id = set.getInteger("id");
		_isWeapon = set.getBool("isWeapon", true);
		_grade = CrystalGrade.valueOf(set.getString("targetGrade", "NONE"));
		_maxEnchantLevel = set.getInteger("maxEnchant", Config.MAX_ENCHANT_LEVEL);
		_chanceAdd = set.getDouble("successRate", Config.ENCHANT_CHANCE);
		_itemIds = items;
	}

	/*
		@isValid  возвращает при заточке удовлетворяет ли итем тербованиям или нет.
	*/
	public boolean isValid(L2ItemInstance enchantItem)
	{
		if(enchantItem == null)
		{
			return false;
		}
		if(!enchantItem.isEnchantable())
		{
			return false;
		}
		if(!isValidItemType(enchantItem.getItem().getType2()))
		{
			return false;
		}
		if(_maxEnchantLevel != 0 && enchantItem.getEnchantLevel() >= _maxEnchantLevel)
		{
			return false;
		}
		if(_grade != enchantItem.getItem().getItemGradeSPlus())
		{
			return false;
		}
		return !(!enchantItem.isEnchantable() && (_itemIds.isEmpty() || !_itemIds.contains(enchantItem.getItemId())) || !_itemIds.isEmpty() && !_itemIds.contains(enchantItem.getItemId()));

	}

	private boolean isValidItemType(int type2)
	{
		if(type2 == L2Item.TYPE2_WEAPON)
		{
			return _isWeapon;
		}
		if(type2 == L2Item.TYPE2_SHIELD_ARMOR || type2 == L2Item.TYPE2_ACCESSORY)
		{
			return !_isWeapon;
		}
		return false;
	}

	/**
	 * @return chance increase
	 */
	public double getChanceAdd()
	{
		return _chanceAdd;
	}

	public int getMaxEnchantLevel()
	{
		return _maxEnchantLevel;
	}

	public int getScrollId()
	{
		return _id;
	}
}