package dwo.gameserver.engine.geodataengine.driver.l2j;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.geodataengine.driver.GeoDriver;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.nio.MappedByteBuffer;

/**
 * @author Forsaiken
 */

public class L2jGeoDriver implements GeoDriver
{
	private static final Logger _log = LogManager.getLogger(L2jGeoDriver.class);

	private final L2jGeoRegion[] _geoRegions;

	public L2jGeoDriver()
	{
		_geoRegions = new L2jGeoRegion[GeoEngine.getRegionOffset2(19, 19) + 1];
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
			for(byte[] regionIndex : regionIndexes)
			{
				reloadGeo(regionIndex[0], regionIndex[1]);
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
		MappedByteBuffer mbb = GeoEngine.loadRegion(regionX, regionY);
		if(mbb == null)
		{
			return false;
		}

		L2jGeoByteBuffer gbb = new L2jGeoByteBuffer(mbb);

		int layers;
		int maxLayers = 1;
		int[] indexs = new int[65536];
		for(int block = 0, index = 0; block < 65536; block++)
		{
			indexs[block] = index;
			switch(gbb.get(index++))
			{
				case GeoEngine.GEO_BLOCK_TYPE_FLAT:
					index += 2; // 1x short
					break;

				case GeoEngine.GEO_BLOCK_TYPE_COMPLEX:
					index += 128; // 64 x short
					break;

				case GeoEngine.GEO_BLOCK_TYPE_MULTILEVEL:
					for(int i = 64; i-- > 0; )
					{
						layers = gbb.get(index);
						if(!GeoEngine.layersValid(layers))
						{
							throw new RuntimeException("Broken geo data " + regionX + '_' + regionY + ".l2j, invalid layer count: " + layers);
						}

						if(layers > maxLayers)
						{
							maxLayers = layers;
						}

						index += (layers << 1) + 1;
					}
					break;
			}
		}

		int regionoffset = GeoEngine.getRegionOffset2(regionX - 10, regionY - 10);
		_geoRegions[regionoffset] = new L2jGeoRegion(indexs, gbb);

		_log.log(Level.INFO, "L2jGeoDriver: Loaded '" + regionX + '_' + regionY + ".l2j', MaxLayers: " + maxLayers);
		return true;
	}

	@Override
	public boolean nHasGeo(int geoX, int geoY)
	{
		return _geoRegions[GeoEngine.getRegionOffset(geoX, geoY)] != null;
	}

	@Override
	public byte nGetType(int geoX, int geoY)
	{
		L2jGeoRegion region = _geoRegions[GeoEngine.getRegionOffset(geoX, geoY)];
		if(region == null)
		{
			return GeoEngine.GEO_BLOCK_TYPE_FLAT;
		}

		int geoIndex = region.getGeoIndex(geoX, geoY);
		L2jGeoByteBuffer geo = region.getGeoData();
		return geo.get(geoIndex);
	}

	@Override
	public short nGetHeightAndNSWE(int geoX, int geoY, int z)
	{
		L2jGeoRegion region = _geoRegions[GeoEngine.getRegionOffset(geoX, geoY)];
		if(region == null)
		{
			return GeoEngine.convertHeightToHeightAndNSWEALL((short) z);
		}

		int geoIndex = region.getGeoIndex(geoX, geoY);
		L2jGeoByteBuffer geo = region.getGeoData();

		switch(geo.get(geoIndex++))
		{
			case GeoEngine.GEO_BLOCK_TYPE_FLAT:
				return GeoEngine.convertHeightToHeightAndNSWEALL(geo.getShort(geoIndex));

			case GeoEngine.GEO_BLOCK_TYPE_COMPLEX:
			{
				int cellX = GeoEngine.getCellXY(geoX);
				int cellY = GeoEngine.getCellXY(geoY);
				int cellIndex = GeoEngine.getCellIndex(cellX, cellY);
				return geo.getShort(geoIndex + (cellIndex << 1));
			}

			case GeoEngine.GEO_BLOCK_TYPE_MULTILEVEL:
				int cellX = GeoEngine.getCellXY(geoX);
				int cellY = GeoEngine.getCellXY(geoY);

				int cellIndex = GeoEngine.getCellIndex(cellX, cellY);
				int layers;

				while(cellIndex-- > 0)
				{
					layers = geo.get(geoIndex);
					geoIndex += (layers << 1) + 1;
				}

				layers = geo.get(geoIndex++);

				short heightAndNSWE = Short.MIN_VALUE;
				short temp;
				int sub1;
				int sub1Sq;
				int sub2Sq = Integer.MAX_VALUE;
				// from highest z (layer) to lowest z (layer)
				while(layers-- > 0)
				{
					temp = geo.getShort(geoIndex);
					sub1 = z - GeoEngine.getHeight(temp);
					sub1Sq = sub1 * sub1;
					if(sub1Sq < sub2Sq)
					{
						sub2Sq = sub1Sq;
						heightAndNSWE = temp;
					}
					else
					{
						break;
					}

					geoIndex += 2;
				}
				return heightAndNSWE;

			default:
				throw new RuntimeException("Invalid type at " + geoX + ", " + geoY);
		}
	}

	@Override
	public short nGetLowerHeightAndNSWE(int geoX, int geoY, int z)
	{
		L2jGeoRegion region = _geoRegions[GeoEngine.getRegionOffset(geoX, geoY)];
		if(region == null)
		{
			GeoEngine.convertHeightToHeightAndNSWEALL((short) z);
		}

		int geoIndex = region.getGeoIndex(geoX, geoY);
		L2jGeoByteBuffer geo = region.getGeoData();

		switch(geo.get(geoIndex++))
		{
			case GeoEngine.GEO_BLOCK_TYPE_FLAT:
				return GeoEngine.convertHeightToHeightAndNSWEALL(geo.getShort(geoIndex));

			case GeoEngine.GEO_BLOCK_TYPE_COMPLEX:
			{
				int cellX = GeoEngine.getCellXY(geoX);
				int cellY = GeoEngine.getCellXY(geoY);
				int cellIndex = GeoEngine.getCellIndex(cellX, cellY);
				return geo.getShort(geoIndex + (cellIndex << 1));
			}

			case GeoEngine.GEO_BLOCK_TYPE_MULTILEVEL:
				int cellX = GeoEngine.getCellXY(geoX);
				int cellY = GeoEngine.getCellXY(geoY);

				int cellIndex = GeoEngine.getCellIndex(cellX, cellY);
				int layers;

				while(cellIndex-- > 0)
				{
					geoIndex += (geo.get(geoIndex) << 1) + 1;
				}

				layers = geo.get(geoIndex++);

				short heightAndNSWE = Short.MAX_VALUE;
				short temp;
				// from highest z (layer) to lowest z (layer)
				while(layers-- > 0)
				{
					temp = geo.getShort(geoIndex);
					if(GeoEngine.getHeight(temp) < z)
					{
						return temp;
					}

					heightAndNSWE = temp;
					geoIndex += 2;
				}
				return heightAndNSWE;

			default:
				throw new RuntimeException("Invalid type at " + geoX + ", " + geoY);
		}
	}

	@Override
	public void nGetUpperAndLowerHeightAndNSWE(int geoX, int geoY, int z, short[] store)
	{
		L2jGeoRegion region = _geoRegions[GeoEngine.getRegionOffset(geoX, geoY)];
		if(region == null)
		{
			store[0] = Short.MAX_VALUE;
			store[1] = Short.MIN_VALUE;
			return;
		}

		int geoIndex = region.getGeoIndex(geoX, geoY);
		L2jGeoByteBuffer geo = region.getGeoData();

		switch(geo.get(geoIndex++))
		{
			case GeoEngine.GEO_BLOCK_TYPE_FLAT:
				short height = geo.getShort(geoIndex);
				if(z < height)
				{
					store[0] = store[1] = GeoEngine.convertHeightToHeightAndNSWEALL(height);
				}
				else
				{
					store[0] = Short.MAX_VALUE;
					store[1] = GeoEngine.convertHeightToHeightAndNSWEALL(height);
				}
				break;

			case GeoEngine.GEO_BLOCK_TYPE_COMPLEX:
				short heightAndNSWE = geo.getShort(geoIndex + (GeoEngine.getCellIndexFromGeoXY(geoX, geoY) << 1));
				if(z < GeoEngine.getHeight(heightAndNSWE))
				{
					store[0] = store[1] = heightAndNSWE;
				}
				else
				{
					store[0] = Short.MAX_VALUE;
					store[1] = heightAndNSWE;
				}
				break;

			case GeoEngine.GEO_BLOCK_TYPE_MULTILEVEL:
				int cellIndex = GeoEngine.getCellIndexFromGeoXY(geoX, geoY);
				while(cellIndex-- > 0)
				{
					geoIndex += (geo.get(geoIndex) << 1) + 1;
				}

				short upperHeightAndNSWE = Short.MAX_VALUE;
				short lowerHeightAndNSWE = Short.MIN_VALUE;

				int layers = geo.get(geoIndex++);
				while(layers-- > 0)
				{
					lowerHeightAndNSWE = geo.getShort(geoIndex);
					if(z < GeoEngine.getHeight(lowerHeightAndNSWE))
					{
						upperHeightAndNSWE = lowerHeightAndNSWE;
					}
					else
					{
						break;
					}
					geoIndex += 2;
				}

				store[0] = upperHeightAndNSWE;
				store[1] = lowerHeightAndNSWE;
				break;
		}
	}

	@Override
	public short[][][] getBlock(int geoX, int geoY)
	{
		L2jGeoRegion region = _geoRegions[GeoEngine.getRegionOffset(geoX, geoY)];
		if(region == null)
		{
			return null;
		}

		int geoIndex = region.getGeoIndex(geoX, geoY);
		L2jGeoByteBuffer geo = region.getGeoData();

		switch(geo.get(geoIndex++))
		{
			case GeoEngine.GEO_BLOCK_TYPE_FLAT:
			{
				short[][][] block = new short[GeoEngine.GEO_BLOCK_SHIFT][GeoEngine.GEO_BLOCK_SHIFT][1];
				short heightAndNSWE = GeoEngine.convertHeightToHeightAndNSWEALL(geo.getShort(geoIndex));
				for(int i = GeoEngine.GEO_BLOCK_SHIFT; i-- > 0; )
				{
					for(int j = GeoEngine.GEO_BLOCK_SHIFT; j-- > 0; )
					{
						block[i][j][0] = heightAndNSWE;
					}
				}
				return block;
			}

			case GeoEngine.GEO_BLOCK_TYPE_COMPLEX:
			{
				short[][][] block = new short[GeoEngine.GEO_BLOCK_SHIFT][GeoEngine.GEO_BLOCK_SHIFT][1];
				for(int i = GeoEngine.GEO_BLOCK_SHIFT; i-- > 0; )
				{
					for(int j = GeoEngine.GEO_BLOCK_SHIFT; j-- > 0; )
					{
						block[i][j][0] = geo.getShort(geoIndex + (GeoEngine.getCellIndex(i, j) << 1));
					}
				}
				return block;
			}

			case GeoEngine.GEO_BLOCK_TYPE_MULTILEVEL:
				short[][][] block = new short[GeoEngine.GEO_BLOCK_SHIFT][GeoEngine.GEO_BLOCK_SHIFT][];
				for(int i = GeoEngine.GEO_BLOCK_SHIFT, j, geoIndex2, cellIndex, layers; i-- > 0; )
				{
					for(j = GeoEngine.GEO_BLOCK_SHIFT; j-- > 0; )
					{
						geoIndex2 = geoIndex;

						cellIndex = GeoEngine.getCellIndex(i, j);
						while(cellIndex-- > 0)
						{
							layers = geo.get(geoIndex2);
							geoIndex2 += (layers << 1) + 1;
						}

						layers = geo.get(geoIndex2++);

						block[i][j] = new short[layers];
						while(layers-- > 0)
						{
							block[i][j][layers] = geo.getShort(geoIndex2);
							geoIndex2 += 2;
						}
					}
				}
				return block;

			default:
				throw new RuntimeException("Invalid type at " + geoX + ", " + geoY);
		}
	}

	@Override
	public int nGetLayerCount(int geoX, int geoY)
	{
		L2jGeoRegion region = _geoRegions[GeoEngine.getRegionOffset(geoX, geoY)];
		if(region == null)
		{
			return 1;
		}

		int geoIndex = region.getGeoIndex(geoX, geoY);
		L2jGeoByteBuffer geo = region.getGeoData();

		switch(geo.get(geoIndex++))
		{
			case GeoEngine.GEO_BLOCK_TYPE_FLAT:
				return 1;

			case GeoEngine.GEO_BLOCK_TYPE_COMPLEX:
				return 1;

			case GeoEngine.GEO_BLOCK_TYPE_MULTILEVEL:
				int cellX = GeoEngine.getCellXY(geoX);
				int cellY = GeoEngine.getCellXY(geoY);

				int cellIndex = GeoEngine.getCellIndex(cellX, cellY);

				while(cellIndex-- > 0)
				{
					geoIndex += (geo.get(geoIndex) << 1) + 1;
				}
				return geo.get(geoIndex++);

			default:
				throw new RuntimeException("Invalid type at " + geoX + ", " + geoY);
		}
	}
}