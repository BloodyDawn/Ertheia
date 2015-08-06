package dwo.gameserver.model.world.npc.drop;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 04.04.12
 * Time: 0:25
 */

public class EventDropDataTable
{
	private static final List<EventDropData> _allNpcDateDrops = new ArrayList<>();

	public static EventDropDataTable getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * Добавляет EventDropData в глобальный список
	 * @param data данные о дропе
	 */
	public void addEventDrop(EventDropData data)
	{
		_allNpcDateDrops.add(data);
	}

	/**
	 * @param level уровень монстра
	 * @return список ивентевого дропа для монстра указанного уровня
	 */
	public List<EventDropData> getEventDropForLevel(int level)
	{
		List<EventDropData> temp = _allNpcDateDrops.stream().filter(data -> level <= data.getMaxLevel() && level >= data.getMinLevel()).collect(Collectors.toList());
		return temp;
	}

	private static class SingletonHolder
	{
		protected static final EventDropDataTable _instance = new EventDropDataTable();
	}
}