package dwo.scripts.quests;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00627_HeartInSearchOfPower extends Quest
{
	// Квестовые персонажи
	private static final int M_NECROMANCER = 31518;
	private static final int ENFEUX = 31519;

	// Квестовые предметы
	private static final int SEAL_OF_LIGHT = 7170;
	private static final int GEM_OF_SUBMISSION = 7171;
	private static final int GEM_OF_SAINTS = 7172;

	// Квестовые награды
	private static final int MOLD_HARDENER = 4041;
	private static final int ENRIA = 4042;
	private static final int ASOFE = 4043;
	private static final int THONS = 4044;

	public _00627_HeartInSearchOfPower()
	{
		addStartNpc(31518);
		addTalkId(31518, 31519);
		for(int mobs = 21520; mobs <= 21541; mobs++)
		{
			addKillId(mobs);
		}

		questItemIds = new int[]{GEM_OF_SUBMISSION};
	}

	public static void main(String[] args)
	{
		new _00627_HeartInSearchOfPower();
	}

	@Override
	public int getQuestId()
	{
		return 627;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("31518-1.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("31518-3.htm"))
		{
			st.takeItems(GEM_OF_SUBMISSION, 300);
			st.giveItems(SEAL_OF_LIGHT, 1);
			st.setCond(3);
		}
		else if(event.equals("31519-1.htm"))
		{
			st.takeItems(SEAL_OF_LIGHT, 1);
			st.giveItems(GEM_OF_SAINTS, 1);
			st.setCond(4);
		}
		else if(event.equals("31518-5.htm") && st.hasQuestItems(GEM_OF_SAINTS))
		{
			st.takeItems(GEM_OF_SAINTS, 1);
			st.setCond(5);
		}
		else
		{
			switch(event)
			{
				case "31518-6.htm":
					st.giveAdena(100000, true);
					break;
				case "31518-7.htm":
					st.rewardItems(ASOFE, 13);
					st.giveAdena(6400, true);
					break;
				case "31518-8.htm":
					st.rewardItems(THONS, 13);
					st.giveAdena(6400, true);
					break;
				case "31518-9.htm":
					st.rewardItems(ENRIA, 6);
					st.giveAdena(13600, true);
					break;
				case "31518-10.htm":
					st.rewardItems(MOLD_HARDENER, 3);
					st.giveAdena(17200, true);
					break;
			}
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.REPEATABLE);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		long count = st.getQuestItemsCount(GEM_OF_SUBMISSION);
		if(st.getCond() == 1 && count < 300)
		{
			st.giveItems(GEM_OF_SUBMISSION, (long) Config.RATE_QUEST_DROP);
			count += 1 * Config.RATE_QUEST_DROP;
			if(count > 299)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
			else
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = getNoQuestMsg(st.getPlayer());
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == M_NECROMANCER)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 60)
				{
					htmltext = "31518-0.htm";
				}
				else
				{
					htmltext = "31518-0a.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
			}
			else if(cond == 1)
			{
				htmltext = "31518-1a.htm";
			}
			else if(st.getQuestItemsCount(GEM_OF_SUBMISSION) == 300)
			{
				htmltext = "31518-2.htm";
			}
			else if(st.getQuestItemsCount(GEM_OF_SAINTS) > 0)
			{
				htmltext = "31518-4.htm";
			}
			else if(cond == 5)
			{
				htmltext = "31518-5.htm";
			}
		}
		else if(npcId == ENFEUX && st.getQuestItemsCount(SEAL_OF_LIGHT) > 0)
		{
			htmltext = "31519-0.htm";
		}
		return htmltext;
	}
}