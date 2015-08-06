/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.config.network;

import dwo.config.Config;
import dwo.config.ConfigProperties;
import gnu.trove.list.array.TIntArrayList;
import org.apache.log4j.Level;

import java.io.File;

/**
 * @author L0ngh0rn
 */
public class ConfigGameServer extends Config
{
	private static final String path = GAME_SERVER_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			GAMESERVER_HOSTNAME = getString(properties, "GameserverHostname", "*");
			PORT_GAME = getInt(properties, "GameserverPort", 7777);
			GAME_SERVER_LOGIN_PORT = getInt(properties, "LoginPort", 9014);
			GAME_SERVER_LOGIN_HOST = getString(properties, "LoginHost", "127.0.0.1");
			REQUEST_ID = getInt(properties, "RequestServerID", 0);
			ACCEPT_ALTERNATE_ID = getBoolean(properties, "AcceptAlternateID", true);
			MYSQL_DB = getString(properties, "GameDbName", "l2god");
			DATABASE_HOST = getString(properties, "GameDbHost", "127.0.0.1");
			DATABASE_PORT = getInt(properties, "GameDbPort", 3306);
			DATABASE_LOGIN = getString(properties, "GameDbUser", "root");
			DATABASE_PASSWORD = getString(properties, "GameDbPass", "root");
			DATABASE_MAX_CONNECTIONS = getInt(properties, "GameDbCon", 50);
			DATAPACK_ROOT = new File(getString(properties, "DatapackRoot", ".")).getCanonicalFile();
			CNAME_TEMPLATE = getString(properties, "CnameTemplate", ".*");
			PET_NAME_TEMPLATE = getString(properties, "PetNameTemplate", ".*");
			CLAN_NAME_TEMPLATE = getString(properties, "ClanNameTemplate", ".*");
			MAX_CHARACTERS_NUMBER_PER_ACCOUNT = getInt(properties, "CharMaxNumber", 0);
			MAXIMUM_ONLINE_USERS = getInt(properties, "MaximumOnlineUsers", 5000);
			String[] protocols = getString(properties, "AllowedProtocolRevisions", "445").split(";");
			PROTOCOL_LIST = new TIntArrayList(protocols.length);
			USE_UTF8 = getBoolean(properties, "GameDbUtf8", true);
			for(String protocol : protocols)
			{
				try
				{
					PROTOCOL_LIST.add(Integer.parseInt(protocol.trim()));
				}
				catch(NumberFormatException e)
				{
					_log.log(Level.INFO, "Wrong config protocol version: " + protocol + ". Skipped.");
				}
			}
			DATABASE_CLEAN_UP = getBoolean(properties, "DatabaseCleanUp", true);
			CONNECTION_CLOSE_TIME = getLong(properties, "ConnectionCloseTime", 60000L);

			// Настройки бэкапа базы данных
			DATABASE_BACKUP_MAKE_BACKUP_ON_STARTUP = getBoolean(properties, "DatabaseBackupMakeBackupOnStartup", false);
			DATABASE_BACKUP_MAKE_BACKUP_ON_SHUTDOWN = getBoolean(properties, "DatabaseBackupMakeBackupOnShutdown", false);
			DATABASE_BACKUP_DATABASE_NAME = getString(properties, "DatabaseBackupDatabaseName", "l2god");
			DATABASE_BACKUP_SAVE_PATH = getString(properties, "DatabaseBackupSavePath", "/backup/database/");
			DATABASE_BACKUP_COMPRESSION = getBoolean(properties, "DatabaseBackupCompression", true);
			DATABASE_BACKUP_MYSQLDUMP_PATH = getString(properties, "DatabaseBackupMysqldumpPath", ".");
			USE_WINDOWS_LIMIT_BY_IP = getBoolean(properties, "UseWindowsLimitByIP", false);
			WINDOWS_LIMIT_COUNT = getInt(properties, "WindowsLimitCount", 2);
			ENABLE_RC4 = Config.getBoolean(properties, "EnableRC4Crypt", false);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
