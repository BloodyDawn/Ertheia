package dwo.gameserver.network;

import dwo.config.Config;

public class ClientStats
{
	private final int[] _packetsInSecond;
	private final int BUFFER_SIZE;
	public int processedPackets;
	public int droppedPackets;
	public int unknownPackets;
	public int totalQueueSize;
	public int maxQueueSize;
	public int totalBursts;
	public int maxBurstSize;
	public int shortFloods;
	public int longFloods;
	public int totalQueueOverflows;
	public int totalUnderflowExceptions;
	private long _packetCountStartTick;
	private int _head;
	private int _totalCount;
	private int _floodsInMin;
	private long _floodStartTick;
	private int _unknownPacketsInMin;
	private long _unknownPacketStartTick;
	private int _overflowsInMin;
	private long _overflowStartTick;
	private int _underflowReadsInMin;
	private long _underflowReadStartTick;
	private volatile boolean _floodDetected;
	private volatile boolean _queueOverflowDetected;

	public ClientStats()
	{
		BUFFER_SIZE = Config.CLIENT_PACKET_QUEUE_MEASURE_INTERVAL;
		_packetsInSecond = new int[BUFFER_SIZE];
		_head = BUFFER_SIZE - 1;
	}

	/**
	 * Returns true if incoming packet need to be dropped
	 */
	protected boolean dropPacket()
	{
		boolean result = _floodDetected || _queueOverflowDetected;
		if(result)
		{
			droppedPackets++;
		}
		return result;
	}

	/**
	 * Returns true if flood detected first and ActionFail packet need to be sent.
	 * Later during flood returns true (and send ActionFail) once per second.
	 */
	protected boolean countPacket(int queueSize)
	{
		processedPackets++;
		totalQueueSize += queueSize;
		if(maxQueueSize < queueSize)
		{
			maxQueueSize = queueSize;
		}
		if(_queueOverflowDetected && queueSize < 2)
		{
			_queueOverflowDetected = false;
		}

		return countPacket();
	}

	/**
	 * Counts unknown packets and return true if threshold is reached.
	 */
	protected boolean countUnknownPacket()
	{
		unknownPackets++;

		long tick = System.currentTimeMillis();
		if(tick - _unknownPacketStartTick > 60000)
		{
			_unknownPacketStartTick = tick;
			_unknownPacketsInMin = 1;
			return false;
		}

		_unknownPacketsInMin++;
		return _unknownPacketsInMin > Config.CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN;
	}

	/**
	 * Counts burst length and return true if execution of the queue need to be aborted.
	 *
	 * @param count - current number of processed packets in burst
	 */
	protected boolean countBurst(int count)
	{
		if(count > maxBurstSize)
		{
			maxBurstSize = count;
		}

		if(count < Config.CLIENT_PACKET_QUEUE_MAX_BURST_SIZE)
		{
			return false;
		}

		totalBursts++;
		return true;
	}

	/**
	 * Counts queue overflows and return true if threshold is reached.
	 */
	protected boolean countQueueOverflow()
	{
		_queueOverflowDetected = true;
		totalQueueOverflows++;

		long tick = System.currentTimeMillis();
		if(tick - _overflowStartTick > 60000)
		{
			_overflowStartTick = tick;
			_overflowsInMin = 1;
			return false;
		}

		_overflowsInMin++;
		return _overflowsInMin > Config.CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN;
	}

	/**
	 * Counts underflow exceptions and return true if threshold is reached.
	 */
	protected boolean countUnderflowException()
	{
		totalUnderflowExceptions++;

		long tick = System.currentTimeMillis();
		if(tick - _underflowReadStartTick > 60000)
		{
			_underflowReadStartTick = tick;
			_underflowReadsInMin = 1;
			return false;
		}

		_underflowReadsInMin++;
		return _underflowReadsInMin > Config.CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN;
	}

	/**
	 * Returns true if maximum number of floods per minute is reached.
	 */
	protected boolean countFloods()
	{
		return _floodsInMin > Config.CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN;
	}

	private boolean longFloodDetected()
	{
		return _totalCount / BUFFER_SIZE > Config.CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND;
	}

	/**
	 * Returns true if flood detected first and ActionFail packet need to be sent.
	 * Later during flood returns true (and send ActionFail) once per second.
	 */
	private boolean countPacket()
	{
		synchronized(this)
		{
			_totalCount++;
			long tick = System.currentTimeMillis();
			if(tick - _packetCountStartTick > 1000)
			{
				_packetCountStartTick = tick;

				// clear flag if no more flooding during last seconds
				if(_floodDetected && !longFloodDetected() && _packetsInSecond[_head] < Config.CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND / 2)
				{
					_floodDetected = false;
				}

				// wrap head of the buffer around the tail
				if(_head <= 0)
				{
					_head = BUFFER_SIZE;
				}
				_head--;

				_totalCount -= _packetsInSecond[_head];
				_packetsInSecond[_head] = 1;
				return _floodDetected;
			}

			int count = ++_packetsInSecond[_head];
			if(!_floodDetected)
			{
				if(count > Config.CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND)
				{
					shortFloods++;
				}
				else if(longFloodDetected())
				{
					longFloods++;
				}
				else
				{
					return false;
				}

				_floodDetected = true;
				if(tick - _floodStartTick > 60000)
				{
					_floodStartTick = tick;
					_floodsInMin = 1;
				}
				else
				{
					_floodsInMin++;
				}

				return true; // Return true only in the beginning of the flood
			}

			return false;
		}
	}
}