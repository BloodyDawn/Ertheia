package dwo.gameserver.taskmanager.manager;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2GuardInstance;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import javolution.util.FastSet;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Collection;

public class KnownListUpdateTaskManager
{
	protected static final Logger _log = LogManager.getLogger(KnownListUpdateTaskManager.class);

	private static final int FULL_UPDATE_TIMER = 100;
	// Do full update every FULL_UPDATE_TIMER * KNOWNLIST_UPDATE_INTERVAL
	public static int _fullUpdateTimer = FULL_UPDATE_TIMER;
	private static final FastSet<L2WorldRegion> _failedRegions = new FastSet<>();
	public static boolean updatePass = true;

	private KnownListUpdateTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new KnownListUpdate(), 1000, Config.KNOWNLIST_UPDATE_INTERVAL);
	}

	public static KnownListUpdateTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}

	/***
	 * Обновление ноулистов в указанном регионе
	 * @param region регион
	 * @param fullUpdate {@code true} если требуется полное обновление региона
	 * @param forgetObjects {@code true} если требуется очистка ноулиста обьекта перед обновлением
	 */
	public void updateRegion(L2WorldRegion region, boolean fullUpdate, boolean forgetObjects)
	{
		for(L2Object object : region.getVisibleObjects().values())
		{
			if(object == null || !object.isVisible())
			{
				continue; // пропускаем мертвые обьекты
			}

			// Гварды и аггро-мобы требуют более частого обновления KnownList
			boolean aggro = object instanceof L2GuardInstance || object instanceof L2Attackable && ((L2Attackable) object).getEnemyClan() != null;

			if(forgetObjects)
			{
				object.getKnownList().forgetObjects(aggro || fullUpdate);
				continue;
			}
            for (L2WorldRegion regi : region.getSurroundingRegions())
            {
                if ((object instanceof L2Playable) || (aggro && regi.isActive()) || fullUpdate)
                {
                    Collection<L2Object> inrObj = regi.getVisibleObjects().values();
                    for (L2Object obj : inrObj)
                    {
                        if (obj != object)
                        {
                            object.getKnownList().addKnownObject(obj);
                        }
                    }
                }
                else if (object instanceof L2Character)
                {
                    if (regi.isActive())
                    {
                        Collection<L2Object> inrPls = regi.getVisibleObjects().values();

                        for (L2Object obj : inrPls)
                        {
                            if (obj != object)
                            {
                                object.getKnownList().addKnownObject(obj);
                            }
                        }
                    }
                }
            }
		}
	}

	private static class SingletonHolder
	{
		protected static final KnownListUpdateTaskManager _instance = new KnownListUpdateTaskManager();
	}

	private class KnownListUpdate implements Runnable
	{
		public KnownListUpdate()
		{
		}

		@Override
		public void run()
		{
			try
			{
				boolean failed;
				for(L2WorldRegion[] regions : WorldManager.getInstance().getAllWorldRegions())
				{
					for(L2WorldRegion region : regions)
					{
						// try для того, чтобы апдейт не останавливался, если updateRegion() вылетел с ошибкой
						try
						{
							// Если была ошибка в предыдущем апдейте для обрабатываемого региона
							failed = _failedRegions.contains(region);

							// Проверяем активен-ли проверяемый регион
							if(region.isActive())
							{
								updateRegion(region, _fullUpdateTimer == FULL_UPDATE_TIMER || failed, updatePass);
							}
							if(failed)
							{
								_failedRegions.remove(region); // Если в текущей итерации фейловый регион все-же обновился, удаляем его из _failedRegions
							}
						}
						catch(Exception e)
						{
							_log.log(Level.ERROR, "KnownListUpdateTaskManager: updateRegion(" + _fullUpdateTimer + ',' + updatePass + ") failed for region " + region + ". Full update scheduled. " + e.getMessage(), e);
							_failedRegions.add(region);
						}
					}
				}
				updatePass = !updatePass;

				if(_fullUpdateTimer > 0)
				{
					_fullUpdateTimer--;
				}
				else
				{
					_fullUpdateTimer = FULL_UPDATE_TIMER;
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while KnownListUpdate()", e);
			}
		}
	}
}