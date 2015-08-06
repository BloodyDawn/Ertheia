package dwo.gameserver.model.holders;

import dwo.gameserver.model.world.worldstat.CategoryType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 22.02.12
 * Time: 4:38
 */
public class WorldStatisticHolder
{
	private final CategoryType _cat;
	private final short _place;
	private final int _objId;
	private final String _name;
	private final long _value;
	private final int _clanCrestId;
	private final boolean _isClanStatistic;

	/**
	 * Используется для хранения статистики о игроке
	 * @param objId ObjectID персонажа
	 * @param name имя персонажа
	 * @param value значение статистики
	 */
	public WorldStatisticHolder(int catId, int subcatId, short place, int objId, String name, long value)
	{
		_cat = CategoryType.getCategoryById(catId, subcatId);
		_place = place;
		_objId = objId;
		_name = name;
		_value = value;
		_clanCrestId = 0;
		_isClanStatistic = false;
	}

	/**
	 * Используется для хранения статистики о клане
	 * @param objId Id клана
	 * @param name название клана
	 * @param value значение статистики
	 * @param clanCrestId Id значка клана
	 */
	public WorldStatisticHolder(int catId, int subcatId, short place, int objId, String name, long value, int clanCrestId)
	{
		_cat = CategoryType.getCategoryById(catId, subcatId);
		_place = place;
		_objId = objId;
		_name = name;
		_value = value;
		_clanCrestId = clanCrestId;
		_isClanStatistic = true;
	}

	/**
	 * @return ObjectId игрока или Id клана
	 */
	public int getObjId()
	{
		return _objId;
	}

	/**
	 * @return имя игрока или название клана
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * @return значение статистики
	 */
	public long getValue()
	{
		return _value;
	}

	/**
	 * @return Id значка клана
	 */
	public int getClanCrestId()
	{
		return _clanCrestId;
	}

	/**
	 * @return является ли холдер клановой статистикой
	 */
	public boolean isClanStatistic()
	{
		return _isClanStatistic;
	}
}
