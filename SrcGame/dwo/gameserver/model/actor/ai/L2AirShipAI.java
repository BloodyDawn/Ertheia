package dwo.gameserver.model.actor.ai;

import dwo.gameserver.model.actor.L2Vehicle;
import dwo.gameserver.model.actor.instance.L2AirShipInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.airship.ExMoveToLocationAirShip;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.airship.ExStopMoveAirShip;

/**
 * @author DS
 */
public class L2AirShipAI extends L2VehicleAI
{
	public L2AirShipAI(L2Vehicle.AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	public L2AirShipInstance getActor()
	{
		return (L2AirShipInstance) _actor;
	}

	@Override
	protected void moveTo(Location loc)
	{
		if(!_actor.isMovementDisabled())
		{
			_clientMoving = true;
			_accessor.moveTo(loc.getX(), loc.getY(), loc.getZ());
			_actor.broadcastPacket(new ExMoveToLocationAirShip(getActor()));
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
			_actor.broadcastPacket(new ExStopMoveAirShip(getActor()));
		}
	}

	@Override
	public void describeStateToPlayer(L2PcInstance player)
	{
		if(_clientMoving)
		{
			player.sendPacket(new ExMoveToLocationAirShip(getActor()));
		}
	}
}