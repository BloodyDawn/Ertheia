package dwo.scripts.ai.zone;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.DynamicSpawnData;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.holders.SpawnsHolder;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.serverpackets.EventTrigger;
import javolution.util.FastList;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 05.01.12
 * Time: 6:08
 */

public class MagmeldTown extends Quest
{
	private static final int _dancingCat = 33093;

	private static final List<L2Npc> _cats = new FastList<>();
	// Зона города
	private static final int ZONE_ID = 12345;
	// Статусные триггеры (визуальные эффекты)
	private static final int BlueTrigger = 262001;
	private static final int RedTrigger = 262003;
	private static final SpawnsHolder magmeldSpawnHolder = DynamicSpawnData.getInstance().getSpawnsHolder("MagmeldRitual");
	// Таски на ритуал
	ScheduledFuture<?> _ritualStartTask;
	ScheduledFuture<?> _ritualStopTask;
	ScheduledFuture<?> _catsDanceTask;

	public MagmeldTown()
	{
		addSpawnId(_dancingCat);
		addEnterZoneId(ZONE_ID);
		_ritualStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new ritualStartTask(), 1800000);
	}

	public static void main(String[] args)
	{
		new MagmeldTown();
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(npc.getNpcId() == _dancingCat)
		{
			npc.setIsNoAnimation(true);
			_cats.add(npc);
		}
		return super.onSpawn(npc);
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(character.isPlayer())
		{
			// Если в зоне сейчас не протекает ритуал
			if(_catsDanceTask == null)
			{
				character.sendPacket(new EventTrigger(RedTrigger, false));
				character.sendPacket(new EventTrigger(BlueTrigger, true));
			}
			else
			{
				character.sendPacket(new EventTrigger(BlueTrigger, false));
				character.sendPacket(new EventTrigger(RedTrigger, true));
			}
		}
		return null;
	}

	/**
	 * Старт ритуала:
	 * - Спаун действующих персонажей из холдера;
	 * - Запуск таска catsDanceTask();
	 * - Запуск таска на окончание ритуала;
	 */
	private class ritualStartTask implements Runnable
	{
		@Override
		public void run()
		{
			if(_ritualStopTask != null)
			{
				_ritualStopTask.cancel(false);
				_ritualStopTask = null;
			}

			magmeldSpawnHolder.spawnAll();

			_catsDanceTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new catsDanceTask(), 5000, 6000);
			_ritualStopTask = ThreadPoolManager.getInstance().scheduleGeneral(new ritualStopTask(), 1800000);
		}
	}

	/**
	 * Окончание ритуала:
	 * - Отмена таска catsDanceTask();
	 * - Удаление спауна из холдера;
	 * - Старт таска на начало ритуала;
	 */
	private class ritualStopTask implements Runnable
	{
		@Override
		public void run()
		{
			if(_ritualStartTask != null)
			{
				_ritualStartTask.cancel(false);
				_ritualStartTask = null;
			}

			if(_catsDanceTask != null)
			{
				_catsDanceTask.cancel(false);
				_catsDanceTask = null;
			}

			magmeldSpawnHolder.unSpawnAll();

			_cats.clear();

			_ritualStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new ritualStartTask(), 1800000);
		}
	}

	/**
	 * Таск на танцульки котов
	 */
	private class catsDanceTask implements Runnable
	{
		@Override
		public void run()
		{
			synchronized(_cats)
			{
				for(L2Npc npc : _cats)
				{
					npc.broadcastSocialAction(2);
				}
			}
		}
	}
}
