package dwo.scripts.vehicles.airship;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.instancemanager.vehicle.AirShipManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2AirShipInstance;
import dwo.gameserver.model.actor.instance.L2ControllableAirShipInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.VehiclePathPoint;
import dwo.gameserver.model.world.zone.type.L2ScriptZone;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.Level;

import java.util.concurrent.Future;

public abstract class AirShipController extends Quest
{
	private static final int DEPART_INTERVAL = 300000; // 5 min
	private static final int LICENSE = 13559;
	private static final int STARSTONE = 13277;
	private static final int SUMMON_COST = 5;
	private final Runnable _decayTask = new DecayTask();
	private final Runnable _departTask = new DepartTask();
	protected int _dockZone;
	protected int _shipSpawnX;
	protected int _shipSpawnY;
	protected int _shipSpawnZ;
	protected int _shipHeading;
	protected Location _oustLoc;
	protected int _locationId;
	protected VehiclePathPoint[] _arrivalPath;
	protected VehiclePathPoint[] _departPath;
	protected VehiclePathPoint[][] _teleportsTable;
	protected int[] _fuelTable;
	protected int _movieId;
	protected boolean _isBusy;
	protected L2ControllableAirShipInstance _dockedShip;
	private Future<?> _departSchedule;
	private NS _arrivalMessage;

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		int ownerId;
		if(ask == -1724)
		{
			switch(reply)
			{
				case 1: // Взойти на корабль
					if(player.isTransformed())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_TRANSFORMED);
						return null;
					}
					if(player.isParalyzed())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_PETRIFIED);
						return null;
					}
					if(player.isDead() || player.isFakeDeath())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_DEAD);
						return null;
					}
					if(player.isFishing())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_FISHING);
						return null;
					}
					if(player.isInCombat())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_IN_BATTLE);
						return null;
					}
					if(player.isInDuel())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_IN_A_DUEL);
						return null;
					}
					if(player.isSitting())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_SITTING);
						return null;
					}
					if(player.isCastingNow())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_CASTING);
						return null;
					}
					if(player.isCursedWeaponEquipped())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_A_CURSED_WEAPON_IS_EQUIPPED);
						return null;
					}
					if(player.isCombatFlagEquipped())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_HOLDING_A_FLAG);
						return null;
					}
					if(!player.getPets().isEmpty() || player.isMounted())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_A_PET_OR_A_SERVITOR_IS_SUMMONED);
						return null;
					}
					if(player.isFlyingMounted())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_NOT_MEET_REQUEIREMENTS);
						return null;
					}

					if(_dockedShip != null)
					{
						_dockedShip.addPassenger(player);
					}
					break;
				case 2: // Призвать корабль
					if(_dockedShip != null)
					{
						if(_dockedShip.isOwner(player))
						{
							player.sendPacket(SystemMessageId.THE_AIRSHIP_IS_ALREADY_EXISTS);
						}
						return null;
					}
					if(_isBusy)
					{
						player.sendPacket(SystemMessageId.ANOTHER_AIRSHIP_ALREADY_SUMMONED);
						return null;
					}
					if((player.getClanPrivileges() & L2Clan.CP_CL_SUMMON_AIRSHIP) != L2Clan.CP_CL_SUMMON_AIRSHIP)
					{
						player.sendPacket(SystemMessageId.THE_AIRSHIP_NO_PRIVILEGES);
						return null;
					}
					ownerId = player.getClanId();
					if(!AirShipManager.getInstance().hasAirShipLicense(ownerId))
					{
						player.sendPacket(SystemMessageId.THE_AIRSHIP_NEED_LICENSE_TO_SUMMON);
						return null;
					}
					if(AirShipManager.getInstance().hasAirShip(ownerId))
					{
						player.sendPacket(SystemMessageId.THE_AIRSHIP_ALREADY_USED);
						return null;
					}
					if(!player.destroyItemByItemId(ProcessType.CONSUME, STARSTONE, SUMMON_COST, npc, true))
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NEED_MORE_S1).addItemName(STARSTONE));
						return null;
					}

					_isBusy = true;
					L2AirShipInstance ship = AirShipManager.getInstance().getNewAirShip(_shipSpawnX, _shipSpawnY, _shipSpawnZ, _shipHeading, ownerId);
					if(ship != null)
					{
						if(_arrivalPath != null)
						{
							ship.executePath(_arrivalPath);
						}

						if(_arrivalMessage == null)
						{
							_arrivalMessage = new NS(npc.getObjectId(), ChatType.SHOUT, npc.getNpcId(), NpcStringId.THE_AIRSHIP_HAS_BEEN_SUMMONED_IT_WILL_AUTOMATICALLY_DEPART_IN_5_MINUTES);
						}

						npc.broadcastPacket(_arrivalMessage);
					}
					else
					{
						_isBusy = false;
					}
					break;
				case 3: // Подать заявку
					if(player.getClan() == null || player.getClan().getLevel() < 5)
					{
						player.sendPacket(SystemMessageId.THE_AIRSHIP_NEED_CLANLVL_5_TO_SUMMON);
						return null;
					}
					if(!player.isClanLeader())
					{
						player.sendPacket(SystemMessageId.THE_AIRSHIP_NO_PRIVILEGES);
						return null;
					}
					ownerId = player.getClanId();
					if(AirShipManager.getInstance().hasAirShipLicense(ownerId))
					{
						player.sendPacket(SystemMessageId.THE_AIRSHIP_SUMMON_LICENSE_ALREADY_ACQUIRED);
						return null;
					}
					if(!player.destroyItemByItemId(ProcessType.CONSUME, LICENSE, 1, npc, true))
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NEED_MORE_S1).addItemName(STARSTONE));
						return null;
					}

					AirShipManager.getInstance().registerLicense(ownerId);
					player.sendPacket(SystemMessageId.THE_AIRSHIP_SUMMON_LICENSE_ENTERED);
					break;

			}
		}
		else if(ask == -2011)
		{
			switch(reply)
			{
				case 1: // Отправиться в Семя Бессмертия
					player.teleToLocation(-212808, 209672, 4257);
					return null;
				case 2: // Отправиться в Семя Разрушения
					player.teleToLocation(-248536, 250280, 4311);
					return null;
				case 3: // Отправиться в Семя Уничтожения
					player.teleToLocation(-175512, 154488, 2689);
					return null;
				case 4:
					player.teleToLocation(-149406, 255247, -80);
					return null;
				case 5: // Отправиться в Семя Индустриализции
					if(player.getLevel() >= 95)
					{
						player.teleToLocation(-147348, 152615, -14048);
						return null;
					}
			}
		}
		return null;
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2ControllableAirShipInstance)
		{
			if(_dockedShip == null)
			{
				_dockedShip = (L2ControllableAirShipInstance) character;
				_dockedShip.setInDock(_dockZone);
				_dockedShip.setOustLoc(_oustLoc);

				// Ship is not empty - display movie to passengers and dock
				if(_dockedShip.isEmpty())
				{
					_departSchedule = ThreadPoolManager.getInstance().scheduleGeneral(_departTask, DEPART_INTERVAL);
				}
				else
				{
					if(_movieId != 0)
					{
						_dockedShip.getPassengers().stream().filter(passenger -> passenger != null).forEach(passenger -> passenger.showQuestMovie(_movieId));
					}

					ThreadPoolManager.getInstance().scheduleGeneral(_decayTask, 1000);
				}
			}
		}
		return null;
	}

	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2ControllableAirShipInstance)
		{
			if(character.equals(_dockedShip))
			{
				if(_departSchedule != null)
				{
					_departSchedule.cancel(false);
					_departSchedule = null;
				}

				_dockedShip.setInDock(0);
				_dockedShip = null;
				_isBusy = false;
			}
		}
		return null;
	}

	protected void validityCheck()
	{
		L2ScriptZone zone = ZoneManager.getInstance().getZoneById(_dockZone, L2ScriptZone.class);
		if(zone == null)
		{
			_log.log(Level.WARN, getName() + ": Invalid zone " + _dockZone + ", controller disabled");
			_isBusy = true;
			return;
		}

		VehiclePathPoint p;
		if(_arrivalPath != null)
		{
			if(_arrivalPath.length == 0)
			{
				_log.log(Level.WARN, getName() + ": Zero arrival path length.");
				_arrivalPath = null;
			}
			else
			{
				p = _arrivalPath[_arrivalPath.length - 1];
				if(!zone.isInsideZone(p.x, p.y, p.z))
				{
					_log.log(Level.WARN, getName() + ": Arrival path finish point (" + p.x + ',' + p.y + ',' + p.z + ") not in zone " + _dockZone);
					_arrivalPath = null;
				}
			}
		}
		if(_arrivalPath == null)
		{
			if(!ZoneManager.getInstance().getZoneById(_dockZone, L2ScriptZone.class).isInsideZone(_shipSpawnX, _shipSpawnY, _shipSpawnZ))
			{
				_log.log(Level.WARN, getName() + ": Arrival path is null and spawn point not in zone " + _dockZone + ", controller disabled");
				_isBusy = true;
				return;
			}
		}

		if(_departPath != null)
		{
			if(_departPath.length == 0)
			{
				_log.log(Level.WARN, getName() + ": Zero depart path length.");
				_departPath = null;
			}
			else
			{
				p = _departPath[_departPath.length - 1];
				if(zone.isInsideZone(p.x, p.y, p.z))
				{
					_log.log(Level.WARN, getName() + ": Departure path finish point (" + p.x + ',' + p.y + ',' + p.z + ") in zone " + _dockZone);
					_departPath = null;
				}
			}
		}

		if(_teleportsTable != null)
		{
			if(_fuelTable == null)
			{
				_log.log(Level.WARN, getName() + ": Fuel consumption not defined.");
			}
			else
			{
				if(_teleportsTable.length == _fuelTable.length)
				{
					AirShipManager.getInstance().registerAirShipTeleportList(_dockZone, _locationId, _teleportsTable, _fuelTable);
				}
				else
				{
					_log.log(Level.WARN, getName() + ": Fuel consumption not match teleport list.");
				}
			}
		}
	}

	private class DecayTask implements Runnable
	{
		@Override
		public void run()
		{
			if(_dockedShip != null)
			{
				_dockedShip.getLocationController().delete();
			}
		}
	}

	private class DepartTask implements Runnable
	{
		@Override
		public void run()
		{
			if(_dockedShip != null && _dockedShip.isInDock() && !_dockedShip.isMoving())
			{
				if(_departPath != null)
				{
					_dockedShip.executePath(_departPath);
				}
				else
				{
					_dockedShip.getLocationController().delete();
				}
			}
		}
	}

}