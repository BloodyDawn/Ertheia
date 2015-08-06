package dwo.scripts.ai.zone;

import dwo.gameserver.GameTimeController;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.DynamicSpawnData;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.holders.SpawnsHolder;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.EventTrigger;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 20.01.13
 * Time: 8:17
 *
 * в 15.00 + ~4 по игровому деревня меняется в PVP
 * в 18.00 + ~4 по игровому деревня становится мирной
 */

public class VillageOfCarnac extends Quest
{
	private static final SpawnsHolder villageOfCarnacSpawnHolder = DynamicSpawnData.getInstance().getSpawnsHolder("VillageOfCarnac");
	// Статусные триггеры (визуальные эффекты)
	private static final int VILAGE_PVP_TRIGGER = 20140700;
	// Основная зона деревни
	private static final int ZONE_CARNAC_VILLAGE = 4600100;
	// Текущеее состояние зоны
	private boolean isZonePvpNow;

	public VillageOfCarnac()
	{
		addEnterZoneId(ZONE_CARNAC_VILLAGE);
		addExitZoneId(ZONE_CARNAC_VILLAGE);
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ReValidateVillageZone(), 0, 60000L);
	}

	public static void main(String[] args)
	{
		new VillageOfCarnac();
	}

	/***
	 * Принудительно меняем тип зоны для игроков внутри нее, активируем спавн в деревне, сообщение и разруху.
	 */
	private void startPvPZone()
	{
		ZoneManager.getInstance().getZoneById(ZONE_CARNAC_VILLAGE).getCharactersInside().stream().filter(cha -> cha != null && cha.isPlayer()).forEach(cha -> {
			cha.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1600063), ExShowScreenMessage.TOP_CENTER, 10000));
			cha.sendPacket(new EventTrigger(VILAGE_PVP_TRIGGER, true));
			villageOfCarnacSpawnHolder.spawnAll();
			enterPvPZone(cha);
		});
	}

	/***
	 * Принудительно меняем тип зоны для игроков внутри нее и выключаем все изменения.
	 */
	private void stopPvPZone()
	{
		ZoneManager.getInstance().getZoneById(ZONE_CARNAC_VILLAGE).getCharactersInside().stream().filter(cha -> cha != null && cha.isPlayer()).forEach(cha -> {
			cha.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1600066), ExShowScreenMessage.TOP_CENTER, 10000));
			cha.sendPacket(new EventTrigger(VILAGE_PVP_TRIGGER, false));
			villageOfCarnacSpawnHolder.unSpawnAll();
			exitPvPZone(cha);
		});
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(isZonePvpNow && character != null)
		{
			enterPvPZone(character);
		}
		else
		{
			exitPvPZone(character);
		}
		return null;
	}

	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if(character != null)
		{
			clearZone(character);
		}
		return null;
	}

	private void enterPvPZone(L2Character cha)
	{
		cha.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
		cha.setInsideZone(L2Character.ZONE_PEACE, false);
		cha.setInsideZone(L2Character.ZONE_PVP, true);
	}

	private void exitPvPZone(L2Character cha)
	{
		cha.sendPacket(SystemMessageId.ENTER_PEACEFUL_ZONE);
		cha.setInsideZone(L2Character.ZONE_PVP, false);
		cha.setInsideZone(L2Character.ZONE_PEACE, true);
	}

	private void clearZone(L2Character cha)
	{
		if(cha.isInsideZone(L2Character.ZONE_PVP))
		{
			cha.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
			cha.setInsideZone(L2Character.ZONE_PVP, false);
		}
		else if(cha.isInsideZone(L2Character.ZONE_PEACE))
		{
			cha.sendPacket(SystemMessageId.EXIT_PEACEFUL_ZONE);
			cha.setInsideZone(L2Character.ZONE_PEACE, false);
		}
	}

	private class ReValidateVillageZone implements Runnable
	{
		@Override
		public void run()
		{
			int gameTime = GameTimeController.getInstance().getGameTime();
			int hour = gameTime / 60 % 24;
			int minute = gameTime % 60;

			if(hour >= 15 && hour < 18)
			{
				if(!isZonePvpNow)
				{
					isZonePvpNow = true;
					startPvPZone();

					_log.log(Level.INFO, "Village of Carnac: Status: " + (isZonePvpNow ? "PVP" : "PEACE") + " Game Time: Hours: " + hour + " Minutes: " + minute);
				}
			}
			else
			{
				if(isZonePvpNow)
				{
					isZonePvpNow = false;
					stopPvPZone();

					_log.log(Level.INFO, "Village of Carnac: Status: " + (isZonePvpNow ? "PVP" : "PEACE") + " Game Time: Hours: " + hour + " Minutes: " + minute);
				}
			}
		}
	}
}
