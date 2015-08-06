package dwo.gameserver.model.world.zone.type;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerSiegeSide;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 * @author durgus
 */

public class L2PeaceZone extends L2ZoneType
{
	public L2PeaceZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			if(!character.isInsideZone(L2Character.ZONE_PEACE))
			{
				character.sendPacket(SystemMessageId.ENTER_PEACEFUL_ZONE);
			}

			// PVP possible during siege, now for siege participants only
			// Could also check if this town is in siege, or if any siege is going on
			if(((L2PcInstance) character).getSiegeSide() != PlayerSiegeSide.NONE && Config.PEACE_ZONE_MODE == 1)
			{
				return;
			}
		}

		if(Config.PEACE_ZONE_MODE != 2)
		{
			character.setInsideZone(L2Character.ZONE_PEACE, true);
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			if(!character.isInsideZone(L2Character.ZONE_PEACE))
			{
				character.sendPacket(SystemMessageId.EXIT_PEACEFUL_ZONE);
			}
		}
		character.setInsideZone(L2Character.ZONE_PEACE, false);
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