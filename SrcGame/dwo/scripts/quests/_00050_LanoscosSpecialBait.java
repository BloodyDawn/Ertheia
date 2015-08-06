package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00050_LanoscosSpecialBait extends Quest
{
	// NPC
	int Lanosco = 31570;
	int SingingWind = 21026;

	// Items
	int EssenceofWind = 7621;
	int WindFishingLure = 7610;

	public _00050_LanoscosSpecialBait()
	{
		addStartNpc(Lanosco);
		addTalkId(Lanosco);
		addKillId(SingingWind);
		questItemIds = new int[]{EssenceofWind};
	}

	public static void main(String[] args)
	{
		new _00050_LanoscosSpecialBait();
	}

	@Override
	public int getQuestId()
	{
		return 50;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		if(event.equals("31570-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("takeitem"))
		{
			if(st.getQuestItemsCount(EssenceofWind) < 100)
			{
				htmltext = "31570-07.htm";
			}
			else
			{
				htmltext = "31570-06.htm";
				st.unset("cond");
				st.takeItems(EssenceofWind, 100);
				st.giveItems(WindFishingLure, 4);
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
		if(npcId == SingingWind && st.getCond() == 1)
		{
			if(st.getQuestItemsCount(EssenceofWind) < 100 && Rnd.getChance(30))
			{
				st.giveItems(EssenceofWind, 1);
				if(st.getQuestItemsCount(EssenceofWind) == 100)
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

		if(npcId == Lanosco)
		{
			if(id == COMPLETED)
			{
				htmltext = getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
			}
			else if(cond == 1)
			{
				htmltext = "31570-05.htm";
			}
			else if(cond == 2)
			{
				htmltext = "31570-04.htm";
			}
			else if(cond == 0)
			{
				if(st.getPlayer().getLevel() > 26 && st.getPlayer().getLevel() < 30)
				{
					htmltext = "31570-01.htm";
				}
				else
				{
					htmltext = "31570-02.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
			}

		}
		return htmltext;
	}
}