package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import javolution.util.FastList;

import java.util.stream.Collectors;

public class L2ResidenceTeleportZone extends L2ZoneRespawn
{
	private int _residenceId;

	public L2ResidenceTeleportZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if(name.equals("residenceId"))
		{
			_residenceId = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, true); // FIXME: Custom ?
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false); // FIXME: Custom ?
	}

	@Override
	public void onDieInside(L2Character character)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	/**
	 * Returns all players within this zone
	 * @return
	 */
	public FastList<L2PcInstance> getAllPlayers()
	{
		FastList<L2PcInstance> players = getCharactersInside().stream().filter(temp -> temp instanceof L2PcInstance).map(temp -> (L2PcInstance) temp).collect(Collectors.toCollection(FastList::new));

		return players;
	}

	public void oustAllPlayers()
	{
		if(_characterList == null)
		{
			return;
		}
		if(_characterList.isEmpty())
		{
			return;
		}
		for(L2Character character : getCharactersInside())
		{
			if(character == null)
			{
				continue;
			}
			if(character instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) character;
				if(player.isOnline())
				{
					player.teleToLocation(getSpawnLoc(), 200);
				}
			}
		}
	}

	public int getResidenceId()
	{
		return _residenceId;
	}
}
