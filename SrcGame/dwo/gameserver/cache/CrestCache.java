package dwo.gameserver.cache;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.model.holders.CrestBuilderHolder;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.util.fileio.filters.DdsFilter;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Layane
 */

public class CrestCache
{
	private static Logger _log = LogManager.getLogger(CrestCache.class);

	private final TIntObjectHashMap<byte[]> _cachePledge = new TIntObjectHashMap<>();

	private final TIntObjectHashMap<byte[][]> _cachePledgeLarge = new TIntObjectHashMap<>();

	private final TIntObjectHashMap<byte[]> _cacheAlly = new TIntObjectHashMap<>();

	private final Map<Integer, CrestBuilderHolder> _cachePledgeLargeBuffer = new HashMap<>();

	private int _loadedFiles;

	private long _bytesBuffLen;

	private CrestCache()
	{
		reload();
	}

	public static CrestCache getInstance()
	{
		return SingletonHolder._instance;
	}

	public void reload()
	{
		File dir = new File(Config.DATAPACK_ROOT, "data/crests/");

		File[] files = dir.listFiles(new DdsFilter());
		byte[] content;
		synchronized(this)
		{
			_loadedFiles = 0;
			_bytesBuffLen = 0;

			_cachePledge.clear();
			_cachePledgeLarge.clear();
			_cacheAlly.clear();
		}
		L2Clan[] clans = ClanTable.getInstance().getClans();
		if(files != null)
		{
			for(File file : files)
			{
				RandomAccessFile f = null;
				synchronized(this)
				{
					try
					{
						f = new RandomAccessFile(file, "r");
						content = new byte[(int) f.length()];
						f.readFully(content);

						boolean erase = true;
						int crestId;

						if(file.getName().startsWith("Crest_Large_"))
						{
							crestId = Integer.parseInt(file.getName().substring(12, file.getName().length() - 4));
							if(Config.CLEAR_CREST_CACHE)
							{
								for(L2Clan clan : clans)
								{
									if(clan.getCrestLargeId() == crestId)
									{
										erase = false;
										break;
									}
								}
								if(erase)
								{
									file.delete();
									continue;
								}
							}
							_cachePledgeLarge.put(crestId, trimLargeCrestForPacket(content));
						}
						else if(file.getName().startsWith("Crest_"))
						{
							crestId = Integer.parseInt(file.getName().substring(6, file.getName().length() - 4));
							if(Config.CLEAR_CREST_CACHE)
							{
								for(L2Clan clan : clans)
								{
									if(clan.getCrestId() == crestId)
									{
										erase = false;
										break;
									}
								}
								if(erase)
								{
									file.delete();
									continue;
								}
							}
							_cachePledge.put(crestId, content);
						}
						else if(file.getName().startsWith("AllyCrest_"))
						{
							crestId = Integer.parseInt(file.getName().substring(10, file.getName().length() - 4));
							if(Config.CLEAR_CREST_CACHE)
							{
								for(L2Clan clan : clans)
								{
									if(clan.getAllyCrestId() == crestId)
									{
										erase = false;
										break;
									}
								}
								if(erase)
								{
									file.delete();
									continue;
								}
							}
							_cacheAlly.put(crestId, content);
						}
						_loadedFiles++;
						_bytesBuffLen += content.length;
					}
					catch(Exception e)
					{
						_log.log(Level.ERROR, "Problem with crest file " + e.getMessage(), e);
					}
					finally
					{
						try
						{
							f.close();
						}
						catch(Exception e1)
						{
							_log.log(Level.ERROR, "Problem with closing reader for crest file.");
						}
					}
				}
			}
		}

		for(L2Clan clan : clans)
		{
			if(clan.getCrestId() != 0)
			{
				if(getPledgeCrest(clan.getCrestId()) == null)
				{
					_log.log(Level.INFO, "Removing non-existent crest for clan " + clan.getName() + " [" + clan.getClanId() + "], crestId:" + clan.getCrestId());
					clan.setCrestId(0);
					clan.changeClanCrest(0);
				}
			}
			if(clan.getCrestLargeId() != 0)
			{
				if(getPledgeCrestLarge(clan.getCrestLargeId()) == null)
				{
					_log.log(Level.INFO, "Removing non-existent large crest for clan " + clan.getName() + " [" + clan.getClanId() + "], crestLargeId:" + clan.getCrestLargeId());
					clan.setCrestLargeId(0);
					clan.changeLargeCrest(0);
				}
			}
			if(clan.getAllyCrestId() != 0)
			{
				if(getAllyCrest(clan.getAllyCrestId()) == null)
				{
					_log.log(Level.INFO, "Removing non-existent ally crest for clan " + clan.getName() + " [" + clan.getClanId() + "], allyCrestId:" + clan.getAllyCrestId());
					clan.setAllyCrestId(0);
					clan.changeAllyCrest(0, true);
				}
			}
		}
		_log.log(Level.INFO, "Cache[Crest]: " + String.format("%.3f", getMemoryUsage()) + "MB on " + _loadedFiles);
	}

	public float getMemoryUsage()
	{
		return (float) _bytesBuffLen / 1048576;
	}

	public int getLoadedFiles()
	{
		return _loadedFiles;
	}

	public byte[] getPledgeCrest(int id)
	{
		return _cachePledge.get(id);
	}

	public byte[][] getPledgeCrestLarge(int id)
	{
		return _cachePledgeLarge.get(id);
	}

	public byte[] getAllyCrest(int id)
	{
		return _cacheAlly.get(id);
	}

	public void removePledgeCrest(int id)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_" + id + ".dds");
		_cachePledge.remove(id);
		try
		{
			crestFile.delete();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while deleting file " + crestFile + ':', e);
		}
	}

	public void removePledgeCrestLarge(int id)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_Large_" + id + ".dds");
		_cachePledgeLarge.remove(id);
		try
		{
			crestFile.delete();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while deleting file " + crestFile + ':', e);
		}
	}

	public void removeAllyCrest(int id)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/AllyCrest_" + id + ".dds");
		_cacheAlly.remove(id);
		try
		{
			crestFile.delete();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while deleting file " + crestFile + ':', e);
		}
	}

	public boolean savePledgeCrest(int newId, byte[] data)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_" + newId + ".dds");
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(crestFile);
			out.write(data);
			_cachePledge.put(newId, data);
			return true;
		}
		catch(IOException e)
		{
			_log.log(Level.ERROR, "Error saving pledge crest" + crestFile + ':', e);
			return false;
		}
		finally
		{
			try
			{
				out.close();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error closing file " + crestFile + ':', e);
			}
		}
	}

	public boolean savePledgeCrestLarge(int newId, byte[] data)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_Large_" + newId + ".dds");
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(crestFile);
			out.write(data);
			_cachePledgeLarge.put(newId, trimLargeCrestForPacket(data));
			return true;
		}
		catch(IOException e)
		{
			_log.log(Level.INFO, "Error saving Large pledge crest" + crestFile + ':', e);
			return false;
		}
		finally
		{
			try
			{
				out.close();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error closing file " + crestFile + ':', e);
			}
		}
	}

	public boolean saveAllyCrest(int newId, byte[] data)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/AllyCrest_" + newId + ".dds");
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(crestFile);
			out.write(data);
			_cacheAlly.put(newId, data);
			return true;
		}
		catch(IOException e)
		{
			_log.log(Level.ERROR, "Error saving ally crest" + crestFile + ':', e);
			return false;
		}
		finally
		{
			try
			{
				out.close();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error closing file " + crestFile + ':', e);
			}
		}
	}

	/***
	 * Разбивает файл больщой эмблемы на блоки байтов дл отправки в пакетах
	 * @param input изначальная эмблема
	 * @return последовательность блоков для отправки в пакетах
	 */
	public byte[][] trimLargeCrestForPacket(byte[] input)
	{
		int sourceLength = input.length;
		int currentPacketIndex = 0;
		int allPacketCount = 0;
		while(sourceLength > 0)
		{
			++currentPacketIndex;
			int currentPacketLenght = currentPacketIndex % 5 == 0 ? 8320 : 14336;

			++allPacketCount;
			sourceLength -= currentPacketLenght;
		}

		byte[][] output = new byte[allPacketCount][];

		currentPacketIndex = 0;
		int offset = 0;
		for(int itemIndex = 0; itemIndex < output.length; itemIndex++)
		{
			++currentPacketIndex;
			int currentPacketLenght = currentPacketIndex % 5 == 0 ? 8320 : 14336;
			if(offset + currentPacketLenght > input.length)
			{
				currentPacketLenght = input.length - offset;
			}
			output[itemIndex] = Arrays.copyOfRange(input, offset, offset + currentPacketLenght);
			offset += currentPacketLenght;
		}
		return output;
	}

	public CrestBuilderHolder createCrestLargeBuffer(int clanId, byte[] initBuffer)
	{
		return _cachePledgeLargeBuffer.put(clanId, new CrestBuilderHolder(initBuffer));
	}

	public CrestBuilderHolder getCrestLargeBuffer(int clanId)
	{
		return _cachePledgeLargeBuffer.get(clanId);
	}

	public void removeCrestLargeBuffer(int clanId)
	{
		_cachePledgeLargeBuffer.remove(clanId);
	}

	private static class SingletonHolder
	{
		protected static final CrestCache _instance = new CrestCache();
	}
}