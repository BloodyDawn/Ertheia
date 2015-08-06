package dwo.gameserver.model.actor.ai;

import dwo.gameserver.model.actor.L2Vehicle;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2ShuttleInstance;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle.ExShuttuleInfo;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle.ExSuttleMove;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 03.08.11
 * Time: 23:06
 */

public class L2ShuttleAI extends L2VehicleAI
{
	public L2ShuttleAI(L2Vehicle.AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	public L2ShuttleInstance getActor()
	{
		return (L2ShuttleInstance) _actor;
	}

	@Override
	protected void moveTo(Location loc)
	{
		if(!_actor.isMovementDisabled())
		{
			_clientMoving = true;
			_accessor.moveTo(loc.getX(), loc.getY(), loc.getZ()); //говорим серверу что мы движемся
			_actor.broadcastPacket(new ExSuttleMove(getActor())); //Шлем клиенту новую координату для начала движения
		}
	}

	@Override
	protected void clientStopMoving(Location pos)
	{
		if(_actor.isMoving())
		{
			_accessor.stopMove(pos);
		}

		if(_clientMoving || pos != null)
		{
			_clientMoving = false;
			_actor.broadcastPacket(new ExShuttuleInfo(getActor()));
		}
	}

	@Override
	public void describeStateToPlayer(L2PcInstance player)
	{
		if(_clientMoving)
		{
			player.sendPacket(new ExSuttleMove(getActor()));
		}
	}
}
