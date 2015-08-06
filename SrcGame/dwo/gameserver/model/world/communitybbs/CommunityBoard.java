package dwo.gameserver.model.world.communitybbs;

import dwo.config.Config;
import dwo.config.mods.ConfigCommunityBoardPVP;
import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.restriction.RestrictionChain;
import dwo.gameserver.model.actor.restriction.RestrictionResponse;
import dwo.gameserver.model.world.communitybbs.Manager.ClanBBSManager;
import dwo.gameserver.model.world.communitybbs.Manager.ClassBBSManager;
import dwo.gameserver.model.world.communitybbs.Manager.EnchantBBSManager;
import dwo.gameserver.model.world.communitybbs.Manager.PostBBSManager;
import dwo.gameserver.model.world.communitybbs.Manager.RegionBBSManager;
import dwo.gameserver.model.world.communitybbs.Manager.TopBBSManager;
import dwo.gameserver.model.world.communitybbs.Manager.TopicBBSManager;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.packet.show.ShowBoard;
import dwo.gameserver.network.game.serverpackets.packet.variation.ExShowVariationCancelWindow;
import dwo.gameserver.network.game.serverpackets.packet.variation.ExShowVariationMakeWindow;

import java.util.StringTokenizer;

public class CommunityBoard
{
	private CommunityBoard()
	{
	}

	public static CommunityBoard getInstance()
	{
		return SingletonHolder._instance;
	}

	public void handleCommands(L2GameClient client, String command)
	{
		L2PcInstance activeChar = client.getActiveChar();
		if(activeChar == null)
		{
			return;
		}

        boolean allow = false;
        
		RestrictionResponse rest;
		if(!(rest = activeChar.getRestrictionController().check(RestrictionChain.CUSTOM_SERVICE)).passed())
		{
			switch(rest.getReason())
			{
				case PARTICIPATING_OLYMPIAD:
					activeChar.sendMessage("Сервис запрещено использовать во время участия в Великой Олимпиаде.");
					break;
				case OBSERVING:
					activeChar.sendMessage("Сервис запрещено использовать в режиме наблюдателя.");
					break;
				case DEAD:
					activeChar.sendMessage("Сервис запрещено использовать, когда Вы мертвы.");
					break;
				case PARTICIPATING_COMBAT:
					activeChar.sendMessage("Сервис запрещено использовать во время боя.");
					break;
				case COMBAT_FLAG_EQUPPIED:
					activeChar.sendMessage("Сервис разрешено использовать только на мирной территории.");
					break;
				case BAD_REPUTATION:
                    if (!ConfigCommunityBoardPVP.COMMUNITY_BOARD_ALLOW_PK)
                    {
                        activeChar.sendMessage("Сервис запрещено использовать в состоянии Хаоса.");
                    } 
                    else 
                    {
                        allow = true;
                    }
				case PARTICIPATING_SIEGE:
					activeChar.sendMessage("Сервис запрещено использовать во время Осад.");
					break;
				case CASTING:
					activeChar.sendMessage("Сервис запрещено использовать в данных условиях.");
					break;
				case ATTACKING:
					activeChar.sendMessage("Сервис запрещено использовать в данных условиях.");
					break;
				case PRISONER:
					activeChar.sendMessage("Сервис запрещено использовать в тюрьме.");
					break;
				case FLYING:
					activeChar.sendMessage("Сервис запрещено использовать во время полета.");
					break;
				case PARTICIPATING_DUEL:
					activeChar.sendMessage("Сервис запрещено использовать на дуели.");
					break;
			}
            if (!allow) 
            {
                return;
            }
		}

		if (EventManager.onHandleCommandBBS(activeChar))
		{
			activeChar.sendMessage("Сервис запрещено использовать находясь в PvP ивенте.");
			return;
		}

		switch(Config.COMMUNITY_TYPE)
		{
			default:
			case 0: //disabled
				activeChar.sendPacket(SystemMessageId.CB_OFFLINE);
				break;
			case 1: // old
				RegionBBSManager.getInstance().parsecmd(command, activeChar);
				break;
			case 2: // new
				if(command.startsWith("_bbsclan"))
				{
					ClanBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if(command.startsWith("_bbsmemo") || command.startsWith("_bbstopics"))
				{
					TopicBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if(command.startsWith("_bbsposts"))
				{
					PostBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if(command.startsWith("_bbstop") || command.startsWith("_bbshome"))
				{
					TopBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if(command.startsWith("_bbsloc"))
				{
					RegionBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if(command.startsWith("_bbsenchant"))
				{
					if(ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_ENCHANT)
					{
						EnchantBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("Функция выключена Администратором!");
						return;
					}
				}
				else if(command.startsWith("_bbs_carrier_enable"))
				{
					ClassBBSManager.getInstance().enableCarrier(activeChar);
				}
				else if(command.startsWith("_bbs_carrier_disable"))
				{
					ClassBBSManager.getInstance().disableCarrier(activeChar);
				}
				else if(command.startsWith("_bbs_carrier_nobless"))
				{
					ClassBBSManager.getInstance().giveNobless(activeChar);
				}
				else if(command.startsWith("_bbsmultisell;"))
				{
					if(ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_MULTISELL)
					{
						StringTokenizer st = new StringTokenizer(command, ";");
						st.nextToken();
						TopBBSManager.getInstance().parsecmd("_bbstop;shop/" + st.nextToken(), activeChar);
						int multisell = Integer.parseInt(st.nextToken());
						MultiSellData.getInstance().separateAndSend(multisell, activeChar, null);
					}
					else
					{
						activeChar.sendMessage("В данных условиях Магазин использовать запрещено!");
						return;
					}
				}
				else if(command.startsWith("_bbsscripts;"))
				{
					StringTokenizer st = new StringTokenizer(command, ";");
					st.nextToken();
					TopBBSManager.getInstance().parsecmd("_bbstop;" + st.nextToken(), activeChar);
					String com = st.nextToken();
					String[] word = com.split("\\s+");
					String[] args = com.substring(word[0].length()).trim().split("\\s+");
					String[] path = word[0].split(":");
					if(path.length != 2)
					{
						System.out.println("Bad Script bypass from Community Board!");
						return;
					}
					if(word.length == 1)
					{
						runScript(activeChar, path[0], path[1]);
					}
					else
					{
						runScript(activeChar, path[0], path[1], args);
					}
				}
				else if(command.startsWith("_bbsAugment;add"))
				{
					if(ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_MULTISELL)
					{
						TopBBSManager.getInstance().parsecmd(command, activeChar);
						activeChar.sendPacket(SystemMessageId.SELECT_THE_ITEM_TO_BE_AUGMENTED);
						activeChar.sendPacket(new ExShowVariationMakeWindow());
						activeChar.cancelActiveTrade();
						return;
					}
					else
					{
						activeChar.sendMessage("Магазин выключен Администратором!");
						return;
					}
				}
				else if(command.startsWith("_bbsAugment;remove"))
				{
					if(ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_ALLOW_COMMUNITY_MULTISELL)
					{
						TopBBSManager.getInstance().parsecmd(command, activeChar);
						activeChar.sendPacket(SystemMessageId.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION);
						activeChar.sendPacket(new ExShowVariationCancelWindow());
						activeChar.cancelActiveTrade();
						return;
					}
					else
					{
						activeChar.sendMessage("Функция выключена Администратором!");
						return;
					}
				}
				else if(command.startsWith("bbs_add_fav"))
				{
					activeChar.sendMessage("Функция 'Закладки' отключена Администратором.");
				}
				else
				{
					/*
					ShowBoard sb = new ShowBoard("<html><body><br><br><center>"+"Комманда: "+command+" не реализована"+"</center></body></html>", "101");
					activeChar.sendPacket(sb);
					activeChar.sendPacket(new ShowBoard(null, "102"));
					activeChar.sendPacket(new ShowBoard(null, "103"));
                    */
					//Временая заглушка
					TopBBSManager.getInstance().parsecmd("_bbshome", activeChar);
				}
				break;
		}
	}

	/**
	 * @param client
	 * @param url
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	public void handleWriteCommands(L2GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		L2PcInstance activeChar = client.getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		switch(Config.COMMUNITY_TYPE)
		{
			case 2:
				switch(url)
				{
					case "Topic":
						TopicBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
						break;
					case "Post":
						PostBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
						break;
					case "Region":
						RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
						break;
					case "Notice":
						ClanBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
						break;
					default:
						ShowBoard sb = new ShowBoard("<html><body><br><br><center>" + "Комманда: " + url + " не реализована" + "</center></body></html>", "101");
						activeChar.sendPacket(sb);
						activeChar.sendPacket(new ShowBoard(null, "102"));
						activeChar.sendPacket(new ShowBoard(null, "103"));
						break;
				}
				break;
			case 1:
				RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				break;
			default:
			case 0:
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>Общественная Доска временно отключена!</center></body></html>", "101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
				break;
		}
	}

	/**
	 * Запуск скрипта откуда угодно
	 * @param scriptName имя скрипта
	 * @param scriptData евент скрипта
	 */
	public void runScript(L2PcInstance player, String scriptName, String scriptData)
	{
		runScript(player, scriptName, scriptData, null);
	}

	/**
	 * Запуск скрипта откуда угодно
	 * @param scriptName имя скрипта
	 * @param scriptData onEvent скрипта
	 * @param args входящие аргументы для скрипты
	 */
	public void runScript(L2PcInstance player, String scriptName, String scriptData, String[] args)
	{
		Quest qs = QuestManager.getInstance().getQuest(scriptName);
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		String event = scriptData;
		if(args != null)
		{
			event += ":";
			for(String arg : args)
			{
				event += arg + ';';
			}
			event = event.substring(0, event.lastIndexOf(';'));
		}
		String content = qs.onAdvEvent(event, null, player);
		if(content != null)
		{
			html.setHtml(content);
			player.sendPacket(html);
			player.sendActionFailed();
		}
	}

	private static class SingletonHolder
	{
		protected static final CommunityBoard _instance = new CommunityBoard();
	}
}
