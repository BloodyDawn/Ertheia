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
public class ConfigWedding extends Config
{
	private static final String path = L2JS_WEDDING_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			ALLOW_WEDDING = getBoolean(properties, "AllowWedding", false);
			WEDDING_PRICE = getInt(properties, "WeddingPrice", 250000000);
			WEDDING_PUNISH_INFIDELITY = getBoolean(properties, "WeddingPunishInfidelity", true);
			WEDDING_TELEPORT = getBoolean(properties, "WeddingTeleport", true);
			WEDDING_TELEPORT_PRICE = getInt(properties, "WeddingTeleportPrice", 50000);
			WEDDING_TELEPORT_DURATION = getInt(properties, "WeddingTeleportDuration", 60);
			WEDDING_SAMESEX = getBoolean(properties, "WeddingAllowSameSex", false);
			WEDDING_FORMALWEAR = getBoolean(properties, "WeddingFormalWear", true);
			WEDDING_DIVORCE_COSTS = getInt(properties, "WeddingDivorceCosts", 30);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
