package dwo.gameserver.model.world.olympiad;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerOlympiadSide;
import dwo.gameserver.model.skills.stats.StatsSet;

public class Participant
{
	private final int _objectId;
	private final String _name;
	private final PlayerOlympiadSide _side;
	private final int _baseClass;
	private final StatsSet _stats;
	private L2PcInstance _player;
	private boolean _disconnected;
	private boolean _defaulted;
	private String _clanName;
	private int _clanId;

	public Participant(L2PcInstance plr, PlayerOlympiadSide olympiadSide)
	{
		_objectId = plr.getObjectId();
		_player = plr;
		_name = plr.getName();
		_side = olympiadSide;
		_baseClass = plr.getBaseClassId();
		_stats = Olympiad.getInstance().getNobleStats(plr);
		_clanName = plr.getClan() != null ? plr.getClan().getName() : "";
		_clanId = plr.getClanId();
	}

	public void updatePlayer()
	{
		if(_player == null || !_player.isOnline())
		{
			_player = WorldManager.getInstance().getPlayer(_objectId);
		}
	}

	public void updateStat(String statName, int increment)
	{
		_stats.set(statName, Math.max(_stats.getInteger(statName) + increment, 0));
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public L2PcInstance getPlayer()
	{
		return _player;
	}

	public void setPlayer(L2PcInstance player)
	{
		_player = player;
	}

	public String getName()
	{
		return _name;
	}

	public PlayerOlympiadSide getSide()
	{
		return _side;
	}

	public int getBaseClass()
	{
		return _baseClass;
	}

	public boolean isDisconnected()
	{
		return _disconnected;
	}

	public void setDisconnected(boolean disconnected)
	{
		_disconnected = disconnected;
	}

	public boolean isDefaulted()
	{
		return _defaulted;
	}

	public void setDefaulted(boolean defaulted)
	{
		_defaulted = defaulted;
	}

	public StatsSet getStats()
	{
		return _stats;
	}

	public String getClanName()
	{
		return _clanName;
	}

	public void setClanName(String clanName)
	{
		_clanName = clanName;
	}

	public int getClanId()
	{
		return _clanId;
	}

	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}
}