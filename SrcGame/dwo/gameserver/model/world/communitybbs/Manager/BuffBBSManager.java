package dwo.gameserver.model.world.communitybbs.Manager;

import dwo.config.mods.ConfigCommunityBoardPVP;
import dwo.gameserver.datatables.xml.CommunityBuffTable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;
import javolution.text.TextBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.StringTokenizer;

public class BuffBBSManager
{
	private static BuffBBSManager _instance = new BuffBBSManager();
	private static Logger _log = LogManager.getLogger(BuffBBSManager.class);

	public static BuffBBSManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new BuffBBSManager();
		}
		return _instance;
	}

	public String parsecmd(String var, L2PcInstance activeChar, String content)
	{
		int priceRebuff;
		TextBuilder html = new TextBuilder("");
		L2Skill skill;
		String buffer_top;
		String buffer_bottom;
		String buffer_body = null;

		int idGroup = 0;
		String name = "";

		if(var != null)
		{
			if(var.startsWith("save"))
			{
				if(!activeChar.getFloodProtectors().getCommunityBoard().tryPerformAction(FloodAction.BBS_BUFF_SAVE))
				{
					activeChar.sendMessage("Данное действие нельзя выполнять несколько раз подряд!");
					return null;
				}
				activeChar.updateBBSBuff();
				activeChar.sendMessage("Набор Ваших Бафов сохранен.");
				return null;
			}
			else if(var.startsWith("load"))
			{
				if(!activeChar.getFloodProtectors().getCommunityBoard().tryPerformAction(FloodAction.BBS_BUFF_LOAD))
				{
					activeChar.sendMessage("Данное действие нельзя выполнять несколько раз подряд!");
					return null;
				}
				priceRebuff = activeChar.calcBBSBuff();
				if(priceRebuff > 0)
				{
					if(activeChar.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, priceRebuff, activeChar, true))
					{
						activeChar.castBBSBuff(false);
						if(activeChar.hasSummon())
						{
							activeChar.castBBSBuff(true);
						}
					}
				}
				return null;
			}
			/*
			else if (var.startsWith("heal"))
			{
				if (!activeChar.getFloodProtectors().getCommunityBoard().tryPerformAction("bbs_buff_heal"))
				{
					activeChar.sendMessage("Данное действие нельзя выполнять несколько раз подряд!");
					return null;
				}
				activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
				activeChar.setCurrentCp(activeChar.getMaxCp());
				return null;
			}
			*/
			else if(var.startsWith("cancel"))
			{
				if(!activeChar.getFloodProtectors().getCommunityBoard().tryPerformAction(FloodAction.BBS_BUFF_CANCEL))
				{
					activeChar.sendMessage("Данное действие нельзя выполнять несколько раз подряд!");
					return null;
				}

				// Снимаем с персонажа только положительные эффекты
				for(L2Effect buff : activeChar.getAllEffects())
				{
					if(buff != null && !buff.getSkill().isDebuff() && !buff.getSkill().isMentor() && !buff.getSkill().isToggle())
					{
						buff.exit();
					}
				}
				return null;
			}
			else if(var.startsWith("skill"))
			{
				StringTokenizer st = new StringTokenizer(var, "-");
				st.nextToken(); //_bbs_buff_skill
				int sGrp = Integer.parseInt(st.nextToken()); //1
				int sId = Integer.parseInt(st.nextToken());  //100
				int sLvl = Integer.parseInt(st.nextToken()); //1

				int price = CommunityBuffTable.getInstance().getPriceGroup(sGrp);
				if(!activeChar.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, price, activeChar, true))
				{
					return null;
				}

				skill = SkillTable.getInstance().getInfo(sId, sLvl);
				skill.getEffects(activeChar, activeChar, ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_BUFF_TIME_OVERRIDE);

				if(activeChar.hasSummon())
				{
					for(L2Summon pet : activeChar.getPets())
					{
						skill = SkillTable.getInstance().getInfo(sId, sLvl);
						skill.getEffects(activeChar, pet, ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_BUFF_TIME_OVERRIDE);
					}
				}
				return null;
			}
		}

		html.clear();
		html.append("<center>");
		html.append("<table>");
		html.append("<tr>");
		for(Map.Entry<Integer, CommunityBuffTable.BBSGroupBuffStat> entry : CommunityBuffTable.getInstance().getBBSGroups().entrySet())
		{
			idGroup = entry.getKey();
			name = entry.getValue().getName();
			html.append("<td>");
			html.append("<button value=\"").append(name).append("\" action=\"bypass -h _bbstop;buff:group-").append(String.valueOf(idGroup)).append("\" width=90 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			html.append("</td>");
		}
		html.append("</tr>");
		html.append("</table>");
		html.append("</center><br><br>");
		buffer_top = html.toString();

		html.clear();
		html.append("<center>");
		html.append("<table>");
		html.append("<tr>");
		html.append("<td><button value=\"Сохранить\" action=\"bypass -h _bbstop;buff:save\" width=90 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br1></td>");
		html.append("<td><button value=\"Востановить\" action=\"bypass -h _bbstop;buff:load\" width=90 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br1></td>");
		html.append("<td><button value=\"Канцел\" action=\"bypass -h _bbstop;buff:cancel\" width=90 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br1></td>");
		//html.append("<td><button value=\"hp/mp/cp\" action=\"bypass -h _bbstop;buff:heal\" width=90 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("</center>");
		buffer_bottom = html.toString();

		if(var != null && var.startsWith("group"))
		{
			StringTokenizer st = new StringTokenizer(var, "-");
			st.nextToken();
			int idGrp = Integer.parseInt(st.nextToken());

			if(idGrp == 0)
			{
				idGrp = 1;
			}

			int idSkill = 0;
			int lvlSkill = 0;
			int column = 0;
			String StringSkill = "";
			String skillIcon = "";
			String skillName = "";

			html.clear();
			html.append("<center>Стоимость бафа в данной группе: <font color=F2C202>").append(String.valueOf(CommunityBuffTable.getInstance().getPriceGroup(idGrp))).append("</font>.</center><br>");
			html.append("<table width=600>");
			html.append("<tr>");
			for(Map.Entry<Integer, Integer> entry : CommunityBuffTable.getInstance().getBBSBuffsForGoup(idGrp).entrySet())
			{
				column++;
				idSkill = entry.getKey();
				lvlSkill = entry.getValue();
				StringSkill = Integer.toString(idSkill);
				switch(StringSkill.length())
				{
					case 1:
						skillIcon = "icon.skill000" + idSkill;
						break;
					case 2:
						skillIcon = "icon.skill00" + idSkill;
						break;
					case 3:
						skillIcon = "icon.skill0" + idSkill;
						break;
					default:
						skillIcon = "icon.skill" + idSkill;
						break;
				}

				// Корейки накосячили с иконками
				if(idSkill == 11522)
				{
					skillIcon = "icon.skill11518";
				}
				if(idSkill == 11518)
				{
					skillIcon = "icon.skill11522";
				}
				if(idSkill == 11567)
				{
					skillIcon = "icon.skill11824";
				}
				if(idSkill == 11566)
				{
					skillIcon = "icon.skill11567";
				}

				L2Skill skillBuff = SkillTable.getInstance().getInfo(idSkill, 1);
				if(skillBuff == null)
				{
					_log.log(Level.WARN, "BuffBBSManager: skill id: " + idSkill + " not found");
					continue;
				}
				skillName = skillBuff.getName();
				html.append("<td width=150>");
				html.append("<center><img src=\"").append(skillIcon).append("\" width=32 height=32 align=center></center><br><center><a action=\"bypass -h _bbstop;buff:skill-").append(String.valueOf(idGrp)).append("-").append(String.valueOf(idSkill)).append("-").append(String.valueOf(lvlSkill)).append("\">").append(skillName).append("</a></center>");
				html.append("<br><br></td>");
				if(column == 4)
				{
					html.append("</tr>");
					html.append("<tr>");
					column = 0;
				}
			}
			html.append("</tr>");
			html.append("</table>");
			buffer_body = html.toString();

		}

		content = content.replace("%buffer_top%", buffer_top);
		content = content.replace("%buffer_bottom%", buffer_bottom);
		content = content.replace("%buffer_body%", buffer_body == null ? "" : buffer_body);
		return content;
	}
}