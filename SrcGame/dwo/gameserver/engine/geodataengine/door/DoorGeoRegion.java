package dwo.gameserver.engine.geodataengine.door;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.geodataengine.door.blocktype.DoorGeoBlockTypeEmpty;
import dwo.gameserver.engine.geodataengine.door.blocktype.DoorGeoBlockTypeMultiLayer;
import dwo.gameserver.engine.geodataengine.door.blocktype.DoorGeoBlockTypeSingleLayer;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.templates.L2DoorTemplate;

import java.util.List;

/**
 * @author Forsaiken
 */

public class DoorGeoRegion
{
	private final int _regionX;
	private final int _regionY;
	private final int _geoRegionX;
	private final int _geoRegionY;
	private final DoorGeoBlockType[][] _geoBlockTypes;

	public DoorGeoRegion(int regionX, int regionY, DoorGeoEngine engine)
	{
		_regionX = regionX;
		_regionY = regionY;
		_geoRegionX = _regionX - 10;
		_geoRegionY = _regionY - 10;
		_geoBlockTypes = new DoorGeoBlockType[GeoEngine.GEO_REGION_SIZE][GeoEngine.GEO_REGION_SIZE];
		reload(engine);
	}

	public void reload(DoorGeoEngine engine)
	{
		List<L2DoorTemplate> doorsInRegion = engine.getAllDoorsInRegion(_geoRegionX, _geoRegionY);

		if(doorsInRegion == null)
		{
			for(int i = GeoEngine.GEO_REGION_SIZE, j; i-- > 0; )
			{
				for(j = GeoEngine.GEO_REGION_SIZE; j-- > 0; )
				{
					_geoBlockTypes[i][j] = DoorGeoBlockTypeEmpty.STATIC_INSTANCE;
				}
			}
		}
		else
		{
			for(int i = GeoEngine.GEO_REGION_SIZE, j, geoX, geoY; i-- > 0; )
			{
				for(j = GeoEngine.GEO_REGION_SIZE; j-- > 0; )
				{
					geoX = GeoEngine.getGeoXY(_geoRegionX, i);
					geoY = GeoEngine.getGeoXY(_geoRegionY, j);

					if(engine.isDoorInSameBlock(geoX, geoY, doorsInRegion))
					{
						switch(GeoEngine.getInstance().nGetType(geoX, geoY))
						{
							case GeoEngine.GEO_BLOCK_TYPE_FLAT:
							case GeoEngine.GEO_BLOCK_TYPE_COMPLEX:
							{
								short[][][] block = GeoEngine.getInstance().getBlock(geoX, geoY);
								_geoBlockTypes[i][j] = block != null ? new DoorGeoBlockTypeSingleLayer(geoX, geoY, block, 0) : DoorGeoBlockTypeEmpty.STATIC_INSTANCE;
								break;
							}

							case GeoEngine.GEO_BLOCK_TYPE_MULTILEVEL:
								short[][][] block = GeoEngine.getInstance().getBlock(geoX, geoY);
								_geoBlockTypes[i][j] = block != null ? new DoorGeoBlockTypeMultiLayer(geoX, geoY, block, 0) : DoorGeoBlockTypeEmpty.STATIC_INSTANCE;
								break;

							default:
								_geoBlockTypes[i][j] = DoorGeoBlockTypeEmpty.STATIC_INSTANCE;
								break;
						}
					}
					else
					{
						_geoBlockTypes[i][j] = DoorGeoBlockTypeEmpty.STATIC_INSTANCE;
					}
				}
			}
		}
	}

	public void updateCells(int geoX, int geoY, List<L2DoorInstance> doors)
	{
		int blockX = GeoEngine.getBlockXY(geoX);
		int blockY = GeoEngine.getBlockXY(geoY);
		_geoBlockTypes[blockX][blockY].updateCells(doors);
	}

	public short getCell(int geoX, int geoY, short height, int instanceId)
	{
		int blockX = GeoEngine.getBlockXY(geoX);
		int blockY = GeoEngine.getBlockXY(geoY);
		return _geoBlockTypes[blockX][blockY].getCell(geoX, geoY, height, instanceId);
	}

	public short getCellBeyond(int geoX, int geoY, short height, int instanceId)
	{
		int blockX = GeoEngine.getBlockXY(geoX);
		int blockY = GeoEngine.getBlockXY(geoY);
		return _geoBlockTypes[blockX][blockY].getCellBeyond(geoX, geoY, height, instanceId);
	}
}