package dwo.gameserver.cache;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author -Nemesiss-
 */

public class WarehouseCacheManager
{
	// TODO: полностью переписать
	protected final ConcurrentHashMap<L2PcInstance, Long> _cachedWh;
	protected final long _cacheTime;

	private WarehouseCacheManager()
	{
		_cacheTime = Config.WAREHOUSE_CACHE_TIME * 60000L;
		_cachedWh = new ConcurrentHashMap<>();/** Убить */
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new CacheScheduler(), 120000, 60000);
	}

	public static WarehouseCacheManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void addCacheTask(L2PcInstance pc)
	{
		_cachedWh.put(pc, System.currentTimeMillis());
	}

	public void remCacheTask(L2PcInstance pc)
	{
		_cachedWh.remove(pc);
	}

	private static class SingletonHolder
	{
		protected static final WarehouseCacheManager _instance = new WarehouseCacheManager();
	}

	public class CacheScheduler implements Runnable
	{
		@Override
		public void run()
		{
			long cTime = System.currentTimeMillis();
			_cachedWh.keySet().stream().filter(pc -> cTime - _cachedWh.get(pc) > _cacheTime).forEach(pc -> {
				pc.clearWarehouse();
				_cachedWh.remove(pc);
			});
		}
	}
}
