package dwo.gameserver.model.items.base;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.items.base.type.L2ItemType;

public class L2WarehouseItem
{
	private L2Item _item;
	private int _object;
	private long _count;
	private int _owner;
	private long _locationSlot;
	private int _enchant;
	private int _block;
	private CrystalGrade _grade;
	private boolean _isAugmented;
	private int _augmentationId;
	private int _customType1;
	private int _customType2;
	private int _mana;

	private int _elemAtkType = -2;
	private int _elemAtkPower;
	private int[] _elemDefAttr = {
		0, 0, 0, 0, 0, 0
	};
	private int[] _enchanteffect = {
		0, 0, 0
	};
	private int _skin;
	private int _time;

	public L2WarehouseItem(L2ItemInstance item)
	{
		_item = item.getItem();
		_object = item.getObjectId();
		_count = item.getCount();
		_owner = item.getOwnerId();
		_locationSlot = item.getLocationSlot();
		_enchant = item.getEnchantLevel();
		_block = item.isBlocked();
		_customType1 = item.getCustomType1();
		_customType2 = item.getCustomType2();
		_grade = item.getItem().getItemGrade();
		if(item.isAugmented())
		{
			_isAugmented = true;
			_augmentationId = item.getAugmentation().getAugmentationId();
		}
		else
		{
			_isAugmented = false;
		}
		_mana = item.getMana();
		_time = item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000) : -1;

		_elemAtkType = item.getAttackElementType();
		_elemAtkPower = item.getAttackElementPower();
		for(byte i = 0; i < 6; i++)
		{
			_elemDefAttr[i] = item.getElementDefAttr(i);
		}
		_enchanteffect = item.getEnchantEffect();
		_skin = item.getSkin();
	}

	/**
	 * Returns the item.
	 * @return L2Item
	 */
	public L2Item getItem()
	{
		return _item;
	}

	/**
	 * Returns the unique objectId
	 * @return int
	 */
	public int getObjectId()
	{
		return _object;
	}

	/**
	 * Returns the owner
	 * @return int
	 */
	public int getOwnerId()
	{
		return _owner;
	}

	/**
	 * Returns the LocationSlot
	 * @return int
	 */
	public long getLocationSlot()
	{
		return _locationSlot;
	}

	/**
	 * Returns the count
	 * @return int
	 */
	public long getCount()
	{
		return _count;
	}

	/**
	 * Returns the first type
	 * @return int
	 */
	public int getType1()
	{
		return _item.getType1();
	}

	/**
	 * Returns the second type
	 * @return int
	 */
	public int getType2()
	{
		return _item.getType2();
	}

	/**
	 * Returns the second type
	 * @return int
	 */
	public L2ItemType getItemType()
	{
		return _item.getItemType();
	}

	/**
	 * Returns the ItemId
	 * @return int
	 */
	public int getItemId()
	{
		return _item.getItemId();
	}

	/**
	 * Returns the part of body used with this item
	 * @return int
	 */
	public long getBodyPart()
	{
		return _item.getBodyPart();
	}

	/**
	 * Returns the enchant level
	 * @return int
	 */
	public int getEnchantLevel()
	{
		return _enchant;
	}

	public int isBlock()
	{
		return _block;
	}

	/**
	 * Returns the item grade
	 * @return int
	 */
	public CrystalGrade getItemGrade()
	{
		return _grade;
	}

	/**
	 * Returns true if it is a weapon
	 * @return boolean
	 */
	public boolean isWeapon()
	{
		return _item instanceof L2Weapon;
	}

	/**
	 * Returns true if it is an armor
	 * @return boolean
	 */
	public boolean isArmor()
	{
		return _item instanceof L2Armor;
	}

	/**
	 * Returns true if it is an EtcItem
	 * @return boolean
	 */
	public boolean isEtcItem()
	{
		return _item instanceof L2EtcItem;
	}

	/**
	 * Returns the name of the item
	 * @return String
	 */
	public String getItemName()
	{
		return _item.getName();
	}

	public boolean isAugmented()
	{
		return _isAugmented;
	}

	public int getAugmentationId()
	{
		return _augmentationId;
	}

	/**
	 * Returns the name of the item
	 * @return String
	 * @deprecated beware to use getItemName() instead because getName() is final in L2Object and could not be overridden! Allover L2Object.getName() may return null!
	 */
	public String getName()
	{
		return _item.getName();
	}

	public int getCustomType1()
	{
		return _customType1;
	}

	public int getCustomType2()
	{
		return _customType2;
	}

	public int getMana()
	{
		return _mana;
	}

	public int getAttackElementType()
	{
		return _elemAtkType;
	}

	public int getAttackElementPower()
	{
		return _elemAtkPower;
	}

	public int getElementDefAttr(byte i)
	{
		return _elemDefAttr[i];
	}

	public int[] getEnchantEffect()
	{
		return _enchanteffect;
	}

	/*
	 *	Внешний вид оружия / брани
	 */
	public int getSkin()
	{
		return _skin;
	}

	public int getTime()
	{
		return _time;
	}

	/**
	 * Returns the name of the item
	 * @return String
	 */
	@Override
	public String toString()
	{
		return _item.toString();
	}
}
