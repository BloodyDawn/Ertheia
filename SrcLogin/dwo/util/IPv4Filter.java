package dwo.util;

import dwo.util.mmocore.IAcceptFilter;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;

/**
 * Formatted Forsaiken's IPv4 filter [DrHouse]
 *
 * @author Forsaiken
 *
 */
public class IPv4Filter implements IAcceptFilter, Runnable
{
	private static final long SLEEP_TIME = 5000;
	private final TIntObjectHashMap<Flood> _ipFloodMap;

	public IPv4Filter()
	{
		_ipFloodMap = new TIntObjectHashMap<>();
		Thread t = new Thread(this);
		t.setName(getClass().getSimpleName());
		t.setDaemon(true);
		t.start();
	}

	/**
	 *
	 * @param ip
	 * @return
	 */
	private static int hash(byte[] ip)
	{
		return ip[0] & 0xFF | ip[1] << 8 & 0xFF00 | ip[2] << 16 & 0xFF0000 | ip[3] << 24 & 0xFF000000;
	}

	@Override
	public boolean accept(SocketChannel sc)
	{
		InetAddress addr = sc.socket().getInetAddress();
		int h = hash(addr.getAddress());

		long current = System.currentTimeMillis();
		Flood f;
		synchronized(_ipFloodMap)
		{
			f = _ipFloodMap.get(h);
		}
		if(f != null)
		{
			if(f.trys == -1)
			{
				f.lastAccess = current;
				return false;
			}

			if(f.lastAccess + 1000 > current)
			{
				f.lastAccess = current;

				if(f.trys >= 3)
				{
					f.trys = -1;
					return false;
				}

				f.trys++;
			}
			else
			{
				f.lastAccess = current;
			}
		}
		else
		{
			synchronized(_ipFloodMap)
			{
				_ipFloodMap.put(h, new Flood());
			}
		}

		return true;
	}

	@Override
	public void run()
	{
		while(true)
		{
			long reference = System.currentTimeMillis() - 1000 * 300;
			synchronized(_ipFloodMap)
			{
				TIntObjectIterator<Flood> it = _ipFloodMap.iterator();
				while(it.hasNext())
				{
					it.advance();
					Flood f = it.value();
					if(f.lastAccess < reference)
					{
						it.remove();
					}
				}
			}

			try
			{
				Thread.sleep(SLEEP_TIME);
			}
			catch(InterruptedException e)
			{
				return;
			}
		}
	}

	protected static class Flood
	{
		long lastAccess;
		int trys;

		Flood()
		{
			lastAccess = System.currentTimeMillis();
			trys = 0;
		}
	}

}