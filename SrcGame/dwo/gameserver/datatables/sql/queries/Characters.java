package dwo.gameserver.datatables.sql.queries;

import dwo.config.Config;
import dwo.gameserver.model.actor.stat.PcStat;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 02.04.12
 * Time: 16:08
 */

public class Characters
{
	//characters
	public static final String INSERT_CHARACTER = "INSERT INTO characters (account_name,charId,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp, face, hairStyle, hairColor, custom_face, custom_hair_style, custom_hair_color, sex,exp,sp,reputation,fame,pvpkills,pkkills,clanid,race,classid,base_class,deletetime,cancraft,title,title_color,accesslevel,online,clan_privs,power_grade,nobless,createDate,pcbang_points) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String SELECT_CHARACTERS = "SELECT account_name, charId, char_name, level, maxHp, curHp, maxMp, curMp, face, hairStyle, hairColor, `custom_face`, `custom_hair_style`, `custom_hair_color`, sex, heading, x, y, z, exp, sp, reputation, pvpkills, pkkills, clanid, race, classid, deletetime, cancraft, title, accesslevel, online, char_slot, lastAccess, base_class, transform_id FROM characters WHERE account_name=?";
	public static final String SELECT_CHARACTERS_DELETE_TIME = "SELECT deletetime FROM characters WHERE account_name=?";
	public static final String SELECT_CHARACTERS_ACCESSLEVEL_BY_NAME = "SELECT charId,accesslevel FROM characters WHERE char_name=?";
	public static final String SELECT_CHARACTERS_ACCESSLEVEL_BY_CHARID = "SELECT char_name,accesslevel FROM characters WHERE charId=?";
	public static final String SELECT_ACCOUNT_NAME_BY_CHAR_NAME = "SELECT account_name FROM characters WHERE char_name=?";
	public static final String SELECT_COUNT_CHAR = "SELECT COUNT(char_name) FROM characters WHERE account_name=?";
	public static final String SELECT_CHARACTERS_ACCESSLEVEL_ALL = "SELECT charId,char_name,accesslevel FROM characters";
	public static final String SELECT_CHARACTERS_LEVEL_CLASSID_CHAR_NAME = "SELECT char_name, classid, level FROM characters WHERE charId = ?";
	public static final String SELECT_CHARACTERS_ONLINE_LEVEL = "SELECT account_name, charId, char_name, level, clanid, accesslevel, online FROM characters";
	public static final String SELECT_CHARACTERS_CLANID = "SELECT clanId FROM characters WHERE charId=?";
	public static final String SELECT_CHARACTERS_CHAR_NAME = "SELECT char_name FROM characters WHERE account_name=?";
	public static final String SELECT_CHARACTERS_ACCOUNT_NAME = "SELECT account_name FROM characters WHERE char_name=?";
	public static final String SELECT_CHARACTERS_PUNISH_LEVEL = "SELECT punish_level FROM characters WHERE char_name=?";
	public static final String SELECT_CHARACTERS_CHARID = "SELECT charId FROM characters WHERE char_name=?";
	public static final String SELECT_CHARACTERS_LEVEL_CLASSID_CHAR_NAME_BASE_CLASS = "SELECT char_name, level, base_class FROM characters WHERE charId = ?";
	public static final String SELECT_RESTORE_CHARACTER = "SELECT account_name, charId, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, face, hairStyle, hairColor, `custom_face`, `custom_hair_style`, `custom_hair_color`, sex, heading, x, y, z, exp, expBeforeDeath, sp, reputation, fame, pvpkills, pkkills, clanid, race, classid, deletetime, cancraft, title, title_color, name_color, accesslevel, online, char_slot, lastAccess, clan_privs, base_class, onlinetime, punish_level, punish_timer, nobless, power_grade, subpledge, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally,clan_join_expiry_time,clan_create_expiry_time,bookmarkslot,createDate,pcbang_points FROM characters WHERE charId=?";
	public static final String SELECT_CHAR_ACCOUNT = "SELECT char_name FROM characters WHERE account_name=?";
	public static final String SELECT_CHAR_CREATEDATE = "SELECT charId, createDate FROM characters WHERE createDate LIKE ?";
	public static final String SELECT_CHAR_ACCOUNT_NAME_CHARID = "SELECT charId, char_name FROM characters WHERE account_name=? AND charId<>?";
	public static final String SELECT_CHAR_ACCOUNT_NAME_CLANID = "SELECT char_name,level,classid,charId,title,power_grade,subpledge,apprentice,sponsor,sex,race FROM characters WHERE clanid=?";
	public static final String SELECT_CHARACTER = "SELECT * FROM `characters` WHERE `charId` = ?";
	public static final String UPDATE_CHAR_NAME = "UPDATE characters SET punish_level=?, punish_timer=? WHERE char_name=?";
	public static final String UPDATE_CHAR_X_Y_Z_NAME = "UPDATE characters SET x=?, y=?, z=?, punish_level=?, punish_timer=? WHERE char_name=?";
	public static final String UPDATE_CHAR_ACCESSLEVEL_NAME = "UPDATE characters SET accesslevel=? WHERE char_name=?";
	public static final String UPDATE_CHAR_CLAN_PRIVS_CHARID = "UPDATE characters SET clan_privs = ? WHERE charId = ?";
	public static final String UPDATE_CHAR_REPAIR = "UPDATE characters SET x=-84318, y=244579, z=-3730 WHERE char_name=?";
	public static final String UPDATE_CHAR_REPAIR_PLAYER = "UPDATE characters SET x=17867, y=170259, z=-3503, transform_id=0 WHERE charId=?";
	public static final String UPDATE_CHAR_X_Y_Z = "UPDATE characters SET x=?, y=?, z=? WHERE char_name=?";
	public static final String UPDATE_CHAR_REPUTATION_PKKILLS = "UPDATE characters SET reputation=?, pkkills=? WHERE charId=?";
	public static final String UPDATE_CHARACTER = "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,custom_face=?,custom_hair_style=?,custom_hair_color=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,reputation=?,fame=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,title_color=?,accesslevel=?,online=?,clan_privs=?,base_class=?,onlinetime=?,punish_level=?,punish_timer=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,bookmarkslot=?,pcbang_points=?,name_color=?,account_name=? WHERE charId=?";
	public static final String UPDATE_CHAR_TRANSFORM = "UPDATE characters SET transform_id=? WHERE charId=?";
	public static final String UPDATE_CHAR_CLAN_REMOVEMEMBER = "UPDATE characters SET clanid=0, title=?, clan_join_expiry_time=?, clan_create_expiry_time=?, clan_privs=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE charId=?";
	public static final String UPDATE_CHAR_APPRENTICE = "UPDATE characters SET apprentice=0 WHERE apprentice=?";
	public static final String UPDATE_CHAR_SPONSOR = "UPDATE characters SET sponsor=0 WHERE sponsor=?";
	public static final String UPDATE_CHAR_SUBPLEDGE = "UPDATE characters SET subpledge=? WHERE charId=?";
	public static final String UPDATE_CHAR_POWER_GRADE = "UPDATE characters SET power_grade=? WHERE charId=?";
	public static final String UPDATE_CHAR_APPRENTICE_SPONSOR = "UPDATE characters SET apprentice=?,sponsor=? WHERE charId=?";
	public static final String UPDATE_CHAR_DELETETIME = "UPDATE characters SET deletetime=? WHERE charId=?";
	public static final String UPDATE_CHAR_DELETETIME_0 = "UPDATE characters SET deletetime=0 WHERE charId=?";
	public static final String DELETE_CHAR_CHARID = "DELETE FROM characters WHERE charId=?";

	// character_relations
	public static final String INSERT_CHARACTER_RELATIONS_RELATION = "INSERT INTO character_relations (charId, friendId, relation, note) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE relation=?, note=?";
	public static final String SELECT_CHARACTER_RELATIONS_BY_CHARID = "SELECT friendId FROM character_relations WHERE charId=?";
	public static final String SELECT_CHARACTER_RELATIONS_BY_CHARID_RELATION = "SELECT friendId, relation, note FROM character_relations WHERE charId=?";
	public static final String DELETE_CHARACTER_RELATIONS_BY_CHARID = "DELETE FROM character_relations WHERE charId=? OR friendId=?";

	// character_sharedata
	public static final String SELECT_CHARACTER_SHAREDATA = "SELECT * FROM character_sharedata ORDER BY account_name";
	public static final String REPLACE_CHARACTER_SHAREDATA = "REPLACE INTO character_sharedata (account_name, var, value) VALUES (?, ?, ?)";

	public static final String SELECT_CHARACTER_SUBCLASSES_BY_CHARID = "SELECT exp, sp, level FROM character_subclasses WHERE charId=? && class_id=? ORDER BY charId";
	public static final String SELECT_CHARACTER_QUESTS_NAME_VALUE = "SELECT name,value FROM character_quests WHERE charId=? AND var=?";
	public static final String SELECT_CHARACTER_QUESTS_NAME_VALUE_VAR = "SELECT name,var,value FROM character_quests WHERE charId=? AND var<>?";
	public static final String SELECT_QUEST_GLOBAL_DATA_VALUE = "SELECT value FROM quest_global_data WHERE quest_name = ? AND var = ?";
	public static final String SELECT_CLAN_DATA_CLAN_ID = "SELECT clan_id FROM clan_data";
	public static final String SELECT_CHARACTER_SUMMONS = "SELECT summon_object_id, summon_skill_id, cur_hp, cur_mp FROM character_summons WHERE owner_id = ? LIMIT ?";
	public static final String SELECT_PETS = "SELECT item_obj_id FROM pets WHERE ownerId=? AND restore = 'true'";

	// Виталити
	public static final String VITALITY_ADD = "INSERT INTO character_vitality (char_id, class_index, vitality_points, vitality_items) VALUES (?,?,?,?)";
	public static final String VITALITY_RESTORE = "SELECT class_index, vitality_points, vitality_items FROM character_vitality WHERE char_id=?";
	public static final String VITALITY_UPDATE = "UPDATE character_vitality SET vitality_points=?, vitality_items=? WHERE char_id=? AND class_index=?";
	public static final String VITALITY_CLEAR = "UPDATE character_vitality SET vitality_points=" + PcStat.MAX_VITALITY_POINTS + ", vitality_items=" + Config.VITALITY_ITEMS_WEEKLY_LIMIT;

	// Character Skill SQL String Definitions:
	public static final String SKILLS_RESTORE = "SELECT skill_id,skill_level FROM character_skills WHERE charId=? AND class_index IN (?,?)";
	public static final String SKILLS_ADD = "INSERT INTO character_skills (charId,skill_id,skill_level,class_index) VALUES (?,?,?,?)";
	public static final String SKILLS_LEVEL_UPDATE = "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND charId=? AND class_index=?";
	public static final String SKILLS_DELETE = "DELETE FROM character_skills WHERE skill_id=? AND charId=? AND class_index=?";
	public static final String SKILLS_DELETE_ALL = "DELETE FROM character_skills WHERE charId=? AND class_index=?";

	// Character Skill Save SQL String Definitions:
	public static final String EFFECTS_STORE = "INSERT INTO character_skills_save (charId,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)";
	public static final String EFFECTS_RESTORE = "SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay, systime, restore_type FROM character_skills_save WHERE charId=? AND class_index=? ORDER BY buff_index ASC";
	public static final String EFFECTS_CLEAR = "DELETE FROM character_skills_save WHERE charId=? AND class_index=?";

	// Character Item Reuse Time String Definition:
	public static final String ITEM_REUSE_STORE = "INSERT INTO character_item_reuse_save (charId,itemId,itemObjId,reuseDelay,systime) VALUES (?,?,?,?,?)";
	public static final String ITEM_REUSE_RESTORE = "SELECT charId,itemId,itemObjId,reuseDelay,systime FROM character_item_reuse_save WHERE charId=?";
	public static final String ITEM_REUSE_CLEAR = "DELETE FROM character_item_reuse_save WHERE charId=?";

	// Бинды игроков
	public static final String BINDS_STORE = "REPLACE INTO character_binds (obj_id, bind_data) values(?,?)";
	public static final String BINDS_RESTORE = "SELECT bind_data FROM character_binds WHERE obj_id=?";

	// Character Teleport Bookmark:
	public static final String TP_BOOKMARK_ADD = "INSERT INTO character_tpbookmark (charId,Id,x,y,z,icon,tag,name) values (?,?,?,?,?,?,?,?)";
	public static final String TP_BOOKMARK_UPDATE = "UPDATE character_tpbookmark SET icon=?,tag=?,name=? where charId=? AND Id=?";
	public static final String TP_BOOKMARK_RESTORE = "SELECT Id,x,y,z,icon,tag,name FROM character_tpbookmark WHERE charId=?";
	public static final String TP_BOOKMARK_DELETE = "DELETE FROM character_tpbookmark WHERE charId=? AND Id=?";

	// Character Subclass SQL String Definitions:
	public static final String SUBCLASSES_RESTORE = "SELECT class_id,exp,sp,level,class_index,class_type FROM character_subclasses WHERE charId=? ORDER BY class_index ASC";
	public static final String SUBCLASSES_ADD = "INSERT INTO character_subclasses (charId,class_id,exp,sp,level,class_index,class_type) VALUES (?,?,?,?,?,?,?)";
	public static final String SUBCLASSES_UPDATE = "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=?,class_type=? WHERE charId=? AND class_index =?";
	public static final String SUBCLASSES_DELETE = "DELETE FROM character_subclasses WHERE charId=? AND class_index=?";

	// Character Henna SQL String Definitions:
	public static final String HENNAS_RESTORE = "SELECT slot,symbol_id FROM character_hennas WHERE charId=? AND class_index=?";
	public static final String HENNAS_ADD = "INSERT INTO character_hennas (charId,symbol_id,slot,class_index) VALUES (?,?,?,?)";
	public static final String HENNAS_DELETE = "DELETE FROM character_hennas WHERE charId=? AND slot=? AND class_index=?";
	public static final String HENNAS_DELETE_ALL = "DELETE FROM character_hennas WHERE charId=? AND class_index=?";

	// Character Shortcut SQL String Definitions:
	public static final String SHORTCUTS_DELETE = "DELETE FROM character_shortcuts WHERE charId=? AND class_index=?";

	// Character zone restart time SQL String Definitions - L2Master mod
	public static final String ZONE_RESTART_LIMIT_CLEAR = "DELETE FROM character_norestart_zone_time WHERE charId = ?";
	public static final String ZONE_RESTART_LIMIT_RESTORE = "SELECT time_limit FROM character_norestart_zone_time WHERE charId = ?";
	public static final String ZONE_RESTART_LIMIT_UPDATE = "REPLACE INTO character_norestart_zone_time (charId, time_limit) VALUES (?,?)";

	// Character BBS buff Save SQL String Definitions:
	public static final String BBS_SCHEMA_ADD = "INSERT INTO character_community_buffs (charId,skill_id,skill_level) VALUES (?,?,?)";
	public static final String BBS_SCHEMA_RESTORE = "SELECT * FROM character_community_buffs WHERE charId=?";
	public static final String BBS_SCHEMA_CLEAR = "DELETE FROM character_community_buffs WHERE charId=?";
}