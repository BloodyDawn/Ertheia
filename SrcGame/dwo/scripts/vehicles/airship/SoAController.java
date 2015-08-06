package dwo.scripts.vehicles.airship;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.VehiclePathPoint;

public class SoAController extends AirShipController
{
	private static final int DOCK_ZONE = 50604;
	private static final int LOCATION = 103;
	private static final int CONTROLLER_ID = 32779;

	private static final VehiclePathPoint[] ARRIVAL = {
		new VehiclePathPoint(-174946, 155306, 3105, 280, 2000),
	};

	private static final VehiclePathPoint[] DEPART = {
		new VehiclePathPoint(-178609, 160250, 4383, 280, 2000)
	};

	private static final VehiclePathPoint[][] TELEPORTS = {
		{
			new VehiclePathPoint(-178609, 160250, 4383, 280, 2000), new VehiclePathPoint(-179557, 166493, 4023, 0, 0)
		}, {
		new VehiclePathPoint(-178609, 160250, 4383, 280, 2000), new VehiclePathPoint(-195357, 233430, 2500, 0, 0)
	}
	};

	private static final int[] FUEL = {
		0, 150
	};

	public SoAController()
	{
		addAskId(CONTROLLER_ID, -1724);
		addAskId(CONTROLLER_ID, -2011);
		addFirstTalkId(CONTROLLER_ID);

		_dockZone = DOCK_ZONE;
		addEnterZoneId(DOCK_ZONE);
		addExitZoneId(DOCK_ZONE);

		_shipSpawnX = -170695;
		_shipSpawnY = 153530;
		_shipSpawnZ = 4358;

		_oustLoc = new Location(-175689, 154160, 2712);

		_locationId = LOCATION;
		_arrivalPath = ARRIVAL;
		_departPath = DEPART;
		_teleportsTable = TELEPORTS;
		_fuelTable = FUEL;

		_movieId = 1004;

		validityCheck();
	}

	public static void main(String[] args)
	{
		new SoAController();
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "air_manager_kserth1002.htm";
	}
}