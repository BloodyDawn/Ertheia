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
package dwo.config.main;

import dwo.config.Config;
import dwo.config.ConfigProperties;
import org.apache.log4j.Level;

/**
 * @author L0ngh0rn, ANZO
 */
public class ConfigSiege extends Config
{
	private static final String path = SIEGE_CONFIGURATION_FILE;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);

			// Castle siege
			GLUDIO_MAX_MERCENARIES = getInt(properties, "GludioMaxMercenaries", 100);
			DION_MAX_MERCENARIES = getInt(properties, "DionMaxMercenaries", 150);
			GIRAN_MAX_MERCENARIES = getInt(properties, "GiranMaxMercenaries", 200);
			OREN_MAX_MERCENARIES = getInt(properties, "OrenMaxMercenaries", 300);
			ADEN_MAX_MERCENARIES = getInt(properties, "AdenMaxMercenaries", 400);
			INNADRIL_MAX_MERCENARIES = getInt(properties, "InnadrilMaxMercenaries", 400);
			GODDARD_MAX_MERCENARIES = getInt(properties, "GoddardMaxMercenaries", 400);
			RUNE_MAX_MERCENARIES = getInt(properties, "RuneMaxMercenaries", 400);
			SCHUTTGART_MAX_MERCENARIES = getInt(properties, "SchuttgartMaxMercenaries", 400);

			// Clanhall siege
			CHS_MAX_ATTACKERS = getInt(properties, "MaxAttackers", 500);
			CHS_CLAN_MINLEVEL = getInt(properties, "MinClanLevel", 4);
			CHS_MAX_FLAGS_PER_CLAN = getInt(properties, "MaxFlagsPerClan", 1);
			CHS_ENABLE_FAME = getBoolean(properties, "EnableFame", false);
			CHS_FAME_AMOUNT = getInt(properties, "FameAmount", 0);
			CHS_FAME_FREQUENCY = getInt(properties, "FameFrequency", 0);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
