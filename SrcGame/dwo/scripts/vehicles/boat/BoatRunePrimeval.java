package dwo.scripts.vehicles.boat;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.vehicle.BoatManager;
import dwo.gameserver.model.actor.instance.L2BoatInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.VehiclePathPoint;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import org.apache.log4j.Level;

public class BoatRunePrimeval extends Quest implements Runnable
{
	// Time: 239s
	private static final VehiclePathPoint[] RUNE_TO_PRIMEVAL = {
		new VehiclePathPoint(32750, -39300, -3610, 180, 800), new VehiclePathPoint(27440, -39328, -3610, 250, 1000),
		new VehiclePathPoint(19616, -39360, -3610, 270, 1000), new VehiclePathPoint(3840, -38528, -3610, 270, 1000),
		new VehiclePathPoint(1664, -37120, -3610, 270, 1000), new VehiclePathPoint(896, -34560, -3610, 180, 1800),
		new VehiclePathPoint(832, -31104, -3610, 180, 180), new VehiclePathPoint(2240, -29132, -3610, 150, 1800),
		new VehiclePathPoint(4160, -27828, -3610, 150, 1800), new VehiclePathPoint(5888, -27279, -3610, 150, 1800),
		new VehiclePathPoint(7000, -27279, -3610, 150, 1800), new VehiclePathPoint(10342, -27279, -3610, 150, 1800)
	};
	private static final VehiclePathPoint PRIMEVAL_DOCK = RUNE_TO_PRIMEVAL[RUNE_TO_PRIMEVAL.length - 1];
	// Time: 221s
	private static final VehiclePathPoint[] PRIMEVAL_TO_RUNE = {
		new VehiclePathPoint(15528, -27279, -3610, 180, 800), new VehiclePathPoint(22304, -29664, -3610, 290, 800),
		new VehiclePathPoint(33824, -26880, -3610, 290, 800), new VehiclePathPoint(38848, -21792, -3610, 240, 1200),
		new VehiclePathPoint(43424, -22080, -3610, 180, 1800), new VehiclePathPoint(44320, -25152, -3610, 180, 1800),
		new VehiclePathPoint(40576, -31616, -3610, 250, 800), new VehiclePathPoint(36819, -35315, -3610, 220, 800)
	};
	private static final VehiclePathPoint[] RUNE_DOCK = {
		new VehiclePathPoint(34381, -37680, -3610, 220, 800)
	};
	private static L2BoatInstance _boat;
	private static PlaySound RUNE_SOUND;
	private static PlaySound PRIMEVAL_SOUND;
	private int _cycle;
	private int _shoutCount;

	public BoatRunePrimeval()
	{
		_boat = BoatManager.getInstance().getNewBoat(5, 34381, -37680, -3610, 40785);
		if(_boat != null)
		{
			RUNE_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", 1, _boat.getObjectId(), RUNE_DOCK[0].x, RUNE_DOCK[0].y, RUNE_DOCK[0].z);
			PRIMEVAL_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", 1, _boat.getObjectId(), PRIMEVAL_DOCK.x, PRIMEVAL_DOCK.y, PRIMEVAL_DOCK.z);

			_boat.registerEngine(this);
			_boat.runEngine(180000);
			BoatManager.getInstance().dockShip(BoatManager.RUNE_HARBOR, true);
		}
	}

	public static void main(String[] args)
	{
		new BoatRunePrimeval();
	}

	@Override
	public void run()
	{
		try
		{
			switch(_cycle)
			{
				case 0:
					BoatManager.getInstance().dockShip(BoatManager.RUNE_HARBOR, false);
					BoatManager.getInstance().broadcastPackets(RUNE_DOCK[0], PRIMEVAL_DOCK, RUNE_SOUND);
					_boat.payForRide(8925, 1, 34513, -38009, -3640);
					_boat.executePath(RUNE_TO_PRIMEVAL);
					break;
				case 1:
					BoatManager.getInstance().broadcastPackets(PRIMEVAL_DOCK, RUNE_DOCK[0], PRIMEVAL_SOUND);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 180000);
					break;
				case 2:
					BoatManager.getInstance().broadcastPackets(PRIMEVAL_DOCK, RUNE_DOCK[0], PRIMEVAL_SOUND);
					_boat.payForRide(8924, 1, 10447, -24982, -3664);
					_boat.executePath(PRIMEVAL_TO_RUNE);
					break;
				case 3:
					if(BoatManager.getInstance().dockBusy(BoatManager.RUNE_HARBOR))
					{
						if(_shoutCount == 0)
						{
							BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], PRIMEVAL_DOCK);
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
				case 4:
					BoatManager.getInstance().dockShip(BoatManager.RUNE_HARBOR, true);
					BoatManager.getInstance().broadcastPackets(RUNE_DOCK[0], PRIMEVAL_DOCK, RUNE_SOUND);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 180000);
					break;
			}
			_shoutCount = 0;
			_cycle++;
			if(_cycle > 4)
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