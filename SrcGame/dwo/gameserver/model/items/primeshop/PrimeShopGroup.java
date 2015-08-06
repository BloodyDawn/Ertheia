package dwo.gameserver.model.items.primeshop;

import dwo.gameserver.model.skills.stats.StatsSet;

import java.util.List;

public class PrimeShopGroup
{
	private final int _brId;
	private final int _category;
	private final int _paymentType;
	private final int _price;
	private final int _panelType;
	private final int _recommended;
	private final int _start;
	private final int _end;
	private final int _daysOfWeek;
	private final int _startHour;
	private final int _startMinute;
	private final int _stopHour;
	private final int _stopMinute;
	private final int _stock;
	private final int _maxStock;
	private final int _salePercent;
	private final int _minLevel;
	private final int _maxLevel;
	private final int _minBirthday;
	private final int _maxBirthday;
	private final int _restrictionDay;
	private final int _availableCount;
	private final List<PrimeShopItem> _items;
	
	public PrimeShopGroup(StatsSet set, List<PrimeShopItem> items)
	{
		_brId = set.getInteger("id");
		_category = set.getInteger("cat", 0);
		_paymentType = set.getInteger("paymentType", 0);
		_price = set.getInteger("price");
		_panelType = set.getInteger("panelType", 0);
		_recommended = set.getInteger("recommended", 0);
		_start = set.getInteger("startSale", 0);
		_end = set.getInteger("endSale", 0);
		_daysOfWeek = set.getInteger("daysOfWeek", 127);
		_startHour = set.getInteger("startHour", 0);
		_startMinute = set.getInteger("startMinute", 0);
		_stopHour = set.getInteger("stopHour", 0);
		_stopMinute = set.getInteger("stopMinute", 0);
		_stock = set.getInteger("stock", 0);
		_maxStock = set.getInteger("maxStock", -1);
		_salePercent = set.getInteger("salePercent", 0);
		_minLevel = set.getInteger("minLevel", 0);
		_maxLevel = set.getInteger("maxLevel", 0);
		_minBirthday = set.getInteger("minBirthday", 0);
		_maxBirthday = set.getInteger("maxBirthday", 0);
		_restrictionDay = set.getInteger("restrictionDay", 0);
		_availableCount = set.getInteger("availableCount", 0);
		_items = items;
	}
	
	public int getBrId()
	{
		return _brId;
	}
	
	public int getCat()
	{
		return _category;
	}
	
	public int getPaymentType()
	{
		return _paymentType;
	}
	
	public int getPrice()
	{
		return _price;
	}
	
	public long getCount()
	{
		return _items.stream().mapToLong(PrimeShopItem::getCount).sum();
	}
	
	public int getWeight()
	{
		return _items.stream().mapToInt(PrimeShopItem::getWeight).sum();
	}
	
	public int getPanelType()
	{
		return _panelType;
	}
	
	public int getRecommended()
	{
		return _recommended;
	}
	
	public int getStartSale()
	{
		return _start;
	}
	
	public int getEndSale()
	{
		return _end;
	}
	
	public int getDaysOfWeek()
	{
		return _daysOfWeek;
	}
	
	public int getStartHour()
	{
		return _startHour;
	}
	
	public int getStartMinute()
	{
		return _startMinute;
	}
	
	public int getStopHour()
	{
		return _stopHour;
	}
	
	public int getStopMinute()
	{
		return _stopMinute;
	}
	
	public int getStock()
	{
		return _stock;
	}
	
	public int getTotal()
	{
		return _maxStock;
	}
	
	public int getSalePercent()
	{
		return _salePercent;
	}
	
	public int getMinLevel()
	{
		return _minLevel;
	}
	
	public int getMaxLevel()
	{
		return _maxLevel;
	}
	
	public int getMinBirthday()
	{
		return _minBirthday;
	}
	
	public int getMaxBirthday()
	{
		return _maxBirthday;
	}
	
	public int getRestrictionDay()
	{
		return _restrictionDay;
	}
	
	public int getAvailableCount()
	{
		return _availableCount;
	}
	
	public List<PrimeShopItem> getItems()
	{
		return _items;
	}
}
