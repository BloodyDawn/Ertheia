package dwo.gameserver.engine.geodataengine.driver.inc;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.geodataengine.driver.GeoDriver;
import dwo.gameserver.engine.geodataengine.driver.inc.blocktype.InCGeoBlockTypeComplex;
import dwo.gameserver.engine.geodataengine.driver.inc.blocktype.InCGeoBlockTypeFlat;
import dwo.gameserver.engine.geodataengine.driver.inc.blocktype.InCGeoBlockTypeMultiLevel;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.nio.MappedByteBuffer;

/**
 * @author Forsaiken
 */

public class InCGeoDriver implements GeoDriver
{
	private static final Logger _log = LogManager.getLogger(InCGeoDriver.class);

	private final InCGeoRegion[][] _geoRegions;

	public InCGeoDriver()
	{
		_geoRegions = new InCGeoRegion[19][19];
		reloadGeo();
	}

	@Override
	public boolean reloadGeo()
	{
		byte[][] regionIndexes = GeoEngine.loadRegionIndexes();
		if(regionIndexes == null)
		{
			return false;
		}

		try
		{
			FastMap<InCGeoBlockType, InCGeoBlockType> geoBlockBufferFlat = new FastMap<>();
			FastMap<InCGeoBlockType, InCGeoBlockType> geoBlockBufferComplex = new FastMap<>();
			FastMap<InCGeoBlockType, InCGeoBlockType> geoBlockBufferMultiLevel = new FastMap<>();

			for(byte[] regionIndex : regionIndexes)
			{
				if(reloadGeo(regionIndex[0], regionIndex[1], geoBlockBufferFlat, geoBlockBufferComplex, geoBlockBufferMultiLevel))
				{
					_log.log(Level.INFO, "loaded geo file '" + regionIndex[0] + '_' + regionIndex[1] + ".l2j'");
				}
				else
				{
					_log.log(Level.ERROR, "Unable to load geo file '" + regionIndex[0] + '_' + regionIndex[1] + ".l2j'");
				}
			}

			return true;
		}
		catch(Exception e)
		{
			_log.log(Level.INFO, "Error while loading geo region", e);
			return false;
		}
	}

	@Override
	public boolean reloadGeo(byte regionX, byte regionY)
	{
		return reloadGeo(regionX, regionY, new FastMap<>(), new FastMap<>(), new FastMap<>());
	}

	@Override
	public boolean nHasGeo(int geoX, int geoY)
	{
		return getGeoRegion(geoX, geoY) != null;
	}

	@Override
	public byte nGetType(int geoX, int geoY)
	{
		InCGeoRegion region = getGeoRegion(geoX, geoY);
		return region != null ? region.nGetType(geoX, geoY) : GeoEngine.GEO_BLOCK_TYPE_FLAT;
	}

	@Override
	public short nGetHeightAndNSWE(int geoX, int geoY, int z)
	{
		InCGeoRegion region = getGeoRegion(geoX, geoY);
		return region != null ? region.nGetHeightAndNSWE(geoX, geoY, z) : GeoEngine.convertHeightToHeightAndNSWEALL((short) z);
	}

	@Override
	public short nGetLowerHeightAndNSWE(int geoX, int geoY, int z)
	{
		InCGeoRegion region = getGeoRegion(geoX, geoY);
		return region != null ? region.nGetLowerHeightAndNSWE(geoX, geoY, z) : GeoEngine.convertHeightToHeightAndNSWEALL((short) z);
	}

	@Override
	public void nGetUpperAndLowerHeightAndNSWE(int geoX, int geoY, int z, short[] store)
	{
		InCGeoRegion region = getGeoRegion(geoX, geoY);
		if(region != null)
		{
			region.nGetUpperAndLowerHeightAndNSWE(geoX, geoY, z, store);
		}
		else
		{
			store[0] = Short.MAX_VALUE;
			store[1] = Short.MIN_VALUE;
		}
	}

	@Override
	public short[][][] getBlock(int geoX, int geoY)
	{
		InCGeoRegion region = getGeoRegion(geoX, geoY);
		return region != null ? region.getBlock(geoX, geoY) : null;
	}

	@Override
	public int nGetLayerCount(int geoX, int geoY)
	{
		InCGeoRegion region = getGeoRegion(geoX, geoY);
		return region != null ? region.nGetLayerCount(geoX, geoY) : 1;
	}

	public boolean reloadGeo(byte regionX, byte regionY, FastMap<InCGeoBlockType, InCGeoBlockType> geoBlockBufferFlat, FastMap<InCGeoBlockType, InCGeoBlockType> geoBlockBufferComplex, FastMap<InCGeoBlockType, InCGeoBlockType> geoBlockBufferMultiLevel)
	{
		MappedByteBuffer bb = GeoEngine.loadRegion(regionX, regionY);
		if(bb == null)
		{
			return false;
		}

		geoBlockBufferComplex.clear();
		geoBlockBufferMultiLevel.clear();

		InCGeoBlockType[][] blockTypes = new InCGeoBlockType[GeoEngine.GEO_REGION_SIZE][GeoEngine.GEO_REGION_SIZE];

		InCGeoBlockType loaded;
		InCGeoBlockType stored;
		for(int x = 0; x < GeoEngine.GEO_REGION_SIZE; x++)
		{
			for(int y = 0; y < GeoEngine.GEO_REGION_SIZE; y++)
			{
				switch(bb.get())
				{
					case GeoEngine.GEO_BLOCK_TYPE_FLAT:
						loaded = new InCGeoBlockTypeFlat(bb);
						stored = Config.GEODATA_DRIVER_INC_OPTIMIZE ? geoBlockBufferFlat.putIfAbsent(loaded, loaded) : null;
						blockTypes[x][y] = stored == null ? loaded : stored;
						break;

					case GeoEngine.GEO_BLOCK_TYPE_COMPLEX:
						loaded = new InCGeoBlockTypeComplex(bb);
						stored = Config.GEODATA_DRIVER_INC_OPTIMIZE ? geoBlockBufferComplex.putIfAbsent(loaded, loaded) : null;
						blockTypes[x][y] = stored == null ? loaded : stored;
						break;

					case GeoEngine.GEO_BLOCK_TYPE_MULTILEVEL:
						loaded = new InCGeoBlockTypeMultiLevel(bb);
						stored = Config.GEODATA_DRIVER_INC_OPTIMIZE ? geoBlockBufferMultiLevel.putIfAbsent(loaded, loaded) : null;
						blockTypes[x][y] = stored == null ? loaded : stored;
						break;

					default:
						throw new RuntimeException();
				}
			}
		}

		int maxLayerCount = InCGeoBlockTypeMultiLevel.maxLayerCount;
		InCGeoBlockTypeMultiLevel.maxLayerCount = 0;

		int correctedLayersDublicated = InCGeoBlockTypeMultiLevel.correctedLayersDublicated;
		InCGeoBlockTypeMultiLevel.correctedLayersDublicated = 0;

		int correctedLayersWrongOrder = InCGeoBlockTypeMultiLevel.correctedLayersWrongOrder;
		InCGeoBlockTypeMultiLevel.correctedLayersWrongOrder = 0;

		_log.log(Level.INFO, "InCGeoDriver: Loaded '" + regionX + '_' + regionY + ".l2j', MaxLayers: " + maxLayerCount + ", MultiLayerCorrections: " + correctedLayersDublicated + " (dublicated), " + correctedLayersWrongOrder + " (wrong order)");

		regionX -= 10;
		regionY -= 10;
		_geoRegions[regionX][regionY] = new InCGeoRegion(regionX, regionY, blockTypes);
		return true;
	}

	public InCGeoRegion getGeoRegion(int geoX, int geoY)
	{
		int regionX = GeoEngine.getRegionXY(geoX);
		int regionY = GeoEngine.getRegionXY(geoY);
		return _geoRegions[regionX][regionY];
	}
}