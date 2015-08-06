package dwo.gameserver.engine.geodataengine;


import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.geometry.Point3D;

/**
 * @author Forsaiken
 */
public class NullGeoEngine extends GeoEngine
{
	@Override
	public void reloadGeo()
	{

	}

	@Override
	public void reloadGeo(byte regionX, byte regionY)
	{

	}

	@Override
	public boolean hasGeo(int x, int y)
	{
		return false;
	}

	@Override
	public byte getType(int x, int y)
	{
		return GeoEngine.GEO_BLOCK_TYPE_FLAT;
	}

	@Override
	public short getHeight(int x, int y, int z)
	{
		return (short) z;
	}

	@Override
	public short getSpawnHeight(int x, int y, int minZ, int maxZ)
	{
		return (short) minZ;
	}

	@Override
	public boolean canSeeTarget(L2Object cha, L2Object target)
	{
		return Math.abs(target.getZ() - cha.getZ()) < 1000;
	}

	@Override
	public boolean canSeeTarget(L2Object cha, Point3D worldPosition)
	{
		return Math.abs(worldPosition.getZ() - cha.getZ()) < 1000;
	}

	@Override
	public boolean canSeeTarget(int x1, int y1, int z1, int x2, int y2, int z2, int instanceId)
	{
		return Math.abs(z1 - z2) < 1000;
	}

	@Override
	public Location moveCheck(int x1, int y1, int z1, int x2, int y2, int z2, int instanceId)
	{
		return new Location(x2, y2, z2);
	}

	@Override
	public boolean canMoveFromToTarget(int x1, int y1, int z1, int x2, int y2, int z2, int instanceId)
	{
		return true;
	}

	@Override
	public short[][][] getBlock(int geoX, int geoY)
	{
		return null;
	}

	@Override
	public int[][] tracertCells(int x1, int y1, int z1, int x2, int y2, boolean checkBlocking)
	{
		return null;
	}

	@Override
	public byte nGetType(int geoX, int geoY)
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
		return GeoEngine.convertHeightToHeightAndNSWEALL((short) z);
	}

	@Override
	public short nGetLowerHeightAndNSWE(int geoX, int geoY, int z)
	{
		return GeoEngine.convertHeightToHeightAndNSWEALL((short) z);
	}

	@Override
	public boolean nCanMoveFromToTargetNoChecks(int geoX1, int geoY1, int z1, int geoX2, int geoY2, int z2, int instanceId)
	{
		return true;
	}
}