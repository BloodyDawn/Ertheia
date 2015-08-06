package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2SiegeClan;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.residence.function.FunctionType;
import dwo.gameserver.model.world.zone.Location;
import org.apache.log4j.Level;

public class RequestRestartPoint extends L2GameClientPacket
{
	protected int _requestedPointType;

	@Override
	protected void readImpl()
	{
		_requestedPointType = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}

		if(EventManager.isStarted() && EventManager.isPlayerParticipant(activeChar))
		{
			return;
		}

		if(activeChar.isFakeDeath())
		{
			activeChar.stopFakeDeath(true);
			return;
		}
		if(!activeChar.isDead())
		{
			_log.log(Level.WARN, "Living player [" + activeChar.getName() + "] called RestartPointPacket! Ban this player!");
			return;
		}

		Castle castle = CastleManager.getInstance().getCastle(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		if(castle != null && castle.getSiege().isInProgress())
		{
			if(activeChar.getClan() != null && castle.getSiege().checkIsAttacker(activeChar.getClan()))
			{
				// Schedule respawn delay for attacker
				ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), castle.getSiege().getAttackerRespawnDelay());
				if(castle.getSiege().getAttackerRespawnDelay() > 0)
				{
					activeChar.sendMessage("You will be re-spawned in " + castle.getSiege().getAttackerRespawnDelay() / 1000 + " seconds");
				}
				return;
			}
		}

		portPlayer(activeChar);
	}

	@Override
	public String getType()
	{
		return "[C] 6D RequestRestartPoint";
	}

	private void portPlayer(L2PcInstance activeChar)
	{
		Location loc = null;
		Castle castle = null;
		Fort fort = null;
		ClanHallSiegable hall = null;
		boolean isInDefense = false;
		int instanceId = 0;

		// force jail
		if(activeChar.isInJail())
		{
			_requestedPointType = 27;
		}
		switch(_requestedPointType)
		{
			case 1: // to clanhall
				if(activeChar.getClan() == null || activeChar.getClan().getClanhallId() == 0)
				{
					_log.log(Level.WARN, "Player [" + activeChar.getName() + "] called RestartPointPacket - To Clanhall and he doesn't have Clanhall!");
					return;
				}
				loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CLANHALL);

				if(ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()) != null && ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(FunctionType.XP_RESTORE) != null)
				{
					activeChar.restoreExp(ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunctionLevel(FunctionType.XP_RESTORE));
				}
				break;

			case 2: // to castle
				castle = CastleManager.getInstance().getCastle(activeChar);

				if(castle != null && castle.getSiege().isInProgress())
				{
					// Siege in progress
					if(castle.getSiege().checkIsDefender(activeChar.getClan()))
					{
						loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CASTLE);
					}
					// Just in case you lost castle while being dead.. Port to nearest Town.
					else if(castle.getSiege().checkIsAttacker(activeChar.getClan()))
					{
						loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.TOWN);
					}
					else
					{
						_log.log(Level.WARN, "Player [" + activeChar.getName() + "] called RestartPointPacket - To Castle and he doesn't have Castle!");
						return;
					}
				}
				else
				{
					if(activeChar.getClan() == null || activeChar.getClan().getCastleId() == 0)
					{
						return;
					}
					loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CASTLE);
				}
				if(CastleManager.getInstance().getCastleByOwner(activeChar.getClan()) != null && CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).getFunction(FunctionType.XP_RESTORE) != null)
				{
					activeChar.restoreExp(CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).getFunctionLevel(FunctionType.XP_RESTORE));
				}
				break;

			case 3: // to fortress
				//fort = FortManager.getInstance().getFort(activeChar);

				if((activeChar.getClan() == null || activeChar.getClan().getFortId() == 0) && !isInDefense)
				{
					_log.log(Level.WARN, "Player [" + activeChar.getName() + "] called RestartPointPacket - To Fortress and he doesn't have Fortress!");
					return;
				}
				loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.FORTRESS);
				if(FortManager.getInstance().getFortByOwner(activeChar.getClan()) != null && FortManager.getInstance().getFortByOwner(activeChar.getClan()).getFunction(FunctionType.XP_RESTORE) != null)
				{
					activeChar.restoreExp(FortManager.getInstance().getFortByOwner(activeChar.getClan()).getFunctionLevel(FunctionType.XP_RESTORE));
				}
				break;

			case 4: // to siege HQ
				L2SiegeClan siegeClan = null;
				castle = CastleManager.getInstance().getCastle(activeChar);
				fort = FortManager.getInstance().getFort(activeChar);
				hall = ClanHallSiegeManager.getInstance().getNearbyClanHall(activeChar);

				if(castle != null && castle.getSiege().isInProgress())
				{
					siegeClan = castle.getSiege().getAttackerClan(activeChar.getClan());
				}
				else if(fort != null && fort.getSiege().isInProgress())
				{
					siegeClan = fort.getSiege().getAttackerClan(activeChar.getClan());
				}
				else if(hall != null && hall.isInSiege())
				{
					siegeClan = hall.getSiege().getAttackerClan(activeChar.getClan());
				}
				if(siegeClan == null || siegeClan.getFlag().isEmpty())
				{
					// Check if clan hall has inner spawns loc
					if(hall != null && (loc = hall.getSiege().getInnerSpawnLoc(activeChar)) != null)
					{
						break;
					}

					_log.log(Level.WARN, "Player [" + activeChar.getName() + "] called RestartPointPacket - To Siege HQ and he doesn't have Siege HQ!");
					return;
				}
				loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.SIEGE_FLAG);
				break;

			case 5: // Fixed or Player is a festival participant
				if(!activeChar.isGM() && !activeChar.canUseFeatherOfBlessing())
				{
					_log.log(Level.WARN, "Player [" + activeChar.getName() + "] called RestartPointPacket - Fixed and he isn't festival participant!");
					return;
				}
				if(activeChar.canUseFeatherOfBlessing())
				{
					activeChar.useFeatherOfBlessing();
				}
				activeChar.doRevive(100); // TODO: Confirm.
				return;
			case 6: // TODO: agathion ress
				break;
			case 27: // to jail
				if(!activeChar.isInJail())
				{
					return;
				}
				loc = new Location(-114356, -249645, -2984);
				break;
			default:
				loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.TOWN);
				break;
		}

		// Teleport and revive
		if(loc != null)
		{
			activeChar.getInstanceController().setInstanceId(instanceId);
			activeChar.setIsPendingRevive(true);
			activeChar.teleToLocation(loc, true);
		}
	}

	private class DeathTask implements Runnable
	{
		final L2PcInstance activeChar;

		private DeathTask(L2PcInstance _activeChar)
		{
			activeChar = _activeChar;
		}

		@Override
		public void run()
		{
			portPlayer(activeChar);
		}
	}
}