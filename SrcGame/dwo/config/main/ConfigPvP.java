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

import java.util.Arrays;

/**
 * @author L0ngh0rn
 */
public class ConfigPvP extends Config
{
	private static final String path = PVP_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			REPUTATION_LOST_DEFAULT_VALUE = getInt(properties, "ReputationLostRestoreDefaultValue", 240);
			REPUTATION_MAX_VALUE = getInt(properties, "MaxReputation", 10000);
			REPUTATION_XP_DIVIDER = getLong(properties, "XPDivider", 260L);
			REPUTATION_DROP_GM = getBoolean(properties, "CanGMDropEquipment", false);
			REPUTATION_DROPITEM_VALUE = getInt(properties, "MinimumPKRequiredToDrop", 30);
			REPUTATION_NONDROPPABLE_PET_ITEMS = getString(properties, "ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650,9882");
			REPUTATION_NONDROPPABLE_ITEMS = Config.getString(properties, "ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,7694,8181,5575,7694,9388,9389,9390");

			String[] array = REPUTATION_NONDROPPABLE_PET_ITEMS.split(",");
			REPUTATION_LIST_NONDROPPABLE_PET_ITEMS = new int[array.length];

			for(int i = 0; i < array.length; i++)
			{
				REPUTATION_LIST_NONDROPPABLE_PET_ITEMS[i] = Integer.parseInt(array[i]);
			}

			array = REPUTATION_NONDROPPABLE_ITEMS.split(",");
			REPUTATION_LIST_NONDROPPABLE_ITEMS = new int[array.length];

			for(int i = 0; i < array.length; i++)
			{
				REPUTATION_LIST_NONDROPPABLE_ITEMS[i] = Integer.parseInt(array[i]);
			}

			// sorting so binarySearch can be used later
			Arrays.sort(REPUTATION_LIST_NONDROPPABLE_PET_ITEMS);
			Arrays.sort(REPUTATION_LIST_NONDROPPABLE_ITEMS);

			PVP_NORMAL_TIME = getInt(properties, "PvPVsNormalTime", 120000);
			PVP_PVP_TIME = getInt(properties, "PvPVsPvPTime", 60000);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
