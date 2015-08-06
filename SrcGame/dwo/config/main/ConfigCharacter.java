package dwo.config.main;

import dwo.config.Config;
import dwo.config.ConfigProperties;
import dwo.gameserver.util.StringUtil;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author L0ngh0rn
 */
public class ConfigCharacter extends Config
{
	private static final String path = CHARACTER_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			ALT_GAME_DELEVEL = getBoolean(properties, "Delevel", true);
			DECREASE_SKILL_LEVEL = getBoolean(properties, "DecreaseSkillOnDelevel", true);
			ALT_WEIGHT_LIMIT = getDouble(properties, "AltWeightLimit", 1);
			RUN_SPD_BOOST = getInt(properties, "RunSpeedBoost", 0);
			RESPAWN_RESTORE_CP = getDouble(properties, "RespawnRestoreCP", 0) / 100;
			RESPAWN_RESTORE_HP = getDouble(properties, "RespawnRestoreHP", 70) / 100;
			RESPAWN_RESTORE_MP = getDouble(properties, "RespawnRestoreMP", 70) / 100;
			HP_REGEN_MULTIPLIER = getDouble(properties, "HpRegenMultiplier", 100) / 100;
			MP_REGEN_MULTIPLIER = getDouble(properties, "MpRegenMultiplier", 100) / 100;
			CP_REGEN_MULTIPLIER = getDouble(properties, "CpRegenMultiplier", 100) / 100;
			ALT_GAME_TIREDNESS = getBoolean(properties, "AltGameTiredness", false);
			ENABLE_MODIFY_SKILL_DURATION = getBoolean(properties, "EnableModifySkillDuration", false);

			// Create Map only if enabled
			if(ENABLE_MODIFY_SKILL_DURATION)
			{
				String[] propertySplit = getString(properties, "SkillDurationList", "").split(";");
				SKILL_DURATION_LIST = new TIntIntHashMap(propertySplit.length);
				for(String skill : propertySplit)
				{
					String[] skillSplit = skill.split(",");
					if(skillSplit.length == 2)
					{
						try
						{
							SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch(NumberFormatException nfe)
						{
							if(!skill.isEmpty())
							{
								_log.log(Level.WARN, StringUtil.concat("[SkillDurationList]: invalid config property -> SkillList \"", skillSplit[0], "\"", skillSplit[1]));
							}
						}
					}
					else
					{
						_log.log(Level.WARN, StringUtil.concat("[SkillDurationList]: invalid config property -> SkillDurationList \"", skill, "\""));
					}
				}
			}
			ENABLE_MODIFY_SKILL_REUSE = getBoolean(properties, "EnableModifySkillReuse", false);
			// Create Map only if enabled
			if(ENABLE_MODIFY_SKILL_REUSE)
			{
				String[] propertySplit = getString(properties, "SkillReuseList", "").split(";");
				SKILL_REUSE_LIST = new TIntIntHashMap(propertySplit.length);
				for(String skill : propertySplit)
				{
					String[] skillSplit = skill.split(",");
					if(skillSplit.length == 2)
					{
						try
						{
							SKILL_REUSE_LIST.put(Integer.valueOf(skillSplit[0]), Integer.valueOf(skillSplit[1]));
						}
						catch(NumberFormatException nfe)
						{
							if(!skill.isEmpty())
							{
								_log.log(Level.WARN, StringUtil.concat("[SkillReuseList]: invalid config property -> SkillList \"", skillSplit[0], "\"", skillSplit[1]));
							}
						}
					}
					else
					{
						_log.log(Level.WARN, StringUtil.concat("[SkillReuseList]: invalid config property -> SkillReuseList \"", skill, "\""));
					}
				}
			}

			AUTO_LEARN_SKILLS = getBoolean(properties, "AutoLearnSkills", false);
			AUTO_LEARN_FS_SKILLS = getBoolean(properties, "AutoLearnForgottenScrollSkills", false);
			BUFFS_MAX_AMOUNT = getByte(properties, "MaxBuffAmount", Byte.parseByte("20"));
			DANCES_MAX_AMOUNT = getByte(properties, "MaxDanceAmount", Byte.parseByte("12"));
			TRIGGERED_BUFFS_MAX_AMOUNT = getByte(properties, "MaxTriggeredBuffAmount", Byte.parseByte("12"));
			DANCE_CANCEL_BUFF = getBoolean(properties, "DanceCancelBuff", false);
			DANCE_CONSUME_ADDITIONAL_MP = getBoolean(properties, "DanceConsumeAdditionalMP", true);
			AUTO_LEARN_DIVINE_INSPIRATION = getBoolean(properties, "AutoLearnDivineInspiration", false);
			EFFECT_CANCELING = getBoolean(properties, "CancelLesserEffect", true);
			ALT_GAME_MAGICFAILURES = getBoolean(properties, "MagicFailures", true);
			PLAYER_FAKEDEATH_UP_PROTECTION = getInt(properties, "PlayerFakeDeathUpProtection", 0);
			STORE_SKILL_COOLTIME = getBoolean(properties, "StoreSkillCooltime", true);
			SUBCLASS_STORE_SKILL_COOLTIME = getBoolean(properties, "SubclassStoreSkillCooltime", false);
			SUMMON_STORE_SKILL_COOLTIME = getBoolean(properties, "SummonStoreSkillCooltime", true);
			ALT_PERFECT_SHLD_BLOCK = getInt(properties, "AltPerfectShieldBlockRate", 10);
			LIFE_CRYSTAL_NEEDED = getBoolean(properties, "LifeCrystalNeeded", true);
			ES_SP_BOOK_NEEDED = getBoolean(properties, "EnchantSkillSpBookNeeded", true);
			ALLOW_AUTOLOOT_COMMAND = getBoolean(properties, "AutoLootVoiceCommand", false);
			DIVINE_SP_BOOK_NEEDED = getBoolean(properties, "DivineInspirationSpBookNeeded", true);
			ALT_GAME_SKILL_LEARN = getBoolean(properties, "AltGameSkillLearn", false);
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = getBoolean(properties, "AltSubClassWithoutQuests", false);
			ALLOW_TRANSFORM_WITHOUT_QUEST = getBoolean(properties, "AltTransfomarionWithoutQuest", false);
			ENABLE_VITALITY = getBoolean(properties, "EnableVitality", true);
			DIVIDE_VITALITY_GAIN_BYLEVEL = getBoolean(properties, "DivideVitalityGainByLevel", false);
			STARTING_VITALITY_POINTS = getInt(properties, "StartingVitalityPoints", 140000);
			VITALITY_ITEMS_WEEKLY_LIMIT = getInt(properties, "VitalityItemsWeeklyLimit", 5);

			MAX_BONUS_EXP = getDouble(properties, "MaxExpBonus", 3.5);
			MAX_BONUS_SP = getDouble(properties, "MaxSpBonus", 3.5);
			MAX_RUN_SPEED = getInt(properties, "MaxRunSpeed", 300);
			MAX_PCRIT_RATE = getInt(properties, "MaxPCritRate", 500);
			MAX_MCRIT_RATE = getInt(properties, "MaxMCritRate", 200);
			MAX_PATK_SPEED = getInt(properties, "MaxPAtkSpeed", 1500);
			MAX_MATK_SPEED = getInt(properties, "MaxMAtkSpeed", 1999);
			MAX_EVASION_RATE = getInt(properties, "MaxEvasion", 250);
			MIN_DEBUFF_CHANCE = getInt(properties, "MinDebuffChance", 10);
			MAX_DEBUFF_CHANCE = getInt(properties, "MaxDebuffChance", 90);
			MAX_SUBCLASS = getByte(properties, "MaxSubclass", Byte.parseByte("3"));
			MAX_SUBCLASS_LEVEL = getByte(properties, "MaxSubclassLevel", Byte.parseByte("80"));
			MAX_PVTSTORESELL_SLOTS_DWARF = getInt(properties, "MaxPvtStoreSellSlotsDwarf", 4);
			MAX_PVTSTORESELL_SLOTS_OTHER = getInt(properties, "MaxPvtStoreSellSlotsOther", 3);
			MAX_PVTSTOREBUY_SLOTS_DWARF = getInt(properties, "MaxPvtStoreBuySlotsDwarf", 5);
			MAX_PVTSTOREBUY_SLOTS_OTHER = getInt(properties, "MaxPvtStoreBuySlotsOther", 4);
			INVENTORY_MAXIMUM_NO_DWARF = getInt(properties, "MaximumSlotsForNoDwarf", 80);
			INVENTORY_MAXIMUM_DWARF = getInt(properties, "MaximumSlotsForDwarf", 100);
			INVENTORY_MAXIMUM_GM = getInt(properties, "MaximumSlotsForGMPlayer", 250);
			INVENTORY_MAXIMUM_QUEST_ITEMS = getInt(properties, "MaximumSlotsForQuestItems", 100);
			MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
			WAREHOUSE_SLOTS_DWARF = getInt(properties, "MaximumWarehouseSlotsForDwarf", 120);
			WAREHOUSE_SLOTS_NO_DWARF = getInt(properties, "MaximumWarehouseSlotsForNoDwarf", 100);
			WAREHOUSE_SLOTS_CLAN = getInt(properties, "MaximumWarehouseSlotsForClan", 150);
			ALT_FREIGHT_SLOTS = getInt(properties, "MaximumFreightSlots", 20);
			ALT_FREIGHT_PRIECE = getInt(properties, "FreightPriece", 1000);
			MAX_ENCHANT_LEVEL = getInt(properties, "MaxEnchantLevel", 0);
			ENCHANT_CHANCE = getDouble(properties, "EnchantChance", 66.66);
			ENCHANT_CHANCE_ELEMENT_STONE = getInt(properties, "EnchantChanceElementStone", 50);
			ENCHANT_CHANCE_ELEMENT_CRYSTAL = getInt(properties, "EnchantChanceElementCrystal", 30);
			ENCHANT_CHANCE_ELEMENT_JEWEL = getInt(properties, "EnchantChanceElementJewel", 20);
			ENCHANT_CHANCE_ELEMENT_ENERGY = getInt(properties, "EnchantChanceElementEnergy", 10);
			ENCHANT_SAFE_MAX = getInt(properties, "EnchantSafeMax", 3);
			ENCHANT_SAFE_MAX_FULL = getInt(properties, "EnchantSafeMaxFull", 4);
			String[] notenchantable = Config.getString(properties, "EnchantBlackList", "7816,7817,7818,7819,7820,7821,7822,7823,7824,7825,7826,7827,7828,7829,7830,7831,13293,13294,13296").split(",");
			ENCHANT_BLACKLIST = new int[notenchantable.length];
			for(int i = 0; i < notenchantable.length; i++)
			{
				ENCHANT_BLACKLIST[i] = Integer.parseInt(notenchantable[i]);
			}
			Arrays.sort(ENCHANT_BLACKLIST);
			ELEMENTAL_CUSTOM_LEVEL_ENABLE = getBoolean(properties, "ElementalCustomLevelEnable", false);
			ELEMENTAL_LEVEL_WEAPON = getInt(properties, "ElementalLevelWeapon", 14);
			ELEMENTAL_LEVEL_WEAPON = ELEMENTAL_LEVEL_WEAPON < 1 ? 1 : ELEMENTAL_LEVEL_WEAPON;
			ELEMENTAL_LEVEL_WEAPON = ELEMENTAL_LEVEL_WEAPON > 14 ? 14 : ELEMENTAL_LEVEL_WEAPON;
			ELEMENTAL_LEVEL_ARMOR = getInt(properties, "ElementalLevelArmor", 14);
			ELEMENTAL_LEVEL_ARMOR = ELEMENTAL_LEVEL_ARMOR < 1 ? 1 : ELEMENTAL_LEVEL_ARMOR;
			ELEMENTAL_LEVEL_ARMOR = ELEMENTAL_LEVEL_ARMOR > 14 ? 14 : ELEMENTAL_LEVEL_ARMOR;
			AUGMENTATION_NG_SKILL_CHANCE = getInt(properties, "AugmentationNGSkillChance", 15);
			AUGMENTATION_NG_GLOW_CHANCE = getInt(properties, "AugmentationNGGlowChance", 0);
			AUGMENTATION_MID_SKILL_CHANCE = getInt(properties, "AugmentationMidSkillChance", 30);
			AUGMENTATION_MID_GLOW_CHANCE = getInt(properties, "AugmentationMidGlowChance", 40);
			AUGMENTATION_HIGH_SKILL_CHANCE = getInt(properties, "AugmentationHighSkillChance", 45);
			AUGMENTATION_HIGH_GLOW_CHANCE = getInt(properties, "AugmentationHighGlowChance", 70);
			AUGMENTATION_TOP_SKILL_CHANCE = getInt(properties, "AugmentationTopSkillChance", 60);
			AUGMENTATION_TOP_GLOW_CHANCE = getInt(properties, "AugmentationTopGlowChance", 100);
			AUGMENTATION_FORGOTTEN_SKILL_CHANCE = getInt(properties, "AugmentationForgottenSkillChance", 80); //TODO: Guessed
			AUGMENTATION_FORGOTTEN_GLOW_CHANCE = getInt(properties, "AugmentationForgottenGlowChance", 100);
			AUGMENTATION_BASESTAT_CHANCE = getInt(properties, "AugmentationBaseStatChance", 1);
			AUGMENTATION_ACC_SKILL_CHANCE = getInt(properties, "AugmentationAccSkillChance", 0);

			String[] array = getString(properties, "AugmentationBlackList", "6656,6657,6658,6659,6660,6661,6662,8191,10170,10314,13740,13741,13742,13743,13744,13745,13746,13747,13748,14592,14593,14594,14595,14596,14597,14598,14599,14600,14664,14665,14666,14667,14668,14669,14670,14671,14672,14801,14802,14803,14804,14805,14806,14807,14808,14809,15282,15283,15284,15285,15286,15287,15288,15289,15290,15291,15292,15293,15294,15295,15296,15297,15298,15299,16025,16026,21712").split(",");
			AUGMENTATION_BLACKLIST = new int[array.length];

			for(int i = 0; i < array.length; i++)
			{
				AUGMENTATION_BLACKLIST[i] = Integer.parseInt(array[i]);
			}

			Arrays.sort(AUGMENTATION_BLACKLIST);

			MENTOR_PENALTY_FOR_MENTEE_COMPLETE = getLong(properties, "MentorPenaltyForMenteeComplete", 1) * 24 * 60 * 60 * 1000;
			MENTOR_PENALTY_FOR_MENTEE_LEAVE = getLong(properties, "MentorPenaltyForMenteeLeave", 2) * 24 * 60 * 60 * 1000;
			MAX_PERSONAL_FAME_POINTS = getInt(properties, "MaxPersonalFamePoints", 20000000);
			FORTRESS_ZONE_FAME_TASK_FREQUENCY = getInt(properties, "FortressZoneFameTaskFrequency", 300);
			FORTRESS_ZONE_FAME_AQUIRE_POINTS = getInt(properties, "FortressZoneFameAquirePoints", 310);
			CASTLE_ZONE_FAME_TASK_FREQUENCY = getInt(properties, "CastleZoneFameTaskFrequency", 300);
			CASTLE_ZONE_FAME_AQUIRE_POINTS = getInt(properties, "CastleZoneFameAquirePoints", 1250);
			FAME_FOR_DEAD_PLAYERS = getBoolean(properties, "FameForDeadPlayers", true);
			IS_CRAFTING_ENABLED = getBoolean(properties, "CraftingEnabled", true);
			DWARF_RECIPE_LIMIT = getInt(properties, "DwarfRecipeLimit", 50);
			COMMON_RECIPE_LIMIT = getInt(properties, "CommonRecipeLimit", 50);
			ALT_BLACKSMITH_USE_RECIPES = getBoolean(properties, "AltBlacksmithUseRecipes", true);
			ALT_CLAN_JOIN_DAYS = getInt(properties, "DaysBeforeJoinAClan", 1);
			ALT_CLAN_CREATE_DAYS = getInt(properties, "DaysBeforeCreateAClan", 10);
			ALT_CLAN_DISSOLVE_DAYS = getInt(properties, "DaysToPassToDissolveAClan", 7);
			ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = getInt(properties, "DaysBeforeJoinAllyWhenLeaved", 1);
			ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = getInt(properties, "DaysBeforeJoinAllyWhenDismissed", 1);
			ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = getInt(properties, "DaysBeforeAcceptNewClanWhenDismissed", 1);
			ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = getInt(properties, "DaysBeforeCreateNewAllyWhenDissolved", 1);
			ALT_MAX_NUM_OF_CLANS_IN_ALLY = getInt(properties, "AltMaxNumOfClansInAlly", 3);
			ALT_CLAN_MEMBERS_FOR_WAR = getInt(properties, "AltClanMembersForWar", 15);
			REPUTATION_BONUS_MIN_LEVEL = getInt(properties, "ReputationBonusMinLevel", 40);
			ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = getBoolean(properties, "AltMembersCanWithdrawFromClanWH", false);
			REMOVE_CASTLE_CIRCLETS = getBoolean(properties, "RemoveCastleCirclets", true);
			ALT_PARTY_RANGE = getInt(properties, "AltPartyRange", 1600);
			ALT_PARTY_RANGE2 = getInt(properties, "AltPartyRange2", 1400);
			ALT_LEAVE_PARTY_LEADER = getBoolean(properties, "AltLeavePartyLeader", false);
			STARTING_ADENA = getLong(properties, "StartingAdena", 0);
			STARTING_LEVEL = getByte(properties, "StartingLevel", Byte.parseByte("1"));
			STARTING_SP = getInt(properties, "StartingSP", 0);
			AUTO_LOOT = getBoolean(properties, "AutoLoot", false);
			AUTO_LOOT_RAIDS = getBoolean(properties, "AutoLootRaids", false);
			LOOT_RAIDS_PRIVILEGE_INTERVAL = getInt(properties, "RaidLootRightsInterval", 900) * 1000;
			LOOT_RAIDS_PRIVILEGE_CC_SIZE = getInt(properties, "RaidLootRightsCCSize", 45);
			UNSTUCK_INTERVAL = getInt(properties, "UnstuckInterval", 300);
			TELEPORT_WATCHDOG_TIMEOUT = getInt(properties, "TeleportWatchdogTimeout", 0);
			PLAYER_SPAWN_PROTECTION = getInt(properties, "PlayerSpawnProtection", 0);
			String[] items = getString(properties, "PlayerSpawnProtectionAllowedItems", "0").split(",");
			SPAWN_PROTECTION_ALLOWED_ITEMS = new ArrayList<>(items.length);
			for(String item : items)
			{
				Integer itm = 0;
				try
				{
					itm = Integer.parseInt(item);
				}
				catch(NumberFormatException nfe)
				{
					_log.log(Level.WARN, "Player Spawn Protection: Wrong ItemId passed: " + item);
					_log.log(Level.WARN, nfe);
				}
				if(itm != 0)
				{
					SPAWN_PROTECTION_ALLOWED_ITEMS.add(itm);
				}
			}
			SPAWN_PROTECTION_ALLOWED_ITEMS.trimToSize();
			PLAYER_TELEPORT_PROTECTION = getInt(properties, "PlayerTeleportProtection", 0);
			RANDOM_RESPAWN_IN_TOWN_ENABLED = getBoolean(properties, "RandomRespawnInTownEnabled", true);
			OFFSET_ON_TELEPORT_ENABLED = getBoolean(properties, "OffsetOnTeleportEnabled", true);
			MAX_OFFSET_ON_TELEPORT = getInt(properties, "MaxOffsetOnTeleport", 50);
			RESTORE_PLAYER_INSTANCE = getBoolean(properties, "RestorePlayerInstance", false);
			ALLOW_SUMMON_TO_INSTANCE = getBoolean(properties, "AllowSummonToInstance", true);
			PETITIONING_ALLOWED = getBoolean(properties, "PetitioningAllowed", true);
			MAX_PETITIONS_PER_PLAYER = getInt(properties, "MaxPetitionsPerPlayer", 5);
			MAX_PETITIONS_PENDING = getInt(properties, "MaxPetitionsPending", 25);
			ALT_GAME_FREE_TELEPORT = getBoolean(properties, "AltFreeTeleporting", false);
			DELETE_DAYS = getInt(properties, "DeleteCharAfterDays", 7);
			PARTY_XP_CUTOFF_METHOD = getString(properties, "PartyXpCutoffMethod", "auto");
			PARTY_XP_CUTOFF_PERCENT = getDouble(properties, "PartyXpCutoffPercent", 3.0);
			PARTY_XP_CUTOFF_LEVEL = getInt(properties, "PartyXpCutoffLevel", 30);
			DISABLE_TUTORIAL = getBoolean(properties, "DisableTutorial", false);
			EXPERTISE_PENALTY = getBoolean(properties, "ExpertisePenalty", true);
			STORE_RECIPE_SHOPLIST = getBoolean(properties, "StoreRecipeShopList", false);
			FORBIDDEN_NAMES = getString(properties, "ForbiddenNames", "").split(",");
			SILENCE_MODE_EXCLUDE = getBoolean(properties, "SilenceModeExclude", false);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
