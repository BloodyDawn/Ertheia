import dwo.config.Config;
import dwo.gameserver.GameServerStartup;
import dwo.gameserver.engine.logengine.L2Log;
import dwo.gameserver.util.database.DatabaseBackupManager;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;

import static dwo.gameserver.util.Tools.printSection;

public class BootManager
{
	private static Logger _log = LogManager.getLogger(BootManager.class);

	private BootManager() throws Throwable
	{
		printSection("Boot Manager");
		_log.log(Level.INFO, "BootManager: Initializing Boot Manager.");

		_log.log(Level.INFO, "BootManager: Initializing Configs.");
		Config.loadAll();
		_log.log(Level.INFO, "BootManager: Config Successfully Loaded.");

		if(Config.DATABASE_BACKUP_MAKE_BACKUP_ON_STARTUP)
		{
			printSection("Database Backup");
			_log.log(Level.INFO, "BootManager: Starting backup database...");
			DatabaseBackupManager.makeBackup();
		}

		printSection("Game Server");
		_log.log(Level.INFO, "BootManager: Preparations Done. Starting GameServer!");

		new GameServerStartup();
	}

	public static void main(String[] args) throws Throwable
	{
		createBootDirs();
		L2Log.initLogging();

		_log.log(Level.INFO, "BootManager: All Directories and Files Created!");
		new BootManager();
	}

	private static void createBootDirs()
	{
		new File("log").mkdirs();
		new File("log/java").mkdirs();
		new File("log/GMAudit").mkdirs();
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
	}
}