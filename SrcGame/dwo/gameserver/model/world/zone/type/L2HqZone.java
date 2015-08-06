package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.L2ZoneType;

/**
 * Zone where 'Build Headquarters' is allowed.
 *
 * @author Gnacik
 */
public class L2HqZone extends L2ZoneType
{
	public L2HqZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{

	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_HQ, true);
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_HQ, false);
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
