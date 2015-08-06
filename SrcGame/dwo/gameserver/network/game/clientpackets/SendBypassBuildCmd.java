package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.AdminTable;
import dwo.gameserver.handler.AdminCommandHandler;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.util.GMAudit;
import org.apache.log4j.Level;

public class SendBypassBuildCmd extends L2GameClientPacket
{
	public static final int GM_MESSAGE = 9;
	public static final int ANNOUNCEMENT = 10;

	private String _command;

	@Override
	protected void readImpl()
	{
		_command = readS();
		if(_command != null)
		{
			_command = _command.trim();
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		String command = "admin_" + _command.split(" ")[0];

		IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);

		if(ach == null)
		{
			if(activeChar.isGM())
			{
				activeChar.sendMessage("The command " + command.substring(6) + " does not exists!");
			}

			_log.log(Level.WARN, "No handler registered for admin command '" + command + '\'');
			return;
		}

		if(!AdminTable.getInstance().hasAccess(command, activeChar.getAccessLevel()))
		{
			activeChar.sendMessage("You don't have the access right to use this command!");
			_log.log(Level.WARN, "Character " + activeChar.getName() + " tryed to use admin command " + command + ", but have no access to it!");
			return;
		}

		if(Config.GMAUDIT)
		{
			GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + ']', _command, activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target");
		}

		ach.useAdminCommand("admin_" + _command, activeChar);
	}

	@Override
	public String getType()
	{
		return "[C] 5b SendBypassBuildCmd";
	}
}
