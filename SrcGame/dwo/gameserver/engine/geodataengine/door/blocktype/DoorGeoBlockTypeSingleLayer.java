package dwo.gameserver.engine.geodataengine.door.blocktype;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.geodataengine.door.DoorGeoBlockType;
import dwo.gameserver.model.actor.instance.L2DoorInstance;

import java.util.List;

/**
 * @author Forsaiken
 */

public class DoorGeoBlockTypeSingleLayer implements DoorGeoBlockType
{
	private final int _geoX;
	private final int _geoY;
	private final int _instanceId;
	private final short[][][] _baseCells;
	private final short[][][] _workCells;
	private DoorGeoBlockTypeSingleLayer _nextInstance;

	public DoorGeoBlockTypeSingleLayer(int geoX, int geoY, short[][][] cells, int instanceId)
	{
		_geoX = geoX;
		_geoY = geoY;
		_instanceId = instanceId;
		_baseCells = cells;
		_workCells = copyCells(cells);
	}

	private static short[][][] copyCells(short[][][] baseCells)
	{
		short[][][] workCells = new short[GeoEngine.GEO_BLOCK_SHIFT][GeoEngine.GEO_BLOCK_SHIFT][1];
		for(int i = GeoEngine.GEO_BLOCK_SHIFT, j; i-- > 0; )
		{
			for(j = GeoEngine.GEO_BLOCK_SHIFT; j-- > 0; )
			{
				workCells[i][j][0] = baseCells[i][j][0];
			}
		}
		return workCells;
	}

	@Override
	public void updateCells(List<L2DoorInstance> doors)
	{
		_nextInstance = null;

		resetCells();

		for(int i = doors.size(); i-- > 0; )
		{
			insertInstanceCells(doors.get(i));
		}
	}

	@Override
	public void resetCells()
	{
		for(int i = GeoEngine.GEO_BLOCK_SHIFT, j; i-- > 0; )
		{
			for(j = GeoEngine.GEO_BLOCK_SHIFT; j-- > 0; )
			{
				_workCells[i][j][0] = -1;
			}
		}
	}

	@Override
	public short getCell(int geoX, int geoY, short height, int instanceId)
	{
		int cellX = GeoEngine.getCellXY(geoX);
		int cellY = GeoEngine.getCellXY(geoY);
		return getInstanceCells(instanceId)[cellX][cellY][0];
	}

	@Override
	public short getCellBeyond(int geoX, int geoY, short height, int instanceId)
	{
		int cellX = GeoEngine.getCellXY(geoX);
		int cellY = GeoEngine.getCellXY(geoY);
		return getInstanceCells(instanceId)[cellX][cellY][0];
	}

	private void insertInstanceCells(L2DoorInstance door)
	{
		DoorGeoBlockTypeSingleLayer instanceBlock = getOrCreateInstance(door.getInstanceId());

		if(door.isOpened() || door.isAlikeDead() || !door.isVisible())
		{
			door.setPainted(false);
			return;
		}

		int cellX;
		int cellY;

		int[][] geoCells = door.getDoorTemplate().getCells();
		for(int i = geoCells.length; i-- > 0; )
		{
			cellX = geoCells[i][0] - _geoX;
			cellY = geoCells[i][1] - _geoY;

			if(cellX < 0 || cellX >= GeoEngine.GEO_BLOCK_SHIFT)
			{
				continue;
			}

			if(cellY < 0 || cellY >= GeoEngine.GEO_BLOCK_SHIFT)
			{
				continue;
			}

			instanceBlock._workCells[cellX][cellY][0] &= geoCells[i][2];
		}

		door.setPainted(true);
	}

	private DoorGeoBlockTypeSingleLayer getOrCreateInstance(int instanceId)
	{
		if(instanceId == _instanceId)
		{
			return this;
		}

		DoorGeoBlockTypeSingleLayer next = this;
		DoorGeoBlockTypeSingleLayer prev = this;
		while((next = next._nextInstance) != null)
		{
			prev = next;
			if(next._instanceId == instanceId)
			{
				return next;
			}
		}

		prev._nextInstance = new DoorGeoBlockTypeSingleLayer(_geoX, _geoY, _baseCells, instanceId);
		prev._nextInstance.resetCells();
		return prev._nextInstance;
	}

	private short[][][] getInstanceCells(int instanceId)
	{
		if(instanceId == 0)
		{
			return _workCells;
		}

		DoorGeoBlockTypeSingleLayer next = this;
		while((next = next._nextInstance) != null)
		{
			if(next._instanceId == instanceId)
			{
				return next._workCells;
			}
		}
		return _workCells;
	}
}