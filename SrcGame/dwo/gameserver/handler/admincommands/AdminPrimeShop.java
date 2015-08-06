package dwo.gameserver.handler.admincommands;

import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.util.Util;

import java.util.StringTokenizer;

public class AdminPrimeShop implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_addprimepoints", "admin_removeprimepoints", "admin_setprimepoints", "admin_getprimepoints",
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		String cmd = st.nextToken();

		if(ADMIN_COMMANDS[0].equals(cmd))
		{
			if(activeChar.getTarget() instanceof L2PcInstance && st.hasMoreTokens())
			{
				L2PcInstance target = L2PcInstance.class.cast(activeChar.getTarget());
				String next = st.nextToken();
				if(Util.isDigit(next))
				{
					int points = Integer.parseInt(next);
					target.setGamePoints(activeChar.getGamePoints() + points, true);
					activeChar.sendMessage("You've added " + points + " points to " + target.getName());
				}
			}
			else
			{
				activeChar.sendMessage("Your target cannot be found or you haven't specifed amount of points to be added.");
			}
		}
		else if(ADMIN_COMMANDS[1].equals(cmd))
		{
			if(activeChar.getTarget() instanceof L2PcInstance && st.hasMoreTokens())
			{
				L2PcInstance target = L2PcInstance.class.cast(activeChar.getTarget());
				String next = st.nextToken();
				if(Util.isDigit(next))
				{
					int points = Integer.parseInt(next);
					target.setGamePoints(activeChar.getGamePoints() - points, true);
					activeChar.sendMessage("You've remove " + points + " points from " + target.getName());
				}
			}
			else
			{
				activeChar.sendMessage("Your target cannot be found or you haven't specifed amount of points to be removed.");
			}
		}
		else if(ADMIN_COMMANDS[2].equals(cmd))
		{
			if(activeChar.getTarget() instanceof L2PcInstance && st.hasMoreTokens())
			{
				L2PcInstance target = L2PcInstance.class.cast(activeChar.getTarget());
				String next = st.nextToken();
				if(Util.isDigit(next))
				{
					int points = Integer.parseInt(next);
					target.setGamePoints(points, true);
					activeChar.sendMessage("You've set " + points + " points to " + target.getName());
				}
			}
			else
			{
				activeChar.sendMessage("Your target cannot be found or you haven't specifed amount of points to be set.");
			}
		}
		else if(ADMIN_COMMANDS[3].equals(cmd))
		{
			if(activeChar.getTarget() instanceof L2PcInstance)
			{
				L2PcInstance target = L2PcInstance.class.cast(activeChar.getTarget());
				activeChar.sendMessage(target.getName() + " now have " + target.getGamePoints() + " Prime Shop points.");
			}
			else
			{
				activeChar.sendMessage("Your target cannot be found.");
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