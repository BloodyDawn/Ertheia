package dwo.scripts.vehicles.airship;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.vehicle.AirShipManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2AirShipInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.VehiclePathPoint;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NS;
import org.apache.log4j.Level;

public class AirShipGludioGracia extends Quest implements Runnable
{
	private static final int[] CONTROLLERS = {32607, 32609};

	private static final int GLUDIO_DOCK_ID = 10;
	private static final int GRACIA_DOCK_ID = 11;

	private static final Location OUST_GLUDIO = new Location(-149379, 255246, -80);
	private static final Location OUST_GRACIA = new Location(-186563, 243590, 2608);

	private static final VehiclePathPoint[] GLUDIO_TO_WARPGATE = {
		new VehiclePathPoint(-151202, 252556, 231), new VehiclePathPoint(-160403, 256144, 222),
		new VehiclePathPoint(-167874, 256731, -509, 0, 41035) // teleport: x,y,z,speed=0,heading
	};

	private static final VehiclePathPoint[] WARPGATE_TO_GRACIA = {
		new VehiclePathPoint(-169763, 254815, 282), new VehiclePathPoint(-171822, 250061, 425),
		new VehiclePathPoint(-172595, 247737, 398), new VehiclePathPoint(-174538, 246185, 39),
		new VehiclePathPoint(-179440, 243651, 1337), new VehiclePathPoint(-182601, 243957, 2739),
		new VehiclePathPoint(-184952, 245122, 2694), new VehiclePathPoint(-186936, 244563, 2617)
	};

	private static final VehiclePathPoint[] GRACIA_TO_WARPGATE = {
		new VehiclePathPoint(-187801, 244997, 2672), new VehiclePathPoint(-188520, 245932, 2465),
		new VehiclePathPoint(-189932, 245243, 1682), new VehiclePathPoint(-191192, 242969, 1523),
		new VehiclePathPoint(-190408, 239088, 1706), new VehiclePathPoint(-187475, 237113, 2768),
		new VehiclePathPoint(-184673, 238433, 2802), new VehiclePathPoint(-184524, 241119, 2816),
		new VehiclePathPoint(-182129, 243385, 2733), new VehiclePathPoint(-179440, 243651, 1337),
		new VehiclePathPoint(-174538, 246185, 39), new VehiclePathPoint(-172595, 247737, 398),
		new VehiclePathPoint(-171822, 250061, 425), new VehiclePathPoint(-169763, 254815, 282),
		new VehiclePathPoint(-168067, 256626, 343), new VehiclePathPoint(-157261, 255664, 221, 0, 64781)
		// teleport: x,y,z,speed=0,heading
	};

	private static final VehiclePathPoint[] WARPGATE_TO_GLUDIO = {
		new VehiclePathPoint(-153414, 255385, 221), new VehiclePathPoint(-149548, 258172, 221),
		new VehiclePathPoint(-146884, 257097, 221), new VehiclePathPoint(-146672, 254239, 221),
		new VehiclePathPoint(-147855, 252712, 206), new VehiclePathPoint(-149378, 252552, 198)
	};

	private final L2AirShipInstance _ship;
	private int _cycle;

	private boolean _foundAtcGludio;
	private L2Npc _atcGludio;
	private boolean _foundAtcGracia;
	private L2Npc _atcGracia;

	public AirShipGludioGracia()
	{
		addAskId(CONTROLLERS, -1724);
		_ship = AirShipManager.getInstance().getNewAirShip(-149378, 252552, 198, 33837);
		_ship.setOustLoc(OUST_GLUDIO);
		_ship.setInDock(GLUDIO_DOCK_ID);
		_ship.registerEngine(this);
		_ship.runEngine(60000);
	}

	public static void main(String[] args)
	{
		new AirShipGludioGracia();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -1724)
		{
			if(reply == 4)
			{
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

				if(_ship.isInDock() && _ship.isInsideRadius(player, 600, true, false))
				{
					_ship.addPassenger(player);
				}
			}
			else if(reply == 1)
			{
				player.teleToLocation(-212808, 209672, 4257);
			}
			else if(reply == 2)
			{
				player.teleToLocation(-248536, 250280, 4311);
			}
			else if(reply == 3)
			{
				player.teleToLocation(-175512, 154488, 2689);
			}
			else if(reply == 5)
			{
				player.teleToLocation(-147348, 152615, -14048);
			}
		}
		return null;
	}

	@Override
	public boolean unload(boolean removeFromList)
	{
		if(_ship != null)
		{
			_ship.oustPlayers();
			_ship.getLocationController().delete();
		}
		return super.unload(removeFromList);
	}

	@Override
	public void run()
	{
		try
		{
			switch(_cycle)
			{
				case 0:
					broadcastInGludio(NpcStringId.THE_REGULARLY_SCHEDULED_AIRSHIP_THAT_FLIES_TO_THE_GRACIA_CONTINENT_HAS_DEPARTED); // The regularly scheduled airship that flies to the Gracia continent has departed.
					_ship.setInDock(0);
					_ship.executePath(GLUDIO_TO_WARPGATE);
					break;
				case 1:
					//_ship.teleToLocation(-167874, 256731, -509, 41035, false);
					_ship.setOustLoc(OUST_GRACIA);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 5000);
					break;
				case 2:
					_ship.executePath(WARPGATE_TO_GRACIA);
					break;
				case 3:
					broadcastInGracia(NpcStringId.THE_REGULARLY_SCHEDULED_AIRSHIP_HAS_ARRIVED_IT_WILL_DEPART_FOR_THE_ADEN_CONTINENT_IN_1_MINUTE); // The regularly scheduled airship has arrived. It will depart for the Aden continent in 1 minute.
					_ship.setInDock(GRACIA_DOCK_ID);
					_ship.oustPlayers();
					ThreadPoolManager.getInstance().scheduleGeneral(this, 60000);
					break;
				case 4:
					broadcastInGracia(NpcStringId.THE_REGULARLY_SCHEDULED_AIRSHIP_THAT_FLIES_TO_THE_ADEN_CONTINENT_HAS_DEPARTED); // The regularly scheduled airship that flies to the Aden continent has departed.
					_ship.setInDock(0);
					_ship.executePath(GRACIA_TO_WARPGATE);
					break;
				case 5:
					//					_ship.teleToLocation(-157261, 255664, 221, 64781, false);
					_ship.setOustLoc(OUST_GLUDIO);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 5000);
					break;
				case 6:
					_ship.executePath(WARPGATE_TO_GLUDIO);
					break;
				case 7:
					broadcastInGludio(NpcStringId.THE_REGULARLY_SCHEDULED_AIRSHIP_HAS_ARRIVED_IT_WILL_DEPART_FOR_THE_GRACIA_CONTINENT_IN_1_MINUTE); // The regularly scheduled airship has arrived. It will depart for the Gracia continent in 1 minute.
					_ship.setInDock(GLUDIO_DOCK_ID);
					_ship.oustPlayers();
					ThreadPoolManager.getInstance().scheduleGeneral(this, 60000);
					break;
			}
			_cycle++;
			if(_cycle > 7)
			{
				_cycle = 0;
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
	}

	private void broadcastInGludio(NpcStringId msg)
	{
		if(!_foundAtcGludio)
		{
			_foundAtcGludio = true;
			_atcGludio = findController();
		}
		if(_atcGludio != null)
		{
			_atcGludio.broadcastPacket(new NS(_atcGludio.getObjectId(), ChatType.SHOUT, _atcGludio.getNpcId(), msg));
		}
	}

	private void broadcastInGracia(NpcStringId msg)
	{
		if(!_foundAtcGracia)
		{
			_foundAtcGracia = true;
			_atcGracia = findController();
		}
		if(_atcGracia != null)
		{
			_atcGracia.broadcastPacket(new NS(_atcGracia.getObjectId(), ChatType.SHOUT, _atcGracia.getNpcId(), msg));
		}
	}

	private L2Npc findController()
	{
		// check objects around the ship
		for(L2Object obj : WorldManager.getInstance().getVisibleObjects(_ship, 600))
		{
			if(obj instanceof L2Npc)
			{
				for(int id : CONTROLLERS)
				{
					if(((L2Npc) obj).getNpcId() == id)
					{
						return (L2Npc) obj;
					}
				}
			}
		}
		return null;
	}
}