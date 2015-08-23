package dwo.config;

import java.io.File;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 05.10.11
 * Time: 22:05
 */

public class FilePath
{
  public static final File ANNOUNCEMENTS = new File( "./config/announcements.txt" );

  // Player
  public static final File HIT_CONDITION_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/player/HitConditionData.xml" );
  public static final File ENCHANT_SKILL_GROUPS_TABLE = new File( Config.DATAPACK_ROOT, "/data/stats/player/EnchantSkillGroupsData.xml" );
  public static final File EXPERIENCE_TABLE = new File( Config.DATAPACK_ROOT, "/data/stats/player/ExperienceData.xml" );
  public static final File ACCESS_LEVELS = new File( Config.DATAPACK_ROOT, "/data/stats/player/accesscontrol/AccessLevelsData.xml" );
  public static final File ADMIN_COMMAND_ACCESS_RIGHTS = new File( Config.DATAPACK_ROOT, "/data/stats/player/accesscontrol/AccessRightsData.xml" );
  public static final File CHAR_STARTING_ITEMS_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/player/CharStartingItems.xml" );
  public static final File BASE_STAT_BONUS_DATA = new File( Config.DATAPACK_ROOT + "/data/stats/player/BaseStatBonusData.xml" );

  public static final File SKILL_TREES_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/player/skillTrees/" );
  public static final File CLASS_TEMPLATES_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/player/class_data/" );
  public static final File CHAR_TEMPLATE_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/player/template_data" );

  // Skills
  public static final File SKILL_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/skills/base/" );
  public static final File CUSTOM_SKILL_DATA = new File( Config.DATAPACK_ROOT, "/custom/skills/" );
  public static final File BUFF_STACK_GROUP_DATA = new File( Config.DATAPACK_ROOT + "/data/stats/skills/BuffStackGroupData.xml" );

  // Items
  public static final File ITEMS_DIR = new File( Config.DATAPACK_ROOT, "/data/stats/items/base/" );
  public static final File CUSTOM_ITEMS_DIR = new File( Config.DATAPACK_ROOT, "/custom/items/" );
  public static final File CRYSTALLIZATION_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/items/CrystallizationData.xml" );
  public static final File SHAPE_SHIFTING_ITEMS = new File( Config.DATAPACK_ROOT, "/data/stats/items/ShapeShiftingItems.xml" );
  public static final File ENCHANT_ARMOR_HP_BONUS_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/items/EnchantArmorHPBonusData.xml" );
  public static final File ENCHANT_ITEM_TABLE = new File( Config.DATAPACK_ROOT, "/data/stats/items/EnchantItemData.xml" );
  public static final File RECIPE_DATA = new File( Config.DATAPACK_ROOT + "/data/stats/items/RecipeData.xml" );
  public static final File ARMOR_SETS_DIR = new File( Config.DATAPACK_ROOT, "/data/stats/items/armorsets/" );
  public static final File AUGMENTATION_SKILLMAP = new File( Config.DATAPACK_ROOT + "/data/stats/items/augmentation/augmentation_skillmap.xml" );
  public static final File AUGMENTATION_DIR = new File( Config.DATAPACK_ROOT + "/data/stats/items/augmentation/" );
  public static final File HENNA_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/items/henna/HennasData.xml" );
  public static final File HENNA_TREE_TABLE = new File( Config.DATAPACK_ROOT, "/data/stats/items/henna/HennasTreesData.xml" );
  public static final File FISH_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/items/FishData.xml" );
  public static final File SOUL_CRYSTALS_UPGRADE_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/items/SoulCrystalsUpgradeData.xml" );
  //TODO public static final File VARIATION_DATA = new File(Config.DATAPACK_ROOT, "/data/stats/items/variation_data/");
  public static final File OPTION_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/items/option_data/" );
  public static final File ITEM_PRICE_DATA = new File( Config.DATAPACK_ROOT + "/data/stats/items/ItemPrice.xml" );
  public static final File PRIME_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/shop/PrimeShopData.xml" );

  // Shop
  public static final File MANOR_DATA = new File( Config.DATAPACK_ROOT, "data/stats/shop/ManorData.xml" );
  public static final File ITEM_AUCTION_MANAGER = new File( Config.DATAPACK_ROOT + "/data/stats/shop/ItemAuctionData.xml" );
  public static final File BUYLIST_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/shop/BuylistData.xml" );
  public static final File BEAUTY_SHOP_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/shop/BeautyShopData.xml" );
  public static final File MULTISELL_DIR = new File( Config.DATAPACK_ROOT, "/data/stats/shop/multisell/" );
  public static final File CUSTOM_MULTISELL_DIR = new File( Config.DATAPACK_ROOT, "/custom/multisell/" );

  // World
  public static final File RESIDENCE_FUNCTION_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/world/ResidenceFunctionData.xml" );
  public static final File DYNAMIC_QUEST_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/world/DynamicQuestsData.xml" );
  public static final File INSTANCE_NAMES_DATA = new File( Config.DATAPACK_ROOT, "data/stats/world/InstanceNamesData.xml" );
  public static final File CURSED_WEAPONS_MANAGER = new File( Config.DATAPACK_ROOT + "/data/stats/world/CursedWeaponsData.xml" );
  public static final File JUMP_ROUTES = new File( Config.DATAPACK_ROOT, "/data/stats/world/JumpRoutesData.xml" );
  public static final File HELL_BOUND_TRUST_POINTS_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/world/HellboundTrustPointsData.xml" );
  public static final File FISH_RODS_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/world/FishRodsData.xml" );
  public static final File FISH_MONSTERS_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/world/FishMonstersData.xml" );
  public static final File OBSCENE_FILTER_TABLE = new File( Config.DATAPACK_ROOT, "/data/stats/world/ObsceneFilterTable.xml" );
  public static final File COMMUNITY_BUFFS = new File( Config.DATAPACK_ROOT, "/data/stats/world/CommunityBuffs.xml" );
  public static final File COMMUNITY_TELEPORT = new File( Config.DATAPACK_ROOT, "/data/stats/world/CommunityTeleport.xml" );
  public static final File MAP_REGION_DATA = new File( Config.DATAPACK_ROOT, "/data/maps/mapregion/" );
  public static final File ZONE_DATA = new File( Config.DATAPACK_ROOT, "/data/maps/zones/" );
  public static final File GR_SEED_OF_DESTRUCTION = new File( Config.DATAPACK_ROOT, "/data/stats/world/scripts/GR_SeedOfDestruction.xml" );
  public static final File FORT_SPAWNLIST = new File( Config.DATAPACK_ROOT, "/data/stats/world/FortSpawnList.xml" );
  public static final File RESIDENCE_SIEGE_MUSIC_LIST = new File( Config.DATAPACK_ROOT, "/data/stats/world/ResidenceSiegeMusicList.xml" );

  // Npc
  public static final File TELEPORT_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/npc/TeleportsData.xml" );
  public static final File SUMMON_POINTS_TABLE = new File( Config.DATAPACK_ROOT, "/data/stats/npc/SummonPointsData.xml" );
  public static final File AUTO_CHAT_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/npc/AutoChatData.xml" );
  public static final File NPC_WALKER_ROUTES_TABLE = new File( Config.DATAPACK_ROOT, "/data/stats/npc/WalkingRoutesAutoData.xml" );
  public static final File WALKING_MANAGER = new File( Config.DATAPACK_ROOT, "/data/stats/npc/WalkingRoutesData.xml" );
  public static final File DOOR_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/npc/DoorsData.xml" );
  public static final File HERB_DROP = new File( Config.DATAPACK_ROOT, "/data/stats/npc/HerbDropData.xml" );
  public static final File STATIC_OBJECTS_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/npc/StaticObjectsData.xml" );
  public static final File SUMMON_ITEMS_DATA = new File( Config.DATAPACK_ROOT + "/data/stats/npc/SummonItemsData.xml" );
  public static final File DYNAMIC_SPAWN_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/npc/spawn/dynamic/" );
  public static final File STATIC_SPAWN_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/npc/spawn/static/" );
  public static final File CUSTOM_STATIC_SPAWN_DATA = new File( Config.DATAPACK_ROOT, "/custom/spawn/" );
  public static final File PET_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/npc/pets/" );
  public static final File DROPLIST_DIR = new File( Config.DATAPACK_ROOT, "/data/stats/npc/droplist/" );
  public static final File CUSTOM_DROPLIST_DIR = new File( Config.DATAPACK_ROOT, "/custom/droplist/" );
  public static final File NPC_STATS = new File( Config.DATAPACK_ROOT, "/data/stats/npc/data/" );
  public static final File CUSTOM_NPC_STATS = new File( Config.DATAPACK_ROOT, "/custom/npc/" );

  // Scripts
  public static final File RSS_CACHE = new File( Config.DATAPACK_ROOT + "/data/cache/RssCache.xml" );
  public static final File ITEMS_ON_LEVEL_DATA = new File( Config.DATAPACK_ROOT + "/data/stats/custom/ItemsOnLevel.xml" );
  public static final File RAIDRADAR_TABLE = new File( Config.DATAPACK_ROOT + "/data/stats/world/scripts/RaidRadarTable.xml" );

  //Ability
  public static final File ABILITY_SKILL_DATA = new File( Config.DATAPACK_ROOT, "/data/stats/skills/ability/AbilityPoints.xml" );

  //Alchemy
  public static final File ALCHEMY_CONVERSION_DATA_FILE = new File( Config.DATAPACK_ROOT, "data/stats/world/AlchemyConversionData.xml" );
}