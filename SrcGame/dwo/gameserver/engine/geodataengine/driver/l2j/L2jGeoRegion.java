package dwo.gameserver.engine.geodataengine.driver.l2j;

import dwo.gameserver.engine.geodataengine.GeoEngine;

/**
 * @author Forsaiken
 */

public class L2jGeoRegion
{
	private final int[] _geoIndexes;
	private final L2jGeoByteBuffer _geoData;

	public L2jGeoRegion(int[] geoIndexes, L2jGeoByteBuffer geoData)
	{
		_geoIndexes = geoIndexes;
		_geoData = geoData;
	}

	public int getGeoIndex(int geoX, int geoY)
	{
		return getGeoIndexByBlockXY(GeoEngine.getBlockXY(geoX), GeoEngine.getBlockXY(geoY));
	}

	public int getGeoIndexByBlockXY(int blockX, int blockY)
	{
		return _geoIndexes[GeoEngine.getBlockIndex(blockX, blockY)];
	}

	public L2jGeoByteBuffer getGeoData()
	{
		return _geoData;
	}
}