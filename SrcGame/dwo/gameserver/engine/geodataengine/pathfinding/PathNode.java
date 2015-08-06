package dwo.gameserver.engine.geodataengine.pathfinding;

import dwo.gameserver.engine.geodataengine.GeoEngine;

/**
 * @author Forsaiken
 */
public class PathNode
{
	public static final PathNode[] EMPTY_NODES = new PathNode[0];

	private final PathNode[] _neighbors;
	private byte _neighborsSize;

	private int _geoX;
	private int _geoY;
	private short _heightAndNSWE;
	private byte _direction;

	private float _cost;
	private float _heuristic;

	private PathNode _parent;
	private PathNode _upper;
	private PathNode _lower;

	public PathNode(int geoX, int geoY, short heightAndNSWE)
	{
		_geoX = geoX;
		_geoY = geoY;
		_heightAndNSWE = heightAndNSWE;
		_neighbors = null;
	}

	public PathNode(boolean allowDiagonalMovement, boolean startNode)
	{
		_neighbors = new PathNode[allowDiagonalMovement ? startNode ? 8 : 5 : startNode ? 4 : 3];
	}

	public PathNode init(int geoX, int geoY, short heightAndNSWE, byte direction, float cost)
	{
		_geoX = geoX;
		_geoY = geoY;
		_heightAndNSWE = heightAndNSWE;
		_direction = direction;
		_cost = cost;
		_neighborsSize = 0;
		_upper = null;
		_lower = null;
		return this;
	}

	public PathNode[] getNeighbors()
	{
		return _neighbors;
	}

	public byte getNeighborsSize()
	{
		return _neighborsSize;
	}

	public void attachNeighbor(PathNode neighbor)
	{
		_neighbors[_neighborsSize++] = neighbor;
	}

	public boolean isComputed()
	{
		return _neighborsSize == -1;
	}

	public void setComputed()
	{
		_neighborsSize = -1;
	}

	public byte getDirection()
	{
		return _direction;
	}

	public PathNode getParent()
	{
		return _parent;
	}

	public void setParent(PathNode parent)
	{
		_parent = parent;
	}

	public PathNode getUpper()
	{
		return _upper;
	}

	public void setUpper(PathNode upper)
	{
		_upper = upper;
	}

	public PathNode getLower()
	{
		return _lower;
	}

	public void setLower(PathNode lower)
	{
		_lower = lower;
	}

	public void setGeoXY(int geoX, int geoY)
	{
		_geoX = geoX;
		_geoY = geoY;
	}

	public int getX()
	{
		int x = GeoEngine.getWorldX(_geoX);

		if(!GeoEngine.checkEAST(_heightAndNSWE))
		{
			x -= 16;
		}

		if(!GeoEngine.checkWEST(_heightAndNSWE))
		{
			x += 16;
		}

		return x + 8;
	}

	public int getY()
	{
		int y = GeoEngine.getWorldY(_geoY);

		if(!GeoEngine.checkSOUTH(_heightAndNSWE))
		{
			y -= 16;
		}

		if(!GeoEngine.checkNORTH(_heightAndNSWE))
		{
			y += 16;
		}

		return y + 8;
	}

	public int getZ()
	{
		return getHeight();
	}

	public int getGeoX()
	{
		return _geoX;
	}

	public int getGeoY()
	{
		return _geoY;
	}

	public short getHeight()
	{
		return GeoEngine.getHeight(_heightAndNSWE);
	}

	public short getHeightAndNSWE()
	{
		return _heightAndNSWE;
	}

	public void setHeightAndNSWE(short heightAndNSWE)
	{
		_heightAndNSWE = heightAndNSWE;
	}

	public float getCost()
	{
		return _cost;
	}

	public void setCost(float cost)
	{
		_cost = cost;
	}

	public float getHeuristic()
	{
		return _heuristic;
	}

	public void setHeuristic(float heuristic)
	{
		_heuristic = heuristic;
	}

	public float getF()
	{
		return _cost + _heuristic;
	}

	public boolean equals(PathNode node)
	{
		if(_geoX != node._geoX)
		{
			return false;
		}

		return _geoY == node._geoY;

	}

	@Override
	public int hashCode()
	{
		return _geoX * 31 + _geoY;
	}

	@Override
	public boolean equals(Object object)
	{
		return object instanceof PathNode && equals((PathNode) object);
	}

	@Override
	public PathNode clone()
	{
		PathNode n = new PathNode(_geoX, _geoY, _heightAndNSWE);
		n._heuristic = _heuristic;
		n._cost = _cost;
		return n;
	}

	@Override
	public String toString()
	{
		return _geoX + ", " + _geoY + ", " + _heightAndNSWE + '@' + super.hashCode();
	}
}