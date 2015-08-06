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
import org.apache.log4j.Level;

import java.io.File;

/**
 * @author L0ngh0rn
 */
public class ConfigLoginServer extends Config
{
	private static final String path = LOGIN_SERVER_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			GAME_SERVER_LOGIN_HOST = getString(properties, "LoginHostname", "*");
			GAME_SERVER_LOGIN_PORT = getInt(properties, "LoginPort", 9013);
			LOGIN_BIND_ADDRESS = getString(properties, "LoginserverHostname", "*");
			PORT_LOGIN = getInt(properties, "LoginserverPort", 2106);
			DEBUG = getBoolean(properties, "Debug", false);
			ACCEPT_NEW_GAMESERVER = getBoolean(properties, "AcceptNewGameServer", true);
			LOGIN_TRY_BEFORE_BAN = getInt(properties, "LoginTryBeforeBan", 10);
			LOGIN_BLOCK_AFTER_BAN = getInt(properties, "LoginBlockAfterBan", 600);
			LOG_LOGIN_CONTROLLER = getBoolean(properties, "LogLoginController", true);
			MYSQL_DB = getString(properties, "LoginDbName", "l2god_login");
			DATABASE_HOST = getString(properties, "LoginDbHost", "127.0.0.1");
			DATABASE_LOGIN = getString(properties, "LoginDbUser", "root");
			DATABASE_PASSWORD = getString(properties, "LoginDbPass", "root");
			DATABASE_MAX_CONNECTIONS = getInt(properties, "LoginDbCon", 10);
			USE_UTF8 = getBoolean(properties, "LoginDbUtf8", true);

			SHOW_LICENCE = getBoolean(properties, "ShowLicence", false);
			AUTO_CREATE_ACCOUNTS = getBoolean(properties, "AutoCreateAccounts", true);
			FLOOD_PROTECTION = getBoolean(properties, "EnableFloodProtection", true);
			FAST_CONNECTION_LIMIT = getInt(properties, "FastConnectionLimit", 15);
			NORMAL_CONNECTION_TIME = getInt(properties, "NormalConnectionTime", 700);
			FAST_CONNECTION_TIME = getInt(properties, "FastConnectionTime", 350);
			MAX_CONNECTION_PER_IP = getInt(properties, "MaxConnectionPerIP", 50);
			//FIXME: in login?
			DATAPACK_ROOT = new File(".").getCanonicalFile();
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
