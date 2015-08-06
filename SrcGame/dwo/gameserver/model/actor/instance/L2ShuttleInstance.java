package dwo.gameserver.model.actor.instance;

import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.L2Vehicle;
import dwo.gameserver.model.actor.ai.L2ShuttleAI;
import dwo.gameserver.model.actor.templates.L2CharTemplate;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle.ExShuttuleInfo;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle.ExSuttleMove;

import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 03.08.11
 * Time: 23:01
 */
public class L2ShuttleInstance extends L2Vehicle
{
	private int _shuttleId;
	private int[] _type = new int[5];

	public L2ShuttleInstance(int objectId, L2CharTemplate template, int id, int[] type)
	{
		super(objectId, template);
		setAI(new L2ShuttleAI(new AIAccessor()));
		_shuttleId = id;
		_type = type;
	}

	@Override
	public boolean isShuttle()
	{
		return true;
	}

	@Override
	public void updateAbnormalEffect()
	{
		broadcastPacket(new ExShuttuleInfo(this));
	}

	@Override
	public void stopMove(Location pos, boolean updateKnownObjects)
	{
		super.stopMove(pos, updateKnownObjects);

		//broadcastPacket(new ExStopMoveAirShip(this));
	}

	@Override
	public boolean moveToNextRoutePoint()
	{
		boolean result = super.moveToNextRoutePoint();
		if(result)
		{
			broadcastPacket(new ExSuttleMove(this));
		}

		return result;
	}

	@Override
	public boolean onDelete()
	{
		return super.onDelete();
	}

	public int getId()
	{
		return _shuttleId;
	}

	public int[] getType()
	{
		return _type;
	}

	public void setType(int[] type)
	{
		_type = type;
	}

	public void openDoor(int doorId)
	{
		L2DoorInstance door = DoorGeoEngine.getInstance().getDoor(doorId);
		if(door != null)
		{
			door.openMe();
		}
	}

	public void closeDoor(int doorId)
	{
		L2DoorInstance door = DoorGeoEngine.getInstance().getDoor(doorId);
		if(door != null)
		{
			door.closeMe();
		}
	}

	public void teleportPlayers()
	{
		List<L2PcInstance> objects = getPassengers();
		for(L2PcInstance object : objects)
		{
			object.getLocationController().setZ(getLoc().getZ());
			object.revalidateZone(true);
			for(L2Summon summon : object.getPets())
			{
				summon.getLocationController().setZ(getLoc().getZ());
				summon.revalidateZone(true);
			}
		}
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new ExShuttuleInfo(this));
	}
}
