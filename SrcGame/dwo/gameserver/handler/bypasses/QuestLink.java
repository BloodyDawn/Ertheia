package dwo.gameserver.handler.bypasses;

import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.util.StringUtil;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Quest links handler.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class QuestLink extends CommandHandler<String>
{
	/**
	 * Open a choose quest window on client with all quests available of the L2NpcInstance.<BR><BR>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance </li><BR><BR>
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 * @param npc The table containing quests of the L2NpcInstance
	 * @param quests
	 */
	public static void showQuestChooseWindow(L2PcInstance player, L2Npc npc, Quest[] quests)
	{
		StringBuilder sb = StringUtil.startAppend(150, "<html><body>");
		String state = "";
		int questId = -1;
		int questCount = 0;
		for(Quest q : quests)
		{
			if(q == null)
			{
				continue;
			}

			String stateColor = "";

			// Вычисляем цвет сообщения
			QuestState qs = player.getQuestState(q.getName());

			if(qs == null && !q.isLevelSatisfy(player.getLevel()))
			{
				continue;
			}

			// "Красим" только строки квестов, с которыми игрок взаимодействовал
			if(qs != null && !qs.isCreated())
			{
				if(!q.canBeStarted(player) && !qs.isStarted() && !qs.isCompleted())
				{
					stateColor = "<font color=\"a62f31\">"; // Красный
				}
				else if(qs.isCompleted())
				{
					stateColor = "<font color=\"777777\">"; // Серый
				}
				else if(qs.isStarted())
				{
					stateColor = "<font color=\"6699ff\">"; // Синий
				}
			}
			else
			{
				if(!q.canBeStarted(player))
				{
					stateColor = "<font color=\"a62f31\">"; // Красный
				}
			}

			StringUtil.append(sb, "<a action=\"bypass -h npc_%objectId%_Quest ", q.getName(), "\">" + stateColor + '[');
			questCount++;

			if(qs == null || qs.isCreated())
			{
				state = "01";
			}
			else if(qs.isStarted() && qs.getInt("cond") > 0)
			{
				state = "02";
			}
			else if(qs.isCompleted())
			{
				state = "03";
			}

			questId = q.getQuestId();
			if(q.getQuestId() > 10000)
			{
				questId -= 5000;
			}
			else if(questId == 146)
			{
				questId = 640;
			}
			StringUtil.append(sb, "<fstring>", String.valueOf(questId), state, "</fstring>");

			sb.append(']').append(stateColor.isEmpty() ? "" : "</font>").append("</a><br>");
		}
		if(questCount == 0)
		{
			sb.append("Для Вас нет доступных квестов на данный момент.");
		}
		sb.append("</body></html>");
		npc.insertObjectIdAndShowChatWindow(player, sb.toString());
	}

	@TextCommand("quest_accept")
	public boolean questAccept(BypassHandlerParams params)
	{
		L2PcInstance player = params.getPlayer();

		if(!player.validateBypass(params.getSource()))
		{
			return false;
		}

		if(player.getLastQuestNpcObject() <= 0)
		{
			return false;
		}

		L2Object npc = WorldManager.getInstance().findObject(player.getLastQuestNpcObject());

		if(npc == null || !player.isInsideRadius(npc, L2Npc.INTERACTION_DISTANCE, false, false))
		{
			return false;
		}

		/*
		 * Команда имеет формат menu_select?ask=X[&reply=Y]
		 * X - тип запроса, Y - ID (квеста, магазина и т.п.)
		 * reply может отсутствовать, например <button action="bypass -h menu_select?ask=0" value="В начало" ...
		 */
		int questId = -1;

		// ask должен быть обязательно
		if(!params.getQueryArgs().containsKey("quest_id"))
		{
			return false;
		}

		try
		{
			questId = Integer.parseInt(params.getQueryArgs().get("quest_id"));
		}
		catch(Exception ignored)
		{
		}

		if(questId < 0)
		{
			return false;
		}

		Quest quest = QuestManager.getInstance().getQuest(questId);

		if(quest == null)
		{
			return false;
		}

		player.processQuestEvent(quest, "quest_accept");
		return true;
	}

	@TextCommand("quest_start")
	public boolean questStart(BypassHandlerParams params)
	{
		String command = params.getCommand();
		L2PcInstance player = params.getPlayer();
		L2Character target = params.getTarget();

		if(target != null && !target.isNpc())
		{
			return false;
		}

		Integer questId = null;

		if(!params.getQueryArgs().containsKey("quest_id"))
		{
			return false;
		}

		try
		{
			questId = Integer.parseInt(params.getQueryArgs().get("quest_id"));
		}
		catch(Exception ignored)
		{

		}

		if(questId == null)
		{
			return false;
		}

		Quest quest = QuestManager.getInstance().getQuest(questId);

		if(quest == null)
		{
			return false;
		}
		quest.notifyStartFromItem(player);
		return true;
	}

	@TextCommand("talk_select")
	public boolean talkSelect(BypassHandlerParams params)
	{
		L2PcInstance player = params.getPlayer();
		L2Character target = params.getTarget();

		if(!target.isNpc())
		{
			return false;
		}

		L2Npc npc = player.getLastFolkNPC();

		if(npc == null || !player.isInsideRadius(npc, L2Npc.INTERACTION_DISTANCE, false, false))
		{
			return false;
		}

		showQuestWindow(player, (L2Npc) target);
		return true;
	}

	@TextCommand("quest")
	public boolean quest(BypassHandlerParams params)
	{
		L2PcInstance player = params.getPlayer();
		L2Character target = params.getTarget();

		if(!target.isNpc())
		{
			return false;
		}

		if(params.getArgs().isEmpty())
		{
			return talkSelect(params);
		}
		// TODO Раскоментить как избавимся от старого типа квестов
			/*
			if(!player.validateBypass(_command))
			{
				return false;
			}
			*/

		if(params.getArgs().size() <= 0)
		{
			return false;
		}

		String questArg = params.getArgs().get(0);

		Quest quest = QuestManager.getInstance().getQuest(questArg);
		int questId = 0;
		if(quest == null)
		{
			try
			{
				// TODO ВЫПИЛИ МЕНЯ КОГДА СДЕЛАЕШЬ 	!!!!
				if(questArg != null && questArg.contains("_"))
				{
					questId = !questArg.isEmpty() && questArg.charAt(0) == '_' ? Integer.parseInt(questArg.split("_")[1]) : Integer.parseInt(questArg.split("_")[0]);

					quest = QuestManager.getInstance().getQuest(questId);
				}
			}
			catch(IndexOutOfBoundsException ioobe)
			{
				log.log(Level.ERROR, ioobe);
			}
		}
		else
		{
			questId = quest.getQuestId();
		}

		if(params.getArgs().size() == 2)
		{
			player.processQuestEvent(quest, params.getArgs().get(1));
		}
		else
		{
			showQuestWindow(player, (L2Npc) target, questId);
		}

		return true;
	}

	/**
	 * Open a quest window on client with the text of the L2NpcInstance.<BR><BR>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the text of the quest state in the folder quests/questId/stateId.htm </li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance </li>
	 * <li>Send a Server->Client ActionFail to the L2PcInstance in order to avoid that the client wait another packet </li><BR><BR>
	 * @param player the L2PcInstance that talk with the {@code npc}.
	 * @param npc the L2NpcInstance that chats with the {@code player}.
	 * @param questId the Id of the quest to display the message.
	 */
	public void showQuestWindow(L2PcInstance player, L2Npc npc, int questId)
	{
		String content = null;
		QuestState qs = null;
		Quest quest = QuestManager.getInstance().getQuest(questId);

		if(quest != null)
		{
			qs = player.getQuestState(quest.getName());
			if(questId >= 1 && questId < 20000 && (player.getWeightPenalty() >= 3 || !player.isInventoryUnder90(true)))
			{
				player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
				return;
			}

			if(qs == null)
			{
				if(quest.getQuestId() >= 1 && quest.getQuestId() < 20000)
				{
					// Превышен лимит взятых квестов
					if(player.getAllActiveQuests().length > 40)
					{
						NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
						html.setFile(player.getLang(), "default/fullquest.htm");
						player.sendPacket(html);
						return;
					}
				}
				// check for start point
				List<Quest> qlst = npc.getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);

				if(qlst != null && !qlst.isEmpty())
				{
					for(Quest temp : qlst)
					{
						if(temp.equals(quest))
						{
							qs = quest.newQuestState(player);
							break;
						}
					}
				}
			}
		}
		else
		{
			content = Quest.getNoQuestMsg(player); // no quests found
		}

		if(qs != null)
		{
			// If the quest is already started, no need to show a window
			if(!qs.getQuest().notifyTalk(npc, qs))
			{
				return;
			}
		}

		// Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance
		if(content != null)
		{
			npc.insertObjectIdAndShowChatWindow(player, content);
		}

		// Send a Server->Client ActionFail to the L2PcInstance in order to avoid that the client wait another packet
		player.sendActionFailed();
	}

	/**
	 * Collect awaiting quests/start points and display a QuestChooseWindow (if several available) or QuestWindow.
	 * @param player the L2PcInstance that talk with the {@code npc}.
	 * @param npc the L2NpcInstance that chats with the {@code player}.
	 */
	public void showQuestWindow(L2PcInstance player, L2Npc npc)
	{
		// collect awaiting quests and start points
		List<Quest> options = new ArrayList<>();

		QuestState[] awaits = player.getQuestsForTalk(npc.getTemplate().getNpcId());
		List<Quest> starts = npc.getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);

		// Quests are limited between 1 and 999 because those are the quests that are supported by the client.
		// By limiting them there, we are allowed to create custom quests at higher IDs without interfering
		if(awaits != null)
		{
			for(QuestState x : awaits)
			{
				if(!options.contains(x.getQuest()))
				{
					if(x.getQuest().getQuestId() > 0 && x.getQuest().getQuestId() < 20000)
					{
						options.add(x.getQuest());
					}
				}
			}
		}

		if(starts != null)
		{
			starts.stream().filter(quest -> !options.contains(quest)).forEach(quest -> {
				if(quest.getQuestId() > 0 && quest.getQuestId() < 20000)
				{
					options.add(quest);
				}
			});
		}

		// Display a QuestChooseWindow (if several quests are available) or QuestWindow
		if(options.size() > 1)
		{
			showQuestChooseWindow(player, npc, options.toArray(new Quest[options.size()]));
		}
		else if(options.size() == 1)
		{
			showQuestWindow(player, npc, options.get(0).getQuestId());
		}
		else
		{
			showQuestWindow(player, npc, 0);
		}
	}
}