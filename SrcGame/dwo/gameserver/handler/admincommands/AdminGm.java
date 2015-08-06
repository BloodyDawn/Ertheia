package dwo.gameserver.handler.admincommands;

import dwo.gameserver.datatables.xml.AdminTable;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class AdminGm implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_gm"
	};
	private static Logger _log = LogManager.getLogger(AdminGm.class);

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(command.equals("admin_gm") && activeChar.isGM())
		{
			AdminTable.getInstance().deleteGm(activeChar);
			activeChar.setAccessLevel(0);
			activeChar.sendMessage("Вы потеряли статус Гейм Мастера.");
			_log.log(Level.INFO, "GM: " + activeChar.getName() + '(' + activeChar.getObjectId() + ") turned his GM status off");
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
