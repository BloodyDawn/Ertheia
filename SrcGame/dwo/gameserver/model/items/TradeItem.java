package dwo.gameserver.model.items;

import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;

public class TradeItem
{
	private final L2Item _item;
	private final long _location;
	private final int _type1;
	private final int _type2;
	private final byte _elemAtkType;
	private final int _elemAtkPower;
	private final int[] _elemDefAttr = {
		0, 0, 0, 0, 0, 0
	};
	//private long _price;
	private long _referencePrice;
	private long _currentValue;
	private int _lastRechargeTime;
	private int _rechargeTime;
	private int _objectId;
	private int _enchant;
	private long _count;
	private long _storeCount;
	private long _price;
	private int[] _enchanteffect = {0, 0, 0};
	private int _skin;

	public TradeItem(L2ItemInstance item, long count, long price)
	{
		_objectId = item.getObjectId();
		_item = item.getItem();
		_location = item.getLocationSlot();
		_enchant = item.getEnchantLevel();
		_type1 = item.getCustomType1();
		_type2 = item.getCustomType2();
		_count = count;
		_price = price;
		_elemAtkType = item.getAttackElementType();
		_elemAtkPower = item.getAttackElementPower();
		for(byte i = 0; i < 6; i++)
		{
			_elemDefAttr[i] = item.getElementDefAttr(i);
		}
		_enchanteffect = item.getEnchantEffect();
		_skin = item.getSkin();
	}

	public TradeItem(L2Item item, long count, long price)
	{
		_objectId = 0;
		_item = item;
		_location = 0;
		_enchant = 0;
		_type1 = 0;
		_type2 = 0;
		_count = count;
		_storeCount = count;
		_price = price;
		_elemAtkType = Elementals.NONE;
		_elemAtkPower = 0;
	}

	public TradeItem(TradeItem item, long count, long price)
	{
		_objectId = item._objectId;
		_item = item._item;
		_location = item._location;
		_enchant = item._enchant;
		_type1 = item._type1;
		_type2 = item._type2;
		_count = count;
		_storeCount = count;
		_price = price;
		_elemAtkType = item._elemAtkType;
		_elemAtkPower = item._elemAtkPower;
		for(byte i = 0; i < 6; i++)
		{
			_elemDefAttr[i] = item.getElementDefAttr(i);
		}
		_enchanteffect = item._enchanteffect;
		_skin = item._skin;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public void setObjectId(int objectId)
	{
		_objectId = objectId;
	}

	public L2Item getItem()
	{
		return _item;
	}

	public long getLocationSlot()
	{
		return _location;
	}

	public int getEnchantLevel()
	{
		return _enchant;
	}

	public void setEnchantLevel(int enchant)
	{
		_enchant = enchant;
	}

	public int getCustomType1()
	{
		return _type1;
	}

	public int getCustomType2()
	{
		return _type2;
	}

	public long getCount()
	{
		return _count;
	}

	public void setCount(long count)
	{
		_count = count;
	}

	public long getStoreCount()
	{
		return _storeCount;
	}

	public long getPrice()
	{
		return _price;
	}

	public void setPrice(long price)
	{
		_price = price;
	}

	public byte getAttackElementType()
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

	/**
	 * Проверка на блокировку предмета
	 * 0 - заблокирован, 1 - не заблокирован
	 * TODO: Реализуй меня! :D
	 * @return заблокирован ли предмет
	 */
	public int isBlocked()
	{
		return 1;
	}

	public int getSkin()
	{
		return _skin;
	}

	public long getOwnersPrice()
	{
		return _price;
	}

	//TODO копипаст из оверов
	public void setOwnersPrice(long price)
	{
		_price = price;
	}

	public long getReferencePrice()
	{
		return _referencePrice;
	}

	public void setReferencePrice(long price)
	{
		_referencePrice = price;
	}

	public long getStorePrice()
	{
		return _referencePrice / 2;
	}

	public long getCurrentValue()
	{
		return _currentValue;
	}

	public void setCurrentValue(long value)
	{
		_currentValue = value;
	}

	/**
	 * Возвращает время респауна предмета, используется в NPC магазинах с ограниченным количеством.
	 * @return unixtime в минутах
	 */
	public int getRechargeTime()
	{
		return _rechargeTime;
	}

	/**
	 * Устанавливает время респауна предмета, используется в NPC магазинах с ограниченным количеством.
	 * @param rechargeTime : unixtime в минутах
	 */
	public void setRechargeTime(int rechargeTime)
	{
		_rechargeTime = rechargeTime;
	}

	/**
	 * Возвращает ограничен ли этот предмет в количестве, используется в NPC магазинах с ограниченным количеством.
	 * @return true, если ограничен
	 */
	public boolean isCountLimited()
	{
		return _count > 0;
	}

	/**
	 * Возвращает время последнего респауна предмета, используется в NPC магазинах с ограниченным количеством.
	 * @return unixtime в минутах
	 */
	public int getLastRechargeTime()
	{
		return _lastRechargeTime;
	}

	/**
	 * Устанавливает время последнего респауна предмета, используется в NPC магазинах с ограниченным количеством.
	 * @param lastRechargeTime : unixtime в минутах
	 */
	public void setLastRechargeTime(int lastRechargeTime)
	{
		_lastRechargeTime = lastRechargeTime;
	}
}
