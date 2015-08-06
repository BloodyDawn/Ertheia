package dwo.gameserver.model.items;

import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2WarehouseItem;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.player.L2TradeList.*;

/**
 * Get all information from L2ItemInstance to generate ItemInfo.<BR><BR>
 */
public class ItemInfo
{
    private long _body;
    /**
	 * Identifier of the L2ItemInstance
	 */
	private int _objectId;

	/**
	 * The L2Item template of the L2ItemInstance
	 */
	private L2Item _item;

	/**
	 * The level of enchant on the L2ItemInstance
	 */
	private int _enchant;

	/**
	 * The augmentation of the item
	 */
	private int _augmentation;

	/**
	 * The quantity of L2ItemInstance
	 */
	private long _count;

	/**
	 * The price of the L2ItemInstance
	 */
	private int _price;

	/**
	 * The custom L2ItemInstance types (used loto, race tickets)
	 */
	private int _type1;
	private int _type2;

	/**
	 * If True the L2ItemInstance is equipped
	 */
	private int _equipped;

	/**
	 * The action to do clientside (1=ADD, 2=MODIFY, 3=REMOVE)
	 */
	private int _change;

	/**
	 * Заблокирован ли итем (0 - да 1 нет)
	 */
	private int _block;

	/*
	 * Внешний вид оружия / брони
	 */
	private int _skin;

	/**
	 * The mana of this item
	 */
	private int _mana;
	private int _time;

	private long _location;

	private int _elemAtkType = -2;
	private int _elemAtkPower;
	private int[] _elemDefAttr = {0, 0, 0, 0, 0, 0};
	private int[] _enchanteffect = {0, 0, 0};

	/**
	 * Get all information from L2ItemInstance to generate ItemInfo.<BR><BR>
	 */
	public ItemInfo(L2ItemInstance item)
	{
		if(item == null)
		{
			return;
		}

		// Get the Identifier of the L2ItemInstance
        _objectId = item.getObjectId();

        // Get the L2Item of the L2ItemInstance
        _item = item.getItem();

        // Get the enchant level of the L2ItemInstance
        _enchant = item.getEnchantLevel();

        _block = item.isBlocked();

        // Get the augmentation boni
        _augmentation = item.isAugmented() ? item.getAugmentation().getAugmentationId() : 0;

        // Get the quantity of the L2ItemInstance
        _count = item.getCount();
        _price = item.getReferencePrice();

        // Get custom item types (used loto, race tickets)
        _type1 = item.getCustomType1();
        _type2 = item.getCustomType2();

        // Verify if the L2ItemInstance is equipped
        _equipped = item.isEquipped() ? 1 : 0;

        // Get the action to do clientside
        switch(item.getLastChange())
        {
            case L2ItemInstance.ADDED:
                _change = 1;
                break;
            case L2ItemInstance.MODIFIED:
                _change = 2;
                break;
            case L2ItemInstance.REMOVED:
                _change = 3;
                break;
        }

        // Get shadow item mana
        _mana = item.getMana();
        _time = item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000) : -9999;
        _location = item.getLocationSlot();

        _elemAtkType = item.getAttackElementType();
        _elemAtkPower = item.getAttackElementPower();
        for(byte i = 0; i < 6; i++)
        {
            _elemDefAttr[i] = item.getElementDefAttr(i);
        }
        _enchanteffect = item.getEnchantEffect();
        _skin = item.getSkin();
	}

	public ItemInfo(L2ItemInstance item, int change)
	{
		if(item == null)
		{
			return;
		}

		// Get the Identifier of the L2ItemInstance
		_objectId = item.getObjectId();

		// Get the L2Item of the L2ItemInstance
		_item = item.getItem();

		// Get the enchant level of the L2ItemInstance
		_enchant = item.getEnchantLevel();

		_block = item.isBlocked();

		// Get the augmentation boni
		_augmentation = item.isAugmented() ? item.getAugmentation().getAugmentationId() : 0;

		// Get the quantity of the L2ItemInstance
		_count = item.getCount();

		// Get custom item types (used loto, race tickets)
		_type1 = item.getCustomType1();
		_type2 = item.getCustomType2();

		// Verify if the L2ItemInstance is equipped
		_equipped = item.isEquipped() ? 1 : 0;

		// Get the action to do clientside
		_change = change;

		// Get shadow item mana
		_mana = item.getMana();
		_time = item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000) : -9999;

		_location = item.getLocationSlot();

		_elemAtkType = item.getAttackElementType();
		_elemAtkPower = item.getAttackElementPower();
		for(byte i = 0; i < 6; i++)
		{
			_elemDefAttr[i] = item.getElementDefAttr(i);
		}
		_enchanteffect = item.getEnchantEffect();
		_skin = item.getSkin();
	}

    public ItemInfo(TradeItem item)
    {
        if (item == null)
        {
            return;
        }
        _objectId = item.getObjectId();
        _item = item.getItem();
        _enchant = item.getEnchantLevel();
        _augmentation = 0;
        _count = item.getCount();
        _type1 = item.getCustomType1();
        _type2 = item.getCustomType2();
        _equipped = 0;
        _change = 0;
        _mana = -1;
        _time = -9999;
        _location = item.getLocationSlot();
        _elemAtkType = item.getAttackElementType();
        _elemAtkPower = item.getAttackElementPower();
        for (byte i = 0; i < 6; i++)
        {
            _elemDefAttr[i] = item.getElementDefAttr(i);
        }
        _enchanteffect = item.getEnchantEffect();
    }

    public ItemInfo(L2TradeItem item)//TODO
    {
        if (item == null)
        {
            return;
        }

        _objectId = 0;
        _item = item.getTemplate();
        _location = 0;
        _count = item.getCurrentCount();
        _type1 = item.getTemplate().getType2();
        _type2 = 0;
        _equipped = 0;
        _enchant = 0;
        _body = item.getTemplate().getBodyPart();
        _mana = -1;
        _time = -9999;
        _block = 1;
    }

    public ItemInfo(L2WarehouseItem item)
    {
        if (item == null)
        {
            return;
        }

        _objectId = item.getObjectId();
        _item = item.getItem();
        _enchant = item.getEnchantLevel();

        if (item.isAugmented())
        {
            _augmentation = item.getAugmentationId();
        }
        else
        {
            _augmentation = 0;
        }

        _count = item.getCount();
        _type1 = item.getCustomType1();
        _type2 = item.getCustomType2();
        _equipped = 0;
        _mana = item.getMana();
        _time = item.getTime();
        _location = item.getLocationSlot();

        _elemAtkType = item.getAttackElementType();
        _elemAtkPower = item.getAttackElementPower();
        for (byte i = 0; i < 6; i++)
        {
            _elemDefAttr[i] = item.getElementDefAttr(i);
        }
        _enchanteffect = item.getEnchantEffect();
    }



	public int getObjectId()
	{
		return _objectId;
	}

	public L2Item getItem()
	{
		return _item;
	}

	public int getEnchant()
	{
		return _enchant;
	}

	public int getAugmentationBonus()
	{
		return _augmentation;
	}

	public long getCount()
	{
		return _count;
	}

	public int getPrice()
	{
		return _price;
	}

	public int getCustomType1()
	{
		return _type1;
	}

	public int getCustomType2()
	{
		return _type2;
	}

	public int getEquipped()
	{
		return _equipped;
	}

	public int getChange()
	{
		return _change;
	}

	public int getMana()
	{
		return _mana;
	}

	public int getTime()
	{
		return _time;
	}

	public long getLocation()
	{
		return _location;
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

	public int isBlock()
	{
		return _block;
	}

	public int getSkin()
	{
		return _skin;
	}

    public long getBodyPart()
    {
        return _body;
    }
}
