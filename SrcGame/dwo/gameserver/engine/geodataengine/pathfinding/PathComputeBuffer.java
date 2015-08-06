package dwo.gameserver.engine.geodataengine.pathfinding;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.geodataengine.PathFinding;
import dwo.gameserver.model.world.zone.Location;
import org.inc.incolution.util.list.IncArrayList;

/**
 * @author Forsaiken
 */
public class PathComputeBuffer
{
	final IncArrayList<PathNode> _pathBuildQueueOne;
	final IncArrayList<PathNode> _pathBuildQueueTwo;
	private final boolean _allowDiagonalMovement;
	private final PathNode _startNode;
	private final PathNode _endNode;
	private final PathNodeList _open;
	private final PathNodePositionSet _closed;
	private final PathNode[] _nodeBuffer;
	private int _nodeBufferPos;

	public PathComputeBuffer(boolean allowDiagonalMovement, int capacity)
	{
		_allowDiagonalMovement = allowDiagonalMovement;
		_startNode = new PathNode(allowDiagonalMovement, true);
		_endNode = new PathNode(allowDiagonalMovement, false);

		_open = new PathNodeList(capacity);
		_closed = new PathNodePositionSet(this, capacity);

        _nodeBuffer = new PathNode[capacity];
        int i = capacity;
        while (i-- > 0) {
            _nodeBuffer[i] = new PathNode(allowDiagonalMovement, false);
        }
		_nodeBufferPos = _nodeBuffer.length;

		_pathBuildQueueOne = new IncArrayList<>();
		_pathBuildQueueTwo = new IncArrayList<>();
	}

	private static Location computeLocation(PathNode node)
	{
		return new Location(GeoEngine.getWorldX(node.getGeoX()) + 8, GeoEngine.getWorldY(node.getGeoY()) + 8, node.getHeight());
	}

	public boolean allowDiagonalMovement()
	{
		return _allowDiagonalMovement;
	}

	public PathNode getStartNode(int geoX, int geoY, short heightAndNSWE)
	{
		return _startNode.init(geoX, geoY, heightAndNSWE, PathFinding.DIRECTION_NONE, 0);
	}

	public PathNode getEndNode(int geoX, int geoY, short heightAndNSWE)
	{
		return _endNode.init(geoX, geoY, heightAndNSWE, PathFinding.DIRECTION_NONE, 0);
	}

	public void reset()
	{
		_open.reset();
		_closed.clear();

//		if(_nodeBufferPos != _nodeBuffer.length)
//		{
//			throw new IllegalArgumentException(_nodeBufferPos + ", " + _nodeBuffer.length);
//		}
	}

	public PathNodeList getOpen()
	{
		return _open;
	}

	public PathNodePositionSet getClosed()
	{
		return _closed;
	}

	public Location[] buildPath(PathNode node, int x1, int y1, int z1, int x2, int y2, int z2, boolean playerMove, int instanceId)
	{
		_pathBuildQueueOne.reset();
		_pathBuildQueueOne.addLast(node);

		if(!playerMove || !Config.GEODATA_PATHFINDING_ADVANCED_PATH_FILTER_PC)
		{
			int previousDirectionX = Integer.MIN_VALUE;
			int previousDirectionY = Integer.MIN_VALUE;
			int directionX;
			int directionY;
			int tmpX;
			int tmpY;

			while(node.getParent() != null)
			{
				if(node.getParent().getParent() != null)
				{
					tmpX = node.getGeoX() - node.getParent().getParent().getGeoX();
					tmpY = node.getGeoY() - node.getParent().getParent().getGeoY();
					if(Math.abs(tmpX) == Math.abs(tmpY))
					{
						directionX = tmpX;
						directionY = tmpY;
					}
					else
					{
						directionX = node.getGeoX() - node.getParent().getGeoX();
						directionY = node.getGeoY() - node.getParent().getGeoY();
					}
				}
				else
				{
					directionX = node.getGeoX() - node.getParent().getGeoX();
					directionY = node.getGeoY() - node.getParent().getGeoY();
				}
				// only add a new route point if moving direction changes
				if(directionX != previousDirectionX || directionY != previousDirectionY)
				{
					previousDirectionX = directionX;
					previousDirectionY = directionY;

					_pathBuildQueueOne.addLast(node);
				}

				node = node.getParent();
			}
		}
		else
		{
			while(node.getParent() != null)
			{
				node = node.getParent();
				_pathBuildQueueOne.addLast(node);
			}
		}

		if(_pathBuildQueueOne.size() > 2)
		{
			_pathBuildQueueTwo.reset();

			PathNode cur;
			PathNode next;
			for(int i = _pathBuildQueueOne.size(), j; i-- > 0; )
			{
				cur = _pathBuildQueueOne.getUnsafe(i);
				_pathBuildQueueTwo.addLast(cur);

				next = cur;

				for(j = 0; j < i; )
				{
					next = _pathBuildQueueOne.getUnsafe(j);
					if(GeoEngine.getInstance().nCanMoveFromToTargetNoChecks(cur.getGeoX(), cur.getGeoY(), cur.getHeight(), next.getGeoX(), next.getGeoY(), next.getHeight(), instanceId))
					{
						break;
					}
					else
					{
						j++;
					}
				}

				if(!next.equals(cur) && i != j)
				{
					i = j + 1;
				}
				else
				{
					break;
				}
			}

			Location[] locations = new Location[_pathBuildQueueTwo.size()];
			locations[0] = new Location(x1, y1, z1);

			for(int i = _pathBuildQueueTwo.size(); i-- > 1; )
			{
				locations[i] = computeLocation(_pathBuildQueueTwo.getUnsafe(i));
			}
			return locations;
		}
		else
		{
			Location[] locations = new Location[_pathBuildQueueOne.size()];
			locations[0] = new Location(x1, y1, z1);
			locations[locations.length - 1] = new Location(x2, y2, z2);

			for(int i = _pathBuildQueueOne.size() - 1, j = 1; i-- > 1; j++)
			{
				locations[j] = computeLocation(node);
			}
			return locations;
		}
	}

	public PathNode getQueuedPathNode(int geoX, int geoY, short heightAndNSWE, byte direction, float cost)
	{
		return _nodeBuffer[--_nodeBufferPos].init(geoX, geoY, heightAndNSWE, direction, cost);
	}

	public void queueNode(PathNode node)
	{
		try
		{
			_nodeBuffer[_nodeBufferPos++] = node;
		}
		catch(Exception e)
		{
			// Ignored
		}
	}

	/**
	 *
	 * @return True if it has more then 7 nodes in queue
	 */
	public boolean hasRemainingNodes()
	{
		return _nodeBufferPos > 7;
	}
}