import dwo.config.Config;
import dwo.log.L2Log;
import dwo.loginserver.LoginServer;
import dwo.util.Tools;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;

public class BootManager
{
	private static final Logger _log = LogManager.getLogger(BootManager.class);

	public BootManager() //test
	{
		createBootDirs();
		Tools.printSection("Boot Manager");
		_log.log(Level.INFO, "BootManager: Initializing Boot Manager.");

		// reads Config b4 init gameserver boot
		// --------------------------------------------
		_log.log(Level.INFO, "BootManager: Initializing Configs.");
		Config.load();
		_log.log(Level.INFO, "BootManager: Config Sucessffully Loaded.");

		Tools.printSection("Login Server");
		_log.log(Level.INFO, "BootManager: Preparations Done. Staring LoginServer!");
		LoginServer.getInstance();
	}

	public static void main(String[] args)
	{
		L2Log.initLogging();
		new BootManager();
	}

	private void createBootDirs()
	{
		new File("log").mkdirs();
		_log.log(Level.INFO, "BootManager: All Directories and Files Created!");
	}
}