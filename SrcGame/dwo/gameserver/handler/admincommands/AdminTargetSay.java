package dwo.gameserver.handler.admincommands;

import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2StaticObjectInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.Say2;

public class AdminTargetSay implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_targetsay"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(command.startsWith("admin_targetsay"))
		{
			try
			{
				L2Object obj = activeChar.getTarget();
				if(obj instanceof L2StaticObjectInstance || !(obj instanceof L2Character))
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}

				String message = command.substring(16);
				L2Character target = (L2Character) obj;

				target.broadcastPacket(new Say2(target.getObjectId(), ChatType.ALL, target.getName(), message));
			}
			catch(StringIndexOutOfBoundsException e)
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_SYNTAX);
				return false;
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
