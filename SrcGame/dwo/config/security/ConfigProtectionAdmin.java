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
package dwo.config.security;

import dwo.config.Config;
import dwo.config.ConfigProperties;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author L0ngh0rn
 */

public class ConfigProtectionAdmin extends Config
{
	private static final String path = L2JS_PROTECTION_ADMIN;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			ENABLE_SAFE_ADMIN_PROTECTION = getBoolean(properties, "EnableSafeAdminProtection", true);
			String[] props = getStringArray(properties, "SafeAdminName", new String[]{}, ",");
			SAFE_ADMIN_NAMES = new ArrayList<>(props.length);
			if(props.length != 0)
			{
				Collections.addAll(SAFE_ADMIN_NAMES, props);
			}
			SAFE_ADMIN_PUNISH = getInt(properties, "SafeAdminPunish", 3);
			SAFE_ADMIN_SHOW_ADMIN_ENTER = getBoolean(properties, "SafeAdminShowAdminEnter", false);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
