package dwo.scripts.vehicles.shuttle;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.vehicle.ShuttleManager;
import dwo.gameserver.model.actor.instance.L2ShuttleInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.VehiclePathPoint;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle.ExShuttuleInfo;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 02.03.12
 * Time: 17:00
 */
public class ShuttleArcanaRight extends Quest implements Runnable
{
	private static final VehiclePathPoint[] down = {
		new VehiclePathPoint(206580, 82700, -1035, 300, 0)
	};
	private static final VehiclePathPoint[] top = {
		new VehiclePathPoint(206580, 82700, 323, 300, 0)
	};
	private final L2ShuttleInstance _shuttle;
	private int _cycle;

	public ShuttleArcanaRight()
	{
		_shuttle = ShuttleManager.getInstance().getNewShuttle(206580, 82700, -1035, 0, 1, new int[]{1, 1, 1, 0, 0});
		_shuttle.registerEngine(this);
		_shuttle.runEngine(10);
	}

	public static void main(String[] args)
	{
		new ShuttleArcanaRight();
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
					_shuttle.openDoor(26200002);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 10000);
					break;
				case 1:
					//закрываем двери лифта
					_shuttle.closeDoor(26200002);
					_shuttle.setType(new int[]{0, 1, 1, 0, 0});
					_shuttle.broadcastPacket(new ExShuttuleInfo(_shuttle));
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
					break;
				case 2:
					//Начинаем движение лифта
					_shuttle.setType(new int[]{0, 0, 1, 1, 1});
					_shuttle.executePath(top);
					break;
				case 3:
					//Открываем двери и ждем 10 сек
					_shuttle.teleportPlayers();
					_shuttle.openDoor(26200005);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 10000);
					break;
				case 4:
					//закрываем двери лифта
					_shuttle.closeDoor(26200005);
					_shuttle.setType(new int[]{0, 0, 1, 0, 1});
					_shuttle.broadcastPacket(new ExShuttuleInfo(_shuttle));
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
					break;
				case 5:
					//Начинаем движение лифта
					_shuttle.setType(new int[]{1, 1, 1, 0, 0});
					_shuttle.executePath(down);
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
			_log.log(Level.ERROR, "ShuttleArcanaRight cycle " + _cycle + ' ' + e);
		}
	}
}