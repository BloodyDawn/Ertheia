package dwo.config.mods;

import dwo.config.PropertyListenerImpl;
import dwo.gameserver.util.StringUtil;
import jfork.nproperty.Cfg;
import jfork.nproperty.CfgIgnore;
import jfork.nproperty.CfgSplit;
import jfork.nproperty.ConfigParser;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * L2GOD Team
 * User: Bacek, ANZO
 * Date: 12.06.13
 * Time: 11:32
 */

@Cfg
public class ConfigCommunityBoardPVP extends PropertyListenerImpl
{
    public static final String PATH = "./config/mods/CommunityBoardPVP.ini";
    public static boolean COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS;
    @CfgSplit(splitter = ",")
    public static List<Integer> COMMUNITY_BOARD_PVP_ALLOW_CLASS_MASTERS_LIST;
    @CfgSplit(splitter = ",")
    public static int[] COMMUNITY_BOARD_PVP_CLASS_MASTERS_PRICE_LIST;
    public static boolean COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS_GIVE_NOBLESS;
    public static int COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS_NOBLESS_PRICE;
    public static int COMMUNITY_BOARD_PVP_CLASS_MASTERS_PRICE_TO_CHANGE_DUAL_ITEM;
    public static int COMMUNITY_BOARD_PVP_CLASS_MASTERS_PRICE_TO_CHANGE_DUAL_PRICE;
    public static int COMMUNITY_BOARD_SERVICES_PRICE_ITEM;
    public static boolean COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_BUFF;
    public static int COMMUNITY_BOARD_PVP_BUFF_TIME_OVERRIDE;
    public static boolean COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_TELEPORT;
    public static boolean COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_TELEPORT_NEW;
    public static int teleportPriceId;
    public static int teleportPriceCount;
    public static int teleportSavePriceId;
    public static int teleportSavePriceCount;
    public static int teleportMinLevelForPrice;
    public static boolean COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_MULTISELL;
    public static boolean COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_ENCHANT;
    public static boolean COMMUNITY_BOARD_SERVICES_ENABLE;
    public static int COMMUNITY_BOARD_PVP_ENCHANT_ITEM;
    public static int COMMUNITY_BOARD_CHANGE_NAME_PRICE;
    public static int COMMUNITY_BOARD_CHANGE_SEX_PRICE;
    public static int COMMUNITY_BOARD_CHANGE_CLANNAME_PRICE;
    public static int COMMUNITY_BOARD_NOBLESS_PRICE;
    public static int COMMUNITY_BOARD_CHANGE_PET_NAME_PRICE;
    public static int COMMUNITY_BOARD_REMOVE_CLAN_PENALTY_PRICE;
    public static int COMMUNITY_BOARD_REMOVE_CHARACTER_PRICE;
    public static int COMMUNITY_BOARD_TRANSFER_CHARACTER_PRICE;
    public static int CommunityBoardChangeNickColorPrice;
    @CfgSplit(splitter = ",")
    public static List<String> CommunityBoardChangeNickColors;
    @CfgSplit(splitter = ",")
    public static List<Integer> CommunityBoardCasinoItemId;
    @CfgSplit(splitter = ",")
    public static List<Integer> CommunityBoardCasinoItemCount;
    @CfgIgnore
    public static Map<Integer, Integer> COMMUNITY_BOARD_WINDOW_PRICE;
    public static int COMMUNITY_BOARD_PVP_CLASS_MASTERS_PRICE_ITEM;
    public static boolean COMMUNITY_BOARD_RSS_SYSTEM_ENABLE;
    public static String COMMUNITY_BOARD_RSS_SERVER;
    public static String COMMUNITY_BOARD_RSS_DOCUMENT;
    public static boolean COMMUNITY_BOARD_ALLOW_PK;
    public static boolean COMMUNITY_BOARD_ALLOW_HEAL_HP_CP_MP;
    @CfgIgnore
    public static int COMMUNITY_BOARD_RSS_RELOAD_DELAY;

    @Cfg("COMMUNITY_BOARD_RSS_RELOAD_DELAY")
    private static void ctfRssReloadDelay(final int time) {
        if (time > 0) {
            ConfigCommunityBoardPVP.COMMUNITY_BOARD_RSS_RELOAD_DELAY = time * 60 * 1000;
        }
    }

    @Cfg("COMMUNITY_BOARD_WINDOW_PRICE")
    private static void cfgWindowPrice(String value) {
        if (value.isEmpty()) {
            value = "30,10";
        }
        final String[] buffs = value.split(";");
        if (!buffs[0].isEmpty()) {
            ConfigCommunityBoardPVP.COMMUNITY_BOARD_WINDOW_PRICE = new HashMap<Integer, Integer>();
            for (final String skill : buffs) {
                final String[] skillSplit = skill.split(",");
                if (skillSplit.length != 2) {
                    PropertyListenerImpl._log.log(Level.WARN, StringUtil.concat("ConfigCommunityBoardPVP[Config.load()]: invalid config property -> COMMUNITY_BOARD_WINDOW_PRICE \"", skill, "\""));
                }
                else {
                    try {
                        ConfigCommunityBoardPVP.COMMUNITY_BOARD_WINDOW_PRICE.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
                    }
                    catch (NumberFormatException nfe) {
                        if (!skill.isEmpty()) {
                            PropertyListenerImpl._log.log(Level.WARN, StringUtil.concat("ConfigCommunityBoardPVP[Config.load()]: invalid config property -> COMMUNITY_BOARD_WINDOW_PRICE \"", skill, "\""));
                        }
                    }
                }
            }
        }
    }

    private ConfigCommunityBoardPVP() {
        super();
        try {
            ConfigParser.parse(this, PATH, true);
            if (ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS && ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM == 0) {
                PropertyListenerImpl._log.log(Level.WARN, "ConfigCommunityBoardPVP[Config.load()]: invalid config property -> COMMUNITY_BOARD_SERVICES_PRICE_ITEM");
            }
        }
        catch (Exception e) {
            throw new Error("Failed to Load ./config/mods/CommunityBoardPVP.ini File.", e);
        }
    }

    public static void loadConfig() {
        new ConfigCommunityBoardPVP();
    }

    static {
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS = false;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_CLASS_MASTERS_LIST = new ArrayList<Integer>();
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_CLASS_MASTERS_PRICE_LIST = new int[4];
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS_GIVE_NOBLESS = false;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_CLASS_NOBLESS_PRICE = 100;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM = 57;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_BUFF = false;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_BUFF_TIME_OVERRIDE = 1;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_TELEPORT = false;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_TELEPORT_NEW = false;
        ConfigCommunityBoardPVP.teleportPriceId = 57;
        ConfigCommunityBoardPVP.teleportPriceCount = 1;
        ConfigCommunityBoardPVP.teleportSavePriceId = 57;
        ConfigCommunityBoardPVP.teleportSavePriceCount = 1;
        ConfigCommunityBoardPVP.teleportMinLevelForPrice = 40;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_MULTISELL = false;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_ENCHANT = false;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_ENABLE = false;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ENCHANT_ITEM = 4037;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_NAME_PRICE = 1;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_SEX_PRICE = 1;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_CLANNAME_PRICE = 1;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_NOBLESS_PRICE = 1;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_PET_NAME_PRICE = 1;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_REMOVE_CLAN_PENALTY_PRICE = 1;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_REMOVE_CHARACTER_PRICE = 1;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_TRANSFER_CHARACTER_PRICE = 1;
        ConfigCommunityBoardPVP.CommunityBoardChangeNickColorPrice = 1;
        ConfigCommunityBoardPVP.CommunityBoardChangeNickColors = new ArrayList<String>();
        ConfigCommunityBoardPVP.CommunityBoardCasinoItemId = new ArrayList<Integer>();
        ConfigCommunityBoardPVP.CommunityBoardCasinoItemCount = new ArrayList<Integer>();
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_CLASS_MASTERS_PRICE_ITEM = 57;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_RSS_SYSTEM_ENABLE = false;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_RSS_SERVER = "bash.im";
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_RSS_DOCUMENT = "rss/";
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_ALLOW_PK = false;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_ALLOW_HEAL_HP_CP_MP = false;
        ConfigCommunityBoardPVP.COMMUNITY_BOARD_RSS_RELOAD_DELAY = 10800000;
    }
}
