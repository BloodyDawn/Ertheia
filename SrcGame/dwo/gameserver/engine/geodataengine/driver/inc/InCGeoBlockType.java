package dwo.gameserver.engine.geodataengine.driver.inc;

/**
 * @author Forsaiken
 */

public interface InCGeoBlockType
{
	byte getType();

	int nGetLayerCount(int geoX, int geoY);

	short nGetHeightAndNSWE(int geoX, int geoY, int z);

	short nGetLowerHeightAndNSWE(int geoX, int geoY, int z);

	void nGetUpperAndLowerHeightAndNSWE(int geoX, int geoY, int z, short[] store);

	short[][][] getBlock();
}