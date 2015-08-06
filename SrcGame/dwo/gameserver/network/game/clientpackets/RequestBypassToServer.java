package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.AdminTable;
import dwo.gameserver.datatables.xml.BuyListData;
import dwo.gameserver.datatables.xml.RaidRadarTable;
import dwo.gameserver.handler.AdminCommandHandler;
import dwo.gameserver.handler.BypassCommandManager;
import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.HandlerParams.CommandWrapper;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.handler.VoicedHandlerManager;
import dwo.gameserver.instancemanager.HeroManager;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcFreight;
import dwo.gameserver.model.player.L2TradeList;
import dwo.gameserver.model.world.communitybbs.CommunityBoard;
import dwo.gameserver.model.world.npc.L2Event;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.ConfirmDlg;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.PackageToList;
import dwo.gameserver.network.game.serverpackets.ShopPreviewList;
import dwo.gameserver.network.game.serverpackets.TutorialCloseHtml;
import dwo.gameserver.network.game.serverpackets.WareHouseWithdrawList;
import dwo.gameserver.network.game.serverpackets.packet.curioushouse.ExCuriousHouseObserveList;
import dwo.gameserver.network.game.serverpackets.packet.tradelist.ExBuySellList;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExHeroList;
import dwo.gameserver.util.GMAudit;
import org.apache.log4j.Level;

import java.util.List;
import java.util.StringTokenizer;

public class RequestBypassToServer extends L2GameClientPacket
{
	private String _command;

	/**
	 * @param activeChar client
	 */
	private static void comeHere(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if(obj == null)
		{
			return;
		}
		if(obj instanceof L2Npc)
		{
			L2Npc temp = (L2Npc) obj;
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 0));
		}
	}

	@Override
	protected void readImpl()
	{
		_command = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(_command.isEmpty())
		{
			_log.log(Level.INFO, player.getName() + " send empty requestbypass");
			player.logout();
			return;
		}

		try
		{
			if(_command.startsWith("admin_")) //&& activeChar.getAccessLevel() >= Config.GM_ACCESSLEVEL)
			{
				String command = _command.split(" ")[0];

				IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);

				if(ach == null)
				{
					if(player.isGM())
					{
						player.sendMessage("The command " + command.substring(6) + " does not exist!");
					}

					_log.log(Level.WARN, "No handler registered for admin command '" + command + '\'');
					return;
				}

				if(!AdminTable.getInstance().hasAccess(command, player.getAccessLevel()))
				{
					player.sendMessage("You don't have the access rights to use this command!");
					_log.log(Level.WARN, "Character " + player.getName() + " tried to use admin command " + command + ", without proper access level!");
					return;
				}

				if(AdminTable.getInstance().requireConfirm(command))
				{
					player.getPcAdmin().setAdminConfirmCmd(_command);
					ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1);
					dlg.addString("Are you sure you want execute command " + _command.substring(6) + " ?");
					player.sendPacket(dlg);
				}
				else
				{
					if(Config.GMAUDIT)
					{
						GMAudit.auditGMAction(player.getName() + " [" + player.getObjectId() + ']', _command, player.getTarget() != null ? player.getTarget().getName() : "no-target");
					}

					ach.useAdminCommand(_command, player);
				}
			}
			else if(_command.startsWith("voiced_"))
			{
				String command = _command.split(" ")[0];
				command = command.substring(7);

				VoicedHandlerManager.getInstance().execute(new HandlerParams<>(player, command, HandlerParams.parseArgs(_command.substring(7 + command.length())), null));

			}
			else if(_command.equals("come_here") && player.isGM())
			{
				comeHere(player);
			}
			else if(_command.startsWith("npc_"))
			{
				if(!player.validateBypass(_command))
				{
					return;
				}

				int endOfId = _command.indexOf('_', 5);
				String id;
				id = endOfId > 0 ? _command.substring(4, endOfId) : _command.substring(4);
				try
				{
					L2Object object = WorldManager.getInstance().findObject(Integer.parseInt(id));

					if(_command.substring(endOfId + 1).startsWith("event_participate"))
					{
						L2Event.inscribePlayer(player);
						return;
					}
					if(object instanceof L2Npc && endOfId > 0 && player.isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
					{
						((L2Npc) object).onBypassFeedback(player, _command.substring(endOfId + 1));
						return;
					}
					player.sendActionFailed();
				}
				catch(NumberFormatException ignored)
				{
				}
			}
			// Navigate through Manor windows
			else if(_command.startsWith("manor_menu_select"))
			{
				CommandWrapper command = HandlerParams.parseCommand(_command);
				BypassCommandManager.getInstance().execute(new BypassHandlerParams(player, _command, command.getCommand(), null, command.getArgs(), command.getQueryArgs()));
			}
			else if(_command.startsWith("_bbs") || _command.startsWith("bbs"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if(_command.startsWith("_mail") || _command.startsWith("_friend"))
			{
				player.sendPacket(SystemMessageId.CB_OFFLINE);

			}
			else if(_command.startsWith("_match"))
			{
				String params = _command.substring(_command.indexOf('?') + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = HeroManager.getInstance().getHeroByClass(heroclass);
				if(heroid > 0)
				{
					HeroManager.getInstance().showHeroFights(player, heroclass, heroid, heropage);
				}
			}
			else if(_command.startsWith("_diary"))
			{
				String params = _command.substring(_command.indexOf('?') + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = HeroManager.getInstance().getHeroByClass(heroclass);
				if(heroid > 0)
				{
					HeroManager.getInstance().showHeroDiary(player, heroclass, heroid, heropage);
				}
			}
			else if(_command.startsWith("_heroes"))
			{
				player.sendPacket(new ExHeroList());
			}
			else if(_command.startsWith("_pledgegame"))
			{
				player.sendPacket(new ExCuriousHouseObserveList());
			}
			else if(_command.startsWith("package_withdraw"))
			{
				PcFreight freight = player.getFreight();
				if(freight != null)
				{
					if(freight.getSize() > 0)
					{
						player.setActiveWarehouse(freight);
						player.sendPacket(new WareHouseWithdrawList(player, WareHouseWithdrawList.FREIGHT));
					}
					else
					{
						player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
					}
				}
			}
			else if(_command.startsWith("package_deposit"))
			{
				if(player.getAccountChars().size() < 1)
				{
					player.sendPacket(SystemMessageId.CHARACTER_DOES_NOT_EXIST);
				}
				else
				{
					player.sendPacket(new PackageToList(player.getAccountChars()));
				}
			}
			else if(_command.startsWith("menu_select"))
			{
				// Защита от пробелов в запросе
				_command = _command.replaceAll(" ", "");

				L2Npc npc = player.getLastFolkNPC();

				if(npc == null || !player.isInsideRadius(npc, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					return;
				}

				/*
				 * Команда имеет формат menu_select?ask=X[&reply=Y[&state=1]]
				 * X - тип запроса, Y - ID (квеста, магазина и т.п.)
				 * reply может отсутствовать, например <button action="bypass -h menu_select?ask=0" value="В начало" ...
				 */

				Integer ask = null;
				Integer reply = null;
				Integer state = null; // TODO: Никита, че это?)

				String[] str = _command.split("\\?")[1].split("&");
				// ask должен быть обязательно

				try
				{
					ask = Integer.parseInt(str[0].split("=")[1]);
				}
				catch(Exception ignored)
				{
				}
				try
				{
					reply = Integer.parseInt(str[1].split("=")[1]);
				}
				catch(Exception ignored)
				{
				}
				try
				{
					state = Integer.parseInt(str[2].split("=")[1]);
				}
				catch(Exception ignored)
				{
				}

				if(ask == null)
				{
					return;
				}

				if(reply == null)
				{
					reply = 0;
				}

				if(reply < Integer.MIN_VALUE)
				{
					return;
				}

				// Ишем ask среди скриптов
				Quest st = Quest.getQuestsForAskId(npc.getNpcId(), ask);
				if(st != null)
				{
					st.notifyAsk(npc, player, ask, reply);
					return;
				}

				if(!player.validateBypass(_command))
				{
					return;
				}

				switch(ask)
				{
					// Обычный магазин
					case -1:
						// Магазин в Варке и Кетре (уровень альянса проверяется в скриптах)
					case -31:
						L2TradeList tradeList = BuyListData.getInstance().getBuyList(npc.getNpcId(), reply);
						if(tradeList != null)
						{
							switch(reply)
							{
								case 0: // Стандартые мультиселлы имеют порядковые номера 0 и 1
								case 1:
									double buyTotalTaxRate = npc.getAdenaTotalTaxRate(ProcessType.BUY);
									double sellTotalTaxRate = npc.getAdenaTotalTaxRate(ProcessType.SELL);
									player.tempInventoryDisable();
									player.sendPacket(new ExBuySellList(player, tradeList, ProcessType.BUY, buyTotalTaxRate, false, player.getAdenaCount()));
									player.sendPacket(new ExBuySellList(player, tradeList, ProcessType.SELL, sellTotalTaxRate, false, player.getAdenaCount()));
									player.sendActionFailed();
									break;
								case 2: // Примерочные мультиселлы имеют порядковые номера 2 и 3
								case 3:
									player.sendPacket(new ShopPreviewList(tradeList, player.getAdenaCount(), player.getExpertiseLevel()));
									break;
							}
						}

						return;
					// Открытие дверек у обычных НПЦ
					case -201:
						// Открытие дверей с последующим автоматическим их закрытием
					case -35:
						switch(reply)
						{
							case 0:
								npc.openMyDoors(3600000);
								break;
							case 1:
								npc.openMyDoors();
								return;
							case 2:
								npc.closeMyDoors();
								return;
						}
						// Мультиселл
					case -303:
						CommandWrapper command = HandlerParams.parseCommand(_command);
						command.setCommand("multisell");
						BypassCommandManager.getInstance().execute(new BypassHandlerParams(player, _command, command.getCommand(), npc, command.getArgs(), command.getQueryArgs()));
						return;
					// Информация о территориях
					case -1000:
						switch(reply)
						{
							case 1:
								CommandWrapper commandWrapper = HandlerParams.parseCommand(_command);
								BypassCommandManager.getInstance().execute(new BypassHandlerParams(player, _command, commandWrapper.getCommand(), npc, commandWrapper.getArgs(), commandWrapper.getQueryArgs()));
								return;
							case 0:
								npc.showChatWindow(player);
								return;
						}
					case 0:
						npc.showChatWindow(player);
						return;
				}

				// Ишем ask среди квестов   TODO Перенести выше switch (ask) когда квесты переделаем на аски
				Quest quest = QuestManager.getInstance().getQuest(ask);
				if(quest != null)
				{
					player.processQuestEvent(quest, reply);
				}
			}
			else if(_command.startsWith("link "))
			{
				L2Npc npc = player.getLastFolkNPC();

				if(npc == null || !player.isInsideRadius(npc, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					return;
				}

				String html = _command.substring(5);

				if(html.isEmpty())
				{
					return;
				}

				// Линк закрытия туториала
				if(html.startsWith("tutorial_close_0"))
				{
					player.sendPacket(new TutorialCloseHtml());
					return;
				}

				// Хак для хтмлок с байпассом аля "<a action="link fishing_manual008.htm#7561">"
				if(html.contains("\\#"))
				{
					html = html.split("\\#")[0];
				}

				for(Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_TALK))
				{
					player.processQuestEvent(quest, html);
				}
			}
			else if(_command.equals("teleport_request"))
			{
				L2Npc npc = player.getLastFolkNPC();
				if(npc == null || !player.isInsideRadius(npc, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					return;
				}

				// Если у текущего НПЦ есть событие ON_TELEPORT_REQUEST, то уведомляем его без показа дефолтного листа #1
				List<Quest> quests = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_TELEPORT_REQUEST);
				if(quests != null)
				{
					for(Quest quest : quests)
					{
						quest.notifyTeleportRequest(npc, player);
					}
				}
				else
				{
					// Показываем стандартный лист под номером 1
					npc.showTeleportList(player, 1);
				}
			}
			else if(_command.equals("learn_skill"))
			{
				L2Npc npc = player.getLastFolkNPC();
				if(npc == null || !player.isInsideRadius(npc, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					return;
				}

				List<Quest> quests = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_LEARN_SKILL);
				if(quests != null)
				{
					for(Quest quest : quests)
					{
						quest.notifyLearnSkill(npc, player);
					}
				}
			}
			else if(_command.startsWith("class_change"))
			{
				L2Npc npc = player.getLastFolkNPC();
				if(npc == null || !player.isInsideRadius(npc, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					return;
				}

				Integer classId = null;
				String classIdToken = "class_name=";

				int classIdIndex = _command.indexOf(classIdToken);
				if(classIdIndex < 0)
				{
					return;
				}

				try
				{
					classId = Integer.parseInt(_command.substring(classIdIndex + classIdToken.length()));
				}
				catch(Exception ignored)
				{

				}

				if(classId == null)
				{
					return;
				}

				List<Quest> quests = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_CLASS_CHANGE_REQUESTED);
				if(quests != null)
				{
					for(Quest quest : quests)
					{
						quest.notifyClassChangeRequest(npc, player, classId);
					}
				}
			}
			else if(_command.startsWith("show_radar"))
			{
				long groupId = Long.parseLong(_command.substring(_command.indexOf("id=") + 3, _command.indexOf('&')));
				int indexId = Integer.parseInt(_command.substring(_command.indexOf("&index=") + 7));
				RaidRadarTable.getInstance().addRadar(player, groupId, indexId);
			}
			else if(_command.startsWith("dynamic_quest"))
			{
				CommandWrapper command = HandlerParams.parseCommand(_command);
				BypassCommandManager.getInstance().execute(new BypassHandlerParams(player, _command, command.getCommand(), null, command.getArgs(), command.getQueryArgs()));
			}
			else
			{
				boolean sendBypass = false;

				L2Npc npc = player.getLastFolkNPC();
				CommandWrapper commandWrapper = HandlerParams.parseCommand(_command);
				if(BypassCommandManager.getInstance().execute(new BypassHandlerParams(player, _command, commandWrapper.getCommand(), npc, commandWrapper.getArgs(), commandWrapper.getQueryArgs())))
				{
					sendBypass = true;
				}

				if(!sendBypass)
				{
					_log.log(Level.WARN, getClient() + " sent not handled RequestBypassToServer: [" + _command + ']');
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, getClient() + " sent bad RequestBypassToServer: \"" + _command + '"', e);
			if(player.isGM())
			{
				StringBuilder sb = new StringBuilder(200);
				sb.append("<html><body>");
				sb.append("Bypass error: ").append(e).append("<br1>");
				sb.append("Bypass command: ").append(_command).append("<br1>");
				sb.append("StackTrace:<br1>");
				for(StackTraceElement ste : e.getStackTrace())
				{
					sb.append(ste).append("<br1>");
				}
				sb.append("</body></html>");
				// item html
				NpcHtmlMessage msg = new NpcHtmlMessage(0, 12807);
				msg.setHtml(sb.toString());
				msg.disableValidation();
				player.sendPacket(msg);
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] 23 RequestBypassToServer";
	}
}
