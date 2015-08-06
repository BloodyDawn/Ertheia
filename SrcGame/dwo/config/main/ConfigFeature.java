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
public class ConfigFeature extends Config
{
	private static final String path = FEATURE_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			SIEGE_HOUR = getInt(properties, "SiegeHour", 20);

			FS_BLOOD_OATH_COUNT = getInt(properties, "FortressBloodOathCount", 1);
			FS_UPDATE_FRQ = getInt(properties, "FortressPeriodicUpdateFrequency", 360);
			FS_MAX_SUPPLY_LEVEL = getInt(properties, "FortressMaxSupplyLevel", 6);
			FS_FEE_FOR_CASTLE = getInt(properties, "FortressFeeForCastle", 25000);
			FS_MAX_OWN_TIME = getInt(properties, "FortressMaximumOwnTime", 168);

			TAKE_FORT_POINTS = getInt(properties, "TakeFortPoints", 200);
			LOOSE_FORT_POINTS = getInt(properties, "LooseFortPoints", 0);
			TAKE_CASTLE_POINTS = getInt(properties, "TakeCastlePoints", 1500);
			LOOSE_CASTLE_POINTS = getInt(properties, "LooseCastlePoints", 3000);
			CASTLE_DEFENDED_POINTS = getInt(properties, "CastleDefendedPoints", 750);
			FESTIVAL_WIN_POINTS = getInt(properties, "FestivalOfDarknessWin", 200);
			HERO_POINTS = getInt(properties, "HeroPoints", 5000);
			ROYAL_GUARD_COST = getInt(properties, "CreateRoyalGuardCost", 5000);
			KNIGHT_UNIT_COST = getInt(properties, "CreateKnightUnitCost", 10000);
			KNIGHT_REINFORCE_COST = getInt(properties, "ReinforceKnightUnitCost", 5000);
			BALLISTA_POINTS = getInt(properties, "KillBallistaPoints", 30);
			BLOODALLIANCE_POINTS = getInt(properties, "BloodAlliancePoints", 500);
			BLOODOATH_POINTS = getInt(properties, "BloodOathPoints", 200);
			KNIGHTSEPAULETTE_POINTS = getInt(properties, "KnightsEpaulettePoints", 20);
			REPUTATION_SCORE_PER_KILL = getInt(properties, "ReputationScorePerKill", 1);
			RAID_RANKING_1ST = getInt(properties, "1stRaidRankingPoints", 1250);
			RAID_RANKING_2ND = getInt(properties, "2ndRaidRankingPoints", 900);
			RAID_RANKING_3RD = getInt(properties, "3rdRaidRankingPoints", 700);
			RAID_RANKING_4TH = getInt(properties, "4thRaidRankingPoints", 600);
			RAID_RANKING_5TH = getInt(properties, "5thRaidRankingPoints", 450);
			RAID_RANKING_6TH = getInt(properties, "6thRaidRankingPoints", 350);
			RAID_RANKING_7TH = getInt(properties, "7thRaidRankingPoints", 300);
			RAID_RANKING_8TH = getInt(properties, "8thRaidRankingPoints", 200);
			RAID_RANKING_9TH = getInt(properties, "9thRaidRankingPoints", 150);
			RAID_RANKING_10TH = getInt(properties, "10thRaidRankingPoints", 100);
			RAID_RANKING_UP_TO_50TH = getInt(properties, "UpTo50thRaidRankingPoints", 25);
			RAID_RANKING_UP_TO_100TH = getInt(properties, "UpTo100thRaidRankingPoints", 12);
			CLAN_LEVEL_6_COST = getInt(properties, "ClanLevel6Cost", 5000);
			CLAN_LEVEL_7_COST = getInt(properties, "ClanLevel7Cost", 10000);
			CLAN_LEVEL_8_COST = getInt(properties, "ClanLevel8Cost", 20000);
			CLAN_LEVEL_9_COST = getInt(properties, "ClanLevel9Cost", 40000);
			CLAN_LEVEL_10_COST = getInt(properties, "ClanLevel10Cost", 40000);
			CLAN_LEVEL_11_COST = getInt(properties, "ClanLevel11Cost", 75000);
			CLAN_LEVEL_6_REQUIREMENT = getInt(properties, "ClanLevel6Requirement", 30);
			CLAN_LEVEL_7_REQUIREMENT = getInt(properties, "ClanLevel7Requirement", 50);
			CLAN_LEVEL_8_REQUIREMENT = getInt(properties, "ClanLevel8Requirement", 80);
			CLAN_LEVEL_9_REQUIREMENT = getInt(properties, "ClanLevel9Requirement", 120);
			CLAN_LEVEL_10_REQUIREMENT = getInt(properties, "ClanLevel10Requirement", 140);
			CLAN_LEVEL_11_REQUIREMENT = getInt(properties, "ClanLevel11Requirement", 170);
			ALLOW_WYVERN_DURING_SIEGE = getBoolean(properties, "AllowRideWyvernDuringSiege", true);
			PLAYER_MOVEMENT_BLOCK_TIME = getInt(properties, "NpcTalkBlockingTime", 0) * 1000;
			MAMMONS_TELEPORT_RATE = getInt(properties, "MammonsTeleportRate", 240) * 60 * 1000;
			MAMMONS_VOICE_LOC_ENABLE = getBoolean(properties, "MammonsVoiceLocEnable", false);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
