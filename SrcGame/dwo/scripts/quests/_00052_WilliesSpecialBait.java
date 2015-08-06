package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00052_WilliesSpecialBait extends Quest
{
	private static final int Willie = 31574;
	private static final int[] TarlkBasilisks = {20573, 20574};
	private static final int EyeOfTarlkBasilisk = 7623;
	private static final int EarthFishingLure = 7612;

	public _00052_WilliesSpecialBait()
	{
		addStartNpc(Willie);
		addTalkId(Willie);
		addKillId(TarlkBasilisks);
		questItemIds = new int[]{EyeOfTarlkBasilisk};
	}

	public static void main(String[] args)
	{
		new _00052_WilliesSpecialBait();
	}

	@Override
	public int getQuestId()
	{
		return 52;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		if(event.equals("31574-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("takeitem"))
		{
			if(st.getQuestItemsCount(EyeOfTarlkBasilisk) < 100)
			{
				htmltext = "31574-07.htm";
			}
			else
			{
				htmltext = "31574-06.htm";
				st.unset("cond");
				st.takeItems(EyeOfTarlkBasilisk, -1);
				st.giveItems(EarthFishingLure, 4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == TarlkBasilisks[0] || npcId == TarlkBasilisks[1] && st.getCond() == 1)
		{
			if(st.getQuestItemsCount(EyeOfTarlkBasilisk) < 100 && Rnd.getChance(30))
			{
				st.giveItems(EyeOfTarlkBasilisk, 1);
				if(st.getQuestItemsCount(EyeOfTarlkBasilisk) == 100)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(2);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		QuestStateType id = st.getState();
		String htmltext = getNoQuestMsg(st.getPlayer());

		if(npcId == Willie)
		{
			if(id == COMPLETED)
			{
				htmltext = getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
			}
			else if(cond == 1)
			{
				htmltext = "31574-05.htm";
			}
			else if(cond == 2)
			{
				htmltext = "31574-04.htm";
			}
			else if(cond == 0)
			{
				if(st.getPlayer().getLevel() > 47 && st.getPlayer().getLevel() < 51)
				{
					htmltext = "31574-01.htm";
				}
				else
				{
					htmltext = "31574-02.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
			}
		}
		return htmltext;
	}
}