package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.03.12
 * Time: 9:21
 */

public class _00140_ShadowFoxPart2 extends Quest
{
	// Квестовые персонажи
	private static final int KLUCK = 30895;
	private static final int XENOVIA = 30912;

	// Квестовые предметы
	private static final int CRYSTAL = 10347;
	private static final int OXYDE = 10348;
	private static final int CRYPT = 10349;

	// Квестовые монстры
	private static final int[] _mobs = {20789, 20790, 20791, 20792};

	public _00140_ShadowFoxPart2()
	{
		addFirstTalkId(KLUCK);
		addTalkId(KLUCK, XENOVIA);
		addKillId(_mobs);
		questItemIds = new int[]{CRYSTAL, OXYDE, CRYPT};
	}

	public static void main(String[] args)
	{
		new _00140_ShadowFoxPart2();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		switch(event)
		{
			case "30895-02.htm":
				st.startQuest();
				break;
			case "30895-05.htm":
				st.setCond(2);
				st.setState(STARTED);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30895-09.htm":
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.giveAdena(18775, true);
				st.addExpAndSp(30000, 2000);
				QuestState q = player.getQuestState(_00141_ShadowFox3.class);
				if(q == null)
				{
					q = newQuestState(player);
					q.setState(STARTED);
				}
				st.exitQuest(QuestType.ONE_TIME);
				break;
			case "30912-07.htm":
				st.setCond(3);
				st.setState(STARTED);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30912-09.htm":
				st.takeItems(CRYSTAL, 5);
				if(Rnd.getChance(60))
				{
					st.giveItems(OXYDE, 1);
					if(st.getQuestItemsCount(OXYDE) >= 3)
					{
						event = "30912-09b.htm";
						st.setCond(4);
						st.setState(STARTED);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						st.takeItems(CRYSTAL, -1);
						st.takeItems(OXYDE, -1);
						st.giveItems(CRYPT, 1);
					}
				}
				else
				{
					event = "30912-09a.htm";
				}
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(st.getCond() == 3 && Rnd.getChance(80))
		{
			st.giveItems(CRYSTAL, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == KLUCK)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() >= 37 ? "30895-01.htm" : "30895-00.htm";
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							return "30895-02.htm";
						case 2:
						case 3:
							return "30895-06.htm";
						case 4:
							if(st.getInt("talk") == 1)
							{
								return "30895-08.htm";
							}
							else
							{
								st.takeItems(CRYPT, -1);
								st.set("talk", "1");
								return "30895-07.htm";
							}
					}
					break;
			}
		}
		else if(npc.getNpcId() == XENOVIA)
		{
			switch(st.getCond())
			{
				case 2:
					return "30912-01.htm";
				case 3:
					return st.getQuestItemsCount(CRYSTAL) >= 5 ? "30912-08.htm" : "30912-07.htm";
				case 4:
					return "30912-10.htm";
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			st = newQuestState(player);
		}
		QuestState qs = player.getQuestState(_00139_ShadowFoxPart1.class);
		if(qs != null && qs.isCompleted() && st.isCreated())
		{
			st.setState(STARTED);
		}
		npc.showChatWindow(player);
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_00139_ShadowFoxPart1.class);
		QuestState st = player.getQuestState(getClass());
		return player.getLevel() >= 37 && previous != null && previous.isCompleted() && st != null && st.isCreated();

	}
}
