package dwo.scripts.vehicles.airship;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.VehiclePathPoint;

public class SoDController extends AirShipController
{
	private static final int DOCK_ZONE = 50601;
	private static final int LOCATION = 102;
	private static final int CONTROLLER_ID = 32605;

	private static final VehiclePathPoint[] ARRIVAL = {
		new VehiclePathPoint(-246445, 252331, 4359, 280, 2000),
	};

	private static final VehiclePathPoint[] DEPART = {
		new VehiclePathPoint(-245245, 251040, 4359, 280, 2000)
	};

	private static final VehiclePathPoint[][] TELEPORTS = {
		{
			new VehiclePathPoint(-245245, 251040, 4359, 280, 2000), new VehiclePathPoint(-235693, 248843, 5100, 0, 0)
		}, {
		new VehiclePathPoint(-245245, 251040, 4359, 280, 2000), new VehiclePathPoint(-195357, 233430, 2500, 0, 0)
	}
	};

	private static final int[] FUEL = {
		0, 100
	};

	public SoDController()
	{
		addAskId(CONTROLLER_ID, -1724);
		addAskId(CONTROLLER_ID, -2011);
		addFirstTalkId(CONTROLLER_ID);

		_dockZone = DOCK_ZONE;
		addEnterZoneId(DOCK_ZONE);
		addExitZoneId(DOCK_ZONE);

		_shipSpawnX = -247702;
		_shipSpawnY = 253631;
		_shipSpawnZ = 4359;

		_oustLoc = new Location(-247746, 251079, 4328);

		_locationId = LOCATION;
		_arrivalPath = ARRIVAL;
		_departPath = DEPART;
		_teleportsTable = TELEPORTS;
		_fuelTable = FUEL;

		_movieId = 1003;

		validityCheck();
	}

	public static void main(String[] args)
	{
		new SoDController();
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "air_manager_destruction1001.htm";
	}
}