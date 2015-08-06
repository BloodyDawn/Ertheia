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
import dwo.gameserver.util.StringUtil;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.log4j.Level;

/**
 * @author L0ngh0rn
 */
public class ConfigNPC extends Config
{
	private static final String path = NPC_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			DROP_WITHOUT_PENALTY = getBoolean(properties, "AltDropWithoutPenalty", false);
			EXP_SP_WITHOUT_PENALTY = getBoolean(properties, "AltExpSpWithoutPenalty", false);
			ALT_MOB_AGRO_IN_PEACEZONE = getBoolean(properties, "AltMobAgroInPeaceZone", true);
			ALT_ATTACKABLE_NPCS = getBoolean(properties, "AltAttackableNpcs", true);
			ALT_GAME_VIEWNPC = getBoolean(properties, "AltGameViewNpc", false);
			MAX_DRIFT_RANGE = getInt(properties, "MaxDriftRange", 300);
			DEEPBLUE_DROP_RULES = getBoolean(properties, "UseDeepBlueDropRules", true);
			DEEPBLUE_DROP_RULES_RAID = getBoolean(properties, "UseDeepBlueDropRulesRaid", true);
			SHOW_NPC_LVL = getBoolean(properties, "ShowNpcLevel", false);
			SHOW_CREST_WITHOUT_QUEST = getBoolean(properties, "ShowCrestWithoutQuest", false);
			ENABLE_RANDOM_ENCHANT_EFFECT = getBoolean(properties, "EnableRandomEnchantEffect", false);
			MIN_NPC_LVL_DMG_PENALTY = getInt(properties, "MinNPCLevelForDmgPenalty", 78);
			NPC_DMG_PENALTY = parseConfigLine(getString(properties, "DmgPenaltyForLvLDifferences", "0.7, 0.6, 0.6, 0.55"));
			NPC_CRIT_DMG_PENALTY = parseConfigLine(getString(properties, "CritDmgPenaltyForLvLDifferences", "0.75, 0.65, 0.6, 0.58"));
			NPC_SKILL_DMG_PENALTY = parseConfigLine(getString(properties, "SkillDmgPenaltyForLvLDifferences", "0.8, 0.7, 0.65, 0.62"));
			MIN_NPC_LVL_MAGIC_PENALTY = getInt(properties, "MinNPCLevelForMagicPenalty", 78);
			NPC_SKILL_CHANCE_PENALTY = parseConfigLine(getString(properties, "SkillChancePenaltyForLvLDifferences", "2.5, 3.0, 3.25, 3.5"));
			DECAY_TIME_TASK = getInt(properties, "DecayTimeTask", 5000);
			NPC_DECAY_TIME = getInt(properties, "NpcDecayTime", 8500);
			RAID_BOSS_DECAY_TIME = getInt(properties, "RaidBossDecayTime", 30000);
			SPOILED_DECAY_TIME = getInt(properties, "SpoiledDecayTime", 18500);
			GUARD_ATTACK_AGGRO_MOB = getBoolean(properties, "GuardAttackAggroMob", false);
			ALLOW_WYVERN_UPGRADER = getBoolean(properties, "AllowWyvernUpgrader", false);
			RAID_HP_REGEN_MULTIPLIER = getDouble(properties, "RaidHpRegenMultiplier", 100.0D) / 100;
			RAID_MP_REGEN_MULTIPLIER = getDouble(properties, "RaidMpRegenMultiplier", 100.0D) / 100;
			RAID_PDEFENCE_MULTIPLIER = getDouble(properties, "RaidPDefenceMultiplier", 100.0D) / 100;
			RAID_MDEFENCE_MULTIPLIER = getDouble(properties, "RaidMDefenceMultiplier", 100.0D) / 100;
			RAID_PATTACK_MULTIPLIER = getDouble(properties, "RaidPAttackMultiplier", 100.0D) / 100;
			RAID_MATTACK_MULTIPLIER = getDouble(properties, "RaidMAttackMultiplier", 100.0D) / 100;
			RAID_MIN_RESPAWN_MULTIPLIER = getFloat(properties, "RaidMinRespawnMultiplier", 1.0F);
			RAID_MAX_RESPAWN_MULTIPLIER = getFloat(properties, "RaidMaxRespawnMultiplier", 1.0F);
			RAID_MINION_RESPAWN_TIMER = getInt(properties, "RaidMinionRespawnTime", 300000);
			RAID_DISABLE_CURSE = getBoolean(properties, "DisableRaidCurse", false);
			RAID_CHAOS_TIME = getInt(properties, "RaidChaosTime", 10);
			GRAND_CHAOS_TIME = getInt(properties, "GrandChaosTime", 10);
			MINION_CHAOS_TIME = getInt(properties, "MinionChaosTime", 10);
			INVENTORY_MAXIMUM_PET = getInt(properties, "MaximumSlotsForPet", 12);
			PET_HP_REGEN_MULTIPLIER = getDouble(properties, "PetHpRegenMultiplier", 100.0D) / 100;
			PET_MP_REGEN_MULTIPLIER = getDouble(properties, "PetMpRegenMultiplier", 100.0D) / 100;
			String[] split = getStringArray(properties, "NonTalkingNpcs", new String[]{
				"18684", "18685", "18686", "18687", "18688", "18689", "18690", "19691", "18692", "31557", "31606",
				"31671", "31672", "31673", "31674", "32026", "32030", "32031", "32032", "32306", "32619", "32620",
				"32621", "33018", "33000", "33002", "33007", "19126"
			}, ",");
			NON_TALKING_NPCS = new TIntArrayList(split.length);
			for(String npcId : split)
			{
				try
				{
					NON_TALKING_NPCS.add(Integer.parseInt(npcId));
				}
				catch(NumberFormatException nfe)
				{
					if(!npcId.isEmpty())
					{
						_log.log(Level.WARN, "Could not parse " + npcId + " id for NonTalkingNpcs. Please check that all values are digits and coma separated.");
					}
				}
			}
			String[] propertySplit = getStringArray(properties, "CustomMinionsRespawnTime", null, ";");
			MINIONS_RESPAWN_TIME = new TIntIntHashMap(propertySplit.length);
			for(String prop : propertySplit)
			{
				String[] propSplit = prop.split(",");
				if(propSplit.length != 2)
				{
					_log.log(Level.WARN, StringUtil.concat("[CustomMinionsRespawnTime]: invalid config property -> CustomMinionsRespawnTime \"", prop, "\""));
				}

				try
				{
					MINIONS_RESPAWN_TIME.put(Integer.valueOf(propSplit[0]), Integer.valueOf(propSplit[1]));
				}
				catch(NumberFormatException nfe)
				{
					if(!prop.isEmpty())
					{
						_log.log(Level.WARN, StringUtil.concat("[CustomMinionsRespawnTime]: invalid config property -> CustomMinionsRespawnTime \"", propSplit[0], "\"", propSplit[1]));
					}
				}
			}
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
