package dwo.gameserver.engine.geodataengine.driver.inc;

import dwo.gameserver.engine.geodataengine.GeoEngine;

/**
 * @author Forsaiken
 */

public class InCGeoRegion
{
	private final int _regionX;
	private final int _regionY;
	private final InCGeoBlockType[][] _geoBlockTypes;

	public InCGeoRegion(int regionX, int regionY, InCGeoBlockType[][] geoBlockTypes)
	{
		_regionX = regionX;
		_regionY = regionY;
		_geoBlockTypes = geoBlockTypes;
	}

	public int getRegionX()
	{
		return _regionX;
	}

	public int getRegionY()
	{
		return _regionY;
	}

	public byte nGetType(int geoX, int geoY)
	{
		int blockX = GeoEngine.getBlockXY(geoX);
		int blockY = GeoEngine.getBlockXY(geoY);
		return _geoBlockTypes[blockX][blockY].getType();
	}

	public short nGetHeightAndNSWE(int geoX, int geoY, int x)
	{
		int blockX = GeoEngine.getBlockXY(geoX);
		int blockY = GeoEngine.getBlockXY(geoY);
		return _geoBlockTypes[blockX][blockY].nGetHeightAndNSWE(geoX, geoY, x);
	}

	public short nGetLowerHeightAndNSWE(int geoX, int geoY, int z)
	{
		int blockX = GeoEngine.getBlockXY(geoX);
		int blockY = GeoEngine.getBlockXY(geoY);
		return _geoBlockTypes[blockX][blockY].nGetLowerHeightAndNSWE(geoX, geoY, z);
	}

	public short[][][] getBlock(int geoX, int geoY)
	{
		int blockX = GeoEngine.getBlockXY(geoX);
		int blockY = GeoEngine.getBlockXY(geoY);
		return _geoBlockTypes[blockX][blockY].getBlock();
	}

	public int nGetLayerCount(int geoX, int geoY)
	{
		int blockX = GeoEngine.getBlockXY(geoX);
		int blockY = GeoEngine.getBlockXY(geoY);
		return _geoBlockTypes[blockX][blockY].nGetLayerCount(geoX, geoY);
	}

	public void nGetUpperAndLowerHeightAndNSWE(int geoX, int geoY, int z, short[] store)
	{
		int blockX = GeoEngine.getBlockXY(geoX);
		int blockY = GeoEngine.getBlockXY(geoY);
		_geoBlockTypes[blockX][blockY].nGetUpperAndLowerHeightAndNSWE(geoX, geoY, z, store);
	}
}