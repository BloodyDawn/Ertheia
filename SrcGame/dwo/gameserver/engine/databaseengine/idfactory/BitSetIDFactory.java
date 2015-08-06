package dwo.gameserver.engine.databaseengine.idfactory;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.util.arrays.PrimeFinder;
import org.apache.log4j.Level;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

public class BitSetIDFactory extends IdFactory
{
	private BitSet _freeIds;
	private AtomicInteger _freeIdCount;
	private AtomicInteger _nextFreeId;

	protected BitSetIDFactory()
	{

		synchronized(BitSetIDFactory.class)
		{
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new BitSetCapacityCheck(), 30000, 30000);
			initialize();
		}
		_log.log(Level.INFO, "IDFactory: " + _freeIds.size() + " id's available.");
	}

	public void initialize()
	{
		try
		{
			_freeIds = new BitSet(PrimeFinder.nextPrime(100000));
			_freeIds.clear();
			_freeIdCount = new AtomicInteger(FREE_OBJECT_ID_SIZE);

			for(int usedObjectId : extractUsedObjectIDTable())
			{
				int objectID = usedObjectId - FIRST_OID;
				if(objectID < 0)
				{
					_log.log(Level.WARN, "Object ID " + usedObjectId + " in DB is less than minimum ID of " + FIRST_OID);
					continue;
				}
				_freeIds.set(usedObjectId - FIRST_OID);
				_freeIdCount.decrementAndGet();
			}

			_nextFreeId = new AtomicInteger(_freeIds.nextClearBit(0));
			_initialized = true;
		}
		catch(Exception e)
		{
			_initialized = false;
			_log.log(Level.ERROR, "BitSet ID Factory could not be initialized correctly: " + e.getMessage(), e);
		}
	}

	@Override
	public int getNextId()
	{
		synchronized(this)
		{
			int newID = _nextFreeId.get();
			_freeIds.set(newID);
			_freeIdCount.decrementAndGet();

			int nextFree = _freeIds.nextClearBit(newID);

			if(nextFree < 0)
			{
				nextFree = _freeIds.nextClearBit(0);
			}
			if(nextFree < 0)
			{
				if(_freeIds.size() < FREE_OBJECT_ID_SIZE)
				{
					increaseBitSetCapacity();
				}
				else
				{
					throw new NullPointerException("Ran out of valid Id's.");
				}
			}

			_nextFreeId.set(nextFree);

			return newID + FIRST_OID;
		}
	}

	@Override
	public void releaseId(int objectID)
	{
		synchronized(this)
		{
			if(objectID - FIRST_OID > -1)
			{
				_freeIds.clear(objectID - FIRST_OID);
				_freeIdCount.incrementAndGet();
			}
			else
			{
				_log.log(Level.WARN, "BitSet ID Factory: release objectID " + objectID + " failed (< " + FIRST_OID + ')');
			}
		}
	}

	@Override
	public int size()
	{
		synchronized(this)
		{
			return _freeIdCount.get();
		}
	}

	protected int usedIdCount()
	{
		synchronized(this)
		{
			return size() - FIRST_OID;
		}
	}

	protected boolean reachingBitSetCapacity()
	{
		synchronized(this)
		{
			return PrimeFinder.nextPrime(usedIdCount() * 11 / 10) > _freeIds.size();
		}
	}

	protected void increaseBitSetCapacity()
	{
		synchronized(this)
		{
			BitSet newBitSet = new BitSet(PrimeFinder.nextPrime(usedIdCount() * 11 / 10));
			newBitSet.or(_freeIds);
			_freeIds = newBitSet;
		}
	}

	protected class BitSetCapacityCheck implements Runnable
	{
		@Override
		public void run()
		{
			synchronized(BitSetIDFactory.this)
			{
				if(reachingBitSetCapacity())
				{
					increaseBitSetCapacity();
				}
			}
		}

	}
}
