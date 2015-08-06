package dwo.gameserver.util;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GMAudit
{
	private static final Logger _log = LogManager.getLogger(GMAudit.class);

	public static void auditAction(String fullCommand, L2PcInstance activeChar, String target)
	{
		if(!Config.GMAUDIT)
		{
			return;
		}

		String[] command = fullCommand.split(" ");

		auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + ']', command[0], target.isEmpty() ? "no-target" : target, command.length > 2 ? command[2] : "");
	}

	/**
	 * @param gmName
	 * @param action
	 * @param target
	 * @param params
	 */
	public static void auditGMAction(String gmName, String action, String target, String params)
	{
		File file = new File("log/GMAudit/" + gmName + ".txt");
		SimpleDateFormat _formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
		try(FileWriter save = new FileWriter(file, true))
		{
			save.write(_formatter.format(new Date()) + '>' + gmName + '>' + action + '>' + target + '>' + params + "\r\n");
		}
		catch(IOException e)
		{
			_log.log(Level.ERROR, "GMAudit for GM " + gmName + " could not be saved: ", e);
		}
	}

	/**
	 * Wrapper method.
	 * @param gmName
	 * @param action
	 * @param target
	 */
	public static void auditGMAction(String gmName, String action, String target)
	{
		auditGMAction(gmName, action, target, "");
	}
}