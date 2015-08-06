package dwo.gameserver.model.items.multisell;

import java.util.ArrayList;
import java.util.List;

public class ListContainer
{
	protected int _listId;
	protected boolean _taxFree;
	protected boolean _keepEnchant;
	protected boolean _showAll;
	protected boolean _allowAugmentedItems;
	protected boolean _allowElementalItems;
	protected boolean _chanceBuy;
	protected boolean _npcCheck;
	protected List<Entry> _entries;

	public ListContainer()
	{
		_entries = new ArrayList<>();
	}

	/**
	 * This constructor used in PreparedListContainer only
	 * ArrayList not created
	 */
	protected ListContainer(int listId)
	{
		_listId = listId;
	}

	public List<Entry> getEntries()
	{
		return _entries;
	}

	public int getListId()
	{
		return _listId;
	}

	public void setListId(int listId)
	{
		_listId = listId;
	}

	public void setApplyTaxes(boolean applyTaxes)
	{
		_taxFree = applyTaxes;
	}

	public boolean isTaxFree()
	{
		return _taxFree;
	}

	/**
	 * @return {@code true} если продукт мультиселла будет иметь те же свойства что и индигриент
	 */
	public boolean isKeepEnchant()
	{
		return _keepEnchant;
	}

	/**
	 * Устанавливает будет-ли продукт те же свойства что и индигриент
	 * @param val true/false
	 */
	public void setKeepEnchant(boolean val)
	{
		_keepEnchant = val;
	}

	/**
	 * @return {@code true} Проверять ли только по 1 ингридиенту
	 */
	public boolean isShowAll()
	{
		return _showAll;
	}

	public void setShowAll(boolean var)
	{
		_showAll = var;
	}

	public boolean isAllowAugmentedItems()
	{
		return _allowAugmentedItems;
	}

	public void setAllowAugmentedItems(boolean var)
	{
		_allowAugmentedItems = var;
	}

	public boolean isAllowElementalItems()
	{
		return _allowElementalItems;
	}

	public void setAllowElementalItems(boolean var)
	{
		_allowElementalItems = var;
	}

	/**
	 * @return тип мультиселла (false - обычный, true - шансовый)
	 */
	public boolean isChanceBuy()
	{
		return _chanceBuy;
	}

	/**
	 * Устанавливает тип джля мультиселла
	 * @param var тип мультиселла
	 */
	public void setChanceBuy(boolean var)
	{
		_chanceBuy = var;
	}

	/**
	 * @return {@code true} если мультиселл требует наличия НПЦ
	 */
	public boolean isNpcRequied()
	{
		return _npcCheck;
	}

	/**
	 * Устанавливает необходимость наличия НПЦ при вызове мультиселла
	 * @param val true/false
	 */
	public void setNpcRequied(boolean val)
	{
		_npcCheck = val;
	}
}