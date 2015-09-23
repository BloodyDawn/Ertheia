package dwo.gameserver.handler.actions;

import dwo.config.Config;
import dwo.gameserver.handler.IActionHandler;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.Elementals;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.npc.drop.L2DropCategory;
import dwo.gameserver.model.world.npc.drop.L2DropData;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class L2NpcActionShift implements IActionHandler
{
	/**
	 * Manage and Display the GM console to modify the L2NpcInstance (GM only).<BR><BR>
	 * <p/>
	 * <B><U> Actions (If the L2PcInstance is a GM only)</U> :</B><BR><BR>
	 * <li>Set the L2NpcInstance as target of the L2PcInstance player (if necessary)</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
	 * <li>If L2NpcInstance is autoAttackable, send a Server->Client packet StatusUpdate to the L2PcInstance in order to update L2NpcInstance HP bar </li>
	 * <li>Send a Server->Client NpcHtmlMessage() containing the GM console about this L2NpcInstance </li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Each group of Server->Client packet must be terminated by a ActionFail packet in order to avoid
	 * that client wait an other packet</B></FONT><BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : Action</li><BR><BR>
	 */

	private static final NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
	private static final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);

	static
	{
		pf.setMaximumFractionDigits(4);
		df.setMinimumFractionDigits(2);
	}

	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		// Check if the L2PcInstance is a GM
		if(activeChar.getAccessLevel().isGm())
		{
			// Set the target of the L2PcInstance activeChar
			activeChar.setTarget(target);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance activeChar
			// The activeChar.getLevel() - getLevel() permit to display the correct color in the select window
			MyTargetSelected my = new MyTargetSelected(target.getObjectId(), activeChar.getLevel() - ((L2Character) target).getLevel());
			activeChar.sendPacket(my);

			// Check if the activeChar is attackable (without a forced attack)
			if(target.isAutoAttackable(activeChar))
			{
				// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
				StatusUpdate su = new StatusUpdate(target);
				su.addAttribute(StatusUpdate.CUR_HP, (int) ((L2Character) target).getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, ((L2Character) target).getMaxHp());
				activeChar.sendPacket(su);
			}

			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile(activeChar.getLang(), "mods/admin/npcinfo.htm");

			html.replace("%objid%", String.valueOf(target.getObjectId()));
			html.replace("%class%", target.getClass().getSimpleName());

			if(target.isSummon())
			{
				html.replace("%id%", String.valueOf(((L2Summon) target).getTemplate().getNpcId()));
				html.replace("%lvl%", String.valueOf(((L2Summon) target).getTemplate().getLevel()));
				html.replace("%name%", String.valueOf(((L2Summon) target).getTemplate().getName()));
				html.replace("%serverName%", "");
				html.replace("%tmplid%", String.valueOf(((L2Summon) target).getTemplate().getNpcId()));
			}
			else
			{
				html.replace("%id%", String.valueOf(((L2Npc) target).getTemplate().getNpcId()));
				html.replace("%lvl%", String.valueOf(((L2Npc) target).getTemplate().getLevel()));
				html.replace("%name%", String.valueOf(((L2Npc) target).getTemplate().getName()));
				html.replace("%serverName%", String.valueOf(((L2Npc) target).getTemplate().getServerName()));
				html.replace("%tmplid%", String.valueOf(((L2Npc) target).getTemplate().getNpcId()));
			}

			html.replace("%aggro%", String.valueOf(target instanceof L2Attackable ? ((L2Attackable) target).getAggroRange() : 0));
			html.replace("%hp%", String.valueOf((int) ((L2Character) target).getCurrentHp()));
			html.replace("%hpmax%", String.valueOf(((L2Character) target).getMaxHp()));
			html.replace("%mp%", String.valueOf((int) ((L2Character) target).getCurrentMp()));
			html.replace("%mpmax%", String.valueOf(((L2Character) target).getMaxMp()));

			html.replace("%patk%", String.valueOf(((L2Character) target).getPAtk(null)));
			html.replace("%matk%", String.valueOf(((L2Character) target).getMAtk(null, null)));
			html.replace("%pdef%", String.valueOf(((L2Character) target).getPDef(null)));
			html.replace("%mdef%", String.valueOf(((L2Character) target).getMDef(null, null)));
			html.replace("%accu%", String.valueOf(((L2Character) target).getPhysicalAccuracy()));
			html.replace("%evas%", String.valueOf(((L2Character) target).getPhysicalEvasionRate(null)));
			html.replace("%crit%", String.valueOf(((L2Character) target).getCriticalHit(null, null)));
			html.replace("%rspd%", String.valueOf(((L2Character) target).getRunSpeed()));
			html.replace("%aspd%", String.valueOf(((L2Character) target).getPAtkSpd()));
			html.replace("%cspd%", String.valueOf(((L2Character) target).getMAtkSpd()));
			html.replace("%str%", String.valueOf(((L2Character) target).getSTR()));
			html.replace("%dex%", String.valueOf(((L2Character) target).getDEX()));
			html.replace("%con%", String.valueOf(((L2Character) target).getCON()));
			html.replace("%int%", String.valueOf(((L2Character) target).getINT()));
			html.replace("%wit%", String.valueOf(((L2Character) target).getWIT()));
			html.replace("%men%", String.valueOf(((L2Character) target).getMEN()));
			html.replace("%loc%", String.valueOf(target.getX() + " " + target.getY() + ' ' + target.getZ()));
			html.replace("%heading%", String.valueOf(((L2Character) target).getHeading()));
			html.replace("%collision_radius%", String.valueOf(((L2Character) target).getTemplate().getFCollisionRadius((L2Character) target)));
			html.replace("%collision_height%", String.valueOf(((L2Character) target).getTemplate().getFCollisionHeight((L2Character) target)));
			html.replace("%dist%", String.valueOf((int) Math.sqrt(activeChar.getDistanceSq(target))));

			if(target instanceof L2MonsterInstance)
			{
				html.replace("%trans_id%", String.valueOf(((L2MonsterInstance) target).getTransformationId()));
			}
			else
			{
				html.replace("%trans_id%", "none");
			}

			if(target.isSummon())
			{
				html.replace("%targetable_base%", "?");
				html.replace("%targetable_core%", "?");

				html.replace("%showname_base%", "?");
				html.replace("%showname_core%", "?");
				html.replace("%random_animation%", "?");
				html.replace("%random_walk%", "?");
			}
			else
			{
				html.replace("%targetable_base%", String.valueOf(((L2Npc) target).isTargetableBase() ? 1 : 0));
				html.replace("%targetable_core%", String.valueOf(((L2Npc) target).isTargetable() ? 1 : 0));

				html.replace("%showname_base%", String.valueOf(((L2Npc) target).isShowNameBase() ? 1 : 0));
				html.replace("%showname_core%", String.valueOf(((L2Npc) target).isShowName() ? 1 : 0));
				html.replace("%random_animation%", String.valueOf(((L2Npc) target).hasRandomAnimation() ? 1 : 0));
				html.replace("%random_walk%", String.valueOf(((L2Npc) target).isNoRndWalk() ? 0 : 1));
			}

			byte attackAttribute = ((L2Character) target).getAttackElement();
			html.replace("%ele_atk%", Elementals.getElementName(attackAttribute));
			html.replace("%ele_atk_value%", String.valueOf(((L2Character) target).getAttackElementValue(attackAttribute)));
			html.replace("%ele_dfire%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.FIRE)));
			html.replace("%ele_dwater%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.WATER)));
			html.replace("%ele_dwind%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.WIND)));
			html.replace("%ele_dearth%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.EARTH)));
			html.replace("%ele_dholy%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.HOLY)));
			html.replace("%ele_ddark%", String.valueOf(((L2Character) target).getDefenseElementValue(Elementals.DARK)));

			if(!target.isSummon() && ((L2Npc) target).getSpawn() != null)
			{
				html.replace("%spawn%", ((L2Npc) target).getSpawn().getLocx() + " " + ((L2Npc) target).getSpawn().getLocy() + ' ' + ((L2Npc) target).getSpawn().getLocz());
				html.replace("%loc2d%", String.valueOf((int) Math.sqrt(((L2Character) target).getPlanDistanceSq(((L2Npc) target).getSpawn().getLocx(), ((L2Npc) target).getSpawn().getLocy()))));
				html.replace("%loc3d%", String.valueOf((int) Math.sqrt(((L2Character) target).getDistanceSq(((L2Npc) target).getSpawn().getLocx(), ((L2Npc) target).getSpawn().getLocy(), ((L2Npc) target).getSpawn().getLocz()))));
				html.replace("%resp%", String.valueOf(((L2Npc) target).getSpawn().getRespawnDelay() / 1000));
			}
			else
			{
				html.replace("%spawn%", "<font color=FF0000>null</font>");
				html.replace("%loc2d%", "<font color=FF0000>--</font>");
				html.replace("%loc3d%", "<font color=FF0000>--</font>");
				html.replace("%resp%", "<font color=FF0000>--</font>");
			}

			if(!target.isSummon() && ((L2Npc) target).hasAI())
			{
				html.replace("%ai_intention%", "<tr><td><table width=270 border=0 bgcolor=131210><tr><td width=100><font color=FFAA00>Intention:</font></td><td align=right width=170>" + ((L2Npc) target).getAI().getIntention().name() + "</td></tr></table></td></tr>");
				html.replace("%ai%", "<tr><td><table width=270 border=0><tr><td width=100><font color=FFAA00>AI</font></td><td align=right width=170>" + ((L2Npc) target).getAI().getClass().getSimpleName() + "</td></tr></table></td></tr>");
				html.replace("%ai_type%", "<tr><td><table width=270 border=0 bgcolor=131210><tr><td width=100><font color=FFAA00>AIType</font></td><td align=right width=170>" + ((L2Npc) target).getAiType() + "</td></tr></table></td></tr>");
				html.replace("%ai_clan%", "<tr><td><table width=270 border=0><tr><td width=100><font color=FFAA00>Clan & Range:</font></td><td align=right width=170>" + ((L2Npc) target).getTemplate().getClan() + ' ' + String.valueOf(((L2Npc) target).getTemplate().getClanRange()) + "</td></tr></table></td></tr>");
				html.replace("%ai_enemy_clan%", "<tr><td><table width=270 border=0 bgcolor=131210><tr><td width=100><font color=FFAA00>Enemy & Range:</font></td><td align=right width=170>" + ((L2Npc) target).getTemplate().getEnemyClan() + ' ' + String.valueOf(((L2Npc) target).getTemplate().getEnemyRange()) + "</td></tr></table></td></tr>");
			}
			else
			{
				html.replace("%ai_intention%", "");
				html.replace("%ai%", "");
				html.replace("%ai_type%", "");
				html.replace("%ai_clan%", "");
				html.replace("%ai_enemy_clan%", "");
			}

			html.replace("%butt%", "");

			activeChar.sendPacket(html);
		}
		else if(Config.ALT_GAME_VIEWNPC)
		{
			// Set the target of the L2PcInstance activeChar
			activeChar.setTarget(target);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance activeChar
			// The activeChar.getLevel() - getLevel() permit to display the correct color in the select window
			MyTargetSelected my = new MyTargetSelected(target.getObjectId(), activeChar.getLevel() - ((L2Character) target).getLevel());
			activeChar.sendPacket(my);

			// Check if the activeChar is attackable (without a forced attack)
			if(target.isAutoAttackable(activeChar))
			{
				// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
				StatusUpdate su = new StatusUpdate(target);
				su.addAttribute(StatusUpdate.CUR_HP, (int) ((L2Character) target).getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, ((L2Character) target).getMaxHp());
				activeChar.sendPacket(su);
			}

			// Для мертвых мобов не показываем
			// табличку, иначе спойлеры плачут
			if(((L2Character) target).isDead())
			{
				return false;
			}

			if(target instanceof L2Npc)
			{
				L2Npc npc = (L2Npc) target;
				if(npc.getTemplate().getDropData() != null)
				{
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

					for(L2DropCategory cat : npc.getTemplate().getDropData())
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
		return true;
	}

	@Override
	public Class<? extends L2Object> getInstanceType()
	{
		return L2Npc.class;
	}
}
