package dwo.gameserver.engine.geodataengine.driver.inc.blocktype;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.geodataengine.driver.inc.InCGeoBlockType;

import java.nio.ByteBuffer;

/**
 * Flat block, 1 level, 1 height.
 *
 * @author Forsaiken
 */
public class InCGeoBlockTypeFlat implements InCGeoBlockType
{
	private final short _heightAndNSWE;

	public InCGeoBlockTypeFlat(ByteBuffer bb)
	{
		_heightAndNSWE = GeoEngine.convertHeightToHeightAndNSWEALL(bb.getShort());
	}

	@Override
	public byte getType()
	{
		return GeoEngine.GEO_BLOCK_TYPE_FLAT;
	}

	@Override
	public int nGetLayerCount(int geoX, int geoY)
	{
		return 1;
	}

	@Override
	public short nGetHeightAndNSWE(int geoX, int geoY, int z)
	{
		return _heightAndNSWE;
	}

	@Override
	public short nGetLowerHeightAndNSWE(int geoX, int geoY, int z)
	{
		return nGetHeightAndNSWE(geoX, geoY, z);
	}

	@Override
	public void nGetUpperAndLowerHeightAndNSWE(int geoX, int geoY, int z, short[] store)
	{
		if(z < GeoEngine.getHeight(_heightAndNSWE))
		{
			store[0] = store[1] = _heightAndNSWE;
		}
		else
		{
			store[0] = Short.MAX_VALUE;
			store[1] = _heightAndNSWE;
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
				block[i][j][0] = _heightAndNSWE;
			}
		}
		return block;
	}

	@Override
	public int hashCode()
	{
		return _heightAndNSWE;
	}

	@Override
	public boolean equals(Object object)
	{
		return object instanceof InCGeoBlockTypeFlat && _heightAndNSWE == ((InCGeoBlockTypeFlat) object)._heightAndNSWE;
	}
}