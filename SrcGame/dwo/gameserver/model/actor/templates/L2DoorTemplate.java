package dwo.gameserver.model.actor.templates;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.model.skills.stats.StatsSet;
import javolution.util.FastList;
import org.jdom2.Element;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Forsaiken
 */

public class L2DoorTemplate
{
	private final int _id;
	private final int[] _position;
	private final int[][] _ranges;
	private final int _geoHeight;
	private final String _groupName;
	private final String _doorName;
	private final double _hp;
	private final int _pDef;
	private final int _mDef;
	private final int _castleId;
	private final int _fortId;
	private final int _clanHallId;
	private final boolean _unlockAble;
	private final boolean _defaultOpen;
	private final boolean _commanderDoor;
	private final int _collisionRadius;
	private final int _collisionHeight;
	private final L2CharTemplate _charTemplate;
	private final int[] _geoRegion;
	private final boolean _destroyAbleByPlayer;
	private final boolean _destroyAbleBySiegeSummon;
	private final int _autoActionDelay;

	private int[][] _traced;
	private int[][] _cells;
	private int[][] _blocks;
	private L2DoorTemplate[] _templatesInSameBlocks;

	public L2DoorTemplate(Element rootElement)
	{
		String key, value;

		int objectId = -1;
		int[] position = null;
		int[][] ranges = null;
		int geoHeight = Integer.MIN_VALUE;

		String groupName = "UNDEFINED";
		String doorName = "UNDEFINED";
		double hp = 158250.0;
		int pDef = 644;
		int mDef = 518;
		int castleId = -1;
		int fortId = -1;
		int clanHallId = -1;
		boolean unlockAble = false;
		boolean defaultOpen = false;
		boolean commanderDoor = false;
		int collisionRadius = 0;
		int tempCollisionRadius;
		boolean destroyAbleByPlayer = false;
		boolean destroyAbleBySiegeSummon = false;
		int autoActionDelay = -1;

		for(Element element : rootElement.getChildren())
		{
            final String name = element.getName();
			if(name.equalsIgnoreCase("set"))
			{
				key = element.getAttributeValue("key");
				value = element.getAttributeValue("value");

				switch(key)
				{
					case "objectId":
						objectId = Integer.parseInt(value);
						break;
					case "position":
						String[] params = value.split(",");
						position = new int[params.length];
						for(int i = params.length; i-- > 0; )
						{
							position[i] = Integer.parseInt(params[i]);
						}
						break;
					case "ranges":
						String[] params1 = value.split(";");
						ranges = new int[params1.length][];
						for(int i = params1.length; i-- > 0; )
						{
							String[] params2 = params1[i].split(",");
							ranges[i] = new int[params2.length];
							for(int j = params2.length; j-- > 0; )
							{
								ranges[i][j] = Integer.parseInt(params2[j]);
							}
						}
						break;
					case "geoHeight":
						geoHeight = Integer.parseInt(value);
						break;
					case "groupName":
						groupName = value;
						break;
					case "doorName":
						doorName = value;
						break;
					case "hp":
						hp = Double.parseDouble(value);
						break;
					case "pDef":
						pDef = Integer.parseInt(value);
						break;
					case "mDef":
						mDef = Integer.parseInt(value);
						break;
					case "castleId":
						castleId = Integer.parseInt(value);
						break;
					case "fortId":
						fortId = Integer.parseInt(value);
						break;
					case "clanHallId":
						clanHallId = Integer.parseInt(value);
						break;
					case "unlockAble":
						unlockAble = Boolean.parseBoolean(value);
						break;
					case "defaultOpen":
						defaultOpen = Boolean.parseBoolean(value);
						break;
					case "commanderDoor":
						commanderDoor = Boolean.parseBoolean(value);
						break;
					case "destroyAbleByPlayer":
						destroyAbleByPlayer = Boolean.parseBoolean(value);
						break;
					case "destroyAbleBySiegeSummon":
						destroyAbleBySiegeSummon = Boolean.parseBoolean(value);
						break;
					case "autoActionDelay":
						autoActionDelay = Integer.parseInt(value);
						break;
				}
			}
		}

		_id = objectId;
		_position = position;
		_ranges = ranges;
		_geoHeight = geoHeight != Integer.MIN_VALUE ? geoHeight : _ranges[0][2];

		_groupName = groupName;
		_doorName = doorName;
		_hp = hp;
		_pDef = pDef;
		_mDef = mDef;
		_castleId = castleId;
		_fortId = fortId;
		_clanHallId = clanHallId;
		_unlockAble = unlockAble;
		_defaultOpen = defaultOpen;
		_commanderDoor = commanderDoor;
		_destroyAbleByPlayer = destroyAbleByPlayer;
		_destroyAbleBySiegeSummon = destroyAbleBySiegeSummon;
		_autoActionDelay = autoActionDelay;

		_geoRegion = new int[]{
			GeoEngine.getRegionXY(GeoEngine.getGeoX(position[0])), GeoEngine.getRegionXY(GeoEngine.getGeoY(position[1]))
		};

		int height = ranges[0][2];

		for(int i = ranges.length; i-- > 0; )
		{
			tempCollisionRadius = Math.abs(position[0] - ranges[i][0]);
			if(tempCollisionRadius > collisionRadius)
			{
				collisionRadius = tempCollisionRadius;
			}

			tempCollisionRadius = Math.abs(position[1] - ranges[i][1]);
			if(tempCollisionRadius > collisionRadius)
			{
				collisionRadius = tempCollisionRadius;
			}

			// Bug fixing for async coords
			if(ranges[i][2] != height)
			{
				ranges[i][2] = height;
			}
		}

		_collisionRadius = collisionRadius;
		_collisionHeight = 50;

		StatsSet npcDat = new StatsSet();
		npcDat.set("npcId", _id);
		npcDat.set("level", 0);
		npcDat.set("jClass", "door");

		npcDat.set("str", 0);
		npcDat.set("con", 0);
		npcDat.set("dex", 0);
		npcDat.set("int", 0);
		npcDat.set("wit", 0);
		npcDat.set("men", 0);

		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseAccCombat", 38);
		npcDat.set("baseEvasRate", 38);
		npcDat.set("base_critical", 38);

		npcDat.set("collision_radius", _collisionRadius);
		npcDat.set("collision_height", _collisionHeight);
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("base_attack_range", 0);
		npcDat.set("org_mp", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("exp", 0);
		npcDat.set("sp", 0);
		npcDat.set("base_physical_attack", 0);
		npcDat.set("base_magic_attack", 0);
		npcDat.set("base_attack_speed", 0);
		npcDat.set("agro_range", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("slot_rhand", 0);
		npcDat.set("slot_lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("ground_high", 0);
		npcDat.set("ground_low", 0);
		npcDat.set("name", _doorName);
		npcDat.set("org_hp", (int) _hp);
		npcDat.set("org_hp_regen", 3.0e-3f);
		npcDat.set("org_mp_regen", 3.0e-3f);
		npcDat.set("base_defend", _pDef);
		npcDat.set("base_magic_defend", _mDef);

		_charTemplate = new L2CharTemplate(npcDat);
	}

	private static int[][][] generateNoneIntersectLines(int[][] points)
	{
		FastList<int[][]> connections = new FastList<>();

		int i;
		int j;
		int k;
		int[][] connection;
		int[][] temp;

		for(i = points.length; i-- > 0; )
		{
			INNER:
			for(j = points.length; j-- > 0; )
			{
				if(j == i)
				{
					continue;
				}

				connection = new int[][]{points[i], points[j]};
				for(k = connections.size(); k-- > 0; )
				{
					temp = connections.get(k);
					if(temp[0][0] == connection[1][0] && temp[0][1] == connection[1][1])
					{
						if(temp[1][0] == connection[0][0] && temp[1][1] == connection[0][1])
						{
							continue INNER;
						}
					}
					else if(temp[1][0] == connection[0][0] && temp[1][1] == connection[0][1])
					{
						if(temp[0][0] == connection[1][0] && temp[0][1] == connection[1][1])
						{
							continue INNER;
						}
					}
				}
				connections.addLast(connection);
			}
		}

		if(points.length != 4)
		{
			return connections.toArray(new int[connections.size()][][]);
		}

		int[][] temp2;

		OUTER:
		for(i = connections.size(); i-- > 0; )
		{
			temp = connections.get(i);

			for(j = connections.size(); j-- > 0; )
			{
				if(j == i)
				{
					continue;
				}

				temp2 = connections.get(j);

				if(Line2D.linesIntersect(temp[0][0], temp[0][1], temp[1][0], temp[1][1], temp2[0][0], temp2[0][1], temp2[1][0], temp2[1][1]))
				{
					if(nodeEqual(temp[0], temp2[0]) ||
						nodeEqual(temp[0], temp2[1]) ||
						nodeEqual(temp[1], temp2[0]) ||
						nodeEqual(temp[1], temp2[1]))
					{
						continue;
					}

					if(i > j)
					{
						connections.remove(i);
						connections.remove(j);
					}
					else
					{
						connections.remove(j);
						connections.remove(i);
					}
					break OUTER;
				}
			}
		}

		return connections.toArray(new int[connections.size()][][]);
	}

	private static boolean nodeEqual(int[] n1, int[] n2)
	{
		return n1[0] == n2[0] && n1[1] == n2[1];
	}

	public int getId()
	{
		return _id;
	}

	public String getGroupName()
	{
		return _groupName;
	}

	public String getDoorName()
	{
		return _doorName;
	}

	public double getMaxHp()
	{
		return _hp;
	}

	public int getPdef()
	{
		return _pDef;
	}

	public int getMdef()
	{
		return _mDef;
	}

	public int getCastleId()
	{
		return _castleId;
	}

	public int getFortId()
	{
		return _fortId;
	}

	public int getClanHallId()
	{
		return _clanHallId;
	}

	public boolean isUnlockAble()
	{
		return _unlockAble;
	}

	public boolean isDefaultOpen()
	{
		return _defaultOpen;
	}

	public boolean isCommanderDoor()
	{
		return _commanderDoor;
	}

	public boolean isDestroyAbleByPlayer()
	{
		return _destroyAbleByPlayer;
	}

	public boolean isDestroyAbleBySiegeSummon()
	{
		return _destroyAbleBySiegeSummon;
	}

	public int getCollisionRadius()
	{
		return _collisionRadius;
	}

	public int getCollisionHeight()
	{
		return _collisionHeight;
	}

	public L2CharTemplate getCharTemplate()
	{
		return _charTemplate;
	}

	public L2DoorTemplate[] getDoorTemplatesInSameBlocks()
	{
		return _templatesInSameBlocks;
	}

	public int[] getGeoRegion()
	{
		return _geoRegion;
	}

	public int getAutoActionDelay()
	{
		return _autoActionDelay;
	}

	public void computeDoorTemplatesInSameBlocks(DoorGeoEngine engine)
	{
		List<L2DoorTemplate> list = new ArrayList<>();
		list.add(this);
		L2DoorTemplate[] templates = engine.getAllDoorTemplates();
		for(L2DoorTemplate template : templates)
		{
			if(!template.equals(this) && isInSameBlock(template))
			{
				list.add(template);
			}
		}
		_templatesInSameBlocks = list.toArray(new L2DoorTemplate[list.size()]);
	}

	public boolean isInSameBlock(L2DoorTemplate template)
	{
		for(int[] tBlock : template._blocks)
		{
			if(isInSameBlock(tBlock[0], tBlock[1]))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isInSameBlock(int tGeoX, int tGeoY)
	{
		int geoX;
		int geoY;
		int regionX;
		int regionY;
		int tRegionX = GeoEngine.getRegionXY(tGeoX);
		int tRegionY = GeoEngine.getRegionXY(tGeoY);
		int blockX;
		int blockY;
		int tBlockX = GeoEngine.getBlockXY(tGeoX);
		int tBlockY = GeoEngine.getBlockXY(tGeoY);

		if(_blocks == null)
		{
			return false;
		}

		try
		{
			for(int[] block : _blocks)
			{
				geoX = block[0];
				geoY = block[1];
				regionX = GeoEngine.getRegionXY(geoX);
				regionY = GeoEngine.getRegionXY(geoY);
				blockX = GeoEngine.getBlockXY(geoX);
				blockY = GeoEngine.getBlockXY(geoY);

				if(regionX == tRegionX && regionY == tRegionY && blockX == tBlockX && blockY == tBlockY)
				{
					return true;
				}
			}
		}
		catch(NullPointerException e)
		{
			// Ignore
		}
		return false;
	}

	public void computeCells()
	{
		List<int[]> cells = new ArrayList<>();
		List<int[]> blocks = new ArrayList<>();

		int[][] traced;
		int[] temp;
		boolean found;
		int blockX;
		int blockY;

		int[][][] lines = generateNoneIntersectLines(_ranges);
		for(int[][] line : lines)
		{
			traced = GeoEngine.getInstance().tracertCells(line[0][0], line[0][1], _geoHeight, line[1][0], line[1][1], false);
			if(traced == null)
			{
				traced = new int[0][0];
			}

			for(int k = traced.length, l; k-- > 0; )
			{
				found = false;
				for(l = cells.size(); l-- > 0; )
				{
					temp = cells.get(l);
					if(temp[0] == traced[k][0] && temp[1] == traced[k][1])
					{
						found = true;
						break;
					}
				}
				if(!found)
				{
					cells.add(traced[k]);
				}
			}
		}

		_traced = cells.toArray(new int[cells.size()][]);
		_cells = computeTracedCells(cells);

		for(int[] cell : _cells)
		{
			blockX = GeoEngine.getBlockXY(cell[0]);
			blockY = GeoEngine.getBlockXY(cell[1]);

			found = false;
			for(int i = blocks.size(); i-- > 0; )
			{
				temp = blocks.get(i);
				if(GeoEngine.getBlockXY(temp[0]) == blockX && GeoEngine.getBlockXY(temp[1]) == blockY)
				{
					found = true;
					break;
				}
			}
			if(!found)
			{
				blocks.add(new int[]{
					cell[0], cell[1]
				});
			}
		}

		_blocks = blocks.toArray(new int[blocks.size()][]);
	}

	private int[][] computeTracedCells(List<int[]> traced)
	{
		List<int[]> cells = new ArrayList<>();

		for(int i = traced.size(); i-- > 0; )
		{
			checkedAdd(traced.get(i), cells, traced, false, GeoEngine.NSWE_ALL);
		}

		return cells.toArray(new int[cells.size()][]);
	}

	private void checkedAdd(int[] cell, List<int[]> cells, List<int[]> traced, boolean neigbour, byte NSWE)
	{
		if(neigbour)
		{
			int[] temp;
			for(int i = traced.size(); i-- > 0; )
			{
				temp = traced.get(i);
				if(temp[0] == cell[0] && temp[1] == cell[1])
				{
					return;
				}
			}

			for(int i = cells.size(); i-- > 0; )
			{
				temp = cells.get(i);
				if(temp[0] == cell[0] && temp[1] == cell[1])
				{
					temp[2] &= GeoEngine.HEIGHT_MASK | NSWE;
					return;
				}
			}

			short checkedHeightAndNSWE = GeoEngine.getInstance().nGetHeightAndNSWE(cell[0], cell[1], _geoHeight);
			short checkedHeightZeroNSWE = (short) (checkedHeightAndNSWE & GeoEngine.HEIGHT_MASK);
			short height = GeoEngine.getHeight(checkedHeightAndNSWE);
			int diff = Math.abs(_geoHeight - height);

			boolean check = diff < 150;
			if(!check)
			{
				switch(GeoEngine.getInstance().nGetLayerCount(cell[0], cell[1]))
				{
					case 0:
						return;

					case 1:
						check = diff < 300;
						break;

					default:
						if(diff < 300)
						{
							//final short checkedUpperHeightAndNSWE = GeoEngine.getInstance().nGetUpperHeightAndNSWE(cell[0], cell[1], height + 1);
							//final short upperHeight = GeoEngine.getHeight(checkedUpperHeightAndNSWE);

							short checkedLowerHeightAndNSWE = GeoEngine.getInstance().nGetLowerHeightAndNSWE(cell[0], cell[1], height);
							short lowerHeight = GeoEngine.getHeight(checkedLowerHeightAndNSWE);

							//System.out.println("Blub: " + cell[0] + ", " + cell[1] + ", " + lowerHeight + ", " + height + ", " + upperHeight);

							// ground level
							if(height == lowerHeight)
							{
								check = true;
							}
						}
						break;
				}
			}

			if(check)
			{
				cells.add(new int[]{cell[0], cell[1], checkedHeightZeroNSWE | NSWE});
			}
		}
		else
		{
			checkedAdd(new int[]{cell[0] + 1, cell[1], cell[2]}, cells, traced, true, GeoEngine.NWEST);
			checkedAdd(new int[]{cell[0] - 1, cell[1], cell[2]}, cells, traced, true, GeoEngine.NEAST);
			checkedAdd(new int[]{cell[0], cell[1] + 1, cell[2]}, cells, traced, true, GeoEngine.NNORTH);
			checkedAdd(new int[]{cell[0], cell[1] - 1, cell[2]}, cells, traced, true, GeoEngine.NSOUTH);
		}
	}

	public int[] getPosition()
	{
		return _position;
	}

	public int[][] getTraced()
	{
		return _traced;
	}

	public int[][] getCells()
	{
		return _cells;
	}

	public int[][] getBlocks()
	{
		return _blocks;
	}

	public int[][] getRanges()
	{
		return _ranges;
	}
}