package dwo.gameserver.model.world.residence.clanhall.type;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2SiegeClan;
import dwo.gameserver.model.player.formation.clan.L2SiegeClan.SiegeClanType;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.residence.clanhall.ClanHall;
import dwo.gameserver.model.world.residence.clanhall.ClanHallSiegeEngine;
import dwo.gameserver.model.world.residence.clanhall.ClanHallSiegeStatus;
import dwo.gameserver.model.world.zone.type.L2SiegableHallZone;
import dwo.gameserver.model.world.zone.type.L2SiegeZone;
import dwo.gameserver.network.game.serverpackets.CastleSiegeInfo;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.Calendar;
import java.util.List;

/**
 * @author BiggBoss
 */

public class ClanHallSiegable extends ClanHall
{
	private static final String SQL_SAVE = "UPDATE siegable_clanhall SET ownerId=?, nextSiege=? WHERE clanHallId=?";

	protected List<String> _doorDefault;

	private Calendar _nextSiege;
	private long _siegeLength;
	private int[] _scheduleConfig = {7, 0, 0, 12, 0};

	private ClanHallSiegeStatus _statusClanHall = ClanHallSiegeStatus.REGISTERING;
	private L2SiegeZone _siegeZone;

	private ClanHallSiegeEngine _siege;

	public ClanHallSiegable(StatsSet set)
	{
		super(set);
		_doorDefault = new FastList<>();
		_siegeLength = set.getLong("siegeLenght");
		String[] rawSchConfig = set.getString("scheduleConfig").split(";");
		if(rawSchConfig.length == 5)
		{
			for(int i = 0; i < 5; i++)
			{
				try
				{
					_scheduleConfig[i] = Integer.parseInt(rawSchConfig[i]);
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "ClanHallSiegable - " + getName() + ": Wrong schedule_config parameters!");
				}
			}
		}
		else
		{
			_log.log(Level.ERROR, getName() + ": Wrong schedule_config value in siegable_halls table, using default (7 days)");
		}

		_nextSiege = Calendar.getInstance();
		long nextSiege = set.getLong("nextSiege");
		if(nextSiege - System.currentTimeMillis() < 0)
		{
			updateNextSiege();
		}
		else
		{
			_nextSiege.setTimeInMillis(nextSiege);
		}
	}

	public List<String> getDoorDefault()
	{
		return _doorDefault;
	}

	public void spawnDoor()
	{
		spawnDoor(false);
	}

	public void spawnDoor(boolean isDoorWeak)
	{
		for(int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);
			if(door.getCurrentHp() <= 0)
			{
				door.getLocationController().decay(); // Kill current if not killed already
				door = DoorGeoEngine.getInstance().newInstance(door.getDoorId(), true);
				if(isDoorWeak)
				{
					door.setCurrentHp(door.getMaxHp() / 2);
				}
				door.getLocationController().spawn(door.getX(), door.getY(), door.getZ());
				getDoors().set(i, door);
			}
			else if(door.isOpened())
			{
				door.closeMe();
			}
		}
	}

	public ClanHallSiegeEngine getSiege()
	{
		return _siege;
	}

	public void setSiege(ClanHallSiegeEngine siegable)
	{
		_siege = siegable;
		_siegeZone.setSiegeInstance(siegable);
	}

	public Calendar getSiegeDate()
	{
		return _nextSiege;
	}

	public long getNextSiegeTime()
	{
		return _nextSiege.getTimeInMillis();
	}

	public long getSiegeLenght()
	{
		return _siegeLength;
	}

	public void setNextSiegeDate(long date)
	{
		_nextSiege.setTimeInMillis(date);
	}

	public void setNextSiegeDate(Calendar c)
	{
		_nextSiege = c;
	}

	public void updateNextSiege()
	{
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, _scheduleConfig[0]);
		c.add(Calendar.MONTH, _scheduleConfig[1]);
		c.add(Calendar.YEAR, _scheduleConfig[2]);
		c.set(Calendar.HOUR_OF_DAY, _scheduleConfig[3]);
		c.set(Calendar.MINUTE, _scheduleConfig[4]);
		c.set(Calendar.SECOND, 0);
		_nextSiege = c;
		updateDb();
	}

	public void addAttacker(L2Clan clan)
	{
		if(_siege != null)
		{
			_siege.getAttackers().put(clan.getClanId(), new L2SiegeClan(clan.getClanId(), SiegeClanType.ATTACKER));
		}
	}

	public void removeAttacker(L2Clan clan)
	{
		if(_siege != null)
		{
			_siege.getAttackers().remove(clan.getClanId());
		}
	}

	public boolean isRegistered(L2Clan clan)
	{
		return _siege != null && _siege.checkIsAttacker(clan);
	}

	public ClanHallSiegeStatus getSiegeStatus()
	{
		return _statusClanHall;
	}

	public boolean isRegistering()
	{
		return _statusClanHall == ClanHallSiegeStatus.REGISTERING;
	}

	public boolean isInSiege()
	{
		return _statusClanHall == ClanHallSiegeStatus.RUNNING;
	}

	public boolean isWaitingBattle()
	{
		return _statusClanHall == ClanHallSiegeStatus.WAITING_BATTLE;
	}

	public void updateSiegeStatus(ClanHallSiegeStatus statusClanHall)
	{
		_statusClanHall = statusClanHall;
	}

	public L2SiegeZone getSiegeZone()
	{
		return _siegeZone;
	}

	public void setSiegeZone(L2SiegeZone zone)
	{
		_siegeZone = zone;
	}

	public void updateSiegeZone(boolean active)
	{
		_siegeZone.setIsSiegeActive(active);
	}

	public void showSiegeInfo(L2PcInstance player)
	{
		player.sendPacket(new CastleSiegeInfo(this));
	}

	@Override
	public L2SiegableHallZone getZone()
	{
		return (L2SiegableHallZone) super.getZone();
	}

	@Override
	public boolean isSiegableHall()
	{
		return true;
	}

	@Override
	public void updateDb()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SQL_SAVE);
			statement.setInt(1, getOwnerId());
			statement.setLong(2, getNextSiegeTime());
			statement.setInt(3, getId());
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "Exception: ClanHallSiegable.updateDb(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}
}
