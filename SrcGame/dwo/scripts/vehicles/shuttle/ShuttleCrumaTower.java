package dwo.scripts.vehicles.shuttle;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.vehicle.ShuttleManager;
import dwo.gameserver.model.actor.instance.L2ShuttleInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.VehiclePathPoint;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle.ExShuttuleInfo;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO, Bacek
 * Date: 20.07.11
 * Time: 11:55
 */
public class ShuttleCrumaTower extends Quest implements Runnable
{
	private static final Location EXIT_POINT = new Location(17728, 115139, -11752, 473);
	private static final VehiclePathPoint[] ShuttleCrumaTower_1 = {
		new VehiclePathPoint(17728, 114176, -3502, 500, 0)
	};
	private static final VehiclePathPoint[] ShuttleCrumaTower_2 = {
		new VehiclePathPoint(17728, 114176, -11712, 500, 0)
	};
	private final L2ShuttleInstance _shuttle;
	private int _cycle;

	public ShuttleCrumaTower()
	{
		_shuttle = ShuttleManager.getInstance().getNewShuttle(17728, 114176, -11712, 0, 3, new int[]{0, 0, 1, 1, 1});
		_shuttle.registerEngine(this);
		_shuttle.runEngine(10);
	}

	public static void main(String[] args)
	{
		new ShuttleCrumaTower();
	}

	@Override
	public void run()
	{
		try
		{
			switch(_cycle)
			{
				case 0:
					//Открываем двери и ждем 10 сек
					_shuttle.teleportPlayers();
					_shuttle.openDoor(20210005);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 10000);
					break;
				case 1:
					//закрываем двери лифта
					_shuttle.closeDoor(20210005);
					_shuttle.setType(new int[]{0, 0, 1, 0, 1});
					_shuttle.broadcastPacket(new ExShuttuleInfo(_shuttle));
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
					break;
				case 2:
					//Начинаем движение лифта
					_shuttle.setType(new int[]{1, 1, 1, 0, 0});
					_shuttle.executePath(ShuttleCrumaTower_1);
					break;
				case 3:
					//Открываем двери и ждем 10 сек
					_shuttle.teleportPlayers();
					_shuttle.openDoor(20210004);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 10000);
					break;
				case 4:
					//закрываем двери лифта
					_shuttle.closeDoor(20210004);
					_shuttle.setType(new int[]{0, 1, 1, 0, 0});
					_shuttle.broadcastPacket(new ExShuttuleInfo(_shuttle));
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
					break;
				case 5:
					//Начинаем движение лифта
					_shuttle.setType(new int[]{0, 0, 1, 1, 1});
					_shuttle.executePath(ShuttleCrumaTower_2);
					break;
			}
			_cycle++;
			if(_cycle > 5)
			{
				_cycle = 0;
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "ShuttleCrumaTower cycle " + _cycle + ' ' + e);
		}
	}
}