package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.world.zone.L2ZoneType;

/**
 * @author durgus
 */

public class L2DerbyTrackZone extends L2ZoneType
{
	public L2DerbyTrackZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(character instanceof L2Playable)
		{
			character.setInsideZone(L2Character.ZONE_MONSTERTRACK, true);
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(character instanceof L2Playable)
		{
			character.setInsideZone(L2Character.ZONE_MONSTERTRACK, false);
		}
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
