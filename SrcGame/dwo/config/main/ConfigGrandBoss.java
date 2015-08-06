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
 * @author L0ngh0rn
 */

public class ConfigGrandBoss extends Config
{
	private static final String path = GRAND_BOSS_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			ANTHARAS_WAIT_TIME = getInt(properties, "AntharasWaitTime", 30);
			if(ANTHARAS_WAIT_TIME < 3 || ANTHARAS_WAIT_TIME > 60)
			{
				ANTHARAS_WAIT_TIME = 30;
			}
			ANTHARAS_WAIT_TIME *= 60000;

			VALAKAS_WAIT_TIME = getInt(properties, "ValakasWaitTime", 30);
			if(VALAKAS_WAIT_TIME < 3 || VALAKAS_WAIT_TIME > 60)
			{
				VALAKAS_WAIT_TIME = 30;
			}
			VALAKAS_WAIT_TIME *= 60000;

			INTERVAL_OF_ANTHARAS_SPAWN = getInt(properties, "IntervalOfAntharasSpawn", 264);
			if(INTERVAL_OF_ANTHARAS_SPAWN < 1 || INTERVAL_OF_ANTHARAS_SPAWN > 480)
			{
				INTERVAL_OF_ANTHARAS_SPAWN = 264;
			}
			INTERVAL_OF_ANTHARAS_SPAWN *= 3600000;

			RANDOM_OF_ANTHARAS_SPAWN = getInt(properties, "RandomOfAntharasSpawn", 72);
			if(RANDOM_OF_ANTHARAS_SPAWN < 1 || RANDOM_OF_ANTHARAS_SPAWN > 192)
			{
				RANDOM_OF_ANTHARAS_SPAWN = 72;
			}
			RANDOM_OF_ANTHARAS_SPAWN *= 3600000;

			INTERVAL_OF_VALAKAS_SPAWN = getInt(properties, "IntervalOfValakasSpawn", 264);
			if(INTERVAL_OF_VALAKAS_SPAWN < 1 || INTERVAL_OF_VALAKAS_SPAWN > 480)
			{
				INTERVAL_OF_VALAKAS_SPAWN = 264;
			}
			INTERVAL_OF_VALAKAS_SPAWN *= 3600000;

			RANDOM_OF_VALAKAS_SPAWN = getInt(properties, "RandomOfValakasSpawn", 72);
			if(RANDOM_OF_VALAKAS_SPAWN < 1 || RANDOM_OF_VALAKAS_SPAWN > 192)
			{
				RANDOM_OF_VALAKAS_SPAWN = 72;
			}
			RANDOM_OF_VALAKAS_SPAWN *= 3600000;

            INTERVAL_OF_LINDVIOR_SPAWN = getInt(properties, "IntervalOfLindviorSpawn", 264);
            if(INTERVAL_OF_LINDVIOR_SPAWN < 1 || INTERVAL_OF_LINDVIOR_SPAWN > 480)
            {
                INTERVAL_OF_LINDVIOR_SPAWN = 264;
            }
            INTERVAL_OF_LINDVIOR_SPAWN *= 3600000;

            RANDOM_OF_LINDVIOR_SPAWN = getInt(properties, "RandomOfLindviorSpawn", 72);
            if(RANDOM_OF_LINDVIOR_SPAWN < 1 || RANDOM_OF_LINDVIOR_SPAWN > 192)
            {
                RANDOM_OF_LINDVIOR_SPAWN = 72;
            }
            RANDOM_OF_LINDVIOR_SPAWN *= 3600000;

			INTERVAL_OF_BAIUM_SPAWN = getInt(properties, "IntervalOfBaiumSpawn", 168);
			if(INTERVAL_OF_BAIUM_SPAWN < 1 || INTERVAL_OF_BAIUM_SPAWN > 480)
			{
				INTERVAL_OF_BAIUM_SPAWN = 168;
			}
			INTERVAL_OF_BAIUM_SPAWN *= 3600000;

			RANDOM_OF_BAIUM_SPAWN = getInt(properties, "RandomOfBaiumSpawn", 48);
			if(RANDOM_OF_BAIUM_SPAWN < 1 || RANDOM_OF_BAIUM_SPAWN > 192)
			{
				RANDOM_OF_BAIUM_SPAWN = 48;
			}
			RANDOM_OF_BAIUM_SPAWN *= 3600000;

			INTERVAL_OF_CORE_SPAWN = getInt(properties, "IntervalOfCoreSpawn", 60);
			if(INTERVAL_OF_CORE_SPAWN < 1 || INTERVAL_OF_CORE_SPAWN > 480)
			{
				INTERVAL_OF_CORE_SPAWN = 60;
			}
			INTERVAL_OF_CORE_SPAWN *= 3600000;

			RANDOM_OF_CORE_SPAWN = getInt(properties, "RandomOfCoreSpawn", 24);
			if(RANDOM_OF_CORE_SPAWN < 1 || RANDOM_OF_CORE_SPAWN > 192)
			{
				RANDOM_OF_CORE_SPAWN = 24;
			}
			RANDOM_OF_CORE_SPAWN *= 3600000;

			INTERVAL_OF_ORFEN_SPAWN = getInt(properties, "IntervalOfOrfenSpawn", 48);
			if(INTERVAL_OF_ORFEN_SPAWN < 1 || INTERVAL_OF_ORFEN_SPAWN > 480)
			{
				INTERVAL_OF_ORFEN_SPAWN = 48;
			}
			INTERVAL_OF_ORFEN_SPAWN *= 3600000;

			RANDOM_OF_ORFEN_SPAWN = getInt(properties, "RandomOfOrfenSpawn", 20);
			if(RANDOM_OF_ORFEN_SPAWN < 1 || RANDOM_OF_ORFEN_SPAWN > 192)
			{
				RANDOM_OF_ORFEN_SPAWN = 20;
			}
			RANDOM_OF_ORFEN_SPAWN *= 3600000;

			INTERVAL_OF_QUEEN_ANT_SPAWN = getInt(properties, "IntervalOfQueenAntSpawn", 36);
			if(INTERVAL_OF_QUEEN_ANT_SPAWN < 1 || INTERVAL_OF_QUEEN_ANT_SPAWN > 480)
			{
				INTERVAL_OF_QUEEN_ANT_SPAWN = 36;
			}
			INTERVAL_OF_QUEEN_ANT_SPAWN *= 3600000;

			RANDOM_OF_QUEEN_ANT_SPAWN = getInt(properties, "RandomOfQueenAntSpawn", 17);
			if(RANDOM_OF_QUEEN_ANT_SPAWN < 1 || RANDOM_OF_QUEEN_ANT_SPAWN > 192)
			{
				RANDOM_OF_QUEEN_ANT_SPAWN = 17;
			}
			RANDOM_OF_QUEEN_ANT_SPAWN *= 3600000;

			INTERVAL_OF_BELETH_SPAWN = getInt(properties, "IntervalOfBelethSpawn", 192);
			if(INTERVAL_OF_BELETH_SPAWN < 1 || INTERVAL_OF_BELETH_SPAWN > 480)
			{
				INTERVAL_OF_BELETH_SPAWN = 192;
			}
			INTERVAL_OF_BELETH_SPAWN *= 3600000;

			RANDOM_OF_BELETH_SPAWN = getInt(properties, "RandomOfBelethSpawn", 148);
			if(RANDOM_OF_BELETH_SPAWN < 1 || RANDOM_OF_BELETH_SPAWN > 192)
			{
				RANDOM_OF_BELETH_SPAWN = 148;
			}
			RANDOM_OF_BELETH_SPAWN *= 3600000;

			BELETH_MIN_PLAYERS = getInt(properties, "BelethMinPlayers", 4);

			MIN_FREYA_PLAYERS = getInt(properties, "MinFreyaPlayers", 18);
			MAX_FREYA_PLAYERS = getInt(properties, "MaxFreyaPlayers", 27);
			MIN_LEVEL_PLAYERS = getInt(properties, "MinLevelPlayers", 82);
			MIN_FREYA_HC_PLAYERS = getInt(properties, "MinFreyaHcPlayers", 36);
			MAX_FREYA_HC_PLAYERS = getInt(properties, "MaxFreyaHcPlayers", 45);
			MIN_LEVEL_FREYA_HC_PLAYERS = getInt(properties, "MinLevelFreyaHcPlayers", 82);

			MIN_ISTINA_PLAYERS = getInt(properties, "MinIstinaPlayers", 14);
			MAX_ISTINA_PLAYERS = getInt(properties, "MaxIstinaPlayers", 35);
			MIN_LEVEL_ISTINA_PLAYERS = getInt(properties, "MinLevelIstinaPlayers", 90);

			MIN_ISTINA_HARD_PLAYERS = getInt(properties, "MinIstinaHardPlayers", 21);
			MAX_ISTINA_HARD_PLAYERS = getInt(properties, "MaxIstinaHardPlayers", 35);
			MIN_LEVEL_ISTINA_HARD_PLAYERS = getInt(properties, "MinLevelIstinaHardPlayers", 97);

			MIN_OCTAVIS_PLAYERS = getInt(properties, "MinOctavisPlayers", 14);
			MAX_OCTAVIS_PLAYERS = getInt(properties, "MaxOctavisPlayers", 49);
			MIN_LEVEL_OCTAVIS_PLAYERS = getInt(properties, "MinLevelOctavisPlayers", 95);
			MIN_OCTAVIS_HARD_PLAYERS = getInt(properties, "MinOctavisHardPlayers", 21);
			MAX_OCTAVIS_HARD_PLAYERS = getInt(properties, "MaxOctavisHardPlayers", 49);
			MIN_LEVEL_OCTAVIS_HARD_PLAYERS = getInt(properties, "MinLevelOctavisHardPlayers", 97);

			MIN_BAILOR_PLAYERS = getInt(properties, "MinBailorHardPlayers", 7);
			MAX_BAILOR_PLAYERS = getInt(properties, "MaxBailorHardPlayers", 7);
			MIN_LEVEL_BAILOR_PLAYERS = getInt(properties, "MinLevelBailorHardPlayers", 97);

			MIN_BALOK_PLAYERS = getInt(properties, "MinBalokHardPlayers", 14);
			MAX_BALOK_PLAYERS = getInt(properties, "MaxBalokHardPlayers", 21);
			MIN_LEVEL_BALOK_PLAYERS = getInt(properties, "MinLevelBalokHardPlayers", 97);

			MIN_TAUTI_PLAYERS = getInt(properties, "MinTautiPlayers", 14);
			MAX_TAUTI_PLAYERS = getInt(properties, "MaxTautiPlayers", 35);
			MIN_LEVEL_TAUTI_PLAYERS = getInt(properties, "MinLevelTautiPlayers", 97);
			MIN_TAUTI_HARD_PLAYERS = getInt(properties, "MinTautiHardPlayers", 21);
			MAX_TAUTI_HARD_PLAYERS = getInt(properties, "MaxTautiHardPlayers", 35);
			MIN_LEVEL_TAUTI_HARD_PLAYERS = getInt(properties, "MinLevelTautiHardPlayers", 97);

			MIN_FORTUNA_PLAYERS = getInt(properties, "MinFortunaPlayers", 7);
			MAX_FORTUNA_PLAYERS = getInt(properties, "MaxFortunaPlayers", 7);
			MIN_LEVEL_FORTUNA_PLAYERS = getInt(properties, "MinLevelFortunaPlayers", 90);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
