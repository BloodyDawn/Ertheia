package dwo.gameserver.handler.admincommands;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.datatables.xml.ClassTemplateTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.controller.player.PvPFlagController;
import dwo.gameserver.model.actor.instance.L2BabyPetInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.Calculator;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.model.world.communitybbs.Manager.RegionBBSManager;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.SetSummonRemainTime;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExSubjobInfo;
import dwo.gameserver.network.game.serverpackets.packet.gmview.GMViewItemList;
import dwo.gameserver.network.game.serverpackets.packet.info.CI;
import dwo.gameserver.network.game.serverpackets.packet.party.PartySmallWindowAll;
import dwo.gameserver.network.game.serverpackets.packet.party.PartySmallWindowDeleteAll;
import dwo.gameserver.util.StringUtil;
import dwo.gameserver.util.Util;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class AdminEditChar implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_edit_character", "admin_current_player", "admin_noreputation", // сбрасывает репутацию персонажу на ноль
		"admin_setreputation", // sets reputation of target char to any amount.
		// //setreputation <reputation>
		"admin_setfame", // sets fame of target char to any amount.
		// //setfame <fame>
		"admin_character_list", // same as character_info, kept for
		// compatibility purposes
		"admin_character_info", // given a player name, displays an
		// information window
		"admin_show_characters",// list of characters
		"admin_find_character", // find a player by his name or a part of it
		// (case-insensitive)
		"admin_find_ip", // find all the player connections from a given
		// IPv4 number
		"admin_find_account", // list all the characters from an account
		// (useful for GMs w/o DB access)
		"admin_find_dualbox", // list all the IPs with more than 1 char
		// logged in (dualbox)
		"admin_strict_find_dualbox", "admin_tracert", "admin_save_modifications", // consider
		// it
		// deprecated...
		"admin_rec", // gives recommendation points
		"admin_settitle", // changes char title
		"admin_changename", // changes char name
		"admin_setsex", // changes characters' sex
		"admin_setcolor", // change charnames' color display
		"admin_settcolor", // change char title color
		"admin_setclass", // changes templates' classId
		"admin_setpk", // changes PK count
		"admin_setpvp", // changes PVP count
		"admin_fullfood", // fulfills a pet's food bar
		"admin_remove_clan_penalty", // removes clan penalties
		"admin_summon_info", // displays an information window about target
		// summon
		"admin_unsummon", "admin_summon_setlvl", "admin_show_pet_inv", "admin_partyinfo", //
		"admin_sethero", "admin_setnoble", "admin_setdonator", "admin_char_item_list", "admin_char_stats"

	};
	private static Logger _log = LogManager.getLogger(AdminEditChar.class);

	/**
	 * Retrieve and replace player's info in filename htm file, sends it to activeChar as NpcHtmlMessage.
	 *
	 * @param activeChar
	 * @param player
	 * @param filename
	 */
	public static void gatherCharacterInfo(L2PcInstance activeChar, L2PcInstance player, String filename)
	{
		String ip = "N/A";
		String account = "N/A";

		if(player != null)
		{
			account = player.getAccountName();
			if(player.getClient() != null)
			{
				if(player.getClient().isDetached())
				{
					activeChar.sendMessage("Клиент отключен.");
				}
				else
				{
					ip = player.getClient().getConnection().getInetAddress().getHostAddress();
				}
			}
			else
			{
				activeChar.sendMessage("Клиент не существует.");
			}
		}
		else
		{
			activeChar.sendMessage("Игрок не существует.");
			return;
		}

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "mods/admin/" + filename);
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%clan%", String.valueOf(player.getClan() != null ? "<a action=\"bypass -h admin_clan_info " + player.getObjectId() + "\">" + player.getClan().getName() + "</a>" : null));
		adminReply.replace("%xp%", String.valueOf(player.getExp()));
		adminReply.replace("%sp%", String.valueOf(player.getSp()));
		adminReply.replace("%class%", ClassTemplateTable.getInstance().getClass(player.getClassId()).getClientCode());
		adminReply.replace("%ordinal%", String.valueOf(player.getClassId().ordinal()));
		adminReply.replace("%classid%", String.valueOf(player.getClassId()));
		adminReply.replace("%baseclassid%", String.valueOf(player.getBaseClassId()));
		adminReply.replace("%baseclass%", ClassTemplateTable.getInstance().getClass(player.getBaseClassId()).getClientCode());
		adminReply.replace("%coords%", String.valueOf(player.getLoc()));
		adminReply.replace("%currenthp%", String.valueOf((int) player.getCurrentHp()));
		adminReply.replace("%maxhp%", String.valueOf(player.getMaxHp()));
		adminReply.replace("%karma%", String.valueOf(player.getReputation()));
		adminReply.replace("%currentmp%", String.valueOf((int) player.getCurrentMp()));
		adminReply.replace("%maxmp%", String.valueOf(player.getMaxMp()));
		adminReply.replace("%pvpflag%", String.valueOf(player.getPvPFlagController().getStateValue()));
		adminReply.replace("%currentcp%", String.valueOf((int) player.getCurrentCp()));
		adminReply.replace("%maxcp%", String.valueOf(player.getMaxCp()));
		adminReply.replace("%pvpkills%", String.valueOf(player.getPvpKills()));
		adminReply.replace("%pkkills%", String.valueOf(player.getPkKills()));
		adminReply.replace("%currentload%", String.valueOf(player.getCurrentLoad()));
		adminReply.replace("%maxload%", String.valueOf(player.getMaxLoad()));
		adminReply.replace("%percent%", String.valueOf(Util.roundTo((float) player.getCurrentLoad() / player.getMaxLoad() * 100, 2)));
		adminReply.replace("%patk%", String.valueOf(player.getPAtk(null)));
		adminReply.replace("%matk%", String.valueOf(player.getMAtk(null, null)));
		adminReply.replace("%pdef%", String.valueOf(player.getPDef(null)));
		adminReply.replace("%mdef%", String.valueOf(player.getMDef(null, null)));
		adminReply.replace("%accuracy%", String.valueOf(player.getPhysicalAccuracy()));
		adminReply.replace("%evasion%", String.valueOf(player.getPhysicalEvasionRate(null)));
		adminReply.replace("%m_accuracy%", String.valueOf(player.getMagicalAccuracy()));
		adminReply.replace("%m_evasion%", String.valueOf(player.getMagicalEvasionRate(null)));
		adminReply.replace("%critical%", String.valueOf(player.getCriticalHit(null, null)));
		adminReply.replace("%m_critical%", String.valueOf(player.getMCriticalHit(null, null)));
		adminReply.replace("%runspeed%", String.valueOf(player.getRunSpeed()));
		adminReply.replace("%patkspd%", String.valueOf(player.getPAtkSpd()));
		adminReply.replace("%matkspd%", String.valueOf(player.getMAtkSpd()));
		adminReply.replace("%race%", String.valueOf(player.getRace()));
		adminReply.replace("%access%", String.valueOf(player.getAccessLevel().getLevel()));
		adminReply.replace("%account%", account);
		adminReply.replace("%ip%", ip);
		adminReply.replace("%ai%", String.valueOf(player.getAI().getIntention().name()));
		adminReply.replace("%inst%", player.getInstanceId() > 0 ? "<tr><td>InstanceId:</td><td><a action=\"bypass -h admin_instance_spawns " + player.getInstanceId() + "\">" + player.getInstanceId() + "</a></td></tr>" : "");
		activeChar.sendPacket(adminReply);
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.equals("admin_current_player"))
		{
			showCharacterInfo(activeChar, activeChar);
		}
		else if(command.startsWith("admin_character_info"))
		{
			String[] data = command.split(" ");
			if(data.length > 1)
			{
				showCharacterInfo(activeChar, WorldManager.getInstance().getPlayer(data[1]));
			}
			else if(activeChar.getTarget() instanceof L2PcInstance)
			{
				showCharacterInfo(activeChar, activeChar.getTarget().getActingPlayer());
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
		}
		else if(command.startsWith("admin_character_list"))
		{
			listCharacters(activeChar, 0);
		}
		else if(command.startsWith("admin_show_characters"))
		{
			try
			{
				String val = command.substring(22);
				int page = Integer.parseInt(val);
				listCharacters(activeChar, page);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				// Case of empty page number
				activeChar.sendMessage("Usage: //show_characters <page_number>");
			}
		}
		else if(command.startsWith("admin_find_character"))
		{
			try
			{
				String val = command.substring(21);
				findCharacter(activeChar, val);
			}
			catch(StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				activeChar.sendMessage("Usage: //find_character <character_name>");
				listCharacters(activeChar, 0);
			}
		}
		else if(command.startsWith("admin_find_ip"))
		{
			try
			{
				String val = command.substring(14);
				findCharactersPerIp(activeChar, val);
			}
			catch(Exception e)
			{ // Case of empty or malformed IP number
				activeChar.sendMessage("Usage: //find_ip <www.xxx.yyy.zzz>");
				listCharacters(activeChar, 0);
			}
		}
		else if(command.startsWith("admin_find_account"))
		{
			try
			{
				String val = command.substring(19);
				findCharactersPerAccount(activeChar, val);
			}
			catch(Exception e)
			{ // Case of empty or malformed player name
				activeChar.sendMessage("Usage: //find_account <player_name>");
				listCharacters(activeChar, 0);
			}
		}
		else if(command.startsWith("admin_edit_character"))
		{
			String[] data = command.split(" ");
			if(data.length > 1)
			{
				editCharacter(activeChar, data[1]);
			}
			else if(activeChar.getTarget() instanceof L2PcInstance)
			{
				editCharacter(activeChar, null);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
		}
		// Reputation control commands
		else if(command.equals("admin_noreputation"))
		{
			setTargetReputation(activeChar, 0);
		}
		else if(command.startsWith("admin_setreputation"))
		{
			try
			{
				String val = command.substring(20);
				int reputation = Integer.parseInt(val);
				setTargetReputation(activeChar, reputation);
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //setreputation <new_reputation_value>");
			}
		}
		else if(command.startsWith("admin_setpk"))
		{
			try
			{
				String val = command.substring(12);
				int pk = Integer.parseInt(val);
				L2Object target = activeChar.getTarget();
				if(target instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance) target;
					player.setPkKills(pk);
					player.broadcastUserInfo();
					player.sendUserInfo();
					player.sendMessage("A GM changed your PK count to " + pk);
					activeChar.sendMessage(player.getName() + "'s PK count changed to " + pk);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
			}
			catch(Exception e)
			{
				if(Config.DEVELOPER)
				{
					_log.log(Level.DEBUG, "Set pk error: " + e);
				}
				activeChar.sendMessage("Usage: //setpk <pk_count>");
			}
		}
		else if(command.startsWith("admin_setpvp"))
		{
			try
			{
				String val = command.substring(13);
				int pvp = Integer.parseInt(val);
				L2Object target = activeChar.getTarget();
				if(target instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance) target;
					player.setPvpKills(pvp);
					player.broadcastUserInfo();
					player.sendUserInfo();
					player.sendMessage("A GM changed your PVP count to " + pvp);
					activeChar.sendMessage(player.getName() + "'s PVP count changed to " + pvp);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
			}
			catch(Exception e)
			{
				if(Config.DEVELOPER)
				{
					_log.log(Level.DEBUG, "Set pvp error: " + e);
				}
				activeChar.sendMessage("Usage: //setpvp <pvp_count>");
			}
		}
		else if(command.startsWith("admin_setfame"))
		{
			try
			{
				String val = command.substring(14);
				int fame = Integer.parseInt(val);
				L2Object target = activeChar.getTarget();
				if(target instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance) target;
					player.setFame(fame);
					player.broadcastUserInfo();
					player.sendUserInfo();
					player.sendMessage("A GM changed your Reputation points to " + fame);
					activeChar.sendMessage(player.getName() + "'s Fame changed to " + fame);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
			}
			catch(Exception e)
			{
				if(Config.DEVELOPER)
				{
					_log.log(Level.DEBUG, "Set Fame error: " + e);
				}
				activeChar.sendMessage("Usage: //setfame <new_fame_value>");
			}
		}
		else if(command.startsWith("admin_save_modifications"))
		{
			try
			{
				String val = command.substring(24);
				adminModifyCharacter(activeChar, val);
			}
			catch(StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				activeChar.sendMessage("Error while modifying character.");
				listCharacters(activeChar, 0);
			}
		}
		else if(command.startsWith("admin_rec"))
		{
			try
			{
				String val = command.substring(10);
				int recVal = Integer.parseInt(val);
				L2Object target = activeChar.getTarget();
				if(target instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance) target;
					player.setRecommendations(recVal);
					player.broadcastUserInfo();
					player.sendUserInfo();
					//player.sendPacket(new ExVoteSystemInfo(player));
					player.sendMessage("A GM changed your Recommend points to " + recVal);
					activeChar.sendMessage(player.getName() + "'s Recommend changed to " + recVal);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //rec number");
			}
		}
		else if(command.startsWith("admin_setclass"))
		{
			try
			{
				String val = command.substring(15).trim();
				int classidval = Integer.parseInt(val);
				L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if(target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					return false;
				}
				boolean valid = false;
				for(ClassId classid : ClassId.values())
				{
					if(classidval == classid.getId())
					{
						valid = true;
					}
				}
				if(valid && player.getClassId().getId() != classidval)
				{
					player.setClassId(classidval);
					if(!player.isSubClassActive())
					{
						player.setBaseClassId(classidval);
					}
					String newclass = ClassTemplateTable.getInstance().getClass(player.getClassId()).getClassName();
					player.store();
					player.sendMessage("A GM changed your class to " + newclass);
					player.broadcastUserInfo();
					player.sendPacket(new ExSubjobInfo(player));
					activeChar.sendMessage(player.getName() + " is a " + newclass);
				}
				else
				{
					activeChar.sendMessage("Usage: //setclass <valid_new_classid>");
				}
			}
			catch(StringIndexOutOfBoundsException e)
			{
				AdminHelpPage.showHelpPage(activeChar, "charclasses/main.htm");
			}
			catch(NumberFormatException e)
			{
				activeChar.sendMessage("Usage: //setclass <valid_new_classid>");
			}
		}
		else if(command.startsWith("admin_settitle"))
		{
			try
			{
				String val = command.substring(15);
				L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if(target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					return false;
				}
				player.setTitle(val);
				player.sendMessage("Your title has been changed by a GM");
				player.broadcastTitleInfo();
			}
			catch(StringIndexOutOfBoundsException e)
			{ // Case of empty character title
				activeChar.sendMessage("You need to specify the new title.");
			}
		}
		else if(command.startsWith("admin_changename"))
		{
			try
			{
				String val = command.substring(17);
				L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if(target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					return false;
				}
				if(CharNameTable.getInstance().getIdByName(val) > 0)
				{
					activeChar.sendMessage("Warning, player " + val + " already exists");
					return false;
				}
				player.setName(val);
				player.store();
				CharNameTable.getInstance().addName(player);

				activeChar.sendMessage("Changed name to " + val);
				player.sendMessage("Your name has been changed by a GM.");
				player.broadcastUserInfo();

				if(player.isInParty())
				{
					// Delete party window for other party members
					player.getParty().broadcastPacket(player, new PartySmallWindowDeleteAll());
					for(L2PcInstance member : player.getParty().getMembers())
					{
						// And re-add
						if(!member.equals(player))
						{
							member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
						}
					}
				}
				if(player.getClan() != null)
				{
					player.getClan().broadcastClanStatus();
				}

				RegionBBSManager.getInstance().changeCommunityBoard();
			}
			catch(StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				activeChar.sendMessage("Usage: //setname new_name_for_target");
			}
		}
		else if(command.startsWith("admin_setsex"))
		{
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			if(target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			else
			{
				return false;
			}
			player.getAppearance().setSex(!player.getAppearance().getSex());
			player.sendMessage("Your gender has been changed by a GM");
			player.broadcastUserInfo();
			player.getLocationController().decay();
			player.getLocationController().spawn(player.getX(), player.getY(), player.getZ());
		}
		else if(command.startsWith("admin_fullfood"))
		{
			L2Object target = activeChar.getTarget();
			if(target instanceof L2PetInstance)
			{
				L2PetInstance targetPet = (L2PetInstance) target;
				targetPet.setCurrentFed(targetPet.getMaxFed());
				targetPet.sendPacket(new SetSummonRemainTime(targetPet.getMaxFed(), targetPet.getCurrentFed()));
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
		}
		else if(command.startsWith("admin_remove_clan_penalty"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				if(st.countTokens() != 3)
				{
					activeChar.sendMessage("Usage: //remove_clan_penalty join|create charname");
					return false;
				}

				st.nextToken();

				boolean changeCreateExpiryTime = st.nextToken().equalsIgnoreCase("create");

				String playerName = st.nextToken();
				L2PcInstance player = null;
				player = WorldManager.getInstance().getPlayer(playerName);

				if(player == null)
				{
					ThreadConnection con = L2DatabaseFactory.getInstance().getConnection();
					FiltredPreparedStatement ps = con.prepareStatement("UPDATE characters SET " + (changeCreateExpiryTime ? "clan_create_expiry_time" : "clan_join_expiry_time") + " WHERE char_name=? LIMIT 1");

					ps.setString(1, playerName);
					ps.execute();
				}
				else
				{
					// removing penalty
					if(changeCreateExpiryTime)
					{
						player.setClanCreateExpiryTime(0);
					}
					else
					{
						player.setClanJoinExpiryTime(0);
					}
				}

				activeChar.sendMessage("Clan penalty successfully removed to character: " + playerName);
			}
			catch(Exception e)
			{
				// Ignored
			}
		}
		else if(command.startsWith("admin_find_dualbox"))
		{
			int multibox = 2;
			try
			{
				String val = command.substring(19);
				multibox = Integer.parseInt(val);
				if(multibox < 1)
				{
					activeChar.sendMessage("Usage: //find_dualbox [number > 0]");
					return false;
				}
			}
			catch(Exception e)
			{
				// Ignored
			}
			findDualbox(activeChar, multibox);
		}
		else if(command.startsWith("admin_strict_find_dualbox"))
		{
			int multibox = 2;
			try
			{
				String val = command.substring(26);
				multibox = Integer.parseInt(val);
				if(multibox < 1)
				{
					activeChar.sendMessage("Usage: //strict_find_dualbox [number > 0]");
					return false;
				}
			}
			catch(Exception e)
			{
				// Ignored
			}
			findDualboxStrict(activeChar, multibox);
		}
		else if(command.startsWith("admin_tracert"))
		{
			String[] data = command.split(" ");
			L2PcInstance pl = null;
			if(data.length > 1)
			{
				pl = WorldManager.getInstance().getPlayer(data[1]);
			}
			else
			{
				L2Object target = activeChar.getTarget();
				if(target instanceof L2PcInstance)
				{
					pl = (L2PcInstance) target;
				}
			}

			if(pl == null)
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}

			if(pl.getClient() == null)
			{
				activeChar.sendMessage("Клиент отсутвует.");
				return false;
			}

			if(pl.getClient().isDetached())
			{
				activeChar.sendMessage("Клиент игрока отключен от сервера.");
				return false;
			}

			String ip;
			int[][] trace = pl.getClient().getTrace();
			for(int i = 0; i < trace.length; i++)
			{
				ip = "";
				for(int o = 0; o < trace[0].length; o++)
				{
					ip += trace[i][o];
					if(o != trace[0].length - 1)
					{
						ip += '.';
					}
				}
				activeChar.sendMessage("Hop" + i + ": " + ip);
			}
		}
		else if(command.startsWith("admin_summon_info"))
		{
			L2Object target = activeChar.getTarget();
			if(target instanceof L2Summon)
			{
				gatherSummonInfo((L2Summon) target, activeChar);
			}
			else
			{
				activeChar.sendMessage("Неверная цель.");
			}
		}
		else if(command.startsWith("admin_unsummon"))
		{
			L2Object target = activeChar.getTarget();
			if(target instanceof L2Summon)
			{
				((L2Summon) target).unSummon(true);
			}
			else
			{
				activeChar.sendMessage("Может использовать только на питомцах.");
			}
		}
		else if(command.startsWith("admin_summon_setlvl"))
		{
			L2Object target = activeChar.getTarget();
			if(target instanceof L2PetInstance)
			{
				L2PetInstance pet = (L2PetInstance) target;
				try
				{
					String val = command.substring(20);
					int level = Integer.parseInt(val);
					long newexp;
					long oldexp = 0;
					oldexp = pet.getStat().getExp();
					newexp = pet.getStat().getExpForLevel(level);
					if(oldexp > newexp)
					{
						pet.getStat().removeExp(oldexp - newexp);
					}
					else if(oldexp < newexp)
					{
						pet.getStat().addExp(newexp - oldexp);
					}
				}
				catch(Exception e)
				{
					// Ignored
				}
			}
			else
			{
				activeChar.sendMessage("Можно использовать только на питомцах.");
			}
		}
		else if(command.startsWith("admin_show_pet_inv"))
		{
			String val;
			int objId;
			L2Object target;
			try
			{
				val = command.substring(19);
				objId = Integer.parseInt(val);
				target = WorldManager.getInstance().getPets(objId).getFirst(); // TODO: Сделать показ инвентаря для всех петов
				if(target instanceof L2PcInstance && !target.getActingPlayer().getPets().isEmpty())
				{
					for(L2Summon summon : target.getActingPlayer().getPets())
					{
						if(summon instanceof L2PetInstance || summon instanceof L2BabyPetInstance)
						{
							target = summon;
							break;
						}
					}
				}
			}
			catch(Exception e)
			{
				target = activeChar.getTarget();
			}

			if(target instanceof L2PetInstance)
			{
				activeChar.sendPacket(new GMViewItemList((L2PetInstance) target));
			}
			else
			{
				activeChar.sendMessage("Можно использовать только на питомцах.");
			}

		}
		else if(command.startsWith("admin_partyinfo"))
		{
			String val;
			L2Object target;
			try
			{
				val = command.substring(16);
				target = WorldManager.getInstance().getPlayer(val);
				if(target == null)
				{
					target = activeChar.getTarget();
				}
			}
			catch(Exception e)
			{
				target = activeChar.getTarget();
			}

			if(target instanceof L2PcInstance)
			{
				if(((L2PcInstance) target).isInParty())
				{
					gatherPartyInfo((L2PcInstance) target, activeChar);
				}
				else
				{
					activeChar.sendMessage("Вы не состоите в группе.");
				}
			}
			else
			{
				activeChar.sendMessage("Неверная цель.");
			}
		}
		else if(command.startsWith("admin_char_item_list "))
		{
			L2PcInstance target = null;
			String[] cmd = command.split(" ");
			if(activeChar.getTarget() != null && activeChar.getTarget() instanceof L2PcInstance)
			{
				target = (L2PcInstance) activeChar.getTarget();
				showCharItems(activeChar, target, Integer.parseInt(cmd[1]));
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			}
		}
		else if(command.startsWith("admin_char_stats"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();

			if(st.hasMoreTokens())
			{
				L2PcInstance player = null;
				String playername = st.nextToken();
				try
				{
					player = WorldManager.getInstance().getPlayer(playername);
				}
				catch(Exception ignored)
				{
				}

				if(player != null)
				{
					int page = -1;
					if(st.hasMoreTokens())
					{
						page = Integer.parseInt(st.nextToken());
					}
					showStats(activeChar, player, page);
					return true;
				}
				else
				{
					activeChar.sendMessage("The player " + playername + " is not online");
					return false;
				}
			}
			else if(activeChar.getTarget() != null && activeChar.getTarget() instanceof L2Character)
			{
				showStats(activeChar, (L2Character) activeChar.getTarget(), -1);
				return true;
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}
		}
        else if (command.equals("admin_setnoble"))
        {
            L2PcInstance player = null;
            if (activeChar.getTarget() == null)
            {
                player = activeChar;
            }
            else if ((activeChar.getTarget() != null) && (activeChar.getTarget() instanceof L2PcInstance))
            {
                player = (L2PcInstance) activeChar.getTarget();
            }

            if (player != null)
            {
                player.setNoble(!player.isNoble());
                if (player.getObjectId() != activeChar.getObjectId())
                {
                    activeChar.sendMessage("Вы получили статус дворянина: " + player.getName());
                }
                player.sendMessage("GM изменил ваш статус Дворянина!");
            }
        }
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void listCharacters(L2PcInstance activeChar, int page)
	{

		L2PcInstance[] players = WorldManager.getInstance().getAllPlayersArray();

		int maxCharactersPerPage = 20;
		int maxPages = players.length / maxCharactersPerPage;

		if(players.length > maxCharactersPerPage * maxPages)
		{
			maxPages++;
		}

		// Check if number of users changed
		if(page > maxPages)
		{
			page = maxPages;
		}

		int charactersStart = maxCharactersPerPage * page;
		int charactersEnd = players.length;
		if(charactersEnd - charactersStart > maxCharactersPerPage)
		{
			charactersEnd = charactersStart + maxCharactersPerPage;
		}

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "mods/admin/charlist.htm");
		adminReply.replace("%online%", String.valueOf(players.length));
		adminReply.replace("%page%", String.valueOf(page + 1));

		StringBuilder replyMSG = new StringBuilder(1000);

		for(int x = 0; x < maxPages; x++)
		{
			int pagenr = x + 1;
			StringUtil.append(replyMSG, "<center><a action=\"bypass -h admin_show_characters ", String.valueOf(x), "\">Page ", String.valueOf(pagenr), "</a></center>");
		}

		adminReply.replace("%pages%", replyMSG.toString());
		replyMSG.setLength(0);

		for(int i = charactersStart; i < charactersEnd; i++)
		{
			// Add player info into new Table row
			StringUtil.append(replyMSG, "<tr><td width=80><a action=\"bypass -h admin_character_info ", players[i].getName(), "\">", players[i].getName(), "</a></td><td width=110>", ClassTemplateTable.getInstance().getClass(players[i].getClassId()).getClassName(), "</td><td width=40>", String.valueOf(players[i].getLevel()), "</td></tr>");
		}

		adminReply.replace("%players%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void showCharacterInfo(L2PcInstance activeChar, L2PcInstance player)
	{
		if(player == null)
		{
			L2Object target = activeChar.getTarget();
			if(target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			else
			{
				return;
			}
		}
		else
		{
			activeChar.setTarget(player);
		}
		gatherCharacterInfo(activeChar, player, "charinfo.htm");
	}

	private void setTargetReputation(L2PcInstance activeChar, int newReputation)
	{
		// function to change karma of selected char
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if(target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			return;
		}

		// for display
		int oldReputation = player.getReputation();
		// update reputation
		player.setReputation(newReputation);
		// Common character information
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_REPUTATION).addString(String.valueOf(newReputation)));
		// Admin information
		activeChar.sendMessage("Репутация изменена игроку " + player.getName() + " с (" + oldReputation + ") на (" + newReputation + ").");
	}

	private void adminModifyCharacter(L2PcInstance activeChar, String modifications)
	{
		L2Object target = activeChar.getTarget();

		if(!(target instanceof L2PcInstance))
		{
			return;
		}

		L2PcInstance player = (L2PcInstance) target;
		StringTokenizer st = new StringTokenizer(modifications);

		if(st.countTokens() != 6)
		{
			editCharacter(activeChar, null);
			return;
		}

		String hp = st.nextToken();
		String mp = st.nextToken();
		String cp = st.nextToken();
		String pvpflag = st.nextToken();
		String pvpkills = st.nextToken();
		String pkkills = st.nextToken();

		int hpval = Integer.parseInt(hp);
		int mpval = Integer.parseInt(mp);
		int cpval = Integer.parseInt(cp);
		int pvpflagval = Integer.parseInt(pvpflag);
		int pvpkillsval = Integer.parseInt(pvpkills);
		int pkkillsval = Integer.parseInt(pkkills);

		// Common character information
		player.sendMessage("Администрация изменила Ваши характеристики." + "  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + "  PvP Флаг: " + pvpflagval + " PvP/PK " + pvpkillsval + '/' + pkkillsval);
		player.setCurrentHp(hpval);
		player.setCurrentMp(mpval);
		player.setCurrentCp(cpval);

		if(PvPFlagController.FlagState.valueOf(pvpflagval) == PvPFlagController.FlagState.NO_FLAG)
		{
			player.getPvPFlagController().stopFlag();
		}
		else
		{
			player.getPvPFlagController().startFlag();
		}

		player.setPvpKills(pvpkillsval);
		player.setPkKills(pkkillsval);

		// Save the changed parameters to the database.
		player.store();

		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_HP, hpval);
		su.addAttribute(StatusUpdate.MAX_HP, player.getMaxHp());
		su.addAttribute(StatusUpdate.CUR_MP, mpval);
		su.addAttribute(StatusUpdate.MAX_MP, player.getMaxMp());
		su.addAttribute(StatusUpdate.CUR_CP, cpval);
		su.addAttribute(StatusUpdate.MAX_CP, player.getMaxCp());
		player.sendPacket(su);

		// Admin information
		activeChar.sendMessage("Изменены характеристики " + player.getName() + '.' + "  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + "  PvP: " + pvpflagval + " / " + pvpkillsval);

		showCharacterInfo(activeChar, null); // Back to start

		player.broadcastPacket(new CI(player));
		player.sendUserInfo();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.getLocationController().decay();
		player.getLocationController().spawn(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}

	private void editCharacter(L2PcInstance activeChar, String targetName)
	{
		L2Object target = null;
		target = targetName != null ? WorldManager.getInstance().getPlayer(targetName) : activeChar.getTarget();

		if(target instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) target;
			gatherCharacterInfo(activeChar, player, "charedit.htm");
		}
	}

	/**
	 * @param activeChar
	 * @param CharacterToFind
	 */
	private void findCharacter(L2PcInstance activeChar, String CharacterToFind)
	{
		int CharactersFound = 0;
		String name;
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "mods/admin/charfind.htm");

		StringBuilder replyMSG = new StringBuilder(1000);

		for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
		{ // Add player info into new Table row
			name = player.getName();
			if(name.toLowerCase().contains(CharacterToFind.toLowerCase()))
			{
				CharactersFound += 1;
				StringUtil.append(replyMSG, "<tr><td width=80><a action=\"bypass -h admin_character_info ", name, "\">", name, "</a></td><td width=110>", ClassTemplateTable.getInstance().getClass(player.getClassId()).getClassName(), "</td><td width=40>", String.valueOf(player.getLevel()), "</td></tr>");
			}
			if(CharactersFound > 20)
			{
				break;
			}
		}
		adminReply.replace("%results%", replyMSG.toString());

		String replyMSG2;

		if(CharactersFound == 0)
		{
			replyMSG2 = "s. Попробуйте снова.";
		}
		else if(CharactersFound > 20)
		{
			adminReply.replace("%number%", " больше чем 20");
			replyMSG2 = "s.<br>Пожалуйста, сузьте область поиска, чтобы просмотреть все результаты.";
		}
		else
		{
			replyMSG2 = CharactersFound == 1 ? "." : "s.";
		}

		adminReply.replace("%number%", String.valueOf(CharactersFound));
		adminReply.replace("%end%", replyMSG2);
		activeChar.sendPacket(adminReply);
	}

	/**
	 * @param activeChar
	 * @param IpAdress
	 * @throws IllegalArgumentException
	 */
	private void findCharactersPerIp(L2PcInstance activeChar, String IpAdress) throws IllegalArgumentException
	{
		boolean findDisconnected = false;

		if(IpAdress.equals("disconnected"))
		{
			findDisconnected = true;
		}
		else
		{
			if(!IpAdress.matches("^(?:(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))$"))
			{
				throw new IllegalArgumentException("Malformed IPv4 number");
			}
		}

		int CharactersFound = 0;
		L2GameClient client;
		String name;
		String ip = "0.0.0.0";
		StringBuilder replyMSG = new StringBuilder(1000);
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "mods/admin/ipfind.htm");
		for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
		{
			client = player.getClient();
			if(client.isDetached())
			{
				if(!findDisconnected)
				{
					continue;
				}
			}
			else
			{
				if(findDisconnected)
				{
					continue;
				}
				else
				{
					ip = client.getConnection().getInetAddress().getHostAddress();
					if(!ip.equals(IpAdress))
					{
						continue;
					}
				}
			}

			name = player.getName();
			CharactersFound += 1;
			StringUtil.append(replyMSG, "<tr><td width=80><a action=\"bypass -h admin_character_list ", name, "\">", name, "</a></td><td width=110>", ClassTemplateTable.getInstance().getClass(player.getClassId()).getClientCode(), "</td><td width=40>", String.valueOf(player.getLevel()), "</td></tr>");

			if(CharactersFound > 20)
			{
				break;
			}
		}
		adminReply.replace("%results%", replyMSG.toString());

		String replyMSG2;

		if(CharactersFound == 0)
		{
			replyMSG2 = "s. Maybe they got d/c? :)";
		}
		else if(CharactersFound > 20)
		{
			adminReply.replace("%number%", " больше чем " + CharactersFound);
			replyMSG2 = "s.<br>Во избежание креша клиента,я не буду <br1>показывать резуотаты с более чем 20-тью символами.";
		}
		else
		{
			replyMSG2 = CharactersFound == 1 ? "." : "s.";
		}
		adminReply.replace("%ip%", IpAdress);
		adminReply.replace("%number%", String.valueOf(CharactersFound));
		adminReply.replace("%end%", replyMSG2);
		activeChar.sendPacket(adminReply);
	}

	/**
	 * @param activeChar
	 * @param characterName
	 * @throws IllegalArgumentException
	 */
	private void findCharactersPerAccount(L2PcInstance activeChar, String characterName) throws IllegalArgumentException
	{
		if(characterName.matches(Config.CNAME_TEMPLATE))
		{
			String account = null;
			TIntObjectHashMap<String> chars;
			L2PcInstance player = WorldManager.getInstance().getPlayer(characterName);
			if(player == null)
			{
				throw new IllegalArgumentException("Player doesn't exist");
			}
			chars = player.getAccountChars();
			account = player.getAccountName();
			StringBuilder replyMSG = new StringBuilder(chars.size() * 20);
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(activeChar.getLang(), "mods/admin/accountinfo.htm");

			for(String charname : chars.values(new String[chars.size()]))
			{
				StringUtil.append(replyMSG, charname, "<br1>");
			}

			adminReply.replace("%characters%", replyMSG.toString());
			adminReply.replace("%account%", account);
			adminReply.replace("%player%", characterName);
			activeChar.sendPacket(adminReply);
		}
		else
		{
			throw new IllegalArgumentException("Malformed character name");
		}
	}

	/**
	 * @param activeChar
	 */
	private void findDualbox(L2PcInstance activeChar, int multibox)
	{
		Map<String, List<L2PcInstance>> ipMap = new HashMap<>();

		String ip = "0.0.0.0";
		L2GameClient client;

		Map<String, Integer> dualboxIPs = new HashMap<>();

		for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
		{
			client = player.getClient();
			if(client == null || client.isDetached())
			{
			}
			else
			{
				ip = client.getConnection().getInetAddress().getHostAddress();
				if(ipMap.get(ip) == null)
				{
					ipMap.put(ip, new ArrayList<>());
				}
				ipMap.get(ip).add(player);

				if(ipMap.get(ip).size() >= multibox)
				{
					Integer count = dualboxIPs.get(ip);
					if(count == null)
					{
						dualboxIPs.put(ip, multibox);
					}
					else
					{
						dualboxIPs.put(ip, count + 1);
					}
				}
			}
		}

		List<String> keys = new ArrayList<>(dualboxIPs.keySet());
		Collections.sort(keys, (left, right) -> dualboxIPs.get(left).compareTo(dualboxIPs.get(right)));
		Collections.reverse(keys);

		StringBuilder results = new StringBuilder();
		for(String dualboxIP : keys)
		{
			StringUtil.append(results, "<a action=\"bypass -h admin_find_ip " + dualboxIP + "\">" + dualboxIP + " (" + dualboxIPs.get(dualboxIP) + ")</a><br1>");
		}

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "mods/admin/dualbox.htm");
		adminReply.replace("%multibox%", String.valueOf(multibox));
		adminReply.replace("%results%", results.toString());
		adminReply.replace("%strict%", "");
		activeChar.sendPacket(adminReply);
	}

	private void findDualboxStrict(L2PcInstance activeChar, int multibox)
	{
		Map<IpPack, List<L2PcInstance>> ipMap = new HashMap<>();

		L2GameClient client;

		Map<IpPack, Integer> dualboxIPs = new HashMap<>();

		for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
		{
			client = player.getClient();
			if(client == null || client.isDetached())
			{
			}
			else
			{
				IpPack pack = new IpPack(client.getConnection().getInetAddress().getHostAddress(), client.getTrace());
				if(ipMap.get(pack) == null)
				{
					ipMap.put(pack, new ArrayList<>());
				}
				ipMap.get(pack).add(player);

				if(ipMap.get(pack).size() >= multibox)
				{
					Integer count = dualboxIPs.get(pack);
					if(count == null)
					{
						dualboxIPs.put(pack, multibox);
					}
					else
					{
						dualboxIPs.put(pack, count + 1);
					}
				}
			}
		}

		List<IpPack> keys = new ArrayList<>(dualboxIPs.keySet());
		Collections.sort(keys, (left, right) -> dualboxIPs.get(left).compareTo(dualboxIPs.get(right)));
		Collections.reverse(keys);

		StringBuilder results = new StringBuilder();
		for(IpPack dualboxIP : keys)
		{
			StringUtil.append(results, "<a action=\"bypass -h admin_find_ip " + dualboxIP.ip + "\">" + dualboxIP.ip + " (" + dualboxIPs.get(dualboxIP) + ")</a><br1>");
		}

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "mods/admin/dualbox.htm");
		adminReply.replace("%multibox%", String.valueOf(multibox));
		adminReply.replace("%results%", results.toString());
		adminReply.replace("%strict%", "strict_");
		activeChar.sendPacket(adminReply);
	}

	private void gatherSummonInfo(L2Summon target, L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar.getLang(), "mods/admin/petinfo.htm");
		String name = target.getName();
		html.replace("%name%", name == null ? "Н/Д" : name);
		html.replace("%npcId%", String.valueOf(target.getNpcId()));
		html.replace("%level%", Integer.toString(target.getLevel()));
		html.replace("%exp%", Long.toString(target.getStat().getExp()));
		String owner = target.getActingPlayer().getName();
		html.replace("%owner%", " <a action=\"bypass -h admin_character_info " + owner + "\">" + owner + "</a>");
		html.replace("%class%", target.getClass().getSimpleName());
		html.replace("%ai%", target.hasAI() ? String.valueOf(target.getAI().getIntention().name()) : "NULL");
		html.replace("%hp%", (int) target.getStatus().getCurrentHp() + "/" + target.getStat().getMaxHp());
		html.replace("%mp%", (int) target.getStatus().getCurrentMp() + "/" + target.getStat().getMaxMp());
		html.replace("%str%", String.valueOf(target.getStat().getSTR()));
		html.replace("%con%", String.valueOf(target.getStat().getCON()));
		html.replace("%dex%", String.valueOf(target.getStat().getDEX()));
		html.replace("%int%", String.valueOf(target.getStat().getINT()));
		html.replace("%wit%", String.valueOf(target.getStat().getWIT()));
		html.replace("%men%", String.valueOf(target.getStat().getMEN()));
		html.replace("%pAtk%", String.valueOf(target.getStat().getPAtk(target)));
		html.replace("%pDef%", String.valueOf(target.getStat().getPDef(target)));
		html.replace("%mAtk%", String.valueOf(target.getStat().getMAtk(target, null)));
		html.replace("%mDef%", String.valueOf(target.getStat().getMDef(target, null)));
		html.replace("%pAtkSpd%", String.valueOf(target.getStat().getPAtkSpd()));
		html.replace("%mAtkSpd%", String.valueOf(target.getStat().getMAtkSpd()));
		html.replace("%karma%", Integer.toString(target.getReputation()));
		html.replace("%undead%", target.isUndead() ? "ДА" : "НЕТ");
		if(target instanceof L2PetInstance)
		{
			int objId = target.getActingPlayer().getObjectId();
			html.replace("%inv%", " <a action=\"bypass admin_show_pet_inv " + objId + "\">view</a>");
		}
		else
		{
			html.replace("%inv%", "none");
		}
		if(target instanceof L2PetInstance)
		{
			html.replace("%food%", ((L2PetInstance) target).getCurrentFed() + "/" + ((L2PetInstance) target).getPetLevelData().getPetMaxFeed());
			html.replace("%load%", target.getInventory().getTotalWeight() + "/" + target.getMaxLoad());
		}
		else
		{
			html.replace("%food%", "N/A");
			html.replace("%load%", "N/A");
		}
		activeChar.sendPacket(html);
	}

	private void gatherPartyInfo(L2PcInstance target, L2PcInstance activeChar)
	{
		boolean color = true;
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar.getLang(), "mods/admin/partyinfo.htm");
		StringBuilder text = new StringBuilder(400);
		for(L2PcInstance member : target.getParty().getMembers())
		{
			if(color)
			{
				text.append("<tr><td><table width=270 border=0 bgcolor=131210 cellpadding=2><tr><td width=30 align=right>");
			}
			else
			{
				text.append("<tr><td><table width=270 border=0 cellpadding=2><tr><td width=30 align=right>");
			}
			text.append(member.getLevel()).append("</td><td width=130><a action=\"bypass -h admin_character_info ").append(member.getName()).append("\">").append(member.getName()).append("</a>");
			text.append("</td><td width=110 align=right>").append(member.getClassId()).append("</td></tr></table></td></tr>");
			color = !color;
		}
		html.replace("%player%", target.getName());
		html.replace("%party%", text.toString());
		activeChar.sendPacket(html);
	}

	private void showCharItems(L2PcInstance activeChar, L2PcInstance target, int page)
	{
		int LIMIT = 30; //количество

		if(page <= 0)
		{
			page = 1;
		}

		StringBuilder html = new StringBuilder();
		List<L2ItemInstance> linkedList = new LinkedList<>();
		if(target == null)
		{
			return;
		}

		L2ItemInstance[] list = target.getInventory().getItems();
		Collections.addAll(linkedList, list);

		int size = list.length;
		int max = size / LIMIT;
		if(size > LIMIT * max)
		{
			max++;
		}

		html.append("<html><title>").append(page).append(" из ").append(max).append("</title><body><br>");
		html.append("<table width=300 bgcolor=666666><tr>");
		html.append("<td>Страница </td>");
		for(int x = 0; x < max; x++)
		{
			int pagenr = x + 1;
			if(page == pagenr)
			{
				html.append("<td>");
				html.append(pagenr);
				html.append("</td>");
			}
			else
			{
				html.append("<td><a action=\"bypass -h admin_char_item_list ");
				html.append(pagenr);
				html.append("\"> ");
				html.append(pagenr);
				html.append("</a></td>");
			}
		}
		html.append("</tr></table>");

		int start = (page - 1) * LIMIT;
		int end = Math.min((page - 1) * LIMIT + LIMIT, size);

		html.append("<table width=\"100%\"><tr><td width=40>id</td><td width=170>name</td><td width=20>Del</td></tr>");
		linkedList.subList(start, end).stream().filter(item -> item != null).forEach(item -> {
			html.append("<tr>");

			html.append("<td>");
			html.append(item.getItemId());
			html.append("</td>");

			html.append("<td>");
			html.append(item.getName());
			html.append("</td>");

			html.append("<td><a action=\"bypass -h admin_char_del_item ");
			html.append(target.getObjectId());
			html.append(' ');
			html.append(item.getObjectId());
			html.append("\"> D");
			html.append("</a></td>");

			html.append("</tr>");

			html.append("<tr>");

			html.append("<td></td>");
			html.append("<td>");
			if(item.getEnchantLevel() > 0)
			{
				html.append("Заточка: +");
				html.append(item.getEnchantLevel());
				html.append(' ');
			}
			html.append("Кол-во: ");
			html.append(item.getCount());

			html.append("</td>");
			html.append("<td></td>");

			html.append("</tr>");
		});
		html.append("</table></body></html>");

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(html.toString());
		activeChar.sendPacket(adminReply);
	}

	public void showStats(L2PcInstance activeChar, L2Character target, int stats)
	{

		StringBuilder html = StringUtil.startAppend(20480,
			// Шапка
			"<html><table width=\"100%\"><tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=180><center><font color=\"LEVEL\">Effects of ", target.getName(), "</font></td><td width=45><button value=\"Back\" action=\"bypass -h admin_current_player\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");

		if(stats == -1)
		{
			html.append("<center><table>");
			// Выводим список калькуляторов у чара
			for(Stats stat : Stats.values())
			{
				Calculator calc = target.getCalculators()[stat.ordinal()];
				if(calc != null)
				{
					StringUtil.append(html, "<tr><td><button value=\"", stat.getValue(), "\" action=\"bypass -h admin_char_stats ", String.valueOf(target.getName()), " ", String.valueOf(stat.ordinal()), "\" width=200 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
				}
			}
			html.append("</table></center>");
		}
		else
		{
			double base = 0;
			if(stats == Stats.MAX_HP.ordinal())
			{
				base = target.getTemplate().getBaseHpMax();
			}
			else if(stats == Stats.MAX_MP.ordinal())
			{
				base = target.getTemplate().getBaseMpMax();
			}
			else if(stats == Stats.MAX_CP.ordinal())
			{
				base = target.getTemplate().getBaseCpMax();
			}
			else if(stats == Stats.MAX_RECOVERABLE_HP.ordinal())
			{
				base = target.getMaxVisibleHp();
			}
			else if(stats == Stats.MAX_RECOVERABLE_MP.ordinal())
			{
				base = target.getMaxMp();
			}
			else if(stats == Stats.MAX_RECOVERABLE_CP.ordinal())
			{
				base = target.getMaxCp();
			}/*
		           	REGENERATE_HP_RATE("regHp"),			// Регенерация HP
					REGENERATE_MP_RATE("regMp"),			// Регенерация MP
					REGENERATE_CP_RATE("regCp"),			// Регенерация CP
					RECHARGE_MP_RATE("gainMp"),             // Эффективность восстановления цели МП

			 */
			else if(stats == Stats.HEAL_EFFECTIVNESS.ordinal())
			{
				base = 100;
			}
			else if(stats == Stats.HEAL_PROFICIENCY.ordinal())
			{
				base = 100;
			}
			else if(stats == Stats.HEAL_STATIC_BONUS.ordinal())
			{
				base = 0;
			}
			else if(stats == Stats.LIMIT_HP.ordinal())
			{
				base = target.getMaxVisibleHp();
			}
			else if(stats == Stats.POWER_DEFENCE.ordinal())
			{
				base = target.getTemplate().getBasePDef();
			}
			else if(stats == Stats.MAGIC_DEFENCE.ordinal())
			{
				base = target.getTemplate().getBaseMDef();
			}
			else if(stats == Stats.POWER_ATTACK.ordinal())
			{
				base = target.getTemplate().getBasePAtk();
			}
			else if(stats == Stats.MAGIC_ATTACK.ordinal())
			{
				base = target.getTemplate().getBaseMAtk();
			}
			else if(stats == Stats.PHYSICAL_SKILL_POWER.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.PHYSICAL_SKILL_POWER_ADD.ordinal())
			{
				base = 0;
			}
			else if(stats == Stats.POWER_ATTACK_SPEED.ordinal())
			{
				base = target.getTemplate().getBasePAtkSpd();
			}
			else if(stats == Stats.MAGIC_ATTACK_SPEED.ordinal())
			{
				base = target.getTemplate().getBaseMAtkSpd();
			}
			else if(stats == Stats.ATK_REUSE.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.P_REUSE.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.MAGIC_REUSE_RATE.ordinal())
			{
				base = target.getTemplate().getBaseMReuseRate();
			}
			else if(stats == Stats.SHIELD_DEFENCE.ordinal())
			{
				base = 0;
			}
			else if(stats == Stats.CRITICAL_DAMAGE.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.CRITICAL_DAMAGE_ADD.ordinal())
			{
				base = 0;
			}
			else if(stats == Stats.MAGIC_CRIT_DMG.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.MAGIC_CRIT_DMG_ADD.ordinal())
			{
				base = 0;
			}
			else if(stats == Stats.PVP_PHYSICAL_DMG.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.PVP_PHYSICAL_DMG_ADD.ordinal())
			{
				base = 0;
			}
			else if(stats == Stats.PVP_MAGICAL_DMG.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.PVP_PHYS_SKILL_DMG.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.PVP_PHYSICAL_DEF.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.PVP_MAGICAL_DEF.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.PVP_PHYS_SKILL_DEF.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.PVE_PHYSICAL_DMG.ordinal())
			{
				base = 1;
			}// PVE_PHYS_SKILL_DMG("pvePhysSkillsDmg"),		// PvE Физ. Урон Скиллами
			else if(stats == Stats.PVE_BOW_DMG.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.PVE_BOW_SKILL_DMG.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.PVE_MAGICAL_DMG.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.EVASION_PHYSICAL_RATE.ordinal())
			{
				base = 0;
			}
			else if(stats == Stats.EVASION_MAGICAL_RATE.ordinal())
			{
				base = 0;
			}
			else if(stats == Stats.P_SKILL_EVASION.ordinal())
			{
				base = 0;
			}
			else if(stats == Stats.CRIT_DAMAGE_EVASION.ordinal())
			{
				base = 100;
			}
			else if(stats == Stats.SHIELD_RATE.ordinal())
			{
				base = 0;
			}
			else if(stats == Stats.PCRITICAL_RATE.ordinal())
			{
				base = target.getTemplate().getBaseCritRate();
			}// BLOW_RATE("blowRate"),						// Шанс Смертельной Атаки
			// LETHAL_RATE("lethalRate"),					// Шанс Смертельной Атаки
			else if(stats == Stats.MCRITICAL_RATE.ordinal())
			{
				base = target.getTemplate().getBaseMCritRate();
			}
			else if(stats == Stats.EXPSP_RATE.ordinal())
			{
				base = 0;  // Зависит от опыта
			}
			else if(stats == Stats.BONUS_EXP.ordinal())
			{
				base = 0;
			}
			else if(stats == Stats.BONUS_SP.ordinal())
			{
				base = 0;
			}// ATTACK_CANCEL("cancel"),					// Шанс Отменить Физ. Атаку
			else if(stats == Stats.MAGIC_FAILURE_RATE.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.ACCURACY_PHYSICAL.ordinal())
			{
				base = 0;
			}
			else if(stats == Stats.ACCURACY_MAGICAL.ordinal())
			{
				base = 0;
			}
			else if(stats == Stats.POWER_ATTACK_RANGE.ordinal())
			{
				base = 66; // Зависит от скила
			}
			// MAGIC_ATTACK_RANGE("mAtkRange"),			// Радиус Маг. Атаки
			else if(stats == Stats.POWER_ATTACK_ANGLE.ordinal())
			{
				base = 120;
			}
			else if(stats == Stats.ATTACK_COUNT_MAX.ordinal())
			{
				base = 1;
			}
			else if(stats == Stats.RUN_SPEED.ordinal())
			{
				base = target.getTemplate().getBaseRunSpd();
			}
			else if(stats == Stats.WALK_SPEED.ordinal())
			{
				base = target.getTemplate().getBaseWalkSpd();
			}
			else if(stats == Stats.STAT_STR.ordinal())
			{
				base = target.getTemplate().getBaseSTR();
			}
			else if(stats == Stats.STAT_CON.ordinal())
			{
				base = target.getTemplate().getBaseCON();
			}
			else if(stats == Stats.STAT_DEX.ordinal())
			{
				base = target.getTemplate().getBaseDEX();
			}
			else if(stats == Stats.STAT_INT.ordinal())
			{
				base = target.getTemplate().getBaseINT();
			}
			else if(stats == Stats.STAT_WIT.ordinal())
			{
				base = target.getTemplate().getBaseWIT();
			}
			else if(stats == Stats.STAT_MEN.ordinal())
			{
				base = target.getTemplate().getBaseMEN();
			}

			html.append("<br><center><font color=\"LEVEL\">Стат: ").append(Stats.values()[stats].name()).append("</font><br1>");
			html.append("<center><font color=\"LEVEL\">База: ").append(base).append("</font></center>");

			html.append("<br><table width=\"100%\"><tr> " +
				"<td width=170>Обьект</td> " +
				"<td width=130>Значение<br></td></tr>");

			double total = base;
			// Вывод самих стат у калькулятора
			Calculator calc = target.getCalculators()[stats];
			if(calc != null)
			{
				for(Calculator.CalculatedStats stat : calc.getCalculatedStats(target, base))
				{
					if(stat != null)
					{
						String name = "null";
						if(stat._func.funcOwner instanceof L2ItemInstance)
						{
							name = ((L2ItemInstance) stat._func.funcOwner).getItem().toString();
						}
						else if(stat._func.funcOwner instanceof L2Skill)
						{
							name = stat._func.funcOwner.toString();
						}
						if(stat._func.funcOwner instanceof L2Effect)
						{
							name = stat._func.funcOwner.toString();
						}
						else if(stat._func.funcOwner != null)
						{
							name = stat._func.funcOwner.toString();
						}

						StringUtil.append(html, "<tr><td>", name, "</td><td>", String.valueOf(stat._vale), "<br></td></tr>");

						total += stat._vale;
					}
				}
			}
			html.append("</table>");
			html.append("<br><br><center><font color=\"LEVEL\">Общее: ").append(total).append("</font></center>");
		}

		html.append("</html>");
		NpcHtmlMessage ms = new NpcHtmlMessage(1);
		ms.setHtml(html.toString());
		activeChar.sendPacket(ms);
	}

	private class IpPack
	{
		final String ip;
		final int[][] tracert;

		public IpPack(String ip, int[][] tracert)
		{
			this.ip = ip;
			this.tracert = tracert;
		}

		@Override
		public int hashCode()
		{
			int prime = 31;
			int result = 1;
			result = prime * result + (ip == null ? 0 : ip.hashCode());
			for(int[] array : tracert)
			{
				result = prime * result + Arrays.hashCode(array);
			}
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
			{
				return true;
			}
			if(obj == null)
			{
				return false;
			}
			if(getClass() != obj.getClass())
			{
				return false;
			}
			IpPack other = (IpPack) obj;
			if(!getOuterType().equals(other.getOuterType()))
			{
				return false;
			}
			if(ip == null)
			{
				if(other.ip != null)
				{
					return false;
				}
			}
			else if(!ip.equals(other.ip))
			{
				return false;
			}
			for(int i = 0; i < tracert.length; i++)
			{
				for(int o = 0; o < tracert[0].length; o++)
				{
					if(tracert[i][o] != other.tracert[i][o])
					{
						return false;
					}
				}
			}
			return true;
		}

		private AdminEditChar getOuterType()
		{
			return AdminEditChar.this;
		}
	}
}
