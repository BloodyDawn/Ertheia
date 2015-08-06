package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 * @author durgus
 */

public class L2ArenaZone extends L2ZoneType
{

	public L2ArenaZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			if(!character.isInsideZone(L2Character.ZONE_PVP))
			{
				character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
			}
		}
		character.setInsideZone(L2Character.ZONE_PVP, true);
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			if(!character.isInsideZone(L2Character.ZONE_PVP))
			{
				character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
			}
		}
		character.setInsideZone(L2Character.ZONE_PVP, false);
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
