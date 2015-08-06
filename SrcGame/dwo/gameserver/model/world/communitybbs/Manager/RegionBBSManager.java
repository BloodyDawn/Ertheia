package dwo.gameserver.model.world.communitybbs.Manager;

import dwo.config.Config;
import dwo.gameserver.GameServerStartup;
import dwo.gameserver.datatables.xml.ClassTemplateTable;
import dwo.gameserver.datatables.xml.ExperienceTable;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.Say2;
import dwo.gameserver.network.game.serverpackets.packet.show.ShowBoard;
import dwo.gameserver.util.StringUtil;
import gnu.trove.iterator.TIntObjectIterator;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;

public class RegionBBSManager extends BaseBBSManager
{
	private static final Comparator<L2PcInstance> playerNameComparator = (p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName());
	private static Logger _logChat = LogManager.getLogger("chat");
	private static FastMap<Integer, FastList<L2PcInstance>> _onlinePlayers = new FastMap<Integer, FastList<L2PcInstance>>().shared();
	private static FastMap<Integer, FastMap<String, String>> _communityPages = new FastMap<Integer, FastMap<String, String>>().shared();
	private int _onlineCount;
	private int _onlineCountGm;

	private RegionBBSManager()
	{
	}

	public static RegionBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if(command.equals("_bbsloc"))
		{
			showOldCommunity(activeChar, 1);
		}
		else if(command.startsWith("_bbsloc;page;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int page = 0;
			try
			{
				page = Integer.parseInt(st.nextToken());
			}
			catch(NumberFormatException nfe)
			{
			}

			showOldCommunity(activeChar, page);
		}
		else if(command.startsWith("_bbsloc;playerinfo;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			String name = st.nextToken();

			showOldCommunityPI(activeChar, name);
		}
		else
		{
			if(Config.COMMUNITY_TYPE == 1)
			{
				showOldCommunity(activeChar, 1);
			}
			else
			{
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>Команда: " + command + " не реализована или не существует</center><br><br></body></html>", "101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
		}
	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		if(activeChar == null)
		{
			return;
		}

		if(ar1.equals("PM"))
		{
			StringBuilder htmlCode = StringUtil.startAppend(500, "<html><body><br>" + "<table border=0><tr><td FIXWIDTH=15></td><td align=center>Доска Сообщества<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");
			try
			{

				L2PcInstance receiver = WorldManager.getInstance().getPlayer(ar2);
				if(receiver == null)
				{
					StringUtil.append(htmlCode, "Игрок не найден!<br><button value=\"Назад\" action=\"bypass _bbsloc;playerinfo;", ar2, "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "</td></tr></table></body></html>");
					separateAndSend(htmlCode.toString(), activeChar);
					return;
				}
				if(activeChar.isInJail() && Config.JAIL_DISABLE_CHAT)
				{
					activeChar.sendMessage("Вы находитесь в Тюрьме! Функция недоступна.");
					return;
				}
				if(activeChar.isChatBanned())
				{
					activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
					return;
				}

				if(Config.LOG_CHAT)
				{
					_logChat.log(Level.INFO, "TELL CB PM " + '[' + activeChar.getName() + " to " + receiver.getName() + ']');
				}

				Say2 cs = new Say2(activeChar.getObjectId(), ChatType.TELL, activeChar.getName(), ar3);
				if(!receiver.isSilenceMode(activeChar.getObjectId()) && !RelationListManager.getInstance().isBlocked(receiver, activeChar))
				{
					receiver.sendPacket(cs);
					activeChar.sendPacket(new Say2(activeChar.getObjectId(), ChatType.TELL, "->" + receiver.getName(), ar3));
					StringUtil.append(htmlCode, "Сообщение отправлено.<br><button value=\"Назад\" action=\"bypass _bbsloc;playerinfo;", receiver.getName(), "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "</td></tr></table></body></html>");
					separateAndSend(htmlCode.toString(), activeChar);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
					parsecmd("_bbsloc;playerinfo;" + receiver.getName(), activeChar);
				}
			}
			catch(StringIndexOutOfBoundsException e)
			{
				// ignore
			}
		}
		else
		{
			ShowBoard sb = new ShowBoard(StringUtil.concat("<html><body><br><br><center>Команда: ", ar1, " не реализована или не существует.</center><br><br></body></html>"), "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	/**
	 * @param activeChar
	 * @param name
	 */
	private void showOldCommunityPI(L2PcInstance activeChar, String name)
	{
		StringBuilder htmlCode = StringUtil.startAppend(1000, "<html><body><br>" + "<table border=0><tr><td FIXWIDTH=15></td><td align=center>Доска Сообщества<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");
		L2PcInstance player = WorldManager.getInstance().getPlayer(name);

		if(player != null)
		{
			String sex = "Парень";
			if(player.getAppearance().getSex())
			{
				sex = "Девушка";
			}
			String levelApprox = "низкий";
			if(player.getLevel() >= 85)
			{
				levelApprox = "очень высокий";
			}
			else if(player.getLevel() >= 80)
			{
				levelApprox = "высокий";
			}
			else if(player.getLevel() >= 60)
			{
				levelApprox = "средний";
			}

			StringUtil.append(htmlCode, "<table border=0><tr><td>", player.getName(), " (", sex, " ", ClassTemplateTable.getInstance().getClass(player.getClassId().getId()).getClientCode(), "):</td></tr>" + "<tr><td>Уровень: ", levelApprox, "</td></tr>" + "<tr><td><br></td></tr>");

			if(activeChar != null && (activeChar.isGM() || player.getObjectId() == activeChar.getObjectId() || Config.SHOW_LEVEL_COMMUNITYBOARD))
			{
				long nextLevelExp = 0;
				long nextLevelExpNeeded = 0;
				if(player.getLevel() < ExperienceTable.getInstance().getMaxLevel() - 1)
				{
					nextLevelExp = ExperienceTable.getInstance().getExpForLevel(player.getLevel() + 1);
					nextLevelExpNeeded = nextLevelExp - player.getExp();
				}

				StringUtil.append(htmlCode, "<tr><td>Уровень: ", String.valueOf(player.getLevel()), "</td></tr>" + "<tr><td>Опыт: ", String.valueOf(player.getExp()), "/", String.valueOf(nextLevelExp), "</td></tr>" + "<tr><td>Нужно опыта для поднятия уровня: ", String.valueOf(nextLevelExpNeeded), "</td></tr>" + "<tr><td><br></td></tr>");
			}

			int uptime = (int) player.getUptime() / 1000;
			int h = uptime / 3600;
			int m = (uptime - h * 3600) / 60;
			int s = uptime - h * 3600 - m * 60;

			StringUtil.append(htmlCode, "<tr><td>Время работы: ", String.valueOf(h), "ч ", String.valueOf(m), "м ", String.valueOf(s), "с</td></tr>" + "<tr><td><br></td></tr>");

			if(player.getClan() != null)
			{
				StringUtil.append(htmlCode, "<tr><td>Клан: ", player.getClan().getName(), "</td></tr>" + "<tr><td><br></td></tr>");
			}

			StringUtil.append(htmlCode, "<tr><td><multiedit var=\"pm\" width=240 height=40><button value=\"Отправить ЛС\" action=\"Write Region PM ", player.getName(), " pm pm pm\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr><tr><td><br><button value=\"Назад\" action=\"bypass _bbsloc\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>" + "</td></tr></table>" + "</body></html>");
			separateAndSend(htmlCode.toString(), activeChar);
		}
		else
		{
			ShowBoard sb = new ShowBoard(StringUtil.concat("<html><body><br><br><center>Нет игрока с именем ", name, "</center><br><br></body></html>"), "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	private void showOldCommunity(L2PcInstance activeChar, int page)
	{
		separateAndSend(getCommunityPage(page, activeChar.isGM() ? "pl" : "pl"), activeChar);
	}

	public void changeCommunityBoard()
	{
		FastList<L2PcInstance> sortedPlayers = new FastList<>();
		TIntObjectIterator<L2PcInstance> it = WorldManager.getInstance().getAllPlayers().iterator();
		while(it.hasNext())
		{
			it.advance();
			if(it.value() != null)
			{
				sortedPlayers.add(it.value());
			}
		}
		Collections.sort(sortedPlayers, playerNameComparator);

		_onlinePlayers.clear();
		_onlineCount = 0;
		_onlineCountGm = 0;

		sortedPlayers.forEach(this::addOnlinePlayer);

		_communityPages.clear();
		writeCommunityPages();
	}

	private void addOnlinePlayer(L2PcInstance player)
	{
		boolean added = false;

		for(FastList<L2PcInstance> page : _onlinePlayers.values())
		{
			if(page.size() < Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
			{
				if(!page.contains(player))
				{
					page.add(player);
					if(!player.getAppearance().getInvisible())
					{
						_onlineCount++;
					}
					_onlineCountGm++;
				}
				added = true;
				break;
			}
			else if(page.contains(player))
			{
				added = true;
				break;
			}
		}

		if(!added)
		{
			FastList<L2PcInstance> temp = new FastList<>();
			int page = _onlinePlayers.size() + 1;
			if(temp.add(player))
			{
				_onlinePlayers.put(page, temp);
				if(!player.getAppearance().getInvisible())
				{
					_onlineCount++;
				}
				_onlineCountGm++;
			}
		}
	}

	private void writeCommunityPages()
	{
		StringBuilder htmlCode = new StringBuilder(2000);
		String tdClose = "</td>";
		String tdOpen = "<td align=left valign=top>";
		String trClose = "</tr>";
		String trOpen = "<tr>";
		String colSpacer = "<td FIXWIDTH=15></td>";

		for(int page : _onlinePlayers.keySet())
		{
			FastMap<String, String> communityPage = new FastMap<>();
			htmlCode.setLength(0);
			StringUtil.append(htmlCode, "<html><body><br>" + "<table>" + trOpen + "<td align=left valign=top>Сервер перезапущен: ", String.valueOf(GameServerStartup.dateTimeServerStarted.getTime()), tdClose + trClose + "</table>" + "<table>" + trOpen + tdOpen + "Рейт XP: x", String.valueOf(Config.RATE_XP), tdClose + colSpacer + tdOpen + "Партийный XP рейт: x", String.valueOf(Config.RATE_XP * Config.RATE_PARTY_XP), trClose + trOpen + tdOpen + "Рейт SP: x", String.valueOf(Config.RATE_SP), tdClose + colSpacer + tdOpen + "Партийный SP рейт: x", String.valueOf(Config.RATE_SP * Config.RATE_PARTY_SP), tdClose + trClose + trOpen + tdOpen + "Рейт дропа: ", String.valueOf(Config.RATE_DROP_ITEMS), tdClose + colSpacer + tdOpen + "Spoil Rate: ", String.valueOf(Config.RATE_DROP_SPOIL), tdClose + colSpacer + tdOpen + "Рейт адены: ", String.valueOf(Config.RATE_DROP_ITEMS_ID.get(PcInventory.ADENA_ID)), tdClose + trClose + "</table>" + "<table>" + trOpen + "<td><img src=\"sek.cbui355\" width=600 height=1><br></td>" + trClose + trOpen + tdOpen, String.valueOf(WorldManager.getInstance().getAllVisibleObjectsCount()), " Object count</td>" + trClose + trOpen + tdOpen, "</td>" + trClose + "</table>");

			int cell = 0;
			if(Config.BBS_SHOW_PLAYERLIST)
			{
				htmlCode.append("<table border=0><tr><td><table border=0>");

				for(L2PcInstance player : getOnlinePlayers(page))
				{
					cell++;

					if(cell == 1)
					{
						htmlCode.append(trOpen);
					}

					StringUtil.append(htmlCode, "<td align=left valign=top FIXWIDTH=110><a action=\"bypass _bbsloc;playerinfo;", player.getName(), "\">");

					if(player.isGM())
					{
						StringUtil.append(htmlCode, "<font color=\"LEVEL\">", player.getName(), "</font>");
					}
					else
					{
						htmlCode.append(player.getName());
					}

					htmlCode.append("</a></td>");

					if(cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
					{
						htmlCode.append(colSpacer);
					}

					if(cell == Config.NAME_PER_ROW_COMMUNITYBOARD)
					{
						cell = 0;
						htmlCode.append(trClose);
					}
				}
				if(cell > 0 && cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
				{
					htmlCode.append(trClose);
				}

				htmlCode.append("</table><br></td></tr>").append(trOpen).append("<td><img src=\"sek.cbui355\" width=600 height=1><br></td>").append(trClose).append("</table>");
			}

			if(getOnlineCount("gm") > Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
			{
				htmlCode.append("<table border=0 width=600><tr>");
				if(page == 1)
				{
					htmlCode.append("<td align=right width=190><button value=\"Назад\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				else
				{
					StringUtil.append(htmlCode, "<td align=right width=190><button value=\"Назад\" action=\"bypass _bbsloc;page;", String.valueOf(page - 1), "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}

				StringUtil.append(htmlCode, "<td FIXWIDTH=10></td>" + "<td align=center valign=top width=200>Displaying ", String.valueOf((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD + 1), " - ", String.valueOf((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD + getOnlinePlayers(page).size()), " player(s)</td>" + "<td FIXWIDTH=10></td>");
				if(getOnlineCount("gm") <= page * Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
				{
					htmlCode.append("<td width=190><button value=\"Вперед\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				else
				{
					StringUtil.append(htmlCode, "<td width=190><button value=\"Вперед\" action=\"bypass _bbsloc;page;", String.valueOf(page + 1), "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				htmlCode.append("</tr></table>");
			}

			htmlCode.append("</body></html>");

			communityPage.put("gm", htmlCode.toString());

			htmlCode.setLength(0);
			StringUtil.append(htmlCode, "<html><body><br>" + "<table>" + trOpen + "<td align=left valign=top>Сервер перезапущен: ", String.valueOf(GameServerStartup.dateTimeServerStarted.getTime()), tdClose + trClose + "</table>" + "<table>" + trOpen + tdOpen + "Рейт XP: x", String.valueOf(Config.RATE_XP), tdClose + colSpacer + tdOpen + "Партийный XP рейт: x", String.valueOf(Config.RATE_PARTY_XP), tdClose + trClose + trOpen + tdOpen + "Рейт SP: x", String.valueOf(Config.RATE_SP), tdClose + colSpacer + tdOpen + "Партийный SP рейт: x", String.valueOf(Config.RATE_PARTY_SP), tdClose + trClose + trOpen + tdOpen + "Рейт дропа: x", String.valueOf(Config.RATE_DROP_ITEMS), tdClose + colSpacer + tdOpen + "Рейт спойла: x", String.valueOf(Config.RATE_DROP_SPOIL), tdClose + colSpacer + tdOpen + "Рейт адены: x", String.valueOf(Config.RATE_DROP_ITEMS_ID.get(PcInventory.ADENA_ID)), tdClose + trClose + "</table>" + "<table>" + trOpen + "<td><img src=\"sek.cbui355\" width=600 height=1><br></td>" + trClose + trOpen + tdOpen, "</td>" + trClose + "</table>");

			if(Config.BBS_SHOW_PLAYERLIST)
			{
				htmlCode.append("<table border=0><tr><td><table border=0>");

				cell = 0;
				for(L2PcInstance player : getOnlinePlayers(page))
				{
					if(player == null || player.getAppearance().getInvisible())
					{
						continue; // Go to next
					}

					cell++;

					if(cell == 1)
					{
						htmlCode.append(trOpen);
					}

					StringUtil.append(htmlCode, "<td align=left valign=top FIXWIDTH=110><a action=\"bypass _bbsloc;playerinfo;", player.getName(), "\">");

					if(player.isGM())
					{
						StringUtil.append(htmlCode, "<font color=\"LEVEL\">", player.getName(), "</font>");
					}
					else
					{
						htmlCode.append(player.getName());
					}

					htmlCode.append("</a></td>");

					if(cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
					{
						htmlCode.append(colSpacer);
					}

					if(cell == Config.NAME_PER_ROW_COMMUNITYBOARD)
					{
						cell = 0;
						htmlCode.append(trClose);
					}
				}
				if(cell > 0 && cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
				{
					htmlCode.append(trClose);
				}

				htmlCode.append("</table><br></td></tr>").append(trOpen).append("<td><img src=\"sek.cbui355\" width=600 height=1><br></td>").append(trClose).append("</table>");
			}

			if(getOnlineCount("pl") > Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
			{
				htmlCode.append("<table border=0 width=600><tr>");

				if(page == 1)
				{
					htmlCode.append("<td align=right width=190><button value=\"Назад\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				else
				{
					//StringUtil.append(htmlCode, "<td align=right width=190><button value=\"Назад\" action=\"bypass _bbsloc;page;", String.valueOf(page - 1), "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
					StringUtil.append(htmlCode, "<td align=right width=190></td>");
				}

	            /*
	            StringUtil.append(htmlCode, "<td FIXWIDTH=10></td>" + "<td align=center valign=top width=200>Показываем ", String.valueOf(((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD) + 1), " - ", String.valueOf(((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
                        + getOnlinePlayers(page).size()), " игроков</td>" + "<td FIXWIDTH=10></td>");
                        */

				if(getOnlineCount("pl") <= page * Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
				{
					//htmlCode.append("<td width=190><button value=\"Вперед\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
					htmlCode.append("<td width=190></td>");
				}
				else
				{
					StringUtil.append(htmlCode, "<td width=190><button value=\"Вперед\" action=\"bypass _bbsloc;page;", String.valueOf(page + 1), "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}

				htmlCode.append("</tr></table>");
			}

			htmlCode.append("</body></html>");

			communityPage.put("pl", htmlCode.toString());

			_communityPages.put(page, communityPage);
		}
	}

	public int getOnlineCount(String type)
	{
		return type.equalsIgnoreCase("gm") ? _onlineCountGm : _onlineCount;
	}

	private FastList<L2PcInstance> getOnlinePlayers(int page)
	{
		return _onlinePlayers.get(page);
	}

	public String getCommunityPage(int page, String type)
	{
		return _communityPages.get(page) != null ? _communityPages.get(page).get(type) : null;
	}

	private static class SingletonHolder
	{
		protected static final RegionBBSManager _instance = new RegionBBSManager();
	}
}