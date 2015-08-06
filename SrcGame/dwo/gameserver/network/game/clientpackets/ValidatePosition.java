package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.airship.ExValidateLocationInAirShip;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocationInVehicle;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle.ExValidateLocationInShuttle;

public class ValidatePosition extends L2GameClientPacket
{
	private static final int MAX_XY_DIFF = 500;
	private static final int MAX_XY_DIFF_SQ = MAX_XY_DIFF * MAX_XY_DIFF;

	private static final int MIN_XY_DIFF = 100;
	private static final int MIN_XY_DIFF_SQ = MIN_XY_DIFF * MIN_XY_DIFF;

	private static final int MIN_Z_DIFF = 100;
	private static final int MAX_Z_DIFF = 20;
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	///private int _data; // vehicle id

	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
		//_data  = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.isTeleporting())
		{
			return;
		}

		int realX = activeChar.getX();
		int realY = activeChar.getY();
		int realZ = activeChar.getZ();

		if(_x == 0 && _y == 0)
		{
			if(realX != 0) // in this case this seems like a client error
			{
				return;
			}
		}

		activeChar.setClientX(_x);
		activeChar.setClientY(_y);
		activeChar.setClientZ(_z);
		activeChar.setClientHeading(_heading);

		activeChar.updatePartyPosition(false);

		double dx = _x - realX;
		double dy = _y - realY;
		double diffSq = dx * dx + dy * dy;

		if(activeChar.isFlying() || activeChar.isInsideZone(L2Character.ZONE_WATER))
		{
			if(activeChar.isFlyingMounted())
			{
				// В летающей трансформе нельзя находиться на территории Aden
				if(_x > -166168)
				{
					activeChar.untransform(true);
					return;
				}

				// В летающей трансформе нельзя летать ниже, чем 0, и выше, чем 6000
				if(_z <= 0 || _z >= 6000)
				{
					activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), Math.min(5950, Math.max(50, _z)));
					return;
				}
			}

			activeChar.setXYZ(realX, realY, _z);
			if(diffSq > 90000) // validate packet, may also cause z bounce if close to land
			{
				sendValidation(activeChar);
				return;
			}

			activeChar.setLastServerPosition(realX, realY, realZ);
			return;
		}
		if(diffSq < MAX_XY_DIFF_SQ) // if too large, messes observation
		{
			if(GeoEngine.getInstance().hasGeo(realX, realY))
			{
				int geoZ = GeoEngine.getInstance().getHeight(realX, realY, realZ);
				if(_z >= geoZ && _z - geoZ < MAX_Z_DIFF)
				{
					if(Config.COORD_SYNCHRONIZE == 1 && (diffSq > MIN_XY_DIFF_SQ || Math.abs(_z - activeChar.getZ()) > MIN_Z_DIFF))
					{
						activeChar.setXYZ(realX, realY, _z);
						sendValidation(activeChar);
					}
					else
					{
						activeChar.setXYZ(realX, realY, _z);
					}
				}
				else if(Config.COORD_SYNCHRONIZE == 1 && (diffSq > MIN_XY_DIFF_SQ || Math.abs(_z - activeChar.getZ()) > 100 || _z < geoZ))
				{
					sendValidation(activeChar);
					return;
				}
			}
			else
			{
				// Нету гео квадрата
				activeChar.setXYZ(realX, realY, _z);
				if(Config.COORD_SYNCHRONIZE == 1)
				{
					if(diffSq > MIN_XY_DIFF_SQ || Math.abs(_z - activeChar.getZ()) > MIN_Z_DIFF)
					{
						sendValidation(activeChar);
					}
				}
			}
		}

		activeChar.setLastServerPosition(realX, realY, realZ);
	}

	@Override
	public String getType()
	{
		return "[C] 48 ValidatePosition";
	}

	private void sendValidation(L2PcInstance activeChar)
	{
		if(activeChar.isInBoat())
		{
			activeChar.sendPacket(new ValidateLocationInVehicle(activeChar));
		}
		else if(activeChar.isInAirShip())
		{
			activeChar.sendPacket(new ExValidateLocationInAirShip(activeChar));
		}
		else if(activeChar.isInShuttle())
		{
			activeChar.sendPacket(new ExValidateLocationInShuttle(activeChar));
		}
		else
		{
			activeChar.sendPacket(new ValidateLocation(activeChar));
		}
	}

	private void validateOldWay()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.isTeleporting())
		{
			return;
		}

		int realX = activeChar.getX();
		int realY = activeChar.getY();
		int realZ = activeChar.getZ();

		if(_x == 0 && _y == 0)
		{
			if(realX != 0) // in this case this seems like a client error
			{
				return;
			}
		}

		activeChar.setClientX(_x);
		activeChar.setClientY(_y);
		activeChar.setClientZ(_z);

		activeChar.updatePartyPosition(false);

		double dx = _x - realX;
		double dy = _y - realY;
		double dz = _z - realZ;
		double diffSq = dx * dx + dy * dy;

		if(activeChar.isFlying() || activeChar.isInsideZone(L2Character.ZONE_WATER))
		{
			activeChar.setXYZ(realX, realY, _z);
			if(diffSq > 90000) // validate packet, may also cause z bounce if close to land
			{
				if(activeChar.isInBoat())
				{
					activeChar.sendPacket(new ValidateLocationInVehicle(activeChar));
				}
				else if(activeChar.isInAirShip())
				{
					activeChar.sendPacket(new ExValidateLocationInAirShip(activeChar));
				}
				else if(activeChar.isInShuttle())
				{
					activeChar.sendPacket(new ExValidateLocationInShuttle(activeChar));
				}
				else
				{
					activeChar.sendPacket(new ValidateLocation(activeChar));
				}
			}
		}
		else if(diffSq < 250000) // if too large, messes observation
		{
			if(Config.COORD_SYNCHRONIZE == -1) // Only Z coordinate synched to server,
			// mainly used when no geodata but can be used also with geodata
			{
				activeChar.setXYZ(realX, realY, _z);
				return;
			}
			if(Config.COORD_SYNCHRONIZE == 1) // Trusting also client x,y coordinates (should not be used with geodata)
			{
				if(!activeChar.isMoving() || !activeChar.validateMovementHeading(_heading)) // Heading changed on client = possible obstacle
				{
					// character is not moving, take coordinates from client
					if(diffSq < 2500) // 50*50 - attack won't work fluently if even small differences are corrected
					{
						activeChar.setXYZ(realX, realY, _z);
					}
					else
					{
						activeChar.setXYZ(_x, _y, _z);
					}
				}
				else
				{
					activeChar.setXYZ(realX, realY, _z);
				}
				activeChar.setHeading(_heading);
				return;
			}
			// Sync 2 (or other),
			// intended for geodata. Sends a validation packet to client
			// when too far from server calculated true coordinate.
			// Due to geodata/zone errors, some Z axis checks are made. (maybe a temporary solution)
			// Important: this code part must work together with L2Character.updatePosition
			if(Config.GEODATA_ENABLED && (diffSq > 10000 || Math.abs(dz) > 200))
			{
				if(Math.abs(dz) > 200 && Math.abs(dz) < 1500 && Math.abs(_z - activeChar.getClientZ()) < 800)
				{
					activeChar.setXYZ(realX, realY, _z);
					realZ = _z;
				}
				else
				{
					if(activeChar.isInBoat())
					{
						activeChar.sendPacket(new ValidateLocationInVehicle(activeChar));
					}
					else if(activeChar.isInAirShip())
					{
						activeChar.sendPacket(new ExValidateLocationInAirShip(activeChar));
					}
					else if(activeChar.isInShuttle())
					{
						activeChar.sendPacket(new ExValidateLocationInShuttle(activeChar));
					}
					else
					{
						activeChar.sendPacket(new ValidateLocation(activeChar));
					}
				}
			}
		}

		activeChar.setClientHeading(_heading); // No real need to validate heading.
		activeChar.setLastServerPosition(realX, realY, realZ);
	}
}
