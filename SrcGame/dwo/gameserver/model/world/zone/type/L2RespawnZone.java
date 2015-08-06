package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.world.zone.L2ZoneType;
import javolution.util.FastMap;

import java.util.Map;

/**
 * @author Nyaran
 */
public class L2RespawnZone extends L2ZoneType
{
	private Map<Race, String> _raceRespawnPoint = new FastMap<>();

	public L2RespawnZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
	}

	@Override
	protected void onExit(L2Character character)
	{
	}

	@Override
	public void onDieInside(L2Character character)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	public void addRaceRespawnPoint(String race, String point)
	{
		_raceRespawnPoint.put(Race.valueOf(race), point);
	}

	public Map<Race, String> getAllRespawnPoints()
	{
		return _raceRespawnPoint;
	}

	public String getRespawnPoint(L2PcInstance activeChar)
	{
		return _raceRespawnPoint.get(activeChar.getRace());
	}
}
