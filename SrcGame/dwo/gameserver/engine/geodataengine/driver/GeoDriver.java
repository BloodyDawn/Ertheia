package dwo.gameserver.engine.geodataengine.driver;

/**
 * @author Forsaiken
 */

public interface GeoDriver
{
	boolean reloadGeo();

	boolean reloadGeo(byte regionX, byte regionY);

	boolean nHasGeo(int geoX, int geoY);

	byte nGetType(int geoX, int geoY);

	short nGetHeightAndNSWE(int geoX, int geoY, int z);

	short nGetLowerHeightAndNSWE(int geoX, int geoY, int z);

	void nGetUpperAndLowerHeightAndNSWE(int geoX, int geoY, int z, short[] store);

	short[][][] getBlock(int blockX, int blockY);

	int nGetLayerCount(int geoX, int geoY);
}