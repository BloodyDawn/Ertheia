package dwo.gameserver.engine.geodataengine.driver.inc.blocktype;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.geodataengine.driver.inc.InCGeoBlockType;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * MultiLevel block, x levels, 64 heights + y (each cell can have multiple heights)
 *
 * @author Forsaiken
 */
public class InCGeoBlockTypeMultiLevel implements InCGeoBlockType
{
	public static int maxLayerCount;
	public static int correctedLayersDublicated;
	public static int correctedLayersWrongOrder;

	private final short[][][] _heights;
	private int _hash;

	public InCGeoBlockTypeMultiLevel(ByteBuffer bb)
	{
		_heights = new short[GeoEngine.GEO_BLOCK_SHIFT][GeoEngine.GEO_BLOCK_SHIFT][];

		short lastLayerHeight;
		short currentLayer;
		short currentLayerHeight;

		int layers;
		for(int x = 0; x < GeoEngine.GEO_BLOCK_SHIFT; x++)
		{
			for(int y = 0; y < GeoEngine.GEO_BLOCK_SHIFT; y++)
			{
				layers = bb.get();
				if(!GeoEngine.layersValid(layers))
				{
					throw new RuntimeException("Invalid layer count " + layers);
				}

				if(layers > maxLayerCount)
				{
					maxLayerCount = layers;
				}

				lastLayerHeight = Short.MAX_VALUE;

				_heights[x][y] = new short[layers];
				for(int i = 0; i < layers; i++)
				{
					currentLayer = bb.getShort();
					currentLayerHeight = GeoEngine.getHeight(currentLayer);
					if(currentLayerHeight == lastLayerHeight)
					{
						correctedLayersDublicated++;
					}
					else if(currentLayerHeight > lastLayerHeight)
					{
						correctedLayersWrongOrder++;
					}
					else
					{
						lastLayerHeight = currentLayerHeight;
						_heights[x][y][i] = currentLayer;
						_hash = _hash * 31 + currentLayer;
					}
				}
			}
		}
	}

	@Override
	public byte getType()
	{
		return GeoEngine.GEO_BLOCK_TYPE_MULTILEVEL;
	}

	@Override
	public int nGetLayerCount(int geoX, int geoY)
	{
		int cellX = GeoEngine.getCellXY(geoX);
		int cellY = GeoEngine.getCellXY(geoY);
		return _heights[cellX][cellY].length;
	}

	@Override
	public short nGetHeightAndNSWE(int geoX, int geoY, int z)
	{
		int cellX = GeoEngine.getCellXY(geoX);
		int cellY = GeoEngine.getCellXY(geoY);
		short[] heights = _heights[cellX][cellY];

		short heightAndNSWE = Short.MIN_VALUE;
		short temp;
		int sub1;
		int sub1Sq;
		int sub2Sq = Integer.MAX_VALUE;
		// from highest z (layer) to lowest z (layer)
		for(short height : heights)
		{
			temp = height;
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
		}
		return heightAndNSWE;
	}

	@Override
	public short nGetLowerHeightAndNSWE(int geoX, int geoY, int z)
	{
		int cellX = GeoEngine.getCellXY(geoX);
		int cellY = GeoEngine.getCellXY(geoY);
		short[] heights = _heights[cellX][cellY];

		short heightAndNSWE = Short.MAX_VALUE;
		short temp;
		// from highest z (layer) to lowest z (layer)
		for(short height : heights)
		{
			temp = height;
			if(GeoEngine.getHeight(temp) < z)
			{
				return temp;
			}

			heightAndNSWE = temp;
		}
		return heightAndNSWE;
	}

	@Override
	public void nGetUpperAndLowerHeightAndNSWE(int geoX, int geoY, int z, short[] store)
	{
		int cellX = GeoEngine.getCellXY(geoX);
		int cellY = GeoEngine.getCellXY(geoY);
		short[] heights = _heights[cellX][cellY];

		short upperHeightAndNSWE = Short.MAX_VALUE;
		short lowerHeightAndNSWE = Short.MIN_VALUE;

		for(short height : heights)
		{
			lowerHeightAndNSWE = height;
			if(z < GeoEngine.getHeight(lowerHeightAndNSWE))
			{
				upperHeightAndNSWE = lowerHeightAndNSWE;
			}
			else
			{
				break;
			}
		}

		store[0] = upperHeightAndNSWE;
		store[1] = lowerHeightAndNSWE;
	}

	@Override
	public short[][][] getBlock()
	{
		short[][][] block = new short[GeoEngine.GEO_BLOCK_SHIFT][GeoEngine.GEO_BLOCK_SHIFT][];
		for(int i = GeoEngine.GEO_BLOCK_SHIFT; i-- > 0; )
		{
			for(int j = GeoEngine.GEO_BLOCK_SHIFT; j-- > 0; )
			{
				block[i][j] = new short[_heights[i][j].length];
				System.arraycopy(_heights[i][j], 0, block[i][j], 0, _heights[i][j].length);
			}
		}
		return block;
	}

	@Override
	public int hashCode()
	{
		return _hash;
	}

	@Override
	public boolean equals(Object object)
	{
		if(object instanceof InCGeoBlockTypeMultiLevel)
		{
			InCGeoBlockTypeMultiLevel mlgb = (InCGeoBlockTypeMultiLevel) object;
			for(int x = 0; x < GeoEngine.GEO_BLOCK_SHIFT; x++)
			{
				for(int y = 0; y < GeoEngine.GEO_BLOCK_SHIFT; y++)
				{
					if(!Arrays.equals(_heights[x][y], mlgb._heights[x][y]))
					{
						return false;
					}
				}
			}
			return true;
		}
		else
		{
			return false;
		}
	}
}