package dwo.gameserver.engine.geodataengine;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.engine.geodataengine.driver.GeoDriver;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2ArtefactInstance;
import dwo.gameserver.model.actor.instance.L2DefenderInstance;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.geometry.Point3D;
import org.inc.incolution.util.list.IncArrayList;

/**
 * @author Forsaiken
 */

public class ComplexGeoEngine extends GeoEngine
{
	private final GeoDriver _driver;

	public ComplexGeoEngine(GeoDriver driver)
	{
		_driver = driver;
	}

	@Override
	public void reloadGeo()
	{
		_driver.reloadGeo();
	}

	@Override
	public void reloadGeo(byte regionX, byte regionY)
	{
		_driver.reloadGeo(regionX, regionY);
	}

	@Override
	public boolean hasGeo(int x, int y)
	{
		int geoX = GeoEngine.getGeoX(x);
		int geoY = GeoEngine.getGeoY(y);
		return _driver.nHasGeo(geoX, geoY);
	}

	@Override
	public byte getType(int x, int y)
	{
		int geoX = GeoEngine.getGeoX(x);
		int geoY = GeoEngine.getGeoY(y);
		return _driver.nGetType(geoX, geoY);
	}

	@Override
	public short getHeight(int x, int y, int z)
	{
		int geoX = GeoEngine.getGeoX(x);
		int geoY = GeoEngine.getGeoY(y);
		return GeoEngine.getHeight(nGetHeightAndNSWE(geoX, geoY, z));
	}

	@Override
	public short getSpawnHeight(int x, int y, int minZ, int maxZ)
	{
		short height = getHeight(x, y, minZ);

		return height > maxZ + 200 || height < minZ - 200 ? (short) minZ : height;
	}

	@Override
	public boolean canSeeTarget(L2Object cha, L2Object target)
	{
		if(cha == null || target == null)
		{
			return false;
		}

		// To be able to see over fences and give the player the viewpoint
		// game client has, all coordinates are lifted 45 from ground.
		// Because of layer selection in LOS algorithm (it selects -45 there
		// and some layers can be very close...) do not change this without
		// changing the LOS code.
		//((L2Character)cha).getTemplate().getCollisionHeight() НЕЛЬЗЯ ИСПОЛЬЗОВАТЬ ТАК КАК У КАЖДОЙ РАСЫ ОН СВОЙ!!!!!!!!
		int z = cha.getZ();

		// L2Defender only: well they don't move closer to balcony fence at the moment :(
		if(cha.is(L2DefenderInstance.class))
		{
			z += 30;
		}

		if(target instanceof L2ArtefactInstance)
		{
			z += 32;
		}

		int z2 = target.getZ();

		// TODO FIX THIS THIS HERE!
		if(target.isDoor())
		{
			return true; // door coordinates are hinge coords..
		}

		// L2Defender only: well they don't move closer to balcony fence at the moment :(
		if(cha.is(L2DefenderInstance.class))
		{
			z2 += 30;
		}

		return canSeeTarget(cha.getX(), cha.getY(), z, target.getX(), target.getY(), z2, cha.getInstanceId());
	}

	@Override
	public boolean canSeeTarget(L2Object cha, Point3D target)
	{
		return canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ(), cha.getInstanceId());
	}

	@Override
	public boolean canSeeTarget(int x1, int y1, int z1, int x2, int y2, int z2, int instanceId)
	{
		int geoX1 = GeoEngine.getGeoX(x1);
		int geoY1 = GeoEngine.getGeoY(y1);
		int geoX2 = GeoEngine.getGeoX(x2);
		int geoY2 = GeoEngine.getGeoY(y2);
		return nCanSeeTarget(geoX1, geoY1, z1, geoX2, geoY2, z2, instanceId);
	}

	@Override
	public Location moveCheck(int x1, int y1, int z1, int x2, int y2, int z2, int instanceId)
	{
		int geoX1 = GeoEngine.getGeoX(x1);
		int geoY1 = GeoEngine.getGeoY(y1);
		int geoX2 = GeoEngine.getGeoX(x2);
		int geoY2 = GeoEngine.getGeoY(y2);

		return nMoveCheck(new Location(x2, y2, z2), geoX1, geoY1, z1, geoX2, geoY2, z2, instanceId);
	}

	@Override
	public boolean canMoveFromToTarget(int x1, int y1, int z1, int x2, int y2, int z2, int instanceId)
	{
		Location destiny = moveCheck(x1, y1, z1, x2, y2, z2, instanceId);
		return destiny.getX() == x2 && destiny.getY() == y2 && destiny.getZ() == z2;
	}

	@Override
	public short[][][] getBlock(int blockX, int blockY)
	{
		return _driver.getBlock(blockX, blockY);
	}

	@Override
	public int[][] tracertCells(int x1, int y1, int z1, int x2, int y2, boolean checkBlocking)
	{
		IncArrayList<int[]> trace = new IncArrayList<>();

		int geoX1 = GeoEngine.getGeoX(x1);
		int geoY1 = GeoEngine.getGeoY(y1);

		int geoX2 = GeoEngine.getGeoX(x2);
		int geoY2 = GeoEngine.getGeoY(y2);

		int dx = geoX2 - geoX1;
		int dy = geoY2 - geoY1;

		// Increment in Z coordinate when moving along X or Y axis
		// and not straight to the target. This is done because
		// calculation moves either in X or Y direction.
		int inc_x;
		int inc_y;

		if(dx < 0)
		{
			inc_x = -1;
			dx = -dx;
		}
		else
		{
			inc_x = 1;
		}

		if(dy < 0)
		{
			inc_y = -1;
			dy = -dy;
		}
		else
		{
			inc_y = 1;
		}

		// next_* are used in NcanMoveNext check from x,y
		int next_x = geoX1;
		int next_y = geoY1;
		short heightAndNSWE;
		int tempz = z1;

		// creates path to the target, using only x or y direction
		// calculation stops when next_* == target
		if(dx >= dy)
		{
			int delta_A = 2 * dy;
			int delta_B = delta_A - 2 * dx;
			int d = delta_A - dx;

			while(dx-- > 0)
			{
				geoX1 = next_x;
				geoY1 = next_y;

				if(d > 0)
				{
					if(checkBlocking)
					{
						next_x += inc_x;
						tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, Config.GEODATA_HEIGHT_DIFF_MOVE_CHECKS, 0);
						if(tempz == Integer.MIN_VALUE)
						{
							return trace.toArray(new int[trace.size()][]);
						}
						trace.add(new int[]{geoX1, geoY1, nGetHeightAndNSWE(geoX1, geoY1, z1)});

						z1 = tempz;

						next_y += inc_y;
						tempz = nCanMoveNext(next_x, geoY1, z1, next_x, next_y, Config.GEODATA_HEIGHT_DIFF_MOVE_CHECKS, 0);
						if(tempz == Integer.MIN_VALUE)
						{
							return trace.toArray(new int[trace.size()][]);
						}
						trace.add(new int[]{next_x, geoY1, nGetHeightAndNSWE(next_x, geoY1, z1)});

						z1 = tempz;
						d += delta_B;
					}
					else
					{
						next_x += inc_x;
						heightAndNSWE = _driver.nGetHeightAndNSWE(geoX1, geoY1, z1);
						trace.add(new int[]{geoX1, geoY1, heightAndNSWE});

						next_y += inc_y;
						heightAndNSWE = _driver.nGetHeightAndNSWE(next_x, geoY1, z1);
						trace.add(new int[]{next_x, geoY1, heightAndNSWE});

						d += delta_B;
					}
				}
				else
				{
					if(checkBlocking)
					{
						next_x += inc_x;
						tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, Config.GEODATA_HEIGHT_DIFF_MOVE_CHECKS, 0);
						if(tempz == Integer.MIN_VALUE)
						{
							return trace.toArray(new int[trace.size()][]);
						}
						trace.add(new int[]{geoX1, geoY1, nGetHeightAndNSWE(geoX1, geoY1, z1)});

						z1 = tempz;
						d += delta_A;
					}
					else
					{
						next_x += inc_x;
						heightAndNSWE = _driver.nGetHeightAndNSWE(geoX1, geoY1, z1);
						trace.add(new int[]{geoX1, geoY1, heightAndNSWE});

						d += delta_A;
					}
				}
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int delta_B = delta_A - 2 * dy;
			int d = delta_A - dy;

			while(dy-- > 0)
			{
				geoX1 = next_x;
				geoY1 = next_y;

				if(d > 0)
				{
					if(checkBlocking)
					{
						next_y += inc_y;
						tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, Config.GEODATA_HEIGHT_DIFF_MOVE_CHECKS, 0);
						if(tempz == Integer.MIN_VALUE)
						{
							return trace.toArray(new int[trace.size()][]);
						}
						trace.add(new int[]{geoX1, geoY1, nGetHeightAndNSWE(geoX1, geoY1, z1)});

						z1 = tempz;

						next_x += inc_x;
						tempz = nCanMoveNext(geoX1, next_y, z1, next_x, next_y, Config.GEODATA_HEIGHT_DIFF_MOVE_CHECKS, 0);
						if(tempz == Integer.MIN_VALUE)
						{
							return trace.toArray(new int[trace.size()][]);
						}
						trace.add(new int[]{geoX1, next_y, nGetHeightAndNSWE(geoX1, next_y, z1)});

						z1 = tempz;
						d += delta_B;
					}
					else
					{
						next_y += inc_y;
						heightAndNSWE = _driver.nGetHeightAndNSWE(geoX1, geoY1, z1);
						trace.add(new int[]{geoX1, geoY1, heightAndNSWE});

						next_x += inc_x;
						heightAndNSWE = _driver.nGetHeightAndNSWE(geoX1, next_y, z1);
						trace.add(new int[]{geoX1, next_y, heightAndNSWE});

						d += delta_B;
					}
				}
				else
				{
					if(checkBlocking)
					{
						next_y += inc_y;
						tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, Config.GEODATA_HEIGHT_DIFF_MOVE_CHECKS, 0);
						if(tempz == Integer.MIN_VALUE)
						{
							return trace.toArray(new int[trace.size()][]);
						}
						trace.add(new int[]{geoX1, geoY1, nGetHeightAndNSWE(geoX1, geoY1, z1)});

						z1 = tempz;
						d += delta_A;
					}
					else
					{
						next_y += inc_y;
						heightAndNSWE = _driver.nGetHeightAndNSWE(geoX1, geoY1, z1);
						trace.add(new int[]{geoX1, geoY1, heightAndNSWE});

						d += delta_A;
					}
				}
			}
		}

		return trace.toArray(new int[trace.size()][]);
	}

	@Override
	public byte nGetType(int geoX, int geoY)
	{
		return _driver.nGetType(geoX, geoY);
	}

	@Override
	public int nGetLayerCount(int geoX, int geoY)
	{
		return _driver.nGetLayerCount(geoX, geoY);
	}

	@Override
	public short nGetHeightAndNSWE(int geoX, int geoY, int z)
	{
		return _driver.nGetHeightAndNSWE(geoX, geoY, z);
	}

	@Override
	public short nGetLowerHeightAndNSWE(int geoX, int geoY, int z)
	{
		return _driver.nGetLowerHeightAndNSWE(geoX, geoY, z);
	}

	@Override
	public boolean nCanMoveFromToTargetNoChecks(int geoX1, int geoY1, int z1, int geoX2, int geoY2, int z2, int instanceId)
	{
		int dx = geoX2 - geoX1;
		int dy = geoY2 - geoY1;

		// Increment in Z coordinate when moving along X or Y axis
		// and not straight to the target. This is done because
		// calculation moves either in X or Y direction.
		int inc_x;
		int inc_y;

		if(dx < 0)
		{
			inc_x = -1;
			dx = -dx;
		}
		else
		{
			inc_x = 1;
		}

		if(dy < 0)
		{
			inc_y = -1;
			dy = -dy;
		}
		else
		{
			inc_y = 1;
		}

		// next_* are used in NcanMoveNext check from x,y
		int next_x = geoX1;
		int next_y = geoY1;

		int tempz = z1;

		// creates path to the target, using only x or y direction
		// calculation stops when next_* == target
		if(dx >= dy)
		{
			int delta_A = 2 * dy;
			int delta_B = delta_A - 2 * dx;
			int d = delta_A - dx;

			if(Config.GEODATA_ALT_MOVE_CHECKS)
			{
				while(dx-- > 0)
				{
					geoX1 = next_x;
					geoY1 = next_y;

					if(d > 0)
					{
						tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y + inc_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						tempz = nCanMoveNext(geoX1, geoY1 + inc_y, tempz, next_x + inc_x, next_y + inc_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						next_x += inc_x;
						tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						z1 = tempz;

						next_y += inc_y;
						tempz = nCanMoveNext(next_x, geoY1, z1, next_x, next_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						z1 = tempz;
						d += delta_B;
					}
					else
					{
						next_x += inc_x;
						tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						z1 = tempz;
						d += delta_A;
					}
				}
			}
			else
			{
				while(dx-- > 0)
				{
					geoX1 = next_x;
					geoY1 = next_y;

					if(d > 0)
					{
						next_x += inc_x;
						tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						z1 = tempz;

						next_y += inc_y;
						tempz = nCanMoveNext(next_x, geoY1, z1, next_x, next_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						z1 = tempz;
						d += delta_B;
					}
					else
					{
						next_x += inc_x;
						tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						z1 = tempz;
						d += delta_A;
					}
				}
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int delta_B = delta_A - 2 * dy;
			int d = delta_A - dy;

			if(Config.GEODATA_ALT_MOVE_CHECKS)
			{
				while(dy-- > 0)
				{
					geoX1 = next_x;
					geoY1 = next_y;

					if(d > 0)
					{
						tempz = nCanMoveNext(geoX1, geoY1, z1, next_x + inc_x, next_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						tempz = nCanMoveNext(geoX1 + inc_x, geoY1, tempz, next_x + inc_x, next_y + inc_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						next_y += inc_y;
						tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						z1 = tempz;

						next_x += inc_x;
						tempz = nCanMoveNext(geoX1, next_y, z1, next_x, next_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						z1 = tempz;
						d += delta_B;
					}
					else
					{
						next_y += inc_y;
						tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						z1 = tempz;
						d += delta_A;
					}
				}
			}
			else
			{
				while(dy-- > 0)
				{
					geoX1 = next_x;
					geoY1 = next_y;

					if(d > 0)
					{
						next_y += inc_y;
						tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						z1 = tempz;

						next_x += inc_x;
						tempz = nCanMoveNext(geoX1, next_y, z1, next_x, next_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						z1 = tempz;
						d += delta_B;
					}
					else
					{
						next_y += inc_y;
						tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, true, instanceId);
						if(tempz == Integer.MIN_VALUE)
						{
							return false;
						}

						z1 = tempz;
						d += delta_A;
					}
				}
			}
		}

		return true;
	}

	private boolean nLOS(short[] store1, short[] store2, int geoX1, int geoY1, int z1, int geoX2, int geoY2, int z2, int instanceId)
	{
		short lowerHeightAndNSWE = store1[1];

		// check if we already hit the the floor beyond
		if(z1 < GeoEngine.getHeight(lowerHeightAndNSWE))
		{
			return false;
		}

		// check if z2 would be above or on the upper layer
		if(z2 >= GeoEngine.getHeight(store1[0])) // upper
		{
			return false;
		}

		_driver.nGetUpperAndLowerHeightAndNSWE(geoX2, geoY2, z2, store2);

		if(!GeoEngine.checkNSWE(lowerHeightAndNSWE, geoX1, geoY1, geoX2, geoY2) || !GeoEngine.checkNSWE(DoorGeoEngine.getInstance().getCellBeyond(geoX1, geoY1, z1, instanceId), geoX1, geoY1, geoX2, geoY2))
		{
			short upperheightAndNSWE = store2[0];
			if(upperheightAndNSWE == Short.MAX_VALUE)
			{
				// we have no upper layer so check if we are beyond the lower
				if(z2 < GeoEngine.getHeight(store2[1])) // lower
				{
					return false;
				}
			}
			else
			{
				// we have an upper layer so check if we are beyond it
				if(z2 < GeoEngine.getHeight(upperheightAndNSWE))
				{
					return false;
				}
			}
		}
		return true;
	}

	public boolean nCanSeeTarget(int geoX1, int geoY1, int z1, int geoX2, int geoY2, int z2, int instanceId)
	{
		while(true)
		{
			if(z1 > z2)
			{
				geoX1 = geoX2;
				geoY1 = geoY2;
				z1 = z2;
				geoX2 = geoX1;
				geoY2 = geoY1;
				z2 = z1;
				continue;
			}

			// TODO Временное решение ( служит для поправки координаты z чтобы в гео находился слой )
			z1 += 50;
			z2 += 50;

			int dx = geoX2 - geoX1;
			int dy = geoY2 - geoY1;
			int dz = z2 - z1;
			double distanceSq = (double) dx * dx + (double) dy * dy;
			if(distanceSq > 90000.0D) // (300*300) 300*16 = 4800 in world coord
			{
				// Avoid too long check
				return false;
			}

			if(!_driver.nHasGeo(geoX1, geoY1) && !_driver.nHasGeo(geoX2, geoY2))
			{
				return true;
			}

			short[] store1 = new short[2];
			_driver.nGetUpperAndLowerHeightAndNSWE(geoX1, geoY1, z1, store1);

			// standing directly beyond/above each other? check for layer between them
			if(dx == 0 && dy == 0)
			{
				short upper = GeoEngine.getHeight(store1[0]);
				short lower = GeoEngine.getHeight(store1[1]);
				if(z1 < lower && z2 >= lower || z1 < upper && z2 >= upper)
				{
					return false;
				}
			}

			// Increment in Z coordinate when moving along X or Y axis
			// and not straight to the target. This is done because
			// calculation moves either in X or Y direction.
			int inc_x;
			int inc_y;

			if(dx < 0)
			{
				inc_x = -1;
				dx = -dx;
			}
			else
			{
				inc_x = 1;
			}

			if(dy < 0)
			{
				inc_y = -1;
				dy = -dy;
			}
			else
			{
				inc_y = 1;
			}

			int delta_A;
			int delta_B;
			int d;

			double inc_z_x = dz * dx / distanceSq;
			double inc_z_y = dz * dy / distanceSq;
			int cur_x;
			int next_x = geoX1;
			int cur_y;
			int next_y = geoY1;
			double cur_z;
			double next_z = z1;
			short[] store2 = new short[2];
			short[] swap;

			if(dx >= dy)
			{
				delta_A = 2 * dy;
				delta_B = delta_A - 2 * dx;
				d = delta_A - dx;

				while(dx-- > 0)
				{
					if(d > 0)
					{
						cur_x = next_x;
						cur_y = next_y;
						cur_z = next_z;
						next_x = cur_x + inc_x;
						next_z = cur_z + inc_z_x;

						if(!nLOS(store1, store2, cur_x, cur_y, (int) cur_z, next_x, next_y, (int) next_z, instanceId))
						{
							return false;
						}

						swap = store1;
						store1 = store2;
						store2 = swap;

						d += delta_B;

						cur_x = next_x;
						cur_y = next_y;
						cur_z = next_z;
						next_y = cur_y + inc_y;
						next_z = cur_z + inc_z_y;

						if(!nLOS(store1, store2, cur_x, cur_y, (int) cur_z, next_x, next_y, (int) next_z, instanceId))
						{
							return false;
						}

						swap = store1;
						store1 = store2;
						store2 = swap;
					}
					else
					{
						cur_x = next_x;
						cur_y = next_y;
						cur_z = next_z;
						next_x = cur_x + inc_x;
						next_z = cur_z + inc_z_x;

						if(!nLOS(store1, store2, cur_x, cur_y, (int) cur_z, next_x, next_y, (int) next_z, instanceId))
						{
							return false;
						}

						swap = store1;
						store1 = store2;
						store2 = swap;

						d += delta_A;
					}
				}
			}
			else
			{
				delta_A = 2 * dx;
				delta_B = delta_A - 2 * dy;
				d = delta_A - dy;

				while(dy-- > 0)
				{
					if(d > 0)
					{
						cur_x = next_x;
						cur_y = next_y;
						cur_z = next_z;
						next_y = cur_y + inc_y;
						next_z = cur_z + inc_z_y;

						if(!nLOS(store1, store2, cur_x, cur_y, (int) cur_z, next_x, next_y, (int) next_z, instanceId))
						{
							return false;
						}

						swap = store1;
						store1 = store2;
						store2 = swap;

						d += delta_B;

						cur_x = next_x;
						cur_y = next_y;
						cur_z = next_z;
						next_x = cur_x + inc_x;
						next_z = cur_z + inc_z_x;

						if(!nLOS(store1, store2, cur_x, cur_y, (int) cur_z, next_x, next_y, (int) next_z, instanceId))
						{
							return false;
						}

						swap = store1;
						store1 = store2;
						store2 = swap;
					}
					else
					{
						cur_x = next_x;
						cur_y = next_y;
						cur_z = next_z;
						next_y = cur_y + inc_y;
						next_z = cur_z + inc_z_y;

						if(!nLOS(store1, store2, cur_x, cur_y, (int) cur_z, next_x, next_y, (int) next_z, instanceId))
						{
							return false;
						}

						swap = store1;
						store1 = store2;
						store2 = swap;

						d += delta_A;
					}
				}
			}
			return true;
		}
	}

	private Location nMoveCheck(Location destiny, int geoX1, int geoY1, int z1, int geoX2, int geoY2, int z2, int instanceId)
	{
		int dx = geoX2 - geoX1;
		int dy = geoY2 - geoY1;

		double distance = (double) dx * dx + (double) dy * dy;

		if(distance == 0)
		{
			return destiny;
		}

		if(distance > 36100) // 190*190*16 = 3040 world coord
		{
			// Avoid too long check
			// Currently we calculate a middle point
			// for wyvern users and otherwise for comfort
			double divider = Math.sqrt(30000.0D / distance);

			geoX2 = geoX1 + (int) (divider * dx);
			geoY2 = geoY1 + (int) (divider * dy);
			z2 = z1 + (int) (divider * (z2 - z1));

			dx = geoX2 - geoX1;
			dy = geoY2 - geoY1;
		}

		// Increment in Z coordinate when moving along X or Y axis
		// and not straight to the target. This is done because
		// calculation moves either in X or Y direction.
		int inc_x;
		int inc_y;

		if(dx < 0)
		{
			inc_x = -1;
			dx = -dx;
		}
		else
		{
			inc_x = 1;
		}

		if(dy < 0)
		{
			inc_y = -1;
			dy = -dy;
		}
		else
		{
			inc_y = 1;
		}

		// next_* are used in NcanMoveNext check from x,y
		int next_x = geoX1;
		int next_y = geoY1;
		int tempz = z1;

		// creates path to the target, using only x or y direction
		// calculation stops when next_* == target
		if(dx >= dy)
		{
			int delta_A = 2 * dy;
			int delta_B = delta_A - 2 * dx;
			int d = delta_A - dx;

			while(dx-- > 0)
			{
				geoX1 = next_x;
				geoY1 = next_y;

				if(d > 0)
				{
					next_x += inc_x;
					tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, instanceId);
					if(tempz == Integer.MIN_VALUE)
					{
						return new Location(GeoEngine.getWorldX(geoX1), GeoEngine.getWorldY(geoY1), z1);
					}

					z1 = tempz;

					next_y += inc_y;
					tempz = nCanMoveNext(next_x, geoY1, z1, next_x, next_y, instanceId);
					if(tempz == Integer.MIN_VALUE)
					{
						return new Location(GeoEngine.getWorldX(geoX1), GeoEngine.getWorldY(geoY1), z1);
					}

					z1 = tempz;
					d += delta_B;
				}
				else
				{
					next_x += inc_x;
					tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, instanceId);
					if(tempz == Integer.MIN_VALUE)
					{
						return new Location(GeoEngine.getWorldX(geoX1), GeoEngine.getWorldY(geoY1), z1);
					}

					z1 = tempz;
					d += delta_A;
				}
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int delta_B = delta_A - 2 * dy;
			int d = delta_A - dy;

			while(dy-- > 0)
			{
				geoX1 = next_x;
				geoY1 = next_y;

				if(d > 0)
				{
					next_y += inc_y;
					tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, instanceId);
					if(tempz == Integer.MIN_VALUE)
					{
						return new Location(GeoEngine.getWorldX(geoX1), GeoEngine.getWorldY(geoY1), z1);
					}

					z1 = tempz;

					next_x += inc_x;
					tempz = nCanMoveNext(geoX1, next_y, z1, next_x, next_y, instanceId);
					if(tempz == Integer.MIN_VALUE)
					{
						return new Location(GeoEngine.getWorldX(geoX1), GeoEngine.getWorldY(geoY1), z1);
					}

					z1 = tempz;
					d += delta_B;
				}
				else
				{
					next_y += inc_y;
					tempz = nCanMoveNext(geoX1, geoY1, z1, next_x, next_y, instanceId);
					if(tempz == Integer.MIN_VALUE)
					{
						return new Location(GeoEngine.getWorldX(geoX1), GeoEngine.getWorldY(geoY1), z1);
					}

					z1 = tempz;
					d += delta_A;
				}
			}
		}

		destiny.setZ(z1);
		return destiny;
	}

	private int nCanMoveNext(int geoX1, int geoY1, int z1, int geoX2, int geoY2, int instanceId)
	{
		return nCanMoveNext(geoX1, geoY1, z1, geoX2, geoY2, Config.GEODATA_HEIGHT_DIFF_MOVE_CHECKS, instanceId);
	}

	private int nCanMoveNext(int geoX1, int geoY1, int z1, int geoX2, int geoY2, boolean checkHeight, int instanceId)
	{
		short heightAndNSWE = GeoEngine.getInstance().nGetHeightAndNSWE(geoX1, geoY1, z1);

		if(GeoEngine.checkNSWE(heightAndNSWE, geoX1, geoY1, geoX2, geoY2))
		{
			if(GeoEngine.checkNSWE(DoorGeoEngine.getInstance().getCell(geoX1, geoY1, z1, instanceId), geoX1, geoY1, geoX2, geoY2))
			{
				if(checkHeight && _driver.nHasGeo(geoX1, geoY1) && _driver.nHasGeo(geoX2, geoY2))
				{
					short height = GeoEngine.getHeight(heightAndNSWE);
					short heightAndNSWE2 = GeoEngine.getInstance().nGetHeightAndNSWE(geoX2, geoY2, height);
					if(GeoEngine.getHeight(heightAndNSWE2) - height > 32)
					{
						return Integer.MIN_VALUE;
					}
				}

				return GeoEngine.getHeight(heightAndNSWE);
			}
			else
			{
				return Integer.MIN_VALUE;
			}
		}
		else
		{
			return Integer.MIN_VALUE;
		}
	}
}