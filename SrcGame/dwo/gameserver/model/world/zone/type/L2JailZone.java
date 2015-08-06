package dwo.gameserver.model.world.zone.type;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 * @author durgus
 */

public class L2JailZone extends L2ZoneType
{
	public L2JailZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_JAIL, true);
			if(Config.JAIL_IS_PVP)
			{
				character.setInsideZone(L2Character.ZONE_PVP, true);
				character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
			}
			if(Config.JAIL_DISABLE_TRANSACTION)
			{
				character.setInsideZone(L2Character.ZONE_NOSTORE, true);
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_JAIL, false);
			if(Config.JAIL_IS_PVP)
			{
				character.setInsideZone(L2Character.ZONE_PVP, false);
				character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
			}
			if(((L2PcInstance) character).isInJail())
			{
				// when a player wants to exit jail even if he is still jailed, teleport him back to jail
				ThreadPoolManager.getInstance().scheduleGeneral(new BackToJail(character), 2000);
				character.sendMessage("You cannot cheat your way out of here. You must wait until your jail time is over.");
			}
			if(Config.JAIL_DISABLE_TRANSACTION)
			{
				character.setInsideZone(L2Character.ZONE_NOSTORE, false);
			}
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

	static class BackToJail implements Runnable
	{
		private L2PcInstance _activeChar;

		BackToJail(L2Character character)
		{
			_activeChar = (L2PcInstance) character;
		}

		@Override
		public void run()
		{
			_activeChar.teleToLocation(-114356, -249645, -2984); // Jail
		}
	}
}
