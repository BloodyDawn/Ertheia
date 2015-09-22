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
package dwo.gameserver.handler.admincommands;

import dwo.config.Config;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.handler.actions.L2NpcActionShift;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.npc.drop.L2DropCategory;
import dwo.gameserver.model.world.npc.drop.L2DropData;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowLines;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author terry
 */
public class AdminEditNpc implements IAdminCommandHandler
{
	private static final int PAGE_LIMIT = 20;
	private static final NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
	private static final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);

	static
	{
		pf.setMaximumFractionDigits(4);
		df.setMinimumFractionDigits(2);
	}

	private static final String[] ADMIN_COMMANDS = {
		"admin_show_npc", "admin_edit_npc", "admin_save_npc", "admin_show_droplist", "admin_close_window",
		"admin_show_skilllist_npc", "admin_add_skill_npc", "admin_edit_skill_npc", "admin_del_skill_npc",
		"admin_log_npc_spawn", "admin_showNpcList"
	};
	private static Logger _log = LogManager.getLogger(AdminEditNpc.class);

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.startsWith("admin_log_npc_spawn"))
		{
			L2Object target = activeChar.getTarget();
			if(target instanceof L2Npc)
			{
				L2Npc npc = (L2Npc) target;
				_log.log(Level.INFO, "('',1," + npc.getNpcId() + ',' + npc.getX() + ',' + npc.getY() + ',' + npc.getZ() + ",0,0," + npc.getHeading() + ",60,0,0),");
			}
		}
		else if(command.startsWith("admin_showNpcList "))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			activeChar.sendPacket(new ExShowLines(activeChar));
			/*
			try
            {
                int radius = Integer.parseInt(st.nextToken());
                int page = 1;
                if (st.hasMoreTokens())
                    page = Integer.parseInt(st.nextToken());
                showRadiusNpc(activeChar, radius, page);
            }
            catch (Exception e)
            {
                activeChar.sendMessage("Usage: //showNpcList <radius> [<page>]");
            }
            */

		}
		else if(command.startsWith("admin_show_npc "))
		{
			try
			{
				new L2NpcActionShift().action(activeChar, activeChar.getTarget(), false);
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Wrong usage of NPC Info.");
			}
		}
		else if(command.startsWith("admin_edit_npc "))
		{
			try
			{
				if(activeChar.getTarget() != null && activeChar.getTarget() instanceof L2Npc)
				{
					showNpcProperty(activeChar, (L2Npc) activeChar.getTarget());
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Wrong usage: //edit_npc <npcId>");
			}
		}
		else if(command.startsWith("admin_show_droplist "))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			try
			{
				int npcId = Integer.parseInt(st.nextToken());
				int page = 1;
				if(st.hasMoreTokens())
				{
					page = Integer.parseInt(st.nextToken());
				}
				showNpcDropList(activeChar, npcId, page);
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //show_droplist <npc_id> [<page>]");
				_log.log(Level.ERROR, e); // Временно, узнать причину почему не работает
			}
		}
		else if(command.startsWith("admin_save_npc"))
		{
			try
			{
				saveNpcProperty(activeChar, command);
			}
			catch(StringIndexOutOfBoundsException ignored)
			{
			}
		}
		else if(command.startsWith("admin_show_skilllist_npc"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			try
			{
				int npcId = Integer.parseInt(st.nextToken());
				int page = 0;
				if(st.hasMoreTokens())
				{
					page = Integer.parseInt(st.nextToken());
				}
				showNpcSkillList(activeChar, npcId, page);
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //show_skilllist_npc <npc_id> <page>");
				_log.log(Level.ERROR, e);
			}
		}
		else if(command.startsWith("admin_edit_skill_npc "))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int npcId = Integer.parseInt(st.nextToken());
				int skillId = Integer.parseInt(st.nextToken());
				if(st.hasMoreTokens())
				{
					int level = Integer.parseInt(st.nextToken());
					updateNpcSkillData(activeChar, npcId, skillId, level);
				}
				else
				{
					showNpcSkillEdit(activeChar, npcId, skillId);
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //edit_skill_npc <npc_id> <item_id> [<level>]");
			}
		}
		else if(command.startsWith("admin_add_skill_npc "))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int npcId = Integer.parseInt(st.nextToken());
				if(st.hasMoreTokens())
				{
					int skillId = Integer.parseInt(st.nextToken());
					int level = Integer.parseInt(st.nextToken());
					addNpcSkillData(activeChar, npcId, skillId, level);
				}
				else
				{
					showNpcSkillAdd(activeChar, npcId);
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //add_skill_npc <npc_id> [<skill_id> <level>]");
			}
		}
		else if(command.startsWith("admin_del_skill_npc "))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int npcId = Integer.parseInt(st.nextToken());
				int skillId = Integer.parseInt(st.nextToken());
				deleteNpcSkillData(activeChar, npcId, skillId);
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //del_skill_npc <npc_id> <skill_id>");
			}
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void showNpcProperty(L2PcInstance activeChar, L2Npc npc)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		L2NpcTemplate template = npc.getTemplate();
		String content = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/admin/editnpc.htm");

		if(content != null)
		{
			adminReply.setHtml(content);
			adminReply.replace("%npcId%", String.valueOf(template.getNpcId()));
			adminReply.replace("%templateId%", String.valueOf(template.getIdTemplate()));
			adminReply.replace("%name%", template.getName());
			adminReply.replace("%serverSideName%", template.isServerSideName() ? "1" : "0");
			adminReply.replace("%title%", template.getTitle());
			adminReply.replace("%serverSideTitle%", template.isServerSideTitle() ? "1" : "0");
			adminReply.replace("%collisionRadius%", String.valueOf(template.getFCollisionRadius(npc)));
			adminReply.replace("%collisionHeight%", String.valueOf(template.getFCollisionHeight(npc)));
			adminReply.replace("%level%", String.valueOf(template.getLevel()));
			adminReply.replace("%sex%", String.valueOf(template.getSex()));
			adminReply.replace("%type%", String.valueOf(template.getType()));
			adminReply.replace("%attackRange%", String.valueOf(template.getBaseAtkRange()));
			adminReply.replace("%hp%", String.valueOf(template.getBaseHpMax()));
			adminReply.replace("%mp%", String.valueOf(template.getBaseMpMax()));
			adminReply.replace("%hpRegen%", String.valueOf(template.getBaseHpReg()));
			adminReply.replace("%mpRegen%", String.valueOf(template.getBaseMpReg()));
			adminReply.replace("%str%", String.valueOf(template.getBaseSTR()));
			adminReply.replace("%con%", String.valueOf(template.getBaseCON()));
			adminReply.replace("%dex%", String.valueOf(template.getBaseDEX()));
			adminReply.replace("%int%", String.valueOf(template.getBaseINT()));
			adminReply.replace("%wit%", String.valueOf(template.getBaseWIT()));
			adminReply.replace("%men%", String.valueOf(template.getBaseMEN()));
			adminReply.replace("%exp%", String.valueOf(template.getRewardExp()));
			adminReply.replace("%sp%", String.valueOf(template.getRewardSp()));
			adminReply.replace("%pAtk%", String.valueOf(template.getBasePAtk()));
			adminReply.replace("%pDef%", String.valueOf(template.getBasePDef()));
			adminReply.replace("%mAtk%", String.valueOf(template.getBaseMAtk()));
			adminReply.replace("%mDef%", String.valueOf(template.getBaseMDef()));
			adminReply.replace("%pAtkSpd%", String.valueOf(template.getBasePAtkSpd()));
			adminReply.replace("%aggro%", String.valueOf(template.getAggroRange()));
			adminReply.replace("%mAtkSpd%", String.valueOf(template.getBaseMAtkSpd()));
			adminReply.replace("%rHand%", String.valueOf(template.getRightHand()));
			adminReply.replace("%lHand%", String.valueOf(template.getLeftHand()));
			adminReply.replace("%enchant%", String.valueOf(template.getEnchantEffect()));
			adminReply.replace("%walkSpd%", String.valueOf(template.getBaseWalkSpd()));
			adminReply.replace("%runSpd%", String.valueOf(template.getBaseRunSpd()));
			adminReply.replace("%factionId%", template.getClan() == null ? "" : template.getClan());
			adminReply.replace("%factionRange%", String.valueOf(template.getClanRange()));
			adminReply.replace("%displayEffect%", String.valueOf(npc.getDisplayEffect()));
		}
		else
		{
			adminReply.setHtml("<html><head><body>File not found: admin/editnpc.htm</body></html>");
		}
		activeChar.sendPacket(adminReply);
	}

	private void saveNpcProperty(L2PcInstance activeChar, String command)
	{
		String[] commandSplit = command.split(" ");

		if(commandSplit.length < 4)
		{
			return;
		}

		StatsSet newNpcData = new StatsSet();

		L2Npc npc = null;
		if(activeChar.getTarget() != null && activeChar.getTarget() instanceof L2Npc)
		{
			npc = activeChar.getTarget().getNpcInstance();
		}

		try
		{
			newNpcData.set("npcId", commandSplit[1]);

			String statToSet = commandSplit[2];
			String value = commandSplit[3];

			if(commandSplit.length > 4)
			{
				for(int i = 0; i < commandSplit.length - 3; i++)
				{
					value += ' ' + commandSplit[i + 4];
				}
			}

			int intVal = Integer.parseInt(value);

			switch(statToSet)
			{
				case "templateId":
					newNpcData.set("idTemplate", intVal);
					break;
				case "name":
					newNpcData.set("name", value);
					break;
				case "server_side_name":
					newNpcData.set("server_side_name", intVal);
					break;
				case "title":
					newNpcData.set("title", value);
					break;
				case "server_side_title":
					newNpcData.set("server_side_title", intVal == 1 ? 1 : 0);
					break;
				case "collisionRadius":
					newNpcData.set("collision_radius", intVal);
					break;
				case "collisionHeight":
					newNpcData.set("collision_height", intVal);
					break;
				case "level":
					newNpcData.set("level", intVal);
					break;
				case "sex":
					int intValue = intVal;
					newNpcData.set("sex", intValue == 0 ? "male" : intValue == 1 ? "female" : "etc");
					break;
				case "type":
					Class.forName("dwo.gameserver.model.actor.instance." + value + "Instance");
					newNpcData.set("type", value);
					break;
				case "base_attack_range":
					newNpcData.set("base_attack_range", intVal);
					break;
				case "hp":
					newNpcData.set("hp", intVal);
					break;
				case "mp":
					newNpcData.set("mp", intVal);
					break;
				case "hpRegen":
					newNpcData.set("hpreg", intVal);
					break;
				case "mpRegen":
					newNpcData.set("mpreg", intVal);
					break;
				case "str":
					newNpcData.set("str", intVal);
					break;
				case "con":
					newNpcData.set("con", intVal);
					break;
				case "dex":
					newNpcData.set("dex", intVal);
					break;
				case "int":
					newNpcData.set("int", intVal);
					break;
				case "wit":
					newNpcData.set("wit", intVal);
					break;
				case "men":
					newNpcData.set("men", intVal);
					break;
				case "exp":
					newNpcData.set("exp", intVal);
					break;
				case "sp":
					newNpcData.set("sp", intVal);
					break;
				case "pAtk":
				case "pDef":
				case "mAtk":
				case "mDef":
				case "pAtkSpd":
				case "mAtkSpd":
				case "displayEffect":
					switch(statToSet)
					{
						case "pAtk":
							newNpcData.set("patk", intVal);
							if(npc != null)
							{
								npc.getTemplate().setBasePAtk(intVal);
							}
							break;
						case "pDef":
							newNpcData.set("pdef", intVal);
							if(npc != null)
							{
								npc.getTemplate().setBasePDef(intVal);
							}
							break;
						case "mAtk":
							newNpcData.set("matk", intVal);
							if(npc != null)
							{
								npc.getTemplate().setBaseMatk(intVal);
							}
							break;
						case "mDef":
							newNpcData.set("mdef", intVal);
							if(npc != null)
							{
								npc.getTemplate().setBaseMDef(intVal);
							}
							break;
						case "pAtkSpd":
							newNpcData.set("atkspd", intVal);
							if(npc != null)
							{
								npc.getTemplate().setBasePAtkSpd(intVal);
							}
							break;
						case "mAtkSpd":
							newNpcData.set("matkspd", intVal);
							if(npc != null)
							{
								npc.getTemplate().setBaseMAtkSpd(intVal);
							}
							break;
						case "displayEffect":
							if(npc != null)
							{
								npc.setDisplayEffect(intVal);
							}
							break;
					}
					if(npc != null)
					{
						npc.initCharStat();
					}
					break;
				case "aggro":
					newNpcData.set("aggro", intVal);
					break;
				case "slot_rhand":
					newNpcData.set("slot_rhand", intVal);
					break;
				case "slot_lhand":
					newNpcData.set("slot_lhand", intVal);
					break;
				case "armor":
					newNpcData.set("armor", intVal);
					break;
				case "enchant":
					newNpcData.set("enchant", intVal);
					break;
				case "runSpd":
					newNpcData.set("runspd", intVal);
					break;
				case "isUndead":
					newNpcData.set("isUndead", intVal == 1 ? 1 : 0);
					break;
				case "absorbLevel":
					newNpcData.set("absorb_level", intVal < 0 ? 0 : intVal > 16 ? 0 : intVal);
					break;
			}
		}
		catch(Exception e)
		{
			activeChar.sendMessage("Could not save npc property!");
			_log.log(Level.ERROR, "Error saving new npc value (" + command + "): " + e);
		}

		showNpcProperty(activeChar, npc);
	}

	private void showNpcDropList(L2PcInstance activeChar, int npcId, int page)
	{
		L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);

		if(npcData == null)
		{
			activeChar.sendMessage("Unknown npc template id " + npcId);
			return;
		}

		if(npcData.getDropData() != null)
		{
			L2Object target = activeChar.getTarget();
			if(target == null)
			{
				activeChar.sendMessage("Нету таргета для " + npcId);
				return;
			}
			if(target instanceof L2Npc)
			{
				L2Npc npc = (L2Npc) target;
				//===============================================
				// Штраф за уровень
				int deepBlueDrop = 1;
				int levelModifier = npc.calculateLevelModifierForDrop(activeChar.isInParty() ? activeChar.getParty().getLevel() : activeChar.getLevel());

				StringBuilder builder = new StringBuilder(100);
				builder.append("<html><body><center>");
				builder.append("<table width=270 border=0>");
				builder.append("<tr><td align=\"center\"><font color=\"LEVEL\">Дроп</font></td></tr>");
				builder.append("<tr><td><img src=\"L2UI.SquareWhite\" width=270 height=1> </td></tr>");
				builder.append("<tr><td><img src=\"L2UI.SquareBlank\" width=270 height=10> </td></tr>");

				for(L2DropCategory cat : npcData.getDropData())
				{
					List<L2DropData> items = cat.getItems();
					double categoryDropChance = cat.getCategoryChance();
					if(!(npc.isRaid() && Config.DEEPBLUE_DROP_RULES) || npc.isRaid() && Config.DEEPBLUE_DROP_RULES_RAID)
					{
						// We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
						// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
						if(levelModifier > 0)
						{
							deepBlueDrop = 3;
						}
					}

					// Avoid dividing by 0
					if(deepBlueDrop == 0)
					{
						deepBlueDrop = 1;
					}

					// Check if we should apply our maths so deep blue mobs will not drop that easy
					if(!npc.isRaid() && Config.DEEPBLUE_DROP_RULES || npc.isRaid() && Config.DEEPBLUE_DROP_RULES_RAID)
					{
						categoryDropChance = (categoryDropChance - categoryDropChance * levelModifier / 100) / deepBlueDrop;
					}

					// Applies Drop rates
					categoryDropChance *= npc.isRaid() && !npc.isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;

					// Применяем рейт ПА, если убивший монстра имеет ПА
					if(Config.PREMIUM_ENABLED && activeChar != null && activeChar.isPremiumState() && !npc.isRaid())
					{
						categoryDropChance *= Config.PREMIUM_DROP_ITEM_RATE;
					}

					if(npc.getChampion() > 0)
					{
						categoryDropChance *= Config.CHAMPION_REWARDS * npc.getChampion();
					}

					// Set our limits for chance of drop
					if(categoryDropChance < 1)
					{
						categoryDropChance = 1;
					}

					if(categoryDropChance > L2DropData.MAX_CHANCE)
					{
						categoryDropChance = L2DropData.MAX_CHANCE;
					}

					builder.append("<tr><td><img src=\"L2UI.SquareBlank\" width=270 height=10> </td></tr>");
					builder.append("<tr><td>");
					builder.append("<table width=270 border=0 bgcolor=333333>");
					if(cat.isSweep())
					{
						builder.append("<tr><td align=\"center\"><font color=\"LEVEL\">Споил</font></td></tr>");
						builder.append("<tr><td><img src=\"L2UI.SquareWhite\" width=270 height=1> </td></tr>");
						builder.append("<tr><td><img src=\"L2UI.SquareBlank\" width=270 height=10> </td></tr>");
					}
					else
					{
						builder.append("<tr><td width=170><font color=\"a2a0a2\">Group Chance: </font><font color=\"b09979\">").append(pf.format(categoryDropChance / L2DropData.MAX_CHANCE)).append("</font></td>");
						builder.append("<td width=100 align=right>");
						builder.append("</td></tr>");
					}
					builder.append("</table>").append("</td></tr>");

					builder.append("<tr><td><table>");
					for(L2DropData drop : items)
					{
						double dropChance = drop.getChance();

						float multiplier = 1;
						// Applies Drop rates
						if(Config.RATE_DROP_ITEMS_ID.get(drop.getItemId()) != 0)
						{
							multiplier = Config.RATE_DROP_ITEMS_ID.get(drop.getItemId());
						}
						else if(cat.isSweep())
						{
							multiplier = Config.RATE_DROP_SPOIL;
						}
						else
						{
							multiplier = npc.isRaid() && !npc.isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
						}

						// Для мобов чемпионов
						if(npc.getChampion() > 0)
						{
							multiplier = Config.CHAMPION_REWARDS * npc.getChampion();
						}

						// Если последний атакующий имеет ПА применяем его рейт
						if(Config.PREMIUM_ENABLED && activeChar != null && activeChar.isPremiumState() && !npc.isRaid())
						{
							multiplier *= Config.PREMIUM_DROP_ITEM_RATE;
						}

						// Получаем мин и макс количество предметов
						int min = 0;
						int max = 0;

						if(cat.isSweep())
						{
							dropChance *= multiplier;
							int rate = (int) (dropChance / L2DropData.MAX_CHANCE);
							min = Math.round(drop.getMinDrop() * (rate > 0 ? rate : 1));
							max = Math.round(drop.getMaxDrop() * (rate > 0 ? rate : 1));
						}
						else
						{
							min = Math.round(drop.getMinDrop() * (drop.getItemId() == PcInventory.ADENA_ID ? (int) multiplier : 1));
							max = Math.round(drop.getMaxDrop() * (int) multiplier);
						}

						L2Item item = ItemTable.getInstance().getTemplate(drop.getItemId());

						if(dropChance > L2DropData.MAX_CHANCE)
						{
							dropChance = L2DropData.MAX_CHANCE;
						}

						// Хардкод
						if(!item.isStackable())
						{
							min = 1;
							max = 1;
						}
						String icon = item.getIcon();
						if(icon == null || icon.isEmpty())
						{
							icon = "icon.etc_question_mark_i00";
						}
						builder.append("<tr><td width=32><img src=").append(icon).append(" width=32 height=32></td><td width=238>").append(item.getName()).append("<br1>");
						builder.append("<font color=\"b09979\">[").append(min).append("..").append(max).append("]&nbsp;");
						builder.append(pf.format(dropChance / L2DropData.MAX_CHANCE)).append("</font></td></tr>");
					}
					builder.append("</table></td></tr>");
				}
				builder.append("</table>");
				builder.append("</center></body></html>");
				NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setHtml(builder.toString());
				activeChar.sendPacket(html);
			}
		}
	}

	private void showNpcSkillList(L2PcInstance activeChar, int npcId, int page)
	{
		L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
		if(npcData == null)
		{
			activeChar.sendMessage("Template id unknown: " + npcId);
			return;
		}

		Map<Integer, L2Skill> skills = new HashMap<>();
		if(npcData.getSkills() != null)
		{
			skills = npcData.getSkills();
		}

		int _skillsize = skills.size();

		int MaxSkillsPerPage = PAGE_LIMIT;
		int MaxPages = _skillsize / MaxSkillsPerPage;
		if(_skillsize > MaxSkillsPerPage * MaxPages)
		{
			MaxPages++;
		}

		if(page > MaxPages)
		{
			page = MaxPages;
		}

		int SkillsStart = MaxSkillsPerPage * page;
		int SkillsEnd = _skillsize;
		if(SkillsEnd - SkillsStart > MaxSkillsPerPage)
		{
			SkillsEnd = SkillsStart + MaxSkillsPerPage;
		}

		StringBuilder replyMSG = new StringBuilder("<html><title>Show NPC Skill List</title><body><center><font color=\"LEVEL\">");
		replyMSG.append(npcData.getName());
		replyMSG.append(" (");
		replyMSG.append(npcData.getNpcId());
		replyMSG.append("): ");
		replyMSG.append(_skillsize);
		replyMSG.append(" skills</font></center><table width=300 bgcolor=666666><tr>");

		for(int x = 0; x < MaxPages; x++)
		{
			int pagenr = x + 1;
			if(page == x)
			{
				replyMSG.append("<td>Page ");
				replyMSG.append(pagenr);
				replyMSG.append("</td>");
			}
			else
			{
				replyMSG.append("<td><a action=\"bypass -h admin_show_skilllist_npc ");
				replyMSG.append(npcData.getNpcId());
				replyMSG.append(' ');
				replyMSG.append(x);
				replyMSG.append("\"> Page ");
				replyMSG.append(pagenr);
				replyMSG.append(" </a></td>");
			}
		}
		replyMSG.append("</tr></table><table width=\"100%\" border=0><tr><td>Skill name [skill id-skill lvl]</td><td>Delete</td></tr>");
		Iterator<L2Skill> skillite = skills.values().iterator();

		for(int i = 0; i < SkillsStart; i++)
		{
			if(skillite.hasNext())
			{
				skillite.next();
			}
		}

		int cnt = SkillsStart;
		L2Skill sk;
		while(skillite.hasNext())
		{
			cnt++;
			if(cnt > SkillsEnd)
			{
				break;
			}

			sk = skillite.next();
			replyMSG.append("<tr><td width=240><a action=\"bypass -h admin_edit_skill_npc ");
			replyMSG.append(npcData.getNpcId());
			replyMSG.append(' ');
			replyMSG.append(sk.getId());
			replyMSG.append("\">");
			if(sk.getSkillType() == L2SkillType.NOTDONE)
			{
				replyMSG.append("<font color=\"777777\">").append(sk.getName()).append("</font>");
			}
			else
			{
				replyMSG.append(sk.getName());
			}
			replyMSG.append(" [");
			replyMSG.append(sk.getId());
			replyMSG.append('-');
			replyMSG.append(sk.getLevel());
			replyMSG.append("]</a></td><td width=60><a action=\"bypass -h admin_del_skill_npc ");
			replyMSG.append(npcData.getNpcId());
			replyMSG.append(' ');
			replyMSG.append(sk.getId());
			replyMSG.append("\">Delete</a></td></tr>");
		}
		replyMSG.append("</table><br><center><button value=\"Add Skill\" action=\"bypass -h admin_add_skill_npc ");
		replyMSG.append(npcId);
		replyMSG.append("\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Close\" action=\"bypass -h admin_close_window\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void showNpcSkillEdit(L2PcInstance activeChar, int npcId, int skillId)
	{
		try
		{
			StringBuilder replyMSG = new StringBuilder("<html><title>NPC Skill Edit</title><body>");

			L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
			if(npcData == null)
			{
				activeChar.sendMessage("Template id unknown: " + npcId);
				return;
			}
			if(npcData.getSkills() == null)
			{
				return;
			}

			L2Skill npcSkill = npcData.getSkills().get(skillId);

			if(npcSkill != null)
			{
				replyMSG.append("<table width=\"100%\"><tr><td>NPC: </td><td>");
				replyMSG.append(NpcTable.getInstance().getTemplate(npcId).getName());
				replyMSG.append(" (");
				replyMSG.append(npcId);
				replyMSG.append(")</td></tr><tr><td>Skill: </td><td>");
				replyMSG.append(npcSkill.getName());
				replyMSG.append(" (");
				replyMSG.append(skillId);
				replyMSG.append(")</td></tr><tr><td>Skill Lvl: (");
				replyMSG.append(npcSkill.getLevel());
				replyMSG.append(") </td><td><edit var=\"level\" width=50></td></tr></table><br><center><button value=\"Save\" action=\"bypass -h admin_edit_skill_npc ");
				replyMSG.append(npcId);
				replyMSG.append(' ');
				replyMSG.append(skillId);
				replyMSG.append(" $level\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1><button value=\"Back to SkillList\" action=\"bypass -h admin_show_skilllist_npc ");
				replyMSG.append(npcId);
				replyMSG.append("\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>");
			}

			replyMSG.append("</body></html>");

			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
		}
		catch(Exception e)
		{
			activeChar.sendMessage("Could not edit npc skills!");
			_log.log(Level.WARN, "Error while editing npc skills (" + npcId + ", " + skillId + "): " + e);
		}
	}

	private void updateNpcSkillData(L2PcInstance activeChar, int npcId, int skillId, int level)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		FiltredPreparedStatement statement2 = null;
		try
		{
			L2Skill skillData = SkillTable.getInstance().getInfo(skillId, level);
			if(skillData == null)
			{
				activeChar.sendMessage("Could not update npc skill: not existing skill id with that level!");
				showNpcSkillEdit(activeChar, npcId, skillId);
				return;
			}

			if(skillData.getLevel() != level)
			{
				activeChar.sendMessage("Skill id with requested level doesn't exist! Skill level not changed.");
				showNpcSkillEdit(activeChar, npcId, skillId);
				return;
			}

			con = L2DatabaseFactory.getInstance().getConnection();
			int updated = 0;
			if(Config.CUSTOM_NPC_SKILLS_TABLE)
			{
				statement2 = con.prepareStatement("UPDATE `custom_npcskills` SET `level`=? WHERE `npcid`=? AND `skillid`=?");
				statement2.setInt(1, level);
				statement2.setInt(2, npcId);
				statement2.setInt(3, skillId);

				updated = statement2.executeUpdate();
				DatabaseUtils.closeStatement(statement2);
			}
			if(updated == 0)
			{
				statement = con.prepareStatement("UPDATE `npcskills` SET `level`=? WHERE `npcid`=? AND `skillid`=?");
				statement.setInt(1, level);
				statement.setInt(2, npcId);
				statement.setInt(3, skillId);

				statement.execute();
				DatabaseUtils.closeStatement(statement);
			}
			reloadNpcSkillList(npcId);

			showNpcSkillList(activeChar, npcId, 0);
			activeChar.sendMessage("Updated skill id " + skillId + " for npc id " + npcId + " to level " + level + '.');
		}
		catch(Exception e)
		{
			activeChar.sendMessage("Could not update npc skill!");
			_log.log(Level.WARN, "Error while updating npc skill (" + npcId + ", " + skillId + ", " + level + "): " + e);
		}
		finally
		{
			DatabaseUtils.closeConnection(con);
		}
	}

	private void showNpcSkillAdd(L2PcInstance activeChar, int npcId)
	{
		L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);

		StringBuilder replyMSG = new StringBuilder("<html><title>NPC Skill Add</title><body><table width=\"100%\"><tr><td>NPC: </td><td>");
		replyMSG.append(npcData.getName());
		replyMSG.append(" (");
		replyMSG.append(npcData.getNpcId());
		replyMSG.append(")</td></tr><tr><td>SkillId: </td><td><edit var=\"skillId\" width=80></td></tr><tr><td>Level: </td><td><edit var=\"level\" width=80></td></tr></table><br><center><button value=\"Add Skill\" action=\"bypass -h admin_add_skill_npc ");
		replyMSG.append(npcData.getNpcId());
		replyMSG.append(" $skillId $level\"  width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1><button value=\"Back to SkillList\" action=\"bypass -h admin_show_skilllist_npc ");
		replyMSG.append(npcData.getNpcId());
		replyMSG.append("\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void addNpcSkillData(L2PcInstance activeChar, int npcId, int skillId, int level)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			// skill check
			L2Skill skillData = SkillTable.getInstance().getInfo(skillId, level);
			if(skillData == null)
			{
				activeChar.sendMessage("Could not add npc skill: not existing skill id with that level!");
				showNpcSkillAdd(activeChar, npcId);
				return;
			}

			con = L2DatabaseFactory.getInstance().getConnection();

			if(Config.CUSTOM_NPC_SKILLS_TABLE)
			{
				statement = con.prepareStatement("INSERT INTO `custom_npcskills`(`npcid`, `skillid`, `level`) VALUES(?,?,?)");
				statement.setInt(1, npcId);
				statement.setInt(2, skillId);
				statement.setInt(3, level);
				statement.execute();
			}
			else
			{
				statement = con.prepareStatement("INSERT INTO `npcskills`(`npcid`, `skillid`, `level`) VALUES(?,?,?)");
				statement.setInt(1, npcId);
				statement.setInt(2, skillId);
				statement.setInt(3, level);
				statement.execute();
			}

			reloadNpcSkillList(npcId);

			showNpcSkillList(activeChar, npcId, 0);
			activeChar.sendMessage("Added skill " + skillId + '-' + level + " to npc id " + npcId + '.');
		}
		catch(Exception e)
		{
			activeChar.sendMessage("Could not add npc skill!");
			_log.log(Level.WARN, "Error while adding a npc skill (" + npcId + ", " + skillId + ", " + level + "): " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void deleteNpcSkillData(L2PcInstance activeChar, int npcId, int skillId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		FiltredPreparedStatement statement2 = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			if(npcId > 0)
			{
				int updated = 0;
				if(Config.CUSTOM_NPC_SKILLS_TABLE)
				{
					statement = con.prepareStatement("DELETE FROM `custom_npcskills` WHERE `npcid`=? AND `skillid`=?");
					statement.setInt(1, npcId);
					statement.setInt(2, skillId);
					updated = statement.executeUpdate();
					DatabaseUtils.closeStatement(statement);
				}
				if(updated == 0)
				{
					statement2 = con.prepareStatement("DELETE FROM `npcskills` WHERE `npcid`=? AND `skillid`=?");
					statement2.setInt(1, npcId);
					statement2.setInt(2, skillId);
					statement2.execute();
					DatabaseUtils.closeStatement(statement2);
				}

				reloadNpcSkillList(npcId);

				showNpcSkillList(activeChar, npcId, 0);
				activeChar.sendMessage("Deleted skill id " + skillId + " from npc id " + npcId + '.');
			}
		}
		catch(Exception e)
		{
			activeChar.sendMessage("Could not delete npc skill!");
			_log.log(Level.WARN, "Error while deleting npc skill (" + npcId + ", " + skillId + "): " + e);
		}
		finally
		{
			DatabaseUtils.closeConnection(con);
		}
	}

	private void reloadNpcSkillList(int npcId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		FiltredPreparedStatement statement2 = null;
		ResultSet skillDataList = null;
		ResultSet skillDataList2 = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);

			L2Skill skillData = null;
			if(npcData.getSkills() != null)
			{
				npcData.getSkills().clear();
			}

			// without race
			statement = con.prepareStatement("SELECT `skillid`, `level` FROM `npcskills` WHERE `npcid`=? AND `skillid` <> 4416");
			statement.setInt(1, npcId);
			skillDataList = statement.executeQuery();

			while(skillDataList.next())
			{
				int idval = skillDataList.getInt("skillid");
				int levelval = skillDataList.getInt("level");
				skillData = SkillTable.getInstance().getInfo(idval, levelval);
				if(skillData != null)
				{
					npcData.addSkill(skillData);
				}
			}
			DatabaseUtils.closeResultSet(skillDataList);
			DatabaseUtils.closeStatement(statement);
			if(Config.CUSTOM_NPC_SKILLS_TABLE)
			{
				statement2 = con.prepareStatement("SELECT `skillid`, `level` FROM `npcskills` WHERE `npcid`=? AND `skillid` <> 4416");
				statement2.setInt(1, npcId);
				skillDataList2 = statement2.executeQuery();

				while(skillDataList2.next())
				{
					int idval = skillDataList2.getInt("skillid");
					int levelval = skillDataList2.getInt("level");
					skillData = SkillTable.getInstance().getInfo(idval, levelval);
					if(skillData != null)
					{
						npcData.addSkill(skillData);
					}
				}
				DatabaseUtils.closeResultSet(skillDataList2);
				DatabaseUtils.closeStatement(statement2);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "Error while reloading npc skill list (" + npcId + "): " + e);
		}
		finally
		{
			DatabaseUtils.closeConnection(con);
		}
	}

	void showRadiusNpc(L2PcInstance activeChar, int radius, int page)
	{
		int LIMIT = 40; //количество нпц на странице

		if(page <= 0)
		{
			page = 1;
		}

		if(radius > 100000)
		{
			radius = 100000;
		}

		StringBuilder html = new StringBuilder();
		List<L2Npc> linkedList = new LinkedList<>();

		//Получаем список нпц в радиусе
		Collection<L2Npc> list = activeChar.getKnownList().getKnownNpcInRadius(radius);
		linkedList.addAll(list);

		int size = list.size();
		int max = size / LIMIT;
		if(size > LIMIT * max)
		{
			max++;
		}

		html.append("<html><title>").append(page).append(" of ").append(max).append("</title><body><br>");
		html.append("<table width=300 bgcolor=666666><tr>");
		html.append("<td>Page </td>");
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
				html.append("<td><a action=\"bypass -h admin_showNpcList ");
				html.append(radius);
				html.append(' ');
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
		linkedList.subList(start, end).stream().filter(npc -> npc != null).forEach(npc -> {
			html.append("<tr>");

			html.append("<td><a action=\"bypass -h admin_move_to ");
			html.append(npc.getX());
			html.append(' ');
			html.append(npc.getY());
			html.append(' ');
			html.append(npc.getZ());
			html.append("\"> ");
			html.append(npc.getNpcId());
			html.append("</a></td>");

			html.append("<td>");
			html.append(npc.getName());
			html.append("</td>");

			html.append("<td><a action=\"bypass -h admin_del_obj ");
			html.append(npc.getObjectId());
			html.append("\"> D");
			html.append("</a></td>");

			html.append("</tr>");
		});
		html.append("</table></body></html>");

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(html.toString());
		activeChar.sendPacket(adminReply);
	}
}
