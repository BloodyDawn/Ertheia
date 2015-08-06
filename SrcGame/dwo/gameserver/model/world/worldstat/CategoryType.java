package dwo.gameserver.model.world.worldstat;

import javolution.util.FastList;

import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.02.12
 * Time: 5:55
 */

public enum CategoryType
{
	// Обычный вид
	EXP_ADDED(0, "expAdded"),
	ADENA_ADDED(1, "adenaAdded"),
	TIME_PLAYED(2, "timePlayed"),
	TIME_IN_BATTLE(3, "timeInBattle"),
	TIME_IN_PARTY(4, "timeInParty"),
	TIME_IN_FULLPARTY(5, "timeInFullParty"),

	WEAPON_ENCHANT_MAX(21, ""),
	WEAPON_ENCHANT_MAX_D(21, "weaponEnchantMaxD", 1),
	WEAPON_ENCHANT_MAX_C(21, "weaponEnchantMaxC", 2),
	WEAPON_ENCHANT_MAX_B(21, "weaponEnchantMaxB", 3),
	WEAPON_ENCHANT_MAX_A(21, "weaponEnchantMaxA", 4),
	WEAPON_ENCHANT_MAX_S(21, "weaponEnchantMaxS", 5),
	WEAPON_ENCHANT_MAX_S80(21, "weaponEnchantMaxS80", 7),
	WEAPON_ENCHANT_MAX_R(21, "weaponEnchantMaxR", 8),
	WEAPON_ENCHANT_MAX_R95(21, "weaponEnchantMaxR95", 9),
	WEAPON_ENCHANT_MAX_R99(21, "weaponEnchantMaxR99", 10),

	ARMOR_ENCHANT_MAX(23, ""),
	ARMOR_ENCHANT_MAX_D(23, "armorEnchantMaxD", 1),
	ARMOR_ENCHANT_MAX_C(23, "armorEnchantMaxC", 2),
	ARMOR_ENCHANT_MAX_B(23, "armorEnchantMaxB", 3),
	ARMOR_ENCHANT_MAX_A(23, "armorEnchantMaxA", 4),
	ARMOR_ENCHANT_MAX_S(23, "armorEnchantMaxS", 5),
	ARMOR_ENCHANT_MAX_S80(23, "armorEnchantMaxS80", 6),
	ARMOR_ENCHANT_MAX_R(23, "armorEnchantMaxR", 8),
	ARMOR_ENCHANT_MAX_R95(23, "armorEnchantMaxR95", 9),
	ARMOR_ENCHANT_MAX_R99(23, "armorEnchantMaxR99", 10),

	WEAPON_ENCHANT_TRY(22, ""),
	WEAPON_ENCHANT_TRY_D(22, "weaponEnchantTryD", 1),
	WEAPON_ENCHANT_TRY_C(22, "weaponEnchantTryC", 2),
	WEAPON_ENCHANT_TRY_B(22, "weaponEnchantTryB", 3),
	WEAPON_ENCHANT_TRY_A(22, "weaponEnchantTryA", 4),
	WEAPON_ENCHANT_TRY_S(22, "weaponEnchantTryS", 5),
	WEAPON_ENCHANT_TRY_S80(22, "weaponEnchantTryS80", 6),
	WEAPON_ENCHANT_TRY_R(22, "weaponEnchantTryR", 8),
	WEAPON_ENCHANT_TRY_R95(22, "weaponEnchantTryR95", 9),
	WEAPON_ENCHANT_TRY_R99(22, "weaponEnchantTryR99", 10),

	ARMOR_ENCHANT_TRY(24, ""),
	ARMOR_ENCHANT_TRY_D(24, "armorEnchantTryD", 1),
	ARMOR_ENCHANT_TRY_C(24, "armorEnchantTryC", 2),
	ARMOR_ENCHANT_TRY_B(24, "armorEnchantTryB", 3),
	ARMOR_ENCHANT_TRY_A(24, "armorEnchantTryA", 4),
	ARMOR_ENCHANT_TRY_S(24, "armorEnchantTryS", 5),
	ARMOR_ENCHANT_TRY_S80(24, "armorEnchantTryS80", 6),
	ARMOR_ENCHANT_TRY_R(24, "armorEnchantTryR", 8),
	ARMOR_ENCHANT_TRY_R95(24, "armorEnchantTryR95", 9),
	ARMOR_ENCHANT_TRY_R99(24, "armorEnchantTryR99", 10),

	PRIVATE_SELL_COUNT(11, "privateSellCount"),
	QUESTS_COMPLETED(12, "questsCompleted"),

	SS_CONSUMED(13, ""),
	SS_CONSUMED_D(13, "ssConsumedD", 1),
	SS_CONSUMED_C(13, "ssConsumedC", 2),
	SS_CONSUMED_B(13, "ssConsumedB", 3),
	SS_CONSUMED_A(13, "ssConsumedA", 4),
	SS_CONSUMED_S(13, "ssConsumedS", 5),
	SS_CONSUMED_R(13, "ssConsumedR", 8),

	SPS_CONSUMED(14, ""),
	SPS_CONSUMED_D(14, "spsConsumedD", 1),
	SPS_CONSUMED_C(14, "spsConsumedC", 2),
	SPS_CONSUMED_B(14, "spsConsumedB", 3),
	SPS_CONSUMED_A(14, "spsConsumedA", 4),
	SPS_CONSUMED_S(14, "spsConsumedS", 5),
	SPS_CONSUMED_R(14, "spsConsumedR", 8),

	RESURRECTED_CHAR_COUNT(18, "resurrectedCharCount"),
	RESURRECTED_BY_OTHER_COUNT(19, "resurrectedByOtherCount"),

	// 20 ВАЖНО: Является суммой KILLED_BY_MONSTER_COUNT,KILLED_IN_PK_COUNT и KILLED_IN_PVP_COUNT
	DIE_COUNT(20, "dieCount"),

	// Вид охотничьего угодья
	MONSTERS_KILLED(1000, "monstersKilled"),
	EXP_FROM_MONSTERS(1001, "expFromMonsters"),

	// TODO: Насчет разбивки на категории: их хранить в базе нет смлыса, при подсчете результатов это надо учитвать, т.е. считать внутри ид профы статистику
	DAMAGE_TO_MONSTERS_MAX(1003, "maxDamageToMonster"),
	DAMAGE_TO_MONSTERS(1004, "allDamageToMonster"),
	DAMAGE_FROM_MONSTERS(1005, "allDamageFromMonster"),
	KILLED_BY_MONSTER_COUNT(1002, "killedByMonsterCount"),

	// Вид рейда
	/**
	 * TODO Категории (npcId + 1000000)
	 * Харнак 				1025774
	 * Траджан				1025785
	 * Истхина				1029195
	 * Спасия				1025779
	 * Афрос				1025866
	 * Октавис				1029194
	 * Валлок				1029218
	 * Байлор				1029213
	 * Истхина сложная		1029196
	 * Спасия сложная		1025867
	 * Октавис				1029212
	 * Земляной червь		1029197
	 */
	EPIC_BOSS_KILLS(1006, ""),
	EPIC_BOSS_KILLS_25774(1006, "raidKilled_25774", 1025774),
	EPIC_BOSS_KILLS_25785(1006, "raidKilled_25785", 1025785),
	EPIC_BOSS_KILLS_29195(1006, "raidKilled_29195", 1029195),
	EPIC_BOSS_KILLS_25779(1006, "raidKilled_25779", 1025779),
	EPIC_BOSS_KILLS_25866(1006, "raidKilled_25866", 1025866),
	EPIC_BOSS_KILLS_29194(1006, "raidKilled_29194", 1029194),
	EPIC_BOSS_KILLS_29218(1006, "raidKilled_29218", 1029218),
	EPIC_BOSS_KILLS_29213(1006, "raidKilled_29213", 1029213),
	EPIC_BOSS_KILLS_29196(1006, "raidKilled_29196", 1029196),
	EPIC_BOSS_KILLS_25867(1006, "raidKilled_25867", 1025867),
	EPIC_BOSS_KILLS_29212(1006, "raidKilled_29212", 1029212),
	EPIC_BOSS_KILLS_29197(1006, "raidKilled_29197", 1029197),

	// Вид ПВП
	KILLED_BY_PK_COUNT(2001, "killedByPkCount"),
	KILLED_IN_PVP_COUNT(2002, "killedInPvpCount"),
	PK_COUNT(2004, "pkCount"),
	PVP_COUNT(2005, "pvpCount"),
	DAMAGE_TO_PC_MAX(2006, "maxDamageToPc"),
	DAMAGE_TO_PC(2007, "allDamageToPc"),
	DAMAGE_FROM_PC(2008, "allDamageFromPc"),

	// Вид клана
	MEMBERS_COUNT(3000, "clanMembersCount", 0, true),
	INVITED_COUNT(3001, "clanInvitesCount", 0, true),
	LEAVED_COUNT(3002, "clanLeavedCount", 0, true),
	REPUTATION_COUNT(3003, "clanReputationCount", 0, true),
	ADENA_COUNT_IN_WH(3004, "clanAdenaAddedInWh", 0, true),
	ALL_CLAN_PVP_COUNT(3005, "clanPvpCount", 0, true),
	CLAN_WAR_WIN_COUNT(3006, "clanWarWinCount", 0, true);

	private final int _id;
	private final int _subcat;
	private final String _dbField;
	private final boolean _isClanStatistic;

	private CategoryType(int id, String dbField, int subcat, boolean isClanStatistic)
	{
		_id = id;
		_subcat = subcat;
		_dbField = dbField;
		_isClanStatistic = isClanStatistic;
	}

	/**
	 *
	 * @param id Category ID.
	 * @param dbField Database field name for store values in DB.
	 */
	private CategoryType(int id, String dbField)
	{
		_id = id;
		_subcat = 0;
		_dbField = dbField;
		_isClanStatistic = false;
	}

	private CategoryType(int id, String dbField, int subcat)
	{
		_id = id;
		_subcat = subcat;
		_dbField = dbField;
		_isClanStatistic = false;
	}

	public static List<String> getAllClanFields()
	{
		List<String> fields = new FastList();
		for(CategoryType cat : CategoryType.values())
		{
			if(!cat._dbField.isEmpty() && cat._isClanStatistic)
			{
				fields.add(cat._dbField);
			}
		}

		return fields;
	}

	public static List<String> getAllFields()
	{
		List<String> fields = new FastList();
		for(CategoryType cat : CategoryType.values())
		{
			if(!cat._dbField.isEmpty() && !cat._isClanStatistic)
			{
				fields.add(cat._dbField);
			}
		}

		return fields;
	}

	public static CategoryType getCategoryById(int catId)
	{
		return getCategoryById(catId, 0);
	}

	public static CategoryType getCategoryById(int catId, int subcatId)
	{
		for(CategoryType category : values())
		{
			if(category._id == catId && category._subcat == subcatId)
			{
				return category;
			}
		}

		return null;
	}

	public boolean isClanStatistic()
	{
		return _isClanStatistic;
	}

	public int getClientId()
	{
		return _id;
	}

	public int getSubcat()
	{
		return _subcat;
	}

	public String getFieldName()
	{
		return _dbField;
	}
}
