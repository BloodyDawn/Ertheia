package dwo.gameserver.engine.geodataengine.door.blocktype;

import dwo.gameserver.engine.geodataengine.door.DoorGeoBlockType;
import dwo.gameserver.model.actor.instance.L2DoorInstance;

import java.util.List;

/**
 * @author Forsaiken
 */

public class DoorGeoBlockTypeEmpty implements DoorGeoBlockType
{
	public static final DoorGeoBlockTypeEmpty STATIC_INSTANCE = new DoorGeoBlockTypeEmpty();

	private DoorGeoBlockTypeEmpty()
	{

	}

	@Override
	public void updateCells(List<L2DoorInstance> doors)
	{

	}

	@Override
	public void resetCells()
	{

	}

	@Override
	public short getCell(int geoX, int geoY, short height, int instanceId)
	{
		return -1;
	}

	@Override
	public short getCellBeyond(int geoX, int geoY, short height, int instanceId)
	{
		return -1;
	}
}