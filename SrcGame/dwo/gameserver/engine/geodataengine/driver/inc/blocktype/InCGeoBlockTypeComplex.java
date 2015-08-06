package dwo.gameserver.engine.geodataengine.driver.inc.blocktype;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.geodataengine.driver.inc.InCGeoBlockType;

import java.nio.ByteBuffer;

/**
 * Complex block, 1 level, 64 heights (each cell in block).
 *
 * @author Forsaiken
 */
public class InCGeoBlockTypeComplex implements InCGeoBlockType
{
	private final short[][] _heights;
	private int _hash;

	public InCGeoBlockTypeComplex(ByteBuffer bb)
	{
		_heights = new short[GeoEngine.GEO_BLOCK_SHIFT][GeoEngine.GEO_BLOCK_SHIFT];
		for(int x = 0; x < GeoEngine.GEO_BLOCK_SHIFT; x++)
		{
			for(int y = 0; y < GeoEngine.GEO_BLOCK_SHIFT; y++)
			{
				_heights[x][y] = bb.getShort();
				_hash = _hash * 31 + _heights[x][y];
			}
		}
	}

	@Override
	public byte getType()
	{
		return GeoEngine.GEO_BLOCK_TYPE_COMPLEX;
	}

	@Override
	public int nGetLayerCount(int geoX, int geoY)
	{
		return 1;
	}

	@Override
	public short nGetHeightAndNSWE(int geoX, int geoY, int z)
	{
		int cellX = GeoEngine.getCellXY(geoX);
		int cellY = GeoEngine.getCellXY(geoY);
		return _heights[cellX][cellY];
	}

	@Override
	public short nGetLowerHeightAndNSWE(int geoX, int geoY, int z)
	{
		return nGetHeightAndNSWE(geoX, geoY, z);
	}

	@Override
	public void nGetUpperAndLowerHeightAndNSWE(int geoX, int geoY, int z, short[] store)
	{
		int cellX = GeoEngine.getCellXY(geoX);
		int cellY = GeoEngine.getCellXY(geoY);
		short heightAndNSWE = _heights[cellX][cellY];
		if(z < GeoEngine.getHeight(heightAndNSWE))
		{
			store[0] = store[1] = heightAndNSWE;
		}
		else
		{
			store[0] = Short.MAX_VALUE;
			store[1] = heightAndNSWE;
		}
	}

	@Override
	public short[][][] getBlock()
	{
		short[][][] block = new short[GeoEngine.GEO_BLOCK_SHIFT][GeoEngine.GEO_BLOCK_SHIFT][1];
		for(int i = GeoEngine.GEO_BLOCK_SHIFT; i-- > 0; )
		{
			for(int j = GeoEngine.GEO_BLOCK_SHIFT; j-- > 0; )
			{
				block[i][j][0] = _heights[i][j];
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
		if(object instanceof InCGeoBlockTypeComplex)
		{
			InCGeoBlockTypeComplex cgb = (InCGeoBlockTypeComplex) object;
			for(int x = 0; x < GeoEngine.GEO_BLOCK_SHIFT; x++)
			{
				for(int y = 0; y < GeoEngine.GEO_BLOCK_SHIFT; y++)
				{
					if(_heights[x][y] != cgb._heights[x][y])
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