package dwo.gameserver.engine.geodataengine;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.driver.inc.InCGeoDriver;
import dwo.gameserver.engine.geodataengine.driver.l2j.L2jGeoDriver;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.geometry.Point3D;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * @author Forsaiken
 */

public abstract class GeoEngine
{
	public static final File GEO_FILES = new File(Config.DATAPACK_ROOT, "/geodata/");
	public static final File GEO_INDEX_FILE = new File(GEO_FILES, "geo_index.txt");
	public static final int CELL_SHIFT = 4; // 16 units each cell
	public static final int GEOX_MIN = 0 >> CELL_SHIFT;
	public static final int GEOX_MAX = WorldManager.MAP_MAX_X - WorldManager.MAP_MIN_X >> CELL_SHIFT;
	public static final int GEOY_MIN = 0 >> CELL_SHIFT;
	public static final int GEOY_MAX = WorldManager.MAP_MAX_Y - WorldManager.MAP_MIN_Y >> CELL_SHIFT;
	public static final int GEO_BLOCK_SHIFT = 8; // 8 cells each block
	public static final int GEO_REGION_SIZE = 1 << GEO_BLOCK_SHIFT;
	public static final int GEO_REGION_MIN_FILE_SIZE = GEO_REGION_SIZE * GEO_REGION_SIZE * 3;
	public static final int GEO_BLOCK_REGION_SHIFT = 11;
	public static final byte GEO_BLOCK_TYPE_FLAT = 0;
	public static final byte GEO_BLOCK_TYPE_COMPLEX = 1;
	public static final byte GEO_BLOCK_TYPE_MULTILEVEL = 2;
	public static final int NSWE_MASK = 0x0000000F;
	public static final int HEIGHT_MASK = 0x0000FFF0;
	public static final byte EAST = 1;
	public static final byte NEAST = ~EAST & NSWE_MASK;
	public static final byte WEST = 1 << 1;
	public static final byte NWEST = ~WEST & NSWE_MASK;
	public static final byte SOUTH = 1 << 2;
	public static final byte NSOUTH = ~SOUTH & NSWE_MASK;
	public static final byte SOUTHWEST = SOUTH | WEST;
	public static final byte SOUTHEAST = SOUTH | EAST;
	public static final byte NORTH = 1 << 3;
	public static final byte NNORTH = ~NORTH & NSWE_MASK;
	public static final byte NORTHWEST = NORTH | WEST;
	public static final byte NORTHEAST = NORTH | EAST;
	public static final byte NSWE_ALL = EAST | WEST | SOUTH | NORTH;
	public static final byte NSWE_NONE = 0;
	protected static final Logger _log = LogManager.getLogger(GeoEngine.class);
	private static GeoEngine _instance;

	public static void init()
	{
		if(Config.GEODATA_ENABLED)
		{
			if(Config.GEODATA_DRIVER.equalsIgnoreCase("InC"))
			{
				_instance = new ComplexGeoEngine(new InCGeoDriver());
				_log.log(Level.INFO, "GeoData: InCGeoDriver activated. GeoData enabled.");
			}
			else if(Config.GEODATA_DRIVER.equalsIgnoreCase("L2j"))
			{
				_instance = new ComplexGeoEngine(new L2jGeoDriver());
				_log.log(Level.INFO, "GeoData: L2jGeoDriver activated. GeoData enabled.");
			}
			else
			{
				_instance = new NullGeoEngine();
				_log.log(Level.INFO, "GeoData: GeoDriver '" + Config.GEODATA_DRIVER + "' unknown. GeoData disabled.");
			}
		}
		else
		{
			_instance = new NullGeoEngine();
			_log.log(Level.INFO, "GeoData: GeoData disabled.");
		}
	}

	public static GeoEngine getInstance()
	{
		return _instance;
	}

	public static int getGeoX(int x)
	{
		return x - WorldManager.MAP_MIN_X >> CELL_SHIFT;
	}

	public static int getGeoY(int y)
	{
		return y - WorldManager.MAP_MIN_Y >> CELL_SHIFT;
	}

	public static int getWorldX(int geoX)
	{
		return (geoX << CELL_SHIFT) + WorldManager.MAP_MIN_X;
	}

	public static int getWorldY(int geoY)
	{
		return (geoY << CELL_SHIFT) + WorldManager.MAP_MIN_Y;
	}

	public static int getBlockXY(int geoXY)
	{
		return (geoXY >> 3) % GEO_REGION_SIZE;
	}

	public static int getCellXY(int geoXY)
	{
		return geoXY % GEO_BLOCK_SHIFT;
	}

	public static int getRegionXY(int geoXY)
	{
		return geoXY >> GEO_BLOCK_REGION_SHIFT;
	}

	public static int getGeoXY(int regionXY, int blockXY)
	{
		return (regionXY << GEO_BLOCK_REGION_SHIFT) + (blockXY << 3);
	}

	public static int getRegionOffset(int geoX, int geoY)
	{
		int regionX = getRegionXY(geoX);
		int regionY = getRegionXY(geoY);
		return getRegionOffset2(regionX, regionY);
	}

	public static int getRegionOffset2(int regionX, int regionY)
	{
		return (regionX << 5) + regionY;
	}

	public static int getBlockIndex(int blockX, int blockY)
	{
		return (blockX << GEO_BLOCK_SHIFT) + blockY;
	}

	public static int getCellIndex(int cellX, int cellY)
	{
		return (cellX << 3) + cellY;
	}

	public static int getCellIndexFromGeoXY(int geoX, int geoY)
	{
		return (getCellXY(geoX) << 3) + getCellXY(geoY);
	}

	public static short getHeight(short height)
	{
		height &= HEIGHT_MASK;
		height >>= 1;
		return height;
	}

	public static short getHeight(int height)
	{
		return getHeight((short) height);
	}

	public static short getNSWE(short heightAndNSWE)
	{
		return (short) (heightAndNSWE & NSWE_MASK);
	}

	public static short getNSWE(int heightAndNSWE)
	{
		return (short) (heightAndNSWE & NSWE_MASK);
	}

	public static short convertHeightToHeightAndNSWEALL(short height)
	{
		height <<= 1;
		height |= NSWE_ALL;
		return height;
	}

	public static boolean layersValid(int layers)
	{
		return layers > 0 && layers < 126;
	}

	public static boolean checkNSWE(short NSWE, int x1, int y1, int x2, int y2)
	{
		if((NSWE & NSWE_ALL) == NSWE_ALL)
		{
			return true;
		}

		if(x2 > x1)
		{
			if((NSWE & EAST) == 0)
			{
				return false;
			}
		}
		else if(x2 < x1)
		{
			if((NSWE & WEST) == 0)
			{
				return false;
			}
		}

		if(y2 > y1)
		{
			if((NSWE & SOUTH) == 0)
			{
				return false;
			}
		}
		else if(y2 < y1)
		{
			if((NSWE & NORTH) == 0)
			{
				return false;
			}
		}

		return true;
	}

	public static boolean checkNSWEALL(short NSWE)
	{
		return (NSWE & NSWE_ALL) == NSWE_ALL;
	}

	public static boolean checkEAST(short NSWE)
	{
		return (NSWE & EAST) == EAST;
	}

	public static boolean checkWEST(short NSWE)
	{
		return (NSWE & WEST) == WEST;
	}

	public static boolean checkSOUTH(short NSWE)
	{
		return (NSWE & SOUTH) == SOUTH;
	}

	public static boolean checkNORTH(short NSWE)
	{
		return (NSWE & NORTH) == NORTH;
	}

	public static boolean checkNORTHWEST(short NSWE)
	{
		return (NSWE & NORTHWEST) == NORTHWEST;
	}

	public static boolean checkNORTHEAST(short NSWE)
	{
		return (NSWE & NORTHEAST) == NORTHEAST;
	}

	public static boolean checkSOUTHWEST(short NSWE)
	{
		return (NSWE & SOUTHWEST) == SOUTHWEST;
	}

	public static boolean checkSOUTHEAST(short NSWE)
	{
		return (NSWE & SOUTHEAST) == SOUTHEAST;
	}

    public static byte[][] loadRegionIndexes() 
    {
        final ArrayList<byte[]> _geoFileIndexes = new ArrayList<>();
        
        for (String fileName : GeoEngine.GEO_FILES.list()) 
        {
            String temp = fileName.substring(0, fileName.indexOf("."));
            String[] params = temp.split("_");
            byte geoX = Byte.parseByte(params[0]);
            byte geoY = Byte.parseByte(params[1]);
            
            _geoFileIndexes.add(new byte[] { geoX, geoY });
        }
        return _geoFileIndexes.toArray(new byte[_geoFileIndexes.size()][]);
    }

	public static MappedByteBuffer loadRegion(byte regionX, byte regionY)
	{
		String geoFileName = regionX + "_" + regionY + ".l2j";
		File geoFile = new File(GEO_FILES, geoFileName);

		// compressGeodataFile(geoFile,new File(geoFile.getName()+".l2p"));

		if(!geoFile.isFile())
		{
			return null;
		}

		FileInputStream fis = null;

		try
		{
			fis = new FileInputStream(geoFile);
			FileChannel fc = fis.getChannel();
			if(fc.size() >= GEO_REGION_MIN_FILE_SIZE)
			{
				MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
				bb.order(ByteOrder.LITTLE_ENDIAN);
				return bb;
			}
			else
			{

				return null;
			}
		}
		catch(IOException e)
		{
			return null;
		}
		finally
		{
			try
			{
				fis.close();
			}
			catch(Exception e)
			{
				// Ignored
			}
		}
	}

	public abstract void reloadGeo();

	public abstract void reloadGeo(byte regionX, byte regionY);

	public abstract boolean hasGeo(int x, int y);

	public abstract byte getType(int x, int y);

	public abstract short getHeight(int x, int y, int z);

	public abstract short getSpawnHeight(int x, int y, int minZ, int maxZ);

	public abstract boolean canSeeTarget(L2Object cha, L2Object target);

	public abstract boolean canSeeTarget(L2Object cha, Point3D worldPosition);

	public abstract boolean canSeeTarget(int x1, int y1, int z1, int x2, int y2, int z2, int instanceId);

	public abstract Location moveCheck(int x1, int y1, int z1, int x2, int y2, int z2, int instanceId);

	public abstract boolean canMoveFromToTarget(int x1, int y1, int z1, int x2, int y2, int z2, int instanceId);

	public abstract short[][][] getBlock(int geoX, int geoY);

	public abstract int[][] tracertCells(int x1, int y1, int z1, int x2, int y2, boolean checkBlocking);

	public abstract byte nGetType(int geoX, int geoY);

	public abstract int nGetLayerCount(int geoX, int geoY);

	public abstract short nGetHeightAndNSWE(int geoX, int geoY, int z);

	public abstract short nGetLowerHeightAndNSWE(int geoX, int geoY, int z);

	public abstract boolean nCanMoveFromToTargetNoChecks(int geoX1, int geoY1, int z1, int geoX2, int geoY2, int z2, int instanceId);
}