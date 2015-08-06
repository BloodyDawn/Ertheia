package dwo.gameserver.handler.admincommands;

import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 02.07.12
 * Time: 22:59
 */

public class AdminHardwareInfo implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_hwinfo"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		showMenu(activeChar);
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void showMenu(L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar.getLang(), "mods/admin/hardwareinfo.htm");
		html.replace("%core_count%", String.valueOf(Runtime.getRuntime().availableProcessors()));
		html.replace("%free_memory%", String.valueOf(Runtime.getRuntime().freeMemory()));
		html.replace("%all_memory%", String.valueOf(Runtime.getRuntime().maxMemory()));
		html.replace("%jvm_memory%", String.valueOf(Runtime.getRuntime().totalMemory()));
		activeChar.sendPacket(html);
	}
}