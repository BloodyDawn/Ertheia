package dwo.scripts.dynamic_quests;

import dwo.gameserver.datatables.xml.DynamicQuestsData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class AbstractOrbisTempleLocalQuest extends DynamicQuest
{
	/**
	 * Комната Мудрости на 3 этаже Храма Орбиса.
	 */
	public static final Location ROOM_OF_WISDOM = new Location(217593, 108701, -12552);
	/**
	 * Комната Доблести на 3 этаже Храма Орбиса.
	 */
	public static final Location ROOM_OF_VALOR = new Location(211010, 108707, -12550);
	/**
	 * Рейд-боссы Комнаты Мудрости.
	 */
	public static final int[] WISDOM_RAIDS = {
		25767, 25770, 25769, 25768, 25766
	};
	/**
	 * Рейд-боссы Комнаты Доблести.
	 */
	public static final int[] VALOR_RAIDS = {
		25762, 25880, 25761, 25764, 25763, 25760
	};
	protected static AbstractOrbisTempleLocalQuest _instance;
	protected boolean _spawnRaidOnNextSchedule;
	protected L2Npc _raidBoss;

	public AbstractOrbisTempleLocalQuest(int id)
	{
		super(DynamicQuestsData.getInstance().getQuest(id));
	}

	public static AbstractOrbisTempleLocalQuest getInstance()
	{
		return _instance;
	}

	public static void main(String[] args)
	{
	}

	/**
	 * Спаунит случайного РБ в Комнате Мудрости или в Комнате Доблести.
	 * @param roomId Комната Доблести - 1; Комната Мудрости - 2.
	 */
	public void spawnRaid(int roomId)
	{
		switch(roomId)
		{
			case 1:
				_raidBoss = addSpawn(Rnd.get(VALOR_RAIDS), ROOM_OF_VALOR);
				break;
			case 2:
				_raidBoss = addSpawn(Rnd.get(WISDOM_RAIDS), ROOM_OF_WISDOM);
				break;
		}
		_spawnRaidOnNextSchedule = false;
	}

	@Override
	public void onCampainDone(boolean succeed)
	{
		if(_raidBoss != null)
		{
			_raidBoss.getLocationController().delete();
			_raidBoss = null;
		}
	}

	/**
	 * Запланирован ли спаун РБ при следующей активации квеста Орбиса.
	 * @return
	 */
	public boolean isRaidSpawnScheduled()
	{
		return _spawnRaidOnNextSchedule;
	}

	/**
	 * Если вызвано, то при следующем квесте в Орбисе будут отспаунены специальные РБ в Комнате Доблести или в Комнате Мудрости (3 этаж).
	 * Спаун контролируется наследуемыми классами.
	 *
	 * @return
	 */
	public void spawnRaidOnNextSchedule()
	{
		_spawnRaidOnNextSchedule = true;
	}
}
