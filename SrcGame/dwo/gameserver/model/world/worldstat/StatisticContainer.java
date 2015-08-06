package dwo.gameserver.model.world.worldstat;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 12.05.12
 * Time: 15:57
 * TODO: В холдеры
 */

public class StatisticContainer
{
	private final CategoryType _id;
	private final int _subId;
	private final long _monthlyStatisticCount;
	private final long _generalStatisticCount;

	public StatisticContainer(CategoryType id, int subId, long monthlyStatisticCount, long generalStatisticCount)
	{
		_id = id;
		_subId = subId;
		_monthlyStatisticCount = monthlyStatisticCount;
		_generalStatisticCount = generalStatisticCount;
	}

	public CategoryType getId()
	{
		return _id;
	}

	public int getSubId()
	{
		return _subId;
	}

	public long getMonthlyStatisticCount()
	{
		return _monthlyStatisticCount;
	}

	/**
	 *  Сумма за месяц и за все время
	 * @return Общая статистика
	 */
	public long getGeneralStatisticCount()
	{
		return _generalStatisticCount + _monthlyStatisticCount;
	}
}
