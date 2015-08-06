package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 26.02.12
 * Time: 14:00
 */

public class L2TeleportZone extends L2ZoneType
{
	private Location _tpLocation;

	public L2TeleportZone(int id)
	{
		super(id);
		setTargetType(L2PcInstance.class); // default only playabale
	}

	@Override
	public void setParameter(String name, String value)
	{
		if(name.equals("teleportTo"))
		{
			_tpLocation = new Location(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			character.teleToLocation(_tpLocation);
		}
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
}