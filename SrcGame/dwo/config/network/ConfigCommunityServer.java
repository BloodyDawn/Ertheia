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

import java.math.BigInteger;

/**
 * @author L0ngh0rn
 */
public class ConfigCommunityServer extends Config
{
	private static final String path = COMMUNITY_SERVER_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			ENABLE_COMMUNITY_BOARD = getBoolean(properties, "EnableCommunityBoard", false);
			COMMUNITY_SERVER_ADDRESS = getString(properties, "CommunityServerHostname", "localhost");
			COMMUNITY_SERVER_PORT = getInt(properties, "CommunityServerPort", 9013);
			COMMUNITY_SERVER_HEX_ID = new BigInteger(properties.getProperty("CommunityServerHexId"), 16).toByteArray();
			COMMUNITY_SERVER_SQL_DP_ID = getInt(properties, "CommunityServerSqlDpId", 200);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
