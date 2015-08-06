package dwo.gameserver.engine.geodataengine.door;

import dwo.gameserver.model.actor.instance.L2DoorInstance;

import java.util.List;

/**
 * @author Forsaiken
 */

public interface DoorGeoBlockType
{
	void updateCells(List<L2DoorInstance> doors);

	void resetCells();

	short getCell(int geoX, int geoY, short height, int instanceId);

	short getCellBeyond(int geoX, int geoY, short height, int instanceId);
}