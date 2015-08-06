package dwo.config.main;

import dwo.config.Config;
import dwo.config.ConfigProperties;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 07.10.11
 * Time: 11:53
 */

public class ConfigGeodata extends Config
{
	private static final String path = GEODATA_CONFIG_FILE;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			GEODATA_ENABLED = getBoolean(properties, "GeoDataEnabled", false);
			GEODATA_DRIVER = getString(properties, "GeoDataGeoDriver", "L2j");
			GEODATA_DRIVER_INC_OPTIMIZE = getBoolean(properties, "GeoDataGeoDriverInCOptimize", true);
			GEODATA_HEIGHT_DIFF_MOVE_CHECKS = getBoolean(properties, "GeoDataHeightDiffMoveChecks", false);
			GEODATA_ALT_MOVE_CHECKS = getBoolean(properties, "GeoDataAltMoveChecks", false);
			GEODATA_PATHFINDING_MODE = getInt(properties, "PathFindingMode", 0);
			GEODATA_PATHFINDING_ADVANCED_PATH_FILTER_PC = getBoolean(properties, "PathFindingAdvancedPathFilterPc", true);
			GEODATA_PATHFINDING_STOP_IF_NO_PATH_FOUND_PC = getBoolean(properties, "PathFindingStopIfNoPathFoundPc", false);
			GEODATA_PATHFINDING_HEURISTIC_MOD_PC = getFloat(properties, "PathFindingHeuristicModPc", 2.0F);
			GEODATA_PATHFINDING_HEURISTIC_MOD_NPC = getFloat(properties, "PathFindingHeuristicModNpc", 3.0F);
			GEODATA_PATHFINDING_ALLOW_DIAGONAL_MOVEMENT = getBoolean(properties, "PathFindingAllowDiagonalMovement", false);
			GEODATA_PATHFINDING_3D_MOVEMENT = getBoolean(properties, "PathFinding3DMovement", false);
			GEODATA_PATHFINDING_COMPUTE_BUFFER_CAPACITY = getInt(properties, "PathFindingComputeBufferCapacity", 4096);
			COORD_SYNCHRONIZE = getInt(properties, "CoordSynchronize", -1);
			WORLD_X_MIN = getInt(properties, "WorldXMin", 10);
			WORLD_X_MAX = getInt(properties, "WorldXMax", 28);
			WORLD_Y_MIN = getInt(properties, "WorldYMin", 10);
			WORLD_Y_MAX = getInt(properties, "WorldYMax", 28);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
