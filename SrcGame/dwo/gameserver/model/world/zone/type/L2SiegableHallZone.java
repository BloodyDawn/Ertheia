package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.world.zone.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * @author BiggBoss
 */
public class L2SiegableHallZone extends L2ClanHallZone
{
	private List<Location> _challengerLocations;

	public L2SiegableHallZone(int id)
	{
		super(id);
	}

	@Override
	public void parseLoc(int x, int y, int z, String type)
	{
		if("challenger".equals(type))
		{
			if(_challengerLocations == null)
			{
				_challengerLocations = new ArrayList<>();
			}
			_challengerLocations.add(new Location(x, y, z));
		}
		else
		{
			super.parseLoc(x, y, z, type);
		}
	}

	public List<Location> getChallengerSpawns()
	{
		return _challengerLocations;
	}

	public void banishNonSiegeParticipants()
	{
		TeleportWhereType type = TeleportWhereType.CLANHALL_BANISH;
		getPlayersInside().stream().filter(player -> player != null && player.isInHideoutSiege()).forEach(player -> player.teleToLocation(type));
	}
}