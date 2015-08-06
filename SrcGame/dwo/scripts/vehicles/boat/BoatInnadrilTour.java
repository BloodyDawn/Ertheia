package dwo.scripts.vehicles.boat;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.vehicle.BoatManager;
import dwo.gameserver.model.actor.instance.L2BoatInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.VehiclePathPoint;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import org.apache.log4j.Level;

public class BoatInnadrilTour extends Quest implements Runnable
{
	// Time: 1867s
	private static final VehiclePathPoint[] TOUR = {
		new VehiclePathPoint(105129, 226240, -3610, 150, 800), new VehiclePathPoint(90604, 238797, -3610, 150, 800),
		new VehiclePathPoint(74853, 237943, -3610, 150, 800), new VehiclePathPoint(68207, 235399, -3610, 150, 800),
		new VehiclePathPoint(63226, 230487, -3610, 150, 800), new VehiclePathPoint(61843, 224797, -3610, 150, 800),
		new VehiclePathPoint(61822, 203066, -3610, 150, 800), new VehiclePathPoint(59051, 197685, -3610, 150, 800),
		new VehiclePathPoint(54048, 195298, -3610, 150, 800), new VehiclePathPoint(41609, 195687, -3610, 150, 800),
		new VehiclePathPoint(35821, 200284, -3610, 150, 800), new VehiclePathPoint(35567, 205265, -3610, 150, 800),
		new VehiclePathPoint(35617, 222471, -3610, 150, 800), new VehiclePathPoint(37932, 226588, -3610, 150, 800),
		new VehiclePathPoint(42932, 229394, -3610, 150, 800), new VehiclePathPoint(74324, 245231, -3610, 150, 800),
		new VehiclePathPoint(81872, 250314, -3610, 150, 800), new VehiclePathPoint(101692, 249882, -3610, 150, 800),
		new VehiclePathPoint(107907, 256073, -3610, 150, 800), new VehiclePathPoint(112317, 257133, -3610, 150, 800),
		new VehiclePathPoint(126273, 255313, -3610, 150, 800), new VehiclePathPoint(128067, 250961, -3610, 150, 800),
		new VehiclePathPoint(128520, 238249, -3610, 150, 800), new VehiclePathPoint(126428, 235072, -3610, 150, 800),
		new VehiclePathPoint(121843, 234656, -3610, 150, 800), new VehiclePathPoint(120096, 234268, -3610, 150, 800),
		new VehiclePathPoint(118572, 233046, -3610, 150, 800), new VehiclePathPoint(117671, 228951, -3610, 150, 800),
		new VehiclePathPoint(115936, 226540, -3610, 150, 800), new VehiclePathPoint(113628, 226240, -3610, 150, 800),
		new VehiclePathPoint(111300, 226240, -3610, 150, 800), new VehiclePathPoint(111264, 226240, -3610, 150, 800)
	};

	private static final VehiclePathPoint DOCK = TOUR[TOUR.length - 1];

	private static L2BoatInstance _boat;
	private static PlaySound INNADRIL_SOUND;
	private int _cycle;

	public BoatInnadrilTour()
	{
		_boat = BoatManager.getInstance().getNewBoat(4, 111264, 226240, -3610, 32768);
		if(_boat != null)
		{
			INNADRIL_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", 1, _boat.getObjectId(), DOCK.x, DOCK.y, DOCK.z);

			_boat.registerEngine(this);
			_boat.runEngine(180000);
		}
	}

	public static void main(String[] args)
	{
		new BoatInnadrilTour();
	}

	@Override
	public void run()
	{
		try
		{
			switch(_cycle)
			{
				case 0:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 240000);
					break;
				case 1:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 40000);
					break;
				case 2:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 20000);
					break;
				case 3:
					BoatManager.getInstance().broadcastPackets(DOCK, DOCK, INNADRIL_SOUND);
					_boat.payForRide(0, 1, 107092, 219098, -3952);
					_boat.executePath(TOUR);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 650000);
					break;
				case 4:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
				case 5:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
				case 6:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
				case 7:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 240000);
					break;
				case 8:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK);
					break;
				case 9:
					BoatManager.getInstance().broadcastPackets(DOCK, DOCK, INNADRIL_SOUND);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
			}
			_cycle++;
			if(_cycle > 9)
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