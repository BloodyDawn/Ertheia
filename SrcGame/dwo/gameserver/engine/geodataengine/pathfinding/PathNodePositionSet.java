package dwo.gameserver.engine.geodataengine.pathfinding;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import org.inc.incolution.util.list.IncArrayList;

/**
 * @author Forsaiken
 */

public class PathNodePositionSet
{
	public static final PathNode STATIC_OBJECT_NOT_FOUND = new PathNode(false, false);
	public static final PathNode STATIC_OBJECT_OUT_OF_GRID = new PathNode(false, false);
	public static final int MAX_HEIGHT_DIFF_SQUARE = 30 * 30;

	private final PathComputeBuffer _pathComputeBuffer;
	private final PathNodeHolder[][] _grid;
	private final IncArrayList<PathNodeHolder> _used;
	private final int _gridMax;
	private int _geoXOffset;
	private int _geoYOffset;

	public PathNodePositionSet(PathComputeBuffer pathComputeBuffer, int initialCapacity)
	{
		_pathComputeBuffer = pathComputeBuffer;

		_gridMax = (int) Math.sqrt(initialCapacity) << 1;
		_grid = new PathNodeHolder[_gridMax][_gridMax];
		for(int i = _gridMax, j; i-- > 0; )
		{
			for(j = _gridMax; j-- > 0; )
			{
				_grid[i][j] = new PathNodeHolder();
			}
		}

		_used = new IncArrayList<>(initialCapacity);
	}

	public boolean init(int geoX1, int geoY1, int geoX2, int geoY2)
	{
		int diffX = Math.abs(geoX2 - geoX1);
		int diffY = Math.abs(geoY2 - geoY1);

		if(diffY >= _gridMax || diffX >= _gridMax)
		{
			return false;
		}

		int gridDiffXMod = _gridMax - (_gridMax - diffX) / 2;
		int gridDiffYMod = _gridMax - (_gridMax - diffY) / 2;

		_geoXOffset = geoX2 > geoX1 ? geoX1 - (_gridMax - gridDiffXMod) : geoX1 - gridDiffXMod;

		_geoYOffset = geoY2 > geoY1 ? geoY1 - (_gridMax - gridDiffYMod) : geoY1 - gridDiffYMod;

		return true;
	}

	public PathNode addIfAbsentHeightUnknown(PathNode node, int instanceId)
	{
		return Config.GEODATA_PATHFINDING_3D_MOVEMENT ? addIfAbsentHeightUnknown3D(node, instanceId) : addIfAbsentHeightUnknown2D(node, instanceId);
	}

	public PathNode addIfAbsentHeightUnknown2D(PathNode node, int instanceId)
	{
		int geoX = node.getGeoX();
		int geoY = node.getGeoY();
		int gridX = geoX - _geoXOffset;
		int gridY = geoY - _geoYOffset;

		if(gridX < 0 || gridX >= _gridMax)
		{
			return STATIC_OBJECT_OUT_OF_GRID;
		}

		if(gridY < 0 || gridY >= _gridMax)
		{
			return STATIC_OBJECT_OUT_OF_GRID;
		}

		PathNodeHolder holder = _grid[gridX][gridY];
		PathNode stored = holder.node;
		if(stored != null)
		{
			return stored;
		}

		holder.node = node;
		_used.addLastUnsafe(holder);

		short heightAndNSWE = GeoEngine.getInstance().nGetHeightAndNSWE(geoX, geoY, node.getHeight());
		node.setHeightAndNSWE(heightAndNSWE);
		return STATIC_OBJECT_NOT_FOUND;
	}

	public PathNode addIfAbsentHeightUnknown3D(PathNode node, int instanceId)
	{
		int geoX = node.getGeoX();
		int geoY = node.getGeoY();
		int gridX = geoX - _geoXOffset;
		int gridY = geoY - _geoYOffset;

		if(gridX < 0 || gridX >= _gridMax)
		{
			return STATIC_OBJECT_OUT_OF_GRID;
		}

		if(gridY < 0 || gridY >= _gridMax)
		{
			return STATIC_OBJECT_OUT_OF_GRID;
		}

		short heightAndNSWE = GeoEngine.getInstance().nGetHeightAndNSWE(geoX, geoY, node.getHeight());
		node.setHeightAndNSWE(heightAndNSWE);

		PathNodeHolder holder = _grid[gridX][gridY];
		PathNode stored = holder.node;
		if(stored != null)
		{
			short newHeight = GeoEngine.getHeight(heightAndNSWE);
			int storedHeight = stored.getHeight();

			if(newHeight == storedHeight)
			{
				return stored;
			}

			PathNode temp;
			if(storedHeight < newHeight)
			{
				PathNode prev = stored;
				temp = stored;
				while((temp = temp.getUpper()) != null)
				{
					if(temp.getHeight() > newHeight)
					{
						prev.setUpper(node);
						node.setUpper(temp);
						return STATIC_OBJECT_NOT_FOUND;
					}
					else
					{
						prev = temp;
					}
				}
				prev.setUpper(node);
			}
			else
			{
				PathNode prev = stored;
				temp = stored;
				while((temp = temp.getLower()) != null)
				{
					if(temp.getHeight() < newHeight)
					{
						prev.setLower(node);
						node.setLower(temp);
						return STATIC_OBJECT_NOT_FOUND;
					}
					else
					{
						prev = temp;
					}
				}
				prev.setLower(node);
			}

			return STATIC_OBJECT_NOT_FOUND;
		}

		holder.node = node;
		_used.addLastUnsafe(holder);

		return STATIC_OBJECT_NOT_FOUND;
	}

	public void clear()
	{
		PathNodeHolder holder;
		PathNode node;
		PathNode temp;
		for(int i = _used.size(); i-- > 0; )
		{
			holder = _used.getUnsafe(i);
			node = holder.node;
			if(node != null)
			{
				holder.node = null;

				_pathComputeBuffer.queueNode(node);

				temp = node;
				while((temp = temp.getUpper()) != null)
				{
					_pathComputeBuffer.queueNode(temp);
				}

				temp = node;
				while((temp = temp.getLower()) != null)
				{
					_pathComputeBuffer.queueNode(temp);
				}
			}
		}
		_used.reset();
	}

	private static class PathNodeHolder
	{
		public PathNode node;

		PathNodeHolder()
		{

		}
	}
}