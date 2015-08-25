package dwo.config;

import dwo.config.events.ConfigEvents;
import dwo.config.main.*;
import dwo.config.mods.*;
import dwo.config.network.*;
import dwo.config.scripts.ConfigChaosFestival;
import dwo.config.security.ConfigProtectionAdmin;
import dwo.config.security.ConfigSecurityAuth;
import dwo.gameserver.network.login.gameserverpackets.ServerStatus;
import dwo.gameserver.util.StringUtil;
import dwo.gameserver.util.Tools;
import dwo.gameserver.util.floodprotector.FloodProtectorConfig;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class Config
{
	public static final String FORTSIEGE_CONFIGURATION_FILE = "./config/main/FortSiege.ini";
    // Folder Main
	// Character.ini
	public static final String CHARACTER_CONFIG = "./config/main/Character.ini";
	// Feature.ini
	public static final String FEATURE_CONFIG = "./config/main/Feature.ini";
	// FloodProtector.ini
	public static final String FLOOD_PROTECTOR_CONFIG = "./config/main/FloodProtector.ini";
	// General.ini
	public static final String GENERAL_CONFIG = "./config/main/General.ini";
	// GeoData.ini
	public static final String GEODATA_CONFIG_FILE = "./config/main/GeoData.ini";
	// GrandBoss.ini
	public static final String GRAND_BOSS_CONFIG = "./config/main/GrandBoss.ini";
	// IDFactory.ini
	public static final String ID_FACTORY_CONFIG = "./config/main/IDFactory.ini";
	// Logger.cfg
	public static final String LOGGER_CONFIG = "./config/main/Logger.ini";
	// MMO.ini
	public static final String MMO_CONFIG = "./config/main/MMO.ini";
	// NPC.ini
	public static final String NPC_CONFIG = "./config/main/NPC.ini";
	// Olympiad.ini
	public static final String OLYMPIAD_CONFIG = "./config/main/Olympiad.ini";
	// PvP.ini
	public static final String PVP_CONFIG = "./config/main/PvP.ini";
	// Rates.ini
	public static final String RATES_CONFIG = "./config/main/Rates.ini";
	// CastleSiegeEngine.ini
	public static final String SIEGE_CONFIGURATION_FILE = "./config/main/Siege.ini";
	// Folder Mods
	// ItemMall.ini
	public static final String L2JS_ITEMMALL_CONFIG = "./config/mods/ItemMall.ini";
	// VoteSystem.ini
	public static final String VOTE_SYSTEM_CONFIG_FILE = "./config/mods/VoteSystem.ini";
	// Banking.ini
	public static final String L2JS_BANKING_CONFIG = "./config/mods/Banking.ini";
	// Birthday.ini
	public static final String BIRTHDAY_EVENT_CONFIG = "./config/mods/BirthDay.ini";
	// Champion.ini
	public static final String L2JS_CHAMPION_CONFIG = "./config/mods/Champion.ini";
	// Chars.ini
	public static final String L2JS_CHARS_CONFIG = "./config/mods/Chars.ini";
	// Chat.ini
	public static final String L2JS_CHAT_CONFIG = "./config/mods/Chat.ini";
	// Custom.ini
	public static final String L2GOD_CUSTOM_CONFIG = "./config/mods/Custom.ini";
	// GraciaSeeds.ini
	public static final String L2JS_GRACIA_SEEDS_CONFIG = "./config/mods/GraciaSeeds.ini";
	// OfflineTrade.ini
	public static final String L2JS_OFFLINE_TRADE_CONFIG = "./config/mods/OfflineTrade.ini";
	// Wedding.ini
	public static final String L2JS_WEDDING_CONFIG = "./config/mods/Wedding.ini";
	// PcBang.ini
	public static final String PCCAFE_CONFIGURATION_FILE = "./config/mods/PcBangCafe.ini";
	// Premium.ini
	public static final String PREMIUM_CONFIGURATION_FILE = "./config/mods/Premium.ini";
	// DynamicSpawn.ini
	public static final String DYNAMIC_SPAWN_CONFIGURATION_FILE = "./config/mods/DynamicSpawn.ini";
	// Folder Network
	// CommunityServer.ini
	public static final String COMMUNITY_SERVER_CONFIG = "./config/network/CommunityServer.ini";
	// Hexid.txt
	public static final String HEXID_CONFIG = "./config/network/hexid.txt";
	// GameServer.ini
	public static final String GAME_SERVER_CONFIG = "./config/network/GameServer.ini";
	// IPConfig.xml
	public static final String IPCONFIG_CONFIG = "./config/network/IPConfig.xml";
	// LoginServer.ini
	public static final String LOGIN_SERVER_CONFIG = "./config/network/LoginServer.ini";
	// Folder Security
	// ProtectionAdmin.ini
	public static final String L2JS_PROTECTION_ADMIN = "./config/security/ProtectionAdmin.ini";
	// SecurityAuth.ini
	public static final String SECURITY_CONFIG_FILE = "./config/security/SecurityAuth.ini";
	protected static final Logger _log = LogManager.getLogger(Config.class);

    /*
      * No classification assigned to the following yet
      */
	public static boolean CHECK_KNOWN;
	public static boolean RESERVE_HOST_ON_LOGIN;
	/*
	  * Attributes used Globally
	  */
	public static String GAME_SERVER_LOGIN_HOST;
	public static int GAME_SERVER_LOGIN_PORT;
	public static boolean DEBUG;
	public static String MYSQL_DB;
	public static String DATABASE_HOST;
	public static int DATABASE_PORT;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MAX_CONNECTIONS;
	public static boolean USE_UTF8;
	public static boolean ALT_GAME_DELEVEL;
	public static boolean DECREASE_SKILL_LEVEL;
	public static double ALT_WEIGHT_LIMIT;
	public static int RUN_SPD_BOOST;
	public static double RESPAWN_RESTORE_CP;
	public static double RESPAWN_RESTORE_HP;
	public static double RESPAWN_RESTORE_MP;
	public static boolean ALT_GAME_TIREDNESS;
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	public static TIntIntHashMap SKILL_DURATION_LIST;
	public static boolean ENABLE_MODIFY_SKILL_REUSE;
	public static TIntIntHashMap SKILL_REUSE_LIST;
	public static boolean AUTO_LEARN_SKILLS;
	public static boolean AUTO_LEARN_FS_SKILLS;
	public static byte BUFFS_MAX_AMOUNT;
	public static byte TRIGGERED_BUFFS_MAX_AMOUNT;
	public static byte DANCES_MAX_AMOUNT;
	public static boolean DANCE_CANCEL_BUFF;
	public static boolean DANCE_CONSUME_ADDITIONAL_MP;
	public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
	public static boolean EFFECT_CANCELING;
	public static boolean ALT_GAME_MAGICFAILURES;
	public static int PLAYER_FAKEDEATH_UP_PROTECTION;
	public static boolean STORE_SKILL_COOLTIME;
	public static boolean SUBCLASS_STORE_SKILL_COOLTIME;
	public static boolean SUMMON_STORE_SKILL_COOLTIME;
	public static int ALT_PERFECT_SHLD_BLOCK;
	public static boolean LIFE_CRYSTAL_NEEDED;
	public static boolean ES_SP_BOOK_NEEDED;
	public static boolean DIVINE_SP_BOOK_NEEDED;
	public static boolean ALLOW_AUTOLOOT_COMMAND;
	public static boolean ALT_GAME_SKILL_LEARN;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALLOW_TRANSFORM_WITHOUT_QUEST;
	public static double MAX_BONUS_EXP;
	public static double MAX_BONUS_SP;
	public static int MAX_RUN_SPEED;
	public static int MAX_PCRIT_RATE;
	public static int MAX_MCRIT_RATE;
	public static int MAX_PATK_SPEED;
	public static int MAX_MATK_SPEED;
	public static int MAX_EVASION_RATE;
	public static int MIN_DEBUFF_CHANCE;
	public static int MAX_DEBUFF_CHANCE;
	public static byte MAX_SUBCLASS;
	public static byte MAX_SUBCLASS_LEVEL;
	public static int MAX_PVTSTORESELL_SLOTS_DWARF;
	public static int MAX_PVTSTORESELL_SLOTS_OTHER;
	public static int MAX_PVTSTOREBUY_SLOTS_DWARF;
	public static int MAX_PVTSTOREBUY_SLOTS_OTHER;
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int INVENTORY_MAXIMUM_QUEST_ITEMS;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static int ALT_FREIGHT_SLOTS;
	public static int ALT_FREIGHT_PRIECE;
	public static long MENTOR_PENALTY_FOR_MENTEE_COMPLETE;
	public static long MENTOR_PENALTY_FOR_MENTEE_LEAVE;
	public static int MAX_PERSONAL_FAME_POINTS;
	public static int FORTRESS_ZONE_FAME_TASK_FREQUENCY;
	public static int FORTRESS_ZONE_FAME_AQUIRE_POINTS;
	public static int CASTLE_ZONE_FAME_TASK_FREQUENCY;
	public static int CASTLE_ZONE_FAME_AQUIRE_POINTS;
	public static boolean FAME_FOR_DEAD_PLAYERS;
	public static boolean IS_CRAFTING_ENABLED;
	public static int DWARF_RECIPE_LIMIT;
	public static int COMMON_RECIPE_LIMIT;
	public static boolean ALT_BLACKSMITH_USE_RECIPES;
	public static int ALT_CLAN_JOIN_DAYS;
	public static int ALT_CLAN_CREATE_DAYS;
	public static int ALT_CLAN_DISSOLVE_DAYS;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
	public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
	public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
	public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
	public static int ALT_CLAN_MEMBERS_FOR_WAR;
	public static int REPUTATION_BONUS_MIN_LEVEL;
	public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
	public static boolean REMOVE_CASTLE_CIRCLETS;
	public static int ALT_PARTY_RANGE;
	public static int ALT_PARTY_RANGE2;
	public static boolean ALT_LEAVE_PARTY_LEADER;
	public static long STARTING_ADENA;
	public static byte STARTING_LEVEL;
	public static int STARTING_SP;
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_RAIDS;
	public static int LOOT_RAIDS_PRIVILEGE_INTERVAL;
	public static int LOOT_RAIDS_PRIVILEGE_CC_SIZE;
	public static int UNSTUCK_INTERVAL;
	public static int TELEPORT_WATCHDOG_TIMEOUT;
	public static int PLAYER_SPAWN_PROTECTION;
	public static ArrayList<Integer> SPAWN_PROTECTION_ALLOWED_ITEMS;
	public static int PLAYER_TELEPORT_PROTECTION;
	public static boolean RANDOM_RESPAWN_IN_TOWN_ENABLED;
	public static boolean OFFSET_ON_TELEPORT_ENABLED;
	public static int MAX_OFFSET_ON_TELEPORT;
	public static boolean RESTORE_PLAYER_INSTANCE;
	public static boolean ALLOW_SUMMON_TO_INSTANCE;
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	public static boolean ALT_GAME_FREE_TELEPORT;
	public static int DELETE_DAYS;
	public static String PARTY_XP_CUTOFF_METHOD;
	public static double PARTY_XP_CUTOFF_PERCENT;
	public static int PARTY_XP_CUTOFF_LEVEL;
	public static boolean DISABLE_TUTORIAL;
	public static boolean EXPERTISE_PENALTY;
	public static boolean STORE_RECIPE_SHOPLIST;
	public static String[] FORBIDDEN_NAMES;
	public static boolean SILENCE_MODE_EXCLUDE;
	public static int MAX_ENCHANT_LEVEL;
	public static double ENCHANT_CHANCE;
	public static int ENCHANT_CHANCE_ELEMENT_STONE;
	public static int ENCHANT_CHANCE_ELEMENT_CRYSTAL;
	public static int ENCHANT_CHANCE_ELEMENT_JEWEL;
	public static int ENCHANT_CHANCE_ELEMENT_ENERGY;
	public static int ENCHANT_SAFE_MAX;
	public static int ENCHANT_SAFE_MAX_FULL;
	public static int[] ENCHANT_BLACKLIST;
	public static boolean ELEMENTAL_CUSTOM_LEVEL_ENABLE;
	public static int ELEMENTAL_LEVEL_WEAPON;
	public static int ELEMENTAL_LEVEL_ARMOR;
	public static int AUGMENTATION_NG_SKILL_CHANCE;
	public static int AUGMENTATION_NG_GLOW_CHANCE;
	public static int AUGMENTATION_MID_SKILL_CHANCE;
	public static int AUGMENTATION_MID_GLOW_CHANCE;
	public static int AUGMENTATION_HIGH_SKILL_CHANCE;
	public static int AUGMENTATION_HIGH_GLOW_CHANCE;
	public static int AUGMENTATION_TOP_SKILL_CHANCE;
	public static int AUGMENTATION_TOP_GLOW_CHANCE;
	public static int AUGMENTATION_FORGOTTEN_SKILL_CHANCE;
	public static int AUGMENTATION_FORGOTTEN_GLOW_CHANCE;
	public static int AUGMENTATION_BASESTAT_CHANCE;
	public static int AUGMENTATION_ACC_SKILL_CHANCE;
	public static int[] AUGMENTATION_BLACKLIST;
	public static double HP_REGEN_MULTIPLIER;
	public static double MP_REGEN_MULTIPLIER;
	public static double CP_REGEN_MULTIPLIER;
	public static boolean ENABLE_VITALITY;
	public static boolean DIVIDE_VITALITY_GAIN_BYLEVEL;
	public static int STARTING_VITALITY_POINTS;
	public static int VITALITY_ITEMS_WEEKLY_LIMIT;
	public static int MAX_ITEM_IN_PACKET;
	public static Integer SIEGE_HOUR;
	public static int FS_BLOOD_OATH_COUNT;
	public static int FS_UPDATE_FRQ;
	public static int FS_MAX_SUPPLY_LEVEL;
	public static int FS_FEE_FOR_CASTLE;
	public static int FS_MAX_OWN_TIME;
	public static int TAKE_FORT_POINTS;
	public static int LOOSE_FORT_POINTS;
	public static int TAKE_CASTLE_POINTS;
	public static int LOOSE_CASTLE_POINTS;
	public static int CASTLE_DEFENDED_POINTS;
	public static int FESTIVAL_WIN_POINTS;
	public static int HERO_POINTS;
	public static int ROYAL_GUARD_COST;
	public static int KNIGHT_UNIT_COST;
	public static int KNIGHT_REINFORCE_COST;
	public static int BALLISTA_POINTS;
	public static int BLOODALLIANCE_POINTS;
	public static int BLOODOATH_POINTS;
	public static int KNIGHTSEPAULETTE_POINTS;
	public static int REPUTATION_SCORE_PER_KILL;
	public static int RAID_RANKING_1ST;
	public static int RAID_RANKING_2ND;
	public static int RAID_RANKING_3RD;
	public static int RAID_RANKING_4TH;
	public static int RAID_RANKING_5TH;
	public static int RAID_RANKING_6TH;
	public static int RAID_RANKING_7TH;
	public static int RAID_RANKING_8TH;
	public static int RAID_RANKING_9TH;
	public static int RAID_RANKING_10TH;
	public static int RAID_RANKING_UP_TO_50TH;
	public static int RAID_RANKING_UP_TO_100TH;
	public static int CLAN_LEVEL_6_COST;
	public static int CLAN_LEVEL_7_COST;
	public static int CLAN_LEVEL_8_COST;
	public static int CLAN_LEVEL_9_COST;
	public static int CLAN_LEVEL_10_COST;
	public static int CLAN_LEVEL_11_COST;
	public static int CLAN_LEVEL_6_REQUIREMENT;
	public static int CLAN_LEVEL_7_REQUIREMENT;
	public static int CLAN_LEVEL_8_REQUIREMENT;
	public static int CLAN_LEVEL_9_REQUIREMENT;
	public static int CLAN_LEVEL_10_REQUIREMENT;
	public static int CLAN_LEVEL_11_REQUIREMENT;
	public static boolean ALLOW_WYVERN_DURING_SIEGE;
	public static int PLAYER_MOVEMENT_BLOCK_TIME;
	public static int MAMMONS_TELEPORT_RATE;
	public static boolean MAMMONS_VOICE_LOC_ENABLE;
	public static boolean CLEAR_CREST_CACHE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_USE_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ROLL_DICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_FIREWORK;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ITEM_PET_SUMMON;
	public static FloodProtectorConfig FLOOD_PROTECTOR_HERO_VOICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_GLOBAL_CHAT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SUBCLASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_DROP_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SERVER_BYPASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MULTISELL;
	public static FloodProtectorConfig FLOOD_PROTECTOR_TRANSACTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANUFACTURE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANOR;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SENDMAIL;
	public static FloodProtectorConfig FLOOD_PROTECTOR_CHARACTER_SELECT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ITEM_AUCTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_COMMUNITY_BOARD;
	public static FloodProtectorConfig FLOOD_PROTECTOR_CLAN_SEARCH;
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	public static boolean SERVER_LIST_BRACKET;
	public static int SERVER_LIST_TYPE;
	public static int SERVER_LIST_AGE;
	public static boolean SERVER_GMONLY;
	public static boolean USE_HTML_CACHE;
	public static boolean GM_HERO_AURA;
	public static boolean GM_STARTUP_INVULNERABLE;
	public static boolean GM_STARTUP_INVISIBLE;
	public static boolean GM_STARTUP_SILENCE;
	public static boolean GM_STARTUP_AUTO_LIST;
	public static boolean GM_STARTUP_DIET_MODE;
	public static String GM_ADMIN_MENU_STYLE;
	public static boolean GM_ITEM_RESTRICTION;
	public static boolean GM_SKILL_RESTRICTION;
	public static boolean GM_TRADE_RESTRICTED_ITEMS;
	public static boolean GM_RESTART_FIGHTING;
	public static boolean GM_ANNOUNCER_NAME;
	public static boolean GM_GIVE_SPECIAL_SKILLS;
	public static boolean GM_GIVE_SPECIAL_AURA_SKILLS;
	public static boolean BYPASS_VALIDATION;
	public static boolean GAMEGUARD_ENFORCE;
	public static boolean GAMEGUARD_PROHIBITACTION;
	public static boolean SKILL_CHECK_ENABLE;
	public static boolean SKILL_CHECK_REMOVE;
	public static boolean SKILL_CHECK_GM;
	public static boolean ACCEPT_GEOEDITOR_CONN;
	public static boolean TEST_SERVER;
	public static boolean ALT_DEV_NO_QUESTS;
	public static boolean DYNAMIC_QUEST_SYSTEM;
	public static boolean ALT_DEV_NO_SPAWNS;
	public static int THREAD_P_EFFECTS;
	public static int THREAD_P_GENERAL;
	public static int GENERAL_PACKET_THREAD_CORE_SIZE;
	public static int IO_PACKET_THREAD_CORE_SIZE;
	public static int GENERAL_THREAD_CORE_SIZE;
	public static int AI_MAX_THREAD;
	public static int CLIENT_PACKET_QUEUE_SIZE;
	public static int CLIENT_PACKET_QUEUE_MAX_BURST_SIZE;
	public static int CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND;
	public static int CLIENT_PACKET_QUEUE_MEASURE_INTERVAL;
	public static int CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND;
	public static int CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN;
	public static boolean DEADLOCK_DETECTOR;
	public static int DEADLOCK_CHECK_INTERVAL;
	public static boolean RESTART_ON_DEADLOCK;
	public static boolean ALLOW_DISCARDITEM;
	public static int AUTODESTROY_ITEM_AFTER;
	public static int HERB_AUTO_DESTROY_TIME;
	public static TIntArrayList LIST_PROTECTED_ITEMS;
	public static int CHAR_STORE_INTERVAL;
	public static boolean LAZY_ITEMS_UPDATE;
	public static boolean UPDATE_ITEMS_ON_CHAR_STORE;
	public static boolean DESTROY_DROPPED_PLAYER_ITEM;
	public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
	public static boolean SAVE_DROPPED_ITEM;
	public static boolean EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
	public static int SAVE_DROPPED_ITEM_INTERVAL;
	public static boolean CLEAR_DROPPED_ITEM_TABLE;
	public static boolean AUTODELETE_INVALID_QUEST_DATA;
	public static boolean PRECISE_DROP_CALCULATION;
	public static boolean MULTIPLE_ITEM_DROP;
	public static boolean FORCE_INVENTORY_UPDATE;
	public static boolean LAZY_CACHE;
	public static boolean CACHE_CHAR_NAMES;
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	public static int MIN_MONSTER_ANIMATION;
	public static int MAX_MONSTER_ANIMATION;
	public static boolean ENABLE_FALLING_DAMAGE;
	public static boolean GRIDS_ALWAYS_ON;
	public static int GRID_NEIGHBOR_TURNON_TIME;
	public static int GRID_NEIGHBOR_TURNOFF_TIME;
	public static boolean MOVE_BASED_KNOWNLIST;
	public static long KNOWNLIST_UPDATE_INTERVAL;
	public static int PEACE_ZONE_MODE;
	public static String DEFAULT_GLOBAL_CHAT;
	public static String DEFAULT_TRADE_CHAT;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean WAREHOUSE_CACHE;
	public static int WAREHOUSE_CACHE_TIME;
	public static boolean ALLOW_MAIL;
	public static boolean ALLOW_ATTACHMENTS;
	public static boolean ALLOW_WEAR;
	public static int WEAR_DELAY;
	public static int WEAR_PRICE;
	public static boolean ALLOW_LOTTERY;
	public static boolean ALLOW_RACE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOWFISHING;
	public static boolean ALLOW_BOAT;
	public static int BOAT_BROADCAST_RADIUS;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean ALLOW_MANOR;
	public static boolean ALLOW_NPC_WALKERS;
	public static boolean ALLOW_PET_WALKERS;
	public static boolean SERVER_NEWS;
	public static int COMMUNITY_TYPE;
	public static boolean BBS_SHOW_PLAYERLIST;
	public static String BBS_DEFAULT;
	public static boolean SHOW_LEVEL_COMMUNITYBOARD;
	public static boolean SHOW_STATUS_COMMUNITYBOARD;
	public static int NAME_PAGE_SIZE_COMMUNITYBOARD;
	public static int NAME_PER_ROW_COMMUNITYBOARD;
	public static boolean USE_SAY_FILTER;
	public static String CHAT_FILTER_CHARS;
	public static int ALT_MANOR_REFRESH_TIME;
	public static int ALT_MANOR_REFRESH_MIN;
	public static int ALT_MANOR_APPROVE_TIME;
	public static int ALT_MANOR_APPROVE_MIN;
	public static int ALT_MANOR_MAINTENANCE_PERIOD;
	public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
	public static int ALT_MANOR_SAVE_PERIOD_RATE;
	public static long ALT_LOTTERY_PRIZE;
	public static long ALT_LOTTERY_TICKET_PRICE;
	public static float ALT_LOTTERY_5_NUMBER_RATE;
	public static float ALT_LOTTERY_4_NUMBER_RATE;
	public static float ALT_LOTTERY_3_NUMBER_RATE;
	public static long ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	public static boolean ALT_ITEM_AUCTION_ENABLED;
	public static int ALT_ITEM_AUCTION_EXPIRED_AFTER;
	public static long ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
	public static int FS_TIME_ATTACK;
	public static int FS_TIME_COOLDOWN;
	public static int FS_TIME_ENTRY;
	public static int FS_TIME_WARMUP;
	public static int FS_PARTY_MEMBER_COUNT;
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY;
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME_MIN;
	public static int RIFT_AUTO_JUMPS_TIME_MAX;
	public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	public static int DEFAULT_PUNISH;
	public static int DEFAULT_PUNISH_PARAM;
	public static boolean ANNOUNCE_PUNISHMENTS;
	public static boolean ONLY_GM_ITEMS_FREE;
	public static boolean JAIL_IS_PVP;
	public static boolean JAIL_DISABLE_CHAT;
	public static boolean JAIL_DISABLE_TRANSACTION;
	public static int WORLD_X_MIN;
	public static int WORLD_X_MAX;
	public static int WORLD_Y_MIN;
	public static int WORLD_Y_MAX;
	public static boolean GEODATA_ENABLED;
	public static String GEODATA_DRIVER;
	public static boolean GEODATA_DRIVER_INC_OPTIMIZE;
	public static boolean GEODATA_HEIGHT_DIFF_MOVE_CHECKS;
	public static boolean GEODATA_ALT_MOVE_CHECKS;
	public static int GEODATA_PATHFINDING_MODE;
	public static boolean GEODATA_PATHFINDING_ADVANCED_PATH_FILTER_PC;
	public static boolean GEODATA_PATHFINDING_STOP_IF_NO_PATH_FOUND_PC;
	public static float GEODATA_PATHFINDING_HEURISTIC_MOD_PC;
	public static float GEODATA_PATHFINDING_HEURISTIC_MOD_NPC;
	public static boolean GEODATA_PATHFINDING_ALLOW_DIAGONAL_MOVEMENT;
	public static boolean GEODATA_PATHFINDING_3D_MOVEMENT;
	public static int GEODATA_PATHFINDING_COMPUTE_BUFFER_CAPACITY;
	public static int COORD_SYNCHRONIZE;
	public static int ANTHARAS_WAIT_TIME;
	public static int VALAKAS_WAIT_TIME;
	public static int INTERVAL_OF_ANTHARAS_SPAWN;
	public static int RANDOM_OF_ANTHARAS_SPAWN;
	public static int INTERVAL_OF_VALAKAS_SPAWN;
	public static int RANDOM_OF_VALAKAS_SPAWN;
	public static int INTERVAL_OF_BAIUM_SPAWN;
	public static int RANDOM_OF_BAIUM_SPAWN;
	public static int INTERVAL_OF_CORE_SPAWN;
	public static int RANDOM_OF_CORE_SPAWN;
	public static int INTERVAL_OF_ORFEN_SPAWN;
	public static int RANDOM_OF_ORFEN_SPAWN;
	public static int INTERVAL_OF_QUEEN_ANT_SPAWN;
	public static int RANDOM_OF_QUEEN_ANT_SPAWN;
	public static int BELETH_MIN_PLAYERS;
	public static int INTERVAL_OF_BELETH_SPAWN;
	public static int RANDOM_OF_BELETH_SPAWN;
	public static int MIN_FREYA_PLAYERS;
	public static int MAX_FREYA_PLAYERS;
	public static int MIN_LEVEL_PLAYERS;
	public static int MIN_FREYA_HC_PLAYERS;
	public static int MAX_FREYA_HC_PLAYERS;
	public static int MIN_LEVEL_FREYA_HC_PLAYERS;

    public static int INTERVAL_OF_LINDVIOR_SPAWN;
    public static int RANDOM_OF_LINDVIOR_SPAWN;
	// Истхина
	public static int MIN_ISTINA_PLAYERS;
	public static int MAX_ISTINA_PLAYERS;
	public static int MIN_LEVEL_ISTINA_PLAYERS;
	public static int MIN_ISTINA_HARD_PLAYERS;
	public static int MAX_ISTINA_HARD_PLAYERS;
	public static int MIN_LEVEL_ISTINA_HARD_PLAYERS;
	// Октавис
	public static int MIN_OCTAVIS_PLAYERS;
	public static int MAX_OCTAVIS_PLAYERS;
	public static int MIN_LEVEL_OCTAVIS_PLAYERS;
	public static int MIN_OCTAVIS_HARD_PLAYERS;
	public static int MAX_OCTAVIS_HARD_PLAYERS;
	public static int MIN_LEVEL_OCTAVIS_HARD_PLAYERS;
	// Байлор
	public static int MIN_BAILOR_PLAYERS;
	public static int MAX_BAILOR_PLAYERS;
	public static int MIN_LEVEL_BAILOR_PLAYERS;
	// Валлок
	public static int MIN_BALOK_PLAYERS;
	public static int MAX_BALOK_PLAYERS;
	public static int MIN_LEVEL_BALOK_PLAYERS;
	// Таути
	public static int MIN_TAUTI_PLAYERS;
	public static int MAX_TAUTI_PLAYERS;
	public static int MIN_LEVEL_TAUTI_PLAYERS;
	public static int MIN_TAUTI_HARD_PLAYERS;
	public static int MAX_TAUTI_HARD_PLAYERS;
	public static int MIN_LEVEL_TAUTI_HARD_PLAYERS;
	// Фортуна
	public static int MIN_FORTUNA_PLAYERS;
	public static int MAX_FORTUNA_PLAYERS;
	public static int MIN_LEVEL_FORTUNA_PLAYERS;
	public static ObjectMapType MAP_TYPE;
	public static ObjectSetType SET_TYPE;
	public static IdFactoryType IDFACTORY_TYPE;
	public static boolean BAD_ID_CHECKING;
	public static boolean ALLOW_LOG_FILE;
	public static boolean LOG_CHAT;
	public static boolean LOG_ITEMS;
	public static boolean LOG_ITEM_ENCHANTS;
	public static boolean LOG_SKILL_ENCHANTS;
	public static boolean GMAUDIT;
	public static boolean LOG_GAME_DAMAGE;
	public static int LOG_GAME_DAMAGE_THRESHOLD;
	public static int MMO_SELECTOR_SLEEP_TIME;
	public static int MMO_MAX_SEND_PER_PASS;
	public static int MMO_MAX_READ_PER_PASS;
	public static int MMO_HELPER_BUFFER_COUNT;
	public static boolean MMO_TCP_NODELAY;
	public static boolean EXP_SP_WITHOUT_PENALTY;
	public static boolean DROP_WITHOUT_PENALTY;
	public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
	public static boolean ALT_ATTACKABLE_NPCS;
	public static boolean ALT_GAME_VIEWNPC;
	public static int MAX_DRIFT_RANGE;
	public static boolean DEEPBLUE_DROP_RULES;
	public static boolean DEEPBLUE_DROP_RULES_RAID;
	public static boolean SHOW_NPC_LVL;
	public static boolean SHOW_CREST_WITHOUT_QUEST;
	public static boolean ENABLE_RANDOM_ENCHANT_EFFECT;
	public static int MIN_NPC_LVL_DMG_PENALTY;
	public static TIntFloatHashMap NPC_DMG_PENALTY;
	public static TIntFloatHashMap NPC_CRIT_DMG_PENALTY;
	public static TIntFloatHashMap NPC_SKILL_DMG_PENALTY;
	public static int MIN_NPC_LVL_MAGIC_PENALTY;
	public static TIntFloatHashMap NPC_SKILL_CHANCE_PENALTY;
	public static int DECAY_TIME_TASK;
	public static int NPC_DECAY_TIME;
	public static int RAID_BOSS_DECAY_TIME;
	public static int SPOILED_DECAY_TIME;
	public static boolean GUARD_ATTACK_AGGRO_MOB;
	public static boolean ALLOW_WYVERN_UPGRADER;
	public static double RAID_HP_REGEN_MULTIPLIER;
	public static double RAID_MP_REGEN_MULTIPLIER;
	public static double RAID_PDEFENCE_MULTIPLIER;
	public static double RAID_MDEFENCE_MULTIPLIER;
	public static double RAID_PATTACK_MULTIPLIER;
	public static double RAID_MATTACK_MULTIPLIER;
	public static double RAID_MINION_RESPAWN_TIMER;
	public static float RAID_MIN_RESPAWN_MULTIPLIER;
	public static float RAID_MAX_RESPAWN_MULTIPLIER;
	public static boolean RAID_DISABLE_CURSE;
	public static int RAID_CHAOS_TIME;
	public static int GRAND_CHAOS_TIME;
	public static int MINION_CHAOS_TIME;
	public static int INVENTORY_MAXIMUM_PET;
	public static double PET_HP_REGEN_MULTIPLIER;
	public static double PET_MP_REGEN_MULTIPLIER;
	public static TIntArrayList NON_TALKING_NPCS;
	public static TIntIntHashMap MINIONS_RESPAWN_TIME;
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_MIN;
	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_BATTLE;
	public static long ALT_OLY_WPERIOD;
	public static long ALT_OLY_VPERIOD;
	public static int ALT_OLY_START_POINTS;
	public static int ALT_OLY_WEEKLY_POINTS;
	public static int ALT_OLY_CLASSED;
	public static int ALT_OLY_NONCLASSED;
	public static int ALT_OLY_REG_DISPLAY;
	public static int[][] ALT_OLY_CLASSED_REWARD;
	public static int[][] ALT_OLY_NONCLASSED_REWARD;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_MIN_MATCHES;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_HERO_POINTS;
	public static int ALT_OLY_RANK1_POINTS;
	public static int ALT_OLY_RANK2_POINTS;
	public static int ALT_OLY_RANK3_POINTS;
	public static int ALT_OLY_RANK4_POINTS;
	public static int ALT_OLY_RANK5_POINTS;
	public static int ALT_OLY_MAX_POINTS;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES_NON_CLASSED;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES_CLASSED;
	public static boolean ALT_OLY_LOG_FIGHTS;
	public static boolean ALT_OLY_SHOW_MONTHLY_WINNERS;
	public static boolean ALT_OLY_ANNOUNCE_GAMES;
	public static TIntArrayList LIST_OLY_RESTRICTED_ITEMS;
	public static int ALT_OLY_ENCHANT_LIMIT;
	public static int ALT_OLY_WAIT_TIME;
	public static boolean OLY_PLAYER_SELECT_RANDOM;
	public static boolean OLY_IGNORE_WEEKLY_COMPTYPE;
	public static int REPUTATION_LOST_DEFAULT_VALUE;
	public static int REPUTATION_MAX_VALUE;
	public static long REPUTATION_XP_DIVIDER;
	public static boolean REPUTATION_DROP_GM;
	public static int REPUTATION_DROPITEM_VALUE;
	public static String REPUTATION_NONDROPPABLE_PET_ITEMS;
	public static String REPUTATION_NONDROPPABLE_ITEMS;
	public static int[] REPUTATION_LIST_NONDROPPABLE_PET_ITEMS;
	public static int[] REPUTATION_LIST_NONDROPPABLE_ITEMS;
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_PARTY_XP;
	public static float RATE_PARTY_SP;
	public static float RATE_HB_TRUST_INCREASE;
	public static float RATE_HB_TRUST_DECREASE;
	public static float RATE_EXTR_FISH;
	public static float RATE_DROP_ITEMS;
	public static float RATE_DROP_ITEMS_BY_RAID;
	public static float RATE_DROP_SPOIL;
	public static int RATE_DROP_MANOR;
	public static float RATE_QUEST_DROP;
	public static float RATE_QUEST_REWARD;
	public static float RATE_QUEST_REWARD_XP;
	public static float RATE_QUEST_REWARD_SP;
	public static float RATE_QUEST_REWARD_ADENA;
	public static boolean RATE_QUEST_REWARD_USE_MULTIPLIERS;
	public static float RATE_QUEST_REWARD_POTION;
	public static float RATE_QUEST_REWARD_SCROLL;
	public static float RATE_QUEST_REWARD_RECIPE;
	public static float RATE_QUEST_REWARD_MATERIAL;
	public static TIntFloatHashMap RATE_DROP_ITEMS_ID;
	public static float RATE_BADREPUTATION_EXP_LOST;
	public static float RATE_DROP_COMMON_HERBS;
	public static float RATE_DROP_HP_HERBS;
	public static float RATE_DROP_MP_HERBS;
	public static float RATE_DROP_SPECIAL_HERBS;
	public static int PLAYER_DROP_LIMIT;
	public static int PLAYER_RATE_DROP;
	public static int PLAYER_RATE_DROP_ITEM;
	public static int PLAYER_RATE_DROP_EQUIP;
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
	public static float PET_XP_RATE;
	public static int PET_FOOD_RATE;
	public static float RATE_CAMPAINS;
	public static float SINEATER_XP_RATE;
	public static int BADREPUTATION_DROP_LIMIT;
	public static int BADREPUTATION_RATE_DROP;
	public static int BADREPUTATION_RATE_DROP_ITEM;
	public static int BADREPUTATION_RATE_DROP_EQUIP;
	public static int BADREPUTATION_RATE_DROP_EQUIP_WEAPON;
	public static double[] PLAYER_XP_PERCENT_LOST;
	public static float RATE_VITALITY_LOST;
	public static float RATE_VITALITY_GAIN;
	public static float RATE_VITALITY;
	public static float WEAPON_BLESSED_ENCHANT_BONUS;
	public static float ARMOR_BLESSED_ENCHANT_BONUS;
	public static int GLUDIO_MAX_MERCENARIES;
	public static int DION_MAX_MERCENARIES;
	public static int GIRAN_MAX_MERCENARIES;
	public static int OREN_MAX_MERCENARIES;
	public static int ADEN_MAX_MERCENARIES;
	public static int INNADRIL_MAX_MERCENARIES;
	public static int GODDARD_MAX_MERCENARIES;
	public static int RUNE_MAX_MERCENARIES;
	public static int SCHUTTGART_MAX_MERCENARIES;
	// Conquerable Halls Settings
	public static int CHS_CLAN_MINLEVEL;
	public static int CHS_MAX_ATTACKERS;
	public static int CHS_MAX_FLAGS_PER_CLAN;
	public static boolean CHS_ENABLE_FAME;
	public static int CHS_FAME_AMOUNT;
	public static int CHS_FAME_FREQUENCY;
	public static int GAME_POINT_ITEM_ID;
	public static boolean MMO_TOP_MANAGER_ENABLED;
	public static int MMO_TOP_MANAGER_INTERVAL;
	public static String MMO_TOP_WEB_ADDRESS;
	public static int MMO_TOP_SAVE_DAYS;
	public static int[] MMO_TOP_REWARD;
	public static int[] MMO_TOP_REWARD_NO_CLAN;
	public static boolean L2_TOP_MANAGER_ENABLED;
	public static int L2_TOP_MANAGER_INTERVAL;
	public static String L2_TOP_WEB_ADDRESS;
	public static String L2_TOP_SMS_ADDRESS;
	public static String L2_TOP_PREFIX;
	public static int L2_TOP_SAVE_DAYS;
	public static int[] L2_TOP_REWARD;
	public static int[] L2_TOP_REWARD_NO_CLAN;
	public static boolean BANKING_SYSTEM_ENABLED;
	public static int BANKING_SYSTEM_GOLDBARS;
	public static int BANKING_SYSTEM_ADENA;
	public static int ALT_BIRTHDAY_GIFT;
	public static String ALT_BIRTHDAY_MAIL_SUBJECT;
	public static String ALT_BIRTHDAY_MAIL_TEXT;
	public static boolean CHAMPION_ENABLE;
	public static boolean CHAMPION_PASSIVE;
	public static int CHAMPION_FREQUENCY;
	public static String CHAMP_TITLE;
	public static int CHAMP_MIN_LVL;
	public static int CHAMP_MAX_LVL;
	public static int CHAMPION_HP;
	public static int CHAMPION_REWARDS;
	public static float CHAMPION_ADENAS_REWARDS;
	public static float CHAMPION_HP_REGEN;
	public static float CHAMPION_ATK;
	public static float CHAMPION_SPD_ATK;
	public static int CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE;
	public static int CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE;
	public static int CHAMPION_REWARD_ID;
	public static int CHAMPION_REWARD_QTY;
	public static boolean CHAMPION_ENABLE_VITALITY;
	public static boolean CHAMPION_ENABLE_IN_INSTANCES;
	public static int CHAMPION_ENABLE_AURA;
	public static boolean ALLOW_NEW_CHARACTER_TITLE;
	public static String NEW_CHARACTER_TITLE;
	public static boolean TITLE_PVP_MODE;
	public static boolean TITLE_PVP_MODE_FOR_SELF;
	public static float TITLE_PVP_MODE_RATE;
	public static boolean CHAT_ADMIN;
	public static String SERVER_NAME;
	public static String CUSTOM_DATA_DIRECTORY;
	public static boolean ALLOW_VALID_ENCHANT;
	public static boolean ALLOW_VALID_EQUIP_ITEM;
	public static boolean DESTROY_ENCHANT_ITEM;
	public static boolean PUNISH_PLAYER;
	public static boolean PVP_ALLOW_REWARD;
	public static String[] PVP_REWARD;
	public static boolean AUGMENTATION_WEAPONS_PVP;
	public static boolean ELEMENTAL_ITEM_PVP;
	public static boolean ENTER_HELLBOUND_WITHOUT_QUEST;
	public static boolean CUSTOM_SPAWNLIST_TABLE;
	public static boolean SAVE_GMSPAWN_ON_CUSTOM;
	public static boolean CUSTOM_NPC_TABLE;
	public static boolean CUSTOM_NPC_SKILLS_TABLE;
	public static boolean CUSTOM_DROPLIST_TABLE;
	public static int SIZE_MESSAGE_HTML_NPC;
	public static int SIZE_MESSAGE_HTML_QUEST;
	public static boolean ALLOW_MANA_POTIONS;
	public static boolean DISABLE_MANA_POTIONS_IN_PVP;
	public static boolean REDUCE_ITEM_PRICE_ON_SELL;
	public static int SOD_TIAT_KILL_COUNT;
	public static long SOD_STAGE_2_LENGTH;
	public static int SOI_EKIMUS_KILL_COUNT;
	public static long SOI_STAGE_2_LENGTH;
	public static boolean OFFLINE_TRADE_ENABLE;
	public static boolean OFFLINE_CRAFT_ENABLE;
	public static boolean RESTORE_OFFLINERS;
	public static int OFFLINE_MAX_DAYS;
	public static boolean OFFLINE_DISCONNECT_FINISHED;
	public static boolean OFFLINE_MODE_NO_DAMAGE;
	public static boolean OFFLINE_SET_NAME_COLOR;
	public static int OFFLINE_NAME_COLOR;
	public static boolean OFFLINE_FAME;
	public static boolean ALLOW_WEDDING;
	public static int WEDDING_PRICE;
	public static boolean WEDDING_PUNISH_INFIDELITY;
	public static boolean WEDDING_TELEPORT;
	public static int WEDDING_TELEPORT_PRICE;
	public static int WEDDING_TELEPORT_DURATION;
	public static boolean WEDDING_SAMESEX;
	public static boolean WEDDING_FORMALWEAR;
	public static int WEDDING_DIVORCE_COSTS;
	public static boolean PCBANG_ENABLED = true;
	public static boolean PCBANG_ACQUISITIONPOINTSRANDOM;
	public static boolean PCBANG_ENABLE_DOUBLE_ACQUISITION_POINTS;
	public static int PCBANG_DOUBLE_ACQUISITION_CHANCE;
	public static double PCBANG_DOUBLE_ACQUISITION_RATE;
	public static boolean PREMIUM_ENABLED;
	public static double PREMIUM_EXPSP_RATE;
	public static double PREMIUM_DROP_ITEM_RATE;
	public static boolean ENABLE_TEST_CATS_SPAWN;
	public static boolean ENABLE_NEWS_NPC_SPAWN;
	public static boolean ENABLE_COMMUNITY_BOARD;
	public static String COMMUNITY_SERVER_ADDRESS;
	public static int COMMUNITY_SERVER_PORT;
	public static byte[] COMMUNITY_SERVER_HEX_ID;
	public static int COMMUNITY_SERVER_SQL_DP_ID;
	public static int SERVER_ID;
	public static byte[] HEX_ID;
	public static String GAMESERVER_HOSTNAME;
	public static int PORT_GAME;
	public static String CNAME_TEMPLATE;
	public static String PET_NAME_TEMPLATE;
	public static String CLAN_NAME_TEMPLATE;
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	public static int MAXIMUM_ONLINE_USERS;
	public static TIntArrayList PROTOCOL_LIST;
	public static boolean DATABASE_CLEAN_UP;
	public static long CONNECTION_CLOSE_TIME;
	public static boolean PACKET_HANDLER_DEBUG;
    public static boolean DEVELOPER;
	public static int REQUEST_ID;
	public static boolean ACCEPT_ALTERNATE_ID;
	public static boolean DATABASE_BACKUP_MAKE_BACKUP_ON_STARTUP;
	public static boolean DATABASE_BACKUP_MAKE_BACKUP_ON_SHUTDOWN;
	public static String DATABASE_BACKUP_DATABASE_NAME;
	public static String DATABASE_BACKUP_SAVE_PATH;
	public static boolean DATABASE_BACKUP_COMPRESSION;
	public static String DATABASE_BACKUP_MYSQLDUMP_PATH;
	public static boolean USE_WINDOWS_LIMIT_BY_IP;
	public static int WINDOWS_LIMIT_COUNT;
	public static boolean ENABLE_RC4;
	public static String[] GAME_SERVER_SUBNETS;
	public static String[] GAME_SERVER_HOSTS;
	public static String LOGIN_BIND_ADDRESS;
	public static int PORT_LOGIN;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static boolean LOG_LOGIN_CONTROLLER;
    public static boolean SHOW_LICENCE;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	public static File DATAPACK_ROOT;
	public static boolean ENABLE_SAFE_ADMIN_PROTECTION;
	public static List<String> SAFE_ADMIN_NAMES;
	public static int SAFE_ADMIN_PUNISH;
	public static boolean SAFE_ADMIN_SHOW_ADMIN_ENTER;
	public static boolean SECOND_AUTH_ENABLED;
	public static int SECOND_AUTH_MAX_ATTEMPTS;
	public static long SECOND_AUTH_BAN_TIME;
	public static String SECOND_AUTH_REC_LINK;

	// Время бана для разного рода нарушений
	public static int LAMEGUARD_BANTIME_PACKETHACK;
	public static int LAMEGUARD_BANTIME_CLIENT_HACK;
	public static int LAMEGUARD_BANTIME_INGAME_BOT;
	public static int LAMEGUARD_BANTIME_BAD_APPLICATION;

	public static void loadMainConfigs()
	{
		Tools.printSection("Main");
		ConfigLocalization.loadConfig();
		ConfigFeature.loadConfig();
		ConfigCharacter.loadConfig();
		ConfigMMO.loadConfig();
		ConfigIDFactory.loadConfig();
		ConfigGeneral.loadConfig();
		ConfigGeodata.loadConfig();
		ConfigFloodProtector.loadConfig();
		ConfigNPC.loadConfig();
		ConfigRates.loadConfig();
		ConfigSiege.loadConfig();
		ConfigPvP.loadConfig();
		ConfigOlympiad.loadConfig();
		ConfigGrandBoss.loadConfig();
		ConfigLogger.loadConfig();
	}

	public static void loadModsConfigs()
	{
		Tools.printSection("Mods");
		ConfigBanking.loadConfig();
		ConfigBirthday.loadConfig();
		ConfigChampion.loadConfig();
		ConfigChars.loadConfig();
		ConfigChat.loadConfig();
		ConfigCustom.loadConfig();
		ConfigDynamicSpawn.loadConfig();
		ConfigGraciaSeeds.loadConfig();
		ConfigOfflineTrade.loadConfig();
		ConfigWedding.loadConfig();
		ConfigItemMall.loadConfig();
		ConfigVoteSystem.loadConfig();
		ConfigCommunityBoardPVP.loadConfig();
		ConfigPcBangCafe.loadConfig();
		ConfigPremium.loadConfig();

		ConfigEvents.loadConfig();
	}

	public static void loadNetworkConfigs()
	{
		Tools.printSection("Network");
		ConfigCommunityServer.loadConfig();
		ConfigGameServer.loadConfig();
		ConfigHexid.loadConfig();
		ConfigIPConfig.loadConfig();
		ConfigGuardEngine.loadConfig();
	}

	public static void loadScriptConfigs()
	{
		Tools.printSection("Script");
		ConfigChaosFestival.loadConfig();
	}

	public static void loadSecurityConfigs()
	{
		Tools.printSection("Security");
		ConfigProtectionAdmin.loadConfig();
		ConfigSecurityAuth.loadConfig();
	}

	public static void loadAll()
	{
		loadNetworkConfigs();
		loadMainConfigs();
		loadModsConfigs();
		loadScriptConfigs();
		loadSecurityConfigs();
	}

	public static void load()
	{
		Tools.printSection("Loading: GameServer Configs");
		loadAll();
	}

	public static boolean setParameterValue(String pName, String pValue)
	{
		if(pName.equalsIgnoreCase("RateXp"))
		{
			RATE_XP = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("RateHellboundTrustIncrease"))
		{
			RATE_HB_TRUST_INCREASE = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("RateHellboundTrustDecrease"))
		{
			RATE_HB_TRUST_DECREASE = Float.parseFloat(pValue);
		}
		else
		{
			try
			{
				if(!pName.startsWith("Interval_") && !pName.startsWith("Random_"))
				{
					pName = pName.toUpperCase();
				}
				Field clazField = Config.class.getField(pName);
				int modifiers = clazField.getModifiers();
				if(!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers))
				{
					throw new SecurityException("Cannot modify non public or non static config!");
				}

				if(clazField.getType().equals(int.class))
				{
					clazField.setInt(clazField, Integer.parseInt(pValue));
				}
				else if(clazField.getType().equals(short.class))
				{
					clazField.setShort(clazField, Short.parseShort(pValue));
				}
				else if(clazField.getType().equals(byte.class))
				{
					clazField.setByte(clazField, Byte.parseByte(pValue));
				}
				else if(clazField.getType().equals(long.class))
				{
					clazField.setLong(clazField, Long.parseLong(pValue));
				}
				else if(clazField.getType().equals(float.class))
				{
					clazField.setFloat(clazField, Float.parseFloat(pValue));
				}
				else if(clazField.getType().equals(double.class))
				{
					clazField.setDouble(clazField, Double.parseDouble(pValue));
				}
				else if(clazField.getType().equals(boolean.class))
				{
					clazField.setBoolean(clazField, Boolean.parseBoolean(pValue));
				}
				else if(clazField.getType().equals(String.class))
				{
					clazField.set(clazField, pValue);
				}
				else
				{
					return false;
				}
			}
			catch(NoSuchFieldException e)
			{
				return false;
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
				return false;
			}
		}
		return true;
	}

	public static void saveHexid(int serverId, String string)
	{
		saveHexid(serverId, string, HEXID_CONFIG);
	}

	public static void saveHexid(int serverId, String hexId, String fileName)
	{
		try
		{
			ConfigProperties hexSetting = new ConfigProperties();
			File file = new File(fileName);
			// Create a new empty file only if it doesn't exist
			file.createNewFile();
			OutputStream out = new FileOutputStream(file);
			hexSetting.setProperty("ServerID", String.valueOf(serverId));
			hexSetting.setProperty("HexID", hexId);
			hexSetting.store(out, "the hexID to auth into login");
			out.close();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, StringUtil.concat("Failed to save hex id to ", fileName, " File."), e);
		}
	}

	public static void loadFloodProtectorConfigs(ConfigProperties properties)
	{
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_USE_ITEM, "UseItem", "4");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ROLL_DICE, "RollDice", "42");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_FIREWORK, "Firework", "42");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ITEM_PET_SUMMON, "ItemPetSummon", "16");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_HERO_VOICE, "HeroVoice", "100");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_GLOBAL_CHAT, "GlobalChat", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SUBCLASS, "Subclass", "20");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_DROP_ITEM, "DropItem", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SERVER_BYPASS, "ServerBypass", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MULTISELL, "MultiSell", "1");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_TRANSACTION, "Transaction", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANUFACTURE, "Manufacture", "3");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANOR, "Manor", "30");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SENDMAIL, "SendMail", "100");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_CHARACTER_SELECT, "CharacterSelect", "30");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ITEM_AUCTION, "ItemAuction", "9");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_COMMUNITY_BOARD, "CommunityBoard", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_CLAN_SEARCH, "ClanSearch", "5");
	}

	public static int getServerTypeId(String[] serverTypes)
	{
		int tType = 0;
		for(String cType : serverTypes)
		{
			cType = cType.trim();
			switch(cType)
			{
				case "Normal":
					tType |= ServerStatus.SERVER_NORMAL;
					break;
				case "Relax":
					tType |= ServerStatus.SERVER_RELAX;
					break;
				case "Test":
					tType |= ServerStatus.SERVER_TEST;
					break;
				case "NoLabel":
					tType |= ServerStatus.SERVER_NOLABEL;
					break;
				case "Restricted":
					tType |= ServerStatus.SERVER_CREATION_RESTRICTED;
					break;
				case "Event":
					tType |= ServerStatus.SERVER_EVENT;
					break;
				case "Free":
					tType |= ServerStatus.SERVER_FREE;
					break;
                case "World":
                    tType |= ServerStatus.SERVER_WORLD_RAID;
                    break;
                case "New":
                    tType |= ServerStatus.SERVER_NEW;
                    break;
                case "Classic":
                    tType |= ServerStatus.SERVER_CLASSIC;
                    break;
			}
		}
		return tType;
	}

	public static TIntFloatHashMap parseConfigLine(String line)
	{
		String[] propertySplit = line.split(",");
		TIntFloatHashMap ret = new TIntFloatHashMap(propertySplit.length);
		int i = 1;
		for(String value : propertySplit)
		{
			ret.put(i++, Float.parseFloat(value));
		}
		return ret;
	}

	public static boolean getBoolean(ConfigProperties properties, String key, boolean defaultValue)
	{
		String value = getString(properties, key, null);

		if(value == null)
		{
			return defaultValue;
		}

		return Boolean.parseBoolean(value);
	}

	public static long getLong(ConfigProperties properties, String key, long defaultValue)
	{
		String value = getString(properties, key, null);

		if(value == null)
		{
			return defaultValue;
		}

		return Long.parseLong(value);
	}

	public static Short getShort(ConfigProperties properties, String key, short defaultValue)
	{
		String value = getString(properties, key, null);

		if(value == null)
		{
			return defaultValue;
		}

		return Short.parseShort(value);
	}

	public static int getInt(ConfigProperties properties, String key, int defaultValue)
	{
		String value = getString(properties, key, null);

		if(value == null)
		{
			return defaultValue;
		}

		return Integer.parseInt(value);
	}

	public static int getIntDecode(ConfigProperties properties, String key, String defaultValue)
	{
		String value = getString(properties, key, null);

		if(value == null)
		{
			return Integer.decode("0x" + defaultValue);
		}

		return Integer.decode("0x" + value);
	}

	public static byte getByte(ConfigProperties properties, String key, byte defaultValue)
	{
		String value = getString(properties, key, null);

		if(value == null)
		{
			return defaultValue;
		}

		return Byte.parseByte(value);
	}

	public static float getFloat(ConfigProperties properties, String key, float defaultValue)
	{
		String value = getString(properties, key, null);

		if(value == null)
		{
			return defaultValue;
		}

		return Float.parseFloat(value);
	}

	public static double getDouble(ConfigProperties properties, String key, double defaultValue)
	{
		String value = getString(properties, key, null);

		if(value == null)
		{
			return defaultValue;
		}

		return Double.parseDouble(value);
	}

	public static String getString(ConfigProperties properties, String key, String defaultValue)
	{
		String value = properties.getProperty(key);
		return value == null ? defaultValue : value;
	}

	public static String[] getStringArray(ConfigProperties properties, String key, String[] defaultValue, String separator)
	{
		String string = getString(properties, key, null);

		if(string == null || string.trim().isEmpty())
		{
			return defaultValue;
		}

		String[] result = string.split(separator);

		for(int i = 0; i < result.length; i++)
		{
			result[i] = result[i].trim();
		}

		return result;
	}

	public static int[] getIntArray(ConfigProperties properties, String key, int[] defaultValue, String separator)
	{
		String string = getString(properties, key, null);

		if(string == null || string.trim().isEmpty())
		{
			return defaultValue;
		}

		String[] stringArray = string.split(separator);
		int[] result = new int[stringArray.length];

		for(int i = 0; i < stringArray.length; i++)
		{
			result[i] = Integer.parseInt(stringArray[i].trim());
		}

		return result;
	}

	public static int[][] parseItemsList(String line)
	{
		String[] propertySplit = line.split(";");
		if(propertySplit.length == 0)
		{
			return null;
		}

		int i = 0;
		String[] valueSplit;
		int[][] result = new int[propertySplit.length][];
		for(String value : propertySplit)
		{
			valueSplit = value.split(",");
			if(valueSplit.length != 2)
			{
				_log.log(Level.WARN, StringUtil.concat("parseItemsList[Config.load()]: invalid entry -> \"", valueSplit[0], "\", should be itemId,itemNumber"));
				return null;
			}

			result[i] = new int[2];
			try
			{
				result[i][0] = Integer.parseInt(valueSplit[0]);
			}
			catch(NumberFormatException e)
			{
				_log.log(Level.ERROR, StringUtil.concat("parseItemsList[Config.load()]: invalid itemId -> \"", valueSplit[0], "\""));
				return null;
			}
			try
			{
				result[i][1] = Integer.parseInt(valueSplit[1]);
			}
			catch(NumberFormatException e)
			{
				_log.log(Level.ERROR, StringUtil.concat("parseItemsList[Config.load()]: invalid item number -> \"", valueSplit[1], "\""));
				return null;
			}
			i++;
		}
		return result;
	}

	private static void loadFloodProtectorConfig(ConfigProperties properties, FloodProtectorConfig config, String configString, String defaultInterval)
	{
		config.FLOOD_PROTECTION_INTERVAL = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "Interval"), defaultInterval));
		config.LOG_FLOODING = Boolean.parseBoolean(properties.getProperty(StringUtil.concat("FloodProtector", configString, "LogFlooding"), "False"));
		config.PUNISHMENT_LIMIT = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentLimit"), "0"));
		config.PUNISHMENT_TYPE = properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentType"), "none");
		config.PUNISHMENT_TIME = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentTime"), "0"));
	}

	public static enum IdFactoryType
	{
		Compaction, BitSet, Stack
	}

	public static enum ObjectMapType
	{
		L2ObjectHashMap, WorldObjectMap
	}

	public static enum ObjectSetType
	{
		L2ObjectHashSet, WorldObjectSet
	}
}