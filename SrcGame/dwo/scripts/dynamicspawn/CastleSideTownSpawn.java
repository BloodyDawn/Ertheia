package dwo.scripts.dynamicspawn;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.DynamicSpawnData;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.instancemanager.TownManager;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.model.holders.SpawnHolder;
import dwo.gameserver.model.holders.SpawnsHolder;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.castle.CastleSide;

import java.util.concurrent.Future;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.03.13
 * Time: 16:26
 */

public class CastleSideTownSpawn extends Quest
{
	private static final SpawnsHolder darkTownSpawnHolders = DynamicSpawnData.getInstance().getSpawnsHolder("CastleDarkTowns");
	private static final SpawnsHolder lightTownSpawnHolders = DynamicSpawnData.getInstance().getSpawnsHolder("CastleLightTowns");

	private static Future<?> _reinitializeSpawnsTask;

	public CastleSideTownSpawn()
	{
		addEventId(HookType.ON_SIEGE_END);
		initNpcSpawns();
	}

	public static void main(String[] args)
	{
		new CastleSideTownSpawn();
	}

	private void initNpcSpawns()
	{
		// Проверяем текущую сторону замка в локациях Dark-нпц
		for(SpawnHolder holder : darkTownSpawnHolders.getHolders())
		{
			Castle castle = CastleManager.getInstance().getCastleById(TownManager.getTownCastle(MapRegionManager.getInstance().getMapRegionLocId(holder.getLocation().getX(), holder.getLocation().getY())));
			if(castle != null)
			{
				if(castle.getCastleSide() == CastleSide.DARK)
				{
					holder.doSpawn();
				}
			}
		}

		// Проверяем текущую сторону замка в локациях Light-нпц
		for(SpawnHolder holder : lightTownSpawnHolders.getHolders())
		{
			Castle castle = CastleManager.getInstance().getCastleById(TownManager.getTownCastle(MapRegionManager.getInstance().getMapRegionLocId(holder.getLocation().getX(), holder.getLocation().getY())));
			if(castle != null)
			{
				if(castle.getCastleSide() == CastleSide.LIGHT)
				{
					holder.doSpawn();
				}
			}
		}
	}

	@Override
	public void onSiegeEnd(Castle castle)
	{
		// Если осады замков кончаются одновременно, то не создаем отдельные таски на обновление спауна для каждого
		if(_reinitializeSpawnsTask == null)
		{
			_reinitializeSpawnsTask = ThreadPoolManager.getInstance().scheduleGeneral(new ReinitalizeSpawns(), 60000);
		}
	}

	private class ReinitalizeSpawns implements Runnable
	{
		public ReinitalizeSpawns()
		{
		}

		@Override
		public void run()
		{
			// Убираем всех заспауненных НПЦ
			darkTownSpawnHolders.unSpawnAll();
			lightTownSpawnHolders.unSpawnAll();

			// Спауним обновленных нпц в зависимости от сторон замка
			initNpcSpawns();
			_reinitializeSpawnsTask.cancel(false);
			_reinitializeSpawnsTask = null;
		}
	}
}