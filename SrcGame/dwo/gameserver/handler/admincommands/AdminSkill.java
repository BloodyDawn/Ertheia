package dwo.gameserver.handler.admincommands;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.ClassTemplateTable;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeSkillList;
import dwo.gameserver.util.StringUtil;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.StringTokenizer;

public class AdminSkill implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_show_skills", "admin_remove_skills", "admin_skill_list", "admin_skill_index", "admin_add_skill",
		"admin_remove_skill", "admin_get_skills", "admin_reset_skills", "admin_give_all_skills",
		"admin_give_all_clan_skills", "admin_remove_all_skills", "admin_add_clan_skill", "admin_setskill"
	};
	private static Logger _log = LogManager.getLogger(AdminSkill.class);
	private static L2Skill[] adminSkills;

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.equals("admin_show_skills"))
		{
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_remove_skills"))
		{
			try
			{
				String val = command.substring(20);
				removeSkillsPage(activeChar, Integer.parseInt(val));
			}
			catch(StringIndexOutOfBoundsException e)
			{
			}
		}
		else if(command.startsWith("admin_skill_list"))
		{
			AdminHelpPage.showHelpPage(activeChar, "skills.htm");
		}
		else if(command.startsWith("admin_skill_index"))
		{
			try
			{
				String val = command.substring(18);
				AdminHelpPage.showHelpPage(activeChar, "skills/" + val + ".htm");
			}
			catch(StringIndexOutOfBoundsException ignored)
			{
			}
		}
		else if(command.startsWith("admin_add_skill"))
		{
			try
			{
				String val = command.substring(15);
				adminAddSkill(activeChar, val);
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //add_skill <skill_id> <level>");
			}
		}
		else if(command.startsWith("admin_remove_skill"))
		{
			try
			{
				String id = command.substring(19);
				int idval = Integer.parseInt(id);
				adminRemoveSkill(activeChar, idval);
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //remove_skill <skill_id>");
			}
		}
		else if(command.equals("admin_get_skills"))
		{
			adminGetSkills(activeChar);
		}
		else if(command.equals("admin_reset_skills"))
		{
			adminResetSkills(activeChar);
		}
		else if(command.equals("admin_give_all_skills"))
		{
			adminGiveAllSkills(activeChar, false);
		}
		else if(command.equals("admin_give_all_clan_skills"))
		{
			adminGiveAllClanSkills(activeChar);
		}
		else if(command.equals("admin_remove_all_skills"))
		{
			L2Object target = activeChar.getTarget();
			if(target == null || !target.isPlayer())
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			else if(target.isPlayer())
			{
				L2PcInstance player = (L2PcInstance) activeChar.getTarget();
				player.getAllSkills().forEach(player::removeSkill);
				activeChar.sendMessage("You removed all skills from " + player.getName());
				player.sendMessage("Admin removed all skills from you.");
				player.sendSkillList();
				player.broadcastUserInfo();
			}
		}
		else if(command.startsWith("admin_add_clan_skill"))
		{
			try
			{
				String[] val = command.split(" ");
				adminAddClanSkill(activeChar, Integer.parseInt(val[1]), Integer.parseInt(val[2]));
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
			}
		}
		else if(command.startsWith("admin_setskill"))
		{
			String[] split = command.split(" ");
			int id = Integer.parseInt(split[1]);
			int lvl = Integer.parseInt(split[2]);
			L2Skill skill = SkillTable.getInstance().getInfo(id, lvl);
			activeChar.addSkill(skill);
			activeChar.sendSkillList();
			activeChar.sendMessage("You added yourself skill " + skill.getName() + '(' + id + ") level " + lvl);
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	/**
	 * This function will give all the skills that the target can learn at his/her level
	 *
	 * @param activeChar: the gm char
	 * @param includedByFs выдавать-ли также умения забытых свитков
	 */
	private void adminGiveAllSkills(L2PcInstance activeChar, boolean includedByFs)
	{
		L2Object target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		L2PcInstance player = target.getActingPlayer();
		//Notify player and admin
		player.giveAvailableAutoGetSkills();
		activeChar.sendMessage("You gave " + player.giveAvailableSkills(includedByFs, true) + " skills to " + player.getName());
		player.sendSkillList();
	}

	/**
	 * This function will give all the skills that the target's clan can learn at it's level.<br>
	 * If the target is not the clan leader, a system message will be sent to the Game Master.
	 * @param activeChar the active char, probably a Game Master.
	 */
	private void adminGiveAllClanSkills(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		L2PcInstance player = target.getActingPlayer();
		L2Clan clan = player.getClan();
		if(clan == null)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
			return;
		}

		if(!player.isClanLeader())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_MUST_BE_IN_CLAN).addPcName(player));
		}

		List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailablePledgeSkills(clan);
		SkillTable st = SkillTable.getInstance();
		for(L2SkillLearn s : skills)
		{
			clan.addNewSkill(st.getInfo(s.getSkillId(), s.getSkillLevel()));
		}

		// Notify target and active char
		clan.broadcastToOnlineMembers(new PledgeSkillList(clan));
		for(L2PcInstance member : clan.getOnlineMembers(0))
		{
			member.sendSkillList();
		}

		activeChar.sendMessage("You gave " + skills.size() + " skills to " + player.getName() + "'s clan " + clan.getName() + '.');
		player.sendMessage("Your clan received " + skills.size() + " skills.");
	}

	private void removeSkillsPage(L2PcInstance activeChar, int page)
	{
		L2Object target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}
		L2PcInstance player = target.getActingPlayer();
		L2Skill[] skills = player.getAllSkills().toArray(new L2Skill[player.getAllSkills().size()]);

		int maxSkillsPerPage = 10;
		int maxPages = skills.length / maxSkillsPerPage;
		if(skills.length > maxSkillsPerPage * maxPages)
		{
			maxPages++;
		}

		if(page > maxPages)
		{
			page = maxPages;
		}

		int skillsStart = maxSkillsPerPage * page;
		int skillsEnd = skills.length;
		if(skillsEnd - skillsStart > maxSkillsPerPage)
		{
			skillsEnd = skillsStart + maxSkillsPerPage;
		}

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuilder replyMSG = StringUtil.startAppend(500 + maxPages * 50 + (skillsEnd - skillsStart + 1) * 50, "<html><body>" +
			"<table width=260><tr>" +
			"<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" +
			"<td width=180><center>Character Selection Menu</center></td>" +
			"<td width=40><button value=\"Back\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" +
			"</tr></table>" +
			"<br><br>" +
			"<center>Editing <font color=\"LEVEL\">", player.getName(), "</font></center>" + "<br><table width=270><tr><td>Lv: ", String.valueOf(player.getLevel()), " ", ClassTemplateTable.getInstance().getClass(player.getClassId()).getClassName(), "</td></tr></table>" +
			"<br><table width=270><tr><td>Note: Dont forget that modifying players skills can</td></tr>" +
			"<tr><td>ruin the game...</td></tr></table>" +
			"<br><center>Click on the skill you wish to remove:</center>" +
			"<br>" +
			"<center><table width=270><tr>");

		for(int x = 0; x < maxPages; x++)
		{
			int pagenr = x + 1;
			StringUtil.append(replyMSG, "<td><a action=\"bypass -h admin_remove_skills ", String.valueOf(x), "\">Page ", String.valueOf(pagenr), "</a></td>");
		}

		replyMSG.append("</tr></table></center>" +
			"<br><table width=270>" +
			"<tr><td width=80>Name:</td><td width=60>Level:</td><td width=40>Id:</td></tr>");

		for(int i = skillsStart; i < skillsEnd; i++)
		{
			StringUtil.append(replyMSG, "<tr><td width=80><a action=\"bypass -h admin_remove_skill ", String.valueOf(skills[i].getId()), "\">", skills[i].getName(), "</a></td><td width=60>", String.valueOf(skills[i].getLevel()), "</td><td width=40>", String.valueOf(skills[i].getId()), "</td></tr>");
		}

		replyMSG.append("</table>" +
			"<br><center><table>" +
			"Remove skill by ID :" +
			"<tr><td>Id: </td>" +
			"<td><edit var=\"id_to_remove\" width=110></td></tr>" +
			"</table></center>" +
			"<center><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>" +
			"<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>" +
			"</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	/**
	 * @param activeChar the active Game Master.
	 */
	private void showMainPage(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		L2PcInstance player = target.getActingPlayer();
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "mods/admin/charskills.htm");
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%class%", ClassTemplateTable.getInstance().getClass(player.getClassId()).getClassServName());
		activeChar.sendPacket(adminReply);
	}

	/**
	 * @param activeChar the active Game Master.
	 */
	private void adminGetSkills(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		L2PcInstance player = target.getActingPlayer();
		if(player.getObjectId() == activeChar.getObjectId())
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
		}
		else
		{
			L2Skill[] skills = player.getAllSkills().toArray(new L2Skill[player.getAllSkills().size()]);
			adminSkills = activeChar.getAllSkills().toArray(new L2Skill[activeChar.getAllSkills().size()]);
			for(L2Skill skill : adminSkills)
			{
				activeChar.removeSkill(skill);
			}
			for(L2Skill skill : skills)
			{
				activeChar.addSkill(skill, true);
			}
			activeChar.sendMessage("You now have all the skills of " + player.getName() + '.');
			activeChar.sendSkillList();
		}
		showMainPage(activeChar);
	}

	/**
	 * @param activeChar the active Game Master.
	 */
	private void adminResetSkills(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		L2PcInstance player = target.getActingPlayer();
		if(adminSkills == null)
		{
			activeChar.sendMessage("You must get the skills of someone in order to do this.");
		}
		else
		{
			L2Skill[] skills = player.getAllSkills().toArray(new L2Skill[player.getAllSkills().size()]);
			for(L2Skill skill : skills)
			{
				player.removeSkill(skill);
			}
			for(L2Skill skill : activeChar.getAllSkills())
			{
				player.addSkill(skill, true);
			}
			for(L2Skill skill : skills)
			{
				activeChar.removeSkill(skill);
			}
			for(L2Skill skill : adminSkills)
			{
				activeChar.addSkill(skill, true);
			}
			player.sendMessage("[GM]" + activeChar.getName() + " updated your skills.");
			activeChar.sendMessage("You now have all your skills back.");
			adminSkills = null;
			activeChar.sendSkillList();
			player.sendSkillList();
		}
		showMainPage(activeChar);
	}

	/**
	 * @param activeChar the active Game Master.
	 * @param val
	 */
	private void adminAddSkill(L2PcInstance activeChar, String val)
	{
		L2Object target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		L2PcInstance player = target.getActingPlayer();
		StringTokenizer st = new StringTokenizer(val);
		if(st.countTokens() == 2)
		{
			L2Skill skill = null;
			try
			{
				String id = st.nextToken();
				String level = st.nextToken();
				int idval = Integer.parseInt(id);
				int levelval = Integer.parseInt(level);
				skill = SkillTable.getInstance().getInfo(idval, levelval);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
			if(skill != null)
			{
				String name = skill.getName();
				// Player's info.
				player.sendMessage("Admin gave you the skill " + name + '.');
				player.addSkill(skill, true);
				player.sendSkillList();
				// Admin info.
				activeChar.sendMessage("You gave the skill " + name + " to " + player.getName() + '.');
				activeChar.sendSkillList();
			}
			else
			{
				activeChar.sendMessage("Error: there is no such skill.");
			}
			showMainPage(activeChar); //Back to start
		}
		else
		{
			showMainPage(activeChar);
		}
	}

	/**
	 * @param activeChar the active Game Master.
	 * @param idval
	 */
	private void adminRemoveSkill(L2PcInstance activeChar, int idval)
	{
		L2Object target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		L2PcInstance player = target.getActingPlayer();
		L2Skill skill = SkillTable.getInstance().getInfo(idval, player.getSkillLevel(idval));
		if(skill != null)
		{
			String skillname = skill.getName();
			player.sendMessage("Admin removed the skill " + skillname + " from your skills list.");
			player.removeSkill(skill);
			//Admin information
			activeChar.sendMessage("You removed the skill " + skillname + " from " + player.getName() + '.');
			if(Config.DEBUG)
			{
				_log.log(Level.INFO, "[GM]" + activeChar.getName() + " removed skill " + skillname + " from " + player.getName() + '.');
			}
			activeChar.sendSkillList();
		}
		else
		{
			activeChar.sendMessage("Error: there is no such skill.");
		}
		removeSkillsPage(activeChar, 0); //Back to previous page
	}

	/**
	 * @param activeChar the active Game Master.
	 * @param id
	 * @param level
	 */
	private void adminAddClanSkill(L2PcInstance activeChar, int id, int level)
	{
		L2Object target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			showMainPage(activeChar);
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		L2PcInstance player = target.getActingPlayer();
		if(!player.isClanLeader())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addString(player.getName()));
			showMainPage(activeChar);
			return;
		}
		if(id < 370 || id > 391 || level < 1 || level > 3)
		{
			activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
			showMainPage(activeChar);
		}
		else
		{
			L2Skill skill = SkillTable.getInstance().getInfo(id, level);
			if(skill != null)
			{
				L2Clan clan = player.getClan();
				clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED).addString(skill.getName()));
				clan.addNewSkill(skill);
				activeChar.sendMessage("You gave the Clan Skill: " + skill.getName() + " to the clan " + clan.getName() + '.');

				clan.broadcastToOnlineMembers(new PledgeSkillList(clan));

				for(L2PcInstance member : clan.getOnlineMembers(0))
				{
					member.sendSkillList();
				}

				showMainPage(activeChar);
			}
			else
			{
				activeChar.sendMessage("Error: there is no such skill.");
			}
		}
	}
}
