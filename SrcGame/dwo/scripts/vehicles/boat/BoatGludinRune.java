package dwo.scripts.vehicles.boat;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.vehicle.BoatManager;
import dwo.gameserver.model.actor.instance.L2BoatInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.VehiclePathPoint;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import org.apache.log4j.Level;

public class BoatGludinRune extends Quest implements Runnable
{
	// Time: 1151s
	private static final VehiclePathPoint[] GLUDIN_TO_RUNE = {
		new VehiclePathPoint(-95686, 155514, -3610, 150, 800), new VehiclePathPoint(-98112, 159040, -3610, 150, 800),
		new VehiclePathPoint(-104192, 160608, -3610, 200, 1800),
		new VehiclePathPoint(-109952, 159616, -3610, 250, 1800),
		new VehiclePathPoint(-112768, 154784, -3610, 290, 1800),
		new VehiclePathPoint(-114688, 139040, -3610, 290, 1800),
		new VehiclePathPoint(-115232, 134368, -3610, 290, 1800),
		new VehiclePathPoint(-113888, 121696, -3610, 290, 1800),
		new VehiclePathPoint(-107808, 104928, -3610, 290, 1800), new VehiclePathPoint(-97152, 75520, -3610, 290, 800),
		new VehiclePathPoint(-85536, 67264, -3610, 290, 1800), new VehiclePathPoint(-64640, 55840, -3610, 290, 1800),
		new VehiclePathPoint(-60096, 44672, -3610, 290, 1800), new VehiclePathPoint(-52672, 37440, -3610, 290, 1800),
		new VehiclePathPoint(-46144, 33184, -3610, 290, 1800), new VehiclePathPoint(-36096, 24928, -3610, 290, 1800),
		new VehiclePathPoint(-33792, 8448, -3610, 290, 1800), new VehiclePathPoint(-23776, 3424, -3610, 290, 1000),
		new VehiclePathPoint(-12000, -1760, -3610, 290, 1000), new VehiclePathPoint(672, 480, -3610, 290, 1800),
		new VehiclePathPoint(15488, 200, -3610, 290, 1000), new VehiclePathPoint(24736, 164, -3610, 290, 1000),
		new VehiclePathPoint(32192, -1156, -3610, 290, 1000), new VehiclePathPoint(39200, -8032, -3610, 270, 1000),
		new VehiclePathPoint(44320, -25152, -3610, 270, 1000), new VehiclePathPoint(40576, -31616, -3610, 250, 800),
		new VehiclePathPoint(36819, -35315, -3610, 220, 800)
	};

	private static final VehiclePathPoint[] RUNE_DOCK = {
		new VehiclePathPoint(34381, -37680, -3610, 200, 800)
	};

	// Time: 967s
	private static final VehiclePathPoint[] RUNE_TO_GLUDIN = {
		new VehiclePathPoint(32750, -39300, -3610, 150, 800), new VehiclePathPoint(27440, -39328, -3610, 180, 1000),
		new VehiclePathPoint(21456, -34272, -3610, 200, 1000), new VehiclePathPoint(6608, -29520, -3610, 250, 800),
		new VehiclePathPoint(4160, -27828, -3610, 270, 800), new VehiclePathPoint(2432, -25472, -3610, 270, 1000),
		new VehiclePathPoint(-8000, -16272, -3610, 220, 1000), new VehiclePathPoint(-18976, -9760, -3610, 290, 800),
		new VehiclePathPoint(-23776, 3408, -3610, 290, 800), new VehiclePathPoint(-33792, 8432, -3610, 290, 800),
		new VehiclePathPoint(-36096, 24912, -3610, 290, 800), new VehiclePathPoint(-46144, 33184, -3610, 290, 800),
		new VehiclePathPoint(-52688, 37440, -3610, 290, 800), new VehiclePathPoint(-60096, 44672, -3610, 290, 800),
		new VehiclePathPoint(-64640, 55840, -3610, 290, 800), new VehiclePathPoint(-85552, 67248, -3610, 290, 800),
		new VehiclePathPoint(-97168, 85264, -3610, 290, 800), new VehiclePathPoint(-107824, 104912, -3610, 290, 800),
		new VehiclePathPoint(-102151, 135704, -3610, 290, 800), new VehiclePathPoint(-96686, 140595, -3610, 290, 800),
		new VehiclePathPoint(-95686, 147717, -3610, 250, 800), new VehiclePathPoint(-95686, 148218, -3610, 200, 800)
	};

	private static final VehiclePathPoint[] GLUDIN_DOCK = {
		new VehiclePathPoint(-95686, 150514, -3610, 150, 800)
	};

	private static L2BoatInstance _boat;
	private static PlaySound GLUDIN_SOUND;
	private static PlaySound RUNE_SOUND;
	private int _cycle;
	private int _shoutCount;

	public BoatGludinRune()
	{
		_boat = BoatManager.getInstance().getNewBoat(3, -95686, 150514, -3610, 16723);
		if(_boat != null)
		{
			GLUDIN_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", 1, _boat.getObjectId(), GLUDIN_DOCK[0].x, GLUDIN_DOCK[0].y, GLUDIN_DOCK[0].z);
			RUNE_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", 1, _boat.getObjectId(), RUNE_DOCK[0].x, RUNE_DOCK[0].y, RUNE_DOCK[0].z);

			_boat.registerEngine(this);
			_boat.runEngine(180000);
			BoatManager.getInstance().dockShip(BoatManager.GLUDIN_HARBOR, true);
		}
	}

	public static void main(String[] args)
	{
		new BoatGludinRune();
	}

	@Override
	public void run()
	{
		try
		{
			switch(_cycle)
			{
				case 0:
					BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0]);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 240000);
					break;
				case 1:
					BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0]);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 40000);
					break;
				case 2:
					BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0]);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 20000);
					break;
				case 3:
					BoatManager.getInstance().dockShip(BoatManager.GLUDIN_HARBOR, false);
					BoatManager.getInstance().broadcastPackets(GLUDIN_DOCK[0], RUNE_DOCK[0]);
					_boat.broadcastPacket(GLUDIN_SOUND);
					_boat.payForRide(7905, 1, -90015, 150422, -3610);
					_boat.executePath(GLUDIN_TO_RUNE);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 250000);
					break;
				case 4:
					BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0]);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
				case 5:
					BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0]);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
				case 6:
					BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0]);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 240000);
					break;
				case 7:
					BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0]);
					break;
				case 8:
					if(BoatManager.getInstance().dockBusy(BoatManager.RUNE_HARBOR))
					{
						if(_shoutCount == 0)
						{
							BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0]);
						}

						_shoutCount++;
						if(_shoutCount > 35)
						{
							_shoutCount = 0;
						}

						ThreadPoolManager.getInstance().scheduleGeneral(this, 5000);
						return;
					}
					_boat.executePath(RUNE_DOCK);
					break;
				case 9:
					BoatManager.getInstance().dockShip(BoatManager.RUNE_HARBOR, true);
					BoatManager.getInstance().broadcastPackets(RUNE_DOCK[0], GLUDIN_DOCK[0]);
					_boat.broadcastPacket(RUNE_SOUND);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
				case 10:
					BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0]);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 240000);
					break;
				case 11:
					BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0]);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 40000);
					break;
				case 12:
					BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0]);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 20000);
					break;
				case 13:
					BoatManager.getInstance().dockShip(BoatManager.RUNE_HARBOR, false);
					BoatManager.getInstance().broadcastPackets(RUNE_DOCK[0], GLUDIN_DOCK[0]);
					_boat.broadcastPacket(RUNE_SOUND);
					_boat.payForRide(7904, 1, 34513, -38009, -3640);
					_boat.executePath(RUNE_TO_GLUDIN);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 60000);
					break;
				case 14:
					BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0]);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
				case 15:
					BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0]);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
				case 16:
					BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0]);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 240000);
					break;
				case 17:
					BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0]);
					break;
				case 18:
					if(BoatManager.getInstance().dockBusy(BoatManager.GLUDIN_HARBOR))
					{
						if(_shoutCount == 0)
						{
							BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0]);
						}

						_shoutCount++;
						if(_shoutCount > 35)
						{
							_shoutCount = 0;
						}

						ThreadPoolManager.getInstance().scheduleGeneral(this, 5000);
						return;
					}
					_boat.executePath(GLUDIN_DOCK);
					break;
				case 19:
					BoatManager.getInstance().dockShip(BoatManager.GLUDIN_HARBOR, true);
					BoatManager.getInstance().broadcastPackets(GLUDIN_DOCK[0], RUNE_DOCK[0]);
					_boat.broadcastPacket(GLUDIN_SOUND);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
			}
			_shoutCount = 0;
			_cycle++;
			if(_cycle > 19)
			{
				_cycle = 0;
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, e.getMessage(), e);
		}
	}
}