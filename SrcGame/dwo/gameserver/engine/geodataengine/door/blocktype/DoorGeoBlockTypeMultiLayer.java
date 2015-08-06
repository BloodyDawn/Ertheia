package dwo.gameserver.engine.geodataengine.door.blocktype;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.geodataengine.door.DoorGeoBlockType;
import dwo.gameserver.model.actor.instance.L2DoorInstance;

import java.util.List;

/**
 * @author Forsaiken
 */

public class DoorGeoBlockTypeMultiLayer implements DoorGeoBlockType
{
	private final int _geoX;
	private final int _geoY;
	private final int _instanceId;
	private final short[][][] _baseCells;
	private final short[][][] _workCells;
	private DoorGeoBlockTypeMultiLayer _nextInstance;

	public DoorGeoBlockTypeMultiLayer(int geoX, int geoY, short[][][] cells, int instanceId)
	{
		_geoX = geoX;
		_geoY = geoY;
		_instanceId = instanceId;
		_baseCells = cells;
		_workCells = copyCells(cells);
	}

	private static short[][][] copyCells(short[][][] baseCells)
	{
		short[][][] workCells = new short[GeoEngine.GEO_BLOCK_SHIFT][GeoEngine.GEO_BLOCK_SHIFT][];
		for(int i = GeoEngine.GEO_BLOCK_SHIFT, j, k; i-- > 0; )
		{
			for(j = GeoEngine.GEO_BLOCK_SHIFT; j-- > 0; )
			{
				workCells[i][j] = new short[baseCells[i][j].length];
				for(k = baseCells[i][j].length; k-- > 0; )
				{
					workCells[i][j][k] = baseCells[i][j][k];
				}
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
		for(int i = GeoEngine.GEO_BLOCK_SHIFT, j, k; i-- > 0; )
		{
			for(j = GeoEngine.GEO_BLOCK_SHIFT; j-- > 0; )
			{
				for(k = _baseCells[i][j].length; k-- > 0; )
				{
					_workCells[i][j][k] = _baseCells[i][j][k];
				}
			}
		}
	}

	@Override
	public short getCell(int geoX, int geoY, short height, int instanceId)
	{
		int cellX = GeoEngine.getCellXY(geoX);
		int cellY = GeoEngine.getCellXY(geoY);
		short[] layers = getInstanceCells(instanceId)[cellX][cellY];

		int layer;
		int sub1;
		int sub2;
		int sub2Sq = Integer.MAX_VALUE;
		for(layer = 0; layer < layers.length; layer++)
		{
			sub1 = height - GeoEngine.getHeight(layers[layer]);
			if(sub1 * sub1 < sub2Sq)
			{
				sub2 = sub1;
				sub2Sq = sub2 * sub2;
			}
			else
			{
				break;
			}
		}
		return layers[layer - 1];
	}

	@Override
	public short getCellBeyond(int geoX, int geoY, short height, int instanceId)
	{
		int cellX = GeoEngine.getCellXY(geoX);
		int cellY = GeoEngine.getCellXY(geoY);
		short[] layers = getInstanceCells(instanceId)[cellX][cellY];

		short heightAndNSWE = Short.MAX_VALUE;
		short temp;
		// from highest z (layer) to lowest z (layer)
		for(short layer : layers)
		{
			temp = layer;
			if(GeoEngine.getHeight(temp) < height)
			{
				return temp;
			}

			heightAndNSWE = temp;
		}
		return heightAndNSWE;
	}

	private void insertInstanceCells(L2DoorInstance door)
	{
		DoorGeoBlockTypeMultiLayer instanceBlock = getOrCreateInstance(door.getInstanceId());
		if(door.isOpened() || door.isAlikeDead() || !door.isVisible())
		{
			door.setPainted(false);
			return;
		}

		int cellX;
		int cellY;
		int layer;
		int sub1;
		int sub2;
		int sub2Sq;
		short height;
		short[] layers;

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

			height = GeoEngine.getHeight(geoCells[i][2]);

			layers = instanceBlock._baseCells[cellX][cellY];
			sub2Sq = Integer.MAX_VALUE;

			// from highest z (layer) to lowest z (layer)
			for(layer = 0; layer < layers.length; layer++)
			{
				sub1 = height - GeoEngine.getHeight(layers[layer]);
				if(sub1 * sub1 < sub2Sq)
				{
					sub2 = sub1;
					sub2Sq = sub2 * sub2;
				}
				else
				{
					break;
				}
			}
			instanceBlock._workCells[cellX][cellY][layer - 1] &= geoCells[i][2];
		}

		door.setPainted(true);
	}

	private DoorGeoBlockTypeMultiLayer getOrCreateInstance(int instanceId)
	{
		if(instanceId == _instanceId)
		{
			return this;
		}

		DoorGeoBlockTypeMultiLayer next = this;
		DoorGeoBlockTypeMultiLayer prev = this;
		while((next = next._nextInstance) != null)
		{
			prev = next;
			if(next._instanceId == instanceId)
			{
				return next;
			}
		}

		prev._nextInstance = new DoorGeoBlockTypeMultiLayer(_geoX, _geoY, _baseCells, instanceId);
		prev._nextInstance.resetCells();
		return prev._nextInstance;
	}

	private short[][][] getInstanceCells(int instanceId)
	{
		if(instanceId == 0)
		{
			return _workCells;
		}

		DoorGeoBlockTypeMultiLayer next = this;
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