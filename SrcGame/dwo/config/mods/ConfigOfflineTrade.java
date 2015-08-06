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
package dwo.config.mods;

import dwo.config.Config;
import dwo.config.ConfigProperties;
import org.apache.log4j.Level;

/**
 * @author L0ngh0rn
 */
public class ConfigOfflineTrade extends Config
{
	private static final String path = L2JS_OFFLINE_TRADE_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			OFFLINE_TRADE_ENABLE = getBoolean(properties, "OfflineTradeEnable", false);
			OFFLINE_CRAFT_ENABLE = getBoolean(properties, "OfflineCraftEnable", false);
			OFFLINE_MODE_NO_DAMAGE = getBoolean(properties, "OfflineModeNoDamage", false);
			OFFLINE_SET_NAME_COLOR = getBoolean(properties, "OfflineSetNameColor", false);
			OFFLINE_NAME_COLOR = getIntDecode(properties, "OfflineNameColor", "808080");
			OFFLINE_FAME = getBoolean(properties, "OfflineFame", true);
			RESTORE_OFFLINERS = getBoolean(properties, "RestoreOffliners", false);
			OFFLINE_MAX_DAYS = getInt(properties, "OfflineMaxDays", 10);
			OFFLINE_DISCONNECT_FINISHED = getBoolean(properties, "OfflineDisconnectFinished", true);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
