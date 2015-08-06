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
public class ConfigCustom extends Config
{
	private static final String path = L2GOD_CUSTOM_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			SERVER_NAME = getString(properties, "ServerName", "L2GOD");
			CUSTOM_DATA_DIRECTORY = getString(properties, "CustomDataDirectory", "");
			ALLOW_VALID_ENCHANT = getBoolean(properties, "AllowValidEnchant", false);
			ALLOW_VALID_EQUIP_ITEM = getBoolean(properties, "AllowValidEquipItem", false);
			DESTROY_ENCHANT_ITEM = getBoolean(properties, "DestroyEnchantItem", false);
			PUNISH_PLAYER = getBoolean(properties, "PunishPlayer", false);
			PVP_ALLOW_REWARD = getBoolean(properties, "PvpAllowReward", false);
			PVP_REWARD = getStringArray(properties, "PvpReward", "57,500000;5575,500".split("\\;"), ";");
			ALLOW_PVP_COLOR_SYSTEM = getBoolean(properties, "AllowPvPColorSystem", false);
			ALLOW_PVP_COLOR_NAME = getBoolean(properties, "AllowPvPColorName", false);
			ALLOW_PVP_COLOR_TITLE = getBoolean(properties, "AllowPvPColorTitle", false);
			try
			{
				String _configLine = getString(properties, "SystemPvPColor", "50,FFFFFF,FFFF77;100,FFFFFF,FFFF77;150,FFFFFF,FFFF77;250,FFFFFF,FFFF77;500,FFFFFF,FFFF77");
				if(ALLOW_PVP_COLOR_SYSTEM)
				{
					SYSTEM_PVP_COLOR = new SystemPvPColor(_configLine);
				}
			}
			catch(Exception e)
			{
				_log.log(Level.WARN, "SystemPvPColor[loadCustomConfig()]: invalid config property -> SYSTEM_PVP_COLOR");
			}
			AUGMENTATION_WEAPONS_PVP = getBoolean(properties, "AugmentationWeaponsPvP", false);
			ELEMENTAL_ITEM_PVP = getBoolean(properties, "ElementalItemPvP", false);
			ENTER_HELLBOUND_WITHOUT_QUEST = getBoolean(properties, "EnterHellBoundWithoutQuest", false);
			CUSTOM_SPAWNLIST_TABLE = getBoolean(properties, "CustomSpawnlistTable", true);
			SAVE_GMSPAWN_ON_CUSTOM = getBoolean(properties, "SaveGmSpawnOnCustom", true);
			CUSTOM_NPC_TABLE = getBoolean(properties, "CustomNpcTable", true);
			CUSTOM_NPC_SKILLS_TABLE = getBoolean(properties, "CustomNpcSkillsTable", true);
			CUSTOM_DROPLIST_TABLE = getBoolean(properties, "CustomDroplistTable", true);
			SIZE_MESSAGE_HTML_NPC = getInt(properties, "SizeHTMLMessageNPC", 20480);
			SIZE_MESSAGE_HTML_QUEST = getInt(properties, "SizeHTMLMessageQuest", 20480);
			ALLOW_MANA_POTIONS = getBoolean(properties, "AllowManaPotions", true);
			DISABLE_MANA_POTIONS_IN_PVP = getBoolean(properties, "DisableManaPotionsInPvp", false);
			REDUCE_ITEM_PRICE_ON_SELL = Config.getBoolean( properties, "ReduceItemPriceOnSell", false );

		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
