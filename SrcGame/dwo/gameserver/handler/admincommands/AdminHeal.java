package dwo.gameserver.handler.admincommands;

import dwo.config.Config;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class AdminHeal implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_heal"
	};
	private static Logger _log = LogManager.getLogger(AdminHeal.class);

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.equals("admin_heal"))
		{
			handleHeal(activeChar);
		}
		else if(command.startsWith("admin_heal"))
		{
			try
			{
				String healTarget = command.substring(11);
				handleHeal(activeChar, healTarget);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				if(Config.DEVELOPER)
				{
					_log.log(Level.DEBUG, "Heal error: " + e);
				}
				activeChar.sendMessage("Incorrect target/radius specified.");
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void handleHeal(L2PcInstance activeChar)
	{
		handleHeal(activeChar, null);
	}

	private void handleHeal(L2PcInstance activeChar, String player)
	{

		L2Object obj = activeChar.getTarget();
		if(player != null)
		{
			L2PcInstance plyr = WorldManager.getInstance().getPlayer(player);

			if(plyr != null)
			{
				obj = plyr;
			}
			else
			{
				try
				{
					int radius = Integer.parseInt(player);
					activeChar.getKnownList().getKnownObjects().values().stream().filter(object -> object instanceof L2Character).forEach(object -> {
						L2Character character = (L2Character) object;
						character.setCurrentHpMp(character.getMaxHp(), character.getMaxMp());
						if(object instanceof L2PcInstance)
						{
							character.setCurrentCp(character.getMaxCp());
						}
					});
					activeChar.sendMessage("Healed within " + radius + " unit radius.");
					return;
				}
				catch(NumberFormatException nbe)
				{
					// Ignored
				}
			}
		}
		if(obj == null)
		{
			obj = activeChar;
		}
		if(obj instanceof L2Character)
		{
			L2Character target = (L2Character) obj;
			target.setCurrentHpMp(target.getMaxHp(), target.getMaxMp());
			if(target instanceof L2PcInstance)
			{
				target.setCurrentCp(target.getMaxCp());
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
	}
}
