package dwo.scripts.npc.town;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.log4j.Level;

import java.util.concurrent.ScheduledFuture;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 26.11.11
 * Time: 12:14
 */

public class MammonsMoving extends Quest
{
	public static final int[] Preachs = {33511, 33512};
	public static final int merchant_of_mammon = 33739;
	public static final int blacksmith_of_mammon = 31126;
	private static final TIntObjectHashMap<Location> PreachCoords = new TIntObjectHashMap<>();
	private static final TIntObjectHashMap<Location> MammonBlackCoords = new TIntObjectHashMap<>();
	private static final TIntObjectHashMap<Location> MammonMerchanterCoords = new TIntObjectHashMap<>();
	public static L2Npc PreachNPC;
	public static L2Npc MammonMerchanterNPC;
	public static L2Npc MammonBlackNPC;
	private static ScheduledFuture<?> _movingTask;

	public MammonsMoving()
	{
		addSpawnId(Preachs);
		addSpawnId(blacksmith_of_mammon, merchant_of_mammon);

		// Аден - ok
		PreachCoords.put(0, new Location(146882, 29665, -2200));
		MammonBlackCoords.put(0, new Location(146873, 29448, -2200));
		MammonMerchanterCoords.put(0, new Location(146872, 29569, -2200));
		// Гиран - ok
		PreachCoords.put(1, new Location(81284, 150155, -3528, 0));  // -- retail
		MammonBlackCoords.put(1, new Location(81266, 150091, -3528, 0));  // -- retail
		MammonMerchanterCoords.put(1, new Location(81272, 150041, -3528, 0));  // -- retail
		// Руна - ok
		PreachCoords.put(2, new Location(42784, -41236, -2186));
		MammonBlackCoords.put(2, new Location(42825, -41337, -2186));
		MammonMerchanterCoords.put(2, new Location(42803, -41283, -2186));

		startSpawn();
	}

	private static void validatePosition()
	{
		int town = Rnd.get(2);

		PreachNPC.getSpawn().setLocation(PreachCoords.get(town));
		PreachNPC.teleToLocation(PreachCoords.get(town), false);

		MammonMerchanterNPC.getSpawn().setLocation(MammonMerchanterCoords.get(town));
		MammonMerchanterNPC.teleToLocation(MammonMerchanterCoords.get(town), false);

		MammonBlackNPC.getSpawn().setLocation(MammonBlackCoords.get(town));
		MammonBlackNPC.teleToLocation(MammonBlackCoords.get(town), false);

		_log.log(Level.INFO, "Mammons Engine: Mammons are gone in other town # " + town);
	}

	public static Location getMammonCoords()
	{
		return MammonBlackNPC.getLoc();
	}

	public static void toOtherTown()
	{
		if(_movingTask != null)
		{
			_movingTask.cancel(false);
			_movingTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new moveToOtherTown(), 100, Config.MAMMONS_TELEPORT_RATE);
		}
	}

	public static void main(String[] args)
	{
		new MammonsMoving();
	}

	private void startSpawn()
	{
		int town = Rnd.get(2);

		PreachNPC = addSpawn(Preachs[Rnd.get(1)], PreachCoords.get(town));
		MammonBlackNPC = addSpawn(blacksmith_of_mammon, MammonBlackCoords.get(town));
		MammonMerchanterNPC = addSpawn(merchant_of_mammon, MammonMerchanterCoords.get(town));

		_movingTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new moveToOtherTown(), 20000, Config.MAMMONS_TELEPORT_RATE);
		_log.log(Level.INFO, "Mammons Engine: Initializing...");
	}

	private static class moveToOtherTown implements Runnable
	{
		@Override
		public void run()
		{
			validatePosition();
		}
	}
}